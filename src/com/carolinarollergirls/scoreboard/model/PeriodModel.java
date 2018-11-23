package com.carolinarollergirls.scoreboard.model;

import java.util.List;
import com.carolinarollergirls.scoreboard.model.JamModel.JamSnapshotModel;
import com.carolinarollergirls.scoreboard.model.TimeoutModel.TimeoutSnapshotModel;
import com.carolinarollergirls.scoreboard.view.Period;

public interface PeriodModel extends Period {
    public ScoreBoardModel getScoreBoardModel();
    
    public PeriodSnapshotModel snapshot();
    public void restoreSnapshot(PeriodSnapshotModel snapshot);
    
    public void unlink();
    
    public PeriodModel getPrevious();
    public void setPrevious(PeriodModel p);
    public PeriodModel getNext();
    public void setNext(PeriodModel n);
    public void updateNumber();
    
    public void setRunning(boolean r);
    public void setCurrent(boolean c);

    public List<TimeoutModel> getTimeoutModels();
    public TimeoutModel getCurrentTimeoutModel();
    public void removeTimeout(TimeoutModel t);
    public void addTimeout(TimeoutModel t);
    public void addTimeout();

    public List<JamModel> getJamModels();
    public JamModel getCurrentJamModel();
    public JamModel getFirstJamModel();
    public JamModel getLastJamModel();
    public void removeJam(JamModel j);
    public void addJam(JamModel j);
    public void addJam();

    public void startNextJam();
    public void stopCurrentJam();

    public static interface PeriodSnapshotModel {
	public boolean isCurrent();
	public boolean isRunning();
	public long getWalltimeEnd();
	public JamModel getCurrentJam();
	public JamSnapshotModel getCurrentJamSnapshot();
	public JamSnapshotModel getNextJamSnapshot();
	public TimeoutModel getCurrentTimeout();
	public TimeoutSnapshotModel getCurrentTimeoutSnapshot();
    }
}
