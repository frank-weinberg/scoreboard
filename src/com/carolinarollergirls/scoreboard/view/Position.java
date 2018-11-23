package com.carolinarollergirls.scoreboard.view;

public enum Position {
    NOT_IN_GAME("NotInGame"),
    TEMPORARILY_OUT("TemporarilyOut"),
    STAFF("Staff"),
    BENCH("Bench"),
    JAMMER("Jammer"),
    PIVOT("Pivot"),
    BLOCKER("Blocker");
    
    Position(String str) {
	string = str;
    }
    
    public String toString() { return string; }
    public static Position fromString(String s) {
	for (Position p : Position.values()) {
	    if (p.toString().equals(s)) { return p; }
	}
	return null;
    }
    
    private String string;
}
