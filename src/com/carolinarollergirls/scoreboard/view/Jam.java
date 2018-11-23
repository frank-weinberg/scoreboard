package com.carolinarollergirls.scoreboard.view;

import java.util.List;
import com.carolinarollergirls.scoreboard.event.ScoreBoardEventProvider;

public interface Jam extends ScoreBoardEventProvider {
    public String getId();
    public Period getPeriod();
    public int getNumber();
    
    public Jam getPrevious();
    public Jam getNext();
    
    public boolean isCurrent();
    public boolean isRunning();

    public boolean getInjury();
    public long getDuration();
    public long getPeriodClockElapsedStart();
    public long getPeriodClockElapsedEnd();
    public long getWalltimeStart();
    public long getWalltimeEnd();

    public List<TeamJam> getTeamJams();
    public TeamJam getTeamJam(Team t);
    
    public List<Penalty> getPenalties();
    
    public List<Timeout> getTimeoutsAfter();
    
    public static final String EVENT_INJURY = "Injury";
    public static final String EVENT_NUMBER = "Number";
    public static final String EVENT_CURRENT = "Current";
    public static final String EVENT_RUNNING = "Running";
    public static final String EVENT_DURATION = "JamClockElapsedEnd";
    public static final String EVENT_PERIOD_CLOCK_START = "PeriodClockElapsedStart";
    public static final String EVENT_PERIOD_CLOCK_END = "PeriodClockElapsedEnd";
    public static final String EVENT_WALLTIME_START = "WalltimeStart";
    public static final String EVENT_WALLTIME_END = "WalltimeEnd";
    public static final String EVENT_ADD_PENALTY = "AddPenalty";
    public static final String EVENT_REMOVE_PENALTY = "RemovePenalty";
    public static final String EVENT_ADD_TIMEOUT = "AddTimeout";
    public static final String EVENT_REMOVE_TIMEOUT = "RemoveTimeout";
    public static final String EVENT_PREVIOUS = "Previous";
    public static final String EVENT_NEXT = "Next";
}
