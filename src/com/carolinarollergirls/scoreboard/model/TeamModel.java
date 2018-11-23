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

import com.carolinarollergirls.scoreboard.view.Team;

public interface TeamModel extends Team {
    public ScoreBoardModel getScoreBoardModel();

    public Team getTeam();

    public void reset();

    public void setName(String name);

    public void startJam();
    public void stopJam();

    public List<AlternateNameModel> getAlternateNameModels();
    public AlternateNameModel getAlternateNameModel(String id);
    public void setAlternateNameModel(String id, String name);
    public void removeAlternateNameModel(String id);
    public void removeAlternateNameModels();

    public List<ColorModel> getColorModels();
    public ColorModel getColorModel(String id);
    public void setColorModel(String id, String color);
    public void removeColorModel(String id);
    public void removeColorModels();

    public void setLogo(String logo);

    public TeamJamModel getCurrentTeamJamModel();
    public TeamJamModel getPreviousTeamJamModel();
    public TeamJamModel getNextTeamJamModel();
    public void removeTeamJamModel(TeamJamModel t);
    public void updateTeamJamModels();
    
    public void timeout();
    public void officialReview();
    
    public void setScore(int score);
    public void changeScore(int change);
    public void addTrip();
    public void removeTrip();

    public List<TimeoutModel> getTimeoutModels();
    public void addTimeoutModel(TimeoutModel timeout);
    public void removeTimeoutModel(TimeoutModel timeout);
    public void setRetainedOfficialReview(boolean retained_official_review);

    public List<SkaterModel> getSkaterModels();
    public void addSkaterModel(SkaterModel skater);
    public void removeSkaterModel(SkaterModel skater);

    public static interface AlternateNameModel extends AlternateName {
        public void setName(String n);

        public TeamModel getTeamModel();
    }

    public static interface ColorModel extends Color {
        public void setColor(String c);

        public TeamModel getTeamModel();
    }
}
