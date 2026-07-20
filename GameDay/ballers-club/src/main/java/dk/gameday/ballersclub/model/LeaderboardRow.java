package dk.gameday.ballersclub.model;

public record LeaderboardRow(
        int rank,
        String username,
        int gamesPlayed,
        int exactScores,
        int correctResults,
        int totalPoints,
        int bonusPoints
) {

    public int hitPercentage() {
        if (gamesPlayed == 0) {
            return 0;
        }
        return Math.round(((exactScores + correctResults) * 100f) / gamesPlayed);
    }

    public int totalWithBonus() {
        return totalPoints + bonusPoints;
    }
}
