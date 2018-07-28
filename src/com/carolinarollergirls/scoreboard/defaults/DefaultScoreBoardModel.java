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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.carolinarollergirls.scoreboard.Clock;
import com.carolinarollergirls.scoreboard.Ruleset;
import com.carolinarollergirls.scoreboard.ScoreBoard;
import com.carolinarollergirls.scoreboard.ScoreBoardManager;
import com.carolinarollergirls.scoreboard.Settings;
import com.carolinarollergirls.scoreboard.Team;
import com.carolinarollergirls.scoreboard.event.ConditionalScoreBoardListener;
import com.carolinarollergirls.scoreboard.event.ScoreBoardEvent;
import com.carolinarollergirls.scoreboard.event.ScoreBoardListener;
import com.carolinarollergirls.scoreboard.model.ClockModel;
import com.carolinarollergirls.scoreboard.model.ScoreBoardModel;
import com.carolinarollergirls.scoreboard.model.SettingsModel;
import com.carolinarollergirls.scoreboard.model.TeamModel;
import com.carolinarollergirls.scoreboard.penalties.PenaltyCodesManager;
import com.carolinarollergirls.scoreboard.snapshots.ScoreBoardSnapshot;
import com.carolinarollergirls.scoreboard.xml.XmlScoreBoard;

public class DefaultScoreBoardModel extends DefaultScoreBoardEventProvider implements ScoreBoardModel
{
	public DefaultScoreBoardModel() {
		settings = new DefaultSettingsModel(this, this);
		Ruleset.registerRule(settings, "ScoreBoard." + Clock.ID_INTERMISSION + ".PreGame");
		Ruleset.registerRule(settings, "ScoreBoard." + Clock.ID_INTERMISSION + ".Intermission");
		Ruleset.registerRule(settings, "ScoreBoard." + Clock.ID_INTERMISSION + ".Unofficial");
		Ruleset.registerRule(settings, "ScoreBoard." + Clock.ID_INTERMISSION + ".Official");
		Ruleset.registerRule(settings, "ScoreBoard.Clock.Sync");
		Ruleset.registerRule(settings, "ScoreBoard." + Clock.ID_JAM + ".ResetNumberEachPeriod");
		Ruleset.registerRule(settings, "ScoreBoard." + Clock.ID_LINEUP + ".AutoStart");
		Ruleset.registerRule(settings, "ScoreBoard." + Clock.ID_LINEUP + ".AutoStartBuffer");
		Ruleset.registerRule(settings, "ScoreBoard." + Clock.ID_LINEUP + ".AutoStartType");
		Ruleset.registerRule(settings, "Clock." + Clock.ID_INTERMISSION + ".Time");
		Ruleset.registerRule(settings, "Clock." + Clock.ID_LINEUP + ".Time");
		Ruleset.registerRule(settings, "Clock." + Clock.ID_LINEUP + ".OvertimeTime");

		settings.addRuleMapping("ScoreBoard.BackgroundStyle", new String[] { "ScoreBoard.Preview_BackgroundStyle", "ScoreBoard.View_BackgroundStyle" });
		settings.addRuleMapping("ScoreBoard.BoxStyle",        new String[] { "ScoreBoard.Preview_BoxStyle",        "ScoreBoard.View_BoxStyle" });
		settings.addRuleMapping("ScoreBoard.CurrentView",     new String[] { "ScoreBoard.Preview_CurrentView",     "ScoreBoard.View_CurrentView" });
		settings.addRuleMapping("ScoreBoard.CustomHtml",      new String[] { "ScoreBoard.Preview_CustomHtml",      "ScoreBoard.View_CustomHtml" });
		settings.addRuleMapping("ScoreBoard.HideJamTotals",   new String[] { "ScoreBoard.Preview_HideJamTotals",   "ScoreBoard.View_HideJamTotals" });
		settings.addRuleMapping("ScoreBoard.Image",           new String[] { "ScoreBoard.Preview_Image",           "ScoreBoard.View_Image" });
		settings.addRuleMapping("ScoreBoard.SidePadding",     new String[] { "ScoreBoard.Preview_SidePadding",     "ScoreBoard.View_SidePadding" });
		settings.addRuleMapping("ScoreBoard.SwapTeams",       new String[] { "ScoreBoard.Preview_SwapTeams",       "ScoreBoard.View_SwapTeams" });
		settings.addRuleMapping("ScoreBoard.Video",           new String[] { "ScoreBoard.Preview_Video",           "ScoreBoard.View_Video" });

		Ruleset.registerRule(settings, "ScoreBoard.BackgroundStyle");
		Ruleset.registerRule(settings, "ScoreBoard.BoxStyle");
		Ruleset.registerRule(settings, "ScoreBoard.CurrentView");
		Ruleset.registerRule(settings, "ScoreBoard.CustomHtml");
		Ruleset.registerRule(settings, "ScoreBoard.HideJamTotals");
		Ruleset.registerRule(settings, "ScoreBoard.Image");
		Ruleset.registerRule(settings, "ScoreBoard.SidePadding");
		Ruleset.registerRule(settings, "ScoreBoard.SwapTeams");
		Ruleset.registerRule(settings, "ScoreBoard.Video");
		Ruleset.registerRule(settings, PenaltyCodesManager.PenaltiesFileSetting);

		reset();
		addInPeriodListeners();
		xmlScoreBoard = new XmlScoreBoard(this);
	}

	public String getProviderName() { return "ScoreBoard"; }
	public Class<ScoreBoard> getProviderClass() { return ScoreBoard.class; }
	public String getProviderId() { return ""; }

	public XmlScoreBoard getXmlScoreBoard() { return xmlScoreBoard; }

	public ScoreBoard getScoreBoard() { return this; }

	public void reset() {
		_getRuleset().apply(true);

		Iterator<ClockModel> c = getClockModels().iterator();
		while (c.hasNext())
			c.next().reset();
		Iterator<TeamModel> t = getTeamModels().iterator();
		while (t.hasNext())
			t.next().reset();

		setTimeoutOwner(DEFAULT_TIMEOUT_OWNER);
		setOfficialReview(false);
		setInPeriod(false);
		setInOvertime(false);

		settings.reset();
	}

	public boolean isInPeriod() { return inPeriod; }
	public void setInPeriod(boolean p) {
		synchronized (inPeriodLock) {
			if (p == inPeriod) { return; }
			Boolean last = new Boolean(inPeriod);
			inPeriod = p;
			scoreBoardChange(new ScoreBoardEvent(this, EVENT_IN_PERIOD, new Boolean(inPeriod), last));
		}
	}
	protected void addInPeriodListeners() {
		addScoreBoardListener(new ConditionalScoreBoardListener(Clock.class, Clock.ID_PERIOD, "Running", Boolean.FALSE, periodEndListener));
		addScoreBoardListener(new ConditionalScoreBoardListener(Clock.class, Clock.ID_JAM, "Running", Boolean.FALSE, jamEndListener));
		addScoreBoardListener(new ConditionalScoreBoardListener(Clock.class, Clock.ID_INTERMISSION, "Running", Boolean.FALSE, intermissionEndListener));
		addScoreBoardListener(new ConditionalScoreBoardListener(Clock.class, Clock.ID_LINEUP, "Time", lineupClockListener));
	}

	public boolean isInOvertime() { return inOvertime; }
	public void setInOvertime(boolean o) {
		if (o == inOvertime) { return; }
		synchronized (inOvertimeLock) {
			Boolean last = new Boolean(inOvertime);
			inOvertime = o;
			scoreBoardChange(new ScoreBoardEvent(this, EVENT_IN_OVERTIME, new Boolean(inOvertime), last));
		}
		ClockModel lc = getClockModel(Clock.ID_LINEUP);
		if (!o && lc.isCountDirectionDown()) {
			lc.setMaximumTime(settings.getLong("Clock." + Clock.ID_LINEUP + ".Time"));
		}
	}
	public void startOvertime() {
		synchronized (runLock) {
			requestBatchStart();
			ClockModel pc = getClockModel(Clock.ID_PERIOD);
			ClockModel jc = getClockModel(Clock.ID_JAM);
			ClockModel lc = getClockModel(Clock.ID_LINEUP);
			ClockModel tc = getClockModel(Clock.ID_TIMEOUT);
			if (pc.isRunning() || jc.isRunning())
				return;
			if (pc.getNumber() < pc.getMaximumNumber())
				return;
			if (!pc.isTimeAtEnd())
				return;
			createSnapshot(ACTION_OVERTIME);
			
			setInOvertime(true);
			if (tc.isRunning()) {
				_endTimeout();
			}
			long otLineupTime = settings.getLong("Clock." + Clock.ID_LINEUP + ".OvertimeTime");
			if (lc.getMaximumTime() < otLineupTime) {
				lc.setMaximumTime(otLineupTime);
			}
			_startLineup();
			requestBatchEnd();
		}
	}

	public boolean isOfficialScore() { return officialScore; }
	public void setOfficialScore(boolean o) {
		synchronized (officialScoreLock) {
			Boolean last = new Boolean(officialScore);
			officialScore = o;
			scoreBoardChange(new ScoreBoardEvent(this, EVENT_OFFICIAL_SCORE, new Boolean(officialScore), last));
		}
	}

	public void startJam() {
		synchronized (runLock) {
			if (!getClock(Clock.ID_JAM).isRunning()) {
				createSnapshot(ACTION_START_JAM);
				_startJam();
				ScoreBoardManager.gameSnapshot();
			}
		}
	}
	public void stopJam() {
		synchronized (runLock) {
			ClockModel jc = getClockModel(Clock.ID_JAM);
			ClockModel lc = getClockModel(Clock.ID_LINEUP);
			ClockModel tc = getClockModel(Clock.ID_TIMEOUT);

			if (jc.isRunning()) {
				ScoreBoardManager.gameSnapshot(true);
				createSnapshot(ACTION_STOP_JAM);
				_endJam();
			} else if (tc.isRunning()) {
				createSnapshot(ACTION_STOP_TO);
				_endTimeout();
			} else if (!lc.isRunning()) {
				createSnapshot(ACTION_LINEUP);
				_startLineup();
			}
		}
	}
	public void timeout() { 
		synchronized (runLock) {
			createSnapshot(ACTION_TIMEOUT);

			requestBatchStart();
			_startTimeout();
			requestBatchEnd();

			ScoreBoardManager.gameSnapshot();
		}
	}
	public void setTimeoutType(String owner, boolean review) {
		ClockModel tc = getClockModel(Clock.ID_TIMEOUT);

		requestBatchStart();
		if (!tc.isRunning()) {
			timeout();
		}
		setTimeoutOwner(owner);
		setOfficialReview(review);
		requestBatchEnd();
	}
	private void _preparePeriod() {
		ClockModel pc = getClockModel(Clock.ID_PERIOD);
		ClockModel jc = getClockModel(Clock.ID_JAM);
		ClockModel ic = getClockModel(Clock.ID_INTERMISSION);

		requestBatchStart();
		pc.setNumber(ic.getNumber()+1);
		pc.resetTime();
		if (settings.getBoolean("ScoreBoard." + Clock.ID_JAM + ".ResetNumberEachPeriod")) {
			jc.setNumber(jc.getMinimumNumber());
		}
		for (TeamModel t : getTeamModels()) {
			t.resetTimeouts(false);
		}		
		requestBatchEnd();
	}
	private void _possiblyEndPeriod() {
		ClockModel pc = getClockModel(Clock.ID_PERIOD);
		ClockModel jc = getClockModel(Clock.ID_JAM);
		ClockModel lc = getClockModel(Clock.ID_LINEUP);
		ClockModel tc = getClockModel(Clock.ID_TIMEOUT);

		if (pc.isTimeAtEnd() && !pc.isRunning() && !jc.isRunning() && !tc.isRunning()) {
			requestBatchStart();
			setInPeriod(false);
			setOfficialScore(false);
			lc.stop();
			_startIntermission();
			requestBatchEnd();
		}
	}
	private void _startJam() {
		ClockModel pc = getClockModel(Clock.ID_PERIOD);
		ClockModel jc = getClockModel(Clock.ID_JAM);
		ClockModel lc = getClockModel(Clock.ID_LINEUP);
		ClockModel tc = getClockModel(Clock.ID_TIMEOUT);
		ClockModel ic = getClockModel(Clock.ID_INTERMISSION);

		requestBatchStart();
		if (ic.isRunning()) {
			_endIntermission();
		}
		if (tc.isRunning()) {
			_endTimeout();
		}
		lc.stop();
		setInPeriod(true);
		pc.start();
		jc.startNext();

		getTeamModel("1").startJam();
		getTeamModel("2").startJam();
		requestBatchEnd();
	}
	private void _endJam() {
		ClockModel pc = getClockModel(Clock.ID_PERIOD);
		ClockModel jc = getClockModel(Clock.ID_JAM);

		requestBatchStart();
		jc.stop();
		getTeamModel("1").stopJam();
		getTeamModel("2").stopJam();
		setInOvertime(false);

		if (pc.isRunning()) {
			_startLineup();
		} else {
			_possiblyEndPeriod();
		}
		requestBatchEnd();
	}
	private void _startLineup() {
		ClockModel lc = getClockModel(Clock.ID_LINEUP);
		ClockModel ic = getClockModel(Clock.ID_INTERMISSION);

		requestBatchStart();
		if (ic.isRunning()) {
			_endIntermission();
		}
		setInPeriod(true);
		lc.startNext();
		requestBatchEnd();
	}
	private void _startTimeout() {
		ClockModel pc = getClockModel(Clock.ID_PERIOD);
		ClockModel jc = getClockModel(Clock.ID_JAM);
		ClockModel lc = getClockModel(Clock.ID_LINEUP);
		ClockModel tc = getClockModel(Clock.ID_TIMEOUT);
		ClockModel ic = getClockModel(Clock.ID_INTERMISSION);

		if (tc.isRunning()) {
			if (getTimeoutOwner()=="") {
				setTimeoutOwner("O");
			} else {
				setTimeoutOwner("");
			}
			return; 
		}
		
		requestBatchStart();
		pc.stop();
		lc.stop();
		if (jc.isRunning()) {
			_endJam();
		}
		if (ic.isRunning()) {
			_endIntermission();
			setInPeriod(true);
		}
		tc.startNext();
	}
	private void _endTimeout() {
		ClockModel tc = getClockModel(Clock.ID_TIMEOUT);
		ClockModel lc = getClockModel(Clock.ID_LINEUP);
		ClockModel pc = getClockModel(Clock.ID_PERIOD);

		requestBatchStart();
		tc.stop();
		setTimeoutOwner("");
		setOfficialReview(false);
		if (pc.isTimeAtEnd()) {
			_possiblyEndPeriod();
		} else {
			lc.startNext();
		}
		requestBatchEnd();
	}
	private void _startIntermission() {
		ClockModel pc = getClockModel(Clock.ID_PERIOD);
		ClockModel ic = getClockModel(Clock.ID_INTERMISSION);

		requestBatchStart();
		ic.setNumber(pc.getNumber());
		ic.setMaximumTime(settings.getLong("Clock." + Clock.ID_INTERMISSION + ".Time"));
		ic.resetTime();
		ic.start();		
		requestBatchEnd();
	}
	private void _endIntermission() {
		ClockModel ic = getClockModel(Clock.ID_INTERMISSION);
		ClockModel pc = getClockModel(Clock.ID_PERIOD);

		requestBatchStart();
		ic.stop();
		if (ic.getTimeRemaining() < 60000 && pc.getNumber() < pc.getMaximumNumber()) {
			//If less than one minute of intermission is left and there is another period, 
			// go to the next period. Otherwise extend the previous period.
			_preparePeriod();
		}
		requestBatchEnd();
	}


	protected void createSnapshot(String type) {
		snapshot = new ScoreBoardSnapshot(this, type);
	}
	protected void restoreSnapshot() {
		relapseTime = System.currentTimeMillis() - snapshot.getSnapshotTime();
		for (ClockModel clock : getClockModels()) {
			clock.restoreSnapshot(snapshot.getClockSnapshot(clock.getId()));
		}
		for (TeamModel team : getTeamModels()) {
			team.restoreSnapshot(snapshot.getTeamSnapshot(team.getId()));
		}
		setTimeoutOwner(snapshot.getTimeoutOwner());
		setOfficialReview(snapshot.isOfficialReview());
		setInOvertime(snapshot.inOvertime());
		setInPeriod(snapshot.inPeriod());
		snapshot = null;
	}
	protected void relapseTime() {
		for (ClockModel clock : getClockModels()) {
			if (clock.isRunning()) {
				clock.elapseTime(relapseTime);
			}
		}
	}
	public void clockUndo() {
		if (snapshot == null) { return; }
		synchronized (runLock) {
			requestBatchStart();
			restoreSnapshot();
			relapseTime();
			requestBatchEnd();
			ScoreBoardManager.gameSnapshot();
		}
	}
	public void unStartJam() {
		if (snapshot != null && 
				snapshot.getType() == ACTION_START_JAM) {
			clockUndo();
		}
	}
	public void unStopJam() {
		if (snapshot != null && 
				(snapshot.getType() == ACTION_STOP_JAM ||
				 snapshot.getType() == ACTION_STOP_TO ||
				 snapshot.getType() == ACTION_LINEUP)) {
			clockUndo();
		}
	}
	public void unTimeout() {
		if (snapshot != null && 
				snapshot.getType() == ACTION_TIMEOUT) {
			clockUndo();
		}
	}

	public Ruleset _getRuleset() {
		synchronized (rulesetLock) {
			if (ruleset == null) {
				ruleset = Ruleset.findRuleset(null, true);
			}
			return ruleset;
		}
	}
	public String getRuleset() { return _getRuleset().getId().toString(); }
	public void setRuleset(String id) {
		synchronized (rulesetLock) {
			String last = getRuleset();
			ruleset = Ruleset.findRuleset(id, true);
			ruleset.apply(false);
			scoreBoardChange(new ScoreBoardEvent(this, EVENT_RULESET, ruleset.getId().toString(), last));
		}
	}

	public void penalty(String teamId, String skaterId, String penaltyId, boolean fo_exp, int period, int jam, String code){
		getTeamModel(teamId).penalty(skaterId, penaltyId, fo_exp, period, jam, code);
	}

	public Settings getSettings() { return (Settings)settings; }
	public SettingsModel getSettingsModel() { return settings; }

	public List<ClockModel> getClockModels() { return new ArrayList<ClockModel>(clocks.values()); }
	public List<TeamModel> getTeamModels() { return new ArrayList<TeamModel>(teams.values()); }

	public List<Clock> getClocks() { return new ArrayList<Clock>(getClockModels()); }
	public List<Team> getTeams() { return new ArrayList<Team>(getTeamModels()); }

	public Clock getClock(String id) { return getClockModel(id).getClock(); }
	public Team getTeam(String id) { return getTeamModel(id).getTeam(); }

	public ClockModel getClockModel(String id) {
		synchronized (clocks) {
// FIXME - don't auto-create!	 return null instead - or throw exception.	Need to update all callers to handle first.
			if (!clocks.containsKey(id))
				createClockModel(id);

			return clocks.get(id);
		}
	}

	public TeamModel getTeamModel(String id) {
		synchronized (teams) {
// FIXME - don't auto-create!	 return null instead - or throw exception.	Need to update all callers to handle first.
			if (!teams.containsKey(id))
				createTeamModel(id);

			return teams.get(id);
		}
	}

	public String getTimeoutOwner() { return timeoutOwner; }
	public void setTimeoutOwner(String owner) {
		synchronized (timeoutOwnerLock) {
			String last = timeoutOwner;
			timeoutOwner = owner;
			for (TeamModel tm : getTeamModels()) {
				tm.setInTimeout(tm.getId() == owner);
			}
			scoreBoardChange(new ScoreBoardEvent(this, EVENT_TIMEOUT_OWNER, timeoutOwner, last));
		}
	}
	public boolean isOfficialReview() { return officialReview; }
	public void setOfficialReview(boolean official) {
		synchronized (officialReviewLock) {
			boolean last = officialReview;
			officialReview = official;
			for (TeamModel tm : getTeamModels()) {
				tm.setInOfficialReview(tm.getId() == getTimeoutOwner() && official);
			}
			scoreBoardChange(new ScoreBoardEvent(this, EVENT_OFFICIAL_REVIEW, new Boolean(officialReview), last));
		}
	}

	protected void createClockModel(String id) {
		if ((id == null) || (id.equals("")))
			return;

		ClockModel model = new DefaultClockModel(this, id);
		model.addScoreBoardListener(this);
		clocks.put(id, model);
		scoreBoardChange(new ScoreBoardEvent(this, EVENT_ADD_CLOCK, model, null));
	}

	protected void createTeamModel(String id) {
		if ((id == null) || (id.equals("")))
			return;

		TeamModel model = new DefaultTeamModel(this, id);
		model.addScoreBoardListener(this);
		teams.put(id, model);
		scoreBoardChange(new ScoreBoardEvent(this, EVENT_ADD_TEAM, model, null));
	}

	protected HashMap<String,ClockModel> clocks = new HashMap<String,ClockModel>();
	protected HashMap<String,TeamModel> teams = new HashMap<String,TeamModel>();

	protected Object runLock = new Object();
	protected ScoreBoardSnapshot snapshot = null;
	protected long relapseTime = 0;

	protected String timeoutOwner;
	protected Object timeoutOwnerLock = new Object();
	protected boolean officialReview;
	protected Object officialReviewLock = new Object();

	protected boolean inPeriod = false;
	protected Object inPeriodLock = new Object();

	protected boolean inOvertime = false;
	protected Object inOvertimeLock = new Object();

	protected boolean officialScore = false;
	protected Object officialScoreLock = new Object();

	protected Ruleset ruleset = null;
	protected Object rulesetLock = new Object();
	protected DefaultSettingsModel settings = null;

	protected XmlScoreBoard xmlScoreBoard;

	protected ScoreBoardListener periodEndListener = new ScoreBoardListener() {
		public void scoreBoardChange(ScoreBoardEvent event) {
			_possiblyEndPeriod();
		}
	};
	protected ScoreBoardListener jamEndListener = new ScoreBoardListener() {
		public void scoreBoardChange(ScoreBoardEvent event) {
			ClockModel jc = getClockModel(Clock.ID_JAM);
			if (jc.isTimeAtEnd()) {
				//clock has run down naturally
				_endJam();
			}
		}
	};
	protected ScoreBoardListener intermissionEndListener = new ScoreBoardListener() {
		public void scoreBoardChange(ScoreBoardEvent event) {
			if (getClock(Clock.ID_INTERMISSION).isTimeAtEnd()) {
				//clock has run down naturally
				_endIntermission();
			}
		}
	};
	protected ScoreBoardListener lineupClockListener = new ScoreBoardListener() {
		public void scoreBoardChange(ScoreBoardEvent event) {
			if (settings.getBoolean("ScoreBoard." + Clock.ID_LINEUP + ".AutoStart")) {
				ClockModel lc = getClockModel(Clock.ID_LINEUP);
				long bufferTime = settings.getLong("ScoreBoard." + Clock.ID_LINEUP + ".AutoStartBuffer"); 
				long triggerTime = bufferTime + (isInOvertime() ? 
							settings.getLong("Clock." + Clock.ID_LINEUP + ".OvertimeTime") :
							settings.getLong("Clock." + Clock.ID_LINEUP + ".Time"));
				if (lc.getTimeElapsed() >= triggerTime) {
					if (Boolean.parseBoolean(settings.get("ScoreBoard." + Clock.ID_LINEUP + ".AutoStartType"))) {
						requestBatchStart();
						ClockModel jc = getClockModel(Clock.ID_JAM);
						startJam();
						jc.elapseTime(bufferTime);
						requestBatchEnd();
					} else {
						requestBatchStart();
						ClockModel pc = getClockModel(Clock.ID_PERIOD);
						ClockModel tc = getClockModel(Clock.ID_TIMEOUT);
						timeout();
						pc.elapseTime(-bufferTime);
						tc.elapseTime(bufferTime);
						requestBatchEnd();
					}
				}
			}
		}
	};
	public static final String DEFAULT_TIMEOUT_OWNER = "";

	public static final String POLICY_KEY = DefaultScoreBoardModel.class.getName() + ".policy";
	
	public static final String ACTION_START_JAM = "Start Jam";
	public static final String ACTION_STOP_JAM = "Stop Jam";
	public static final String ACTION_STOP_TO = "End Timeout";
	public static final String ACTION_LINEUP = "Lineup";
	public static final String ACTION_TIMEOUT = "Timeout";
	public static final String ACTION_OVERTIME = "Overtime";
}

