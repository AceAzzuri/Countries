package dk.gameday.ballersclub.model;

import java.util.List;

public record UserPredictionProfile(
        LeaderboardRow row,
        int totalPredictions,
        List<UserPredictionLine> predictions
) {
}
