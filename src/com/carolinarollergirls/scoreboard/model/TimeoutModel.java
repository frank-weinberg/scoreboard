package com.carolinarollergirls.scoreboard.model;

import com.carolinarollergirls.scoreboard.view.Timeout;

public interface TimeoutModel extends Timeout {
    public TimeoutSnapshotModel snapshot();
    public void restoreSnapshot(TimeoutSnapshotModel snapshot);

    public JamModel getPrecedingJamModel();
    public void setPrecedingJamModel(JamModel j);
    
    public void unlink();
    
    public void setCurrent(boolean c);

    public void stop();

    public void setOwner(TimeoutOwner o);
    public void setOfficialReview(boolean o);

    public static interface TimeoutSnapshotModel {
	public long getDuration();
	public long getPeriodClockElapsedEnd();
	public long getWalltimeEnd();
    }
}
