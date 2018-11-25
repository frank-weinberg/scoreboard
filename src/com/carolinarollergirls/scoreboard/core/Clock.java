package com.carolinarollergirls.scoreboard.core;
/**
 * Copyright (C) 2008-2012 Mr Temper <MrTemper@CarolinaRollergirls.com>
 *
 * This file is part of the Carolina Rollergirls (CRG) ScoreBoard.
 * The CRG ScoreBoard is licensed under either the GNU General Public
 * License version 3 (or later), or the Apache License 2.0, at your option.
 * See the file COPYING for details.
 */

import com.carolinarollergirls.scoreboard.event.ScoreBoardEventProvider;

public interface Clock extends ScoreBoardEventProvider {
    public ScoreBoard getScoreBoard();

    public void reset();

    public ClockSnapshot snapshot();
    public void restoreSnapshot(ClockSnapshot s);

    public void start();
    public void stop();
    public void startNext();

    public String getId();

    public String getName();
    public void setName(String name);

    public int getNumber();
    public void setNumber(int n);
    public void changeNumber(int n);
    public int getMinimumNumber();
    public void setMinimumNumber(int n);
    public void changeMinimumNumber(int n);
    public int getMaximumNumber();
    public void setMaximumNumber(int n);
    public void changeMaximumNumber(int n);

    /**
     *
     * @return The time displayed on the clock (in ms)
     */
    public long getTime();
    public void setTime(long ms);
    /**
     * Add time to the clock.
     * @param ms The amount of change (can be negative)
     */
    public void changeTime(long ms);
    /**
     *
     * @return The clock's maximum time minus the time displayed on the clock (in ms)
     */
    public long getInvertedTime();
    /**
     *
     * @return The time the clock has run (in ms). This is either the time or inverted time depending on the direction of the clock
     */
    public long getTimeElapsed();
    /**
     * Change the clock in the direction it is running.
     * This function is the inverse of changeTime(), when the clock counts down.
     * @param ms The amount of change (can be negative)
     */
    public void elapseTime(long ms);
    public void resetTime();
    /**
     *
     * @return The time until the clock reaches its maximum or zero (in ms). This is the inverse of getTimeElapsed.
     */
    public long getTimeRemaining();
    public long getMinimumTime();
    public void setMinimumTime(long ms);
    public void changeMinimumTime(long ms);
    public long getMaximumTime();
    public void setMaximumTime(long ms);
    public void changeMaximumTime(long ms);
    public boolean isTimeAtStart(long time);
    public boolean isTimeAtStart();
    public boolean isTimeAtEnd(long time);
    public boolean isTimeAtEnd();

    public boolean isRunning();

    public boolean isCountDirectionDown();
    public void setCountDirectionDown(boolean down);

    public static interface ClockSnapshot {
        public String getId();
        public int getNumber();
        public long getTime();
        public boolean isRunning();
    }

    public static final String SETTING_SYNC = "ScoreBoard.Clock.Sync";

    public static final String ID_PERIOD = "Period";
    public static final String ID_JAM = "Jam";
    public static final String ID_LINEUP = "Lineup";
    public static final String ID_TIMEOUT = "Timeout";
    public static final String ID_INTERMISSION = "Intermission";

    public static final String EVENT_NAME = "Name";
    public static final String EVENT_NUMBER = "Number";
    public static final String EVENT_MINIMUM_NUMBER = "MinimumNumber";
    public static final String EVENT_MAXIMUM_NUMBER = "MaximumNumber";
    public static final String EVENT_TIME = "Time";
    public static final String EVENT_INVERTED_TIME = "InvertedTime";
    public static final String EVENT_MINIMUM_TIME = "MinimumTime";
    public static final String EVENT_MAXIMUM_TIME = "MaximumTime";
    public static final String EVENT_DIRECTION = "Direction";
    public static final String EVENT_RUNNING = "Running";
}
