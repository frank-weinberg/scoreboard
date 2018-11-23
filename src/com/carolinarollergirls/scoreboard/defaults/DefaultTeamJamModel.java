package com.carolinarollergirls.scoreboard.defaults;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.carolinarollergirls.scoreboard.event.DefaultScoreBoardEventProvider;
import com.carolinarollergirls.scoreboard.event.ScoreBoardEvent;
import com.carolinarollergirls.scoreboard.model.FieldingModel;
import com.carolinarollergirls.scoreboard.model.JamModel;
import com.carolinarollergirls.scoreboard.model.ScoreBoardModel;
import com.carolinarollergirls.scoreboard.model.ScoringTripModel;
import com.carolinarollergirls.scoreboard.model.SkaterModel;
import com.carolinarollergirls.scoreboard.model.TeamJamModel;
import com.carolinarollergirls.scoreboard.model.TeamModel;
import com.carolinarollergirls.scoreboard.utils.Comparators;
import com.carolinarollergirls.scoreboard.view.Fielding;
import com.carolinarollergirls.scoreboard.view.FloorPosition;
import com.carolinarollergirls.scoreboard.view.Jam;
import com.carolinarollergirls.scoreboard.view.Position;
import com.carolinarollergirls.scoreboard.view.ScoringTrip;
import com.carolinarollergirls.scoreboard.view.Team;
import com.carolinarollergirls.scoreboard.view.TeamJam;

public class DefaultTeamJamModel extends DefaultScoreBoardEventProvider implements TeamJamModel {
    public DefaultTeamJamModel(String id, JamModel j, TeamModel t) {
	this.id = id;
	scoreBoardModel = j.getScoreBoardModel();
	jamModel = j;
        teamModel = t;
        addScoringTripModel(new DefaultScoringTripModel(this, null));
        scoreBoardModel.registerTeamJamModel(this);
    }

    public ScoreBoardModel getScoreBoardModel() { return scoreBoardModel; }
    
    public JamModel getJamModel() { return jamModel; }
    public Jam getJam() { return getJamModel(); }

    public TeamModel getTeamModel() { return teamModel; }
    public Team getTeam() { return getTeamModel(); }
 
    public TeamJamModel getPrevious() { return previous; }
    public void setPrevious(TeamJamModel p) {
        synchronized (coreLock) {
            if (p == previous) { return; }
            requestBatchStart();
            previous = p;
            if (previous != null) { previous.setNext(this); }
            recalculateScores();
            requestBatchEnd();
        }
    }
    public TeamJamModel getNext() { return next; }
    public void setNext(TeamJamModel n) {
        synchronized (coreLock) {
            if (n == next) { return; }
            requestBatchStart();
            next = n;
            if (next != null) { next.setPrevious(this); }
            requestBatchEnd();
        }
    }
    
    public String getProviderName() { return "TeamJam"; }
    public Class<TeamJam> getProviderClass() { return TeamJam.class; }
    public String getProviderId() { return getId(); }

    public String getId() { return id; }
    public String toString() { return getId(); }
    
    public void unlink() {
	synchronized (coreLock) {
	    requestBatchStart();
	    teamModel.removeTeamJamModel(this);
	    for (ScoringTripModel st : new ArrayList<ScoringTripModel>(trips)) {
		scoreBoardModel.deleteScoringTripModel(st);
	    }
	    for (FieldingModel f : new ArrayList<FieldingModel>(fieldings.values())) {
		scoreBoardModel.deleteFieldingModel(f);
	    }
	    requestBatchEnd();
	}
    }

    public ScoringTrip getCurrentScoringTrip() { return getCurrentScoringTripModel(); }
    public ScoringTripModel getCurrentScoringTripModel() { return trips.get(trips.size()-1); }
    public List<ScoringTrip> getScoringTrips() { return new ArrayList<ScoringTrip>(trips); }
    public List<ScoringTripModel> getScoringTripModels() { return trips; }
    public void addScoringTripModel() {
	synchronized (coreLock) {
	    getCurrentScoringTripModel().stop();
	    addScoringTripModel(new DefaultScoringTripModel(this, getCurrentScoringTripModel()));
	    getCurrentScoringTripModel().start();
	}
    }
    public void addScoringTripModel(ScoringTripModel t) {
	synchronized (coreLock) {
	    if (trips.contains(t)) { return; }
            requestBatchStart();
	    trips.add(t);
	    if (t.isAfterSP() && (t.getPrevious() == null || !t.getPrevious().isAfterSP())) {
		setStarPassTrip(t);
	    }
	    if (t.getNext() != null) {
		Collections.sort(trips, Comparators.ScoringTripComparator);
	    }
	    recalculateScores();
            scoreBoardChange(new ScoreBoardEvent(this, EVENT_ADD_SCORING_TRIP, t, null));
            requestBatchEnd();
	}
    }
    public void removeScoringTripModel(ScoringTripModel t) {
	synchronized (coreLock) {
	    if (!trips.contains(t)) { return; }
            requestBatchStart();
	    trips.remove(t);
	    if (!cleaning) {
		if (trips.size() == 0) {
		    addScoringTripModel(new DefaultScoringTripModel(this, null));
		}
		if (t == starPassTrip) {
		    setStarPassTrip(t.getNext());
		}
		recalculateScores();
	    }
            scoreBoardChange(new ScoreBoardEvent(this, EVENT_REMOVE_SCORING_TRIP, t, null));
            requestBatchEnd();
	}
    }
    
    public int getOsOffset() { return osOffset; }
    public void setOsOffset(int o) {
        synchronized (coreLock) {
            if (o == osOffset) { return; }
            requestBatchStart();
            int last = osOffset;
            osOffset = o;
            recalculateScores();
            scoreBoardChange(new ScoreBoardEvent(this, EVENT_OS_OFFSET, osOffset, last));
            requestBatchEnd();
        }
    }
    public void changeOsOffset(int change) {
	setOsOffset(osOffset + change);
    }

    public String getOsOffsetReason() { return osOffsetReason; }
    public void setOsOffsetReason(String o) {
        synchronized (coreLock) {
            if (o == osOffsetReason) { return; }
            requestBatchStart();
            String last = osOffsetReason;
            osOffsetReason = o;
            scoreBoardChange(new ScoreBoardEvent(this, EVENT_OS_OFFSET_REASON, o, last));
            requestBatchEnd();
        }
    }

    public int getJamScore() { return jamScore; }
    public int getTotalScore() { return totalScore; }
    public void recalculateScores() {
        synchronized (coreLock) {
            requestBatchStart();
            int last = totalScore;
            int lastJam = jamScore;
            int previousScore = 0;
            if (previous != null) {
                previousScore = previous.getTotalScore();
            }
            jamScore = osOffset;
            for (ScoringTrip trip : trips) {
                jamScore += trip.getPoints();
            }
            totalScore = previousScore + jamScore;
            if (next != null) {
        	next.recalculateScores();
            }
            if (totalScore != last) {
        	scoreBoardChange(new ScoreBoardEvent(this, EVENT_SCORE, totalScore, last));
                if (this == teamModel.getCurrentTeamJam()) {
                    scoreBoardChange(new ScoreBoardEvent(teamModel, Team.EVENT_SCORE, totalScore, last));
                }
            }
            if (jamScore != lastJam) {
        	scoreBoardChange(new ScoreBoardEvent(this, EVENT_JAM_SCORE, jamScore, lastJam));
                if (this == teamModel.getCurrentTeamJam()) {
                    scoreBoardChange(new ScoreBoardEvent(teamModel, Team.EVENT_JAM_SCORE, jamScore, lastJam));
                }
            }
            requestBatchEnd();
        }
    }
    
    public void start() { trips.get(0).start(); }

    public boolean isLost() { return lost; }
    public void setLost(boolean l) {
        synchronized (coreLock) {
            if (l == lost) { return; }
            requestBatchStart();
            boolean last = lost;
            boolean lastDisplay = teamModel.displayLead();
            lost = l;
            scoreBoardChange(new ScoreBoardEvent(this, EVENT_LOST, lost, last));
            if (this == teamModel.getCurrentTeamJam()) {
        	scoreBoardChange(new ScoreBoardEvent(teamModel, Team.EVENT_LOST, lost, last));
        	scoreBoardChange(new ScoreBoardEvent(teamModel, Team.EVENT_DISPLAY_LEAD, teamModel.displayLead(), lastDisplay));
            }
            requestBatchEnd();
        }
    }

    public boolean isLead() { return lead; }
    public void setLead(boolean l) {
        synchronized (coreLock) {
            if (l == lead) { return; }
            requestBatchStart();
            boolean last = lead;
            boolean lastDisplay = teamModel.displayLead();
            lead = l;
            scoreBoardChange(new ScoreBoardEvent(this, EVENT_LEAD, lead, last));
            if (this == teamModel.getCurrentTeamJam()) {
        	scoreBoardChange(new ScoreBoardEvent(teamModel, Team.EVENT_LEAD, lead, last));
        	scoreBoardChange(new ScoreBoardEvent(teamModel, Team.EVENT_DISPLAY_LEAD, teamModel.displayLead(), lastDisplay));
            }
            requestBatchEnd();
        }
    }

    public boolean isCalloff() { return calloff; }
    public void setCalloff(boolean c) {
        synchronized (coreLock) {
            if (c == calloff) { return; }
            requestBatchStart();
            boolean last = calloff;
            calloff = c;
            scoreBoardChange(new ScoreBoardEvent(this, EVENT_CALLOFF, calloff, last));
            if (this == teamModel.getCurrentTeamJam()) {
        	scoreBoardChange(new ScoreBoardEvent(teamModel, Team.EVENT_CALLOFF, calloff, last));
            }
            requestBatchEnd();
        }
    }
    
    public boolean isInjury() { return jamModel.getInjury(); }
    public void setInjury(boolean i) {
        synchronized (coreLock) {
            jamModel.setInjury(i);
        }
    }

    public boolean isStarPass() { return (starPassTrip != null); }
    public ScoringTrip getStarPassTrip() { return starPassTrip; }
    public void setStarPassTrip(ScoringTripModel trip) {
        synchronized (coreLock) {
            if (trip == starPassTrip) { return; }
            requestBatchStart();
            ScoringTripModel last = starPassTrip;
            boolean lastBool = isStarPass();
            int lastNumber = starPassTrip == null ? 0 : starPassTrip.getNumber();
            starPassTrip = trip;
            if (trip == null) { 
        	getCurrentScoringTripModel().setAfterSP(false);
            } else {
        	if (trip.isAfterSP() && trip.getPrevious() != null) {
        	    trip.getPrevious().setAfterSP(false);
        	} else {
        	    trip.setAfterSP(true);
        	}
            }
            scoreBoardChange(new ScoreBoardEvent(this, EVENT_STAR_PASS_TRIP, starPassTrip, last));
            scoreBoardChange(new ScoreBoardEvent(this, EVENT_STAR_PASS_TRIP_NUMBER,
        	    starPassTrip == null ? 0 : starPassTrip.getNumber(), lastNumber));
            if (this == teamModel.getCurrentTeamJam() && lastBool != isStarPass()) {
        	scoreBoardChange(new ScoreBoardEvent(teamModel, Team.EVENT_STAR_PASS, isStarPass(), lastBool));
            }
            requestBatchEnd();
        }
    }

    public Fielding getFielding(FloorPosition p) {
	return fieldings.get(p);
    }
    public FieldingModel getFieldingModel(FloorPosition p) {
	return fieldings.get(p);
    }
    public void removeFieldingModel(FieldingModel f) {
	synchronized (coreLock) {
	    if (!(fieldings.get(f.getFloorPosition()) == f)) { return; }
	    requestBatchStart();
	    fieldings.remove(f.getFloorPosition());
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_REMOVE_FIELDING, f, null));
	    scoreBoardChange(new ScoreBoardEvent(teamModel, f.getFloorPosition().toString(), null, f.getSkater()));
	    requestBatchEnd();
	}
    }
    public void addFieldingModel(FieldingModel f) {
	synchronized (coreLock) {
	    if (fieldings.get(f.getFloorPosition()) == f) { return; }
	    requestBatchStart();
	    fieldings.put(f.getFloorPosition(), f);
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_ADD_FIELDING, f, null));
	    if ((scoreBoardModel.isInJam() && this == teamModel.getCurrentTeamJam()) || 
		    (!scoreBoardModel.isInJam() && this == teamModel.getNextTeamJam())) {
		scoreBoardChange(new ScoreBoardEvent(teamModel, f.getFloorPosition().toString(), f.getSkater(), null));
	    }
	    requestBatchEnd();
	}
    }
    public void fieldSkater(SkaterModel s, Position p) {
	synchronized (coreLock) {
	    requestBatchStart();
	    FieldingModel fielding = s.getCurrentFieldingModel();
	    FloorPosition newPosition = getReplacableFloorPosition(p);
	    if (newPosition != null) {
		FieldingModel replaced = fieldings.get(newPosition);
		if (replaced != null) {
		    if (p == Position.PIVOT && newPosition != FloorPosition.PIVOT) {
			scoreBoardModel.deleteFieldingModel(fieldings.get(newPosition));
			replaced.setFloorPosition(newPosition);
			setNoPivot(false);
		    } else {
			scoreBoardModel.deleteFieldingModel(replaced);
		    }
		}
		if (fielding == null) {
		    fielding = new DefaultFieldingModel(this, s, newPosition);
		} else {
		    fielding.setFloorPosition(newPosition);
		}
		addFieldingModel(fielding);
		if (newPosition == FloorPosition.PIVOT) {
		    setNoPivot(p == Position.BLOCKER);
		}
	    } else if (p == Position.BLOCKER || p == Position.PIVOT) {
		//all applicable spots have been taken by skaters with box trips.
		//but maybe this is just a Pivot turning Blocker or vice versa
		if (fielding != null) {
		    if (fielding.getFloorPosition() == FloorPosition.PIVOT) {
			if (p == Position.BLOCKER) {
			    setNoPivot(true);
			} else if (p == Position.PIVOT) {
			    setNoPivot(false);
			}
		    } else if (fielding.getFloorPosition().toPosition() == Position.BLOCKER 
			    && p == Position.PIVOT && hasNoPivot) {
			FieldingModel swapped = fieldings.get(FloorPosition.PIVOT);
			FloorPosition fp = fielding.getFloorPosition();
			fielding.setFloorPosition(FloorPosition.PIVOT);
			swapped.setFloorPosition(fp);
			fieldings.put(fp, swapped);
			fieldings.put(FloorPosition.PIVOT, fielding);
		    }
		}
	    } else if (p == Position.BENCH) {
		scoreBoardModel.deleteFieldingModel(fielding);		
	    }
	    requestBatchEnd();
	}
    }
    private FloorPosition getReplacableFloorPosition(Position pos) {
	FieldingModel fm;
	switch (pos) {
	case JAMMER:
	    fm = fieldings.get(FloorPosition.JAMMER);
	    if (fm == null || !fm.hasBoxTrips()) {
		return FloorPosition.JAMMER;
	    } else {
		return null;
	    }
	case PIVOT:
	    fm = fieldings.get(FloorPosition.PIVOT);
	    if (!hasNoPivot || fm == null || !fm.hasBoxTrips()) {
		return FloorPosition.PIVOT;
	    }
	    //no break;
	case BLOCKER:
	    if (fieldings.get(FloorPosition.BLOCKER1) == null) {
		return FloorPosition.BLOCKER1;
	    } else if (fieldings.get(FloorPosition.BLOCKER2) == null) {
		return FloorPosition.BLOCKER2;
	    } else if (fieldings.get(FloorPosition.BLOCKER3) == null) {
		return FloorPosition.BLOCKER3;
	    } else if (fieldings.get(FloorPosition.PIVOT) == null) {
		return FloorPosition.PIVOT;
	    } else if (!fieldings.get(FloorPosition.BLOCKER1).hasBoxTrips()) {
		return FloorPosition.BLOCKER1;
	    } else if (!fieldings.get(FloorPosition.BLOCKER2).hasBoxTrips()) {
		return FloorPosition.BLOCKER2;
	    } else if (!fieldings.get(FloorPosition.BLOCKER3).hasBoxTrips()) {
		return FloorPosition.BLOCKER3;
	    } else if (hasNoPivot && !fieldings.get(FloorPosition.PIVOT).hasBoxTrips()) {
		return FloorPosition.PIVOT;
	    } else {
		return null;
	    }
	default:
	    return null;
	}
    }
    
    public boolean hasNoPivot() { return hasNoPivot; }
    public void setNoPivot(boolean np) {
        synchronized (coreLock) {
            if (np == hasNoPivot) { return; }
            requestBatchStart();
            boolean last = hasNoPivot;
            hasNoPivot = np;
            scoreBoardChange(new ScoreBoardEvent(this, EVENT_NO_PIVOT, hasNoPivot, last));
            requestBatchEnd();
        }
    }

    private ScoreBoardModel scoreBoardModel;
    
    private JamModel jamModel;
    private TeamModel teamModel;
    
    private TeamJamModel previous, next;
    
    private static Object coreLock = DefaultScoreBoardModel.getCoreLock();
    
    private String id;
    private List<ScoringTripModel> trips = new ArrayList<ScoringTripModel>();
    private int osOffset = 0;
    private String osOffsetReason = "";
    private int jamScore;
    private int totalScore;
    private boolean lost = false;
    private boolean lead = false;
    private boolean calloff = false;
    private ScoringTripModel starPassTrip = null;
    private boolean hasNoPivot = false;
    private Map<FloorPosition, FieldingModel> fieldings = new ConcurrentHashMap<FloorPosition, FieldingModel>();
    
    private boolean cleaning = false;
}
