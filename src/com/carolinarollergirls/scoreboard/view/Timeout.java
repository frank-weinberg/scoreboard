package com.carolinarollergirls.scoreboard.view;

import com.carolinarollergirls.scoreboard.event.ScoreBoardEventProvider;

public interface Timeout extends ScoreBoardEventProvider {
    public String getId();
    public Jam getPrecedingJam();
    
    public boolean isCurrent();
    public boolean isRunning();

    public long getDuration();
    public long getPeriodClockElapsedStart();
    public long getPeriodClockElapsedEnd();
    public long getWalltimeStart();
    public long getWalltimeEnd();

    public TimeoutOwner getOwner();
    public boolean isOfficialReview();

    public static final String EVENT_OWNER = "Owner";
    public static final String EVENT_REVIEW = "Review";
    public static final String EVENT_CURRENT = "Current";
    public static final String EVENT_RUNNING = "Running";
    public static final String EVENT_DURATION = "Duration";
    public static final String EVENT_PERIOD_CLOCK_START = "PeriodClockElapsedStart";
    public static final String EVENT_PERIOD_CLOCK_END = "PeriodClockElapsedEnd";
    public static final String EVENT_WALLTIME_START = "WalltimeStart";
    public static final String EVENT_WALLTIME_END = "WalltimeEnd";
    public static final String EVENT_PRECEDING_JAM = "PrecedingJam";
   
    public interface TimeoutOwner {
	public String getId();
	    
	public static final String OTO = "O";
	public static final String NONE = "";
    }
}
