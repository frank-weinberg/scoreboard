package com.carolinarollergirls.scoreboard.model;

import java.util.Collection;
import java.util.List;
import com.carolinarollergirls.scoreboard.view.Jam;
import com.carolinarollergirls.scoreboard.view.Team;

public interface JamModel extends Jam {
    public ScoreBoardModel getScoreBoardModel();
    
    public JamSnapshotModel snapshot();
    public void restoreSnapshot(JamSnapshotModel snapshot);
    
    public void unlink();

    public PeriodModel getPeriodModel();
    public void setPeriodModel(PeriodModel p);
    
    public JamModel getPrevious();
    public void setPrevious(JamModel p);
    public JamModel getNext();
    public void setNext(JamModel n);
    public void updateNumber();

    public void setCurrent(boolean c);

    public void start();
    public void stop();

    public void setInjury(boolean i);

    public List<TeamJamModel> getTeamJamModels();
    public TeamJamModel getTeamJamModel(Team t);

    public Collection<PenaltyModel> getPenaltyModels();
    public void addPenaltyModel(PenaltyModel p);
    public void removePenaltyModel(PenaltyModel p);

    public Collection<TimeoutModel> getTimeoutModelsAfter();
    public void addTimeoutAfter(TimeoutModel t);
    public void removeTimeoutAfter(TimeoutModel t);
    
    public static interface JamSnapshotModel {
	public boolean isCurrent();
	public long getDuration();
	public long getPeriodClockElapsedStart();
	public long getPeriodClockElapsedEnd();
	public long getWalltimeStart();
	public long getWalltimeEnd();
    }
}
