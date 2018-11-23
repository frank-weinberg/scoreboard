package com.carolinarollergirls.scoreboard.utils;

import java.util.Comparator;

import com.carolinarollergirls.scoreboard.view.Fielding;
import com.carolinarollergirls.scoreboard.view.Jam;
import com.carolinarollergirls.scoreboard.view.Penalty;
import com.carolinarollergirls.scoreboard.view.Period;
import com.carolinarollergirls.scoreboard.view.ScoringTrip;
import com.carolinarollergirls.scoreboard.view.Skater;
import com.carolinarollergirls.scoreboard.view.TeamJam;
import com.carolinarollergirls.scoreboard.view.Timeout;

public class Comparators {
    public static Comparator<Period> PeriodComparator = new Comparator<Period>() {
        public int compare(Period p1, Period p2) {
            if (p2 == null) {
                return 1;
            }
            if (p1 == null) {
                return -1;
            }
            int n1 = p1.getNumber();
            int n2 = p2.getNumber();
            return n1 - n2;
        }
    };

    public static Comparator<Timeout> TimeoutComparator = new Comparator<Timeout>() {
        public int compare(Timeout t1, Timeout t2) {
            if (t2 == null) {
                return 1;
            }
            if (t1 == null) {
                return -1;
            }
            return (int)(t1.getWalltimeStart() - t2.getWalltimeStart());
        }
    };

    public static Comparator<Jam> JamComparator = new Comparator<Jam>() {
        public int compare(Jam j1, Jam j2) {
            if (j2 == null) {
                return 1;
            }
            if (j1 == null) {
                return -1;
            }
            if (j1.getPeriod() == j2.getPeriod()) {
                int n1 = j1.getNumber();
                int n2 = j2.getNumber();
                return n1 - n2;
            } else {
        	return PeriodComparator.compare(j1.getPeriod(), j2.getPeriod());
            }
        }
    };

    public static Comparator<TeamJam> TeamJamComparator = new Comparator<TeamJam>() {
        public int compare(TeamJam j1, TeamJam j2) {
            if (j2 == null) {
                return 1;
            }
            if (j1 == null) {
                return -1;
            }
            return JamComparator.compare(j1.getJam(), j2.getJam());
        }
    };

    public static Comparator<Fielding> FieldingComparator = new Comparator<Fielding>() {
        public int compare(Fielding f1, Fielding f2) {
            if (f2 == null) {
                return 1;
            }
            if (f1 == null) {
                return -1;
            }
            return TeamJamComparator.compare(f1.getTeamJam(), f2.getTeamJam());
        }
    };

    public static Comparator<ScoringTrip> ScoringTripComparator = new Comparator<ScoringTrip>() {
        public int compare(ScoringTrip st1, ScoringTrip st2) {
            if (st2 == null) {
                return 1;
            }
            if (st1 == null) {
                return -1;
            }
            if (st1.getTeamJam() == st2.getTeamJam()) {
                int n1 = st1.getNumber();
                int n2 = st2.getNumber();
                return n1 - n2;
            } else {
        	return TeamJamComparator.compare(st1.getTeamJam(), st2.getTeamJam());
            }
        }
    };

    public static Comparator<Skater> SkaterComparator = new Comparator<Skater>() {
        public int compare(Skater s1, Skater s2) {
            if (s2 == null) {
                return 1;
            }
            String n1 = s1.getNumber();
            String n2 = s2.getNumber();
            if (n1 == null) { return -1; }
            if (n2 == null) { return 1; }

            return n1.compareTo(n2);
        }
    };

    public static Comparator<Penalty> PenaltyComparator = new Comparator<Penalty>() {
        public int compare(Penalty p1, Penalty p2) {
            if (p2 == null) {
                return 1;
            }
            if (p1 == null) {
                return -1;
            }
            return JamComparator.compare(p1.getJam(), p2.getJam());
        }
    };
}
