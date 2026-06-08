package dk.gameday.ballersclub.service;

import dk.gameday.ballersclub.model.LeaderboardRow;
import dk.gameday.ballersclub.model.Prediction;
import dk.gameday.ballersclub.model.UserPredictionLine;
import dk.gameday.ballersclub.model.UserPredictionProfile;
import dk.gameday.ballersclub.repository.PredictionRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LeaderboardService {

    private final PredictionRepository predictionRepository;
    private final ScoringService scoringService;

    public LeaderboardService(PredictionRepository predictionRepository, ScoringService scoringService) {
        this.predictionRepository = predictionRepository;
        this.scoringService = scoringService;
    }

    public List<LeaderboardRow> getLeaderboard() {
        return buildLeaderboard(predictionRepository.findAllForLeaderboard());
    }

    public List<UserPredictionProfile> getPredictionProfiles() {
        List<Prediction> predictions = predictionRepository.findAllForLeaderboard();
        Map<String, List<Prediction>> predictionsByUser = predictions.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        prediction -> prediction.getUser().getUsername(),
                        java.util.LinkedHashMap::new,
                        java.util.stream.Collectors.toList()
                ));
        Map<String, LeaderboardRow> rowsByUsername = buildLeaderboard(predictions).stream()
                .collect(java.util.stream.Collectors.toMap(
                        LeaderboardRow::username,
                        java.util.function.Function.identity(),
                        (existing, replacement) -> existing,
                        java.util.LinkedHashMap::new
                ));

        List<UserPredictionProfile> profiles = new ArrayList<>();
        for (Map.Entry<String, List<Prediction>> entry : predictionsByUser.entrySet()) {
            LeaderboardRow row = rowsByUsername.get(entry.getKey());
            if (row == null) {
                continue;
            }
            List<UserPredictionLine> lines = entry.getValue().stream()
                    .map(this::toPredictionLine)
                    .toList();
            profiles.add(new UserPredictionProfile(row, lines.size(), lines));
        }
        return profiles;
    }

    public List<LeaderboardRow> getTopHitPercentage() {
        return getLeaderboard().stream()
                .filter(row -> row.gamesPlayed() > 0)
                .sorted(Comparator
                        .comparingInt(LeaderboardRow::hitPercentage).reversed()
                        .thenComparingInt(LeaderboardRow::totalPoints).reversed()
                        .thenComparing(LeaderboardRow::username, String.CASE_INSENSITIVE_ORDER))
                .limit(3)
                .toList();
    }

    public List<LeaderboardRow> getTopExactScores() {
        return getLeaderboard().stream()
                .filter(row -> row.exactScores() > 0)
                .sorted(Comparator
                        .comparingInt(LeaderboardRow::exactScores).reversed()
                        .thenComparingInt(LeaderboardRow::totalPoints).reversed()
                        .thenComparing(LeaderboardRow::username, String.CASE_INSENSITIVE_ORDER))
                .limit(3)
                .toList();
    }

    public List<LeaderboardRow> getTopPoints() {
        return getLeaderboard().stream()
                .filter(row -> row.totalPoints() > 0)
                .limit(3)
                .toList();
    }

    List<LeaderboardRow> buildLeaderboard(List<Prediction> predictions) {
        Map<String, Integer> exact = new HashMap<>();
        Map<String, Integer> result = new HashMap<>();
        Map<String, Integer> played = new HashMap<>();
        Map<String, Integer> points = new HashMap<>();

        for (Prediction prediction : predictions) {
            String username = prediction.getUser().getUsername();
            int earned = scoringService.calculatePoints(prediction);
            if (prediction.getMatch().hasResult()) {
                played.merge(username, 1, Integer::sum);
            }
            if (earned == 3) {
                exact.merge(username, 1, Integer::sum);
            } else if (earned == 1) {
                result.merge(username, 1, Integer::sum);
            }
            points.merge(username, earned, Integer::sum);
        }

        List<LeaderboardRow> rows = new ArrayList<>();
        for (String username : points.keySet()) {
            rows.add(new LeaderboardRow(
                    0,
                    username,
                    played.getOrDefault(username, 0),
                    exact.getOrDefault(username, 0),
                    result.getOrDefault(username, 0),
                    points.getOrDefault(username, 0)
            ));
        }

        rows.sort(Comparator
                .comparingInt(LeaderboardRow::totalPoints).reversed()
                .thenComparingInt(LeaderboardRow::exactScores).reversed()
                .thenComparingInt(LeaderboardRow::correctResults).reversed()
                .thenComparing(LeaderboardRow::username, String.CASE_INSENSITIVE_ORDER));

        List<LeaderboardRow> ranked = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            LeaderboardRow row = rows.get(i);
            ranked.add(new LeaderboardRow(i + 1, row.username(), row.gamesPlayed(), row.exactScores(), row.correctResults(), row.totalPoints()));
        }
        return ranked;
    }

    private UserPredictionLine toPredictionLine(Prediction prediction) {
        String resultLabel = prediction.getMatch().hasResult()
                ? prediction.getMatch().getHomeScore() + "-" + prediction.getMatch().getAwayScore()
                : "Afventer resultat";
        return new UserPredictionLine(
                prediction.getMatch().getHomeTeam() + " vs " + prediction.getMatch().getAwayTeam(),
                prediction.getMatch().getKickoffLabel(),
                prediction.getHomeGoals() + "-" + prediction.getAwayGoals(),
                resultLabel,
                scoringService.calculatePoints(prediction)
        );
    }
}
