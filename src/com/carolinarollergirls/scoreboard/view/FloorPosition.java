package com.carolinarollergirls.scoreboard.view;

public enum FloorPosition {
    JAMMER(Position.JAMMER, "Jammer"),
    PIVOT(Position.PIVOT, "Pivot"),
    BLOCKER1(Position.BLOCKER, "Blocker1"),
    BLOCKER2(Position.BLOCKER, "Blocker2"),
    BLOCKER3(Position.BLOCKER, "Blocker3");
    
    FloorPosition(Position pos, String str) {
	position = pos;
	string = str;
    }
    
    public Position toPosition() { return position; }
    public Position toPosition(boolean afterSP) {
	if (!afterSP) {
	    return position; 
	} else {
	    switch (position) {
	    case JAMMER:
		return Position.BLOCKER;
	    case PIVOT:
		return Position.JAMMER;
	    default:
		return position;
	    }		
	}
    }
    
    public String toString() { return string; }
    public static FloorPosition fromString(String s) {
	for (FloorPosition fp : FloorPosition.values()) {
	    if (fp.toString().equals(s)) { return fp; }
	}
	return null;
    }
    
    private Position position;
    private String string;
}
