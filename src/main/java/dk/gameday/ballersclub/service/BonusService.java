package dk.gameday.ballersclub.service;

import dk.gameday.ballersclub.model.BonusReviewRow;
import dk.gameday.ballersclub.model.BonusReviewRow.AnswerGroup;
import dk.gameday.ballersclub.model.PollVote;
import dk.gameday.ballersclub.repository.PollVoteRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
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
    private static final Map<Long, String> OPTION_LABELS = Map.ofEntries(
            Map.entry(101L, "Argentina"), Map.entry(102L, "France"), Map.entry(103L, "Spain"), Map.entry(104L, "Brazil"),
            Map.entry(105L, "England"), Map.entry(106L, "Portugal"), Map.entry(107L, "Germany"), Map.entry(108L, "Netherlands"),
            Map.entry(109L, "Morocco"), Map.entry(110L, "Uruguay"), Map.entry(111L, "Belgium"), Map.entry(112L, "Croatia"),
            Map.entry(113L, "Colombia"), Map.entry(114L, "Senegal"), Map.entry(115L, "Japan"), Map.entry(116L, "United States"),
            Map.entry(201L, "Kylian Mbappe"), Map.entry(202L, "Lionel Messi"), Map.entry(203L, "Erling Haaland"), Map.entry(204L, "Vinicius Junior"),
            Map.entry(205L, "Harry Kane"), Map.entry(206L, "Ousmane Dembele"), Map.entry(207L, "Ismael Saibari"), Map.entry(208L, "Matheus Cunha"),
            Map.entry(209L, "Cristiano Ronaldo"), Map.entry(210L, "Mikel Oyarzabal"), Map.entry(211L, "Julian Quinones"), Map.entry(212L, "Raul Jimenez"),
            Map.entry(213L, "Malik Tillman"), Map.entry(214L, "Rodrygo"),
            Map.entry(301L, "Cabo Verde"), Map.entry(302L, "Curacao"), Map.entry(303L, "Jordan"), Map.entry(304L, "Uzbekistan"),
            Map.entry(305L, "Iraq"), Map.entry(306L, "New Zealand"), Map.entry(307L, "Haiti"), Map.entry(308L, "Congo DR"),
            Map.entry(309L, "Panama"), Map.entry(310L, "Qatar"), Map.entry(311L, "Saudi Arabia"), Map.entry(312L, "South Africa"),
            Map.entry(313L, "Iran"),
            Map.entry(401L, "Luis Malagon"), Map.entry(402L, "Matt Freese"), Map.entry(403L, "Jordan Pickford"), Map.entry(404L, "Mike Maignan"),
            Map.entry(405L, "Diogo Costa"), Map.entry(406L, "Unai Simon"), Map.entry(407L, "Emiliano Martinez"), Map.entry(408L, "Alisson"),
            Map.entry(409L, "Yassine Bounou"), Map.entry(410L, "Gregor Kobel"), Map.entry(412L, "Dominik Livakovic"), Map.entry(413L, "Ronwen Williams"),
            Map.entry(701L, "Kylian Mbappe"), Map.entry(702L, "Lionel Messi"), Map.entry(703L, "Erling Haaland"), Map.entry(704L, "Vinicius Junior"),
            Map.entry(705L, "Lamine Yamal"), Map.entry(706L, "Ousmane Dembele"), Map.entry(707L, "Ismael Saibari"), Map.entry(708L, "Malik Tillman"),
            Map.entry(709L, "Gilberto Mora"), Map.entry(710L, "Julian Quinones"), Map.entry(711L, "Mikel Oyarzabal"), Map.entry(712L, "Rodri"),
            Map.entry(1001L, "Lamine Yamal"), Map.entry(1002L, "Gilberto Mora"), Map.entry(1003L, "Pau Cubarsi"), Map.entry(1004L, "Desire Doue"),
            Map.entry(1005L, "Estevao"), Map.entry(1006L, "Kenan Yildiz"), Map.entry(1007L, "Joao Neves"), Map.entry(1008L, "Nico O'Reilly"),
            Map.entry(1009L, "Endrick"), Map.entry(1010L, "Claudio Echeverri"), Map.entry(1011L, "Kendry Paez"), Map.entry(1012L, "Ayyoub Bouaddi"),
            Map.entry(1013L, "Ibrahim Mbaye"), Map.entry(1014L, "Yan Diomande"), Map.entry(1015L, "Antonio Nusa"),
            Map.entry(1101L, "Morocco"), Map.entry(1102L, "Senegal"), Map.entry(1103L, "Colombia"), Map.entry(1104L, "Japan"),
            Map.entry(1105L, "Canada"), Map.entry(1106L, "Austria"), Map.entry(1107L, "Uruguay"), Map.entry(1108L, "United States"),
            Map.entry(1109L, "Mexico"), Map.entry(1110L, "Ecuador"), Map.entry(1111L, "Ghana"), Map.entry(1112L, "Türkiye")
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
                        correctUsernames(definition),
                        answerGroups(definition)
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

    private List<AnswerGroup> answerGroups(BonusDefinition definition) {
        Map<String, List<String>> usernamesByAnswer = new LinkedHashMap<>();
        for (PollVote vote : loadVotes(definition.pollId())) {
            String answer = OPTION_LABELS.getOrDefault(vote.getOptionId(), "Ukendt svar");
            String username = leaderboardUsername(vote.getUsername());
            usernamesByAnswer.computeIfAbsent(answer, ignored -> new java.util.ArrayList<>());
            if (!usernamesByAnswer.get(answer).contains(username)) {
                usernamesByAnswer.get(answer).add(username);
            }
        }
        return usernamesByAnswer.entrySet().stream()
                .map(entry -> new AnswerGroup(
                        entry.getKey(),
                        entry.getValue().stream().sorted(String.CASE_INSENSITIVE_ORDER).toList()
                ))
                .sorted(Comparator.comparing(AnswerGroup::answer, String.CASE_INSENSITIVE_ORDER))
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
