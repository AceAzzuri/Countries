package dk.gameday.ballersclub.service;

import dk.gameday.ballersclub.model.Poll;
import dk.gameday.ballersclub.model.PollCategory;
import dk.gameday.ballersclub.model.PollOption;
import dk.gameday.ballersclub.model.PollOptionResult;
import dk.gameday.ballersclub.model.PollView;
import dk.gameday.ballersclub.model.PollVote;
import dk.gameday.ballersclub.repository.PollVoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PollService {

    private final List<Poll> polls = new ArrayList<>();
    private final PollVoteRepository voteRepository;

    public PollService(PollVoteRepository voteRepository) {
        this.voteRepository = voteRepository;
        seedPolls();
    }

    public List<PollView> getActivePollViews(String username) {
        String normalizedUsername = normalizeUsername(username);
        return polls.stream()
                .filter(Poll::isActive)
                .map(poll -> buildPollViewSafely(poll, normalizedUsername))
                .toList();
    }

    public List<Poll> getUpcomingPolls() {
        return polls.stream()
                .filter(poll -> !poll.isActive())
                .filter(this::isPointPoll)
                .toList();
    }

    @Transactional
    public void vote(Long pollId, Long optionId, String username) {
        Poll poll = findPoll(pollId);
        if (!poll.isActive()) {
            throw new IllegalArgumentException("Denne poll er ikke åben endnu.");
        }

        String normalizedUsername = normalizeUsername(username);
        if (normalizedUsername.isBlank()) {
            throw new IllegalArgumentException("Log ind før du stemmer.");
        }
        boolean optionExists = poll.getOptions().stream()
                .anyMatch(option -> option.getId().equals(optionId));
        if (!optionExists) {
            throw new IllegalArgumentException("Ukendt valgmulighed.");
        }

        PollVote existingVote = findVoteByUsernameSafely(pollId, normalizedUsername);
        if (existingVote != null) {
            if (!poll.isLegacyAdjustableAwardPoll()) {
                throw new IllegalArgumentException("Du har allerede stemt på denne poll.");
            }
            if (existingVote.getOptionId().equals(optionId)) {
                return;
            }
            existingVote.updateOption(optionId, LocalDateTime.now());
            voteRepository.save(existingVote);
            return;
        }

        voteRepository.save(new PollVote(pollId, optionId, normalizedUsername, LocalDateTime.now()));
    }

    private PollView buildPollView(Poll poll, String username) {
        List<PollVote> pollVotes = loadPollVotesSafely(poll.getId());
        int totalVotes = pollVotes.size();

        PollVote myVote = username.isBlank() ? null : findVoteByUsernameSafely(poll.getId(), username);
        String myVoteOptionLabel = null;
        String originalVoteOptionLabel = null;
        if (myVote != null) {
            myVoteOptionLabel = poll.getOptions().stream()
                    .filter(option -> option.getId().equals(myVote.getOptionId()))
                    .map(PollOption::getLabel)
                    .findFirst()
                    .orElse("");
            originalVoteOptionLabel = poll.getOptions().stream()
                    .filter(option -> option.getId().equals(myVote.getOriginalOptionId()))
                    .map(PollOption::getLabel)
                    .findFirst()
                    .orElse(myVoteOptionLabel);
        }

        List<PollOptionResult> optionResults = poll.getOptions().stream()
                .map(option -> {
                    int count = (int) pollVotes.stream()
                            .filter(vote -> vote.getOptionId().equals(option.getId()))
                            .count();
                    return new PollOptionResult(option, count, percentage(count, totalVotes));
                })
                .toList();

        boolean legacyAwardVote = poll.isLegacyAdjustableAwardPoll() && myVote != null;
        boolean legacyChangedVote = legacyAwardVote && myVote.isChangedAfterQuarterFinal();
        boolean legacyOriginalVote = legacyAwardVote && myVote.isOriginalVoteBeforeReopen() && !legacyChangedVote;
        boolean legacyLateVote = legacyAwardVote && !myVote.isOriginalVoteBeforeReopen() && !legacyChangedVote;

        return new PollView(poll, totalVotes, myVote, myVoteOptionLabel, originalVoteOptionLabel,
                legacyOriginalVote, legacyChangedVote, legacyLateVote, optionResults, pollVotes.stream().limit(5).toList());
    }

    private PollView buildPollViewSafely(Poll poll, String username) {
        try {
            return buildPollView(poll, username);
        } catch (RuntimeException e) {
            return new PollView(poll, 0, null, null, null, false, false, false, List.of(), List.of());
        }
    }

    private List<PollVote> loadPollVotesSafely(Long pollId) {
        try {
            return voteRepository.findByPollIdOrderBySubmittedAtDesc(pollId);
        } catch (RuntimeException e) {
            return List.of();
        }
    }

    private boolean isPointPoll(Poll poll) {
        return switch (poll.getCategory()) {
            case TOURNAMENT, PLAYER -> true;
            case CULTURE, DAILY -> false;
        };
    }

    private Poll findPoll(Long pollId) {
        return polls.stream()
                .filter(poll -> poll.getId().equals(pollId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Poll blev ikke fundet."));
    }

    private PollVote findVoteByUsernameSafely(Long pollId, String username) {
        try {
            return voteRepository.findByPollIdAndUsernameIgnoreCase(pollId, username).orElse(null);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private int percentage(int count, int totalVotes) {
        if (totalVotes == 0) {
            return 0;
        }
        return Math.round((count * 100f) / totalVotes);
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim();
    }

    private void seedPolls() {
        polls.add(new Poll(7L, PollCategory.PLAYER, "Turneringens spiller: hvem tager den?", true, "Frisk bonus - start fra nul, og hold dit valg til finalebilledet er klart.",
                List.of(new PollOption(701L, "Kylian Mbappe"), new PollOption(702L, "Lionel Messi"), new PollOption(703L, "Erling Haaland"), new PollOption(704L, "Vinicius Junior"), new PollOption(705L, "Lamine Yamal"), new PollOption(706L, "Ousmane Dembele"), new PollOption(707L, "Ismael Saibari"), new PollOption(708L, "Malik Tillman"), new PollOption(709L, "Gilberto Mora"), new PollOption(710L, "Julian Quinones"), new PollOption(711L, "Mikel Oyarzabal"), new PollOption(712L, "Rodri"))));
        polls.add(new Poll(10L, PollCategory.PLAYER, "Turneringens unge spiller?", true, "Frisk bonus - FIFA Young Player-style valg blandt VM-spillerne.",
                List.of(new PollOption(1001L, "Lamine Yamal"), new PollOption(1002L, "Gilberto Mora"), new PollOption(1003L, "Pau Cubarsi"), new PollOption(1004L, "Desire Doue"), new PollOption(1005L, "Estevao"), new PollOption(1006L, "Kenan Yildiz"), new PollOption(1007L, "Joao Neves"), new PollOption(1008L, "Nico O'Reilly"), new PollOption(1009L, "Endrick"), new PollOption(1010L, "Claudio Echeverri"), new PollOption(1011L, "Kendry Paez"), new PollOption(1012L, "Ayyoub Bouaddi"), new PollOption(1013L, "Ibrahim Mbaye"), new PollOption(1014L, "Yan Diomande"), new PollOption(1015L, "Antonio Nusa"))));
        polls.add(new Poll(1L, PollCategory.TOURNAMENT, "Hvem vinder VM 2026?", true, "Tidlige picks kan give flere bonuspoint.",
                List.of(new PollOption(101L, "Argentina"), new PollOption(102L, "France"), new PollOption(103L, "Spain"), new PollOption(104L, "Brazil"), new PollOption(105L, "England"), new PollOption(106L, "Portugal"), new PollOption(107L, "Germany"), new PollOption(108L, "Netherlands"), new PollOption(109L, "Morocco"), new PollOption(110L, "Uruguay"), new PollOption(111L, "Belgium"), new PollOption(112L, "Croatia"), new PollOption(113L, "Colombia"), new PollOption(114L, "Senegal"), new PollOption(115L, "Japan"), new PollOption(116L, "United States"))));
        polls.add(new Poll(2L, PollCategory.PLAYER, "Golden Boot: hvem ender som topscorer?", true, "Officiel award-pick til bonus efter turneringen.",
                List.of(new PollOption(201L, "Kylian Mbappe"), new PollOption(202L, "Lionel Messi"), new PollOption(203L, "Erling Haaland"), new PollOption(204L, "Vinicius Junior"), new PollOption(205L, "Harry Kane"), new PollOption(206L, "Ousmane Dembele"), new PollOption(207L, "Ismael Saibari"), new PollOption(208L, "Matheus Cunha"), new PollOption(209L, "Cristiano Ronaldo"), new PollOption(210L, "Mikel Oyarzabal"), new PollOption(211L, "Julian Quinones"), new PollOption(212L, "Raul Jimenez"), new PollOption(213L, "Malik Tillman"), new PollOption(214L, "Rodrygo"))));
        polls.add(new Poll(3L, PollCategory.TOURNAMENT, "Hvilken lille nation eller outsider når længst?", true, "Underdog pick. Lavere rangering kan senere give ekstra bonus.",
                List.of(new PollOption(301L, "Cabo Verde"), new PollOption(302L, "Curacao"), new PollOption(303L, "Jordan"), new PollOption(304L, "Uzbekistan"), new PollOption(305L, "Iraq"), new PollOption(306L, "New Zealand"), new PollOption(307L, "Haiti"), new PollOption(308L, "Congo DR"), new PollOption(309L, "Panama"), new PollOption(310L, "Qatar"), new PollOption(311L, "Saudi Arabia"), new PollOption(312L, "South Africa"), new PollOption(313L, "Iran"))));
        polls.add(new Poll(4L, PollCategory.PLAYER, "Golden Glove: hvem bliver turneringens keeper?", true, "Officiel award-pick til bonus efter turneringen.",
                List.of(new PollOption(401L, "Luis Malagon"), new PollOption(402L, "Matt Freese"), new PollOption(403L, "Jordan Pickford"), new PollOption(404L, "Mike Maignan"), new PollOption(405L, "Diogo Costa"), new PollOption(406L, "Unai Simon"), new PollOption(407L, "Emiliano Martinez"), new PollOption(408L, "Alisson"), new PollOption(409L, "Yassine Bounou"), new PollOption(410L, "Gregor Kobel"), new PollOption(412L, "Dominik Livakovic"), new PollOption(413L, "Ronwen Williams"))));
        polls.add(new Poll(11L, PollCategory.TOURNAMENT, "Hvilket hold bliver den største dark horse?", true, "Bonus for at spotte turneringens overraskelse.",
                List.of(new PollOption(1101L, "Morocco"), new PollOption(1102L, "Senegal"), new PollOption(1103L, "Colombia"), new PollOption(1104L, "Japan"), new PollOption(1105L, "Canada"), new PollOption(1106L, "Austria"), new PollOption(1107L, "Uruguay"), new PollOption(1108L, "United States"), new PollOption(1109L, "Mexico"), new PollOption(1110L, "Ecuador"), new PollOption(1111L, "Ghana"), new PollOption(1112L, "Türkiye"))));
        polls.add(new Poll(5L, PollCategory.CULTURE, "Hvilket land kommer med stærkest fan-energi?", false, "",
                List.of(new PollOption(501L, "Mexico"), new PollOption(502L, "Argentina"), new PollOption(503L, "Morocco"), new PollOption(504L, "Colombia"), new PollOption(505L, "Ghana"), new PollOption(506L, "Japan"), new PollOption(507L, "Brazil"), new PollOption(508L, "Senegal"), new PollOption(509L, "United States"), new PollOption(510L, "South Africa"))));
        polls.add(new Poll(6L, PollCategory.CULTURE, "Hvilket land har den bedste VM-trøje?", false, "",
                List.of(new PollOption(601L, "Mexico"), new PollOption(602L, "Japan"), new PollOption(603L, "Argentina"), new PollOption(604L, "France"), new PollOption(605L, "Brazil"), new PollOption(606L, "Nigeria"), new PollOption(607L, "Germany"), new PollOption(608L, "Netherlands"), new PollOption(609L, "Ghana"), new PollOption(610L, "Croatia"))));
        polls.add(new Poll(8L, PollCategory.CULTURE, "Hvilket land har allerede vundet dit hjerte?", false, "",
                List.of(new PollOption(801L, "Morocco"), new PollOption(802L, "Japan"), new PollOption(803L, "Senegal"), new PollOption(804L, "Canada"), new PollOption(805L, "Cabo Verde"), new PollOption(806L, "Jamaica"), new PollOption(807L, "South Africa"), new PollOption(808L, "Haiti"), new PollOption(809L, "Uzbekistan"), new PollOption(810L, "Jordan"))));
        polls.add(new Poll(13L, PollCategory.CULTURE, "Hvad er dit mest mindeværdige VM-moment?", false, "",
                List.of(new PollOption(1301L, "Iniesta afgør finalen mod Holland"), new PollOption(1302L, "Götze afgør finalen mod Argentina"), new PollOption(1303L, "Tyskland 7-1 Brasilien"), new PollOption(1304L, "Mbappé vs Messi-finalen"), new PollOption(1305L, "Zidanes skalle mod Materazzi"), new PollOption(1306L, "Ghana brænder straffe efter Suárez' hånd"), new PollOption(1307L, "Ronaldo dominerer VM 2002"), new PollOption(1308L, "Marokko slår Portugal ud"))));
        polls.add(new Poll(9L, PollCategory.CULTURE, "Hvilke fans skaber den bedste atmosfære?", false, "Åbner tættere på kickoff.",
                List.of(new PollOption(901L, "Mexico"), new PollOption(902L, "Argentina"), new PollOption(903L, "Morocco"), new PollOption(904L, "Colombia"), new PollOption(905L, "Ghana"), new PollOption(906L, "Brazil"), new PollOption(907L, "Japan"), new PollOption(908L, "United States"))));
    }
}
