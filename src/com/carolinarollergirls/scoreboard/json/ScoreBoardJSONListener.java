package com.carolinarollergirls.scoreboard.json;
/**
 * Copyright (C) 2008-2012 Mr Temper <MrTemper@CarolinaRollergirls.com>
 *
 * This file is part of the Carolina Rollergirls (CRG) ScoreBoard.
 * The CRG ScoreBoard is licensed under either the GNU General Public
 * License version 3 (or later), or the Apache License 2.0, at your option.
 * See the file COPYING for details.
 */

import java.util.LinkedList;
import java.util.List;

import com.carolinarollergirls.scoreboard.ScoreBoardManager;
import com.carolinarollergirls.scoreboard.event.ScoreBoardEvent;
import com.carolinarollergirls.scoreboard.event.ScoreBoardEventProvider;
import com.carolinarollergirls.scoreboard.event.ScoreBoardListener;
import com.carolinarollergirls.scoreboard.penalties.PenaltyCode;
import com.carolinarollergirls.scoreboard.penalties.PenaltyCodesDefinition;
import com.carolinarollergirls.scoreboard.penalties.PenaltyCodesManager;
import com.carolinarollergirls.scoreboard.view.BoxTrip;
import com.carolinarollergirls.scoreboard.view.Clock;
import com.carolinarollergirls.scoreboard.view.Fielding;
import com.carolinarollergirls.scoreboard.view.FloorPosition;
import com.carolinarollergirls.scoreboard.view.FrontendSettings;
import com.carolinarollergirls.scoreboard.view.Jam;
import com.carolinarollergirls.scoreboard.view.Penalty;
import com.carolinarollergirls.scoreboard.view.Period;
import com.carolinarollergirls.scoreboard.view.ScoreBoard;
import com.carolinarollergirls.scoreboard.view.ScoringTrip;
import com.carolinarollergirls.scoreboard.view.Settings;
import com.carolinarollergirls.scoreboard.view.Skater;
import com.carolinarollergirls.scoreboard.view.Team;
import com.carolinarollergirls.scoreboard.view.Team.AlternateName;
import com.carolinarollergirls.scoreboard.view.TeamJam;
import com.carolinarollergirls.scoreboard.view.Timeout;

/**
 * Converts a ScoreBoardEvent into a representative JSON Update
 */
public class ScoreBoardJSONListener implements ScoreBoardListener {
    public ScoreBoardJSONListener(ScoreBoard sb, JSONStateManager jsm) {
        this.jsm = jsm;
        initialize(sb);
        sb.addScoreBoardListener(this);
    }

    public void scoreBoardChange(ScoreBoardEvent event) {
        synchronized (this) {
            try {
                ScoreBoardEventProvider p = event.getProvider();
                String provider = p.getProviderName();
                String prop = event.getProperty();
                if (prop.equals(ScoreBoardEvent.BATCH_START)) {
                    batch++;
                    return;
                }
                if (prop.equals(ScoreBoardEvent.BATCH_END)) {
                    if (batch == 0) {
                        return;
                    }
                    if (--batch == 0) {
                        updateState();
                    }
                    return;
                }

                Object v = event.getValue();
                if (p instanceof ScoreBoard) {
                    if(prop.equals(ScoreBoard.EVENT_ADD_CLOCK)) {
                	processClock((Clock)v);
                    } else if(prop.equals(ScoreBoard.EVENT_REMOVE_CLOCK)) {
                	remove(getPath((Clock)v));
                    } else if(prop.equals(ScoreBoard.EVENT_ADD_TEAM)) {
                    	processTeam((Team)v);
                    } else if(prop.equals(ScoreBoard.EVENT_REMOVE_TEAM)) {
                    	remove(getPath((Team)v));
                    } else if(prop.equals(ScoreBoard.EVENT_ADD_PERIOD)) {
                    	processPeriod((Period)v);
                    } else if(prop.equals(ScoreBoard.EVENT_REMOVE_PERIOD)) {
                	remove(getPath((Period)v));
                    } else {
                        update("ScoreBoard", prop, v);
                    }
                } else if (p instanceof Settings) {
                    Settings s = (Settings)p;
                    String prefix = null;
                    if (s.getParent() instanceof ScoreBoard) {
                        prefix = "ScoreBoard";
                    }
                    if(prop.equals(PenaltyCodesManager.SETTING_PENALTIES_FILE)) {
                        update(prefix, "Setting(" + prop + ")", v);
                        processPenaltyCodes(s);
                    } else if (prefix == null) {
                        ScoreBoardManager.printMessage(provider + " update of unknown kind.  prop: " + prop + ", v: " + v);
                    } else {
                        update(prefix, "Setting(" + prop + ")", v);
                    }
                } else if (p instanceof FrontendSettings) {
                    update("ScoreBoard.FrontendSettings", prop, v);
                } else if (p instanceof Clock) {
                    update(getPath((Clock)p), prop, v);
                } else if (p instanceof Team) {
                    if(prop.equals(Team.EVENT_ADD_SKATER)) {
                	processSkater((Skater)v);
                    } else if(prop.equals(Team.EVENT_REMOVE_SKATER)) {
                	remove(getPath((Skater)v));
                    } else if(prop.equals(Team.EVENT_ADD_ALTERNATE_NAME)) {
                    	processAlternateName((Team.AlternateName)v);
                    } else if(prop.equals(Team.EVENT_REMOVE_ALTERNATE_NAME)) {
                    	remove(getPath((Team.AlternateName)v));
                    } else if(prop.equals(Team.EVENT_ADD_COLOR)) {
                    	processColor((Team.Color)v);
                    } else if(prop.equals(Team.EVENT_REMOVE_COLOR)) {
                    	remove(getPath((Team.Color)v));
                    } else {
                        update(getPath((Team)p), prop, v);
                    }
                } else if (p instanceof Team.AlternateName) {
                    processAlternateName((AlternateName)p);
                } else if (p instanceof Team.Color) {
                    processColor((Team.Color)p);
                } else if (p instanceof Skater) {
                    if(prop.equals(Skater.EVENT_PENALTY)) {
                	remove(getPath((Skater)p) + ".Penalty");
                        for (Penalty pen : ((Skater)p).getPenalties()) {
                            processPenalty(pen);
                        }
                    } else if (prop.equals(Skater.EVENT_POSITION)) {
                	update(getPath((Skater)p), prop, v);
                    } else {
                        update(getPath((Skater)p), prop, v);
                    }
                } else if (p instanceof Penalty) {
                    if(prop.equals(Penalty.EVENT_ADD_BOX_TRIP)) {
                	update(getPath((Penalty)p), "BoxTrip(" + ((BoxTrip)v).getId() + ")", ((BoxTrip)v).getId());
                    } else if(prop.equals(Penalty.EVENT_REMOVE_BOX_TRIP)) {
                	update(getPath((Penalty)p), "BoxTrip(" + ((BoxTrip)v).getId() + ")", null);
                    } else {
                        update(getPath((Penalty)p), prop, v);
                    }
                } else if (p instanceof Period) {
                    if(prop.equals(Period.EVENT_ADD_TIMEOUT)) {
                	processTimeout((Timeout)v);
                    } else if(prop.equals(Period.EVENT_REMOVE_TIMEOUT)) {
                	remove(getPath((Timeout)v));
                    } else if(prop.equals(Period.EVENT_ADD_JAM)) {
                    	processJam((Jam)v);
                    } else if(prop.equals(Period.EVENT_REMOVE_JAM)) {
                    	remove(getPath((Jam)v));
                    } else if(prop.equals(Period.EVENT_NUMBER)) {
                    	processPeriod((Period)p);
                    } else {
                        update(getPath((Period)p), prop, v);
                    }
                } else if (p instanceof Timeout) {
                    update(getPath((Timeout)p), prop, v);
                } else if (p instanceof Jam) {
                    if(prop.equals(Jam.EVENT_ADD_TIMEOUT)) {
                	update(getPath((Jam)p), "Timeout(" + ((Timeout)v).getId() + ")", ((Timeout)v).getId());
                    } else if(prop.equals(Jam.EVENT_REMOVE_TIMEOUT)) {
                	update(getPath((Jam)p), "Timeout(" + ((Timeout)v).getId() + ")", null);
                    } else if(prop.equals(Jam.EVENT_ADD_PENALTY)) {
                	update(getPath((Jam)p), "Penalty(" + ((Penalty)v).getId() + ")", ((Penalty)v).getId());
                    } else if(prop.equals(Jam.EVENT_REMOVE_PENALTY)) {
                	update(getPath((Jam)p), "Penalty(" + ((Penalty)v).getId() + ")", null);
                    } else if(prop.equals(Jam.EVENT_NUMBER)) {
                    	processJam((Jam)p);
                    } else {
                        update(getPath((Jam)p), prop, v);
                    }
                } else if (p instanceof TeamJam) {
                    if(prop.equals(TeamJam.EVENT_ADD_SCORING_TRIP)) {
                	processScoringTrip((ScoringTrip)v);
                    } else if(prop.equals(TeamJam.EVENT_REMOVE_SCORING_TRIP)) {
                	remove(getPath((ScoringTrip)v));
                    } else if(prop.equals(TeamJam.EVENT_ADD_FIELDING)) {
                    	processFielding((Fielding)v);
                    } else if(prop.equals(TeamJam.EVENT_REMOVE_FIELDING)) {
                    	remove(getPath((Fielding)v));
                    } else {
                        update(getPath((TeamJam)p), prop, v);
                    }
                } else if (p instanceof ScoringTrip) {
                    if(prop.equals(ScoringTrip.EVENT_NUMBER)) {
                	processScoringTrip((ScoringTrip)p);
                    } else {
                	update(getPath((ScoringTrip)p), prop, v);
                    }
                } else if (p instanceof Fielding) {
                    if(prop.equals(Fielding.EVENT_ADD_BOX_TRIP)) {
                	if ((Fielding)p == ((BoxTrip)v).getFieldings().first()) {
                	    processBoxTrip((BoxTrip)v);
                	} else {
                	    update(getPath((Fielding)p), "BoxTrip(" + ((BoxTrip)v).getId() + ")", ((BoxTrip)v).getId());
                	}
                    } else if(prop.equals(Fielding.EVENT_REMOVE_BOX_TRIP)) {
                	remove(getPath((Fielding)p) + ".BoxTrip(" + ((BoxTrip)v).getId() + ")");
                    } else {
                        update(getPath((Fielding)p), prop, v);
                    }
                } else if (p instanceof BoxTrip) {
                    if(prop.equals(BoxTrip.EVENT_ADD_FIELDING)) {
                	update(getPath((BoxTrip)p), "Fielding(" + ((Fielding)v).getId() + ")", ((Fielding)v).getId());
                    } else if(prop.equals(BoxTrip.EVENT_REMOVE_FIELDING)) {
                	update(getPath((BoxTrip)p), "Fielding(" + ((Fielding)v).getId() + ")", null);
                    } else if(prop.equals(BoxTrip.EVENT_ADD_PENALTY)) {
                	update(getPath((BoxTrip)p), "Penalty(" + ((Penalty)v).getId() + ")", ((Penalty)v).getId());
                    } else if(prop.equals(BoxTrip.EVENT_REMOVE_PENALTY)) {
                	update(getPath((BoxTrip)p), "Penalty(" + ((Penalty)v).getId() + ")", null);
                    } else {
                	BoxTrip bt = (BoxTrip)p;
                        update(getPath(bt), prop, v);
                        for (Fielding f : bt.getFieldings()) {
                            update(getPath(f), "BoxTripCodeBeforeSP(" + bt.getId() + ")", bt.getNotation(f, false));
                            update(getPath(f), "BoxTripCodeAfterSP("+ bt.getId() + ")", bt.getNotation(f, true));
                        }
                    }
                } else {
                    ScoreBoardManager.printMessage(provider + " update of unknown kind.	prop: " + prop + ", v: " + v);
                }

            } catch (Exception e) {
                ScoreBoardManager.printMessage("Error!  " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (batch == 0) {
                    updateState();
                }
            }
        }
    }
    
    private String getPath(Clock clock) {
	return "ScoreBoard.Clock(" + clock.getId() + ")"; 
    }

    private String getPath(Team team) {
	return "ScoreBoard.Team(" + team.getId() + ")"; 
    }

    private String getPath(Skater skater) {
	return getPath(skater.getTeam()) + ".Skater(" + skater.getId() + ")"; 
    }

    private String getPath(Penalty penalty) {
	return getPath(penalty.getSkater()) + ".Penalty(" + penalty.getNumber() + ")"; 
    }

    private String getPath(Team.AlternateName alternateName) {
	return getPath(alternateName.getTeam()) + ".AlternateName(" + alternateName.getId() + ")"; 
    }

    private String getPath(Team.Color color) {
	return getPath(color.getTeam()) + ".Color(" + color.getId() + ")"; 
    }

    private String getPath(Period period) {
	return "ScoreBoard.Period(" + period.getNumber() + ")";
    }

    private String getPath(Timeout timeout) {
	return getPath(timeout.getPrecedingJam().getPeriod()) + ".Timeout(" + timeout.getId() + ")"; 
    }

    private String getPath(Jam jam) {
	return getPath(jam.getPeriod()) + ".Jam(" + jam.getNumber() + ")"; 
    }

    private String getPath(TeamJam teamJam) {
	//use "Team" instead of "TeamJam" for backwards compatibility
	return getPath(teamJam.getJam()) + ".Team(" + teamJam.getTeam().getId() + ")"; 
    }

    private String getPath(ScoringTrip scoringTrip) {
	return getPath(scoringTrip.getTeamJam()) + ".ScoringTrip(" + scoringTrip.getNumber() + ")"; 
    }

    private String getPath(Fielding fielding) {
	return getPath(fielding.getTeamJam()) + ".Fielding(" + fielding.getFloorPosition() + ")";
    }

    private String getPath(BoxTrip boxTrip) {
	return getPath(boxTrip.getFieldings().first()) + ".BoxTrip(" + boxTrip.getId() + ")"; 
    }

    private void updateState() {
        synchronized (this) {
            if (updates.isEmpty()) {
                return;
            }
            jsm.updateState(updates);
            updates.clear();
        }
    }

    private void update(String prefix, String prop, Object v) {
        if (v == null || v instanceof String || v instanceof Integer || 
        	v instanceof Long || v instanceof Boolean) {
            updates.add(new WSUpdate(prefix + "." + prop, v));
        } else {
            update(prefix, prop, String.valueOf(v));
        }
    }
    
    private void remove(String path) { updates.add(new WSUpdate(path, null)); }

    private void processSettings(String path, Settings s) {
        remove(path);

        for (String key : s.getAll().keySet()) {
            update(path, "Setting(" + key + ")", s.get(key));
        }
    }

    private void processFrontendSettings(String path, FrontendSettings s) {
        remove(path);

        for (String key : s.getAll().keySet()) {
            update(path, key, s.get(key));
        }
    }

    private void processClock(Clock c) {
        String path = getPath(c);
        remove(path);

        update(path, Clock.EVENT_NAME, c.getName());
        update(path, Clock.EVENT_NUMBER, c.getNumber());
        update(path, Clock.EVENT_TIME, c.getTime());
        update(path, Clock.EVENT_INVERTED_TIME, c.getInvertedTime());
        update(path, Clock.EVENT_MINIMUM_TIME, c.getMinimumTime());
        update(path, Clock.EVENT_MAXIMUM_TIME, c.getMaximumTime());
        update(path, Clock.EVENT_DIRECTION, c.isCountDirectionDown());
        update(path, Clock.EVENT_RUNNING, c.isRunning());
    }

    private void processTeam(Team t) {
        String path = getPath(t);
        remove(path);

        update(path, Team.EVENT_NAME, t.getName());
        update(path, Team.EVENT_LOGO, t.getLogo());
        update(path, Team.EVENT_SCORE, t.getScore());
        update(path, Team.EVENT_JAM_SCORE, t.getJamScore());
        update(path, Team.EVENT_TIMEOUTS, t.getTimeoutsRemaining());
        update(path, Team.EVENT_OFFICIAL_REVIEWS, t.getOfficialReviewsRemaining());
        update(path, Team.EVENT_IN_TIMEOUT, t.inTimeout());
        update(path, Team.EVENT_IN_OFFICIAL_REVIEW, t.inOfficialReview());
        update(path, Team.EVENT_RETAINED_OFFICIAL_REVIEW, t.retainedOfficialReview());
        update(path, Team.EVENT_DISPLAY_LEAD, t.displayLead());
        update(path, Team.EVENT_LOST, t.isLost());
        update(path, Team.EVENT_LEAD, t.isLead());
        update(path, Team.EVENT_CALLOFF, t.isCalloff());
        update(path, Team.EVENT_INJURY, t.isInjury());
        update(path, Team.EVENT_STAR_PASS, t.isStarPass());

        for (FloorPosition fp: FloorPosition.values()) {
            update(path, fp.toString(), t.getCurrentSkater(fp));
        }
        
        // Skaters
        for (Skater s : t.getSkaters()) {
            processSkater(s);
        }

        // Alternate Names
        for (Team.AlternateName an : t.getAlternateNames()) {
            processAlternateName(an);
        }

        // Colors
        for (Team.Color c : t.getColors()) {
            processColor(c);
        }
    }

    private void processAlternateName(Team.AlternateName an) {
        String path = getPath(an.getTeam());
        remove(path);

        update(path, "AlternateName(" + an.getId() + ")", an.getName());
    }

    private void processColor(Team.Color c) {
        String path = getPath(c.getTeam());
        remove(path);

        update(path, "Color(" + c.getId() + ")", c.getColor());
    }

    private void processSkater(Skater s) {
        String path = getPath(s);
        remove(path);

        update(path, Skater.EVENT_NAME, s.getName());
        update(path, Skater.EVENT_NUMBER, s.getNumber());
        update(path, Skater.EVENT_POSITION, s.getPosition());
        update(path, Skater.EVENT_FLAGS, s.getFlags());
        update(path, Skater.EVENT_PENALTY_BOX, s.isInBox());
        if (s.getFOEXPPenalty() != null) {
            update(path, Skater.EVENT_PENALTY_FOEXP, s.getFOEXPPenalty().getId());
        }

        for (Penalty p : s.getPenalties()) {
            processPenalty(p);
        }
    }

    private void processPenalty(Penalty p) {
	String path = getPath(p);
        remove(path);
	
        update(path, "Id", p.getId());
        update(path, Penalty.EVENT_JAM, p.getJam().getNumber());
        update(path, Penalty.EVENT_PERIOD, p.getJam().getPeriod().getNumber());
        update(path, Penalty.EVENT_CODE, p.getCode());
        update(path, Penalty.EVENT_NUMBER, p.getNumber());
        update(path, Penalty.EVENT_EXPULSION, p.isExpulsion());
        update(path, Penalty.EVENT_SERVED, p.isServed());
        
        for (BoxTrip bt : p.getBoxTrips()) {
            update(path, "BoxTrip(" + bt.getId() + ")", bt.getId());
        }
    }

    private void processPeriod(Period p) {
	String path = getPath(p);
        remove(path);
	
	update(path, "Id", p.getId());
        update(path, Period.EVENT_NUMBER, p.getNumber());
        update(path, Period.EVENT_CURRENT, p.isCurrent());
        update(path, Period.EVENT_RUNNING, p.isRunning());
        update(path, Period.EVENT_DURATION, p.getDuration());
        update(path, Period.EVENT_WALLTIME_START, p.getWalltimeStart());
        update(path, Period.EVENT_WALLTIME_END, p.getWalltimeEnd());
        if (p.getPrevious() != null) {
            update(path, Period.EVENT_PREVIOUS, p.getPrevious().getId());
        }
        if (p.getNext() != null) {
            update(path, Period.EVENT_NEXT, p.getNext().getId());
        }

        for (Jam j : p.getJams()) {
            processJam(j);
        }
	
        for (Timeout t : p.getTimeouts()) {
            processTimeout(t);
        }
    }
    
    private void processTimeout(Timeout t) {
	String path = getPath(t);
        remove(path);
	
        update(path, Timeout.EVENT_OWNER, t.getOwner().toString());
        update(path, Timeout.EVENT_REVIEW, t.isOfficialReview());
        update(path, Timeout.EVENT_CURRENT, t.isCurrent());
        update(path, Timeout.EVENT_RUNNING, t.isRunning());
        update(path, Timeout.EVENT_DURATION, t.getDuration());
        update(path, Timeout.EVENT_PERIOD_CLOCK_START, t.getPeriodClockElapsedStart());
        update(path, Timeout.EVENT_PERIOD_CLOCK_END, t.getPeriodClockElapsedEnd());
        update(path, Timeout.EVENT_WALLTIME_START, t.getWalltimeStart());
        update(path, Timeout.EVENT_WALLTIME_END, t.getWalltimeEnd());
        update(path, Timeout.EVENT_PRECEDING_JAM, t.getPrecedingJam().getId());
    }

    private void processJam(Jam j) {
	String path = getPath(j);
        remove(path);
	
	update(path, "Id", j.getId());
        update(path, Jam.EVENT_NUMBER, j.getNumber());
        update(path, Jam.EVENT_INJURY, j.getInjury());
        update(path, Jam.EVENT_CURRENT, j.isCurrent());
        update(path, Jam.EVENT_RUNNING, j.isRunning());
        update(path, Jam.EVENT_DURATION, j.getDuration());
        update(path, Jam.EVENT_PERIOD_CLOCK_START, j.getPeriodClockElapsedStart());
        update(path, Jam.EVENT_PERIOD_CLOCK_END, j.getPeriodClockElapsedEnd());
        update(path, Jam.EVENT_WALLTIME_START, j.getWalltimeStart());
        update(path, Jam.EVENT_WALLTIME_END, j.getWalltimeEnd());
        if (j.getPrevious() != null) {
            update(path, Jam.EVENT_PREVIOUS, j.getPrevious().getId());
        }
        if (j.getNext() != null) {
            update(path, Jam.EVENT_NEXT, j.getNext().getId());
        }

        for (TeamJam tj : j.getTeamJams()) {
            processTeamJam(tj);
        }
        
        for (Penalty p : j.getPenalties()) {
            update(path, "Penalty(" + p.getId() + ")", p.getId());
        }

        for (Timeout t : j.getTimeoutsAfter()) {
            update(path, "Timeout(" + t.getId() + ")", t.getId());
        }
}

    private void processTeamJam(TeamJam tj) {
	String path = getPath(tj);
        remove(path);
	
	update(path, "Id", tj.getId());
	update(path, TeamJam.EVENT_OS_OFFSET, tj.getOsOffset());
        update(path, TeamJam.EVENT_OS_OFFSET_REASON, tj.getOsOffsetReason());
        update(path, TeamJam.EVENT_SCORE, tj.getTotalScore());
        update(path, TeamJam.EVENT_JAM_SCORE, tj.getJamScore());
        update(path, TeamJam.EVENT_LOST, tj.isLost());
        update(path, TeamJam.EVENT_LEAD, tj.isLead());
        if (tj.getStarPassTrip() != null) {
            update(path, TeamJam.EVENT_STAR_PASS_TRIP, tj.getStarPassTrip().getId());
            update(path, TeamJam.EVENT_STAR_PASS_TRIP_NUMBER, tj.getStarPassTrip().getNumber());
        } else {
            update(path, TeamJam.EVENT_STAR_PASS_TRIP_NUMBER, 0);
        }
        update(path, TeamJam.EVENT_NO_PIVOT, tj.hasNoPivot());

        for (ScoringTrip st : tj.getScoringTrips()) {
            processScoringTrip(st);
        }
        for (FloorPosition fp: FloorPosition.values()) {
            Fielding f = tj.getFielding(fp);
            if (f != null) {
        	processFielding(f);
            }
        }
    }

    private void processScoringTrip(ScoringTrip st) {
	String path = getPath(st);
        remove(path);
	
	update(path, "Id", st.getId());
        update(path, ScoringTrip.EVENT_POINTS, st.getPoints());
        update(path, ScoringTrip.EVENT_NUMBER, st.getNumber());
        update(path, ScoringTrip.EVENT_AFTER_SP, st.isAfterSP());
        update(path, ScoringTrip.EVENT_DURATION, st.getDuration());
        update(path, ScoringTrip.EVENT_JAM_CLOCK_START, st.getJamClockElapsedStart());
        update(path, ScoringTrip.EVENT_JAM_CLOCK_END, st.getJamClockElapsedEnd());
        update(path, ScoringTrip.EVENT_WALLTIME_START, st.getWalltimeStart());
        update(path, ScoringTrip.EVENT_WALLTIME_END, st.getWalltimeEnd());
        if (st.getPrevious() != null) {
            update(path, ScoringTrip.EVENT_PREVIOUS, st.getPrevious().getId());
        }
        if (st.getNext() != null) {
            update(path, ScoringTrip.EVENT_NEXT, st.getNext().getId());
        }
    }

    private void processFielding(Fielding f) {
	String path = getPath(f);
        remove(path);
	
	update(path, "Id", f.getId());
        update(path, Fielding.EVENT_SKATER, f.getSkater().getId());
        update(path, Fielding.EVENT_POSITION, f.getFloorPosition());
        update(path, Fielding.EVENT_3_JAMS, f.gotSat3Jams());

        for (BoxTrip bt : f.getBoxTrips()) {
            if (f == bt.getFieldings().first()) {
        	processBoxTrip(bt);
        	update(path, "BoxTripCodeBeforeSP(" + bt.getId() + ")", bt.getNotation(f, false));
        	update(path, "BoxTripCodeAfterSP("+ bt.getId() + ")", bt.getNotation(f, true));
            } else {
        	update(path, "BoxTripCodeBeforeSP(" + bt.getId() + ")", bt.getNotation(f, false));
        	update(path, "BoxTripCodeAfterSP("+ bt.getId() + ")", bt.getNotation(f, true));
            }
        }
    }

    private void processPenaltyCodes(Settings s) {
        updates.add(new WSUpdate("ScoreBoard.PenaltyCode", null));
        String file = s.get(PenaltyCodesManager.SETTING_PENALTIES_FILE);
        if(file != null && !file.isEmpty()) {
            PenaltyCodesDefinition penalties = pm.loadFromJSON(file);
            for(PenaltyCode p : penalties.getPenalties()) {
                updates.add(new WSUpdate("ScoreBoard.PenaltyCode."+p.getCode(), p.CuesForWS(p)));
            }
            updates.add(new WSUpdate("ScoreBoard.PenaltyCode.?","Unknown"));
        }

    }

    private void processBoxTrip(BoxTrip bt) {
	String path = getPath(bt);
        remove(path);
	
	update(path, BoxTrip.EVENT_CURRENT, bt.isCurrent());
        update(path, BoxTrip.EVENT_START_AFTER_SP, bt.startedAfterStarPass());
        update(path, BoxTrip.EVENT_END_AFTER_SP, bt.endedAfterStarPass());
        update(path, BoxTrip.EVENT_START_BETWEEN, bt.startedBetweenJams());
        update(path, BoxTrip.EVENT_END_BETWEEN, bt.endedBetweenJams());

        for (Penalty p : bt.getPenalties()) {
            update(path, "Penalty(" + p.getId() + ")", p.getId());
        }
        for (Fielding f: bt.getFieldings()) {
            update(path, "Fielding(" + f.getId() + ")", f.getId());
            if (bt.getAnnotation(f) != null) {
                update(path, "Annotation(" + f.getId() + ")", bt.getAnnotation(f));
            }
        }
    }

    private void initialize(ScoreBoard sb) {
        update("ScoreBoard", ScoreBoard.EVENT_IN_PERIOD, sb.isInPeriod());
        update("ScoreBoard", ScoreBoard.EVENT_IN_OVERTIME, sb.isInOvertime());
        update("ScoreBoard", ScoreBoard.EVENT_OFFICIAL_SCORE, sb.isOfficialScore());
        update("ScoreBoard", ScoreBoard.EVENT_RULESET, sb.getRuleset());
        update("ScoreBoard", ScoreBoard.EVENT_TIMEOUT_OWNER, sb.getTimeoutOwner());
        update("ScoreBoard", ScoreBoard.EVENT_OFFICIAL_REVIEW, sb.isOfficialReview());
        update("ScoreBoard", ScoreBoard.EVENT_GAME_DURATION, sb.getGameDuration());
        update("ScoreBoard", ScoreBoard.EVENT_GAME_WALLTIME_START, sb.getGameWalltimeStart());
        update("ScoreBoard", ScoreBoard.EVENT_GAME_WALLTIME_END, sb.getGameWalltimeEnd());



        // Process Settings
        processSettings("ScoreBoard", sb.getSettings());
        processFrontendSettings("ScoreBoard", sb.getFrontendSettings());

        processPenaltyCodes(sb.getSettings());

        // Process Teams
        for (Team t : sb.getTeams()) {
            processTeam(t);
        }

        // Process Clocks
        for (Clock c : sb.getClocks()) {
            processClock(c);
        }

        updateState();
    }


    private JSONStateManager jsm;
    private PenaltyCodesManager pm = new PenaltyCodesManager();
    private List<WSUpdate> updates = new LinkedList<WSUpdate>();
    private long batch = 0;
}
