package dk.gameday.ballersclub.model;

public record UserPredictionLine(
        String matchLabel,
        String kickoffLabel,
        String predictionLabel,
        String resultLabel,
        int points
) {
}
