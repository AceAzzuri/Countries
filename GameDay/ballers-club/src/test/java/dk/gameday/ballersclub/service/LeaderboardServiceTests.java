package dk.gameday.ballersclub.service;

import dk.gameday.ballersclub.model.LeaderboardRow;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LeaderboardServiceTests {

    private final LeaderboardService leaderboardService = new LeaderboardService(null, new ScoringService(), null);

    @Test
    void ticketCategoriesRequireAtLeastFifteenPlayedMatches() {
        List<LeaderboardRow> rows = List.of(
                row("janvasquez", 14, 7, 0),
                row("Benni", 15, 3, 3)
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
                row("EK", 20, 1, 7),
                row("Batman FC", 20, 1, 7),
                row("Benni", 15, 3, 5)
        );

        assertThat(leaderboardService.topExactScores(rows))
                .extracting(LeaderboardRow::username)
                .containsExactly("Benni", "Batman FC", "EK");
    }

    @Test
    void rollDownKeepsPlayersBelowTicketMinimumVisible() {
        List<LeaderboardRow> rows = List.of(
                row("Benni", 15, 3, 3),
                row("ak81", 14, 4, 2)
        );

        assertThat(leaderboardService.topExactScores(rows))
                .extracting(LeaderboardRow::username)
                .containsExactly("Benni");
        assertThat(leaderboardService.rollDownRows(
                        leaderboardService.exactScoreRanking(rows),
                        leaderboardService.topExactScores(rows)
                ))
                .extracting(LeaderboardRow::username)
                .containsExactly("ak81");
    }

    private LeaderboardRow row(String username, int gamesPlayed, int exactScores, int correctResults) {
        return new LeaderboardRow(0, username, gamesPlayed, exactScores, correctResults, exactScores * 3 + correctResults, 0);
    }
}
