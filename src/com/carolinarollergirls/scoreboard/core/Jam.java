package com.carolinarollergirls.scoreboard.core;

import com.carolinarollergirls.scoreboard.event.NumberedScoreBoardEventProvider;
import com.carolinarollergirls.scoreboard.event.ScoreBoardEvent.AddRemoveProperty;
import com.carolinarollergirls.scoreboard.event.ScoreBoardEvent.CommandProperty;
import com.carolinarollergirls.scoreboard.event.ScoreBoardEvent.PermanentProperty;
import com.carolinarollergirls.scoreboard.event.ScoreBoardEvent.ValueWithId;
import com.carolinarollergirls.scoreboard.event.ScoreBoardEventProvider;

public interface Jam extends NumberedScoreBoardEventProvider<Jam> {
    public void setParent(ScoreBoardEventProvider p);

    public long getDuration();
    public long getPeriodClockElapsedStart();
    public long getPeriodClockElapsedEnd();
    public long getWalltimeStart();
    public long getWalltimeEnd();

    public TeamJam getTeamJam(String id);

    public void start();
    public void stop();

    public enum Value implements PermanentProperty {
        PERIOD_NUMBER(Integer.class, 0),
        STAR_PASS(Boolean.class, false), //true, if either team had an SP
        DURATION(Long.class, 0L),
        PERIOD_CLOCK_ELAPSED_START(Long.class, 0L),
        PERIOD_CLOCK_ELAPSED_END(Long.class, 0L),
        PERIOD_CLOCK_DISPLAY_END(Long.class, 0L),
        WALLTIME_START(Long.class, 0L),
        WALLTIME_END(Long.class, 0L);

        private Value(Class<?> t, Object dv) { type = t; defaultValue = dv; }
        private final Class<?> type;
        private final Object defaultValue;
        @Override
        public Class<?> getType() { return type; }
        @Override
        public Object getDefaultValue() { return defaultValue; }
    }
    public enum Child implements AddRemoveProperty {
        TEAM_JAM(TeamJam.class),
        PENALTY(Penalty.class),
        TIMEOUTS_AFTER(Timeout.class);

        private Child(Class<? extends ValueWithId> t) { type = t; }
        private final Class<? extends ValueWithId> type;
        @Override
        public Class<? extends ValueWithId> getType() { return type; }
    }
    public enum Command implements CommandProperty {
        DELETE,
        INSERT_BEFORE;
        
        @Override
        public Class<Boolean> getType() { return Boolean.class; }
    }
}
