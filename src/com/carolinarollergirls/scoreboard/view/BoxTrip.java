package com.carolinarollergirls.scoreboard.view;

import java.util.Set;
import java.util.SortedSet;

import com.carolinarollergirls.scoreboard.event.ScoreBoardEventProvider;

public interface BoxTrip extends ScoreBoardEventProvider {
    public String getId();

    public boolean isCurrent();
    public boolean startedBetweenJams();
    public boolean endedBetweenJams();
    
    public boolean startedAfterStarPass();
    public boolean endedAfterStarPass();

    public Set<Penalty> getPenalties();

    public SortedSet<Fielding> getFieldings();
    public String getNotation(Fielding f, boolean afterStarPass);
    public String getAnnotation(Fielding f);
    
    public static final String EVENT_CURRENT = "Current";
    public static final String EVENT_START_BETWEEN = "StartBetween";
    public static final String EVENT_END_BETWEEN = "EndBetween";
    public static final String EVENT_START_AFTER_SP = "StartAfterSP";
    public static final String EVENT_END_AFTER_SP = "EndAfterSP";
    public static final String EVENT_ADD_FIELDING = "AddFielding";
    public static final String EVENT_REMOVE_FIELDING = "RemoveFielding";
    public static final String EVENT_ADD_PENALTY = "AddPenalty";
    public static final String EVENT_REMOVE_PENALTY = "RemovePenalty";
    public static final String EVENT_ANNOTATION = "Annotation";
}
