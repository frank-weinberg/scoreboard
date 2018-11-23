package com.carolinarollergirls.scoreboard.defaults;
/**
 * Copyright (C) 2008-2012 Mr Temper <MrTemper@CarolinaRollergirls.com>
 *
 * This file is part of the Carolina Rollergirls (CRG) ScoreBoard.
 * The CRG ScoreBoard is licensed under either the GNU General Public
 * License version 3 (or later), or the Apache License 2.0, at your option.
 * See the file COPYING for details.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import com.carolinarollergirls.scoreboard.event.DefaultScoreBoardEventProvider;
import com.carolinarollergirls.scoreboard.event.ScoreBoardEvent;
import com.carolinarollergirls.scoreboard.model.FieldingModel;
import com.carolinarollergirls.scoreboard.model.PenaltyModel;
import com.carolinarollergirls.scoreboard.model.ScoreBoardModel;
import com.carolinarollergirls.scoreboard.model.SkaterModel;
import com.carolinarollergirls.scoreboard.model.TeamModel;
import com.carolinarollergirls.scoreboard.utils.Comparators;
import com.carolinarollergirls.scoreboard.view.Fielding;
import com.carolinarollergirls.scoreboard.view.Jam;
import com.carolinarollergirls.scoreboard.view.Penalty;
import com.carolinarollergirls.scoreboard.view.Position;
import com.carolinarollergirls.scoreboard.view.Skater;
import com.carolinarollergirls.scoreboard.view.Team;
import com.carolinarollergirls.scoreboard.view.TeamJam;

public class DefaultSkaterModel extends DefaultScoreBoardEventProvider implements SkaterModel {
    public DefaultSkaterModel(TeamModel tm, String i, String n, String num, String flags) {
        teamModel = tm;
        scoreBoardModel = tm.getScoreBoardModel();
        setId(i);
        setName(n);
        setNumber(num);
        setFlags(flags);
        scoreBoardModel.registerSkaterModel(this);
    }

    public String getProviderName() { return "Skater"; }
    public Class<Skater> getProviderClass() { return Skater.class; }
    public String getProviderId() { return getId(); }

    public ScoreBoardModel getScoreBoardModel() { return scoreBoardModel; }

    public Team getTeam() { return teamModel.getTeam(); }
    public TeamModel getTeamModel() { return teamModel; }

    public String toString() { return getId(); }
    public String getId() { return id; }
    private void setId(String i) {
        synchronized (coreLock) {
            UUID uuid;
            try {
                uuid = UUID.fromString(i);
            } catch (IllegalArgumentException iae) {
                uuid = UUID.randomUUID();
            }
            id = uuid.toString();
        }
    }

    public Skater getSkater() { return this; }

    public void unlink() {
	for (FieldingModel f : new ArrayList<FieldingModel>(fieldings.values())) {
	    scoreBoardModel.deleteFieldingModel(f);
	}
	for (PenaltyModel p : new ArrayList<PenaltyModel>(penalties)) {
	    scoreBoardModel.deletePenaltyModel(p);
	}
	teamModel.removeSkaterModel(this);
    }
    
    public String getName() { return name; }
    public void setName(String n) {
        synchronized (coreLock) {
            String last = name;
            name = n;
            scoreBoardChange(new ScoreBoardEvent(getSkater(), EVENT_NAME, name, last));
        }
    }

    public String getNumber() { return number; }
    public void setNumber(String n) {
        synchronized (coreLock) {
            String last = number;
            number = n;
            scoreBoardChange(new ScoreBoardEvent(getSkater(), EVENT_NUMBER, number, last));
        }
    }

    public Position getPosition() {
	if (getCurrentFielding() != null) {
	    return getCurrentFielding().getFloorPosition().toPosition();
	} else {
	    return basePosition;
	}
    }
    public void setBasePosition(Position position) {
        synchronized (coreLock) {
            if (position == basePosition || position == Position.JAMMER
        	    || position == Position.PIVOT || position == Position.BLOCKER) {
                return;
            }
            Position last = getPosition();
            basePosition = position;
            if (getPosition() != last) {
        	scoreBoardChange(new ScoreBoardEvent(this, EVENT_POSITION, getPosition(), last));
            }
        }
    }

    public boolean isInBox() {
	if (getCurrentFielding() == null) {
	    return false;
	} else {
	    return getCurrentFielding().isInBox();
	}
    }

    public String getFlags() { return flags; }
    public void setFlags(String f) {
        synchronized (coreLock) {
            String last = flags;
            flags = f;
            scoreBoardChange(new ScoreBoardEvent(getSkater(), EVENT_FLAGS, flags, last));
        }
    }

    public void bench() {
        synchronized (coreLock) {
            if (isInBox() || (getUnservedPenalties().size() > 0 && getCurrentFielding() != null)) {
        	Position nextPosition = getCurrentFielding().getFloorPosition().toPosition(
        		teamModel.getCurrentTeamJam().isStarPass());
        	teamModel.getNextTeamJamModel().fieldSkater(this, nextPosition);
        	if (isInBox()) {
        	    getCurrentFieldingModel().getCurrentBoxTripModel().addFielding(fieldings.get(teamModel.getNextTeamJam()));
        	}
            }
            checkTempRemoval();
        }
    }
    
    public void changeSitFor3(FieldingModel f) {
	synchronized (coreLock) {
	    if (f.gotSat3Jams() && !satFor3In.contains(f)) {
		satFor3In.add(f);
		checkTempRemoval();
	    } 
	    if (!f.gotSat3Jams() && satFor3In.contains(f)) {
		satFor3In.remove(f);
		checkTempRemoval();
	    }
	}
    }
    private void checkTempRemoval() {
	if (basePosition == Position.NOT_IN_GAME || basePosition == Position.STAFF) {
	    return;
	}
	boolean removed = (foExpPenalty != null);
	int number = satFor3In.size();
	if (number > 0) {
	    Jam lastSatJam = satFor3In.last().getTeamJam().getJam();
	    if (lastSatJam.getPeriod() == scoreBoardModel.getCurrentPeriod()) {
		if (number == 1) {
		    int jamsSince = scoreBoardModel.getCurrentJam().getNumber() - lastSatJam.getNumber();
		    removed = (jamsSince <= 3);
		} else {
		    removed = true;
		}
	    }
	}
	if (removed) {
	    setBasePosition(Position.TEMPORARILY_OUT);
	} else {
	    setBasePosition(Position.BENCH);
	}
    }

    public void startBoxTrip(boolean betweenJams, boolean afterStarPass) {
	synchronized (coreLock) {
	    if (isInBox()) { return; }
	    FieldingModel fm = getCurrentFieldingModel();
	    if (fm != null && (!betweenJams || scoreBoardModel.isInJam())) {
		fm.addBoxTripModel(new DefaultBoxTripModel(fm, betweenJams, afterStarPass));
		if (!scoreBoardModel.isInJam()) {
		    bench();
		}
	    } else {
        	Position nextPosition = Position.BLOCKER;
        	if (fm != null) {
        	    nextPosition = fm.getFloorPosition().toPosition(
        		teamModel.getCurrentTeamJam().isStarPass());
        	}
		fm = fieldings.get(teamModel.getNextTeamJamModel());
		if (fm == null) {
		    teamModel.getNextTeamJamModel().fieldSkater(this, nextPosition);
		    fm = fieldings.get(teamModel.getNextTeamJamModel());
		}
		fm.addBoxTripModel(new DefaultBoxTripModel(fm, betweenJams, afterStarPass));
	    }
	}
    }
    public void finishBoxTrip(boolean betweenJams, boolean afterStarPass) {
	synchronized (coreLock) {
	    if (!isInBox()) { return; }
	    getCurrentFieldingModel().getCurrentBoxTripModel().end(betweenJams, afterStarPass);
	}
    }
    
    public Fielding getCurrentFielding() { return getCurrentFieldingModel(); }
    public FieldingModel getCurrentFieldingModel() {
	if (scoreBoardModel.isInJam()) {
	    return getFieldingModel(teamModel.getCurrentTeamJam());
	} else {
	    return getFieldingModel(teamModel.getNextTeamJam());
	}
    }
    public Fielding getFielding(TeamJam jam) { return getFieldingModel(jam); }
    public FieldingModel getFieldingModel(TeamJam jam) {
	return fieldings.get(jam);
    }
    public void addFieldingModel(FieldingModel f) {
	synchronized (coreLock) {
	    requestBatchStart();
	    Position last = getPosition();
	    fieldings.put(f.getTeamJam(), f);
	    if (f.gotSat3Jams()) {
		changeSitFor3(f);
	    }
	    scoreBoardChange(new ScoreBoardEvent(getSkater(), EVENT_POSITION, getPosition(), last));
	    requestBatchEnd();
	}
    }
    public void removeFieldingModel(FieldingModel f) {
	synchronized (coreLock) {
	    requestBatchStart();
	    Position last = getPosition();
	    fieldings.remove(f.getTeamJam());
	    if (f.gotSat3Jams()) {
		satFor3In.remove(f);
		checkTempRemoval();
	    }
	    scoreBoardChange(new ScoreBoardEvent(getSkater(), EVENT_POSITION, getPosition(), last));
	    requestBatchEnd();
	}
    }
    
    public List<Penalty> getPenalties() {
        synchronized (coreLock) {
            return Collections.unmodifiableList(new ArrayList<Penalty>(penalties));
        }
    }
    public Penalty getFOEXPPenalty() { return foExpPenalty; }
    public List<PenaltyModel> getPenaltyModels() { return penalties; }
    public List<PenaltyModel> getUnservedPenalties() {
	List<PenaltyModel> upm = new ArrayList<PenaltyModel>();
	for (PenaltyModel pm : penalties) {
	    if (!pm.isServed()) {
		upm.add(pm);
	    }
	}
	return upm;
    }
    public PenaltyModel getFOEXPPenaltyModel() { return foExpPenalty; }
    public void addPenaltyModel(PenaltyModel p) {
        synchronized (coreLock) {
            requestBatchStart();
            if (p.isExpulsion()) {
        	foExpPenalty = p;
                scoreBoardChange(new ScoreBoardEvent(getSkater(), EVENT_PENALTY_FOEXP, foExpPenalty, null));
            }
            penalties.add(p);
            updatePenalties();
            requestBatchEnd();
        }
    }
    public void removePenaltyModel(PenaltyModel p) {
	synchronized (coreLock) {
	    requestBatchStart();
	    penalties.remove(p);
	    if (foExpPenalty == p) {
		foExpPenalty = null;
                scoreBoardChange(new ScoreBoardEvent(getSkater(), EVENT_PENALTY_FOEXP, foExpPenalty, null));
	    }
	    updatePenalties();
	    requestBatchEnd();
	}
    }
    public void updatePenalties() {
	synchronized (coreLock) {
	    Collections.sort(penalties, Comparators.PenaltyComparator);
	    for (int i = 0; i < penalties.size(); i++) {
		penalties.get(i).setNumber(i+1);
	    }
	    int foLimit = scoreBoardModel.getSettings().getInt(Penalty.SETTING_FO_LIMIT) - 1;
            if (penalties.size() > foLimit
            	&& (foExpPenalty == null || !foExpPenalty.isExpulsion())) {
                foExpPenalty = penalties.get(foLimit);
                scoreBoardChange(new ScoreBoardEvent(getSkater(), EVENT_PENALTY_FOEXP, foExpPenalty, null));
            }
            scoreBoardChange(new ScoreBoardEvent(getSkater(), EVENT_PENALTY, getPenalties(), null));
	}
    }

    private TeamModel teamModel;
    
    private ScoreBoardModel scoreBoardModel;
    
    private static Object coreLock = DefaultScoreBoardModel.getCoreLock();

    private String id;
    private String name;
    private String number;
    private Position basePosition = Position.BENCH;
    private String flags;
    private HashMap<TeamJam, FieldingModel> fieldings = new HashMap<TeamJam, FieldingModel>();
    private SortedSet<Fielding> satFor3In = new TreeSet<Fielding>(Comparators.FieldingComparator);
    private List<PenaltyModel> penalties = new LinkedList<PenaltyModel>();
    private PenaltyModel foExpPenalty;
}
