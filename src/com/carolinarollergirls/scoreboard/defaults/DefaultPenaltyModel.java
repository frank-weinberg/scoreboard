package com.carolinarollergirls.scoreboard.defaults;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.carolinarollergirls.scoreboard.event.DefaultScoreBoardEventProvider;
import com.carolinarollergirls.scoreboard.event.ScoreBoardEvent;
import com.carolinarollergirls.scoreboard.model.BoxTripModel;
import com.carolinarollergirls.scoreboard.model.JamModel;
import com.carolinarollergirls.scoreboard.model.PenaltyModel;
import com.carolinarollergirls.scoreboard.model.SkaterModel;
import com.carolinarollergirls.scoreboard.view.BoxTrip;
import com.carolinarollergirls.scoreboard.view.Jam;
import com.carolinarollergirls.scoreboard.view.Penalty;
import com.carolinarollergirls.scoreboard.view.Skater;

public class DefaultPenaltyModel extends DefaultScoreBoardEventProvider implements PenaltyModel {
    public DefaultPenaltyModel(SkaterModel s, JamModel j, String c, boolean e) {
	this(UUID.randomUUID().toString(), s, j, c, e);
    }
    public DefaultPenaltyModel(String i, SkaterModel s, JamModel j, String c, boolean e) {
        id = i;
        skater = s;
        jam = j;
        if (jam != null) { jam.addPenaltyModel(this); }
        code = c;
        expulsion = e;
        skater.getScoreBoardModel().registerPenaltyModel(this);
    }
    public String getId() { return id; }
    public String toString() { return getId(); }

    public String getProviderName() { return "Penalty"; }
    public Class<Penalty> getProviderClass() { return Penalty.class; }
    public String getProviderId() { return getId(); }
    
    public Skater getSkater() { return skater; }
    public SkaterModel getSkaterModel() { return skater; }
    public void setSkaterModel(SkaterModel s) {
	synchronized (coreLock) {
	    if (s == skater) { return; }
	    requestBatchStart();
	    SkaterModel last = skater;
	    skater.removePenaltyModel(this);
	    skater = s;
	    skater.addPenaltyModel(this);
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_SKATER, skater, last));
	    requestBatchEnd();
	}
    }
    
    public Jam getJam() { return jam; }
    public JamModel getJamModel() { return jam; }
    public void setJamModel(JamModel j) {
	synchronized (coreLock) {
	    if (j == jam) { return; }
	    requestBatchStart();
	    jam.removePenaltyModel(this);
	    jam = j;
	    jam.addPenaltyModel(this);
	    skater.updatePenalties();
	    requestBatchEnd();
	}
    }
    
    public void unlink() {
	synchronized (coreLock) {
	    requestBatchStart();
	    skater.removePenaltyModel(this);
	    jam.removePenaltyModel(this);
	    for (BoxTripModel b : new ArrayList<BoxTripModel>(boxTrips)) {
		b.removePenaltyModel(this);
	    }
	    requestBatchEnd();
	}
    }
    
    public int getNumber() { return number; }
    public void setNumber(int num) {
	synchronized (coreLock) {
	    if (num == number) { return; }
	    requestBatchStart();
	    int last = number;
	    number = num;
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_NUMBER, number, last));
	    requestBatchEnd();
	}
    }
    
    public String getCode() { return code; }
    public void setCode(String c) {
	synchronized (coreLock) {
	    if (c == code) { return; }
	    requestBatchStart();
	    String last = code;
	    code = c;
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_CODE, code, last));
	    requestBatchEnd();
	}
    }
    
    public boolean isExpulsion() { return expulsion; }
    public void setExpulsion(boolean e) {
	synchronized (coreLock) {
	    if (e == expulsion) { return; }
	    requestBatchStart();
	    boolean last = expulsion;
	    expulsion = e;
	    skater.updatePenalties();
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_EXPULSION, expulsion, last));
	    requestBatchEnd();
	}
    }
    
    public boolean isServed() {
	return forceServed || boxTrips.size() > 0;
    }
    public void forceServed(boolean force) {
	synchronized (coreLock) {
	    if (force == isServed()) { return; }
	    requestBatchStart();
	    boolean last = isServed();
	    forceServed = force;
	    if (!force)  {
		for (BoxTripModel btm : boxTrips) {
		    btm.removePenaltyModel(this);
		}
		boxTrips.clear();
	    }
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_SERVED, isServed(), last));
	    requestBatchEnd();
	}
    }

    public Set<BoxTrip> getBoxTrips() { return new HashSet<BoxTrip>(boxTrips); } 
    public Set<BoxTripModel> getBoxTripModels() { return boxTrips; } 
    public void addBoxTrip(BoxTripModel b) {
	synchronized (coreLock) {
	    if (boxTrips.contains(b)) { return; }
	    requestBatchStart();
	    boxTrips.add(b);
	    b.addPenaltyModel(this);
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_ADD_BOX_TRIP, b, null));
	    requestBatchEnd();
	}
    }
    public void removeBoxTrip(BoxTripModel b) {
	synchronized (coreLock) {
	    if (!boxTrips.contains(b)) { return; }
	    requestBatchStart();
	    boxTrips.remove(b);
	    b.removePenaltyModel(this);
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_REMOVE_BOX_TRIP, b, null));
	    requestBatchEnd();
	}
    }

    private static Object coreLock = DefaultScoreBoardModel.getCoreLock();
    
    private String id;
    private SkaterModel skater;
    private JamModel jam;
    private int number;
    private String code;
    private boolean expulsion;
    private boolean forceServed = false;
    private Set<BoxTripModel> boxTrips = new HashSet<BoxTripModel>();
}
