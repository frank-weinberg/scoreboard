package com.carolinarollergirls.scoreboard.json;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.carolinarollergirls.scoreboard.ScoreBoardManager;
import com.carolinarollergirls.scoreboard.utils.ScoreBoardClock;
import com.carolinarollergirls.scoreboard.defaults.DefaultPenaltyModel;
import com.carolinarollergirls.scoreboard.defaults.DefaultScoreBoardModel;
import com.carolinarollergirls.scoreboard.defaults.DefaultSkaterModel;
import com.carolinarollergirls.scoreboard.jetty.JettyServletScoreBoardController;
import com.carolinarollergirls.scoreboard.model.PenaltyModel;
import com.carolinarollergirls.scoreboard.model.ScoringTripModel;
import com.carolinarollergirls.scoreboard.model.SkaterModel;
import com.carolinarollergirls.scoreboard.view.Position;
import com.carolinarollergirls.scoreboard.view.Team;
import com.carolinarollergirls.scoreboard.view.Timeout.TimeoutOwner;

public class ScoreBoardJSONListenerTests {

    private DefaultScoreBoardModel sbm;
    private JSONStateManager jsm;

    private Map<String, Object> state;
    private JSONStateListener jsonListener = new JSONStateListener() {
        @Override
        public void sendUpdates(Map<String, Object> s, Set<String> changed) {
            state = s;
        }
    };

    @Before
    public void setUp() throws Exception {
        ScoreBoardClock.getInstance().stop();
        ScoreBoardManager.setPropertyOverride(JettyServletScoreBoardController.class.getName() + ".html.dir", "html");
        sbm = new DefaultScoreBoardModel();

        jsm = new JSONStateManager();
        new ScoreBoardJSONListener(sbm, jsm);
        jsm.register(jsonListener);
    }

    @After
    public void tearDown() throws Exception {
        // Make sure events are still flowing through the ScoreBoardJSONListener.
        sbm.getFrontendSettingsModel().set("teardownTest", "foo");
        advance(0);
        assertEquals("foo", state.get("ScoreBoard.FrontendSettings.teardownTest"));
        ScoreBoardClock.getInstance().start(false);
    }

    private void advance(long time_ms) {
        ScoreBoardClock.getInstance().advance(time_ms);
        jsm.waitForSent();
    }

    @Test
    public void testScoreBoardEvents() {
        assertEquals("00000000-0000-0000-0000-000000000000", state.get("ScoreBoard.Ruleset"));
        assertEquals(false, state.get("ScoreBoard.InPeriod"));
        assertEquals(false, state.get("ScoreBoard.InOvertime"));
        assertEquals(false, state.get("ScoreBoard.OfficialScore"));
        assertEquals(TimeoutOwner.NONE, state.get("ScoreBoard.TimeoutOwner"));
        assertEquals(false, state.get("ScoreBoard.OfficialReview"));

        sbm.getCurrentPeriodModel().setRunning(true);
        advance(0);
        assertEquals(true, state.get("ScoreBoard.InPeriod"));

        sbm.setInOvertime(true);
        advance(0);
        assertEquals(true, state.get("ScoreBoard.InOvertime"));

        sbm.setOfficialScore(true);
        advance(0);
        assertEquals(true, state.get("ScoreBoard.OfficialScore"));

        sbm.setTimeoutType(sbm.getTeam(Team.ID_1), true);
        advance(0);
        assertEquals(Team.ID_1, state.get("ScoreBoard.TimeoutOwner"));
        assertEquals(true, state.get("ScoreBoard.OfficialReview"));
    }

    @Test
    public void testTeamEvents() {
        sbm.startJam();
        advance(0);

        sbm.getTeamModel("1").changeScore(5);
        advance(0);
        assertEquals(5, state.get("ScoreBoard.Team(1).Score"));
        assertEquals(5, state.get("ScoreBoard.Team(1).JamScore"));

        ScoringTripModel trip = sbm.getTeamModel("1").getCurrentTeamJamModel().getCurrentScoringTripModel();
        sbm.getTeamModel("1").getCurrentTeamJamModel().setStarPassTrip(trip);
        advance(0);
        assertEquals(1, state.get("ScoreBoard.Period(1).Jam(1).Team(1).StarPassTripNumber"));
        assertEquals(true, state.get("ScoreBoard.Team(1).StarPass"));

        sbm.getTeamModel("1").getCurrentTeamJamModel().setLost(true);
        sbm.getTeamModel("1").getCurrentTeamJamModel().setLead(true);
        sbm.getTeamModel("1").getCurrentTeamJamModel().setCalloff(true);
        sbm.getTeamModel("1").getCurrentTeamJamModel().setInjury(true);
        advance(0);
        assertEquals(true, state.get("ScoreBoard.Team(1).Lost"));
        assertEquals(true, state.get("ScoreBoard.Team(1).Lead"));
        assertEquals(true, state.get("ScoreBoard.Team(1).Calloff"));
        assertEquals(true, state.get("ScoreBoard.Team(1).Injury"));
        assertEquals(true, state.get("ScoreBoard.Team(2).Injury"));

        sbm.getTeamModel("1").timeout();
        sbm.getTeamModel("1").setRetainedOfficialReview(true);
        advance(0);
        assertEquals(2, state.get("ScoreBoard.Team(1).Timeouts"));
        assertEquals(1, state.get("ScoreBoard.Team(1).OfficialReviews"));
        assertEquals(true, state.get("ScoreBoard.Team(1).RetainedOfficialReview"));
        assertEquals(false, state.get("ScoreBoard.Team(1).InOfficialReview"));
        assertEquals(true, state.get("ScoreBoard.Team(1).InTimeout"));

        sbm.getTeamModel("1").officialReview();
        advance(0);
        assertEquals(true, state.get("ScoreBoard.Team(1).InOfficialReview"));
        assertEquals(false, state.get("ScoreBoard.Team(1).InTimeout"));

        sbm.getTeamModel("1").setName("ATeam");
        sbm.getTeamModel("1").setLogo("ATeamLogo");
        advance(0);
        assertEquals("ATeam", state.get("ScoreBoard.Team(1).Name"));
        assertEquals("ATeamLogo", state.get("ScoreBoard.Team(1).Logo"));

        sbm.getTeamModel("1").setAlternateNameModel("overlay", "AT");
        advance(0);
        assertEquals("AT", state.get("ScoreBoard.Team(1).AlternateName(overlay)"));

        sbm.getTeamModel("1").setAlternateNameModel("overlay", "AT");
        advance(0);
        assertEquals("AT", state.get("ScoreBoard.Team(1).AlternateName(overlay)"));
        sbm.getTeamModel("1").removeAlternateNameModel("overlay");
        advance(0);
        assertEquals(null, state.get("ScoreBoard.Team(1).AlternateName(overlay)"));

        sbm.getTeamModel("1").setColorModel("overlay", "red");
        advance(0);
        assertEquals("red", state.get("ScoreBoard.Team(1).Color(overlay)"));
        sbm.getTeamModel("1").removeColorModel("overlay");
        advance(0);
        assertEquals(null, state.get("ScoreBoard.Team(1).Color(overlay)"));
    }

    @Test
    public void testSkaterAndPositionEvents() {
        sbm.startJam();
        advance(0);

        String id = "00000000-0000-0000-0000-000000000001";

        sbm.getTeamModel("1").addSkaterModel(new DefaultSkaterModel(sbm.getTeamModel("1"), id, "Uno", "01", ""));
        advance(0);
        assertEquals("Uno", state.get("ScoreBoard.Team(1).Skater(00000000-0000-0000-0000-000000000001).Name"));
        assertEquals("01", state.get("ScoreBoard.Team(1).Skater(00000000-0000-0000-0000-000000000001).Number"));
        assertEquals("", state.get("ScoreBoard.Team(1).Skater(00000000-0000-0000-0000-000000000001).Flags"));
        assertEquals("Bench", state.get("ScoreBoard.Team(1).Skater(00000000-0000-0000-0000-000000000001).Position"));
        assertEquals(false, state.get("ScoreBoard.Team(1).Skater(00000000-0000-0000-0000-000000000001).PenaltyBox"));

        sbm.getTeamModel("1").getCurrentTeamJamModel().fieldSkater(sbm.getSkaterModel(id), Position.JAMMER);
        sbm.getSkaterModel(id).startBoxTrip(false, false);
        advance(0);
        assertEquals("Jammer", state.get("ScoreBoard.Team(1).Skater(00000000-0000-0000-0000-000000000001).Position"));
        assertEquals(true, state.get("ScoreBoard.Team(1).Skater(00000000-0000-0000-0000-000000000001).PenaltyBox"));
        assertEquals("00000000-0000-0000-0000-000000000001", state.get("ScoreBoard.Team(1).Jammer"));

        sbm.deleteSkaterModel(sbm.getSkaterModel(id));
        advance(0);
        assertEquals(null, state.get("ScoreBoard.Team(1).Skater(00000000-0000-0000-0000-000000000001).Name"));
        assertEquals(null, state.get("ScoreBoard.Team(1).Skater(00000000-0000-0000-0000-000000000001).Number"));
        assertEquals(null, state.get("ScoreBoard.Team(1).Skater(00000000-0000-0000-0000-000000000001).Flags"));
        assertEquals(null, state.get("ScoreBoard.Team(1).Skater(00000000-0000-0000-0000-000000000001).Position"));
        assertEquals(null, state.get("ScoreBoard.Team(1).Skater(00000000-0000-0000-0000-000000000001).PenaltyBox"));
        assertEquals(null, state.get("ScoreBoard.Team(1).Jammer"));
    }

    @Test
    public void testPenaltyEvents() {
        String sid = "00000000-0000-0000-0000-000000000001";
        String pid = "00000000-0000-0000-0000-000000000002";
        sbm.startJam();

        SkaterModel skaterModel = new DefaultSkaterModel(sbm.getTeamModel("1"), sid, "Uno", "01", "");
        sbm.getTeamModel("1").addSkaterModel(skaterModel);
        PenaltyModel penaltyModel = new DefaultPenaltyModel(pid, skaterModel, sbm.getCurrentJamModel().getNext(), "X", false);
        skaterModel.addPenaltyModel(penaltyModel);
        advance(0);
        assertEquals(pid, state.get("ScoreBoard.Team(1).Skater(00000000-0000-0000-0000-000000000001).Penalty(1).Id"));
        assertEquals(1, state.get("ScoreBoard.Team(1).Skater(00000000-0000-0000-0000-000000000001).Penalty(1).Period"));
        assertEquals(2, state.get("ScoreBoard.Team(1).Skater(00000000-0000-0000-0000-000000000001).Penalty(1).Jam"));
        assertEquals("X", state.get("ScoreBoard.Team(1).Skater(00000000-0000-0000-0000-000000000001).Penalty(1).Code"));

        sbm.deletePenaltyModel(penaltyModel);
        advance(0);
        assertEquals(null, state.get("ScoreBoard.Team(1).Skater(00000000-0000-0000-0000-000000000001).Penalty(1).Id"));
        assertEquals(null, state.get("ScoreBoard.Team(1).Skater(00000000-0000-0000-0000-000000000001).Penalty(1).Period"));
        assertEquals(null, state.get("ScoreBoard.Team(1).Skater(00000000-0000-0000-0000-000000000001).Penalty(1).Jam"));
        assertEquals(null, state.get("ScoreBoard.Team(1).Skater(00000000-0000-0000-0000-000000000001).Penalty(1).Code"));

        penaltyModel = new DefaultPenaltyModel(pid, skaterModel, sbm.getCurrentJamModel().getNext(), "B", true);
        skaterModel.addPenaltyModel(penaltyModel);
        advance(0);
        assertEquals(pid, state.get("ScoreBoard.Team(1).Skater(00000000-0000-0000-0000-000000000001).PenaltyFOEXP"));

        sbm.deletePenaltyModel(penaltyModel);
        advance(0);
        assertEquals(null, state.get("ScoreBoard.Team(1).Skater(00000000-0000-0000-0000-000000000001).PenaltyFOEXP"));
    }

    @Test
    public void testStatsEvents() {
        String id = "00000000-0000-0000-0000-000000000001";

        SkaterModel skater = new DefaultSkaterModel(sbm.getTeamModel("1"), id, "Uno", "1", "");
        sbm.getTeamModel("1").addSkaterModel(skater);
        sbm.getTeamModel("1").getNextTeamJamModel().fieldSkater(skater, Position.JAMMER);
        sbm.startJam();
        advance(2000);

        assertEquals(0L, state.get("ScoreBoard.Period(1).Jam(1).PeriodClockElapsedStart"));
        assertEquals(0, state.get("ScoreBoard.Period(1).Jam(1).Team(1).JamScore"));
        assertEquals(0, state.get("ScoreBoard.Period(1).Jam(1).Team(1).TotalScore"));
        assertEquals(0, state.get("ScoreBoard.Period(1).Jam(1).Team(1).StarPassTripNumber"));
        assertEquals("00000000-0000-0000-0000-000000000001", state.get("ScoreBoard.Period(1).Jam(1).Team(1).Fielding(Jammer).Skater"));
        assertEquals(false, state.get("ScoreBoard.Period(1).Jam(1).Team(1).Fielding(Jammer).ThreeJams"));
        assertEquals("Jammer", state.get("ScoreBoard.Period(1).Jam(1).Team(1).Fielding(Jammer).Position"));

        sbm.getTeamModel("1").getCurrentTeamJamModel().fieldSkater(skater, Position.BENCH);
        advance(0);
        assertEquals(null, state.get("ScoreBoard.Period(1).Jam(1).Team(1).Fielding(Jammer).Skater"));
        assertEquals(null, state.get("ScoreBoard.Period(1).Jam(1).Team(1).Fielding(Jammer).ThreeJams"));
        assertEquals(null, state.get("ScoreBoard.Period(1).Jam(1).Team(1).Fielding(Jammer).Position"));

        sbm.getTeamModel("1").getCurrentTeamJamModel().fieldSkater(skater, Position.JAMMER);
        advance(0);
        assertEquals("00000000-0000-0000-0000-000000000001", state.get("ScoreBoard.Period(1).Jam(1).Team(1).Fielding(Jammer).Skater"));
        sbm.deleteSkaterModel(skater);
        advance(0);
        assertEquals(null, state.get("ScoreBoard.Period(1).Jam(1).Team(1).Fielding(Jammer).Skater"));
        assertEquals(null, state.get("ScoreBoard.Period(1).Jam(1).Team(1).Fielding(Jammer).ThreeJams"));
        assertEquals(null, state.get("ScoreBoard.Period(1).Jam(1).Team(1).Fielding(Jammer).Position"));

        sbm.stopJamTO();
        advance(1000);
        assertEquals(2000L, state.get("ScoreBoard.Period(1).Jam(1).JamClockElapsedEnd"));
        assertEquals(2000L, state.get("ScoreBoard.Period(1).Jam(1).PeriodClockElapsedEnd"));

        sbm.startJam();
        advance(1000);
        sbm.stopJamTO();
        advance(1000);
        sbm.startJam();
        advance(1000);
        sbm.stopJamTO();
        advance(1000);
        assertEquals(3000L, state.get("ScoreBoard.Period(1).Jam(2).PeriodClockElapsedStart"));
        assertEquals(5000L, state.get("ScoreBoard.Period(1).Jam(3).PeriodClockElapsedStart"));
        // Remove a jam.
        sbm.deleteJamModel(sbm.getCurrentJamModel().getPrevious());
        advance(0);
        assertEquals(5000L, state.get("ScoreBoard.Period(1).Jam(2).PeriodClockElapsedStart"));
        assertEquals(0L, state.get("ScoreBoard.Period(1).Jam(3).PeriodClockElapsedStart"));

    }

}
