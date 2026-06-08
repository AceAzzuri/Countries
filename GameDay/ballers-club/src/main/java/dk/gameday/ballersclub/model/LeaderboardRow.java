package dk.gameday.ballersclub.model;

public record LeaderboardRow(
        int rank,
        String username,
        int gamesPlayed,
        int exactScores,
        int correctResults,
        int totalPoints
) {

    public int hitPercentage() {
        if (gamesPlayed == 0) {
            return 0;
        }
        return Math.round(((exactScores + correctResults) * 100f) / gamesPlayed);
    }
}
