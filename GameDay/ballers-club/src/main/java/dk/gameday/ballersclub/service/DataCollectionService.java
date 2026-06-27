package dk.gameday.ballersclub.service;

import dk.gameday.ballersclub.model.DataCollectionSummary;
import dk.gameday.ballersclub.model.Prediction;
import dk.gameday.ballersclub.model.PredictionFeedItem;
import dk.gameday.ballersclub.repository.PredictionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DataCollectionService {

    private final PredictionRepository predictionRepository;
    private final ScoringService scoringService;

    public DataCollectionService(PredictionRepository predictionRepository, ScoringService scoringService) {
        this.predictionRepository = predictionRepository;
        this.scoringService = scoringService;
    }

    @Transactional(readOnly = true)
    public DataCollectionSummary getSummary() {
        List<Prediction> predictions = predictionRepository.findAllWithUserAndMatch();
        int total = predictions.size();
        int activePredictors = (int) predictions.stream()
                .map(prediction -> prediction.getUser().getId())
                .distinct()
                .count();
        int matchesWithData = (int) predictions.stream()
                .map(prediction -> prediction.getMatch().getId())
                .distinct()
                .count();

        Map.Entry<String, Long> popularScore = predictions.stream()
                .collect(Collectors.groupingBy(
                        prediction -> prediction.getHomeGoals() + "-" + prediction.getAwayGoals(),
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .max(Comparator
                        .<Map.Entry<String, Long>>comparingLong(Map.Entry::getValue)
                        .thenComparing(Map.Entry::getKey))
                .orElse(null);

        String mostPopularScore = popularScore == null ? "Ingen data endnu" : popularScore.getKey();
        int mostPopularScorePercentage = popularScore == null ? 0 : percentage(popularScore.getValue().intValue(), total);

        return new DataCollectionSummary(
                total,
                activePredictors,
                matchesWithData,
                mostPopularScore,
                mostPopularScorePercentage
        );
    }

    @Transactional(readOnly = true)
    public List<PredictionFeedItem> getPredictionFeed() {
        List<Prediction> resultedPredictions = predictionRepository.findAllWithUserAndMatch().stream()
                .filter(prediction -> prediction.getMatch().hasResult())
                .sorted(Comparator
                        .comparing((Prediction prediction) -> prediction.getMatch().getKickoffAt()).reversed()
                        .thenComparing(Prediction::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        List<PredictionFeedItem> items = new ArrayList<>();
        items.addAll(streakItems(resultedPredictions));
        items.addAll(matchInsightItems(resultedPredictions));
        return items.stream()
                .limit(10)
                .toList();
    }

    private List<PredictionFeedItem> streakItems(List<Prediction> predictions) {
        Map<String, List<Prediction>> byUser = predictions.stream()
                .collect(Collectors.groupingBy(
                        prediction -> prediction.getUser().getUsername(),
                        HashMap::new,
                        Collectors.toList()
                ));

        List<UserStreak> streaks = new ArrayList<>();
        for (Map.Entry<String, List<Prediction>> entry : byUser.entrySet()) {
            List<Prediction> userPredictions = entry.getValue().stream()
                    .sorted(Comparator.comparing((Prediction prediction) -> prediction.getMatch().getKickoffAt()).reversed())
                    .toList();

            int streak = 0;
            Prediction latestHit = null;
            for (Prediction prediction : userPredictions) {
                if (scoringService.calculatePoints(prediction) <= 0) {
                    break;
                }
                streak++;
                if (latestHit == null) {
                    latestHit = prediction;
                }
            }
            if (streak >= 3 && latestHit != null) {
                streaks.add(new UserStreak(entry.getKey(), streak, latestHit));
            }
        }

        return streaks.stream()
                .sorted(Comparator
                        .comparingInt(UserStreak::count).reversed()
                        .thenComparing(UserStreak::username, String.CASE_INSENSITIVE_ORDER))
                .map(streak -> new PredictionFeedItem(
                        streak.username() + " har ramt " + streak.count() + " i streg",
                        "Seneste kald: " + matchLabel(streak.latestHit()) + " - pick " + predictedScore(streak.latestHit())
                ))
                .toList();
    }

    private List<PredictionFeedItem> matchInsightItems(List<Prediction> predictions) {
        Map<Long, List<Prediction>> byMatch = predictions.stream()
                .collect(Collectors.groupingBy(
                        prediction -> prediction.getMatch().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<PredictionFeedItem> items = new ArrayList<>();
        for (List<Prediction> matchPredictions : byMatch.values()) {
            if (matchPredictions.isEmpty()) {
                continue;
            }
            Prediction sample = matchPredictions.get(0);
            List<Prediction> exactPredictions = matchPredictions.stream()
                    .filter(prediction -> scoringService.calculatePoints(prediction) == 3)
                    .toList();
            List<Prediction> outcomePredictions = matchPredictions.stream()
                    .filter(prediction -> scoringService.calculatePoints(prediction) == 1)
                    .toList();
            long exact = exactPredictions.size();
            long outcome = outcomePredictions.size();
            long totalHits = exact + outcome;

            if (totalHits == 0) {
                items.add(new PredictionFeedItem(
                        "Ingen ramte " + resultLabel(sample),
                        matchLabel(sample) + " endte " + resultLabel(sample) + " - feltet undervurderede " + winnerLabel(sample)
                ));
            } else {
                items.add(new PredictionFeedItem(
                        totalHits + " ramte " + matchLabel(sample),
                        "Resultat " + resultLabel(sample) + " - " + hitSummary("Præcis", exactPredictions)
                                + " - " + hitSummary("Udfald", outcomePredictions)
                ));
            }
        }
        return items;
    }

    private String hitSummary(String label, List<Prediction> predictions) {
        if (predictions.isEmpty()) {
            return label + ": ingen";
        }
        return label + ": " + predictions.stream()
                .map(prediction -> prediction.getUser().getUsername())
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.joining(", "));
    }

    private String matchLabel(Prediction prediction) {
        return prediction.getMatch().getHomeTeam() + " vs " + prediction.getMatch().getAwayTeam();
    }

    private String resultLabel(Prediction prediction) {
        return prediction.getMatch().getHomeScore() + "-" + prediction.getMatch().getAwayScore();
    }

    private String predictedScore(Prediction prediction) {
        return prediction.getHomeGoals() + "-" + prediction.getAwayGoals();
    }

    private String winnerLabel(Prediction prediction) {
        int homeScore = prediction.getMatch().getHomeScore();
        int awayScore = prediction.getMatch().getAwayScore();
        if (homeScore > awayScore) {
            return prediction.getMatch().getHomeTeam();
        }
        if (awayScore > homeScore) {
            return prediction.getMatch().getAwayTeam();
        }
        return "det uafgjorte resultat";
    }

    private int percentage(int count, int total) {
        if (total == 0) {
            return 0;
        }
        return Math.round((count * 100f) / total);
    }

    private record UserStreak(String username, int count, Prediction latestHit) {
    }
}
