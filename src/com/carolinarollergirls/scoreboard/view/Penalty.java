package com.carolinarollergirls.scoreboard.view;

import java.util.Set;

import com.carolinarollergirls.scoreboard.event.ScoreBoardEventProvider;

public interface Penalty extends ScoreBoardEventProvider {
    public String getId();
    public Skater getSkater();
    public Jam getJam();
    public int getNumber();
    public String getCode();
    public boolean isExpulsion();
    public boolean isServed();
    public Set<BoxTrip> getBoxTrips(); 

    public static final String SETTING_FO_LIMIT = "Rule.Penalties.NumberToFoulout";

    public static final String EVENT_CODE = "Code";
    public static final String EVENT_JAM = "Jam";
    public static final String EVENT_PERIOD = "Period";
    public static final String EVENT_SKATER = "Skater";
    public static final String EVENT_NUMBER = "Number";
    public static final String EVENT_EXPULSION = "Expulsion";
    public static final String EVENT_SERVED = "Served";
    public static final String EVENT_ADD_BOX_TRIP = "AddBoxTrip";
    public static final String EVENT_REMOVE_BOX_TRIP = "RemoveBoxTrip";
}
