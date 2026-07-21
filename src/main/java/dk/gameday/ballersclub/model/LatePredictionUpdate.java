package dk.gameday.ballersclub.model;

import java.time.LocalDateTime;

public record LatePredictionUpdate(
        String username,
        String matchLabel,
        String predictionLabel,
        LocalDateTime kickoffAt,
        LocalDateTime updatedAt
) {
}
