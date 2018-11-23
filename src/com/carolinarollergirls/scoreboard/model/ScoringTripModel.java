package com.carolinarollergirls.scoreboard.model;

import com.carolinarollergirls.scoreboard.view.ScoringTrip;

public interface ScoringTripModel extends ScoringTrip {
    public TeamJamModel getTeamJamModel();
    
    public void unlink();
    
    public ScoringTripModel getPrevious();
    public void setPrevious(ScoringTripModel p);
    public ScoringTripModel getNext();
    public void setNext(ScoringTripModel n);
    
    public void updateNumber();

    public void setAfterSP(boolean sp);
    
    public void setPoints(int p);
    public void changePoints(int change);
    
    public void start();
    public void stop();
}
