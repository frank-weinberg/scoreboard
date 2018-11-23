package com.carolinarollergirls.scoreboard.defaults;

import java.util.UUID;

import com.carolinarollergirls.scoreboard.event.DefaultScoreBoardEventProvider;
import com.carolinarollergirls.scoreboard.event.ScoreBoardEvent;
import com.carolinarollergirls.scoreboard.model.JamModel;
import com.carolinarollergirls.scoreboard.model.ScoreBoardModel;
import com.carolinarollergirls.scoreboard.model.TeamModel;
import com.carolinarollergirls.scoreboard.model.TimeoutModel;
import com.carolinarollergirls.scoreboard.utils.ScoreBoardClock;
import com.carolinarollergirls.scoreboard.view.Clock;
import com.carolinarollergirls.scoreboard.view.Jam;
import com.carolinarollergirls.scoreboard.view.ScoreBoard;
import com.carolinarollergirls.scoreboard.view.Timeout;

public class DefaultTimeoutModel extends DefaultScoreBoardEventProvider implements TimeoutModel {
    public DefaultTimeoutModel(JamModel jam) {
	this(UUID.randomUUID().toString(), jam, true, 0, 0, 0, 0, 0);
    }
    public DefaultTimeoutModel(String id, JamModel jam, boolean cur, long dur, long pcStart,
	    long pcEnd, long wallStart, long wallEnd) {
	this.id = id;
	isCurrent = cur;
	duration = dur;
	periodClockElapsedStart = pcStart;
	periodClockElapsedEnd = pcEnd;
	walltimeStart = wallStart;
	walltimeEnd = wallEnd;
	precedingJamModel = jam;
	precedingJamModel.addTimeoutAfter(this);
	scoreBoardModel = jam.getScoreBoardModel();
	owner = scoreBoardModel.getTimeoutOwner(TimeoutOwner.NONE);
	scoreBoardModel.registerTimeoutModel(this);
    }
    
    public String getProviderName() { return "Timeout"; }
    public Class<Timeout> getProviderClass() { return Timeout.class; }
    public String getProviderId() { return getId(); }

    public String getId() { return id; }
    public String toString() { return getId(); }
    
    public TimeoutSnapshotModel snapshot() { return new DefaultTimeoutSnapshotModel(this); }
    public void restoreSnapshot(TimeoutSnapshotModel snapshot) {
	synchronized (coreLock) {
	    requestBatchStart();
	    duration = snapshot.getDuration();
	    periodClockElapsedEnd = snapshot.getPeriodClockElapsedEnd();
	    walltimeEnd = snapshot.getWalltimeEnd();
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_DURATION, duration, 0));
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_WALLTIME_START, walltimeEnd, 0));
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_PERIOD_CLOCK_START, periodClockElapsedEnd, 0));
	    requestBatchEnd();
	}
    }
    
    public Jam getPrecedingJam() { return precedingJamModel; }
    public JamModel getPrecedingJamModel() { return precedingJamModel; }
    public void setPrecedingJamModel(JamModel j) {
	synchronized (coreLock) {
	    if (j == precedingJamModel) { return; }
	    requestBatchStart();
	    precedingJamModel = j;
	    precedingJamModel.addTimeoutAfter(this);
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_PRECEDING_JAM, precedingJamModel, null));
	    requestBatchEnd();
	}
    }

    public void unlink() {
	synchronized (coreLock) {
	    requestBatchStart();
	    precedingJamModel.removeTimeoutAfter(this);
	    if (owner instanceof TeamModel) {
		((TeamModel)owner).removeTimeoutModel(this);
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
    
    public long getDuration() { return duration; }
    public long getPeriodClockElapsedStart() { return periodClockElapsedStart; }
    public long getPeriodClockElapsedEnd() { return periodClockElapsedEnd; }
    public long getWalltimeStart() { return walltimeStart; }
    public long getWalltimeEnd() { return walltimeEnd; }
    public boolean isRunning() { return (walltimeEnd == 0); }
    public void stop() {
	synchronized (coreLock) {
	    requestBatchStart();
	    duration = scoreBoardModel.getClock(Clock.ID_TIMEOUT).getTimeElapsed();
	    periodClockElapsedEnd = scoreBoardModel.getClock(Clock.ID_PERIOD).getTimeElapsed();
	    walltimeEnd = ScoreBoardClock.getInstance().getCurrentWalltime();
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_DURATION, duration, 0));
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_WALLTIME_START, walltimeEnd, 0));
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_PERIOD_CLOCK_START, periodClockElapsedEnd, 0));
	    if (owner != scoreBoardModel.getTimeoutOwner()) {
		scoreBoardChange(new ScoreBoardEvent(scoreBoardModel, ScoreBoard.EVENT_TIMEOUT_OWNER, scoreBoardModel.getTimeoutOwner(), owner));
	    }
	    if (review != scoreBoardModel.isOfficialReview()) {
		scoreBoardChange(new ScoreBoardEvent(scoreBoardModel, ScoreBoard.EVENT_OFFICIAL_REVIEW, scoreBoardModel.isOfficialReview(), review));
	    }
	    requestBatchEnd();
	}
    }

    public TimeoutOwner getOwner() { return owner; }
    public void setOwner(TimeoutOwner o) {
	synchronized (coreLock) {
	    if (o == owner) { return; }
	    requestBatchStart();
	    if (owner instanceof TeamModel) {
		((TeamModel)owner).removeTimeoutModel(this);
	    }
	    TimeoutOwner last = owner;
	    owner = o;
	    if (owner instanceof TeamModel) {
		((TeamModel)owner).addTimeoutModel(this);
	    }
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_OWNER, owner, last));
	    if (isRunning()) {
		scoreBoardChange(new ScoreBoardEvent(scoreBoardModel, ScoreBoard.EVENT_TIMEOUT_OWNER, owner, last));
	    }
	    requestBatchEnd();
	}
    }
    
    public boolean isOfficialReview() { return review; }
    public void setOfficialReview(boolean r) {
	synchronized (coreLock) {
	    if (r == review) { return; }
	    requestBatchStart();
	    boolean last = review;
	    review = r;
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_REVIEW, review, last));
	    if (isRunning()) {
		scoreBoardChange(new ScoreBoardEvent(scoreBoardModel, ScoreBoard.EVENT_OFFICIAL_REVIEW, review, last));
	    }
	    requestBatchEnd();
	}
    }
    
    private JamModel precedingJamModel;

    private ScoreBoardModel scoreBoardModel; 
    
    protected static Object coreLock = DefaultScoreBoardModel.getCoreLock();
    
    private String id;

    private boolean isCurrent;
    
    private TimeoutOwner owner;
    private boolean review = false;
    
    private long duration = 0;
    private long periodClockElapsedStart;
    private long periodClockElapsedEnd = 0;
    private long walltimeStart = 0;
    private long walltimeEnd = 0;

    public static class DefaultTimeoutSnapshotModel implements TimeoutSnapshotModel {
	private DefaultTimeoutSnapshotModel(DefaultTimeoutModel timeout) {
	    duration = timeout.getDuration();
	    periodClockElapsedEnd = timeout.getPeriodClockElapsedEnd();
	    walltimeEnd = timeout.getWalltimeEnd();
	}

	public long getDuration() { return duration; }
	public long getPeriodClockElapsedEnd() { return periodClockElapsedEnd; }
	public long getWalltimeEnd() { return walltimeEnd; }
	
	private long duration;
	private long periodClockElapsedEnd;
	private long walltimeEnd;
    }
}
