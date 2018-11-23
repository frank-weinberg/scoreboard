package com.carolinarollergirls.scoreboard.defaults;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import com.carolinarollergirls.scoreboard.event.DefaultScoreBoardEventProvider;
import com.carolinarollergirls.scoreboard.event.ScoreBoardEvent;
import com.carolinarollergirls.scoreboard.model.JamModel;
import com.carolinarollergirls.scoreboard.model.PenaltyModel;
import com.carolinarollergirls.scoreboard.model.PeriodModel;
import com.carolinarollergirls.scoreboard.model.ScoreBoardModel;
import com.carolinarollergirls.scoreboard.model.TeamJamModel;
import com.carolinarollergirls.scoreboard.model.TeamModel;
import com.carolinarollergirls.scoreboard.model.TimeoutModel;
import com.carolinarollergirls.scoreboard.utils.ScoreBoardClock;
import com.carolinarollergirls.scoreboard.view.Clock;
import com.carolinarollergirls.scoreboard.view.Jam;
import com.carolinarollergirls.scoreboard.view.Penalty;
import com.carolinarollergirls.scoreboard.view.Period;
import com.carolinarollergirls.scoreboard.view.ScoreBoard;
import com.carolinarollergirls.scoreboard.view.Team;
import com.carolinarollergirls.scoreboard.view.TeamJam;
import com.carolinarollergirls.scoreboard.view.Timeout;

public class DefaultJamModel extends DefaultScoreBoardEventProvider implements JamModel {
    public DefaultJamModel(PeriodModel per, JamModel prev) {
	this((prev == null) ? "Jam0" : UUID.randomUUID().toString(),
		per.getScoreBoardModel(), per, prev, null, 0, 0, 0, 0, 0);
    }
    public DefaultJamModel(String id, ScoreBoardModel sbm, PeriodModel per, JamModel prev, JamModel next, long dur, long pcStart,
	    long pcEnd, long wallStart, long wallEnd) {
	this.id = id;
	periodModel = per;
	duration = dur;
	periodClockElapsedStart = pcStart;
	periodClockElapsedEnd = pcEnd;
	walltimeStart = wallStart;
	walltimeEnd = wallEnd;
	scoreBoardModel = sbm;
	teamJams = new HashMap<Team, TeamJamModel>();
	for (TeamModel t : scoreBoardModel.getTeamModels()) {
	    teamJams.put(t, new DefaultTeamJamModel(getId() + "_" + t.getId(), this, t));
	}
	setPrevious(prev);
	setNext(next);
	scoreBoardModel.registerJamModel(this);
    }

    public ScoreBoardModel getScoreBoardModel() { return scoreBoardModel; }
    
    public String getProviderName() { return "Jam"; }
    public Class<Jam> getProviderClass() { return Jam.class; }
    public String getProviderId() { return getId(); }

    public JamSnapshotModel snapshot() { return new DefaultJamSnapshotModel(this); }
    public void restoreSnapshot(JamSnapshotModel snapshot) {
	synchronized (coreLock) {
	    requestBatchStart();
	    setCurrent(snapshot.isCurrent());
	    duration = snapshot.getDuration();
	    periodClockElapsedStart = snapshot.getPeriodClockElapsedStart();
	    periodClockElapsedEnd = snapshot.getPeriodClockElapsedEnd();
	    walltimeStart = snapshot.getWalltimeStart();
	    walltimeEnd = snapshot.getWalltimeEnd();
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_DURATION, duration, null));
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_PERIOD_CLOCK_START, periodClockElapsedStart, null));
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_PERIOD_CLOCK_END, periodClockElapsedEnd, null));
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_WALLTIME_START, walltimeStart, null));
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_WALLTIME_END, walltimeEnd, null));
	    requestBatchEnd();
	}
    }

    public String getId() { return id; }
    public String toString() { return getId(); }
    
    public void unlink() {
	synchronized (coreLock) {
	    requestBatchStart();
	    for (TimeoutModel t : new ArrayList<TimeoutModel>(timeoutsAfter)) {
		previous.addTimeoutAfter(t);
		t.setPrecedingJamModel(previous);
	    }
	    for (PenaltyModel p : new ArrayList<PenaltyModel>(penalties)) {
		p.setJamModel((next != null) ? next : previous);
	    }
	    for (Team t : scoreBoardModel.getTeams()) {
		scoreBoardModel.deleteTeamJamModel(teamJams.get(t));
	    }
	    if (next != null) {
		next.setPrevious(previous);
		
		//in WS/JSON all following jams will overwrite their predecessor's old entry
		//due to renumbering. This ensures the old entry for the last jam is removed
		number = getPeriod().getJams().size();
	    } else {
		previous.setNext(null);
	    }
	    periodModel.removeJam(this);
	    requestBatchEnd();
	}
    }

    public Period getPeriod() { return periodModel; }
    public PeriodModel getPeriodModel() { return periodModel; }
    public void setPeriodModel(PeriodModel p) {
	synchronized (coreLock) {
	    if (p == periodModel) { return; }
	    requestBatchStart();
	    periodModel.removeJam(this);
	    periodModel = p;
	    periodModel.addJam(this);
	    updateNumber();
	    requestBatchEnd();
	}
    }

    public JamModel getPrevious() { return previous; }
    public void setPrevious(JamModel p) {
	synchronized (coreLock) {
	    if (p == previous) { return; }
	    requestBatchStart();
	    previous = p;
	    if (previous != null) { previous.setNext(this); }
	    updateNumber();
	    for (TeamJamModel t : teamJams.values()) {
		t.setPrevious(previous == null ? null : previous.getTeamJamModel(t.getTeam()));
	    }
            scoreBoardChange(new ScoreBoardEvent(this, EVENT_PREVIOUS, previous, null));
	    requestBatchEnd();
	}
    }
    public JamModel getNext() { return next; }
    public void setNext(JamModel n) {
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
	    } else if (previous.getPeriod() != this.getPeriod() &&
		    scoreBoardModel.getSettings().getBoolean(ScoreBoard.SETTING_JAM_NUMBER_PER_PERIOD)) {
		number = 1;
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
    
    public void start() {
	synchronized (coreLock) {
	    requestBatchStart();
	    periodClockElapsedStart = scoreBoardModel.getClock(Clock.ID_PERIOD).getTimeElapsed();
	    walltimeStart = ScoreBoardClock.getInstance().getCurrentWalltime();
	    for (TeamJamModel tj : teamJams.values()) {
		tj.start();
	    }
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_WALLTIME_START, walltimeStart, 0));
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_PERIOD_CLOCK_START, periodClockElapsedStart, 0));
	    requestBatchEnd();
	}
    }
    public void stop() {
	synchronized (coreLock) {
	    requestBatchStart();
	    duration = scoreBoardModel.getClock(Clock.ID_JAM).getTimeElapsed();
	    periodClockElapsedEnd = scoreBoardModel.getClock(Clock.ID_PERIOD).getTimeElapsed();
	    walltimeEnd = ScoreBoardClock.getInstance().getCurrentWalltime();
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_DURATION, duration, 0));
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_WALLTIME_END, walltimeEnd, 0));
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_PERIOD_CLOCK_END, periodClockElapsedEnd, 0));
	    requestBatchEnd();
	}
    }

    public boolean isRunning() {
	return (walltimeEnd == 0 && walltimeStart > 0);
    }

    public boolean getInjury() { return injury; }
    public void setInjury(boolean i) {
	synchronized (coreLock) {
	    if (i == injury) { return; }
	    requestBatchStart();
	    boolean last = injury; 
	    injury = i;
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_INJURY, injury, last));
	    if (isCurrent()) {
		for (Team t : scoreBoardModel.getTeams()) {
		    scoreBoardChange(new ScoreBoardEvent(t, Team.EVENT_INJURY, injury, last));
		}
	    }
	    requestBatchEnd();
	}
    }

    public long getDuration() { return duration; }
    public long getPeriodClockElapsedStart() { return periodClockElapsedStart; }
    public long getPeriodClockElapsedEnd() { return periodClockElapsedEnd; }
    public long getWalltimeStart() { return walltimeStart; }
    public long getWalltimeEnd() { return walltimeEnd; }

    public List<TeamJam> getTeamJams() { return new ArrayList<TeamJam>(teamJams.values()); }
    public List<TeamJamModel> getTeamJamModels() { return new ArrayList<TeamJamModel>(teamJams.values()); }
    public TeamJam getTeamJam(Team t) { return getTeamJamModel(t); }
    public TeamJamModel getTeamJamModel(Team t) { return teamJams.get(t); }

    public List<Penalty> getPenalties() { return new ArrayList<Penalty>(penalties); }  
    public Collection<PenaltyModel> getPenaltyModels() { return penalties; }
    public void addPenaltyModel(PenaltyModel p) {
	synchronized (coreLock) {
	    if (penalties.contains(p)) { return; }
	    requestBatchStart();
	    penalties.add(p);
	    p.setJamModel(this);
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_ADD_PENALTY, p, null));
	    requestBatchEnd();
	}
    }
    public void removePenaltyModel(PenaltyModel p) {
	synchronized (coreLock) {
	    if (!penalties.contains(p)) { return; }
	    requestBatchStart();
	    penalties.remove(p);
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_REMOVE_PENALTY, p, null));
	    requestBatchEnd();
	}
    }
    
    public List<Timeout> getTimeoutsAfter() { return new ArrayList<Timeout>(timeoutsAfter); }
    public Collection<TimeoutModel> getTimeoutModelsAfter() { return timeoutsAfter; }
    public void addTimeoutAfter(TimeoutModel t) {
	synchronized (coreLock) {
	    if (timeoutsAfter.contains(t)) { return; }
	    requestBatchStart();
	    timeoutsAfter.add(t);
	    t.setPrecedingJamModel(this);
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_ADD_TIMEOUT, t, null));
	    requestBatchEnd();
	}
    }
    public void removeTimeoutAfter(TimeoutModel t) {
	synchronized (coreLock) {
	    if (!timeoutsAfter.contains(t)) { return; }
	    requestBatchStart();
	    timeoutsAfter.remove(t);
	    periodModel.removeTimeout(t);
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_REMOVE_TIMEOUT, t, null));
	    requestBatchEnd();
	}
    }

    private PeriodModel periodModel;
    private JamModel previous, next;

    private ScoreBoardModel scoreBoardModel; 
    
    private static Object coreLock = DefaultScoreBoardModel.getCoreLock();
    
    private String id;
    
    private int number;
    private boolean isCurrent;
    private boolean injury;
    private long duration;
    private long periodClockElapsedStart;
    private long periodClockElapsedEnd;
    private long walltimeStart;
    private long walltimeEnd;
    private HashMap<Team,TeamJamModel> teamJams;
    private Collection<TimeoutModel> timeoutsAfter = new HashSet<TimeoutModel>();
    private Collection<PenaltyModel> penalties = new HashSet<PenaltyModel>();

    public static class DefaultJamSnapshotModel implements JamSnapshotModel {
	private DefaultJamSnapshotModel(DefaultJamModel jam) {
	    isCurrent = jam.isCurrent();
	    duration = jam.getDuration();
	    periodClockElapsedStart = jam.getPeriodClockElapsedStart();
	    periodClockElapsedEnd = jam.getPeriodClockElapsedEnd();
	    walltimeStart = jam.getWalltimeStart();
	    walltimeEnd = jam.getWalltimeEnd();
	}

	public boolean isCurrent() { return isCurrent; }
	public long getDuration() { return duration; }
	public long getPeriodClockElapsedStart() { return periodClockElapsedStart; }
	public long getPeriodClockElapsedEnd() { return periodClockElapsedEnd; }
	public long getWalltimeStart() { return walltimeStart; }
	public long getWalltimeEnd() { return walltimeEnd; }
	
	private boolean isCurrent;
	private long duration;
	private long periodClockElapsedStart;
	private long periodClockElapsedEnd;
	private long walltimeStart;
	private long walltimeEnd;
    }
}
