package com.carolinarollergirls.scoreboard.defaults;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.carolinarollergirls.scoreboard.event.DefaultScoreBoardEventProvider;
import com.carolinarollergirls.scoreboard.event.ScoreBoardEvent;
import com.carolinarollergirls.scoreboard.model.JamModel;
import com.carolinarollergirls.scoreboard.model.JamModel.JamSnapshotModel;
import com.carolinarollergirls.scoreboard.model.TimeoutModel.TimeoutSnapshotModel;
import com.carolinarollergirls.scoreboard.utils.Comparators;
import com.carolinarollergirls.scoreboard.utils.ScoreBoardClock;
import com.carolinarollergirls.scoreboard.model.PeriodModel;
import com.carolinarollergirls.scoreboard.model.ScoreBoardModel;
import com.carolinarollergirls.scoreboard.model.TimeoutModel;
import com.carolinarollergirls.scoreboard.view.Clock;
import com.carolinarollergirls.scoreboard.view.Jam;
import com.carolinarollergirls.scoreboard.view.Period;
import com.carolinarollergirls.scoreboard.view.ScoreBoard;
import com.carolinarollergirls.scoreboard.view.Timeout;

public class DefaultPeriodModel extends DefaultScoreBoardEventProvider implements PeriodModel {
    public DefaultPeriodModel(ScoreBoardModel sb) {
	this.id = "Period0";
	scoreBoardModel = sb;
	previous = null;
        updateNumber();
        currentJam = new DefaultJamModel(this, null);
        addJam(currentJam);
        currentTimeout = null;
        setCurrent(true);
        scoreBoardModel.registerPeriod(this);
    }
    public DefaultPeriodModel(PeriodModel prev) {
	id = UUID.randomUUID().toString();
	scoreBoardModel = prev.getScoreBoardModel();
	setPrevious(prev);
	prev.getLastJamModel().setPeriodModel(this);
	currentJam = previous.getCurrentJamModel();
	currentTimeout = previous.getCurrentTimeoutModel();
        scoreBoardModel.registerPeriod(this);
    }
    public DefaultPeriodModel(String id, PeriodModel prev, PeriodModel next, long walltimeEnd) {
	this.id = id;
	setWalltimeEnd(walltimeEnd);
        setPrevious(prev);
        setNext(next);
        scoreBoardModel.registerPeriod(this);
    }

    public String getId() { return id; }
    public String toString() { return getId(); }

    public ScoreBoardModel getScoreBoardModel() { return scoreBoardModel; }
    
    public String getProviderName() { return "Period"; }
    public Class<Period> getProviderClass() { return Period.class; }
    public String getProviderId() { return getId(); }

    public PeriodSnapshotModel snapshot() { return new DefaultPeriodSnapshotModel(this); }
    public void restoreSnapshot(PeriodSnapshotModel snapshot) {
	synchronized (coreLock) {
	    requestBatchStart();
	    Clock jc = scoreBoardModel.getClock(Clock.ID_JAM);
	    int lastJcNumber = jc.getNumber();
	    setRunning(snapshot.isRunning());
	    setCurrent(snapshot.isCurrent());
	    setWalltimeEnd(snapshot.getWalltimeEnd());
	    currentJam = snapshot.getCurrentJam();
	    getCurrentJamModel().restoreSnapshot(snapshot.getCurrentJamSnapshot());
	    if (snapshot.getNextJamSnapshot() != null) {
		getCurrentJamModel().getNext().restoreSnapshot(snapshot.getNextJamSnapshot());
	    }
	    while (timeouts.size() > 0 && (snapshot.getCurrentTimeout() == null ||
		    Comparators.TimeoutComparator.compare(getCurrentTimeout(), snapshot.getCurrentTimeout()) > 0)) {
		scoreBoardModel.deleteTimeoutModel(getCurrentTimeoutModel());
	    }
	    if (snapshot.getCurrentTimeoutSnapshot() != null) {
		getCurrentTimeoutModel().restoreSnapshot(snapshot.getCurrentTimeoutSnapshot());
	    }
	    if (jc.getNumber() != lastJcNumber) {
		scoreBoardChange(new ScoreBoardEvent(jc, EVENT_NUMBER, jc.getNumber(), lastJcNumber));
	    }
	    requestBatchEnd();
	}
    }
    
    public void unlink() {
	synchronized (coreLock) {
	    requestBatchStart();
	    cleanup = true;
	    for (TimeoutModel t : new ArrayList<TimeoutModel>(timeouts)) {
		scoreBoardModel.deleteTimeoutModel(t);
	    }
	    for (JamModel j : new ArrayList<JamModel>(jams)) {
		scoreBoardModel.deleteJamModel(j);
	    }
	    if (next != null) {
		next.setPrevious(previous);
		
		//in WS/JSON all following periods will overwrite their predecessor's old entry
		//due to renumbering. This ensures the old entry for the last period is removed
		number = scoreBoardModel.getPeriods().size();
	    } else {
		previous.setNext(null);
	    }
	    requestBatchEnd();
	}
    }
    
    public PeriodModel getPrevious() { return previous; }
    public void setPrevious(PeriodModel p) {
        synchronized (coreLock) {
	    if (p == previous) { return; }
            requestBatchStart();
            previous = p;
            if (previous != null) { previous.setNext(this); }
            updateNumber();
            if (jams.size() > 0 && previous != null) {
        	getFirstJamModel().setPrevious(previous.getLastJamModel());
            }
            scoreBoardChange(new ScoreBoardEvent(this, EVENT_PREVIOUS, previous, null));
            requestBatchEnd();
        }
    }
    public PeriodModel getNext() { return next; }
    public void setNext(PeriodModel n) {
        synchronized (coreLock) {
            if (n == next) { return; }
            requestBatchStart();
            next = n;
            if (next != null) { next.setPrevious(this); }
            scoreBoardChange(new ScoreBoardEvent(this, EVENT_NEXT, next, null));
            requestBatchEnd();
        }
    }

    public int getNumber() { return number; }
    public void updateNumber() {
        synchronized (coreLock) {
            requestBatchStart();
            int last = number;
            if (previous == null) {
                number = 0;
            } else {
                number = previous.getNumber() + 1;
            }
            if (number != last) {
        	if (next != null) {
        	    next.updateNumber();
        	}
        	scoreBoardChange(new ScoreBoardEvent(this, EVENT_NUMBER, number, last));
            }
	    requestBatchEnd();
        }
    }

    public boolean isCurrent() { return isCurrent; }
    public void setCurrent(boolean c) {
	synchronized (coreLock) {
	    if (c == isCurrent) { return; }
	    requestBatchStart();
	    boolean last = isCurrent;
	    isCurrent = c;
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_CURRENT, isCurrent, last));
	    requestBatchEnd();
	}
    }
    
    public boolean isRunning() { return isRunning; }
    public void setRunning(boolean r) {
	synchronized (coreLock) {
	    if (r == isRunning) { return; }
	    requestBatchStart();
	    boolean last = isRunning;
	    isRunning = r;
	    if (!isRunning) {
		setWalltimeEnd(ScoreBoardClock.getInstance().getCurrentWalltime());
	    }
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_RUNNING, isRunning, last));
	    scoreBoardChange(new ScoreBoardEvent(scoreBoardModel, ScoreBoard.EVENT_IN_PERIOD, isRunning, last));
	    requestBatchEnd();
	}
    }
    
    public List<Timeout> getTimeouts() { return new ArrayList<Timeout>(timeouts); }
    public List<TimeoutModel> getTimeoutModels() { return timeouts; }
    public Timeout getCurrentTimeout() { return getCurrentTimeoutModel(); }
    public TimeoutModel getCurrentTimeoutModel() { return currentTimeout; }
    public void removeTimeout(TimeoutModel t) {
	synchronized (coreLock) {
	    if (!timeouts.contains(t)) { return; }
	    requestBatchStart();
	    Clock tc = scoreBoardModel.getClock(Clock.ID_TIMEOUT);
	    int last = tc.getNumber();
	    timeouts.remove(t);
	    if (t == currentTimeout && !cleanup) {
		if (timeouts.size() > 0) {
		    currentTimeout = timeouts.get(timeouts.size()-1);
		} else if (previous != null) {
		    currentTimeout = previous.getCurrentTimeoutModel();
		} else {
		    currentTimeout = null;
		}
	    }
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_REMOVE_TIMEOUT, t, null));
	    scoreBoardChange(new ScoreBoardEvent(tc, Clock.EVENT_NUMBER, tc.getNumber(), last));
	    requestBatchEnd();
	}
    }
    public void addTimeout(TimeoutModel t) {
	synchronized (coreLock) {
	    if (timeouts.contains(t)) { return; }
	    requestBatchStart();
	    Clock tc = scoreBoardModel.getClock(Clock.ID_TIMEOUT);
	    int last = tc.getNumber();
	    timeouts.add(t);
	    if (t.isCurrent()) { 
		currentTimeout = t; 
	    } else {
		Collections.sort(timeouts, Comparators.TimeoutComparator);
	    }
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_ADD_TIMEOUT, t, null));
	    scoreBoardChange(new ScoreBoardEvent(tc, Clock.EVENT_NUMBER, tc.getNumber(), last));
	    requestBatchEnd();
	}
    }
    public void addTimeout() { addTimeout(new DefaultTimeoutModel(getCurrentJamModel())); }
    
    public List<Jam> getJams() { return new ArrayList<Jam>(jams); }
    public List<JamModel> getJamModels() { return jams; }
    public Jam getCurrentJam() { return getCurrentJamModel(); }
    public JamModel getCurrentJamModel() { return currentJam; }
    public JamModel getFirstJamModel() { return jams.get(0); }
    public JamModel getLastJamModel() { return jams.get(jams.size()-1); }
    public void removeJam(JamModel j) {
	synchronized (coreLock) {
	    if (!jams.contains(j)) { return; }
	    requestBatchStart();
	    Clock jc = scoreBoardModel.getClock(Clock.ID_JAM);
	    int lastJcNumber = jc.getNumber();
	    jams.remove(j);
	    if (j == currentJam && !cleanup) { currentJam = j.getNext(); }
	    if (!cleanup && currentJam.getNext() == null) { addJam(); }
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_REMOVE_JAM, j, null));
	    if (lastJcNumber != jc.getNumber() ) {
		scoreBoardChange(new ScoreBoardEvent(jc, EVENT_NUMBER, jc.getNumber(), lastJcNumber));
	    }
	    requestBatchEnd();
	}
    }
    public void addJam(JamModel j) {
	synchronized (coreLock) {
	    if (jams.contains(j)) { return; }
	    requestBatchStart();
	    Clock jc = scoreBoardModel.getClock(Clock.ID_JAM);
	    int lastJcNumber = jc.getNumber();
	    jams.add(j);
	    if (j.isCurrent()) { currentJam = j; }
	    if (j.getNext() != null) {
		Collections.sort(jams, Comparators.JamComparator);
	    }
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_ADD_JAM, j, null));
	    if (lastJcNumber != jc.getNumber() ) {
		scoreBoardChange(new ScoreBoardEvent(jc, EVENT_NUMBER, jc.getNumber(), lastJcNumber));
	    }
	    requestBatchEnd();
	}
    }
    public void addJam() { addJam(new DefaultJamModel(this, getLastJamModel())); }

    public void startNextJam() {
	synchronized (coreLock) {
	    requestBatchStart();
	    Clock jc = scoreBoardModel.getClock(Clock.ID_JAM);
	    int last = jc.getNumber();
	    currentJam.setCurrent(false);
	    currentJam = currentJam.getNext();
	    currentJam.setCurrent(true);
	    if (currentJam.getNext() == null) {
		addJam();
	    }
	    currentJam.start();
	    if (currentJam.getPrevious().getPeriod() != this) {
		scoreBoardChange(new ScoreBoardEvent(this, EVENT_WALLTIME_START, getWalltimeStart(), 0));
		if (getNumber() == 1) {
		    scoreBoardChange(new ScoreBoardEvent(scoreBoardModel, ScoreBoard.EVENT_GAME_WALLTIME_START, getWalltimeStart(), 0));
		}
	    }
	    if (jc.getNumber() != last) {
		scoreBoardChange(new ScoreBoardEvent(jc, Clock.EVENT_NUMBER, jc.getNumber(), last));
	    }
	    requestBatchEnd();
	}
    }
    public void stopCurrentJam() {
	synchronized (coreLock) {
	    currentJam.stop();
	}
    }
    
    public long getDuration() { return getWalltimeEnd() - getWalltimeStart(); }
    public long getWalltimeStart() {
	if (cleanup) { return 0; }
	return getFirstJamModel().getWalltimeStart(); }
    public long getWalltimeEnd() { return walltimeEnd; }
    private void setWalltimeEnd(long t) {
	synchronized (coreLock) {
	    if (t == walltimeEnd) { return; }
	    requestBatchStart();
	    long last = walltimeEnd;
	    walltimeEnd = t;
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_WALLTIME_END, walltimeEnd, last));
	    requestBatchEnd();
	}
    }

    private ScoreBoardModel scoreBoardModel; 
    
    private static Object coreLock = DefaultScoreBoardModel.getCoreLock();

    private PeriodModel previous, next;
    
    private JamModel currentJam;
    private TimeoutModel currentTimeout;
    
    private String id;
    private int number;
    private boolean isRunning = false;
    private boolean isCurrent = false;
    private long walltimeEnd = 0;
    private List<TimeoutModel> timeouts = new ArrayList<TimeoutModel>();
    private List<JamModel> jams = new ArrayList<JamModel>();
    
    private boolean cleanup = false;

    public static class DefaultPeriodSnapshotModel implements PeriodSnapshotModel {
	private DefaultPeriodSnapshotModel(DefaultPeriodModel period) {
	    isCurrent = period.isCurrent();
	    isRunning = period.isRunning();
	    walltimeEnd = period.getWalltimeEnd();
	    currentJam = period.getCurrentJamModel();
	    currentJamSnapshot = currentJam.snapshot();
	    JamModel next = period.getCurrentJamModel().getNext();
	    nextJamSnapshot = (next == null) ? null : next.snapshot();
	    currentTimeout = period.getCurrentTimeoutModel();
	    if (currentTimeout != null) {
		currentTimeoutSnapshot = currentTimeout.snapshot();
	    }
	}
	
	public boolean isCurrent() { return isCurrent; }
	public boolean isRunning() { return isRunning; }
	public long getWalltimeEnd() { return walltimeEnd; }
	public JamModel getCurrentJam() { return currentJam; }
	public JamSnapshotModel getCurrentJamSnapshot() { return currentJamSnapshot; }
	public JamSnapshotModel getNextJamSnapshot() { return nextJamSnapshot; }
	public TimeoutModel getCurrentTimeout() { return currentTimeout; }
	public TimeoutSnapshotModel getCurrentTimeoutSnapshot() { return currentTimeoutSnapshot; }
	
	private boolean isCurrent;
	private boolean isRunning;
	private long walltimeEnd;
	private JamModel currentJam;
	private JamSnapshotModel currentJamSnapshot;
	private JamSnapshotModel nextJamSnapshot;
	private TimeoutModel currentTimeout;
	private TimeoutSnapshotModel currentTimeoutSnapshot;
    }
}
