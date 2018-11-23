package com.carolinarollergirls.scoreboard.xml;
/**
 * Copyright (C) 2008-2012 Mr Temper <MrTemper@CarolinaRollergirls.com>
 *
 * This file is part of the Carolina Rollergirls (CRG) ScoreBoard.
 * The CRG ScoreBoard is licensed under either the GNU General Public
 * License version 3 (or later), or the Apache License 2.0, at your option.
 * See the file COPYING for details.
 */

import java.util.Iterator;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import com.carolinarollergirls.scoreboard.defaults.DefaultBoxTripModel;
import com.carolinarollergirls.scoreboard.defaults.DefaultFieldingModel;
import com.carolinarollergirls.scoreboard.defaults.DefaultJamModel;
import com.carolinarollergirls.scoreboard.defaults.DefaultPenaltyModel;
import com.carolinarollergirls.scoreboard.defaults.DefaultPeriodModel;
import com.carolinarollergirls.scoreboard.defaults.DefaultScoringTripModel;
import com.carolinarollergirls.scoreboard.defaults.DefaultSkaterModel;
import com.carolinarollergirls.scoreboard.defaults.DefaultTimeoutModel;
import com.carolinarollergirls.scoreboard.model.BoxTripModel;
import com.carolinarollergirls.scoreboard.model.ClockModel;
import com.carolinarollergirls.scoreboard.model.FieldingModel;
import com.carolinarollergirls.scoreboard.model.FrontendSettingsModel;
import com.carolinarollergirls.scoreboard.model.JamModel;
import com.carolinarollergirls.scoreboard.model.PenaltyModel;
import com.carolinarollergirls.scoreboard.model.PeriodModel;
import com.carolinarollergirls.scoreboard.model.ScoreBoardModel;
import com.carolinarollergirls.scoreboard.model.ScoringTripModel;
import com.carolinarollergirls.scoreboard.model.SettingsModel;
import com.carolinarollergirls.scoreboard.model.SkaterModel;
import com.carolinarollergirls.scoreboard.model.TeamJamModel;
import com.carolinarollergirls.scoreboard.model.TeamModel;
import com.carolinarollergirls.scoreboard.model.TimeoutModel;
import com.carolinarollergirls.scoreboard.view.BoxTrip;
import com.carolinarollergirls.scoreboard.view.Clock;
import com.carolinarollergirls.scoreboard.view.Fielding;
import com.carolinarollergirls.scoreboard.view.FloorPosition;
import com.carolinarollergirls.scoreboard.view.FrontendSettings;
import com.carolinarollergirls.scoreboard.view.Jam;
import com.carolinarollergirls.scoreboard.view.Penalty;
import com.carolinarollergirls.scoreboard.view.Period;
import com.carolinarollergirls.scoreboard.view.Position;
import com.carolinarollergirls.scoreboard.view.ScoreBoard;
import com.carolinarollergirls.scoreboard.view.ScoringTrip;
import com.carolinarollergirls.scoreboard.view.Settings;
import com.carolinarollergirls.scoreboard.view.Skater;
import com.carolinarollergirls.scoreboard.view.Team;
import com.carolinarollergirls.scoreboard.view.TeamJam;
import com.carolinarollergirls.scoreboard.view.Timeout;
import com.carolinarollergirls.scoreboard.view.Timeout.TimeoutOwner;

public class ScoreBoardXmlConverter {
    /*****************************/
    /* ScoreBoard to XML methods */

    public String toString(ScoreBoard scoreBoard) {
        return rawXmlOutputter.outputString(toDocument(scoreBoard));
    }

    public Document toDocument(ScoreBoard scoreBoard) {
        Element sb = new Element("ScoreBoard");
        Document d = new Document(new Element("document").addContent(sb));
     
        toElement(d.getRootElement(), scoreBoard);
        
        return d;
    }
    
    public Element toElement(Element p, ScoreBoard scoreBoard) {
	Element sb = editor.setElement(p, "ScoreBoard");
	
        editor.setElement(sb, "Reset", null, "");
        editor.setElement(sb, "StartJam", null, "");
        editor.setElement(sb, "StopJam", null, "");
        editor.setElement(sb, "Timeout", null, "");
        editor.setElement(sb, "ClockUndo", null, "");
        editor.setElement(sb, "ClockReplace", null, "");
        editor.setElement(sb, "StartOvertime", null, "");
        editor.setElement(sb, "OfficialTimeout", null, "");

        editor.setElement(sb, ScoreBoard.EVENT_TIMEOUT_OWNER, null, scoreBoard.getTimeoutOwner().getId());
        editor.setElement(sb, ScoreBoard.EVENT_OFFICIAL_REVIEW, null, String.valueOf(scoreBoard.isOfficialReview()));
        editor.setElement(sb, ScoreBoard.EVENT_IN_PERIOD, null, String.valueOf(scoreBoard.isInPeriod()));
        editor.setElement(sb, ScoreBoard.EVENT_IN_OVERTIME, null, String.valueOf(scoreBoard.isInOvertime()));
        editor.setElement(sb, ScoreBoard.EVENT_OFFICIAL_SCORE, null, String.valueOf(scoreBoard.isOfficialScore()));
        editor.setElement(sb, ScoreBoard.EVENT_RULESET, null, String.valueOf(scoreBoard.getRuleset()));
        editor.setElement(sb, ScoreBoard.EVENT_GAME_DURATION, null, String.valueOf(scoreBoard.getGameDuration()));
        editor.setElement(sb, ScoreBoard.EVENT_GAME_WALLTIME_START, null, String.valueOf(scoreBoard.getGameWalltimeStart()));
        editor.setElement(sb, ScoreBoard.EVENT_GAME_WALLTIME_END, null, String.valueOf(scoreBoard.getGameWalltimeEnd()));

        toElement(sb, scoreBoard.getSettings());
        toElement(sb, scoreBoard.getFrontendSettings());

        for (Clock clock : scoreBoard.getClocks()) {
            toElement(sb, clock);
        }

        for (Team team : scoreBoard.getTeams()) {
            toElement(sb, team);
        }

        for (Period period : scoreBoard.getPeriods()) {
            toElement(sb, period);
        }

        return sb;
    }

    public Element toElement(Element p, Settings s) {
        Element e = editor.setElement(p, "Settings");
        Iterator<String> keys = s.getAll().keySet().iterator();
        while (keys.hasNext()) {
            String k = keys.next();
            String v = s.get(k);
            if (v != null) {
                editor.setElement(e, Settings.EVENT_SETTING, k, v);
            }
        }
        return e;
    }

    public Element toElement(Element p, FrontendSettings s) {
        Element e = editor.setElement(p, "FrontendSettings");
        Iterator<String> keys = s.getAll().keySet().iterator();
        while (keys.hasNext()) {
            String k = keys.next();
            String v = s.get(k);
            if (v != null) {
                editor.setElement(e, FrontendSettings.EVENT_SETTING, k, v);
            }
        }
        return e;
    }

    public Element toElement(Element sb, Clock c) {
        Element e = editor.setElement(sb, "Clock", c.getId());

        editor.setElement(e, "Start", null, "");
        editor.setElement(e, "UnStart", null, "");
        editor.setElement(e, "Stop", null, "");
        editor.setElement(e, "UnStop", null, "");
        editor.setElement(e, "ResetTime", null, "");

        editor.setElement(e, Clock.EVENT_NAME, null, c.getName());
        editor.setElement(e, Clock.EVENT_NUMBER, null, String.valueOf(c.getNumber()));
        editor.setElement(e, Clock.EVENT_TIME, null, String.valueOf(c.getTime()));
        editor.setElement(e, Clock.EVENT_INVERTED_TIME, null, String.valueOf(c.getInvertedTime()));
        editor.setElement(e, Clock.EVENT_MINIMUM_TIME, null, String.valueOf(c.getMinimumTime()));
        editor.setElement(e, Clock.EVENT_MAXIMUM_TIME, null, String.valueOf(c.getMaximumTime()));
        editor.setElement(e, Clock.EVENT_RUNNING, null, String.valueOf(c.isRunning()));
        editor.setElement(e, Clock.EVENT_DIRECTION, null, String.valueOf(c.isCountDirectionDown()));
        return e;
    }

    public Element toElement(Element sb, Team t) {
        Element e = editor.setElement(sb, "Team", t.getId());

        editor.setElement(e, "Timeout", null, "");
        editor.setElement(e, "OfficialReview", null, "");

        editor.setElement(e, Team.EVENT_NAME, null, t.getName());
        editor.setElement(e, Team.EVENT_LOGO, null, t.getLogo());
        editor.setElement(e, Team.EVENT_SCORE, null, String.valueOf(t.getScore()));
        editor.setElement(e, Team.EVENT_JAM_SCORE, null, String.valueOf(t.getJamScore()));
        editor.setElement(e, Team.EVENT_TIMEOUTS, null, String.valueOf(t.getTimeoutsRemaining()));
        editor.setElement(e, Team.EVENT_OFFICIAL_REVIEWS, null, String.valueOf(t.getOfficialReviewsRemaining()));
        editor.setElement(e, Team.EVENT_IN_TIMEOUT, null, String.valueOf(t.inTimeout()));
        editor.setElement(e, Team.EVENT_IN_OFFICIAL_REVIEW, null, String.valueOf(t.inOfficialReview()));
        editor.setElement(e, Team.EVENT_RETAINED_OFFICIAL_REVIEW, null, String.valueOf(t.retainedOfficialReview()));
        editor.setElement(e, Team.EVENT_DISPLAY_LEAD, null, String.valueOf(t.displayLead()));
        editor.setElement(e, Team.EVENT_LOST, null, String.valueOf(t.isLost()));
        editor.setElement(e, Team.EVENT_LEAD, null, String.valueOf(t.isLead()));
        editor.setElement(e, Team.EVENT_CALLOFF, null, String.valueOf(t.isCalloff()));
        editor.setElement(e, Team.EVENT_INJURY, null, String.valueOf(t.isInjury()));
        editor.setElement(e, Team.EVENT_STAR_PASS, null, String.valueOf(t.isStarPass()));

	for (FloorPosition fp: FloorPosition.values()) {
	    setPosition(e, t, fp);
	}
        
        for (Team.AlternateName alternateName : t.getAlternateNames()) {
            toElement(e, alternateName);
        }

        for (Team.Color color : t.getColors()) {
            toElement(e, color);
        }

        for (Skater skater : t.getSkaters()) {
            toElement(e, skater);
        }

        return e;
    }
    public void setPosition(Element teamElement, Team team, FloorPosition fp) {
	Skater s = team.getCurrentSkater(fp);
	Element e = editor.setElement(teamElement, "Position", fp.toString());
	if (s != null) {
	    editor.setElement(e, "Id", null, s.getId());
	    editor.setElement(e, Skater.EVENT_NAME, null, s.getName());
	    editor.setElement(e, Skater.EVENT_NUMBER, null, s.getNumber());
	    editor.setElement(e, Skater.EVENT_PENALTY_BOX, null, String.valueOf(s.isInBox()));
	    editor.setElement(e, Skater.EVENT_FLAGS, null, s.getFlags());
	} else {
	    editor.setElement(e, "Id", null, "");
	    editor.setElement(e, Skater.EVENT_NAME, null, "");
	    editor.setElement(e, Skater.EVENT_NUMBER, null, "");
	    editor.setElement(e, Skater.EVENT_PENALTY_BOX, null, String.valueOf(false));
	    editor.setElement(e, Skater.EVENT_FLAGS, null, "");
	}
    }

    public Element toElement(Element team, Team.AlternateName n) {
        Element e = editor.setElement(team, "AlternateName", n.getId());

        editor.setElement(e, Team.AlternateName.EVENT_NAME, null, n.getName());

        return e;
    }

    public Element toElement(Element team, Team.Color c) {
        Element e = editor.setElement(team, "Color", c.getId());

        editor.setElement(e, Team.Color.EVENT_COLOR, null, c.getColor());

        return e;
    }

    public Element toElement(Element t, Skater s) {
        Element e = editor.setElement(t, "Skater", s.getId());
        editor.setElement(e, Skater.EVENT_NAME, null, s.getName());
        editor.setElement(e, Skater.EVENT_NUMBER, null, s.getNumber());
        editor.setElement(e, Skater.EVENT_POSITION, null, s.getPosition().toString());
        editor.setElement(e, Skater.EVENT_PENALTY_BOX, null, String.valueOf(s.isInBox()));
        editor.setElement(e, Skater.EVENT_FLAGS, null, s.getFlags());

        for (Penalty p: s.getPenalties()) {
            toElement(e, p);
        }

        if (s.getFOEXPPenalty() != null) {
            editor.setElement(e, Skater.EVENT_PENALTY_FOEXP, null, s.getFOEXPPenalty().getId());
        }

        return e;
    }

    public Element toElement(Element s, Penalty p) {
        Element e = editor.setElement(s, "Penalty", p.getId());
        editor.setElement(e, Penalty.EVENT_CODE, null, p.getCode());
        editor.setElement(e, Penalty.EVENT_JAM, null, String.valueOf(p.getJam().getNumber()));
        editor.setElement(e, Penalty.EVENT_PERIOD, null, String.valueOf(p.getJam().getPeriod().getNumber()));
        editor.setElement(e, Penalty.EVENT_NUMBER, null, String.valueOf(p.getNumber()));
        editor.setElement(e, Penalty.EVENT_EXPULSION, null, String.valueOf(p.isExpulsion()));
        editor.setElement(e, Penalty.EVENT_SERVED, null, String.valueOf(p.isServed()));
        for (BoxTrip b: p.getBoxTrips()) {
            editor.setElement(e, "BoxTrip", b.getId());
        }
        return e;
    }

    public Element toElement(Element sb, Period p) {
        Element e = editor.setElement(sb, "Period", p.getId());
        editor.setElement(e, Period.EVENT_NUMBER, null, String.valueOf(p.getNumber()));
        editor.setElement(e, Period.EVENT_CURRENT, null, String.valueOf(p.isCurrent()));
        editor.setElement(e, Period.EVENT_RUNNING, null, String.valueOf(p.isRunning()));
        editor.setElement(e, Period.EVENT_DURATION, null, String.valueOf(p.getDuration()));
        editor.setElement(e, Period.EVENT_WALLTIME_START, null, String.valueOf(p.getWalltimeStart()));
        editor.setElement(e, Period.EVENT_WALLTIME_END, null, String.valueOf(p.getWalltimeEnd()));
        if (p.getPrevious() != null) {
            editor.setElement(e, Period.EVENT_PREVIOUS, null, p.getPrevious().getId());
        }
        if (p.getNext() != null) {
            editor.setElement(e, Period.EVENT_NEXT, null, p.getNext().getId());
        }
        for (Jam j: p.getJams()) {
            toElement(e, j);
        }
        for (Timeout t: p.getTimeouts()) {
            toElement(e, t);
        }
        return e;
    }

    public Element toElement(Element p, Timeout t) {
        Element e = editor.setElement(p, "Timeout", t.getId());
        editor.setElement(e, Timeout.EVENT_OWNER, null, t.getOwner().toString());
        editor.setElement(e, Timeout.EVENT_REVIEW, null, String.valueOf(t.isOfficialReview()));
        editor.setElement(e, Timeout.EVENT_CURRENT, null, String.valueOf(t.isCurrent()));
        editor.setElement(e, Timeout.EVENT_RUNNING, null, String.valueOf(t.isRunning()));
        editor.setElement(e, Timeout.EVENT_DURATION, null, String.valueOf(t.getDuration()));
        editor.setElement(e, Timeout.EVENT_PERIOD_CLOCK_START, null, String.valueOf(t.getPeriodClockElapsedStart()));
        editor.setElement(e, Timeout.EVENT_PERIOD_CLOCK_END, null, String.valueOf(t.getPeriodClockElapsedEnd()));
        editor.setElement(e, Timeout.EVENT_WALLTIME_START, null, String.valueOf(t.getWalltimeStart()));
        editor.setElement(e, Timeout.EVENT_WALLTIME_END, null, String.valueOf(t.getWalltimeEnd()));
        editor.setElement(e, Timeout.EVENT_PRECEDING_JAM, null, t.getPrecedingJam().getId());
        return e;
    }

    public Element toElement(Element p, Jam j) {
        Element e = editor.setElement(p, "Jam", j.getId());
        editor.setElement(e, Jam.EVENT_NUMBER, null, String.valueOf(j.getNumber()));
        editor.setElement(e, Jam.EVENT_INJURY, null, String.valueOf(j.getInjury()));
        editor.setElement(e, Jam.EVENT_CURRENT, null, String.valueOf(j.isCurrent()));
        editor.setElement(e, Jam.EVENT_RUNNING, null, String.valueOf(j.isRunning()));
        editor.setElement(e, Jam.EVENT_DURATION, null, String.valueOf(j.getDuration()));
        editor.setElement(e, Jam.EVENT_PERIOD_CLOCK_START, null, String.valueOf(j.getPeriodClockElapsedStart()));
        editor.setElement(e, Jam.EVENT_PERIOD_CLOCK_END, null, String.valueOf(j.getPeriodClockElapsedEnd()));
        editor.setElement(e, Jam.EVENT_WALLTIME_START, null, String.valueOf(j.getWalltimeStart()));
        editor.setElement(e, Jam.EVENT_WALLTIME_END, null, String.valueOf(j.getWalltimeEnd()));
        if (j.getPrevious() != null) {
            editor.setElement(e, Jam.EVENT_PREVIOUS, null, j.getPrevious().getId());
        }
        if (j.getNext() != null) {
            editor.setElement(e, Jam.EVENT_NEXT, null, j.getNext().getId());
        }
        for (TeamJam tj: j.getTeamJams()) {
            toElement(e, tj);
        }
        for (Penalty pe: j.getPenalties()) {
            editor.setElement(e, "Penalty", pe.getId());
        }
        for (Timeout t: j.getTimeoutsAfter()) {
            editor.setElement(e, "Timeout", t.getId());
        }
        return e;
    }

    public Element toElement(Element j, TeamJam tj) {
        Element e = editor.setElement(j, "TeamJam", tj.getId());
        editor.setElement(e, TeamJam.EVENT_OS_OFFSET, null, String.valueOf(tj.getOsOffset()));
        editor.setElement(e, TeamJam.EVENT_OS_OFFSET_REASON, null, String.valueOf(tj.getOsOffsetReason()));
        editor.setElement(e, TeamJam.EVENT_JAM_SCORE, null, String.valueOf(tj.getJamScore()));
        editor.setElement(e, TeamJam.EVENT_SCORE, null, String.valueOf(tj.getTotalScore()));
        editor.setElement(e, TeamJam.EVENT_LOST, null, String.valueOf(tj.isLost()));
        editor.setElement(e, TeamJam.EVENT_LEAD, null, String.valueOf(tj.isLead()));
        editor.setElement(e, TeamJam.EVENT_CALLOFF, null, String.valueOf(tj.isCalloff()));
        if (tj.getStarPassTrip() != null) {
            editor.setElement(e, TeamJam.EVENT_STAR_PASS_TRIP, null, tj.getStarPassTrip().toString());
        }
        editor.setElement(e, TeamJam.EVENT_NO_PIVOT, null, String.valueOf(tj.hasNoPivot()));
        for (ScoringTrip st : tj.getScoringTrips()) {
            toElement(e, st);
        }
        for (FloorPosition fp: FloorPosition.values()) {
            Fielding f = tj.getFielding(fp);
            if (f != null) {
        	toElement(e, f);
            }
        }
        return e;
    }

    public Element toElement(Element t, ScoringTrip st) {
        Element e = editor.setElement(t, "ScoringTrip", st.getId());
        editor.setElement(e, ScoringTrip.EVENT_POINTS, null, String.valueOf(st.getPoints()));
        editor.setElement(e, ScoringTrip.EVENT_NUMBER, null, String.valueOf(st.getNumber()));
        editor.setElement(e, ScoringTrip.EVENT_AFTER_SP, null, String.valueOf(st.isAfterSP()));
        editor.setElement(e, ScoringTrip.EVENT_DURATION, null, String.valueOf(st.getDuration()));
        editor.setElement(e, ScoringTrip.EVENT_JAM_CLOCK_START, null, String.valueOf(st.getJamClockElapsedStart()));
        editor.setElement(e, ScoringTrip.EVENT_JAM_CLOCK_END, null, String.valueOf(st.getJamClockElapsedEnd()));
        editor.setElement(e, ScoringTrip.EVENT_WALLTIME_START, null, String.valueOf(st.getWalltimeStart()));
        editor.setElement(e, ScoringTrip.EVENT_WALLTIME_END, null, String.valueOf(st.getWalltimeEnd()));
        if (st.getPrevious() != null) {
            editor.setElement(e, ScoringTrip.EVENT_PREVIOUS, null, st.getPrevious().getId());
        }
        if (st.getNext() != null) {
            editor.setElement(e, ScoringTrip.EVENT_NEXT, null, st.getNext().getId());
        }
        return e;
    }

    public Element toElement(Element t, Fielding f) {
        Element e = editor.setElement(t, "Fielding", f.getId());
        editor.setElement(e, Fielding.EVENT_SKATER, f.getSkater().getId());
        editor.setElement(e, Fielding.EVENT_POSITION, null, f.getFloorPosition().toString());
        editor.setElement(e, Fielding.EVENT_3_JAMS, null, String.valueOf(f.gotSat3Jams()));
        for (BoxTrip bt : f.getBoxTrips()) {
            if (f == bt.getFieldings().first()) {
        	toElement(e, bt);
            } else {
        	editor.setElement(e, "BoxTrip", bt.getId());
            }
        }
        return e;
    }

    public Element toElement(Element fe, BoxTrip bt) {
        Element e = editor.setElement(fe, "BoxTrip", bt.getId());
        editor.setElement(e, BoxTrip.EVENT_CURRENT, null, String.valueOf(bt.isCurrent()));
        editor.setElement(e, BoxTrip.EVENT_START_BETWEEN, null, String.valueOf(bt.startedBetweenJams()));
        editor.setElement(e, BoxTrip.EVENT_END_BETWEEN, null, String.valueOf(bt.endedBetweenJams()));
        editor.setElement(e, BoxTrip.EVENT_START_AFTER_SP, null, String.valueOf(bt.startedAfterStarPass()));
        editor.setElement(e, BoxTrip.EVENT_END_AFTER_SP, null, String.valueOf(bt.endedAfterStarPass()));
        for (Penalty p : bt.getPenalties()) {
            editor.setElement(e, "Penalty", p.getId());
        }
        for (Fielding f : bt.getFieldings()) {
            editor.setElement(e, "Fielding", f.getId());
            if (bt.getAnnotation(f) != null) {
        	editor.setElement(e, BoxTrip.EVENT_ANNOTATION, f.getId(), bt.getAnnotation(f));
            }
        }
        return e;
    }

    /*****************************/
    /* XML to ScoreBoard methods */

    public void processDocument(ScoreBoardModel scoreBoardModel, Document document, boolean ignoreCompat) {
        Iterator<?> children = document.getRootElement().getChildren().iterator();
        while (children.hasNext()) {
            Element element = (Element)children.next();
            if (element.getName().equals("ScoreBoard")) {
                processScoreBoard(scoreBoardModel, element, ignoreCompat);
            }
        }
    }

    public void processScoreBoard(ScoreBoardModel scoreBoardModel, Element scoreBoard, boolean ignoreCompat) {
        Iterator<?> children = scoreBoard.getChildren().iterator();
        while (children.hasNext()) {
            Element element = (Element)children.next();
            try {
                String name = element.getName();
                String value = editor.getText(element);
                boolean bVal = Boolean.parseBoolean(value);

                if (name.equals("Clock")) {
                    processClock(scoreBoardModel, element);
                } else if (name.equals("Team")) {
                    processTeam(scoreBoardModel, element, ignoreCompat);
                } else if (name.equals("Settings")) {
                    processSettings(scoreBoardModel, element);
                } else if (name.equals("FrontendSettings")) {
                    processFrontendSettings(scoreBoardModel, element);
                } else if (name.equals("Period")) {
                    processPeriod(scoreBoardModel, element);
                } else if (null == value) {
                    continue;
                } else if (name.equals(ScoreBoard.EVENT_IN_OVERTIME)) {
                    scoreBoardModel.setInOvertime(bVal);
                } else if (name.equals(ScoreBoard.EVENT_RULESET)) {
                    scoreBoardModel.setRuleset(value);
                } else if (ignoreCompat) {
                    continue;
                } else if (name.equals(ScoreBoard.EVENT_TIMEOUT_OWNER) && scoreBoardModel.isInTimeout()) {
                    scoreBoardModel.getCurrentTimeoutModel().setOwner(scoreBoardModel.getTimeoutOwner(value));
                } else if (name.equals(ScoreBoard.EVENT_OFFICIAL_REVIEW) && scoreBoardModel.isInTimeout()) {
                    scoreBoardModel.getCurrentTimeoutModel().setOfficialReview(bVal);
                } else if (name.equals(ScoreBoard.EVENT_IN_PERIOD)) {
                    scoreBoardModel.getCurrentPeriodModel().setRunning(bVal);
                } else if (name.equals(ScoreBoard.EVENT_OFFICIAL_SCORE)) {
                    scoreBoardModel.setOfficialScore(bVal);
                } else if (bVal) {
                    if (name.equals("Reset")) {
                        scoreBoardModel.reset();
                    } else if (name.equals("StartJam")) {
                        scoreBoardModel.startJam();
                    } else if (name.equals("StopJam")) {
                        scoreBoardModel.stopJamTO();
                    } else if (name.equals("Timeout")) {
                        scoreBoardModel.timeout();
                    } else if (name.equals("ClockUndo")) {
                        scoreBoardModel.clockUndo(false);
                    } else if (name.equals("ClockReplace")) {
                        scoreBoardModel.clockUndo(true);
                    } else if (name.equals("StartOvertime")) {
                        scoreBoardModel.startOvertime();
                    } else if (name.equals("OfficialTimeout")) {
                        scoreBoardModel.setTimeoutType(scoreBoardModel.getTimeoutOwner(TimeoutOwner.OTO), false);
                    }
                }
            } catch ( Exception e ) {
            }
        }
    }

    public void processSettings(ScoreBoardModel scoreBoardModel, Element settings) {
        SettingsModel sm = scoreBoardModel.getSettingsModel();
        Iterator<?> children = settings.getChildren().iterator();
        while (children.hasNext()) {
            Element element = (Element)children.next();
            try {
                String k = element.getAttributeValue("Id");
                String v = editor.getText(element);
                if (v == null) {
                    v = "";
                }
                sm.set(k, v);
            } catch ( Exception e ) {
            }
        }
    }

    public void processFrontendSettings(ScoreBoardModel scoreBoardModel, Element settings) {
        FrontendSettingsModel sm = scoreBoardModel.getFrontendSettingsModel();
        Iterator<?> children = settings.getChildren().iterator();
        while (children.hasNext()) {
            Element element = (Element)children.next();
            try {
                String k = element.getAttributeValue("Id");
                String v = editor.getText(element);
                if (v == null) {
                    v = "";
                }
                sm.set(k, v);
            } catch ( Exception e ) {
            }
        }
    }

    public void processClock(ScoreBoardModel scoreBoardModel, Element clock) {
        String id = clock.getAttributeValue("Id");
        ClockModel clockModel = scoreBoardModel.getClockModel(id);
        boolean requestStart = false;
        boolean requestStop = false;

        Iterator<?> children = clock.getChildren().iterator();
        while (children.hasNext()) {
            Element element = (Element)children.next();
            try {
                String name = element.getName();
                String value = editor.getText(element);

                boolean isChange = Boolean.parseBoolean(element.getAttributeValue("change"));
                boolean isReset = Boolean.parseBoolean(element.getAttributeValue("reset"));

//FIXME - might be better way to handle changes/resets than an attribute...
                if ((null == value) && !isReset) {
                    continue;
                } else if (name.equals("Start") && Boolean.parseBoolean(value)) {
                    requestStart = true;
                } else if (name.equals("Stop") && Boolean.parseBoolean(value)) {
                    requestStop = true;
                } else if (name.equals("ResetTime") && Boolean.parseBoolean(value)) {
                    clockModel.resetTime();
                } else if (name.equals(Clock.EVENT_NAME)) {
                    clockModel.setName(value);
                } else if (name.equals(Clock.EVENT_TIME) && isChange) {
                    clockModel.changeTime(Long.parseLong(value));
                } else if (name.equals(Clock.EVENT_TIME) && isReset) {
                    clockModel.resetTime();
                } else if (name.equals(Clock.EVENT_TIME) && !isChange && !isReset) {
                    clockModel.setTime(Long.parseLong(value));
                } else if (name.equals(Clock.EVENT_MINIMUM_TIME) && isChange) {
                    clockModel.changeMinimumTime(Long.parseLong(value));
                } else if (name.equals(Clock.EVENT_MINIMUM_TIME)) {
                    clockModel.setMinimumTime(Long.parseLong(value));
                } else if (name.equals(Clock.EVENT_MAXIMUM_TIME) && isChange) {
                    clockModel.changeMaximumTime(Long.parseLong(value));
                } else if (name.equals(Clock.EVENT_MAXIMUM_TIME)) {
                    clockModel.setMaximumTime(Long.parseLong(value));
                } else if (name.equals(Clock.EVENT_RUNNING) && Boolean.parseBoolean(value)) {
                    requestStart = true;
                } else if (name.equals(Clock.EVENT_RUNNING) && !Boolean.parseBoolean(value)) {
                    requestStop = true;
                } else if (name.equals(Clock.EVENT_DIRECTION)) {
                    clockModel.setCountDirectionDown(Boolean.parseBoolean(value));
                }
            } catch ( Exception e ) {
            }
        }
        // Process start/stops at the end to allow setting of options (direction/min/max/etc) on load
        if (requestStart) { clockModel.start(); }
        if (requestStop) { clockModel.stop(); }
    }

    public void processTeam(ScoreBoardModel scoreBoardModel, Element team, boolean ignoreCompat) {
        String id = team.getAttributeValue("Id");
        TeamModel teamModel = scoreBoardModel.getTeamModel(id);

        Iterator<?> children = team.getChildren().iterator();
        while (children.hasNext()) {
            Element element = (Element)children.next();
            try {
                String name = element.getName();
                String value = editor.getText(element);

                boolean isChange = Boolean.parseBoolean(element.getAttributeValue("change"));

                if (name.equals("AlternateName")) {
                    processAlternateName(teamModel, element);
                } else if (name.equals("Color")) {
                    processColor(teamModel, element);
                } else if (name.equals("Skater")) {
                    processSkater(teamModel, element);
                } else if (name.equals("Position")) {
                    processPosition(teamModel, element);
                } else if (null == value) {
                    continue;
                } else if (name.equals("Timeout") && Boolean.parseBoolean(value)) {
                    teamModel.timeout();
                } else if (name.equals("OfficialReview") && Boolean.parseBoolean(value)) {
                    teamModel.officialReview();
                } else if (name.equals(Team.EVENT_ADD_TRIP) && Boolean.parseBoolean(value)) {
                    teamModel.addTrip();
                } else if (name.equals(Team.EVENT_REMOVE_TRIP) && Boolean.parseBoolean(value)) {
                    teamModel.removeTrip();
                } else if (name.equals(Team.EVENT_NAME)) {
                    teamModel.setName(value);
                } else if (name.equals(Team.EVENT_LOGO)) {
                    teamModel.setLogo(value);
                } else if (ignoreCompat) {
                    continue;
                } else if (name.equals(Team.EVENT_SCORE) && isChange) {
                    teamModel.changeScore(Integer.parseInt(value));
                } else if (name.equals(Team.EVENT_SCORE) && !isChange) {
                    teamModel.setScore(Integer.parseInt(value));
                } else if (name.equals(Team.EVENT_RETAINED_OFFICIAL_REVIEW)) {
                    teamModel.setRetainedOfficialReview(Boolean.parseBoolean(value));
                } else if (name.equals(Team.EVENT_LOST)) {
                    teamModel.getCurrentTeamJamModel().setLost(Boolean.parseBoolean(value));
                } else if (name.equals(Team.EVENT_LEAD)) {
                    teamModel.getCurrentTeamJamModel().setLead(Boolean.parseBoolean(value));
                } else if (name.equals(Team.EVENT_CALLOFF)) {
                    teamModel.getCurrentTeamJamModel().setCalloff(Boolean.parseBoolean(value));
                } else if (name.equals(Team.EVENT_INJURY)) {
                    teamModel.getCurrentTeamJamModel().setInjury(Boolean.parseBoolean(value));
                } else if (name.equals(Team.EVENT_STAR_PASS)) {
                    teamModel.getCurrentTeamJamModel().setStarPassTrip(
                	    Boolean.parseBoolean(value) ? teamModel.getCurrentTeamJamModel().getCurrentScoringTripModel() : null);
                }
            } catch ( Exception e ) {
            }
        }
    }

    public void processAlternateName(TeamModel teamModel, Element alternateName) {
        String id = alternateName.getAttributeValue("Id");
        TeamModel.AlternateNameModel alternateNameModel = teamModel.getAlternateNameModel(id);

        if (editor.hasRemovePI(alternateName)) {
            teamModel.removeAlternateNameModel(id);
            return;
        }

        if (null == alternateNameModel) {
            teamModel.setAlternateNameModel(id, "");
            alternateNameModel = teamModel.getAlternateNameModel(id);
        }

        Iterator<?> children = alternateName.getChildren().iterator();
        while (children.hasNext()) {
            Element element = (Element)children.next();
            try {
                String name = element.getName();
                String value = editor.getText(element);

                if (null == value) {
                    continue;
                } else if (name.equals(Team.AlternateName.EVENT_NAME)) {
                    alternateNameModel.setName(value);
                }
            } catch ( Exception e ) {
            }
        }
    }

    public void processColor(TeamModel teamModel, Element color) {
        String id = color.getAttributeValue("Id");
        TeamModel.ColorModel colorModel = teamModel.getColorModel(id);

        if (editor.hasRemovePI(color)) {
            teamModel.removeColorModel(id);
            return;
        }

        if (null == colorModel) {
            teamModel.setColorModel(id, "");
            colorModel = teamModel.getColorModel(id);
        }

        Iterator<?> children = color.getChildren().iterator();
        while (children.hasNext()) {
            Element element = (Element)children.next();
            try {
                String name = element.getName();
                String value = editor.getText(element);

                if (null == value) {
                    continue;
                } else if (name.equals(Team.Color.EVENT_COLOR)) {
                    colorModel.setColor(value);
                }
            } catch ( Exception e ) {
            }
        }
    }

    public void processPosition(TeamModel teamModel, Element position) {
        FloorPosition pos = FloorPosition.fromString(position.getAttributeValue("Id"));
        TeamJamModel tjm = teamModel.getCurrentTeamJamModel();
        ScoreBoardModel sbm = teamModel.getScoreBoardModel();
        if (!sbm.isInJam()) { tjm = tjm.getNext(); }

        Iterator<?> children = position.getChildren().iterator();
        while (children.hasNext()) {
            Element element = (Element)children.next();
            try {
                String name = element.getName();
                String value = editor.getText(element);

                if (null == value) {
                    continue;
                } else if (name.equals("Id") && value != "") {
                    tjm.fieldSkater(sbm.getSkaterModel(value), pos.toPosition());
                } else if (tjm.getFieldingModel(pos) == null) {
                    continue;
                } else if ((name.equals("Clear") && Boolean.parseBoolean(value)) ||
                	(name.equals("Id") && value == "")) {
                    tjm.fieldSkater(tjm.getFieldingModel(pos).getSkaterModel(), Position.BENCH);
                } else if (name.equals("PenaltyBox") && Boolean.parseBoolean(value)) {
                    tjm.getFieldingModel(pos).getSkaterModel().startBoxTrip(!sbm.isInJam(), tjm.isStarPass());
                } else if (name.equals("PenaltyBox") && !Boolean.parseBoolean(value)) {
                    tjm.getFieldingModel(pos).getSkaterModel().finishBoxTrip(!sbm.isInJam(), tjm.isStarPass());
                }
            } catch ( Exception e ) {
            }
        }
    }

    public void processSkater(TeamModel teamModel, Element skater) {
        String id = skater.getAttributeValue("Id");
        ScoreBoardModel scoreBoardModel = teamModel.getScoreBoardModel();
        SkaterModel skaterModel = scoreBoardModel.getSkaterModel(id);

        if (editor.hasRemovePI(skater) && skaterModel != null) {
            scoreBoardModel.deleteSkaterModel(skaterModel);
            return;
        }

        if (skaterModel == null) {
            Element nameE = skater.getChild(Skater.EVENT_NAME);
            String name = (nameE == null ? "" : editor.getText(nameE));
            Element numberE = skater.getChild(Skater.EVENT_NUMBER);
            String number = (numberE == null ? "" : editor.getText(numberE));
            Element flagsE = skater.getChild(Skater.EVENT_FLAGS);
            String flags = (flagsE == null ? "" : editor.getText(flagsE));
            skaterModel = new DefaultSkaterModel(teamModel, id, name, number, flags);
            teamModel.addSkaterModel(skaterModel);
        } else {
            Iterator<?> children = skater.getChildren().iterator();
            while (children.hasNext()) {
        	Element element = (Element)children.next();
        	try {
        	    String name = element.getName();
        	    String value = editor.getText(element);

        	    if (name.equals("Penalty")) {
        		processPenalty(skaterModel, element);
        	    } else if (null == value) {
        		continue;
        	    } else if (name.equals(Skater.EVENT_NAME)) {
        		skaterModel.setName(value);
        	    } else if (name.equals(Skater.EVENT_NUMBER)) {
        		skaterModel.setNumber(value);
        	    } else if (name.equals(Skater.EVENT_POSITION)) {
        		teamModel.getCurrentTeamJamModel().fieldSkater(skaterModel, FloorPosition.fromString(value).toPosition());
        	    } else if (name.equals(Skater.EVENT_PENALTY_BOX) && Boolean.parseBoolean(value) && !skaterModel.isInBox()) {
        		skaterModel.startBoxTrip(!scoreBoardModel.isInJam(), teamModel.isStarPass());
        	    } else if (name.equals(Skater.EVENT_PENALTY_BOX) && !Boolean.parseBoolean(value) && skaterModel.isInBox()) {
        		skaterModel.finishBoxTrip(!scoreBoardModel.isInJam(), teamModel.isStarPass());
        	    } else if (name.equals(Skater.EVENT_FLAGS)) {
        		skaterModel.setFlags(value);
        	    }
        	} catch ( Exception e ) {
        	}
            }
        }
    }

    public void processPenalty(SkaterModel skaterModel, Element penalty) {
        String id = penalty.getAttributeValue("Id");
        ScoreBoardModel scoreBoardModel = skaterModel.getTeamModel().getScoreBoardModel();
        PenaltyModel penaltyModel = scoreBoardModel.getPenaltyModel(id);

        if (editor.hasRemovePI(penalty) && penaltyModel != null) {
            scoreBoardModel.deletePenaltyModel(penaltyModel);
            return;
        }

        if (penaltyModel == null) {
            Element jamE = penalty.getChild(Penalty.EVENT_JAM);
            JamModel jam = (jamE == null ? null : scoreBoardModel.getJamModel(editor.getText(jamE)));
            Element codeE = penalty.getChild(Penalty.EVENT_CODE);
            String code = (codeE == null ? "" : editor.getText(codeE));
            Element expulsionE = penalty.getChild(Penalty.EVENT_EXPULSION);
            boolean expulsion = (expulsionE == null ? false : Boolean.parseBoolean(editor.getText(expulsionE)));
            penaltyModel = new DefaultPenaltyModel(id, skaterModel, jam, code, expulsion);
            skaterModel.addPenaltyModel(penaltyModel);
        }
        
        Iterator<?> children = penalty.getChildren().iterator();
        while (children.hasNext()) {
            Element element = (Element)children.next();
            try {
        	String name = element.getName();
        	String value = editor.getText(element);

        	if (name.equals("BoxTrip")) {
        	    penaltyModel.addBoxTrip(scoreBoardModel.getBoxTripModel(editor.getId(element)));
        	} else if (null == value) {
        	    continue;
        	} else if (name.equals(Penalty.EVENT_CODE)) {
        	    penaltyModel.setCode(value);
        	} else if (name.equals(Penalty.EVENT_JAM)) {
        	    penaltyModel.setJamModel(scoreBoardModel.getJamModel(value));
        	} else if (name.equals(Penalty.EVENT_EXPULSION)) {
        	    penaltyModel.setExpulsion(Boolean.parseBoolean(value));
        	} else if (name.equals(Penalty.EVENT_SERVED)) {
        	    penaltyModel.forceServed(Boolean.parseBoolean(value));
        	}
            } catch ( Exception e ) {
            }
        }
    }

    public void processPeriod(ScoreBoardModel scoreBoardModel, Element period) {
        String id = period.getAttributeValue("Id");
	PeriodModel periodModel = scoreBoardModel.getPeriodModel(id);

	if (editor.hasRemovePI(period) && periodModel != null) {
            scoreBoardModel.removePeriod(periodModel);
            return;
        }

        if (periodModel == null) {
            Element prevE = period.getChild(Period.EVENT_PREVIOUS);
            PeriodModel prev = (prevE == null ? null : scoreBoardModel.getPeriodModel(editor.getText(prevE)));
            Element nextE = period.getChild(Period.EVENT_NEXT);
            PeriodModel next = (nextE == null ? null : scoreBoardModel.getPeriodModel(editor.getText(nextE)));
            Element walltimeE = period.getChild(Period.EVENT_WALLTIME_END);
            long walltimeEnd = (walltimeE == null ? 0 : Long.parseLong(editor.getText(walltimeE)));
            periodModel = new DefaultPeriodModel(id, prev, next, walltimeEnd);
            scoreBoardModel.addPeriod(periodModel);
        }
	
        Iterator<?> children = period.getChildren().iterator();
        while (children.hasNext()) {
            Element element = (Element)children.next();
            try {
                String name = element.getName();
        	String value = editor.getText(element);

                if (name.equals("Timeout")) {
                    processTimeout(periodModel, element);
                }
                if (name.equals("Jam")) {
                    processJam(periodModel, element);
                } else if (null == value) {
                    continue;
                } else if (name.equals(Period.EVENT_CURRENT)) {
                    periodModel.setCurrent(Boolean.parseBoolean(value));
                } else if (name.equals(Period.EVENT_RUNNING)) {
                    periodModel.setRunning(Boolean.parseBoolean(value));
                }
            } catch ( Exception e ) {
            }
        }
    }

    public void processTimeout(PeriodModel periodModel, Element timeout) {
	String id = timeout.getAttributeValue("Id");
        ScoreBoardModel scoreBoardModel = periodModel.getScoreBoardModel();
        TimeoutModel timeoutModel = scoreBoardModel.getTimeoutModel(id);

        if (editor.hasRemovePI(timeout) && timeoutModel != null) {
            scoreBoardModel.deleteTimeoutModel(timeoutModel);
            return;
        }

        if (timeoutModel == null) {
            Element jamE = timeout.getChild(Timeout.EVENT_PRECEDING_JAM);
            JamModel jam = (jamE == null ? null : scoreBoardModel.getJamModel(editor.getText(jamE)));
            Element curE = timeout.getChild(Timeout.EVENT_CURRENT);
            boolean cur = (curE == null ? false : Boolean.parseBoolean(editor.getText(curE)));
            Element durE = timeout.getChild(Timeout.EVENT_DURATION);
            long dur = (durE == null ? 0 : Long.parseLong(editor.getText(durE)));
            Element pcStartE = timeout.getChild(Timeout.EVENT_PERIOD_CLOCK_START);
            long pcStart = (pcStartE == null ? 0 : Long.parseLong(editor.getText(pcStartE)));
            Element pcEndE = timeout.getChild(Timeout.EVENT_PERIOD_CLOCK_END);
            long pcEnd = (pcEndE == null ? 0 : Long.parseLong(editor.getText(pcEndE)));
            Element wallStartE = timeout.getChild(Timeout.EVENT_WALLTIME_START);
            long wallStart = (wallStartE == null ? 0 : Long.parseLong(editor.getText(wallStartE)));
            Element wallEndE = timeout.getChild(Timeout.EVENT_WALLTIME_END);
            long wallEnd = (wallEndE == null ? 0 : Long.parseLong(editor.getText(wallEndE)));
            timeoutModel = new DefaultTimeoutModel(id, jam, cur, dur, pcStart, pcEnd, wallStart, wallEnd);
            periodModel.addTimeout(timeoutModel);
        }
        
        Iterator<?> children = timeout.getChildren().iterator();
        while (children.hasNext()) {
            Element element = (Element)children.next();
            try {
                String name = element.getName();
                String value = editor.getText(element);

                if (null == value) {
                    continue;
                } else if (name.equals(Timeout.EVENT_CURRENT)) {
                    timeoutModel.setCurrent(Boolean.parseBoolean(value));
                } else if (name.equals(Timeout.EVENT_OWNER)) {
                    timeoutModel.setOwner(scoreBoardModel.getTimeoutOwner(value));
                } else if (name.equals(Timeout.EVENT_REVIEW)) {
                    timeoutModel.setOfficialReview(Boolean.parseBoolean(value));
                }
            } catch ( Exception e ) {
            }
        }
    }

    public void processJam(PeriodModel periodModel, Element jam) {
	String id = jam.getAttributeValue("Id");
        ScoreBoardModel scoreBoardModel = periodModel.getScoreBoardModel();
        JamModel jamModel = scoreBoardModel.getJamModel(id);

        if (editor.hasRemovePI(jam) && jamModel != null) {
            scoreBoardModel.deleteJamModel(jamModel);
            return;
        }

        if (jamModel == null) {
            Element prevE = jam.getChild(Jam.EVENT_PREVIOUS);
            JamModel prev = (prevE == null ? null : scoreBoardModel.getJamModel(editor.getText(prevE)));
            Element nextE = jam.getChild(Jam.EVENT_NEXT);
            JamModel next = (nextE == null ? null : scoreBoardModel.getJamModel(editor.getText(nextE)));
            Element durE = jam.getChild(Jam.EVENT_DURATION);
            long dur = (durE == null ? 0 : Long.parseLong(editor.getText(durE)));
            Element pcStartE = jam.getChild(Jam.EVENT_PERIOD_CLOCK_START);
            long pcStart = (pcStartE == null ? 0 : Long.parseLong(editor.getText(pcStartE)));
            Element pcEndE = jam.getChild(Jam.EVENT_PERIOD_CLOCK_END);
            long pcEnd = (pcEndE == null ? 0 : Long.parseLong(editor.getText(pcEndE)));
            Element wallStartE = jam.getChild(Jam.EVENT_WALLTIME_START);
            long wallStart = (wallStartE == null ? 0 : Long.parseLong(editor.getText(wallStartE)));
            Element wallEndE = jam.getChild(Jam.EVENT_WALLTIME_END);
            long wallEnd = (wallEndE == null ? 0 : Long.parseLong(editor.getText(wallEndE)));
            jamModel = new DefaultJamModel(id, scoreBoardModel, periodModel, prev, next, dur, pcStart, pcEnd, wallStart, wallEnd);
            periodModel.addJam(jamModel);
        }
        
        Iterator<?> children = jam.getChildren().iterator();
        while (children.hasNext()) {
            Element element = (Element)children.next();
            try {
                String eid = element.getAttributeValue("Id");
                String name = element.getName();
                String value = editor.getText(element);

                if (name.equals("TeamJam")) {
                    processTeamJam(jamModel, element);
                } else if (name.equals("Penalty")) {
                    jamModel.addPenaltyModel(scoreBoardModel.getPenaltyModel(eid));
                } else if (name.equals("Timeout")) {
                    jamModel.addTimeoutAfter(scoreBoardModel.getTimeoutModel(eid));
                } else if (null == value) {
                    continue;
                } else if (name.equals(Jam.EVENT_CURRENT)) {
                    jamModel.setCurrent(Boolean.parseBoolean(value));
                } else if (name.equals(Jam.EVENT_INJURY)) {
                    jamModel.setInjury(Boolean.parseBoolean(value));
                }
            } catch ( Exception e ) {
            }
        }
    }

    public void processTeamJam(JamModel jamModel, Element teamJam) {
	String id = teamJam.getAttributeValue("Id");
        ScoreBoardModel scoreBoardModel = jamModel.getScoreBoardModel();
        TeamJamModel teamJamModel = scoreBoardModel.getTeamJamModel(id);

        if (editor.hasRemovePI(teamJam) && teamJamModel != null) {
            scoreBoardModel.deleteTeamJamModel(teamJamModel);
            return;
        }
        
        Iterator<?> children = teamJam.getChildren().iterator();
        while (children.hasNext()) {
            Element element = (Element)children.next();
            try {
                String name = element.getName();
                String value = editor.getText(element);

                if (name.equals("ScoringTrip")) {
                    processScoringTrip(teamJamModel, element);
                } else if (name.equals("Fielding")) {
                    processFielding(teamJamModel, element);
                } else if (null == value) {
                    continue;
                } else if (name.equals(TeamJam.EVENT_OS_OFFSET)) {
                    teamJamModel.setOsOffset(Integer.parseInt(value));
                } else if (name.equals(TeamJam.EVENT_OS_OFFSET_REASON)) {
                    teamJamModel.setOsOffsetReason(value);
                } else if (name.equals(TeamJam.EVENT_LOST)) {
                    teamJamModel.setLost(Boolean.parseBoolean(value));
                } else if (name.equals(TeamJam.EVENT_LEAD)) {
                    teamJamModel.setLead(Boolean.parseBoolean(value));
                } else if (name.equals(TeamJam.EVENT_CALLOFF)) {
                    teamJamModel.setCalloff(Boolean.parseBoolean(value));
                } else if (name.equals(TeamJam.EVENT_STAR_PASS_TRIP)) {
                    teamJamModel.setStarPassTrip(scoreBoardModel.getScoringTripModel(value));
                } else if (name.equals(TeamJam.EVENT_NO_PIVOT)) {
                    teamJamModel.setNoPivot(Boolean.parseBoolean(value));
                }
            } catch ( Exception e ) {
            }
        }
    }

    public void processScoringTrip(TeamJamModel teamJamModel, Element scoringTrip) {
        String id = scoringTrip.getAttributeValue("Id");
        ScoreBoardModel scoreBoardModel = teamJamModel.getTeamModel().getScoreBoardModel();
        ScoringTripModel scoringTripModel = scoreBoardModel.getScoringTripModel(id);

        if (editor.hasRemovePI(scoringTrip) && scoringTripModel != null) {
            scoreBoardModel.deleteScoringTripModel(scoringTripModel);
            return;
        }

        if (scoringTripModel == null) {
            Element prevE = scoringTrip.getChild(ScoringTrip.EVENT_PREVIOUS);
            ScoringTripModel prev = (prevE == null ? null : scoreBoardModel.getScoringTripModel(editor.getText(prevE)));
            Element nextE = scoringTrip.getChild(ScoringTrip.EVENT_NEXT);
            ScoringTripModel next = (nextE == null ? null : scoreBoardModel.getScoringTripModel(editor.getText(nextE)));
            Element afterSPE = scoringTrip.getChild(ScoringTrip.EVENT_AFTER_SP);
            boolean afterSP = (afterSPE == null ? false : Boolean.parseBoolean(editor.getText(afterSPE)));
            scoringTripModel = new DefaultScoringTripModel(id, teamJamModel, prev, next, afterSP);
            teamJamModel.addScoringTripModel(scoringTripModel);
        }
        
        Iterator<?> children = scoringTrip.getChildren().iterator();
        while (children.hasNext()) {
            Element element = (Element)children.next();
            try {
        	String name = element.getName();
        	String value = editor.getText(element);

        	if (null == value) {
        	    continue;
                } else if (name.equals(ScoringTrip.EVENT_AFTER_SP)) {
                    scoringTripModel.setAfterSP(Boolean.parseBoolean(value));
        	} else if (name.equals(ScoringTrip.EVENT_POINTS)) {
        	    scoringTripModel.setPoints(Integer.parseInt(value));
        	}
            } catch ( Exception e ) {
            }
        }
    }

    public void processFielding(TeamJamModel teamJamModel, Element fielding) {
        String id = fielding.getAttributeValue("Id");
        ScoreBoardModel scoreBoardModel = teamJamModel.getTeamModel().getScoreBoardModel();
        FieldingModel fieldingModel = scoreBoardModel.getFieldingModel(id);

        if (editor.hasRemovePI(fielding) && fieldingModel != null) {
            scoreBoardModel.deleteFieldingModel(fieldingModel);
            return;
        }

        if (fieldingModel == null) {
            Element skaterE = fielding.getChild(Fielding.EVENT_SKATER);
            SkaterModel skater = (skaterE == null ? null : scoreBoardModel.getSkaterModel(editor.getText(skaterE)));
            Element posE = fielding.getChild(Penalty.EVENT_JAM);
            FloorPosition pos = (posE == null ? null : FloorPosition.fromString(editor.getText(posE)));
            fieldingModel = new DefaultFieldingModel(id, teamJamModel, skater, pos);
            teamJamModel.addFieldingModel(fieldingModel);
        }
        
        Iterator<?> children = fielding.getChildren().iterator();
        while (children.hasNext()) {
            Element element = (Element)children.next();
            try {
                String name = element.getName();
                String value = editor.getText(element);

                if (name.equals("BoxTrip")) {
                    processBoxTrip(fieldingModel, element);
                } else if (null == value) {
                    continue;
                } else if (name.equals(Fielding.EVENT_SKATER)) {
                    fieldingModel.setSkaterModel(scoreBoardModel.getSkaterModel(value));
                } else if (name.equals(Fielding.EVENT_POSITION)) {
                    fieldingModel.setFloorPosition(FloorPosition.fromString(value));
                } else if (name.equals(Fielding.EVENT_3_JAMS)) {
                    fieldingModel.setSkaterModel(scoreBoardModel.getSkaterModel(value));
                }
            } catch ( Exception e ) {
            }
        }
    }

    public void processBoxTrip(FieldingModel fieldingModel, Element boxTrip) {
        String id = boxTrip.getAttributeValue("Id");
        ScoreBoardModel scoreBoardModel = fieldingModel.getScoreBoardModel();
        BoxTripModel boxTripModel = scoreBoardModel.getBoxTripModel(id);

        if (editor.hasRemovePI(boxTrip) && boxTripModel != null) {
            scoreBoardModel.deleteBoxTripModel(boxTripModel);
            return;
        }

        if (boxTripModel == null) {
            Element betweenE = boxTrip.getChild(BoxTrip.EVENT_START_BETWEEN);
            boolean betweenJams = (betweenE == null ? null : Boolean.parseBoolean(editor.getText(betweenE)));
            Element afterSpE = boxTrip.getChild(BoxTrip.EVENT_START_AFTER_SP);
            boolean afterStarPass = (afterSpE == null ? null : Boolean.parseBoolean(editor.getText(afterSpE)));
            Element currentE = boxTrip.getChild(BoxTrip.EVENT_CURRENT);
            boolean isCurrent = (currentE == null ? null : Boolean.parseBoolean(editor.getText(currentE)));
            boxTripModel = new DefaultBoxTripModel(id, fieldingModel, betweenJams, afterStarPass, isCurrent);
            fieldingModel.addBoxTripModel(boxTripModel);
        }
        
        Iterator<?> children = boxTrip.getChildren().iterator();
        while (children.hasNext()) {
            Element element = (Element)children.next();
            try {
        	String name = element.getName();
        	String value = editor.getText(element);

        	if (null == value) {
        	    continue;
        	} else if (name.equals(BoxTrip.EVENT_START_BETWEEN)) {
        	    boxTripModel.setStartBetweenJams(Boolean.parseBoolean(value));
        	} else if (name.equals(BoxTrip.EVENT_END_BETWEEN)) {
        	    boxTripModel.setEndBetweenJams(Boolean.parseBoolean(value));
        	} else if (name.equals(BoxTrip.EVENT_START_AFTER_SP)) {
        	    boxTripModel.setStartAfterStarPass(Boolean.parseBoolean(value));
        	} else if (name.equals(BoxTrip.EVENT_END_AFTER_SP)) {
        	    boxTripModel.setEndAfterStarPass(Boolean.parseBoolean(value));
        	}
            } catch ( Exception e ) {
            }
        }
    }

    public static ScoreBoardXmlConverter getInstance() { return scoreBoardXmlConverter; }

    protected XmlDocumentEditor editor = new XmlDocumentEditor();
    protected XMLOutputter rawXmlOutputter = XmlDocumentEditor.getRawXmlOutputter();

    private static ScoreBoardXmlConverter scoreBoardXmlConverter = new ScoreBoardXmlConverter();
}
