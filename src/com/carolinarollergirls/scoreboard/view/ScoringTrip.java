package com.carolinarollergirls.scoreboard.view;

import com.carolinarollergirls.scoreboard.event.ScoreBoardEventProvider;

public interface ScoringTrip extends ScoreBoardEventProvider {
    public String getId();
    
    public TeamJam getTeamJam();

    public ScoringTrip getPrevious();
    public ScoringTrip getNext();

    public boolean isAfterSP();
    
    public int getPoints();
    
    public int getNumber();
    
    public long getDuration();
    public long getJamClockElapsedStart();
    public long getJamClockElapsedEnd();
    public long getWalltimeStart();
    public long getWalltimeEnd();
    
    public static final String EVENT_POINTS = "Points";
    public static final String EVENT_NUMBER = "Number";
    public static final String EVENT_AFTER_SP = "AfterSP";
    public static final String EVENT_DURATION = "Duration";
    public static final String EVENT_JAM_CLOCK_START = "JamClockStart";
    public static final String EVENT_JAM_CLOCK_END = "JamClockEnd";
    public static final String EVENT_WALLTIME_START = "WalltimeStart";
    public static final String EVENT_WALLTIME_END = "WalltimeEnd";
    public static final String EVENT_PREVIOUS = "Previous";
    public static final String EVENT_NEXT = "Next";
}
