package com.carolinarollergirls.scoreboard.view;

import java.util.List;
import com.carolinarollergirls.scoreboard.event.ScoreBoardEventProvider;

public interface Period extends ScoreBoardEventProvider {
    public String getId();
    public int getNumber();
    
    public Period getPrevious();
    public Period getNext();
    
    public boolean isRunning();
    public boolean isCurrent();

    public List<Timeout> getTimeouts();
    public Timeout getCurrentTimeout();
    
    public List<Jam> getJams();
    public Jam getCurrentJam();

    public long getDuration();
    public long getWalltimeStart();
    public long getWalltimeEnd();

    public static final String EVENT_NUMBER = "Number";
    public static final String EVENT_RUNNING = "Running";
    public static final String EVENT_CURRENT = "Current";
    public static final String EVENT_DURATION = "Duration";
    public static final String EVENT_WALLTIME_START = "WalltimeStart";
    public static final String EVENT_WALLTIME_END = "WalltimeEnd";
    public static final String EVENT_ADD_TIMEOUT = "AddTimeout";
    public static final String EVENT_REMOVE_TIMEOUT = "RemoveTimeout";
    public static final String EVENT_ADD_JAM = "AddJam";
    public static final String EVENT_REMOVE_JAM = "RemoveJam";
    public static final String EVENT_PREVIOUS = "Previous";
    public static final String EVENT_NEXT = "Next";
}
