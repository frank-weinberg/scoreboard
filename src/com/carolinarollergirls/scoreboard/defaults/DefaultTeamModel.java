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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.carolinarollergirls.scoreboard.event.DefaultScoreBoardEventProvider;
import com.carolinarollergirls.scoreboard.event.ScoreBoardEvent;
import com.carolinarollergirls.scoreboard.model.ScoreBoardModel;
import com.carolinarollergirls.scoreboard.model.SkaterModel;
import com.carolinarollergirls.scoreboard.model.TeamJamModel;
import com.carolinarollergirls.scoreboard.model.TeamModel;
import com.carolinarollergirls.scoreboard.model.TimeoutModel;
import com.carolinarollergirls.scoreboard.utils.Comparators;
import com.carolinarollergirls.scoreboard.view.FloorPosition;
import com.carolinarollergirls.scoreboard.view.ScoreBoard;
import com.carolinarollergirls.scoreboard.view.Skater;
import com.carolinarollergirls.scoreboard.view.Team;
import com.carolinarollergirls.scoreboard.view.TeamJam;
import com.carolinarollergirls.scoreboard.view.Timeout;

public class DefaultTeamModel extends DefaultScoreBoardEventProvider implements TeamModel {
    public DefaultTeamModel(ScoreBoardModel sbm, String i) {
        scoreBoardModel = sbm;
        id = i;
    }

    public String getProviderName() { return "Team"; }
    public Class<Team> getProviderClass() { return Team.class; }
    public String getProviderId() { return getId(); }

    public ScoreBoard getScoreBoard() { return scoreBoardModel.getScoreBoard(); }
    public ScoreBoardModel getScoreBoardModel() { return scoreBoardModel; }

    public void reset() {
        synchronized (coreLock) {
            updateTeamJamModels();
            setName(DEFAULT_NAME_PREFIX + id);
            setLogo(DEFAULT_LOGO);

            removeAlternateNameModels();
            removeColorModels();
            for (SkaterModel s : new ArrayList<SkaterModel>(skaters)) {
                scoreBoardModel.deleteSkaterModel(s);
            }
        }
    }

    public String getId() { return id; }
    public String toString() { return getId(); }

    public Team getTeam() { return this; }

    public String getName() { return name; }
    public void setName(String n) {
        synchronized (coreLock) {
            String last = name;
            name = n;
            scoreBoardChange(new ScoreBoardEvent(this, EVENT_NAME, name, last));
        }
    }
    
    public void startJam() {
        synchronized (coreLock) {
            updateTeamJamModels();
        }
    }
    public void stopJam() {
        synchronized (coreLock) {
            requestBatchStart();
            benchSkaters();
            requestBatchEnd();
        }
    }
    private void benchSkaters() {
	for (SkaterModel sM : skaters) {
	    sM.bench();
        }
	for (FloorPosition fp : FloorPosition.values()) {
	    scoreBoardChange(new ScoreBoardEvent(this, fp.toString(), getCurrentSkater(fp), null));
	}
    }

    public List<AlternateName> getAlternateNames() {
        synchronized (coreLock) {
            return Collections.unmodifiableList(new ArrayList<AlternateName>(alternateNames.values()));
        }
    }
    public List<AlternateNameModel> getAlternateNameModels() {
        synchronized (coreLock) {
            return Collections.unmodifiableList(new ArrayList<AlternateNameModel>(alternateNames.values()));
        }
    }
    public AlternateName getAlternateName(String i) { return getAlternateNameModel(i); }
    public AlternateNameModel getAlternateNameModel(String i) { return alternateNames.get(i); }
    public void setAlternateNameModel(String i, String n) {
        synchronized (coreLock) {
            if (alternateNames.containsKey(i)) {
                alternateNames.get(i).setName(n);
            } else {
                AlternateNameModel anm = new DefaultAlternateNameModel(this, i, n);
                alternateNames.put(i, anm);
                anm.addScoreBoardListener(this);
                scoreBoardChange(new ScoreBoardEvent(this, EVENT_ADD_ALTERNATE_NAME, anm, null));
            }
        }
    }
    public void removeAlternateNameModel(String i) {
        synchronized (coreLock) {
            AlternateNameModel anm = alternateNames.remove(i);
            anm.removeScoreBoardListener(this);
            scoreBoardChange(new ScoreBoardEvent(this, EVENT_REMOVE_ALTERNATE_NAME, anm, null));
        }
    }
    public void removeAlternateNameModels() {
        synchronized (coreLock) {
            Iterator<AlternateNameModel> i = getAlternateNameModels().iterator();
            while (i.hasNext()) {
                removeAlternateNameModel(i.next().getId());
            }
        }
    }

    public List<Color> getColors() {
        synchronized (coreLock) {
            return Collections.unmodifiableList(new ArrayList<Color>(colors.values()));
        }
    }
    public List<ColorModel> getColorModels() {
        synchronized (coreLock) {
            return Collections.unmodifiableList(new ArrayList<ColorModel>(colors.values()));
        }
    }
    public Color getColor(String i) { return getColorModel(i); }
    public ColorModel getColorModel(String i) { return colors.get(i); }
    public void setColorModel(String i, String c) {
        synchronized (coreLock) {
            if (colors.containsKey(i)) {
                ColorModel cm = colors.get(i);
                cm.setColor(c);
            } else {
                ColorModel cm = new DefaultColorModel(this, i, c);
                colors.put(i, cm);
                cm.addScoreBoardListener(this);
                scoreBoardChange(new ScoreBoardEvent(this, EVENT_ADD_COLOR, cm, null));
            }
        }
    }
    public void removeColorModel(String i) {
        synchronized (coreLock) {
            ColorModel cm = colors.remove(i);
            cm.removeScoreBoardListener(this);
            scoreBoardChange(new ScoreBoardEvent(this, EVENT_REMOVE_COLOR, cm, null));
        }
    }
    public void removeColorModels() {
        synchronized (coreLock) {
            Iterator<ColorModel> i = getColorModels().iterator();
            while (i.hasNext()) {
                removeColorModel(i.next().getId());
            }
        }
    }

    public String getLogo() { return logo; }
    public void setLogo(String l) {
        synchronized (coreLock) {
            String last = logo;
            logo = l;
            scoreBoardChange(new ScoreBoardEvent(this, EVENT_LOGO, logo, last));
        }
    }

    public TeamJam getCurrentTeamJam() { return currentTeamJamModel; }
    public TeamJam getNextTeamJam() { return nextTeamJamModel; }
    public TeamJam getPreviousTeamJam() { return previousTeamJamModel; }
    public TeamJamModel getCurrentTeamJamModel() { return currentTeamJamModel; }
    public TeamJamModel getNextTeamJamModel() { return nextTeamJamModel; }
    public TeamJamModel getPreviousTeamJamModel() { return previousTeamJamModel; }
    public void removeTeamJamModel(TeamJamModel t) {
	synchronized (coreLock) {
	    if (t == currentTeamJamModel || t == previousTeamJamModel || t == nextTeamJamModel) {
		updateTeamJamModels();
	    }
	}
    }
    public void updateTeamJamModels() {
	synchronized (coreLock) {
	    requestBatchStart();
	    boolean lastDisplayLead = currentTeamJamModel == null ? false : displayLead();
	    boolean lastLost = currentTeamJamModel == null ? false : isLost();
	    boolean lastLead = currentTeamJamModel == null ? false : isLead();
	    boolean lastCalloff = currentTeamJamModel == null ? false : isCalloff();
	    boolean lastInjury = currentTeamJamModel == null ? false : isInjury();
	    boolean lastStarPass = currentTeamJamModel == null ? false : isStarPass();
	    currentTeamJamModel = scoreBoardModel.getCurrentJamModel().getTeamJamModel(this);
	    previousTeamJamModel = currentTeamJamModel.getPrevious();
	    nextTeamJamModel = currentTeamJamModel.getNext();
            if (displayLead() != lastDisplayLead) {
        	scoreBoardChange(new ScoreBoardEvent(this, EVENT_DISPLAY_LEAD, displayLead(), lastDisplayLead));
            }
            if (isLost() != lastLost) {
        	scoreBoardChange(new ScoreBoardEvent(this, EVENT_LOST, isLost(), lastLost));
            }
            if (isLead() != lastLead) {
        	scoreBoardChange(new ScoreBoardEvent(this, EVENT_LEAD, isLead(), lastLead));
            }
            if (isCalloff() != lastCalloff) {
        	scoreBoardChange(new ScoreBoardEvent(this, EVENT_CALLOFF, isCalloff(), lastCalloff));
            }
            if (isInjury() != lastInjury) {
        	scoreBoardChange(new ScoreBoardEvent(this, EVENT_INJURY, isInjury(), lastInjury));
            }
            if (isStarPass() != lastStarPass) {
        	scoreBoardChange(new ScoreBoardEvent(this, EVENT_STAR_PASS, isStarPass(), lastStarPass));
            }
	    requestBatchEnd();
	}
    }

    public void timeout() {
        synchronized (coreLock) {
            if (getTimeoutsRemaining() > 0) {
                getScoreBoardModel().setTimeoutType(this, false);
            }
        }
    }
    public void officialReview() {
        synchronized (coreLock) {
            if (getOfficialReviewsRemaining() > 0) {
                getScoreBoardModel().setTimeoutType(this, true);
            }
        }
    }

    public int getScore() { return currentTeamJamModel.getTotalScore(); }
    public int getJamScore() { return currentTeamJamModel.getJamScore(); }
    public void setScore(int score) {
        synchronized (coreLock) {
            if (0 > score) {
                score = 0;
            }
            currentTeamJamModel.changeOsOffset(score - getScore());
        }
    }
    public void changeScore(int change) {
	synchronized (coreLock) {
	    currentTeamJamModel.getCurrentScoringTripModel().changePoints(change);
	}
    }
    public void addTrip() {
	synchronized (coreLock) {
	    currentTeamJamModel.addScoringTripModel();
	}
    }
    public void removeTrip() {
	synchronized (coreLock) {
	    scoreBoardModel.deleteScoringTripModel(currentTeamJamModel.getCurrentScoringTripModel());
	}
    }

    public boolean inTimeout() {
	Timeout t = scoreBoardModel.getCurrentTimeout();
	if (t == null || !t.isRunning()) {
	    return false;
	} else {
	    return (t.getOwner() == this && !t.isOfficialReview());
	}
    }
    public int getTimeoutsRemaining() { 
	 int number = scoreBoardModel.getSettings().getInt(SETTING_NUMBER_TIMEOUTS);
	 boolean perPeriod = scoreBoardModel.getSettings().getBoolean(SETTING_TIMEOUTS_PER_PERIOD);
	 for (Timeout t : timeouts) {
	     if (!t.isOfficialReview() && (!perPeriod || t.getPrecedingJam().getPeriod().isCurrent())) {
		 number--;
	     }
	 }
	 if (number < 0) { number = 0; }
	 return number;
    }
    public boolean inOfficialReview() {
	Timeout t = scoreBoardModel.getCurrentTimeout();
	if (t == null || !t.isRunning()) {
	    return false;
	} else {
	    return (t.getOwner() == this && t.isOfficialReview());
	}
    }
    public int getOfficialReviewsRemaining() { 
	 int max = scoreBoardModel.getSettings().getInt(SETTING_NUMBER_REVIEWS);
	 int number = max;
	 boolean perPeriod = scoreBoardModel.getSettings().getBoolean(SETTING_REVIEWS_PER_PERIOD);
	 for (Timeout t : timeouts) {
	     if (t.isOfficialReview() && (!perPeriod || t.getPrecedingJam().getPeriod().isCurrent())) {
		 number--;
	     }
	 }
	 if (number < max && retained_official_review) { number++; }
	 if (number < 0) { number = 0; }
	 return number;
   }
    public boolean retainedOfficialReview() { return retained_official_review; }
    public void setRetainedOfficialReview(boolean b) {
        synchronized (coreLock) {
            if (b==retained_official_review) {
                return;
            }
            Boolean last = new Boolean(retained_official_review);
            retained_official_review = b;
            scoreBoardChange(new ScoreBoardEvent(this, EVENT_RETAINED_OFFICIAL_REVIEW, new Boolean(b), last));
        }
    }
    public List<Timeout> getTimeouts() { return new ArrayList<Timeout>(timeouts); }
    public List<TimeoutModel> getTimeoutModels() { return timeouts; }
    public void addTimeoutModel(TimeoutModel timeout) {
	synchronized (coreLock) {
	    String event = timeout.isOfficialReview() ? EVENT_OFFICIAL_REVIEWS : EVENT_TIMEOUTS;
	    int last = timeout.isOfficialReview() ? getOfficialReviewsRemaining() : getTimeoutsRemaining();
	    timeouts.add(timeout);
	    int now = timeout.isOfficialReview() ? getOfficialReviewsRemaining() : getTimeoutsRemaining();
	    scoreBoardChange(new ScoreBoardEvent(this, event, now, last));
	}
    }
    public void removeTimeoutModel(TimeoutModel timeout) {
	synchronized (coreLock) {
	    String event = timeout.isOfficialReview() ? EVENT_OFFICIAL_REVIEWS : EVENT_TIMEOUTS;
	    int last = timeout.isOfficialReview() ? getOfficialReviewsRemaining() : getTimeoutsRemaining();
	    timeouts.remove(timeout);
	    int now = timeout.isOfficialReview() ? getOfficialReviewsRemaining() : getTimeoutsRemaining();
	    scoreBoardChange(new ScoreBoardEvent(this, event, now, last));
	}
    }

    public Skater getCurrentSkater(FloorPosition fp) {
	if (scoreBoardModel.isInJam() && currentTeamJamModel.getFieldingModel(fp) != null) {
	    return currentTeamJamModel.getFielding(fp).getSkater();
	} else if (!scoreBoardModel.isInJam() && nextTeamJamModel.getFielding(fp) != null) {
	    return nextTeamJamModel.getFielding(fp).getSkater();
	} else {
	    return null;
	}
    }
    public List<SkaterModel> getSkaterModels() {
        synchronized (coreLock) {
            Collections.sort(skaters, Comparators.SkaterComparator);
            return Collections.unmodifiableList(skaters);
        }
    }
    public List<Skater> getSkaters() {
        synchronized (coreLock) {
            ArrayList<Skater> s = new ArrayList<Skater>(skaters);
            Collections.sort(s, Comparators.SkaterComparator);
            return Collections.unmodifiableList(s);
        }
    }
    public void addSkaterModel(SkaterModel skater) {
        synchronized (coreLock) {
            skaters.add(skater);
            scoreBoardChange(new ScoreBoardEvent(this, EVENT_ADD_SKATER, skater, null));
        }
    }
    public void removeSkaterModel(SkaterModel skater) {
        synchronized (coreLock) {
            skaters.remove(skater);
            scoreBoardChange(new ScoreBoardEvent(this, EVENT_REMOVE_SKATER, skater, null));
        }
    }
    
    public boolean displayLead() {
	return (scoreBoardModel.isInJam() && isLead() && !isLost());
    }
    public boolean isLost() { return currentTeamJamModel.isLost(); }
    public boolean isLead() { return currentTeamJamModel.isLead(); }
    public boolean isCalloff() { return currentTeamJamModel.isCalloff(); }
    public boolean isInjury() { return currentTeamJamModel.isInjury(); }
    public boolean isStarPass() { return currentTeamJamModel.isStarPass(); }

    private ScoreBoardModel scoreBoardModel;

    private static Object coreLock = DefaultScoreBoardModel.getCoreLock();
    
    private TeamJamModel previousTeamJamModel;
    private TeamJamModel currentTeamJamModel;
    private TeamJamModel nextTeamJamModel;

    private String id;
    private String name;
    private String logo = DEFAULT_LOGO;
    private boolean retained_official_review = false;
    
    private List<TimeoutModel> timeouts = new ArrayList<TimeoutModel>();

    private Map<String,AlternateNameModel> alternateNames = new ConcurrentHashMap<String,AlternateNameModel>();

    private Map<String,ColorModel> colors = new ConcurrentHashMap<String,ColorModel>();

    private List<SkaterModel> skaters = new ArrayList<SkaterModel>();

    public static final String DEFAULT_NAME_PREFIX = "Team ";
    public static final String DEFAULT_LOGO = "";

    public class DefaultAlternateNameModel extends DefaultScoreBoardEventProvider implements AlternateNameModel {
        public DefaultAlternateNameModel(TeamModel t, String i, String n) {
            teamModel = t;
            id = i;
            name = n;
        }
        public String getId() { return id; }
        public String getName() { return name; }
        public void setName(String n) {
            synchronized (coreLock) {
                String last = name;
                name = n;
                scoreBoardChange(new ScoreBoardEvent(this, AlternateName.EVENT_NAME, name, last));
            }
        }

        public Team getTeam() { return getTeamModel(); }
        public TeamModel getTeamModel() { return teamModel; }

        public String getProviderName() { return "AlternateName"; }
        public Class<AlternateName> getProviderClass() { return AlternateName.class; }
        public String getProviderId() { return getId(); }

        protected TeamModel teamModel;
        protected String id;
        protected String name;
    }

    public class DefaultColorModel extends DefaultScoreBoardEventProvider implements ColorModel {
        public DefaultColorModel(TeamModel t, String i, String c) {
            teamModel = t;
            id = i;
            color = c;
        }
        public String getId() { return id; }
        public String getColor() { return color; }
        public void setColor(String c) {
            synchronized (coreLock) {
                String last = color;
                color = c;
                scoreBoardChange(new ScoreBoardEvent(this, Color.EVENT_COLOR, color, last));
            }
        }

        public Team getTeam() { return getTeamModel(); }
        public TeamModel getTeamModel() { return teamModel; }

        public String getProviderName() { return "Color"; }
        public Class<Color> getProviderClass() { return Color.class; }
        public String getProviderId() { return getId(); }

        protected TeamModel teamModel;
        protected String id;
        protected String color;
    }
}
