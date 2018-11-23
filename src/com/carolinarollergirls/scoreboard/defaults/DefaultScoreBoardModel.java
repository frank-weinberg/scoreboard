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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.carolinarollergirls.scoreboard.Ruleset;
import com.carolinarollergirls.scoreboard.event.ConditionalScoreBoardListener;
import com.carolinarollergirls.scoreboard.event.DefaultScoreBoardEventProvider;
import com.carolinarollergirls.scoreboard.model.FrontendSettingsModel;
import com.carolinarollergirls.scoreboard.model.JamModel;
import com.carolinarollergirls.scoreboard.model.PenaltyModel;
import com.carolinarollergirls.scoreboard.model.PeriodModel;
import com.carolinarollergirls.scoreboard.model.PeriodModel.PeriodSnapshotModel;
import com.carolinarollergirls.scoreboard.event.ScoreBoardEvent;
import com.carolinarollergirls.scoreboard.event.ScoreBoardListener;
import com.carolinarollergirls.scoreboard.model.BoxTripModel;
import com.carolinarollergirls.scoreboard.model.ClockModel;
import com.carolinarollergirls.scoreboard.model.FieldingModel;
import com.carolinarollergirls.scoreboard.model.ScoreBoardModel;
import com.carolinarollergirls.scoreboard.model.ScoringTripModel;
import com.carolinarollergirls.scoreboard.model.SettingsModel;
import com.carolinarollergirls.scoreboard.model.SkaterModel;
import com.carolinarollergirls.scoreboard.model.TeamJamModel;
import com.carolinarollergirls.scoreboard.model.TeamModel;
import com.carolinarollergirls.scoreboard.model.TimeoutModel;
import com.carolinarollergirls.scoreboard.penalties.PenaltyCodesManager;
import com.carolinarollergirls.scoreboard.utils.ClockConversion;
import com.carolinarollergirls.scoreboard.utils.Comparators;
import com.carolinarollergirls.scoreboard.utils.ScoreBoardClock;
import com.carolinarollergirls.scoreboard.view.BoxTrip;
import com.carolinarollergirls.scoreboard.view.Clock;
import com.carolinarollergirls.scoreboard.view.Fielding;
import com.carolinarollergirls.scoreboard.view.FrontendSettings;
import com.carolinarollergirls.scoreboard.view.Jam;
import com.carolinarollergirls.scoreboard.view.Penalty;
import com.carolinarollergirls.scoreboard.view.Period;
import com.carolinarollergirls.scoreboard.view.ScoreBoard;
import com.carolinarollergirls.scoreboard.view.ScoringTrip;
import com.carolinarollergirls.scoreboard.view.Settings;
import com.carolinarollergirls.scoreboard.view.Skater;
import com.carolinarollergirls.scoreboard.view.Team;
import com.carolinarollergirls.scoreboard.view.TeamJam;
import com.carolinarollergirls.scoreboard.view.Timeout;
import com.carolinarollergirls.scoreboard.view.Timeout.TimeoutOwner;
import com.carolinarollergirls.scoreboard.xml.XmlScoreBoard;

public class DefaultScoreBoardModel extends DefaultScoreBoardEventProvider implements ScoreBoardModel {
    public DefaultScoreBoardModel() {
        setupScoreBoard();
    }

    protected void setupScoreBoard() {
        settings = new DefaultSettingsModel(this);
        settings.addScoreBoardListener(this);
        settings.addRuleMapping(SETTING_PERIOD_DURATION, new String[] {"Clock." + Clock.ID_PERIOD + ".MaximumTime"});
        settings.addRuleMapping(SETTING_JAM_DURATION, new String[] {"Clock." + Clock.ID_JAM + ".MaximumTime"});

        Ruleset.registerRule(settings, SETTING_NUMBER_PERIODS);
        Ruleset.registerRule(settings, SETTING_PERIOD_DURATION);
        Ruleset.registerRule(settings, SETTING_PERIOD_DIRECTION);
        Ruleset.registerRule(settings, SETTING_PERIOD_END_BETWEEN_JAMS);
        Ruleset.registerRule(settings, SETTING_JAM_NUMBER_PER_PERIOD);
        Ruleset.registerRule(settings, SETTING_JAM_DURATION);
        Ruleset.registerRule(settings, SETTING_JAM_DIRECTION);
        Ruleset.registerRule(settings, SETTING_LINEUP_DURATION);
        Ruleset.registerRule(settings, SETTING_OVERTIME_LINEUP_DURATION);
        Ruleset.registerRule(settings, SETTING_LINEUP_DIRECTION);
        Ruleset.registerRule(settings, SETTING_TTO_DURATION);
        Ruleset.registerRule(settings, SETTING_TIMEOUT_DIRECTION);
        Ruleset.registerRule(settings, SETTING_STOP_PC_ON_TO);
        Ruleset.registerRule(settings, SETTING_STOP_PC_ON_OTO);
        Ruleset.registerRule(settings, SETTING_STOP_PC_ON_TTO);
        Ruleset.registerRule(settings, SETTING_STOP_PC_ON_OR);
        Ruleset.registerRule(settings, SETTING_STOP_PC_AFTER_TO_DURATION);
        Ruleset.registerRule(settings, SETTING_INTERMISSION_DURATIONS);
        Ruleset.registerRule(settings, SETTING_INTERMISSION_DIRECTION);
        Ruleset.registerRule(settings, SETTING_AUTO_START);
        Ruleset.registerRule(settings, SETTING_AUTO_START_BUFFER);
        Ruleset.registerRule(settings, SETTING_AUTO_START_JAM);
        Ruleset.registerRule(settings, SETTING_AUTO_END_JAM);
        Ruleset.registerRule(settings, SETTING_AUTO_END_TTO);
        Ruleset.registerRule(settings, Team.SETTING_NUMBER_TIMEOUTS);
        Ruleset.registerRule(settings, Team.SETTING_TIMEOUTS_PER_PERIOD);
        Ruleset.registerRule(settings, Team.SETTING_NUMBER_REVIEWS);
        Ruleset.registerRule(settings, Team.SETTING_REVIEWS_PER_PERIOD);
        Ruleset.registerRule(settings, PenaltyCodesManager.SETTING_PENALTIES_FILE);
        Ruleset.registerRule(settings, Penalty.SETTING_FO_LIMIT);

        frontendSettings = new DefaultFrontendSettingsModel(this);
        frontendSettings.addScoreBoardListener(this);
        timeoutOwners.put(TIMEOUT_OWNER_NONE.getId(), TIMEOUT_OWNER_NONE);
        timeoutOwners.put(TIMEOUT_OWNER_OTO.getId(), TIMEOUT_OWNER_OTO);
        createTeamModel(Team.ID_1);
        createTeamModel(Team.ID_2);
        createClockModel(Clock.ID_PERIOD);
        createClockModel(Clock.ID_JAM);
        createClockModel(Clock.ID_LINEUP);
        createClockModel(Clock.ID_TIMEOUT);
        createClockModel(Clock.ID_INTERMISSION);
        addPeriod(new DefaultPeriodModel(this));
        reset();
        addInPeriodListeners();
        xmlScoreBoard = new XmlScoreBoard(this);
        for (TeamModel t : getTeamModels()) {
            //restore current/next/previous TeamJam as they are not autosaved
            t.updateTeamJamModels();
        }
        //Button may have a label from autosave but undo will not work after restart
        setLabel(BUTTON_UNDO, ACTION_NONE);
    }

    public String getProviderName() { return "ScoreBoard"; }
    public Class<ScoreBoard> getProviderClass() { return ScoreBoard.class; }
    public String getProviderId() { return ""; }

    public XmlScoreBoard getXmlScoreBoard() { return xmlScoreBoard; }

    public static Object getCoreLock() { return coreLock; }

    public ScoreBoard getScoreBoard() { return this; }

    public void reset() {
        synchronized (coreLock) {
            requestBatchStart();
            _getRuleset().apply(true);

            settings.reset();
            // Custom settings are not reset, as broadcast overlays settings etc.
            // shouldn't be lost just because the next game is starting.

            for (SkaterModel s : new ArrayList<SkaterModel>(skaters.values())) {
        	deleteSkaterModel(s);
            }
            for (PeriodModel p : new ArrayList<PeriodModel>(periods.values())) {
        	removePeriod(p);
            }
            for (TimeoutModel t : new ArrayList<TimeoutModel>(timeouts.values())) {
        	deleteTimeoutModel(t);
            }
            for (JamModel j : new ArrayList<JamModel>(jams.values())) {
        	deleteJamModel(j);
            }
            
            currentPeriod = lastPeriod;
            currentPeriod.setCurrent(true);
            currentPeriod.addJam();

            Iterator<ClockModel> c = getClockModels().iterator();
            while (c.hasNext()) {
                c.next().reset();
            }
            Iterator<TeamModel> t = getTeamModels().iterator();
            while (t.hasNext()) {
                t.next().reset();
            }

            setInOvertime(false);
            setOfficialScore(false);
            walltimeStart = 0;
            walltimeEnd = 0;
            restartPcAfterTimeout = false;
            snapshot = null;
            replacePending = false;

            setLabel(BUTTON_START, ACTION_START_JAM);
            setLabel(BUTTON_STOP, ACTION_LINEUP);
            setLabel(BUTTON_TIMEOUT, ACTION_TIMEOUT);
            setLabel(BUTTON_UNDO, ACTION_NONE);
            requestBatchEnd();
        }
    }

    public boolean isInPeriod() { 
	return getCurrentPeriod().isRunning();
    }
    protected void addInPeriodListeners() {
        addScoreBoardListener(new ConditionalScoreBoardListener(Clock.class, Clock.ID_PERIOD, "Running", Boolean.FALSE, periodEndListener));
        addScoreBoardListener(new ConditionalScoreBoardListener(Clock.class, Clock.ID_JAM, "Running", Boolean.FALSE, jamEndListener));
        addScoreBoardListener(new ConditionalScoreBoardListener(Clock.class, Clock.ID_INTERMISSION, "Running", Boolean.FALSE, intermissionEndListener));
        addScoreBoardListener(new ConditionalScoreBoardListener(Clock.class, Clock.ID_LINEUP, "Time", lineupClockListener));
        addScoreBoardListener(new ConditionalScoreBoardListener(Clock.class, Clock.ID_TIMEOUT, "Time", timeoutClockListener));
    }

    public boolean isInOvertime() { return inOvertime; }
    public void setInOvertime(boolean o) {
        synchronized (coreLock) {
            if (o == inOvertime) { return; }
            Boolean last = new Boolean(inOvertime);
            inOvertime = o;
            scoreBoardChange(new ScoreBoardEvent(this, EVENT_IN_OVERTIME, new Boolean(inOvertime), last));
            ClockModel lc = getClockModel(Clock.ID_LINEUP);
            if (!o && lc.isCountDirectionDown()) {
                lc.setMaximumTime(settings.getLong(SETTING_LINEUP_DURATION));
            }
        }
    }
    public void startOvertime() {
        synchronized (coreLock) {
            ClockModel pc = getClockModel(Clock.ID_PERIOD);
            ClockModel jc = getClockModel(Clock.ID_JAM);
            ClockModel lc = getClockModel(Clock.ID_LINEUP);

            if (pc.isRunning() || jc.isRunning()) {
                return;
            }
            if (currentPeriod.getNumber() < getTotalNumberPeriods()) {
                return;
            }
            if (!pc.isTimeAtEnd()) {
                return;
            }
            createSnapshot(ACTION_OVERTIME);

            requestBatchStart();
            _endTimeout(false);
            setInOvertime(true);
            setLabels(ACTION_START_JAM, ACTION_NONE, ACTION_TIMEOUT);
            long otLineupTime = settings.getLong(SETTING_OVERTIME_LINEUP_DURATION);
            if (lc.getMaximumTime() < otLineupTime) {
                lc.setMaximumTime(otLineupTime);
            }
            _startLineup();
            requestBatchEnd();
        }
    }

    public boolean isOfficialScore() { return officialScore; }
    public void setOfficialScore(boolean o) {
        synchronized (coreLock) {
            Boolean last = new Boolean(officialScore);
            officialScore = o;
            if (officialScore) {
        	walltimeEnd = ScoreBoardClock.getInstance().getCurrentWalltime();
                scoreBoardChange(new ScoreBoardEvent(this, EVENT_GAME_WALLTIME_END, getGameWalltimeEnd(), 0));
                scoreBoardChange(new ScoreBoardEvent(this, EVENT_GAME_DURATION, getGameDuration(), 0));
            }
            scoreBoardChange(new ScoreBoardEvent(this, EVENT_OFFICIAL_SCORE, new Boolean(officialScore), last));
        }
    }
    
    public long getGameDuration() { return walltimeEnd - walltimeStart; }
    public long getGameWalltimeStart() { return walltimeStart; }
    public long getGameWalltimeEnd() { return walltimeEnd; }
    
    public boolean isInJam() {
	if (getCurrentJam() != null) {
	    return getCurrentJam().isRunning();
	} else {
	    return false;
	}
    }
    public boolean isInTimeout() { 
	if (getCurrentTimeout() != null) {
	    return getCurrentTimeout().isRunning();
	} else {
	    return false;
	}
    }

    public void startJam() {
        synchronized (coreLock) {
            if (!getClock(Clock.ID_JAM).isRunning()) {
                createSnapshot(ACTION_START_JAM);
                setLabels(ACTION_NONE, ACTION_STOP_JAM, ACTION_TIMEOUT);
                _startJam();
                finishReplace();
            }
        }
    }
    public void stopJamTO() {
        synchronized (coreLock) {
            ClockModel jc = getClockModel(Clock.ID_JAM);
            ClockModel lc = getClockModel(Clock.ID_LINEUP);
            ClockModel tc = getClockModel(Clock.ID_TIMEOUT);

            if (jc.isRunning()) {
                createSnapshot(ACTION_STOP_JAM);
                setLabels(ACTION_START_JAM, ACTION_NONE, ACTION_TIMEOUT);
                _endJam(false);
                finishReplace();
            } else if (tc.isRunning()) {
                createSnapshot(ACTION_STOP_TO);
                setLabels(ACTION_START_JAM, ACTION_NONE, ACTION_TIMEOUT);
                _endTimeout(false);
                finishReplace();
            } else if (!lc.isRunning()) {
                createSnapshot(ACTION_LINEUP);
                setLabels(ACTION_START_JAM, ACTION_NONE, ACTION_TIMEOUT);
                _startLineup();
                finishReplace();
            }
        }
    }
    public void timeout() {
        synchronized (coreLock) {
            if (getClock(Clock.ID_TIMEOUT).isRunning()) {
                createSnapshot(ACTION_RE_TIMEOUT);
            } else {
                createSnapshot(ACTION_TIMEOUT);
            }
            setLabels(ACTION_START_JAM, ACTION_STOP_TO, ACTION_RE_TIMEOUT);
            _startTimeout();
            finishReplace();
        }
    }
    public void setTimeoutType(TimeoutOwner owner, boolean review) {
	synchronized (coreLock) {
	    ClockModel tc = getClockModel(Clock.ID_TIMEOUT);
	    ClockModel pc = getClockModel(Clock.ID_PERIOD);

	    requestBatchStart();
	    if (!tc.isRunning()) {
		timeout();
	    }
	    if (getTimeoutOwner() instanceof Team) {
		scoreBoardChange(new ScoreBoardEvent((Team)getTimeoutOwner(),
			isOfficialReview() ? Team.EVENT_IN_OFFICIAL_REVIEW : Team.EVENT_IN_TIMEOUT,
			false, true));
	    }
	    getCurrentTimeoutModel().setOwner(owner);
	    getCurrentTimeoutModel().setOfficialReview(review);
	    if (getTimeoutOwner() instanceof Team) {
		scoreBoardChange(new ScoreBoardEvent((Team)getTimeoutOwner(),
			isOfficialReview() ? Team.EVENT_IN_OFFICIAL_REVIEW : Team.EVENT_IN_TIMEOUT,
			true, false));
	    }

	    if (!settings.getBoolean(SETTING_STOP_PC_ON_TO)) {
		//Some timeouts don't stop the period clock
		boolean stopPc = false;
		if (!owner.equals(TIMEOUT_OWNER_NONE)) {
		    if (owner.equals(TIMEOUT_OWNER_OTO) ) {
			if (settings.getBoolean(SETTING_STOP_PC_ON_OTO)) {
			    stopPc = true;
			}
		    } else {
			if (review && settings.getBoolean(SETTING_STOP_PC_ON_OR)) {
			    stopPc = true;
			}
			if (!review && settings.getBoolean(SETTING_STOP_PC_ON_TTO)) {
			    stopPc = true;
			}
		    }
		}
		if (stopPc && pc.isRunning()) {
		    pc.stop();
		    pc.elapseTime(-tc.getTimeElapsed());
		}
		if (!stopPc && !pc.isRunning()) {
		    pc.elapseTime(tc.getTimeElapsed());
		    pc.start();
		}
	    }
	    requestBatchEnd();
	}
    }
    private void _preparePeriod() {
        ClockModel pc = getClockModel(Clock.ID_PERIOD);
        ClockModel jc = getClockModel(Clock.ID_JAM);
        ClockModel ic = getClockModel(Clock.ID_INTERMISSION);

        requestBatchStart();
        if (currentPeriod == lastPeriod) {
            addPeriod();
        }
        currentPeriod.setCurrent(false);
        currentPeriod = currentPeriod.getNext();
        currentPeriod.setCurrent(true);
        pc.resetTime();
        scoreBoardChange(new ScoreBoardEvent(pc, Clock.EVENT_NUMBER, pc.getNumber(), pc.getNumber()-1));
        scoreBoardChange(new ScoreBoardEvent(ic, Clock.EVENT_NUMBER, ic.getNumber(), ic.getNumber()-1));
        restartPcAfterTimeout = false;
        jc.resetTime();
        requestBatchEnd();
    }
    private void _possiblyEndPeriod() {
        ClockModel pc = getClockModel(Clock.ID_PERIOD);
        ClockModel jc = getClockModel(Clock.ID_JAM);
        ClockModel tc = getClockModel(Clock.ID_TIMEOUT);

        if (pc.isTimeAtEnd() && !pc.isRunning() && !jc.isRunning() && !tc.isRunning()) {
            requestBatchStart();
            setLabels(ACTION_START_JAM, ACTION_LINEUP, ACTION_TIMEOUT);
            getCurrentPeriodModel().setRunning(false);
            setOfficialScore(false);
            _endLineup();
            _startIntermission();
            requestBatchEnd();
        }
    }
    private void _startJam() {
        ClockModel pc = getClockModel(Clock.ID_PERIOD);
        ClockModel jc = getClockModel(Clock.ID_JAM);

        requestBatchStart();
        _endIntermission(false);
        _endTimeout(false);
        _endLineup();
        getCurrentPeriodModel().setRunning(true);
        pc.start();
        jc.startNext();
        getCurrentPeriodModel().startNextJam();
        getTeamModel(Team.ID_1).startJam();
        getTeamModel(Team.ID_2).startJam();
        requestBatchEnd();
    }
    private void _endJam(boolean force) {
        ClockModel pc = getClockModel(Clock.ID_PERIOD);
        ClockModel jc = getClockModel(Clock.ID_JAM);

        if (!jc.isRunning() && !force) { return; }

        requestBatchStart();
        jc.stop();
        getCurrentPeriodModel().stopCurrentJam();
        getTeamModel(Team.ID_1).stopJam();
        getTeamModel(Team.ID_2).stopJam();
        setInOvertime(false);

        //TODO: Make this value configurable in the ruleset.
        if (pc.getTimeRemaining() < 30000) {
            restartPcAfterTimeout = true;
        }
        if (pc.isRunning()) {
            _startLineup();
        } else {
            _possiblyEndPeriod();
        }
        requestBatchEnd();
    }
    private void _startLineup() {
        ClockModel lc = getClockModel(Clock.ID_LINEUP);

        requestBatchStart();
        _endIntermission(false);
        getCurrentPeriodModel().setRunning(true);
        lc.startNext();
        requestBatchEnd();
    }
    private void _endLineup() {
        ClockModel lc = getClockModel(Clock.ID_LINEUP);

        requestBatchStart();
        lc.stop();
        requestBatchEnd();
    }
    private void _startTimeout() {
        ClockModel pc = getClockModel(Clock.ID_PERIOD);
        ClockModel tc = getClockModel(Clock.ID_TIMEOUT);

        requestBatchStart();
        if (tc.isRunning()) {
            //end the previous timeout before starting a new one
            _endTimeout(true);
        }

        if (settings.getBoolean(SETTING_STOP_PC_ON_TO)) {
            pc.stop();
        }
        _endLineup();
        _endJam(false);
        _endIntermission(false);
        getCurrentPeriodModel().setRunning(true);
        getCurrentPeriodModel().addTimeout();
        tc.startNext();
        requestBatchEnd();
    }
    private void _endTimeout(boolean timeoutFollows) {
        ClockModel tc = getClockModel(Clock.ID_TIMEOUT);
        ClockModel pc = getClockModel(Clock.ID_PERIOD);

        if (!tc.isRunning()) { return; }

        requestBatchStart();
        if (!frontendSettings.get(FRONTEND_SETTING_CLOCK_AFTER_TIMEOUT).equals(Clock.ID_TIMEOUT)) {
            tc.stop();
        }
        if (getTimeoutOwner() instanceof Team) {
            restartPcAfterTimeout = false;
        }
        getCurrentTimeoutModel().stop();
        if (!timeoutFollows) {
            if (pc.isTimeAtEnd()) {
                _possiblyEndPeriod();
            } else {
                if (restartPcAfterTimeout) {
                    pc.start();
                }
                if (frontendSettings.get(FRONTEND_SETTING_CLOCK_AFTER_TIMEOUT).equals(Clock.ID_LINEUP)) {
                    _startLineup();
                }
            }
        }
        requestBatchEnd();
    }
    private void _startIntermission() {
        ClockModel ic = getClockModel(Clock.ID_INTERMISSION);

        requestBatchStart();
        long duration = 0;
        String[] sequence = settings.get(SETTING_INTERMISSION_DURATIONS).split(",");
        int number = sequence.length;
        if (currentPeriod == null) {
            number = 0;
        } else if (currentPeriod.getNumber() < number) {
            number = currentPeriod.getNumber();
        }
        if (number > 0) {
            duration = ClockConversion.fromHumanReadable(sequence[number-1]);
        }
        ic.setMaximumTime(duration);
        ic.resetTime();
        ic.start();
        requestBatchEnd();
    }
    private void _endIntermission(boolean force) {
        ClockModel ic = getClockModel(Clock.ID_INTERMISSION);

        if (!ic.isRunning() && !force && currentPeriod.getNumber() > 0) { return; }

        requestBatchStart();
        ic.stop();
        if (currentPeriod.getNumber() == 0 ||
        	(ic.getTimeRemaining() < 60000 
        	&& currentPeriod.getNumber() < getTotalNumberPeriods())) {
            //Before game start always start the first period.
            //Between periods, if less than one minute of intermission is left
            // and there is another period, go to the next period. 
            // Otherwise extend the previous period.
            _preparePeriod();
        }
        requestBatchEnd();
    }
    private void _possiblyAutostart() {
        ClockModel pc = getClockModel(Clock.ID_PERIOD);
        ClockModel jc = getClockModel(Clock.ID_JAM);
        ClockModel lc = getClockModel(Clock.ID_LINEUP);
        ClockModel tc = getClockModel(Clock.ID_TIMEOUT);

        long bufferTime = settings.getLong(SETTING_AUTO_START_BUFFER);
        long triggerTime = bufferTime + (isInOvertime() ?
                                         settings.getLong(SETTING_OVERTIME_LINEUP_DURATION) :
                                         settings.getLong(SETTING_LINEUP_DURATION));

        requestBatchStart();
        if (lc.getTimeElapsed() >= triggerTime) {
            if (Boolean.parseBoolean(settings.get(SETTING_AUTO_START_JAM))) {
                startJam();
                jc.elapseTime(bufferTime);
            } else {
                timeout();
                pc.elapseTime(-bufferTime);
                tc.elapseTime(bufferTime);
            }
        }
        requestBatchEnd();
    }


    protected void createSnapshot(String type) {
        snapshot = new ScoreBoardSnapshot(this, type);
        setLabel(BUTTON_UNDO, UNDO_PREFIX + type);
    }
    protected void restoreSnapshot() {
        ScoreBoardClock.getInstance().rewindTo(snapshot.getSnapshotTime());

        requestBatchStart();
        int oldJamNr = getCurrentJam().getNumber();
        int oldPeriodNr = getCurrentPeriod().getNumber();
        for (ClockModel clock : getClockModels()) {
            clock.restoreSnapshot(snapshot.getClockSnapshot(clock.getId()));
        }
        if (currentPeriod != snapshot.getCurrentPeriod()) {
            currentPeriod.setCurrent(false);
            currentPeriod = snapshot.getCurrentPeriod();
            currentPeriod.setCurrent(true);
        }
        getCurrentPeriodModel().restoreSnapshot(snapshot.getPeriodSnapshot());
        setInOvertime(snapshot.inOvertime());
        restartPcAfterTimeout = snapshot.restartPcAfterTo();
        setLabels(snapshot.getStartLabel(), snapshot.getStopLabel(), snapshot.getTimeoutLabel());
        setLabel(BUTTON_UNDO, ACTION_NONE);
        setLabel(BUTTON_REPLACED, snapshot.getType());
        if (oldPeriodNr != getCurrentPeriod().getNumber()) {
            scoreBoardChange(new ScoreBoardEvent(getClock(Clock.ID_PERIOD), 
        	    Clock.EVENT_NUMBER, getCurrentPeriod().getNumber(), oldPeriodNr));
            scoreBoardChange(new ScoreBoardEvent(getClock(Clock.ID_INTERMISSION), 
        	    Clock.EVENT_NUMBER, getCurrentPeriod().getNumber(), oldPeriodNr));
        }
        if (oldJamNr != getCurrentJam().getNumber()) {
            scoreBoardChange(new ScoreBoardEvent(getClock(Clock.ID_JAM), 
        	    Clock.EVENT_NUMBER, getCurrentJam().getNumber(), oldJamNr));
        }
        requestBatchEnd();
        snapshot = null;
    }
    protected void finishReplace() {
        if (!replacePending) { return; }
        requestBatchStart();
        ScoreBoardClock.getInstance().start(true);
        replacePending = false;
        requestBatchEnd();
    }
    public void clockUndo(boolean replace) {
        synchronized (coreLock) {
            requestBatchStart();
            if (replacePending) {
                createSnapshot(ACTION_NO_REPLACE);
                finishReplace();
            } else if (snapshot != null) {
                ScoreBoardClock.getInstance().stop();
                restoreSnapshot();
                if (replace) {
                    replacePending = true;
                    setLabel(BUTTON_UNDO, ACTION_NO_REPLACE);
                } else {
                    ScoreBoardClock.getInstance().start(true);
                }
            }
            requestBatchEnd();
        }
    }

    protected void setLabel(String id, String value) {
        settings.set(id, value);
    }
    protected void setLabels(String startLabel, String stopLabel, String timeoutLabel) {
        setLabel(BUTTON_START, startLabel);
        setLabel(BUTTON_STOP, stopLabel);
        setLabel(BUTTON_TIMEOUT, timeoutLabel);
    }

    public Ruleset _getRuleset() {
        synchronized (coreLock) {
            if (ruleset == null) {
                ruleset = Ruleset.findRuleset(null, true);
            }
            return ruleset;
        }
    }
    public String getRuleset() { return _getRuleset().getId().toString(); }
    public void setRuleset(String id) {
        synchronized (coreLock) {
            String last = getRuleset();
            ruleset = Ruleset.findRuleset(id, true);
            ruleset.apply(false);
            scoreBoardChange(new ScoreBoardEvent(this, EVENT_RULESET, ruleset.getId().toString(), last));
        }
    }

    public Settings getSettings() { return settings; }
    public SettingsModel getSettingsModel() { return settings; }

    public FrontendSettings getFrontendSettings() { return frontendSettings; }
    public FrontendSettingsModel getFrontendSettingsModel() { return frontendSettings; }

    public List<ClockModel> getClockModels() { return new ArrayList<ClockModel>(clocks.values()); }
    public List<TeamModel> getTeamModels() { return new ArrayList<TeamModel>(teams.values()); }
    public List<PeriodModel> getPeriodModels() {
	List<PeriodModel> periodModels = new ArrayList<>(periods.values());
	Collections.sort(periodModels, Comparators.PeriodComparator);
	return Collections.unmodifiableList(periodModels);
    }

    public List<Clock> getClocks() { return new ArrayList<Clock>(getClockModels()); }
    public List<Team> getTeams() { return new ArrayList<Team>(getTeamModels()); }
    public List<Period> getPeriods() {return new ArrayList<Period>(getPeriodModels()); }

    public Clock getClock(String id) { return getClockModel(id).getClock(); }
    public Team getTeam(String id) { return getTeamModel(id).getTeam(); }
    public TimeoutOwner getTimeoutOwner(String id) { return timeoutOwners.get(id); }
    public Period getPeriod(String id) { return getPeriodModel(id); }
    public Timeout getTimeout(String id) { return getTimeoutModel(id); }
    public Jam getJam(String id) { return getJamModel(id); }
    public TeamJam getTeamJam(String id) { return getTeamJamModel(id); }
    public ScoringTrip getScoringTrip(String id) { return getScoringTripModel(id); }
    public Fielding getFielding(String id) { return getFieldingModel(id); }
    public Skater getSkater(String id) { return getSkaterModel(id); }
    public Penalty getPenalty(String id) { return getPenaltyModel(id); }
    public BoxTrip getBoxTrip(String id) { return getBoxTripModel(id); }

    public ClockModel getClockModel(String id) { return clocks.get(id); }
    public TeamModel getTeamModel(String id) { return teams.get(id); }
    public PeriodModel getPeriodModel(String id) { return periods.get(id); }
    public TimeoutModel getTimeoutModel(String id) { return timeouts.get(id); }
    public JamModel getJamModel(String id) { return jams.get(id); }
    public TeamJamModel getTeamJamModel(String id) { return teamJams.get(id); }
    public ScoringTripModel getScoringTripModel(String id) { return scoringTrips.get(id); }
    public FieldingModel getFieldingModel(String id) { return fieldings.get(id); }
    public SkaterModel getSkaterModel(String id) { return skaters.get(id); }
    public PenaltyModel getPenaltyModel(String id) { return penalties.get(id); }
    public BoxTripModel getBoxTripModel(String id) { return boxTrips.get(id); }

    public Period getCurrentPeriod() { return getCurrentPeriodModel(); }
    public PeriodModel getCurrentPeriodModel() { return currentPeriod; }
    private void addPeriod() { addPeriod(new DefaultPeriodModel(lastPeriod));}
    public void addPeriod(PeriodModel period) {	
        synchronized (coreLock) {
            requestBatchStart();
            Clock pc = getClock(Clock.ID_PERIOD);
            int last = pc.getNumber();
            if (lastPeriod == null || lastPeriod.getNumber() < period.getNumber()) {
        	lastPeriod = period;
            }
            if (period.isCurrent()) {
        	currentPeriod = period;
            }
            scoreBoardChange(new ScoreBoardEvent(this, EVENT_ADD_PERIOD, period, null));
            if (pc.getNumber() != last) {
        	scoreBoardChange(new ScoreBoardEvent(pc, Clock.EVENT_NUMBER, pc.getNumber(), last));
        	scoreBoardChange(new ScoreBoardEvent(getClock(Clock.ID_INTERMISSION), Clock.EVENT_NUMBER, pc.getNumber(), last));
            }
            requestBatchEnd();
        }
    }
    public void registerPeriod(PeriodModel period) {
        synchronized (coreLock) {
            period.addScoreBoardListener(this);
            periods.put(period.getId(), period);            
        }
    }
    public void removePeriod(PeriodModel period) {
        synchronized (coreLock) {
            if (period.getId() == "Period0") { return; }
            Clock pc = getClock(Clock.ID_PERIOD);
            int last = pc.getNumber();
            requestBatchStart();
            if (currentPeriod == period) { 
        	currentPeriod = currentPeriod.getPrevious();
        	currentPeriod.setCurrent(true);
            }
            if (lastPeriod == period) { 
        	lastPeriod = lastPeriod.getPrevious();
        	lastPeriod.addJam();
            }
            period.removeScoreBoardListener(this);
            period.unlink();
            periods.remove(period.getId());
            scoreBoardChange(new ScoreBoardEvent(this, EVENT_REMOVE_PERIOD, period, null));
            if (pc.getNumber() != last) {
        	scoreBoardChange(new ScoreBoardEvent(pc, Clock.EVENT_NUMBER, pc.getNumber(), last));
        	scoreBoardChange(new ScoreBoardEvent(getClock(Clock.ID_INTERMISSION), Clock.EVENT_NUMBER, pc.getNumber(), last));
            }
            requestBatchEnd();
        }
    }

    public Timeout getCurrentTimeout() { return getCurrentTimeoutModel(); }
    public TimeoutModel getCurrentTimeoutModel() {
	return getCurrentPeriodModel().getCurrentTimeoutModel();
    }
    public void registerTimeoutModel(TimeoutModel timeout) {
	synchronized (coreLock) {
	    timeout.addScoreBoardListener(this);
	    timeouts.put(timeout.getId(), timeout);
	}
    }
    public void deleteTimeoutModel(TimeoutModel timeout) {
	synchronized (coreLock) {
	    timeout.removeScoreBoardListener(this);
	    timeout.unlink();
	    timeouts.remove(timeout.getId());
	}
    }

    public Jam getCurrentJam() { return getCurrentJamModel(); }
    public JamModel getCurrentJamModel() {
	return getCurrentPeriodModel().getCurrentJamModel();
    }
    public void registerJamModel(JamModel jam) {
	synchronized (coreLock) {
	    jam.addScoreBoardListener(this);
	    jams.put(jam.getId(), jam);
	}
    }
    public void deleteJamModel(JamModel jam) {
	synchronized (coreLock) {
	    if (jam.getId() == "Jam0") { return; }
	    jam.removeScoreBoardListener(this);
	    jam.unlink();
	    jams.remove(jam.getId());
	}
    }
    
    public void registerTeamJamModel(TeamJamModel teamjam) {
	synchronized (coreLock) {
	    teamjam.addScoreBoardListener(this);
	    teamJams.put(teamjam.getId(), teamjam);
	}
    }
    public void deleteTeamJamModel(TeamJamModel teamjam) {
	synchronized (coreLock) {
	    teamjam.removeScoreBoardListener(this);
	    teamjam.unlink();
	    teamJams.remove(teamjam.getId());
	}
    }
    
    public void registerScoringTripModel(ScoringTripModel trip) {
	synchronized (coreLock) {
	    trip.addScoreBoardListener(this);
	    scoringTrips.put(trip.getId(), trip);
	}
    }
    public void deleteScoringTripModel(ScoringTripModel trip) {
	synchronized (coreLock) {
	    trip.removeScoreBoardListener(this);
	    trip.unlink();
	    scoringTrips.remove(trip.getId());
	}
    }
    
    public void registerFieldingModel(FieldingModel fielding) {
	synchronized (coreLock) {
	    fielding.addScoreBoardListener(this);
	    fieldings.put(fielding.getId(), fielding);
	}
    }
    public void deleteFieldingModel(FieldingModel fielding) {
	synchronized (coreLock) {
	    fielding.removeScoreBoardListener(this);
	    fielding.unlink();
	    fieldings.remove(fielding.getId());
	}
    }
    
    public void registerSkaterModel(SkaterModel skater) {
	synchronized (coreLock) {
	    skater.addScoreBoardListener(this);
	    skaters.put(skater.getId(), skater);
	}
    }
    public void deleteSkaterModel(SkaterModel skater) {
	synchronized (coreLock) {
	    skater.removeScoreBoardListener(this);
	    skater.unlink();
	    skaters.remove(skater.getId());
	}
    }
    
    public void registerPenaltyModel(PenaltyModel penalty) {
	synchronized (coreLock) {
	    penalty.addScoreBoardListener(this);
	    penalties.put(penalty.getId(), penalty);
	}
    }
    public void deletePenaltyModel(PenaltyModel penalty) {
	synchronized (coreLock) {
	    penalty.removeScoreBoardListener(this);
	    penalty.unlink();
	    penalties.remove(penalty.getId());
	}
    }
    
    public void registerBoxTripModel(BoxTripModel boxTrip) {
	synchronized (coreLock) {
	    boxTrip.addScoreBoardListener(this);
	    boxTrips.put(boxTrip.getId(), boxTrip);
	}
    }
    public void deleteBoxTripModel(BoxTripModel boxTrip) {
	synchronized (coreLock) {
	    boxTrip.removeScoreBoardListener(this);
	    boxTrip.unlink();
	    boxTrips.remove(boxTrip.getId());
	}
    }
    
    public TimeoutOwner getTimeoutOwner() { 
	if (getCurrentTimeout() == null || !getCurrentTimeout().isRunning()) {
	    return TIMEOUT_OWNER_NONE;
	} else {
	    return getCurrentTimeout().getOwner();
	}
    }
    public boolean isOfficialReview() { 
	if (getCurrentTimeout() == null || !getCurrentTimeout().isRunning()) {
	    return false;
	} else {
	    return getCurrentTimeout().isOfficialReview();
	}
    }
    
    public int getTotalNumberPeriods() {
	return settings.getInt(SETTING_NUMBER_PERIODS);
    }

    public int getNumberTimeouts() { return timeouts.size(); }
    
    private void createClockModel(String id) {
        if ((id == null) || (id.equals(""))) {
            return;
        }

        ClockModel model = new DefaultClockModel(this, id);
        model.addScoreBoardListener(this);
        clocks.put(id, model);
        scoreBoardChange(new ScoreBoardEvent(this, EVENT_ADD_CLOCK, model, null));
    }

    private void createTeamModel(String id) {
        if ((id == null) || (id.equals(""))) {
            return;
        }

        TeamModel model = new DefaultTeamModel(this, id);
        model.addScoreBoardListener(this);
        teams.put(id, model);
        timeoutOwners.put(id, model);
        scoreBoardChange(new ScoreBoardEvent(this, EVENT_ADD_TEAM, model, null));
    }

    
    
    private HashMap<String,ClockModel> clocks = new HashMap<String,ClockModel>();
    private HashMap<String,TeamModel> teams = new HashMap<String,TeamModel>();
    private HashMap<String, TimeoutOwner> timeoutOwners = new HashMap<String, TimeoutOwner>();
    private HashMap<String, PeriodModel> periods = new HashMap<String, PeriodModel>();
    private HashMap<String, TimeoutModel> timeouts = new HashMap<String, TimeoutModel>();
    private HashMap<String, JamModel> jams = new HashMap<String, JamModel>();
    private HashMap<String, TeamJamModel> teamJams = new HashMap<String, TeamJamModel>();
    private HashMap<String, ScoringTripModel> scoringTrips = new HashMap<String, ScoringTripModel>();
    private HashMap<String, FieldingModel> fieldings = new HashMap<String, FieldingModel>();
    private HashMap<String, SkaterModel> skaters = new HashMap<String, SkaterModel>();
    private HashMap<String, PenaltyModel> penalties = new HashMap<String, PenaltyModel>();
    private HashMap<String, BoxTripModel> boxTrips = new HashMap<String, BoxTripModel>();

    private PeriodModel currentPeriod;
    private PeriodModel lastPeriod;

    protected ScoreBoardSnapshot snapshot = null;
    private boolean replacePending = false;

    private static Object coreLock = new Object();

    private boolean restartPcAfterTimeout;

    private boolean inOvertime = false;

    private boolean officialScore = false;

    private long walltimeStart = 0;
    private long walltimeEnd = 0;
    
    private Ruleset ruleset = null;
    private DefaultSettingsModel settings = null;
    private DefaultFrontendSettingsModel frontendSettings = null;

    private XmlScoreBoard xmlScoreBoard;

    private ScoreBoardListener periodEndListener = new ScoreBoardListener() {
        public void scoreBoardChange(ScoreBoardEvent event) {
            if (settings.getBoolean(SETTING_PERIOD_END_BETWEEN_JAMS)) {
                _possiblyEndPeriod();
            }
        }
    };
    private ScoreBoardListener jamEndListener = new ScoreBoardListener() {
        public void scoreBoardChange(ScoreBoardEvent event) {
            ClockModel jc = getClockModel(Clock.ID_JAM);
            if (jc.isTimeAtEnd() && settings.getBoolean(SETTING_AUTO_END_JAM)) {
                //clock has run down naturally
                requestBatchStart();
                setLabels(ACTION_START_JAM, ACTION_NONE, ACTION_TIMEOUT);
                _endJam(true);
                requestBatchEnd();
            }
        }
    };
    private ScoreBoardListener intermissionEndListener = new ScoreBoardListener() {
        public void scoreBoardChange(ScoreBoardEvent event) {
            if (getClock(Clock.ID_INTERMISSION).isTimeAtEnd()) {
                //clock has run down naturally
                _endIntermission(true);
            }
        }
    };
    private ScoreBoardListener lineupClockListener = new ScoreBoardListener() {
        public void scoreBoardChange(ScoreBoardEvent event) {
            if (settings.getBoolean(SETTING_AUTO_START)) {
                _possiblyAutostart();
            }
        }
    };
    private ScoreBoardListener timeoutClockListener = new ScoreBoardListener() {
        public void scoreBoardChange(ScoreBoardEvent event) {
            if (settings.getBoolean(SETTING_AUTO_END_TTO) &&
                    (getTimeoutOwner() instanceof Team) &&
                    (long)event.getValue() == settings.getLong(SETTING_TTO_DURATION)) {
                stopJamTO();
            }
            if ((long)event.getValue() == settings.getLong(SETTING_STOP_PC_AFTER_TO_DURATION) &&
                    getClock(Clock.ID_PERIOD).isRunning()) {
                getClockModel(Clock.ID_PERIOD).stop();
            }
        }
    };

    public static class ScoreBoardSnapshot {
        private ScoreBoardSnapshot(DefaultScoreBoardModel sbm, String type) {
            snapshotTime = ScoreBoardClock.getInstance().getCurrentTime();
            this.type = type;
            currentPeriod = sbm.getCurrentPeriodModel();
            inOvertime = sbm.isInOvertime();
            restartPcAfterTo = sbm.restartPcAfterTimeout;
            startLabel = sbm.getSettings().get(BUTTON_START);
            stopLabel = sbm.getSettings().get(BUTTON_STOP);
            timeoutLabel = sbm.getSettings().get(BUTTON_TIMEOUT);
            periodSnapshot = sbm.getCurrentPeriodModel().snapshot();
            clockSnapshots = new HashMap<String, DefaultClockModel.ClockSnapshotModel>();
            for (ClockModel clock : sbm.getClockModels()) {
                clockSnapshots.put(clock.getId(), clock.snapshot());
            }
        }

        public String getType() { return type; }
        public long getSnapshotTime() { return snapshotTime; }
        public boolean inOvertime() { return inOvertime; }
        public boolean restartPcAfterTo() { return restartPcAfterTo; }
        public PeriodModel getCurrentPeriod() { return currentPeriod; }
        public String getStartLabel() { return startLabel; }
        public String getStopLabel() { return stopLabel; }
        public String getTimeoutLabel() { return timeoutLabel; }
        public PeriodSnapshotModel getPeriodSnapshot() { return periodSnapshot; }
        public Map<String, ClockModel.ClockSnapshotModel> getClockSnapshots() { return clockSnapshots; }
        public DefaultClockModel.ClockSnapshotModel getClockSnapshot(String clock) { return clockSnapshots.get(clock); }

        private String type;
        private long snapshotTime;
        private boolean inOvertime;
        private boolean restartPcAfterTo;
        private PeriodModel currentPeriod;
        private String startLabel;
        private String stopLabel;
        private String timeoutLabel;
        private PeriodSnapshotModel periodSnapshot;
        private Map<String, ClockModel.ClockSnapshotModel> clockSnapshots;
    }
    
    public static class SimpleTimeoutOwner implements TimeoutOwner {
	SimpleTimeoutOwner(String id) {
	    this.id = id;
	}
	
	public String getId() { return id; }
	public String toString() { return id; }
	
	private String id;
    }
    
    public static final TimeoutOwner TIMEOUT_OWNER_NONE = new SimpleTimeoutOwner(TimeoutOwner.NONE);
    public static final TimeoutOwner TIMEOUT_OWNER_OTO = new SimpleTimeoutOwner(TimeoutOwner.OTO);
}

