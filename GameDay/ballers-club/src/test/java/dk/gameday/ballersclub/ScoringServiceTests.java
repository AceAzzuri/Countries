package dk.gameday.ballersclub;

import dk.gameday.ballersclub.model.AppUser;
import dk.gameday.ballersclub.model.Prediction;
import dk.gameday.ballersclub.model.WorldCupMatch;
import dk.gameday.ballersclub.service.ScoringService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ScoringServiceTests {

    private final ScoringService scoringService = new ScoringService();

    @Test
    void exactScoreGetsThreePoints() throws Exception {
        Prediction prediction = prediction(2, 1, 2, 1);

        assertThat(scoringService.calculatePoints(prediction)).isEqualTo(3);
    }

    @Test
    void correctResultGetsOnePoint() throws Exception {
        Prediction prediction = prediction(2, 0, 1, 0);

        assertThat(scoringService.calculatePoints(prediction)).isEqualTo(1);
    }

    @Test
    void wrongResultGetsZeroPoints() throws Exception {
        Prediction prediction = prediction(0, 2, 1, 0);

        assertThat(scoringService.calculatePoints(prediction)).isZero();
    }

    private Prediction prediction(int predictedHome, int predictedAway, int actualHome, int actualAway) throws Exception {
        WorldCupMatch match = new WorldCupMatch("Group Stage", "Home", "Away", LocalDateTime.now().minusDays(1), "Arena");
        setField(match, "homeScore", actualHome);
        setField(match, "awayScore", actualAway);
        return new Prediction(new AppUser("tester"), match, predictedHome, predictedAway);
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
