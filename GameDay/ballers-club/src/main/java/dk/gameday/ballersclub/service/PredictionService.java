package dk.gameday.ballersclub.service;

import dk.gameday.ballersclub.model.AppUser;
import dk.gameday.ballersclub.model.LatePredictionUpdate;
import dk.gameday.ballersclub.model.MatchSection;
import dk.gameday.ballersclub.model.MatchPredictionStats;
import dk.gameday.ballersclub.model.Prediction;
import dk.gameday.ballersclub.model.WorldCupMatch;
import dk.gameday.ballersclub.repository.PredictionRepository;
import dk.gameday.ballersclub.repository.WorldCupMatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PredictionService {

    private final WorldCupMatchRepository matchRepository;
    private final PredictionRepository predictionRepository;

    public PredictionService(WorldCupMatchRepository matchRepository, PredictionRepository predictionRepository) {
        this.matchRepository = matchRepository;
        this.predictionRepository = predictionRepository;
    }

    public List<WorldCupMatch> findMatches() {
        return matchRepository.findAllByOrderByKickoffAtAscHomeTeamAsc();
    }

    public List<MatchSection> findMatchSections() {
        List<WorldCupMatch> matches = findMatches();
        List<WorldCupMatch> groupMatchdayOne = filterMatches(matches, LocalDate.of(2026, 6, 11), LocalDate.of(2026, 6, 18), true);
        List<WorldCupMatch> groupMatchdayTwo = filterMatches(matches, LocalDate.of(2026, 6, 18), LocalDate.of(2026, 6, 24), true);
        List<WorldCupMatch> groupMatchdayThree = filterMatches(matches, LocalDate.of(2026, 6, 24), LocalDate.of(2026, 6, 29), true);
        List<WorldCupMatch> knockouts = matches.stream()
                .filter(match -> !match.isGroupMatch())
                .toList();

        return List.of(
                new MatchSection("Gruppekampe 1", "Åbningskampe på tværs af de 12 grupper.", false, groupMatchdayOne),
                new MatchSection("Gruppekampe 2", "Anden runde i gruppespillet.", true, groupMatchdayTwo),
                new MatchSection("Gruppekampe 3", "Sidste gruppekampe med højt pres.", false, groupMatchdayThree),
                new MatchSection("Knockout-vejen", "Fra Round of 32 til finalen.", false, knockouts)
        );
    }

    @Transactional(readOnly = true)
    public Map<Long, Prediction> findPredictionMap(AppUser user) {
        return predictionRepository.findByUserOrderByMatchKickoffAtAsc(user).stream()
                .collect(Collectors.toMap(prediction -> prediction.getMatch().getId(), Function.identity()));
    }

    @Transactional(readOnly = true)
    public Map<Long, MatchPredictionStats> findPredictionStats() {
        return predictionRepository.findAll().stream()
                .collect(Collectors.groupingBy(prediction -> prediction.getMatch().getId()))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> buildStats(entry.getValue())));
    }

    @Transactional(readOnly = true)
    public List<LatePredictionUpdate> findLatePredictionUpdates() {
        return predictionRepository.findLateUpdates().stream()
                .map(prediction -> new LatePredictionUpdate(
                        prediction.getUser().getUsername(),
                        prediction.getMatch().getHomeTeam() + " vs " + prediction.getMatch().getAwayTeam(),
                        prediction.getHomeGoals() + "-" + prediction.getAwayGoals(),
                        prediction.getMatch().getKickoffAt(),
                        prediction.getUpdatedAt()
                ))
                .toList();
    }

    @Transactional
    public int savePrediction(AppUser user, Long matchId, String homeGoals, String awayGoals) {
        WorldCupMatch match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Ukendt kamp."));
        if (!match.isGroupMatch()) {
            throw new IllegalArgumentException("Predictions er kun åbne for gruppekampe.");
        }
        if (!match.isPredictionOpen()) {
            throw new IllegalArgumentException("Predictions lukker ved kickoff.");
        }

        int home = parseGoals(homeGoals);
        int away = parseGoals(awayGoals);
        Prediction prediction = predictionRepository.findByUserAndMatch(user, match)
                .orElseGet(() -> new Prediction(user, match, home, away));
        prediction.updateScore(home, away);
        predictionRepository.save(prediction);
        return 1;
    }

    @Transactional
    public int savePredictions(AppUser user, List<Long> matchIds, List<String> homeGoals, List<String> awayGoals) {
        if (matchIds == null || homeGoals == null || awayGoals == null
                || matchIds.size() != homeGoals.size() || matchIds.size() != awayGoals.size()) {
            throw new IllegalArgumentException("Kunne ikke læse kampene. Prøv at gemme igen.");
        }

        int saved = 0;
        for (int i = 0; i < matchIds.size(); i++) {
            String home = homeGoals.get(i);
            String away = awayGoals.get(i);
            boolean homeBlank = home == null || home.isBlank();
            boolean awayBlank = away == null || away.isBlank();
            if (homeBlank && awayBlank) {
                continue;
            }
            if (homeBlank || awayBlank) {
                throw new IllegalArgumentException("Indtast begge scores for hver kamp, du vil gemme.");
            }
            saved += savePrediction(user, matchIds.get(i), home, away);
        }
        if (saved == 0) {
            throw new IllegalArgumentException("Der var ingen udfyldte kampe at gemme.");
        }
        return saved;
    }

    private int parseGoals(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Indtast begge scores.");
        }
        try {
            int goals = Integer.parseInt(value.trim());
            if (goals < 0 || goals > 30) {
                throw new IllegalArgumentException("Scores skal være mellem 0 og 30.");
            }
            return goals;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Scores skal være hele tal.");
        }
    }

    private List<WorldCupMatch> filterMatches(List<WorldCupMatch> matches, LocalDate fromInclusive, LocalDate toExclusive, boolean groupOnly) {
        return matches.stream()
                .filter(match -> !groupOnly || match.isGroupMatch())
                .filter(match -> {
                    LocalDate kickoffDate = match.getKickoffAt().toLocalDate();
                    return !kickoffDate.isBefore(fromInclusive) && kickoffDate.isBefore(toExclusive);
                })
                .toList();
    }

    private MatchPredictionStats buildStats(List<Prediction> predictions) {
        int totalVotes = predictions.size();
        int homeWinVotes = 0;
        int drawVotes = 0;
        int awayWinVotes = 0;

        for (Prediction prediction : predictions) {
            if (prediction.getHomeGoals() > prediction.getAwayGoals()) {
                homeWinVotes++;
            } else if (prediction.getHomeGoals() < prediction.getAwayGoals()) {
                awayWinVotes++;
            } else {
                drawVotes++;
            }
        }

        return new MatchPredictionStats(
                totalVotes,
                homeWinVotes,
                drawVotes,
                awayWinVotes,
                percentage(homeWinVotes, totalVotes),
                percentage(drawVotes, totalVotes),
                percentage(awayWinVotes, totalVotes)
        );
    }

    private int percentage(int votes, int totalVotes) {
        if (totalVotes == 0) {
            return 0;
        }
        return Math.round((votes * 100f) / totalVotes);
    }
}
