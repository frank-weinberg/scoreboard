package com.carolinarollergirls.scoreboard.defaults;

import java.util.UUID;

import com.carolinarollergirls.scoreboard.event.DefaultScoreBoardEventProvider;
import com.carolinarollergirls.scoreboard.event.ScoreBoardEvent;
import com.carolinarollergirls.scoreboard.model.ScoreBoardModel;
import com.carolinarollergirls.scoreboard.model.ScoringTripModel;
import com.carolinarollergirls.scoreboard.model.TeamJamModel;
import com.carolinarollergirls.scoreboard.utils.ScoreBoardClock;
import com.carolinarollergirls.scoreboard.view.Clock;
import com.carolinarollergirls.scoreboard.view.ScoringTrip;
import com.carolinarollergirls.scoreboard.view.TeamJam;

public class DefaultScoringTripModel extends DefaultScoreBoardEventProvider implements ScoringTripModel {
    public DefaultScoringTripModel(TeamJamModel t, ScoringTripModel prev) {
	this(UUID.randomUUID().toString(), t, prev, null, prev == null ? false : prev.isAfterSP());
    }
    public DefaultScoringTripModel(String id, TeamJamModel t, ScoringTripModel prev, ScoringTripModel next, boolean afterSP) {
	this.id = id;
	teamJamModel = t;
	scoreBoardModel = t.getScoreBoardModel();
	isAfterSP = afterSP;
	setPrevious(prev);
	setNext(next);
	scoreBoardModel.registerScoringTripModel(this);
    }
    
    public TeamJam getTeamJam() { return teamJamModel; }
    public TeamJamModel getTeamJamModel() { return teamJamModel; }
    
    public String getProviderName() { return "ScoringTrip"; }
    public Class<ScoringTrip> getProviderClass() { return ScoringTrip.class; }
    public String getProviderId() { return getId(); }

    public String getId() { return id; }
    public String toString() { return getId(); }
    
    public void unlink() {
	synchronized (coreLock) {
	    requestBatchStart();
	    teamJamModel.removeScoringTripModel(this);
	    if (next != null) {
		next.setPrevious(previous);
		
		//in WS/JSON all following trips will overwrite their predecessor's old entry
		//due to renumbering. This ensures the old entry for the last trip is removed
		number = getTeamJam().getScoringTrips().size();
	    } else if (previous != null) {
		previous.setNext(null);
	    }
	    requestBatchEnd();
	}
    }

    public ScoringTripModel getPrevious() { return previous; }
    public void setPrevious(ScoringTripModel p) {
	synchronized (coreLock) {
	    if (p == previous) { return; }
	    requestBatchStart();
	    previous = p;
	    if (previous != null) { previous.setNext(this); }
	    updateNumber();
            scoreBoardChange(new ScoreBoardEvent(this, EVENT_PREVIOUS, previous, null));
	    requestBatchEnd();
	}
    }
    public ScoringTripModel getNext() { return next; }
    public void setNext(ScoringTripModel n) {
	synchronized (coreLock) {
            if (n == next) { return; }
            requestBatchStart();
            next = n;
            if (next != null) { next.setPrevious(this); }
            scoreBoardChange(new ScoreBoardEvent(this, EVENT_NEXT, next, null));
            requestBatchEnd();
	}
    }

    public boolean isAfterSP() { return isAfterSP; }
    public void setAfterSP(boolean sp) {
	synchronized (coreLock) {
	    if (sp == isAfterSP) { return; }
	    requestBatchStart();
	    boolean last = isAfterSP;
	    isAfterSP = sp;
	    if (sp && next != null) {
		next.setAfterSP(sp);
	    }
	    if (!sp && previous != null) {
		previous.setAfterSP(sp);
	    }
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_AFTER_SP, isAfterSP, last));
	    requestBatchEnd();
	}
    }

    public int getNumber() { return number; }
    public void updateNumber() {
	synchronized (coreLock) {
	    requestBatchStart();
	    int last = number;
	    if (previous == null) { 
		number = 1;
	    } else {
		number = previous.getNumber() + 1;
	    }
	    if (number != last) {
		if (next != null) {
		    next.updateNumber();
		}
		scoreBoardChange(new ScoreBoardEvent(this, EVENT_NUMBER, number, last));
		if (this == teamJamModel.getStarPassTrip() && teamJamModel.getJam().isCurrent()) {
		    scoreBoardChange(new ScoreBoardEvent(teamJamModel, TeamJam.EVENT_STAR_PASS_TRIP_NUMBER, number, last));
		}
	    }
	    requestBatchEnd();
	}
    }

    public int getPoints() { return points; }
    public void setPoints(int p) {
	synchronized (coreLock) {
	    if (points == p) { return; }
	    int last = points;
	    points = p;
	    teamJamModel.recalculateScores();
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_POINTS, points, last));
	}
    }
    public void changePoints(int change) {
	synchronized (coreLock) {
	    setPoints(points + change);
	}
    }
    
    public long getDuration() { return duration; }
    public long getJamClockElapsedStart() { return jamClockElapsedStart; }
    public long getJamClockElapsedEnd() { return jamClockElapsedEnd; }
    public long getWalltimeStart() { return walltimeStart; }
    public long getWalltimeEnd() { return walltimeEnd; }
    public void start() {
	synchronized (coreLock) {
	    requestBatchStart();
	    jamClockElapsedStart = scoreBoardModel.getClock(Clock.ID_JAM).getTimeElapsed();
	    walltimeStart = ScoreBoardClock.getInstance().getCurrentWalltime();
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_WALLTIME_START, walltimeStart, 0));
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_JAM_CLOCK_START, jamClockElapsedStart, 0));
	    requestBatchEnd();
	}
    }
    public void stop() {
	synchronized (coreLock) {
	    requestBatchStart();
	    duration = scoreBoardModel.getClock(Clock.ID_JAM).getTimeElapsed();
	    jamClockElapsedEnd = scoreBoardModel.getClock(Clock.ID_JAM).getTimeElapsed();
	    walltimeEnd = ScoreBoardClock.getInstance().getCurrentWalltime();
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_DURATION, duration, 0));
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_WALLTIME_START, walltimeEnd, 0));
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_JAM_CLOCK_START, jamClockElapsedEnd, 0));
	    requestBatchEnd();
	}
    }

    private ScoreBoardModel scoreBoardModel;
    
    private TeamJamModel teamJamModel;
    private ScoringTripModel previous, next;
    
    private static Object coreLock = DefaultScoreBoardModel.getCoreLock();
    
    private String id;
    private boolean isAfterSP;
    private int number = 1;
    private int points;
    private long duration;
    private long jamClockElapsedStart;
    private long jamClockElapsedEnd;
    private long walltimeStart;
    private long walltimeEnd;
}
