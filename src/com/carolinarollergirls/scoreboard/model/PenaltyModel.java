package com.carolinarollergirls.scoreboard.model;

import java.util.Set;

import com.carolinarollergirls.scoreboard.view.Penalty;

public interface PenaltyModel extends Penalty {
    public SkaterModel getSkaterModel();
    public void setSkaterModel(SkaterModel s);

    public JamModel getJamModel();
    public void setJamModel(JamModel jam);

    public void unlink();
    
    public void setNumber(int num);
    public void setCode(String code);
    public void setExpulsion(boolean exp);
    public void forceServed(boolean force);

    public Set<BoxTripModel> getBoxTripModels(); 
    public void addBoxTrip(BoxTripModel b);
    public void removeBoxTrip(BoxTripModel b);
}
