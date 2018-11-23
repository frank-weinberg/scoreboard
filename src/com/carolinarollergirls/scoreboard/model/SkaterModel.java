package com.carolinarollergirls.scoreboard.model;
/**
 * Copyright (C) 2008-2012 Mr Temper <MrTemper@CarolinaRollergirls.com>
 *
 * This file is part of the Carolina Rollergirls (CRG) ScoreBoard.
 * The CRG ScoreBoard is licensed under either the GNU General Public
 * License version 3 (or later), or the Apache License 2.0, at your option.
 * See the file COPYING for details.
 */

import java.util.List;

import com.carolinarollergirls.scoreboard.view.Position;
import com.carolinarollergirls.scoreboard.view.Skater;
import com.carolinarollergirls.scoreboard.view.TeamJam;

public interface SkaterModel extends Skater {
    public ScoreBoardModel getScoreBoardModel();

    public TeamModel getTeamModel();
    public Skater getSkater();
    
    public void unlink();

    public void setName(String id);
    public void setNumber(String number);
    public void setBasePosition(Position position);
    public void setFlags(String flags);
    
    public void bench();
    
    public void changeSitFor3(FieldingModel f);

    public void startBoxTrip(boolean betweenJams, boolean afterStarPass);
    public void finishBoxTrip(boolean betweenJams, boolean afterStarPass);

    public FieldingModel getCurrentFieldingModel();
    public FieldingModel getFieldingModel(TeamJam jam);
    public void addFieldingModel(FieldingModel f);
    public void removeFieldingModel(FieldingModel f);

    public List<PenaltyModel> getPenaltyModels();
    public List<PenaltyModel> getUnservedPenalties();
    public PenaltyModel getFOEXPPenaltyModel();
    public void addPenaltyModel(PenaltyModel penalty);
    public void removePenaltyModel(PenaltyModel penalty);
    public void updatePenalties();
}
