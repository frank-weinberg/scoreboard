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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.carolinarollergirls.scoreboard.Clock;
import com.carolinarollergirls.scoreboard.Ruleset;
import com.carolinarollergirls.scoreboard.ScoreBoard;
import com.carolinarollergirls.scoreboard.Settings;
import com.carolinarollergirls.scoreboard.event.ScoreBoardEvent;
import com.carolinarollergirls.scoreboard.model.ScoreBoardModel;
import com.carolinarollergirls.scoreboard.model.SettingsModel;

public class DefaultSettingsModel extends DefaultScoreBoardEventProvider implements SettingsModel, Ruleset.RulesetReceiver {
	public DefaultSettingsModel(ScoreBoardModel s, DefaultScoreBoardEventProvider p) {
		sbm = s;
		parent = p;
		addScoreBoardListener(parent);
		reset();
	}

	public void applyRule(String rule, Object value) {
		synchronized (settingsLock) {
			if (ruleMapping.containsKey(rule)) {
				for (String map : ruleMapping.get(rule))
					set(map, String.valueOf(value));
			} else
				set(rule, String.valueOf(value));
		}
	}

	public String getProviderName() { return "Settings"; }
	public Class<Settings> getProviderClass() { return Settings.class; }
	public String getProviderId() { return ""; }

	public DefaultScoreBoardEventProvider getParent() { return parent; }

	public void reset() {
		synchronized (settingsLock) {
			List<String> keys = new ArrayList<String>(settings.keySet());
			for (String k : keys) {
				set(k, null);
			}
		}
		setDefaults();
		sbm._getRuleset().apply(true, this);
	}
	
	private void setDefaults() {
		set(Clock.SETTING_CLOCK_SYNC, "true");
		set(ScoreBoard.SETTING_LINEUP_AFTER_TIMEOUT, "true");
		set("ScoreBoard.Intermission.PreGame", "Time To Derby");
		set("ScoreBoard.Intermission.Intermission", "Intermission");
		set("ScoreBoard.Intermission.Unofficial", "Unofficial Score");
		set("ScoreBoard.Intermission.Official", "Final Score");
		set("ScoreBoard.View_HideJamTotals", "false");
		set("ScoreBoard.View_BackgroundStyle", "bg_black");
		set("ScoreBoard.View_BoxStyle", "box_flat");
		set("ScoreBoard.View_SwapTeams", "false");
		set("ScoreBoard.View_SidePadding", "0");
		set("ScoreBoard.View_CurrentView", "scoreboard");
		set("ScoreBoard.View_CustomHTML", "/customhtml/fullscreen/example.html");
		set("ScoreBoard.View_Image", "/images/fullscreen/American Flag.jpg");
		set("ScoreBoard.View_Video", "/videos/fullscreen/American Flag.webm");
		set("ScoreBoard.Preview_HideJamTotals", "false");
		set("ScoreBoard.Preview_BackgroundStyle", "bg_black");
		set("ScoreBoard.Preview_BoxStyle", "box_flat");
		set("ScoreBoard.Preview_SwapTeams", "false");
		set("ScoreBoard.Preview_SidePadding", "0");
		set("ScoreBoard.Preview_CurrentView", "scoreboard");
		set("ScoreBoard.Preview_CustomHTML", "/customhtml/fullscreen/example.html");
		set("ScoreBoard.Preview_Image", "/images/fullscreen/American Flag.jpg");
		set("ScoreBoard.Preview_Video", "/videos/fullscreen/American Flag.webm");
		set("ScoreBoard.Overlay.TeamLogos", "true");
		set("ScoreBoard.Overlay.LogoBackground", "true");
		set("Clock." + Clock.ID_PERIOD + ".Direction", "true");
		set("Clock." + Clock.ID_JAM + ".Direction", "true");
		set("Clock." + Clock.ID_LINEUP + ".Direction", "false");
		set("Clock." + Clock.ID_TIMEOUT + ".Direction", "false");
		set("Clock." + Clock.ID_INTERMISSION + ".Direction", "true");
	}

	public void addRuleMapping(String rule, String[] mapTo) {
		synchronized (settingsLock) {
			List<String> l = ruleMapping.get(rule);
			if (l == null) {
				l = new ArrayList<String>();
				ruleMapping.put(rule, l);
			}
			for (String map : mapTo)
				l.add(map);
		}
	}

	public Map<String, String> getAll() {
		synchronized (settingsLock) {
			return Collections.unmodifiableMap(new Hashtable<String, String>(settings));
		}
	}
	public String get(String k) {
		synchronized (settingsLock) {
			return settings.get(k);
		}
	}
	public boolean getBoolean(String k) {
		return Boolean.parseBoolean(get(k));
	}
	public long getLong(String k) {
		return Long.parseLong(get(k));
	}
	public int getInt(String k) {
		return Integer.parseInt(get(k));
	}
	public void set(String k, String v) {
		synchronized (settingsLock) {
			String last = settings.get(k);
			if (v == null || v.equals(""))
				v = "";
			settings.put(k, v);
			scoreBoardChange(new ScoreBoardEvent(this, k, v, last));
		}
	}
	public void set(Map<String, String> s) {
		synchronized (settingsLock) {
			// Remove settings not in the new set
			for (String k : settings.keySet())
				if (!s.containsKey(k))
					set(k, null);

			// Set settings from new set
			for (String k : s.keySet())
				set(k, s.get(k));
		}
	}

	protected ScoreBoardModel sbm = null;
	protected Map<String, String> settings = new Hashtable<String, String>();
	protected Map<String, List<String>> ruleMapping = new Hashtable<String, List<String>>();
	protected Object settingsLock = new Object();
	protected DefaultScoreBoardEventProvider parent = null;
}
