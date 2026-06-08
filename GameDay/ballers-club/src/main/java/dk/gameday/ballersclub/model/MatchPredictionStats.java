package dk.gameday.ballersclub.model;

public record MatchPredictionStats(
        int totalVotes,
        int homeWinVotes,
        int drawVotes,
        int awayWinVotes,
        int homeWinPercentage,
        int drawPercentage,
        int awayWinPercentage
) {
}
