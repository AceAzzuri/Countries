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

    static final int TICKET_MINIMUM_PLAYED_MATCHES = 15;

    private final PredictionRepository predictionRepository;
    private final ScoringService scoringService;
    private final BonusService bonusService;

    public LeaderboardService(PredictionRepository predictionRepository, ScoringService scoringService, BonusService bonusService) {
        this.predictionRepository = predictionRepository;
        this.scoringService = scoringService;
        this.bonusService = bonusService;
    }

    public List<LeaderboardRow> getLeaderboard() {
        return buildLeaderboard(predictionRepository.findAllForLeaderboard());
    }

    public List<LeaderboardRow> getEligibleLeaderboard() {
        return getLeaderboard().stream()
                .filter(this::isTicketEligible)
                .toList();
    }

    public List<UserPredictionProfile> getPredictionProfiles() {
        List<Prediction> predictions = predictionRepository.findAllForLeaderboard();
        Map<String, List<Prediction>> predictionsByUser = predictions.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        prediction -> leaderboardUsername(prediction.getUser().getUsername()),
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
        return topHitPercentage(getLeaderboard());
    }

    public List<LeaderboardRow> getTopExactScores() {
        return topExactScores(getLeaderboard());
    }

    public List<LeaderboardRow> getTopCorrectResults() {
        return topCorrectResults(getLeaderboard());
    }

    List<LeaderboardRow> topHitPercentage(List<LeaderboardRow> rows) {
        return hitPercentageRanking(rows).stream()
                .filter(this::isTicketEligible)
                .limit(3)
                .toList();
    }

    List<LeaderboardRow> topExactScores(List<LeaderboardRow> rows) {
        return exactScoreRanking(rows).stream()
                .filter(this::isTicketEligible)
                .limit(3)
                .toList();
    }

    public List<LeaderboardRow> getTopPoints() {
        return pointRanking(getLeaderboard()).stream()
                .filter(this::isTicketEligible)
                .filter(row -> row.totalWithBonus() > 0)
                .limit(3)
                .toList();
    }

    public List<LeaderboardRow> getHitRollDownRows() {
        return rollDownRows(hitPercentageRanking(getLeaderboard()), getTopHitPercentage());
    }

    public List<LeaderboardRow> getExactRollDownRows() {
        return rollDownRows(exactScoreRanking(getLeaderboard()), getTopExactScores());
    }

    public List<LeaderboardRow> getPointRollDownRows() {
        return rollDownRows(pointRanking(getLeaderboard()), getTopPoints());
    }

    public List<LeaderboardRow> getCorrectResultRollDownRows() {
        return rollDownRows(correctResultRanking(getLeaderboard()), getTopCorrectResults());
    }

    private List<LeaderboardRow> hitPercentageRanking(List<LeaderboardRow> rows) {
        return rows.stream()
                .filter(row -> row.gamesPlayed() > 0)
                .sorted(Comparator
                        .comparing(this::isTicketEligible).reversed()
                        .thenComparing(Comparator.comparingInt(LeaderboardRow::hitPercentage).reversed())
                        .thenComparing(Comparator.comparingInt(LeaderboardRow::exactScores).reversed())
                        .thenComparing(Comparator.comparingInt(LeaderboardRow::totalWithBonus).reversed())
                        .thenComparing(LeaderboardRow::username, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    List<LeaderboardRow> exactScoreRanking(List<LeaderboardRow> rows) {
        return rows.stream()
                .filter(row -> row.exactScores() > 0)
                .sorted(Comparator
                        .comparing(this::isTicketEligible).reversed()
                        .thenComparing(Comparator.comparingInt(LeaderboardRow::exactScores).reversed())
                        .thenComparing(Comparator.comparingInt(LeaderboardRow::totalWithBonus).reversed())
                        .thenComparing(LeaderboardRow::username, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private List<LeaderboardRow> topCorrectResults(List<LeaderboardRow> rows) {
        return correctResultRanking(rows).stream()
                .filter(this::isTicketEligible)
                .limit(3)
                .toList();
    }

    private List<LeaderboardRow> correctResultRanking(List<LeaderboardRow> rows) {
        return rows.stream()
                .filter(row -> row.correctResults() > 0)
                .sorted(Comparator
                        .comparing(this::isTicketEligible).reversed()
                        .thenComparing(Comparator.comparingInt(LeaderboardRow::correctResults).reversed())
                        .thenComparing(Comparator.comparingInt(LeaderboardRow::totalWithBonus).reversed())
                        .thenComparing(LeaderboardRow::username, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private List<LeaderboardRow> pointRanking(List<LeaderboardRow> rows) {
        return rows.stream()
                .filter(row -> row.totalWithBonus() > 0)
                .sorted(Comparator
                        .comparing(this::isTicketEligible).reversed()
                        .thenComparing(Comparator.comparingInt(LeaderboardRow::totalWithBonus).reversed())
                        .thenComparing(Comparator.comparingInt(LeaderboardRow::exactScores).reversed())
                        .thenComparing(Comparator.comparingInt(LeaderboardRow::correctResults).reversed())
                        .thenComparing(LeaderboardRow::username, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    List<LeaderboardRow> rollDownRows(List<LeaderboardRow> categoryRows, List<LeaderboardRow> topRows) {
        List<String> topUsernames = topRows.stream()
                .map(LeaderboardRow::username)
                .toList();
        return categoryRows.stream()
                .filter(row -> !topUsernames.contains(row.username()))
                .toList();
    }

    private boolean isTicketEligible(LeaderboardRow row) {
        return row.gamesPlayed() >= TICKET_MINIMUM_PLAYED_MATCHES;
    }

    List<LeaderboardRow> buildLeaderboard(List<Prediction> predictions) {
        Map<String, Integer> exact = new HashMap<>();
        Map<String, Integer> result = new HashMap<>();
        Map<String, Integer> played = new HashMap<>();
        Map<String, Integer> points = new HashMap<>();
        Map<String, Integer> bonusPoints = bonusService.getBonusPointsByUsername();

        for (Prediction prediction : predictions) {
            String username = leaderboardUsername(prediction.getUser().getUsername());
            int earned = scoringService.calculatePoints(prediction);
            if (prediction.getMatch().hasResult()) {
                played.merge(username, 1, Integer::sum);
            }
            if (scoringService.isExactScoreHit(prediction)) {
                exact.merge(username, 1, Integer::sum);
            } else if (scoringService.isCorrectOutcomeHit(prediction)) {
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
                    points.getOrDefault(username, 0),
                    bonusPoints.getOrDefault(username, 0)
            ));
        }

        rows.sort(Comparator
                .comparingInt(LeaderboardRow::totalWithBonus).reversed()
                .thenComparing(Comparator.comparingInt(LeaderboardRow::exactScores).reversed())
                .thenComparing(Comparator.comparingInt(LeaderboardRow::correctResults).reversed())
                .thenComparing(LeaderboardRow::username, String.CASE_INSENSITIVE_ORDER));

        List<LeaderboardRow> ranked = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            LeaderboardRow row = rows.get(i);
            ranked.add(new LeaderboardRow(i + 1, row.username(), row.gamesPlayed(), row.exactScores(), row.correctResults(), row.totalPoints(), row.bonusPoints()));
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

    private String leaderboardUsername(String username) {
        if (username == null) {
            return "";
        }
        String normalized = username.trim().replaceAll("\\s+", " ");
        if (normalized.matches("(?i)^BlckSaitama( \\d+)?$")) {
            return "BlckSaitama";
        }
        return normalized;
    }
}
