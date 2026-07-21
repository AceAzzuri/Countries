package dk.gameday.ballersclub.service;

import dk.gameday.ballersclub.model.DataCollectionSummary;
import dk.gameday.ballersclub.model.Prediction;
import dk.gameday.ballersclub.model.PredictionFeedItem;
import dk.gameday.ballersclub.model.WorldCupMatch;
import dk.gameday.ballersclub.repository.PollVoteRepository;
import dk.gameday.ballersclub.repository.PredictionRepository;
import dk.gameday.ballersclub.repository.WorldCupMatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DataCollectionService {

    private static final long WORLD_CUP_WINNER_POLL_ID = 1L;
    private static final long ARGENTINA_WINNER_OPTION_ID = 101L;
    private static final long SPAIN_WINNER_OPTION_ID = 103L;
    private static final long ENGLAND_WINNER_OPTION_ID = 105L;
    private static final long SPAIN_SEMI_FINAL_MATCH_ID = 101L;
    private static final long ENGLAND_ARGENTINA_SEMI_FINAL_MATCH_ID = 102L;
    private static final long FINAL_MATCH_ID = 104L;

    private final PredictionRepository predictionRepository;
    private final PollVoteRepository pollVoteRepository;
    private final WorldCupMatchRepository matchRepository;
    private final ScoringService scoringService;

    public DataCollectionService(
            PredictionRepository predictionRepository,
            PollVoteRepository pollVoteRepository,
            WorldCupMatchRepository matchRepository,
            ScoringService scoringService
    ) {
        this.predictionRepository = predictionRepository;
        this.pollVoteRepository = pollVoteRepository;
        this.matchRepository = matchRepository;
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
        items.addAll(finalistBonusItems());
        items.addAll(streakItems(resultedPredictions));
        items.addAll(matchInsightItems(resultedPredictions));
        return items.stream()
                .limit(10)
                .toList();
    }

    private List<PredictionFeedItem> finalistBonusItems() {
        List<PredictionFeedItem> items = new ArrayList<>();
        Optional<WorldCupMatch> finalMatch = matchRepository.findById(FINAL_MATCH_ID);

        if (teamHasReachedFinal("Spain", SPAIN_SEMI_FINAL_MATCH_ID, finalMatch)) {
            items.add(new PredictionFeedItem(
                    "Spain er i finalen",
                    finalistPickDetail("Spain", SPAIN_WINNER_OPTION_ID)
            ));
        }

        matchRepository.findById(ENGLAND_ARGENTINA_SEMI_FINAL_MATCH_ID)
                .flatMap(this::winnerOf)
                .filter(winner -> winner.equals("England") || winner.equals("Argentina"))
                .ifPresent(winner -> items.add(new PredictionFeedItem(
                        winner + " er i finalen",
                        finalistPickDetail(winner, winnerOptionId(winner))
                )));

        return items;
    }

    private boolean teamHasReachedFinal(String team, long semiFinalMatchId, Optional<WorldCupMatch> finalMatch) {
        boolean listedInFinal = finalMatch
                .map(match -> team.equals(match.getHomeTeam()) || team.equals(match.getAwayTeam()))
                .orElse(false);
        if (listedInFinal) {
            return true;
        }
        return matchRepository.findById(semiFinalMatchId)
                .flatMap(this::winnerOf)
                .map(team::equals)
                .orElse(false);
    }

    private Optional<String> winnerOf(WorldCupMatch match) {
        if (match.hasAdvancingTeam()) {
            return Optional.of(match.getAdvancingTeam());
        }
        if (!match.hasResult()) {
            return Optional.empty();
        }
        if (match.getHomeScore() > match.getAwayScore()) {
            return Optional.of(match.getHomeTeam());
        }
        if (match.getAwayScore() > match.getHomeScore()) {
            return Optional.of(match.getAwayTeam());
        }
        return Optional.empty();
    }

    private String finalistPickDetail(String team, long optionId) {
        List<String> usernames = pollVoteRepository
                .findByPollIdAndOptionIdOrderByUsernameAsc(WORLD_CUP_WINNER_POLL_ID, optionId)
                .stream()
                .map(vote -> vote.getUsername() == null ? "" : vote.getUsername().trim())
                .filter(username -> !username.isBlank())
                .distinct()
                .toList();
        if (usernames.isEmpty()) {
            return "Ingen havde " + team + " som VM-vinder.";
        }
        return "VM-vinder-picks på " + team + ": " + String.join(", ", usernames);
    }

    private long winnerOptionId(String team) {
        return switch (team) {
            case "Argentina" -> ARGENTINA_WINNER_OPTION_ID;
            case "England" -> ENGLAND_WINNER_OPTION_ID;
            default -> throw new IllegalArgumentException("Ukendt finalehold: " + team);
        };
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
                if (!scoringService.isExactScoreHit(prediction)) {
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
                        streak.username() + " har ramt " + streak.count() + " præcise i streg",
                        "Seneste præcise: " + matchLabel(streak.latestHit()) + " - pick " + predictedScore(streak.latestHit())
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
                    .filter(scoringService::isExactScoreHit)
                    .toList();
            List<Prediction> outcomePredictions = matchPredictions.stream()
                    .filter(scoringService::isCorrectOutcomeHit)
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
        String advancingTeam = prediction.getMatch().getAdvancingTeam();
        return advancingTeam != null ? advancingTeam : "det uafgjorte resultat";
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
