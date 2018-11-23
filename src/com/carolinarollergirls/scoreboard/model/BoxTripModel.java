package com.carolinarollergirls.scoreboard.model;

import java.util.Set;
import java.util.SortedSet;

import com.carolinarollergirls.scoreboard.view.BoxTrip;

public interface BoxTripModel extends BoxTrip {
    public void unlink();
    
    public void end(boolean betweenJams, boolean afterStarPass);
    public void extend();
    
    public void setStartBetweenJams(boolean s);
    public void setEndBetweenJams(boolean e);
    
    public void setStartAfterStarPass(boolean s);
    public void setEndAfterStarPass(boolean e);
    
    public Set<PenaltyModel> getPenaltyModels();
    public void addPenaltyModel(PenaltyModel p);
    public void removePenaltyModel(PenaltyModel p);

    public SortedSet<FieldingModel> getFieldingModels();
    public void addFielding(FieldingModel f);
    public void removeFielding(FieldingModel f);

    public void addAnnotation(FieldingModel f, String a);
}
