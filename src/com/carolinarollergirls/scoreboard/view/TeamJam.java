package com.carolinarollergirls.scoreboard.view;

import java.util.List;

import com.carolinarollergirls.scoreboard.event.ScoreBoardEventProvider;

public interface TeamJam extends ScoreBoardEventProvider {
    public String getId();
    public Jam getJam();
    public Team getTeam();

    public ScoringTrip getCurrentScoringTrip();
    public List<ScoringTrip> getScoringTrips();

    public int getOsOffset();
    public String getOsOffsetReason();
    public int getJamScore();
    public int getTotalScore();

    public boolean isLost();
    public boolean isLead();
    public boolean isCalloff();
    public boolean isInjury();
    public boolean isStarPass();
    public ScoringTrip getStarPassTrip();

    public Fielding getFielding(FloorPosition p);
    public boolean hasNoPivot();
    
    public static final String EVENT_OS_OFFSET = "OsOffset";
    public static final String EVENT_OS_OFFSET_REASON = "OsOffsetReason";
    public static final String EVENT_SCORE = "TotalScore";
    public static final String EVENT_JAM_SCORE = "JamScore";
    public static final String EVENT_LOST = "Lost";
    public static final String EVENT_LEAD = "Lead";
    public static final String EVENT_CALLOFF = "Calloff";
    public static final String EVENT_STAR_PASS_TRIP = "StarPassTrip";
    public static final String EVENT_STAR_PASS_TRIP_NUMBER = "StarPassTripNumber";
    public static final String EVENT_NO_PIVOT = "NoPivot";
    public static final String EVENT_ADD_FIELDING = "AddFielding";
    public static final String EVENT_REMOVE_FIELDING = "RemoveFielding";
    public static final String EVENT_ADD_SCORING_TRIP = "AddScoringTrip";
    public static final String EVENT_REMOVE_SCORING_TRIP = "RemoveScoringTrip";
}
