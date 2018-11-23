package com.carolinarollergirls.scoreboard.xml;
/**
 * Copyright (C) 2008-2012 Mr Temper <MrTemper@CarolinaRollergirls.com>
 *
 * This file is part of the Carolina Rollergirls (CRG) ScoreBoard.
 * The CRG ScoreBoard is licensed under either the GNU General Public
 * License version 3 (or later), or the Apache License 2.0, at your option.
 * See the file COPYING for details.
 */

import org.jdom.Document;
import org.jdom.Element;

import com.carolinarollergirls.scoreboard.ScoreBoardManager;
import com.carolinarollergirls.scoreboard.event.AsyncScoreBoardListener;
import com.carolinarollergirls.scoreboard.event.ScoreBoardEvent;
import com.carolinarollergirls.scoreboard.event.ScoreBoardEventProvider;
import com.carolinarollergirls.scoreboard.event.ScoreBoardListener;
import com.carolinarollergirls.scoreboard.model.SettingsModel;
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
import com.carolinarollergirls.scoreboard.view.Skater;
import com.carolinarollergirls.scoreboard.view.Team;
import com.carolinarollergirls.scoreboard.view.TeamJam;
import com.carolinarollergirls.scoreboard.view.Timeout;

/**
 * Converts a ScoreBoardEvent into a representative XML Document or XML String.
 *
 * This class is not synchronized.	Each event method modifies the same document.
 */
public class ScoreBoardXmlListener implements ScoreBoardListener {
    public ScoreBoardXmlListener() { }
    public ScoreBoardXmlListener(boolean p) {
        setPersistent(p);
    }
    public ScoreBoardXmlListener(ScoreBoard sb, boolean p) {
        setPersistent(p);
        sb.addScoreBoardListener(new AsyncScoreBoardListener(this));
    }
    public ScoreBoardXmlListener(ScoreBoard sb) {
        sb.addScoreBoardListener(new AsyncScoreBoardListener(this));
    }

    public boolean isEmpty() { return empty; }

    public Document getDocument() { return document; }

    public Document resetDocument() {
        Document oldDoc = document;
        empty = true;
        document = editor.createDocument("ScoreBoard");
        return oldDoc;
    }

    private void batchStart() {
        Element root = document.getRootElement();
        String b = root.getAttributeValue("BATCH_START");
        if (b == null) {
            b = "";
        }
        b = b + "X";
        root.setAttribute("BATCH_START", b);
    }

    private void batchEnd() {
        Element root = document.getRootElement();
        String b = root.getAttributeValue("BATCH_END");
        if (b == null) {
            b = "";
        }
        b = b + "X";
        root.setAttribute("BATCH_END", b);
    }

    public void scoreBoardChange(ScoreBoardEvent event) {
        ScoreBoardEventProvider p = event.getProvider();
        String prop = event.getProperty();
        String v = (event.getValue()==null?null:event.getValue().toString());
        if (prop.equals(ScoreBoardEvent.BATCH_START)) {
            batchStart();
        } else if (prop.equals(ScoreBoardEvent.BATCH_END)) {
            batchEnd();
        } else if (p.getProviderName().equals("Settings")) {
            SettingsModel settings = (SettingsModel)p;
            Element e = editor.setElement(getSettingsElement(settings), "Settings");
            if (e != null) {
                if (v == null) {
                    if (isPersistent()) {
                        editor.removeElement(e, "Setting", prop);
                    } else {
                        editor.setRemovePI(editor.setElement(e, "Setting", prop));
                    }
                } else {
                    editor.setElement(e, "Setting", prop, v);
                }
            } else {
                ScoreBoardManager.printMessage("************ ADD SUPPORT FOR SETTINGS TO ScoreBoardXmlListener FOR " + settings.getParent().getProviderName());
            }
        } else if (p.getProviderName().equals("FrontendSettings")) {
            Element e = editor.setElement(getScoreBoardElement(), "FrontendSettings");
            if (v == null) {
                if (isPersistent()) {
                    editor.removeElement(e, FrontendSettings.EVENT_SETTING, prop);
                } else {
                    editor.setRemovePI(editor.setElement(e, FrontendSettings.EVENT_SETTING, prop));
                }
            } else {
                editor.setElement(e, FrontendSettings.EVENT_SETTING, prop, v);
            }
        } else if (p.getProviderName().equals("ScoreBoard")) {
            if (prop.equals(ScoreBoard.EVENT_ADD_CLOCK)) {
                converter.toElement(getScoreBoardElement(), (Clock)event.getValue());
            } else if (prop.equals(ScoreBoard.EVENT_REMOVE_CLOCK)) {
                if (isPersistent()) {
                    editor.removeElement(getScoreBoardElement(), "Clock", ((Clock)event.getValue()).getId());
                } else {
                    editor.setRemovePI(converter.toElement(getScoreBoardElement(), (Clock)event.getValue()));
                }
            } else if (prop.equals(ScoreBoard.EVENT_ADD_TEAM)) {
                converter.toElement(getScoreBoardElement(), (Team)event.getValue());
            } else if (prop.equals(ScoreBoard.EVENT_REMOVE_TEAM)) {
                if (isPersistent()) {
                    editor.removeElement(getScoreBoardElement(), "Team", ((Team)event.getValue()).getId());
                } else {
                    editor.setRemovePI(converter.toElement(getScoreBoardElement(), (Team)event.getValue()));
                }
            } else if (prop.equals(ScoreBoard.EVENT_ADD_PERIOD)) {
                converter.toElement(getScoreBoardElement(), (Period)event.getValue());
            } else if (prop.equals(ScoreBoard.EVENT_REMOVE_PERIOD)) {
        	editor.setRemovePI(converter.toElement(getScoreBoardElement(), (Period)event.getValue()));
            } else {
                editor.setElement(getScoreBoardElement(), prop, null, v);
            }
        } else if (p.getProviderName().equals("Team")) {
            Team t = (Team)p;
            if (prop.equals(Team.EVENT_ADD_ALTERNATE_NAME)) {
                converter.toElement(getTeamElement(t), (Team.AlternateName)event.getValue());
            } else if (prop.equals(Team.EVENT_REMOVE_ALTERNATE_NAME)) {
                if (isPersistent()) {
                    editor.removeElement(getTeamElement(t), "AlternateName", ((Team.AlternateName)event.getValue()).getId());
                } else {
                    editor.setRemovePI(converter.toElement(getTeamElement(t), (Team.AlternateName)event.getValue()));
                }
            } else if (prop.equals(Team.EVENT_ADD_COLOR)) {
                converter.toElement(getTeamElement(t), (Team.Color)event.getValue());
            } else if (prop.equals(Team.EVENT_REMOVE_COLOR)) {
                if (isPersistent()) {
                    editor.removeElement(getTeamElement(t), "Color", ((Team.Color)event.getValue()).getId());
                } else {
                    editor.setRemovePI(converter.toElement(getTeamElement(t), (Team.Color)event.getValue()));
                }
            } else if (prop.equals(Team.EVENT_ADD_SKATER)) {
                converter.toElement(getTeamElement(t), (Skater)event.getValue());
            } else if (prop.equals(Team.EVENT_REMOVE_SKATER)) {
                if (isPersistent()) {
                    editor.removeElement(getTeamElement(t), "Skater", ((Skater)event.getValue()).getId());
                } else {
                    editor.setRemovePI(converter.toElement(getTeamElement(t), (Skater)event.getValue()));
                }
            } else if (prop.equals("Jammer") || prop.equals("Pivot") || prop.equals("Blocker1") ||
        	    prop.equals("Blocker2") || prop.equals("Blocker3")) {
        	converter.setPosition(getTeamElement(t), t, FloorPosition.fromString(prop));
            } else {
                editor.setElement(getTeamElement((Team)p), prop, null, v);
            }
        } else if (p.getProviderName().equals("AlternateName")) {
            editor.setElement(getAlternateNameElement((Team.AlternateName)p), prop, null, v);
        } else if (p.getProviderName().equals("Color")) {
            editor.setElement(getColorElement((Team.Color)p), prop, null, v);
        } else if (p.getProviderName().equals("Skater")) {
            if (prop.equals(Skater.EVENT_PENALTY)) {
                // Replace whole skater.
                converter.toElement(getTeamElement(((Skater)p).getTeam()), (Skater)p);
            } else {
                editor.setElement(getSkaterElement((Skater)p), prop, null, v);
            }
        } else if (p.getProviderName().equals("Penalty")) {
            if (prop.equals(Penalty.EVENT_ADD_BOX_TRIP)) {
                converter.toElement(getPenaltyElement((Penalty)p), (BoxTrip)event.getValue());
            } else if (prop.equals(Penalty.EVENT_REMOVE_BOX_TRIP)) {
        	editor.setRemovePI(converter.toElement(getPenaltyElement((Penalty)p), (BoxTrip)event.getValue()));
            } else {
                editor.setElement(getPenaltyElement((Penalty)p), prop, null, v);
            }
        } else if (p.getProviderName().equals("Period")) {
            if (prop.equals(Period.EVENT_ADD_TIMEOUT)) {
                converter.toElement(getPeriodElement((Period)p), (Timeout)event.getValue());
            } else if (prop.equals(Period.EVENT_REMOVE_TIMEOUT)) {
        	editor.setRemovePI(converter.toElement(getPeriodElement((Period)p), (Timeout)event.getValue()));
            } else if (prop.equals(Period.EVENT_ADD_JAM)) {
        	converter.toElement(getPeriodElement((Period)p), (Jam)event.getValue());
            } else if (prop.equals(Period.EVENT_REMOVE_JAM)) {
        	editor.setRemovePI(converter.toElement(getPeriodElement((Period)p), (Jam)event.getValue()));
            } else {
                editor.setElement(getPeriodElement((Period)p), prop, null, v);
            }
        } else if (p.getProviderName().equals("Timeout")) {
            editor.setElement(getTimeoutElement((Timeout)p), prop, null, v);
        } else if (p.getProviderName().equals("Jam")) {
            if (prop.equals(Jam.EVENT_ADD_TIMEOUT)) {
        	editor.setElement(getJamElement((Jam)p), "Timeout", v);
            } else if (prop.equals(Jam.EVENT_REMOVE_TIMEOUT)) {
        	editor.setRemovePI(editor.getElement(getJamElement((Jam)p), "Timeout", v));
            } else if (prop.equals(Jam.EVENT_ADD_PENALTY)) {
        	editor.setElement(getJamElement((Jam)p), "Penalty", v);
            } else if (prop.equals(Jam.EVENT_REMOVE_PENALTY)) {
        	editor.setRemovePI(editor.getElement(getJamElement((Jam)p), "Penalty", v));
            } else {
                editor.setElement(getJamElement((Jam)p), prop, null, v);
            }
        } else if (p.getProviderName().equals("TeamJam")) {
            TeamJam tj = (TeamJam)p;
            if (prop.equals(TeamJam.EVENT_ADD_SCORING_TRIP)) {
        	converter.toElement(getTeamJamElement(tj), (ScoringTrip)event.getValue());
            } else if (prop.equals(TeamJam.EVENT_REMOVE_SCORING_TRIP)) {
        	editor.setRemovePI(converter.toElement(getTeamJamElement(tj), (ScoringTrip)event.getValue()));
            } else if (prop.equals(TeamJam.EVENT_ADD_FIELDING)) {
        	converter.toElement(getTeamJamElement(tj), (Fielding)event.getValue());
            } else if (prop.equals(TeamJam.EVENT_REMOVE_FIELDING)) {
        	editor.setRemovePI(converter.toElement(getTeamJamElement(tj), (Fielding)event.getValue()));
            } else if (prop.equals(TeamJam.EVENT_STAR_PASS_TRIP) && v == null) {
        	editor.setRemovePI(editor.getElement(getTeamJamElement(tj), prop));
            } else {
                editor.setElement(getTeamJamElement(tj), prop, null, v);
            }
        } else if (p.getProviderName().equals("ScoringTrip")) {
            editor.setElement(getScoringTripElement((ScoringTrip)p), prop, null, v);
        } else if (p.getProviderName().equals("Fielding")) {
            if (prop.equals(Fielding.EVENT_ADD_BOX_TRIP)) {
                if ((Fielding)p == ((BoxTrip)event.getValue()).getFieldings().first()) {
                    converter.toElement(getFieldingElement((Fielding)p), (BoxTrip)event.getValue());
                } else {
                    editor.setElement(getFieldingElement((Fielding)p), "BoxTrip", ((BoxTrip)event.getValue()).getId());
                }
            } else if (prop.equals(Fielding.EVENT_REMOVE_BOX_TRIP)) {
        	editor.setRemovePI(converter.toElement(getFieldingElement((Fielding)p), (BoxTrip)event.getValue()));
            } else {
                editor.setElement(getFieldingElement((Fielding)p), prop, null, v);
            }
        } else if (p.getProviderName().equals("BoxTrip")) {
            if (prop.equals(BoxTrip.EVENT_ADD_FIELDING)) {
        	editor.setElement(getBoxTripElement((BoxTrip)p), "Fielding", v);
            } else if (prop.equals(BoxTrip.EVENT_REMOVE_FIELDING)) {
        	editor.setRemovePI(editor.getElement(getBoxTripElement((BoxTrip)p), "Fielding", v));
            } else if (prop.equals(BoxTrip.EVENT_ADD_PENALTY)) {
            	editor.setElement(getBoxTripElement((BoxTrip)p), "Penalty", v);
            } else if (prop.equals(BoxTrip.EVENT_REMOVE_PENALTY)) {
            	editor.setRemovePI(editor.getElement(getBoxTripElement((BoxTrip)p), "Penalty", v));
            } else if (prop.equals(BoxTrip.EVENT_ANNOTATION)) {
            	editor.setElement(getBoxTripElement((BoxTrip)p), "Annotation", ((Fielding)event.getPreviousValue()).getId(), v);
            } else {
                editor.setElement(getBoxTripElement((BoxTrip)p), prop, null, v);
            }
        } else if (p.getProviderName().equals("Clock")) {
            Element e = editor.setElement(getClockElement((Clock)p), prop, null, v);
            if (prop.equals("Time")) {
                try {
                    long time = ((Long)event.getValue()).longValue();
                    long prevTime = ((Long)event.getPreviousValue()).longValue();
                    if (time % 1000 == 0 || Math.abs(prevTime - time) >= 1000) {
                        editor.setPI(e, "TimeUpdate", "sec");
                    } else {
                        editor.setPI(e, "TimeUpdate", "ms");
                    }
                } catch (Exception ee) { }
            }
        } else {
            return;
        }
        empty = false;
    }

    public boolean isPersistent() { return persistent; }
    public void setPersistent(boolean p) { persistent = p; }

    protected Element getScoreBoardElement() {
        return editor.getElement(document.getRootElement(), "ScoreBoard");
    }

    private Element getSettingsElement(SettingsModel settings) {
        if (settings.getParent().getProviderName().equals("ScoreBoard")) {
            return getScoreBoardElement();
        }
        return null;
    }

    protected Element getClockElement(Clock clock) {
        return editor.getElement(getScoreBoardElement(), "Clock", clock.getId());
    }

    protected Element getTeamElement(Team team) {
        return editor.getElement(getScoreBoardElement(), "Team", team.getId());
    }

    protected Element getSkaterElement(Skater skater) {
        return editor.getElement(getTeamElement(skater.getTeam()), "Skater", skater.getId());
    }

    protected Element getPenaltyElement(Penalty penalty) {
        return editor.getElement(getSkaterElement(penalty.getSkater()), "Penalty", penalty.getId());
    }

    protected Element getAlternateNameElement(Team.AlternateName alternateName) {
        return editor.getElement(getTeamElement(alternateName.getTeam()), "AlternateName", alternateName.getId());
    }

    protected Element getColorElement(Team.Color color) {
        return editor.getElement(getTeamElement(color.getTeam()), "Color", color.getId());
    }

    protected Element getPeriodElement(Period period) {
        return editor.getElement(getScoreBoardElement(), "Period", period.getId());
    }

    protected Element getTimeoutElement(Timeout timeout) {
        return editor.getElement(getPeriodElement(timeout.getPrecedingJam().getPeriod()), "Timeout", timeout.getId());
    }

    protected Element getJamElement(Jam jam) {
        return editor.getElement(getPeriodElement(jam.getPeriod()), "Jam", jam.getId());
    }

    protected Element getTeamJamElement(TeamJam teamJam) {
        return editor.getElement(getJamElement(teamJam.getJam()), "TeamJam", teamJam.getId());
    }

    protected Element getScoringTripElement(ScoringTrip scoringTrip) {
        return editor.getElement(getTeamJamElement(scoringTrip.getTeamJam()), "ScoringTrip", scoringTrip.getId());
    }

    protected Element getFieldingElement(Fielding fielding) {
        return editor.getElement(getTeamJamElement(fielding.getTeamJam()), "Fielding", fielding.getId());
    }

    protected Element getBoxTripElement(BoxTrip boxTrip) {
        return editor.getElement(getFieldingElement(boxTrip.getFieldings().first()), "BoxTrip", boxTrip.getId());
    }

    protected XmlDocumentEditor editor = new XmlDocumentEditor();
    protected ScoreBoardXmlConverter converter = new ScoreBoardXmlConverter();

    protected Document document = editor.createDocument("ScoreBoard");
    protected boolean empty = true;
    protected boolean persistent = false;
}
