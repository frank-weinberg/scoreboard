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

import com.carolinarollergirls.scoreboard.view.ScoreBoard;
import com.carolinarollergirls.scoreboard.view.Timeout.TimeoutOwner;

public interface ScoreBoardModel extends ScoreBoard {
    public ScoreBoard getScoreBoard();

    /** Reset the entire ScoreBoard. */
    public void reset();

    public void setInOvertime(boolean inOvertime);
    public void startOvertime();

    public void setOfficialScore(boolean official);

    public void startJam();
    public void stopJamTO();

    public void timeout();
    public void setTimeoutType(TimeoutOwner team, boolean review);

    public void clockUndo(boolean replace);

    public void setRuleset(String id);
    public SettingsModel getSettingsModel();
    public FrontendSettingsModel getFrontendSettingsModel();

    public List<ClockModel> getClockModels();
    public ClockModel getClockModel(String id);

    public List<TeamModel> getTeamModels();
    public TeamModel getTeamModel(String id);

    public List<PeriodModel> getPeriodModels();
    public PeriodModel getPeriodModel(String id);
    public PeriodModel getCurrentPeriodModel();
    public void addPeriod(PeriodModel period);
    public void registerPeriod(PeriodModel period);
    public void removePeriod(PeriodModel period);

    public TimeoutModel getTimeoutModel(String id);
    public TimeoutModel getCurrentTimeoutModel();
    public void registerTimeoutModel(TimeoutModel timeout);
    public void deleteTimeoutModel(TimeoutModel timeout);
    
    public JamModel getJamModel(String id);
    public JamModel getCurrentJamModel();
    public void registerJamModel(JamModel jam);
    public void deleteJamModel(JamModel jam);
    
    public TeamJamModel getTeamJamModel(String id);
    public void registerTeamJamModel(TeamJamModel teamJam);
    public void deleteTeamJamModel(TeamJamModel teamJam);
    
    public ScoringTripModel getScoringTripModel(String id);
    public void registerScoringTripModel(ScoringTripModel trip);
    public void deleteScoringTripModel(ScoringTripModel trip);
    
    public FieldingModel getFieldingModel(String id);
    public void registerFieldingModel(FieldingModel fielding);
    public void deleteFieldingModel(FieldingModel fielding);
    
    public SkaterModel getSkaterModel(String id);
    public void registerSkaterModel(SkaterModel skater);
    public void deleteSkaterModel(SkaterModel skater);
    
    public PenaltyModel getPenaltyModel(String id);
    public void registerPenaltyModel(PenaltyModel penalty);
    public void deletePenaltyModel(PenaltyModel penalty);
    
    public BoxTripModel getBoxTripModel(String id);
    public void registerBoxTripModel(BoxTripModel boxTrip);
    public void deleteBoxTripModel(BoxTripModel boxTrip);
}

