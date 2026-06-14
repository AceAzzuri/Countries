package dk.gameday.ballersclub.service;

import dk.gameday.ballersclub.model.LeaderboardRow;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LeaderboardServiceTests {

    private final LeaderboardService leaderboardService = new LeaderboardService(null, new ScoringService());

    @Test
    void ticketCategoriesRequireAtLeastEightPlayedMatches() {
        List<LeaderboardRow> rows = List.of(
                row("janvasquez", 7, 7, 0),
                row("Benni", 8, 3, 3)
        );

        assertThat(leaderboardService.topHitPercentage(rows))
                .extracting(LeaderboardRow::username)
                .containsExactly("Benni");
        assertThat(leaderboardService.topExactScores(rows))
                .extracting(LeaderboardRow::username)
                .containsExactly("Benni");
    }

    @Test
    void exactScoreCategorySortsByMostExactScoresFirst() {
        List<LeaderboardRow> rows = List.of(
                row("EK", 8, 1, 7),
                row("Batman FC", 8, 1, 7),
                row("Benni", 8, 3, 5)
        );

        assertThat(leaderboardService.topExactScores(rows))
                .extracting(LeaderboardRow::username)
                .containsExactly("Benni", "Batman FC", "EK");
    }

    private LeaderboardRow row(String username, int gamesPlayed, int exactScores, int correctResults) {
        return new LeaderboardRow(0, username, gamesPlayed, exactScores, correctResults, exactScores * 3 + correctResults);
    }
}
