package com.carolinarollergirls.scoreboard.defaults;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.Queue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.carolinarollergirls.scoreboard.event.ConditionalScoreBoardListener;
import com.carolinarollergirls.scoreboard.event.ScoreBoardEvent;
import com.carolinarollergirls.scoreboard.event.ScoreBoardListener;
import com.carolinarollergirls.scoreboard.model.ScoreBoardModel;
import com.carolinarollergirls.scoreboard.model.TeamModel;
import com.carolinarollergirls.scoreboard.utils.ScoreBoardClock;
import com.carolinarollergirls.scoreboard.view.Settings;
import com.carolinarollergirls.scoreboard.view.Team;

public class DefaultTeamModelTests {

    private ScoreBoardModel sbModelMock;
    private Settings settingsMock;
    private TeamModel otherTeamMock;

    private int maxNumberTimeouts = 3;
    private boolean timeoutsPerPeriod = false;
    private int maxNumberReviews = 1;
    private boolean reviewsPerPeriod = true;

    private Queue<ScoreBoardEvent> collectedEvents;
    public ScoreBoardListener listener = new ScoreBoardListener() {

        @Override
        public void scoreBoardChange(ScoreBoardEvent event) {
            synchronized(collectedEvents) {
                collectedEvents.add(event);
            }
        }
    };


    private DefaultTeamModel team;
    private static String ID = "TEST";

    @Before
    public void setUp() throws Exception {
        collectedEvents = new LinkedList<ScoreBoardEvent>();

        sbModelMock = Mockito.mock(DefaultScoreBoardModel.class);

        settingsMock = Mockito.mock(Settings.class);
        otherTeamMock = Mockito.mock(DefaultTeamModel.class);

        Mockito
        .when(sbModelMock.getScoreBoard())
        .thenReturn(sbModelMock);

        Mockito
        .when(sbModelMock.getSettings())
        .thenReturn(settingsMock);

        Mockito
        .when(sbModelMock.getTeamModel(Mockito.anyString()))
        .thenReturn(otherTeamMock);

        Mockito
        .when(settingsMock.getInt(Team.SETTING_NUMBER_TIMEOUTS))
        .thenAnswer(new Answer<Integer>() {
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                return maxNumberTimeouts;
            }
        });

        Mockito
        .when(settingsMock.getBoolean(Team.SETTING_TIMEOUTS_PER_PERIOD))
        .thenAnswer(new Answer<Boolean>() {
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                return timeoutsPerPeriod;
            }
        });
        Mockito
        .when(settingsMock.getInt(Team.SETTING_NUMBER_REVIEWS))
        .thenAnswer(new Answer<Integer>() {
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                return maxNumberReviews;
            }
        });

        Mockito
        .when(settingsMock.getBoolean(Team.SETTING_REVIEWS_PER_PERIOD))
        .thenAnswer(new Answer<Boolean>() {
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                return reviewsPerPeriod;
            }
        });

        team = new DefaultTeamModel(sbModelMock, ID);
        ScoreBoardClock.getInstance().stop();
    }

    @Test
    public void testSetRetainedOfficialReview() {
        assertFalse(team.retainedOfficialReview());
        assertEquals(1, team.getOfficialReviewsRemaining());
        team.addScoreBoardListener(new ConditionalScoreBoardListener(team, Team.EVENT_RETAINED_OFFICIAL_REVIEW, listener));
        team.addScoreBoardListener(new ConditionalScoreBoardListener(team, Team.EVENT_OFFICIAL_REVIEWS, listener));

        team.setRetainedOfficialReview(true);
        assertTrue(team.retainedOfficialReview());
        assertEquals(1, team.getOfficialReviewsRemaining());
        assertEquals(1, collectedEvents.size());
        ScoreBoardEvent event = collectedEvents.poll();
        assertTrue((Boolean)event.getValue());
        assertFalse((Boolean)event.getPreviousValue());

        //check idempotency
        team.setRetainedOfficialReview(true);
        assertTrue(team.retainedOfficialReview());
        assertEquals(0, collectedEvents.size());

        team.setRetainedOfficialReview(false);
        assertFalse(team.retainedOfficialReview());
    }
}
