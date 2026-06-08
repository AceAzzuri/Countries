package dk.gameday.ballersclub.service;

import dk.gameday.ballersclub.model.DataCollectionSummary;
import dk.gameday.ballersclub.model.Prediction;
import dk.gameday.ballersclub.model.PredictionFeedItem;
import dk.gameday.ballersclub.repository.PredictionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
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
        return predictionRepository.findAllWithUserAndMatch().stream()
                .filter(prediction -> prediction.getMatch().hasResult())
                .filter(prediction -> scoringService.calculatePoints(prediction) > 0)
                .sorted(Comparator
                        .comparing((Prediction prediction) -> prediction.getMatch().getKickoffAt()).reversed()
                        .thenComparing(Prediction::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .map(this::toFeedItem)
                .toList();
    }

    private PredictionFeedItem toFeedItem(Prediction prediction) {
        int points = scoringService.calculatePoints(prediction);
        String username = prediction.getUser().getUsername();
        String homeTeam = prediction.getMatch().getHomeTeam();
        String awayTeam = prediction.getMatch().getAwayTeam();
        String actualScore = prediction.getMatch().getHomeScore() + "-" + prediction.getMatch().getAwayScore();
        String predictedScore = prediction.getHomeGoals() + "-" + prediction.getAwayGoals();

        if (points == 3) {
            return new PredictionFeedItem(
                    username + " ramte præcis " + actualScore,
                    homeTeam + " vs " + awayTeam + " • pick " + predictedScore
            );
        }

        return new PredictionFeedItem(
                username + " læste udfaldet rigtigt",
                homeTeam + " vs " + awayTeam + " • pick " + predictedScore
        );
    }

    private int percentage(int count, int total) {
        if (total == 0) {
            return 0;
        }
        return Math.round((count * 100f) / total);
    }
}
