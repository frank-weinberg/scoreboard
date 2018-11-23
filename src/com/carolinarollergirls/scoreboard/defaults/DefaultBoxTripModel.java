package com.carolinarollergirls.scoreboard.defaults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import com.carolinarollergirls.scoreboard.event.DefaultScoreBoardEventProvider;
import com.carolinarollergirls.scoreboard.event.ScoreBoardEvent;
import com.carolinarollergirls.scoreboard.model.BoxTripModel;
import com.carolinarollergirls.scoreboard.model.FieldingModel;
import com.carolinarollergirls.scoreboard.model.PenaltyModel;
import com.carolinarollergirls.scoreboard.model.ScoreBoardModel;
import com.carolinarollergirls.scoreboard.utils.Comparators;
import com.carolinarollergirls.scoreboard.view.BoxTrip;
import com.carolinarollergirls.scoreboard.view.Fielding;
import com.carolinarollergirls.scoreboard.view.Penalty;
import com.carolinarollergirls.scoreboard.view.Skater;

public class DefaultBoxTripModel extends DefaultScoreBoardEventProvider implements BoxTripModel {
    public DefaultBoxTripModel(FieldingModel first, boolean betweenJams, boolean afterStarPass) {
	this(UUID.randomUUID().toString(), first, betweenJams, afterStarPass, true);
	for (PenaltyModel p : first.getSkaterModel().getUnservedPenalties()) {
	    addPenaltyModel(p);
	}
	if (first.getTeamJam().getJam().isRunning() || (!scoreBoardModel.isInJam() &&
		first.getTeamJam().getTeam().getNextTeamJam() == first.getTeamJam())) {
	    scoreBoardChange(new ScoreBoardEvent(first.getSkaterModel(), 
		    Skater.EVENT_PENALTY_BOX, true, false));
	    scoreBoardChange(new ScoreBoardEvent(first.getSkaterModel().getTeam(),
		    first.getFloorPosition().toString(), first.getSkaterModel(), null));
	}
    }
    public DefaultBoxTripModel(String id, FieldingModel first, boolean betweenJams, 
	    boolean afterStarPass, boolean isCurrent) {
	this.id = id;
	this.isCurrent = isCurrent;
	scoreBoardModel = first.getScoreBoardModel();
	addFielding(first);
	setStartBetweenJams(betweenJams);
	setStartAfterStarPass(afterStarPass);
	scoreBoardModel.registerBoxTripModel(this);
    }
    
    public String getProviderName() { return "BoxTrip"; }
    public Class<BoxTrip> getProviderClass() { return BoxTrip.class; }
    public String getProviderId() { return getId(); }

    public String getId() { return id; }
    public String toString() { return getId(); }
    
    public void unlink() {
	synchronized (coreLock) {
	    requestBatchStart();
	    if (fieldings.last().getTeamJam().getJam().isCurrent()) {
		scoreBoardChange(new ScoreBoardEvent(
			fieldings.last().getSkaterModel(), 
			Skater.EVENT_PENALTY_BOX, false, true));
	    }
	    for (FieldingModel f : new ArrayList<FieldingModel>(fieldings)) {
		f.removeBoxTripModel(this);
	    }
	    for (PenaltyModel p : new ArrayList<PenaltyModel>(penalties)) {
		p.removeBoxTrip(this);
	    }
	    requestBatchEnd();
	}
    }

    public void end(boolean betweenJams, boolean afterStarPass) {
	synchronized (coreLock) {
	    requestBatchStart();
	    boolean last = isCurrent;
	    isCurrent = false;
	    setEndBetweenJams(betweenJams);
	    setEndAfterStarPass(afterStarPass);
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_CURRENT, isCurrent, last));
	    scoreBoardChange(new ScoreBoardEvent(
		    fieldings.last().getSkaterModel(),
		    Skater.EVENT_PENALTY_BOX, false, true));
	    scoreBoardChange(new ScoreBoardEvent(fieldings.last().getSkater().getTeam(),
		    fieldings.last().getFloorPosition().toString(), fieldings.last().getSkater(), null));
	    requestBatchEnd();
	}
    }
    public void extend() {
	synchronized (coreLock) {
	    requestBatchStart();
	    boolean last = isCurrent;
	    isCurrent = true;
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_CURRENT, isCurrent, last));
	    scoreBoardChange(new ScoreBoardEvent(
		    fieldings.last().getSkaterModel(), 
		    Skater.EVENT_PENALTY_BOX, true, false));
	    scoreBoardChange(new ScoreBoardEvent(fieldings.last().getSkater().getTeam(),
		    fieldings.last().getFloorPosition().toString(), fieldings.last().getSkater(), null));
	    requestBatchEnd();
	}
    }
    
    public boolean isCurrent() { return isCurrent; }
    public boolean startedBetweenJams() { return startedBetweenJams; }
    public void setStartBetweenJams(boolean s) {
	synchronized (coreLock) {
	    if (s == startedBetweenJams) { return; }
	    requestBatchStart();
	    boolean last = startedBetweenJams;
	    startedBetweenJams = s;
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_START_BETWEEN, startedBetweenJams, last));
	    requestBatchEnd();
	}
    }
    public boolean endedBetweenJams() { return endedBetweenJams; }
    public void setEndBetweenJams(boolean e) {
	synchronized (coreLock) {
	    if (e == endedBetweenJams) { return; }
	    requestBatchStart();
	    boolean last = endedBetweenJams;
	    endedBetweenJams = e;
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_END_BETWEEN, endedBetweenJams, last));
	    requestBatchEnd();
	}
    }

    public boolean startedAfterStarPass() { return startedAfterStarPass; }
    public void setStartAfterStarPass(boolean s) {
	synchronized (coreLock) {
	    if (s == startedAfterStarPass) { return; }
	    requestBatchStart();
	    boolean last = startedAfterStarPass;
	    startedAfterStarPass = s;
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_START_AFTER_SP, startedAfterStarPass, last));
	    requestBatchEnd();
	}
    }
    public boolean endedAfterStarPass() { return endedAfterStarPass; }
    public void setEndAfterStarPass(boolean e) {
	synchronized (coreLock) {
	    if (e == endedAfterStarPass) { return; }
	    requestBatchStart();
	    boolean last = endedAfterStarPass;
	    endedAfterStarPass = e;
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_END_AFTER_SP, endedAfterStarPass, last));
	    requestBatchEnd();
	}
    }

    public Set<Penalty> getPenalties() { return new HashSet<Penalty>(penalties); }
    public Set<PenaltyModel> getPenaltyModels() { return penalties; }
    public void addPenaltyModel(PenaltyModel p) {
	synchronized (coreLock) {
	    if (penalties.contains(p)) { return; }
	    requestBatchStart();
	    penalties.add(p);
	    p.addBoxTrip(this);
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_ADD_PENALTY, p, null));
	    requestBatchEnd();
	}
    }
    public void removePenaltyModel(PenaltyModel p) {
	synchronized (coreLock) {
	    if (!penalties.contains(p)) { return; }
	    requestBatchStart();
	    penalties.remove(p);
	    p.removeBoxTrip(this);
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_REMOVE_PENALTY, p, null));
	    requestBatchEnd();
	}
    }

    public SortedSet<Fielding> getFieldings() { 
	SortedSet<Fielding> f = new TreeSet<Fielding>(Comparators.FieldingComparator);
	f.addAll(fieldings);
	return f;
    }
    public SortedSet<FieldingModel> getFieldingModels() { return fieldings; }
    public void addFielding(FieldingModel f) {
	synchronized (coreLock) {
	    if (fieldings.contains(f)) { return; }
	    requestBatchStart();
	    fieldings.add(f);
	    f.addBoxTripModel(this);
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_ADD_FIELDING, this, null));
	    requestBatchEnd();
	}
    }
    public void removeFielding(FieldingModel f) {
	synchronized (coreLock) {
	    if (fieldings.contains(f)) { return; }
	    requestBatchStart();
	    fieldings.remove(f);
	    f.removeBoxTripModel(this);
	    if (fieldings.size() == 0) {
		scoreBoardModel.deleteBoxTripModel(this);
	    }
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_REMOVE_FIELDING, this, null));
	    requestBatchEnd();
	}
    }

    public String getNotation(Fielding f, boolean afterStarPass) {
	synchronized (coreLock) {
	    if (!fieldings.contains(f)) {
		return "";
	    }
	    if (f == fieldings.first()) {
		if (!afterStarPass && startedAfterStarPass) {
		    return "";
		}
		if (afterStarPass == startedAfterStarPass 
			&& startedAfterStarPass == endedAfterStarPass
			&& fieldings.size() == 1
			&& !isCurrent && !endedBetweenJams) {
		    return startedBetweenJams ? "$" : "X";
		}
		return startedBetweenJams ? "S" : "/";
	    }
	    if (f == fieldings.last()) {
		if (afterStarPass && !endedAfterStarPass) {
		    return "";
		}
		if (afterStarPass == endedAfterStarPass
			&& !isCurrent && !endedBetweenJams) {
		    return "X";
		}
	    }
	    return "I";
	}
    }

    public String getAnnotation(Fielding f) { return annotations.get(f); }
    public void addAnnotation(FieldingModel f, String a) {
	synchronized (coreLock) {
	    String old = getAnnotation(f);
	    if (old == null) {
		annotations.put(f, a);
	    } else {
		annotations.put(f, old + "; " + a);
	    }
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_ANNOTATION, getAnnotation(f), f));
	}
    }
    
    private ScoreBoardModel scoreBoardModel;

    private static Object coreLock = DefaultScoreBoardModel.getCoreLock();
    
    private String id;

    private boolean isCurrent;
    private boolean startedBetweenJams;
    private boolean endedBetweenJams;
    private boolean startedAfterStarPass;
    private boolean endedAfterStarPass;
    
    private Set<PenaltyModel> penalties = new HashSet<PenaltyModel>();
    private SortedSet<FieldingModel> fieldings = new TreeSet<FieldingModel>(Comparators.FieldingComparator);
    private HashMap<Fielding, String> annotations = new HashMap<Fielding, String>();
}
