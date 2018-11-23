package com.carolinarollergirls.scoreboard.defaults;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.carolinarollergirls.scoreboard.event.DefaultScoreBoardEventProvider;
import com.carolinarollergirls.scoreboard.event.ScoreBoardEvent;
import com.carolinarollergirls.scoreboard.model.BoxTripModel;
import com.carolinarollergirls.scoreboard.model.FieldingModel;
import com.carolinarollergirls.scoreboard.model.ScoreBoardModel;
import com.carolinarollergirls.scoreboard.model.SkaterModel;
import com.carolinarollergirls.scoreboard.model.TeamJamModel;
import com.carolinarollergirls.scoreboard.view.BoxTrip;
import com.carolinarollergirls.scoreboard.view.Fielding;
import com.carolinarollergirls.scoreboard.view.FloorPosition;
import com.carolinarollergirls.scoreboard.view.Skater;
import com.carolinarollergirls.scoreboard.view.TeamJam;

public class DefaultFieldingModel extends DefaultScoreBoardEventProvider implements FieldingModel {
    public DefaultFieldingModel(TeamJamModel teamJam, SkaterModel skater, FloorPosition pos) {
	this(UUID.randomUUID().toString(), teamJam, skater, pos);
    }
    public DefaultFieldingModel(String id, TeamJamModel teamJam, SkaterModel skater, FloorPosition pos) {
	this.id = id;
	scoreBoardModel = teamJam.getScoreBoardModel();
	teamJamModel = teamJam;
	skaterModel = skater;
	position = pos;
	skaterModel.addFieldingModel(this);
	scoreBoardModel.registerFieldingModel(this);
    }

    public String getProviderName() { return "Fielding"; }
    public Class<Fielding> getProviderClass() { return Fielding.class; }
    public String getProviderId() { return getId(); }

    public ScoreBoardModel getScoreBoardModel() { return scoreBoardModel; }
    
    public String getId() { return id; }
    public String toString() { return getId(); }
    
    public void unlink() {
	synchronized (coreLock) {
	    requestBatchStart();
	    teamJamModel.removeFieldingModel(this);
	    skaterModel.removeFieldingModel(this);
	    for (BoxTripModel b : new ArrayList<BoxTripModel>(boxTripModels)) {
		b.removeFielding(this);
	    }
	    requestBatchEnd();
	}
    }
    
    public TeamJam getTeamJam() { return teamJamModel; }
    public TeamJamModel getTeamJamModel() { return teamJamModel; }
    
    public Skater getSkater() { return skaterModel; }
    public SkaterModel getSkaterModel() { return skaterModel; }
    public void setSkaterModel(SkaterModel s) {
	synchronized (coreLock) {
	    if (s == skaterModel) { return; }
	    requestBatchStart();
	    SkaterModel last = skaterModel;
	    skaterModel.removeFieldingModel(this);
	    if (isInBox()) {
		scoreBoardChange(new ScoreBoardEvent(skaterModel, 
			Skater.EVENT_PENALTY_BOX, false, true));
	    }
	    skaterModel = s;
	    skaterModel.addFieldingModel(this);
	    if (isInBox()) {
		scoreBoardChange(new ScoreBoardEvent(skaterModel, 
			Skater.EVENT_PENALTY_BOX, true, false));
	    }
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_SKATER, skaterModel, last));
	    requestBatchEnd();
	}
    }

    public FloorPosition getFloorPosition() { return position; }
    public void setFloorPosition(FloorPosition p) {
	synchronized (coreLock) {
	    if (p == position) { return; }
	    requestBatchStart();
	    FloorPosition last = position;
	    teamJamModel.removeFieldingModel(this);
	    position = p;
	    teamJamModel.addFieldingModel(this);
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_POSITION, position, last));
	    requestBatchEnd();
	}
    }
    
    public boolean gotSat3Jams() { return satFor3; }
    public void sit3Jams(boolean s) {
	synchronized (coreLock) {
	    if (s == satFor3) { return; }
	    requestBatchStart();
	    boolean last = satFor3;
	    satFor3 = s;
	    skaterModel.changeSitFor3(this);
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_3_JAMS, satFor3, last));
	    requestBatchEnd();
	}
    }
    
    public List<BoxTrip> getBoxTrips() { return new ArrayList<BoxTrip>(boxTripModels); }
    public List<BoxTripModel> getBoxTripModels() { return boxTripModels; }
    public BoxTrip getCurrentBoxTrip() { return getCurrentBoxTripModel(); }
    public BoxTripModel getCurrentBoxTripModel() {
	for (BoxTripModel bt : boxTripModels) {
	    if (bt.isCurrent()) { return bt; }
	}
	return null;
    }
    public boolean isInBox() {
	boolean box = false;
	for (BoxTrip bt : boxTripModels) {
	    if (bt.isCurrent()) { box = true; }
	}
	return box;
    }
    public boolean hasBoxTrips() { return boxTripModels.size() > 0; }
    public void addBoxTripModel(BoxTripModel b) {
	synchronized (coreLock) {
	    if (boxTripModels.contains(b)) { return; }
	    requestBatchStart();
	    boxTripModels.add(b);
	    b.addFielding(this);
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_ADD_BOX_TRIP, b, null));
	    requestBatchEnd();
	}
    }
    public void removeBoxTripModel(BoxTripModel b) {
	synchronized (coreLock) {
	    if (!boxTripModels.contains(b)) { return; }
	    requestBatchStart();
	    boxTripModels.remove(b);
	    b.removeFielding(this);
	    scoreBoardChange(new ScoreBoardEvent(this, EVENT_REMOVE_BOX_TRIP, b, null));
	    requestBatchEnd();
	}
    }

    private ScoreBoardModel scoreBoardModel;
    
    private static Object coreLock = DefaultScoreBoardModel.getCoreLock();
    
    private String id;

    private TeamJamModel teamJamModel;
    private SkaterModel skaterModel;
    private FloorPosition position;
    private boolean satFor3;
    private List<BoxTripModel> boxTripModels = new ArrayList<BoxTripModel>();
}
