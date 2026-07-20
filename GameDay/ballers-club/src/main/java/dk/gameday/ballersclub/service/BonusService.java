package dk.gameday.ballersclub.service;

import dk.gameday.ballersclub.model.BonusReviewRow;
import dk.gameday.ballersclub.model.PollVote;
import dk.gameday.ballersclub.repository.PollVoteRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BonusService {

    private static final List<BonusDefinition> BONUS_DEFINITIONS = List.of(
            legacy(1L, "Hvem vinder VM 2026?", "Spain", 103L),
            legacy(2L, "Golden Boot: hvem ender som topscorer?", "Kylian Mbappe", 201L),
            fixed(3L, "Hvilken lille nation eller outsider når længst?", "Ikke officiel FIFA-award", null, 3),
            legacy(4L, "Golden Glove: hvem bliver turneringens keeper?", "Unai Simon", 406L),
            fixed(7L, "Turneringens spiller: hvem tager den?", "Rodri", 712L, 3),
            fixed(10L, "Turneringens unge spiller?", "Pau Cubarsi", 1003L, 3),
            fixed(11L, "Hvilket hold bliver den største dark horse?", "Ikke officiel FIFA-award", null, 3)
    );

    private final PollVoteRepository voteRepository;

    public BonusService(PollVoteRepository voteRepository) {
        this.voteRepository = voteRepository;
    }

    public Map<String, Integer> getBonusPointsByUsername() {
        Map<String, Integer> pointsByUsername = new LinkedHashMap<>();
        for (BonusDefinition definition : BONUS_DEFINITIONS) {
            if (!definition.decided()) {
                continue;
            }
            Map<String, Integer> pollPoints = new LinkedHashMap<>();
            for (PollVote vote : loadVotes(definition.pollId())) {
                int points = pointsFor(definition, vote);
                if (points > 0) {
                    pollPoints.merge(leaderboardUsername(vote.getUsername()), points, Math::max);
                }
            }
            pollPoints.forEach((username, points) -> pointsByUsername.merge(username, points, Integer::sum));
        }
        return pointsByUsername;
    }

    public List<BonusReviewRow> getBonusReviewRows() {
        return BONUS_DEFINITIONS.stream()
                .map(definition -> new BonusReviewRow(
                        definition.question(),
                        definition.correctAnswer() == null ? "Ikke sat" : definition.correctAnswer(),
                        definition.reviewPoints(),
                        definition.decided(),
                        definition.pointScored(),
                        correctUsernames(definition)
                ))
                .toList();
    }

    private List<String> correctUsernames(BonusDefinition definition) {
        if (!definition.decided()) {
            return List.of();
        }
        return loadVotes(definition.pollId()).stream()
                .filter(vote -> pointsFor(definition, vote) > 0)
                .map(vote -> leaderboardUsername(vote.getUsername()))
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private int pointsFor(BonusDefinition definition, PollVote vote) {
        if (definition.correctOptionId() == null || !vote.getOptionId().equals(definition.correctOptionId())) {
            return 0;
        }
        if (!definition.legacyAward()) {
            return definition.basePoints();
        }
        if (vote.isOriginalVoteBeforeReopen() && !vote.isChangedAfterQuarterFinal()) {
            return 6;
        }
        return 2;
    }

    private List<PollVote> loadVotes(Long pollId) {
        try {
            return voteRepository.findByPollIdOrderBySubmittedAtDesc(pollId);
        } catch (RuntimeException e) {
            return List.of();
        }
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

    private static BonusDefinition legacy(Long pollId, String question, String correctAnswer, Long correctOptionId) {
        return new BonusDefinition(pollId, question, correctAnswer, correctOptionId, true, 2);
    }

    private static BonusDefinition fixed(Long pollId, String question, String correctAnswer, Long correctOptionId, int basePoints) {
        return new BonusDefinition(pollId, question, correctAnswer, correctOptionId, false, basePoints);
    }

    private record BonusDefinition(
            Long pollId,
            String question,
            String correctAnswer,
            Long correctOptionId,
            boolean legacyAward,
            int basePoints
    ) {
        private boolean decided() {
            return correctAnswer != null;
        }

        private boolean pointScored() {
            return correctOptionId != null;
        }

        private int reviewPoints() {
            return legacyAward ? 6 : basePoints;
        }
    }
}
