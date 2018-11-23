package com.carolinarollergirls.scoreboard.model;

import java.util.List;

import com.carolinarollergirls.scoreboard.view.FloorPosition;
import com.carolinarollergirls.scoreboard.view.Position;
import com.carolinarollergirls.scoreboard.view.TeamJam;

public interface TeamJamModel extends TeamJam {
    public ScoreBoardModel getScoreBoardModel();
    
    public JamModel getJamModel();
    public TeamModel getTeamModel();

    public void unlink();

    public TeamJamModel getPrevious();
    public void setPrevious(TeamJamModel p);
    public TeamJamModel getNext();
    public void setNext(TeamJamModel n);

    public ScoringTripModel getCurrentScoringTripModel();
    public List<ScoringTripModel> getScoringTripModels();
    public void addScoringTripModel();
    public void addScoringTripModel(ScoringTripModel t);
    public void removeScoringTripModel(ScoringTripModel t);
    
    public void start();

    public void setOsOffset(int offset);
    public void changeOsOffset(int change);
    public void setOsOffsetReason(String reason);
    public void recalculateScores();

    public void setLost(boolean l);
    public void setLead(boolean l);
    public void setCalloff(boolean c);
    public void setInjury(boolean i);
    public void setStarPassTrip(ScoringTripModel trip);

    public FieldingModel getFieldingModel(FloorPosition p);
    public void removeFieldingModel(FieldingModel f);
    public void addFieldingModel(FieldingModel f);
    public void fieldSkater(SkaterModel s, Position p);
    
    public void setNoPivot(boolean np);
}
