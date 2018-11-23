package com.carolinarollergirls.scoreboard.model;

import java.util.List;

import com.carolinarollergirls.scoreboard.view.Fielding;
import com.carolinarollergirls.scoreboard.view.FloorPosition;

public interface FieldingModel extends Fielding {
    public ScoreBoardModel getScoreBoardModel();
    
    public void unlink();
    
    public TeamJamModel getTeamJamModel();
    
    public SkaterModel getSkaterModel();
    public void setSkaterModel(SkaterModel s);
    
    public void setFloorPosition(FloorPosition p);

    public void sit3Jams(boolean s);
    
    public List<BoxTripModel> getBoxTripModels();
    public BoxTripModel getCurrentBoxTripModel();
    public void addBoxTripModel(BoxTripModel b);
    public void removeBoxTripModel(BoxTripModel b);
}
