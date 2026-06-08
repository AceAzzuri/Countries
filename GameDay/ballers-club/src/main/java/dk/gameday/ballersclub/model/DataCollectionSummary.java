package dk.gameday.ballersclub.model;

public record DataCollectionSummary(
        int totalPredictions,
        int activePredictors,
        int matchesWithData,
        String mostPopularScore,
        int mostPopularScorePercentage
) {
}
