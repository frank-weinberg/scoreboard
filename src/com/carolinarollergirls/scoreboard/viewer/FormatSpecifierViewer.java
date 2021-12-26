package com.carolinarollergirls.scoreboard.viewer;
/**
 * Copyright (C) 2008-2012 Mr Temper <MrTemper@CarolinaRollergirls.com>
 *
 * This file is part of the Carolina Rollergirls (CRG) ScoreBoard.
 * The CRG ScoreBoard is licensed under either the GNU General Public
 * License version 3 (or later), or the Apache License 2.0, at your option.
 * See the file COPYING for details.
 */

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.carolinarollergirls.scoreboard.core.interfaces.Clock;
import com.carolinarollergirls.scoreboard.core.interfaces.CurrentClock;
import com.carolinarollergirls.scoreboard.core.interfaces.CurrentGame;
import com.carolinarollergirls.scoreboard.core.interfaces.CurrentTeam;
import com.carolinarollergirls.scoreboard.core.interfaces.FloorPosition;
import com.carolinarollergirls.scoreboard.core.interfaces.Game;
import com.carolinarollergirls.scoreboard.core.interfaces.Position;
import com.carolinarollergirls.scoreboard.core.interfaces.ScoreBoard;
import com.carolinarollergirls.scoreboard.core.interfaces.Skater;
import com.carolinarollergirls.scoreboard.core.interfaces.Team;
import com.carolinarollergirls.scoreboard.core.interfaces.TimeoutOwner;
import com.carolinarollergirls.scoreboard.event.Property;
import com.carolinarollergirls.scoreboard.event.ScoreBoardCondition;
import com.carolinarollergirls.scoreboard.event.ScoreBoardEvent;
import com.carolinarollergirls.scoreboard.event.ScoreBoardEventProvider;

public class FormatSpecifierViewer {
    public FormatSpecifierViewer(ScoreBoard sb) { setScoreBoard(sb); }

    public Map<String, String> getFormatSpecifierDescriptions() {
        Map<String, String> m = new LinkedHashMap<>();
        Iterator<String> keys = scoreBoardValues.keySet().iterator();
        while (keys.hasNext()) {
            String k = keys.next();
            m.put(k, scoreBoardValues.get(k).getDescription());
        }
        return m;
    }

    public void setScoreBoard(ScoreBoard sb) {
        scoreBoard = sb;
        setupScoreBoardValues();
    }

    public String parse(String format) {
        StringBuffer buffer = new StringBuffer();
        Matcher m = formatPattern.matcher(format);
        while (m.find()) { m.appendReplacement(buffer, getFormatSpecifierValue(m.group())); }
        m.appendTail(buffer);
        return buffer.toString();
    }

    public boolean checkCondition(String format, ScoreBoardEvent<?> event) {
        boolean triggerCondition = true;
        Matcher m = conditionPattern.matcher(format);
        if (!m.find()) { throw new IllegalArgumentException("No conditions in format : " + format); }
        do {
            String specifier = m.group(1);
            String comparator = m.group(2);
            String targetValue = m.group(3);
            if (null == comparator || null == targetValue) { continue; }
            String value = scoreBoardValues.get(specifier).getValue();
            if (triggerCondition) {
                triggerCondition = false;
                // If current trigger event value == previous value after processing
                // (e.g. conversion to min:sec) then ignore, to prevent multiple consecutive
                // identical triggers
                if (value.equals(scoreBoardValues.get(specifier).getPreviousValue(event.getPreviousValue()))) {
                    return false;
                }
            }
            try {
                if (!checkConditionValue(value, comparator, targetValue)) { return false; }
            } catch (IllegalArgumentException iaE) { return false; }
        } while (m.find());
        return true;
    }

    protected boolean checkConditionValue(String value, String comparator, String target)
        throws IllegalArgumentException {
        // Check to see if we're talking about times, and if so, de-time them.
        if (comparator.contains("<") || comparator.contains(">") || comparator.contains("%")) {
            // We're doing maths. Are they times? Times have colons.
            if (value.contains(":")) {
                String[] s = value.split(":");
                if (s.length == 2) try {
                        value = String.valueOf(Long.parseLong(s[0]) * 60 + Long.parseLong(s[1]));
                    } catch (NumberFormatException nfE) {}
            }
            if (target.contains(":")) {
                String[] s = target.split(":");
                if (s.length == 2) try {
                        target = String.valueOf(Long.parseLong(s[0]) * 60 + Long.parseLong(s[1]));
                    } catch (NumberFormatException nfE) {}
            }
        }
        if ("=".equals(comparator)) {
            return value.equals(target);
        } else if ("!=".equals(comparator)) {
            return !value.equals(target);
        } else if ("<".equals(comparator)) {
            try {
                return (Long.parseLong(value) < Long.parseLong(target));
            } catch (NumberFormatException nfE) {}
            try {
                return (Double.parseDouble(value) < Double.parseDouble(target));
            } catch (NumberFormatException nfE) {}
            return (value.compareTo(target) < 0);
        } else if ("<=".equals(comparator)) {
            try {
                return (Long.parseLong(value) <= Long.parseLong(target));
            } catch (NumberFormatException nfE) {}
            try {
                return (Double.parseDouble(value) <= Double.parseDouble(target));
            } catch (NumberFormatException nfE) {}
            return (value.compareTo(target) <= 0);
        } else if (">".equals(comparator)) {
            try {
                return (Long.parseLong(value) > Long.parseLong(target));
            } catch (NumberFormatException nfE) {}
            try {
                return (Double.parseDouble(value) > Double.parseDouble(target));
            } catch (NumberFormatException nfE) {}
            return (value.compareTo(target) > 0);
        } else if (">=".equals(comparator)) {
            try {
                return (Long.parseLong(value) >= Long.parseLong(target));
            } catch (NumberFormatException nfE) {}
            try {
                return (Double.parseDouble(value) >= Double.parseDouble(target));
            } catch (NumberFormatException nfE) {}
            return (value.compareTo(target) >= 0);
        } else if ("%".equals(comparator)) {
            try {
                return (0 == (Long.parseLong(value) % Long.parseLong(target)));
            } catch (NumberFormatException nfE) {
                return false;
            } catch (ArithmeticException aE) { // most likely target == 0, % by 0 is invalid
                return false;
            }
        }
        throw new IllegalArgumentException("Invalid comparator : " + comparator);
    }

    public ScoreBoardCondition<?> getScoreBoardCondition(String format) throws IllegalArgumentException {
        Matcher m = eventPattern.matcher(format);
        if (!m.find()) { throw new IllegalArgumentException("No valid event specified"); }
        return getFormatSpecifierScoreBoardCondition(m.group(1));
    }

    protected ScoreBoardCondition<?> getFormatSpecifierScoreBoardCondition(String formatSpecifier)
        throws IllegalArgumentException {
        ScoreBoardValue<?> value = scoreBoardValues.get(formatSpecifier);
        if (null == value) { throw new IllegalArgumentException("Not a valid format specifier : " + formatSpecifier); }
        return value.getScoreBoardCondition();
    }

    protected String getFormatSpecifierValue(String formatSpecifier) {
        ScoreBoardValue<?> value = scoreBoardValues.get(formatSpecifier);
        if (null == value) { return formatSpecifier; }
        return value.getValue();
    }

    public ScoreBoardValue<?> getFormatSpecifierScoreBoardValue(String formatSpecifier) {
        return scoreBoardValues.get(formatSpecifier);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void setupScoreBoardValues() {
        new ScoreBoardValue("%sbto", "ScoreBoard Timeout Owner", getGame(), Game.TIMEOUT_OWNER) {
            @Override
            public String getValue() {
                TimeoutOwner to = getGame().get(Game.TIMEOUT_OWNER);
                return to == null ? "" : to.getId();
            }
        };
        new ScoreBoardValue("%sbip", "ScoreBoard Is In Period", getGame(), Game.IN_PERIOD) {
            @Override
            public String getValue() {
                return String.valueOf(getGame().get(Game.IN_PERIOD));
            }
        };
        new ScoreBoardValue("%sbio", "ScoreBoard Is In Overtime", getGame(), Game.IN_OVERTIME) {
            @Override
            public String getValue() {
                return String.valueOf(getGame().get(Game.IN_OVERTIME));
            }
        };
        new ScoreBoardValue("%sbos", "ScoreBoard Is Score Official", getGame(), Game.OFFICIAL_SCORE) {
            @Override
            public String getValue() {
                return String.valueOf(getGame().get(Game.OFFICIAL_SCORE));
            }
        };

        setupTeamValues("1", Team.ID_1);
        setupTeamValues("2", Team.ID_2);
        setupClockValues("p", Clock.ID_PERIOD);
        setupClockValues("j", Clock.ID_JAM);
        setupClockValues("l", Clock.ID_LINEUP);
        setupClockValues("t", Clock.ID_TIMEOUT);
        setupClockValues("i", Clock.ID_INTERMISSION);

        StringBuffer patternBuffer = new StringBuffer();
        Iterator<String> patterns = scoreBoardValues.keySet().iterator();
        while (patterns.hasNext()) { patternBuffer.append(patterns.next() + "|"); }
        String specifiersRegex = patternBuffer.toString().replaceAll("[|]$", "");
        formatPattern = Pattern.compile(specifiersRegex);
        eventPattern = Pattern.compile("^\\s*(" + specifiersRegex + ")");
        conditionPattern = Pattern.compile("(" + specifiersRegex + ")(?:(" + comparatorRegex + ")(\\S+))?");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void setupTeamValues(String t, final String id) {
        new ScoreBoardValue("%t" + t + "n", "Team " + t + " Name", getTeam(id), Team.DISPLAY_NAME) {
            @Override
            public String getValue() {
                return getTeam(id).get(Team.DISPLAY_NAME);
            }
        };
        new ScoreBoardValue("%t" + t + "Nt", "Team " + t + " Twitter Name", getTeam(id), Team.ALTERNATE_NAME) {
            @Override
            public String getValue() {
                try {
                    return getTeam(id).get(Team.ALTERNATE_NAME, Team.AlternateNameId.TWITTER.toString()).getValue();
                } catch (NullPointerException npE) { return getTeam(id).get(Team.INITIALS); }
            }
        };
        new ScoreBoardValue("%t" + t + "s", "Team " + t + " Score", getTeam(id), Team.SCORE) {
            @Override
            public String getValue() {
                return String.valueOf(getTeam(id).get(Team.SCORE));
            }
        };
        new ScoreBoardValue("%t" + t + "t", "Team " + t + " Timeouts", getTeam(id), Team.TIMEOUTS) {
            @Override
            public String getValue() {
                return String.valueOf(getTeam(id).get(Team.TIMEOUTS));
            }
        };
        new ScoreBoardValue("%t" + t + "or", "Team " + t + " Official Reviews", getTeam(id), Team.OFFICIAL_REVIEWS) {
            @Override
            public String getValue() {
                return String.valueOf(getTeam(id).get(Team.OFFICIAL_REVIEWS));
            }
        };
        new ScoreBoardValue("%t" + t + "l", "Team " + t + " is Lead Jammer", getTeam(id), Team.DISPLAY_LEAD) {
            @Override
            public String getValue() {
                return String.valueOf(getTeam(id).get(Team.DISPLAY_LEAD));
            }
        };
        new ScoreBoardValue("%t" + t + "jn", "Team " + t + " Jammer Name",
                            getTeam(id).getPosition(FloorPosition.JAMMER), Position.SKATER) {
            @Override
            public String getValue() {
                return getSkaterName(getPositionSkater(id, FloorPosition.JAMMER));
            }
            @Override
            public String getPreviousValue(Object o) {
                return getSkaterName((Skater) o);
            }
        };
        new ScoreBoardValue("%t" + t + "jN", "Team " + t + " Jammer Number",
                            getTeam(id).getPosition(FloorPosition.JAMMER), Position.SKATER) {
            @Override
            public String getValue() {
                return getSkaterNumber(getPositionSkater(id, FloorPosition.JAMMER));
            }
            @Override
            public String getPreviousValue(Object o) {
                return getSkaterNumber((Skater) o);
            }
        };
        new ScoreBoardValue("%t" + t + "pn", "Team " + t + " Pivot Name", getTeam(id).getPosition(FloorPosition.PIVOT),
                            Position.SKATER) {
            @Override
            public String getValue() {
                return getSkaterName(getPositionSkater(id, FloorPosition.PIVOT));
            }
            @Override
            public String getPreviousValue(Object o) {
                return getSkaterName((Skater) o);
            }
        };
        new ScoreBoardValue("%t" + t + "pN", "Team " + t + " Pivot Number",
                            getTeam(id).getPosition(FloorPosition.PIVOT), Position.SKATER) {
            @Override
            public String getValue() {
                return getSkaterNumber(getPositionSkater(id, FloorPosition.PIVOT));
            }
            @Override
            public String getPreviousValue(Object o) {
                return getSkaterNumber((Skater) o);
            }
        };
        new ScoreBoardValue("%t" + t + "b1n", "Team " + t + " Blocker1 Name",
                            getTeam(id).getPosition(FloorPosition.BLOCKER1), Position.SKATER) {
            @Override
            public String getValue() {
                return getSkaterName(getPositionSkater(id, FloorPosition.BLOCKER1));
            }
            @Override
            public String getPreviousValue(Object o) {
                return getSkaterName((Skater) o);
            }
        };
        new ScoreBoardValue("%t" + t + "b1N", "Team " + t + " Blocker1 Number",
                            getTeam(id).getPosition(FloorPosition.BLOCKER1), Position.SKATER) {
            @Override
            public String getValue() {
                return getSkaterNumber(getPositionSkater(id, FloorPosition.BLOCKER1));
            }
            @Override
            public String getPreviousValue(Object o) {
                return getSkaterNumber((Skater) o);
            }
        };
        new ScoreBoardValue("%t" + t + "b2n", "Team " + t + " Blocker2 Name",
                            getTeam(id).getPosition(FloorPosition.BLOCKER2), Position.SKATER) {
            @Override
            public String getValue() {
                return getSkaterName(getPositionSkater(id, FloorPosition.BLOCKER2));
            }
            @Override
            public String getPreviousValue(Object o) {
                return getSkaterName((Skater) o);
            }
        };
        new ScoreBoardValue("%t" + t + "b2N", "Team " + t + " Blocker2 Number",
                            getTeam(id).getPosition(FloorPosition.BLOCKER2), Position.SKATER) {
            @Override
            public String getValue() {
                return getSkaterNumber(getPositionSkater(id, FloorPosition.BLOCKER2));
            }
            @Override
            public String getPreviousValue(Object o) {
                return getSkaterNumber((Skater) o);
            }
        };
        new ScoreBoardValue("%t" + t + "b3n", "Team " + t + " Blocker3 Name",
                            getTeam(id).getPosition(FloorPosition.BLOCKER3), Position.SKATER) {
            @Override
            public String getValue() {
                return getSkaterName(getPositionSkater(id, FloorPosition.BLOCKER3));
            }
            @Override
            public String getPreviousValue(Object o) {
                return getSkaterName((Skater) o);
            }
        };
        new ScoreBoardValue("%t" + t + "b3N", "Team " + t + " Blocker3 Number",
                            getTeam(id).getPosition(FloorPosition.BLOCKER3), Position.SKATER) {
            @Override
            public String getValue() {
                return getSkaterNumber(getPositionSkater(id, FloorPosition.BLOCKER3));
            }
            @Override
            public String getPreviousValue(Object o) {
                return getSkaterNumber((Skater) o);
            }
        };
    }

    protected CurrentTeam getTeam(String id) { return getGame().get(CurrentGame.TEAM, id); }

    protected Skater getPositionSkater(String id, FloorPosition position) {
        return getTeam(id).getPosition(position).get(Position.SKATER);
    }

    protected String getSkaterName(Skater s) { return (null == s ? NO_SKATER_NAME_VALUE : s.getName()); }
    protected String getSkaterNumber(Skater s) { return (null == s ? NO_SKATER_NUMBER_VALUE : s.getRosterNumber()); }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void setupClockValues(String c, final String id) {
        new ScoreBoardValue("%c" + c + "n", "Clock " + id + " Name", getClock(id), Clock.NAME) {
            @Override
            public String getValue() {
                return getClock(id).get(Clock.NAME);
            }
        };
        new ScoreBoardValue("%c" + c + "N", "Clock " + id + " Number", getClock(id), Clock.NUMBER) {
            @Override
            public String getValue() {
                return String.valueOf(getClock(id).get(Clock.NUMBER));
            }
        };
        new ScoreBoardValue("%c" + c + "r", "Clock " + id + " is Running", getClock(id), Clock.RUNNING) {
            @Override
            public String getValue() {
                return String.valueOf(getClock(id).get(Clock.RUNNING));
            }
        };
        new ScoreBoardValue("%c" + c + "ts", "Clock " + id + " Time (seconds)", getClock(id), Clock.TIME) {
            @Override
            public String getValue() {
                return getClockSecs(id);
            }
            @Override
            public String getPreviousValue(Object o) {
                return getClockSecs(((Long) o), getClock(id).get(Clock.DIRECTION));
            }
        };
        new ScoreBoardValue("%c" + c + "tms", "Clock " + id + " Time (min:sec)", getClock(id), Clock.TIME) {
            @Override
            public String getValue() {
                return getClockMinSecs(id);
            }
            @Override
            public String getPreviousValue(Object o) {
                return getClockMinSecs(((Long) o), getClock(id).get(Clock.DIRECTION));
            }
        };
    }

    protected CurrentClock getClock(String id) { return getGame().get(CurrentGame.CLOCK, id); }

    protected String getClockSecs(String id) {
        return getClockSecs(getClock(id).get(Clock.TIME), getClock(id).get(Clock.DIRECTION));
    }
    protected String getClockSecs(long time, boolean roundUp) {
        long roundedTime = time / 1000;
        if (roundUp && time % 1000 != 0) { roundedTime++; }
        return String.valueOf(roundedTime);
    }
    protected String getClockMinSecs(String id) {
        return getClockMinSecs(getClock(id).get(Clock.TIME), getClock(id).get(Clock.DIRECTION));
    }
    protected String getClockMinSecs(long time, boolean roundUp) {
        long roundedTime = time / 1000;
        if (roundUp && time % 1000 != 0) { roundedTime++; }
        String min = String.valueOf(roundedTime / 60);
        String sec = String.valueOf(roundedTime % 60);
        if (sec.length() == 1) { sec = "0" + sec; }
        return min + ":" + sec;
    }

    protected CurrentGame getGame() { return scoreBoard.getCurrentGame(); }

    protected ScoreBoard scoreBoard = null;

    protected Pattern formatPattern;
    protected Pattern eventPattern;
    protected Pattern conditionPattern;

    protected String comparatorRegex = "=|!=|<|<=|>|>=|%";

    protected Map<String, ScoreBoardValue<?>> scoreBoardValues = new LinkedHashMap<>();

    public abstract class ScoreBoardValue<T> {
        protected ScoreBoardValue(String f, String d, ScoreBoardEventProvider p, Property<T> prop) {
            format = f;
            description = d;
            scoreBoardCondition = new ScoreBoardCondition<>(p, prop);
            scoreBoardValues.put(format, this);
        }
        protected ScoreBoardValue(String f, String d, Class<? extends ScoreBoardEventProvider> c, String i,
                                  Property<T> prop) {
            format = f;
            description = d;
            scoreBoardCondition = new ScoreBoardCondition<>(c, i, prop);
            scoreBoardValues.put(format, this);
        }
        public abstract String getValue();
        public String getPreviousValue(Object value) { return String.valueOf(value); }
        public String getDescription() { return description; }
        public ScoreBoardCondition<T> getScoreBoardCondition() { return scoreBoardCondition; }

        protected String format;
        protected String description;
        protected ScoreBoardCondition<T> scoreBoardCondition;
    }

    public static final String NO_SKATER_NAME_VALUE = "";
    public static final String NO_SKATER_NUMBER_VALUE = "";
}
