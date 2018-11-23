package com.carolinarollergirls.scoreboard.view;
/**
 * Copyright (C) 2008-2012 Mr Temper <MrTemper@CarolinaRollergirls.com>
 *
 * This file is part of the Carolina Rollergirls (CRG) ScoreBoard.
 * The CRG ScoreBoard is licensed under either the GNU General Public
 * License version 3 (or later), or the Apache License 2.0, at your option.
 * See the file COPYING for details.
 */

import java.util.List;

import com.carolinarollergirls.scoreboard.event.ScoreBoardEventProvider;
import com.carolinarollergirls.scoreboard.view.Timeout.TimeoutOwner;

public interface Team extends ScoreBoardEventProvider, TimeoutOwner {
    public ScoreBoard getScoreBoard();

    public String getId();

    public String getName();

    public List<AlternateName> getAlternateNames();
    public AlternateName getAlternateName(String id);

    public List<Color> getColors();
    public Color getColor(String id);

    public String getLogo();

    public TeamJam getPreviousTeamJam();
    public TeamJam getCurrentTeamJam();
    public TeamJam getNextTeamJam();
    
    public int getScore();
    public int getJamScore();

    public List<Timeout> getTimeouts();
    public int getTimeoutsRemaining();
    public int getOfficialReviewsRemaining();

    public boolean inTimeout();
    public boolean inOfficialReview();
    public boolean retainedOfficialReview();
    
    public List<Skater> getSkaters();
    public Skater getCurrentSkater(FloorPosition fp);

    public boolean displayLead();
    public boolean isLost();
    public boolean isLead();
    public boolean isCalloff();
    public boolean isInjury();
    public boolean isStarPass();

    public static final String ID_1 = "1";
    public static final String ID_2 = "2";

    public static final String SETTING_NUMBER_TIMEOUTS = "Rule.Team.Timeouts";
    public static final String SETTING_TIMEOUTS_PER_PERIOD = "Rule.Team.TimeoutsPer";
    public static final String SETTING_NUMBER_REVIEWS = "Rule.Team.OfficialReviews";
    public static final String SETTING_REVIEWS_PER_PERIOD = "Rule.Team.OfficialReviewsPer";

    public static final String EVENT_NAME = "Name";
    public static final String EVENT_LOGO = "Logo";
    public static final String EVENT_SCORE = "Score";
    public static final String EVENT_JAM_SCORE = "JamScore";
    public static final String EVENT_TIMEOUTS = "Timeouts";
    public static final String EVENT_OFFICIAL_REVIEWS = "OfficialReviews";
    public static final String EVENT_IN_TIMEOUT = "InTimeout";
    public static final String EVENT_IN_OFFICIAL_REVIEW = "InOfficialReview";
    public static final String EVENT_RETAINED_OFFICIAL_REVIEW = "RetainedOfficialReview";
    public static final String EVENT_ADD_SKATER = "AddSkater";
    public static final String EVENT_REMOVE_SKATER = "RemoveSkater";
    public static final String EVENT_DISPLAY_LEAD = "DisplayLead";
    public static final String EVENT_LOST = "Lost";
    public static final String EVENT_LEAD = "Lead";
    public static final String EVENT_CALLOFF = "Calloff";
    public static final String EVENT_INJURY = "Injury";
    public static final String EVENT_STAR_PASS = "StarPass";
    public static final String EVENT_ADD_ALTERNATE_NAME = "AddAlternateName";
    public static final String EVENT_REMOVE_ALTERNATE_NAME = "RemoveAlternateName";
    public static final String EVENT_ADD_COLOR = "AddColor";
    public static final String EVENT_REMOVE_COLOR = "RemoveColor";
    public static final String EVENT_ADD_TRIP = "AddTrip";
    public static final String EVENT_REMOVE_TRIP = "RemoveTrip";

    public static interface AlternateName extends ScoreBoardEventProvider {
        public String getId();
        public String getName();

        public Team getTeam();

        public static final String EVENT_NAME = "Name";

        public static final String ID_OPERATOR = "operator";
        public static final String ID_MOBILE = "mobile";
        public static final String ID_OVERLAY = "overlay";
        public static final String ID_TWITTER = "twitter";
    };

    public static interface Color extends ScoreBoardEventProvider {
        public String getId();
        public String getColor();

        public Team getTeam();

        public static final String EVENT_COLOR = "Color";
    }
}
