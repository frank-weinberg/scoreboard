package com.carolinarollergirls.scoreboard.view;

import java.util.List;

import com.carolinarollergirls.scoreboard.event.ScoreBoardEventProvider;

public interface Fielding extends ScoreBoardEventProvider {
    public String getId();

    public TeamJam getTeamJam();
    
    public Skater getSkater();

    public FloorPosition getFloorPosition();
    
    public boolean gotSat3Jams();

    public List<BoxTrip> getBoxTrips();
    public BoxTrip getCurrentBoxTrip();
    public boolean isInBox();
    public boolean hasBoxTrips();

    public static final String EVENT_SKATER = "Skater";
    public static final String EVENT_POSITION = "Position";
    public static final String EVENT_3_JAMS = "ThreeJams";
    public static final String EVENT_ADD_BOX_TRIP = "AddBoxTrip";
    public static final String EVENT_REMOVE_BOX_TRIP = "RemoveBoxTrip";
}
