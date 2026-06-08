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

    @Transactional(readOnly = true)
    public List<PollView> getActivePollViews(String username) {
        String normalizedUsername = normalizeUsername(username);
        return polls.stream()
                .filter(Poll::isActive)
                .map(poll -> buildPollView(poll, normalizedUsername))
                .toList();
    }

    public List<Poll> getUpcomingPolls() {
        return polls.stream()
                .filter(poll -> !poll.isActive())
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
        if (findVoteByUsername(pollId, normalizedUsername) != null) {
            throw new IllegalArgumentException("Du har allerede stemt på denne poll.");
        }

        boolean optionExists = poll.getOptions().stream()
                .anyMatch(option -> option.getId().equals(optionId));
        if (!optionExists) {
            throw new IllegalArgumentException("Ukendt valgmulighed.");
        }

        voteRepository.save(new PollVote(pollId, optionId, normalizedUsername, LocalDateTime.now()));
    }

    private PollView buildPollView(Poll poll, String username) {
        List<PollVote> pollVotes = voteRepository.findByPollIdOrderBySubmittedAtDesc(poll.getId());
        int totalVotes = pollVotes.size();

        PollVote myVote = username.isBlank() ? null : findVoteByUsername(poll.getId(), username);
        String myVoteOptionLabel = null;
        if (myVote != null) {
            myVoteOptionLabel = poll.getOptions().stream()
                    .filter(option -> option.getId().equals(myVote.getOptionId()))
                    .map(PollOption::getLabel)
                    .findFirst()
                    .orElse("");
        }

        List<PollOptionResult> optionResults = poll.getOptions().stream()
                .map(option -> {
                    int count = (int) pollVotes.stream()
                            .filter(vote -> vote.getOptionId().equals(option.getId()))
                            .count();
                    return new PollOptionResult(option, count, percentage(count, totalVotes));
                })
                .toList();

        return new PollView(poll, totalVotes, myVote, myVoteOptionLabel, optionResults, pollVotes.stream().limit(5).toList());
    }

    private Poll findPoll(Long pollId) {
        return polls.stream()
                .filter(poll -> poll.getId().equals(pollId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Poll blev ikke fundet."));
    }

    private PollVote findVoteByUsername(Long pollId, String username) {
        return voteRepository.findByPollIdAndUsernameIgnoreCase(pollId, username).orElse(null);
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
        polls.add(new Poll(1L, PollCategory.TOURNAMENT, "Hvem vinder VM 2026?", true, "Tidlige picks kan give flere bonuspoint.",
                List.of(new PollOption(101L, "Argentina"), new PollOption(102L, "France"), new PollOption(103L, "Spain"), new PollOption(104L, "Brazil"), new PollOption(105L, "England"), new PollOption(106L, "Portugal"), new PollOption(107L, "Germany"), new PollOption(108L, "Netherlands"), new PollOption(109L, "Morocco"), new PollOption(110L, "Uruguay"), new PollOption(111L, "Belgium"), new PollOption(112L, "Croatia"), new PollOption(113L, "Colombia"), new PollOption(114L, "Senegal"), new PollOption(115L, "Japan"), new PollOption(116L, "United States"))));
        polls.add(new Poll(2L, PollCategory.PLAYER, "Golden Boot: hvem ender som topscorer?", true, "Officiel award-pick til bonus efter turneringen.",
                List.of(new PollOption(201L, "Kylian Mbappe"), new PollOption(202L, "Harry Kane"), new PollOption(203L, "Lionel Messi"), new PollOption(204L, "Erling Haaland"), new PollOption(205L, "Vinicius Junior"), new PollOption(206L, "Cristiano Ronaldo"), new PollOption(207L, "Lautaro Martinez"), new PollOption(208L, "Jude Bellingham"), new PollOption(209L, "Alexander Isak"), new PollOption(210L, "Santiago Gimenez"), new PollOption(211L, "Julian Alvarez"), new PollOption(212L, "Rodrygo"), new PollOption(213L, "Bukayo Saka"), new PollOption(214L, "Lamine Yamal"), new PollOption(215L, "Darwin Nunez"), new PollOption(216L, "Jonathan David"))));
        polls.add(new Poll(3L, PollCategory.TOURNAMENT, "Hvilken lille nation eller outsider når længst?", true, "Underdog pick. Lavere rangering kan senere give ekstra bonus.",
                List.of(new PollOption(301L, "Cabo Verde"), new PollOption(302L, "Curacao"), new PollOption(303L, "Jordan"), new PollOption(304L, "Uzbekistan"), new PollOption(305L, "Iraq"), new PollOption(306L, "New Zealand"), new PollOption(307L, "Haiti"), new PollOption(308L, "Congo DR"), new PollOption(309L, "Panama"), new PollOption(310L, "Qatar"), new PollOption(311L, "Saudi Arabia"), new PollOption(312L, "South Africa"))));
        polls.add(new Poll(4L, PollCategory.PLAYER, "Golden Glove: hvem bliver turneringens keeper?", true, "Officiel award-pick til bonus efter turneringen.",
                List.of(new PollOption(401L, "Emiliano Martinez"), new PollOption(402L, "Mike Maignan"), new PollOption(403L, "Alisson"), new PollOption(404L, "Jordan Pickford"), new PollOption(405L, "Diogo Costa"), new PollOption(406L, "Unai Simon"), new PollOption(407L, "Thibaut Courtois"), new PollOption(408L, "Yassine Bounou"), new PollOption(409L, "Marc-Andre ter Stegen"), new PollOption(410L, "Edouard Mendy"), new PollOption(411L, "Gianluigi Donnarumma"), new PollOption(412L, "Gregor Kobel"), new PollOption(413L, "Matt Turner"), new PollOption(414L, "Ronwen Williams"))));
        polls.add(new Poll(10L, PollCategory.PLAYER, "Turneringens unge spiller?", true, "Ung spiller-bonus til slut i turneringen.",
                List.of(new PollOption(1001L, "Lamine Yamal"), new PollOption(1002L, "Endrick"), new PollOption(1003L, "Jamal Musiala"), new PollOption(1004L, "Arda Guler"), new PollOption(1005L, "Kendry Paez"), new PollOption(1006L, "Warren Zaire-Emery"), new PollOption(1007L, "Claudio Echeverri"), new PollOption(1008L, "Antonio Nusa"), new PollOption(1009L, "Alejandro Garnacho"), new PollOption(1010L, "Benjamin Sesko"), new PollOption(1011L, "Xavi Simons"), new PollOption(1012L, "Gavi"))));
        polls.add(new Poll(11L, PollCategory.TOURNAMENT, "Hvilket hold bliver den største dark horse?", true, "Bonus for at spotte turneringens overraskelse.",
                List.of(new PollOption(1101L, "Morocco"), new PollOption(1102L, "Senegal"), new PollOption(1103L, "Colombia"), new PollOption(1104L, "Japan"), new PollOption(1105L, "Canada"), new PollOption(1106L, "Austria"), new PollOption(1107L, "Uruguay"), new PollOption(1108L, "United States"), new PollOption(1109L, "Mexico"), new PollOption(1110L, "Ecuador"), new PollOption(1111L, "Ghana"), new PollOption(1112L, "Türkiye"))));
        polls.add(new Poll(5L, PollCategory.CULTURE, "Hvilket land kommer med stærkest fan-energi?", true, "",
                List.of(new PollOption(501L, "Mexico"), new PollOption(502L, "Argentina"), new PollOption(503L, "Morocco"), new PollOption(504L, "Colombia"), new PollOption(505L, "Ghana"), new PollOption(506L, "Japan"), new PollOption(507L, "Brazil"), new PollOption(508L, "Senegal"), new PollOption(509L, "United States"), new PollOption(510L, "South Africa"))));
        polls.add(new Poll(6L, PollCategory.CULTURE, "Hvilket land har den bedste VM-trøje?", true, "",
                List.of(new PollOption(601L, "Mexico"), new PollOption(602L, "Japan"), new PollOption(603L, "Argentina"), new PollOption(604L, "France"), new PollOption(605L, "Brazil"), new PollOption(606L, "Nigeria"), new PollOption(607L, "Germany"), new PollOption(608L, "Netherlands"), new PollOption(609L, "Ghana"), new PollOption(610L, "Croatia"))));
        polls.add(new Poll(8L, PollCategory.CULTURE, "Hvilket land har allerede vundet dit hjerte?", true, "",
                List.of(new PollOption(801L, "Morocco"), new PollOption(802L, "Japan"), new PollOption(803L, "Senegal"), new PollOption(804L, "Canada"), new PollOption(805L, "Cabo Verde"), new PollOption(806L, "Jamaica"), new PollOption(807L, "South Africa"), new PollOption(808L, "Haiti"), new PollOption(809L, "Uzbekistan"), new PollOption(810L, "Jordan"))));
        polls.add(new Poll(13L, PollCategory.CULTURE, "Hvad er dit mest mindeværdige VM-moment?", true, "",
                List.of(new PollOption(1301L, "Iniesta afgør finalen mod Holland"), new PollOption(1302L, "Götze afgør finalen mod Argentina"), new PollOption(1303L, "Tyskland 7-1 Brasilien"), new PollOption(1304L, "Mbappé vs Messi-finalen"), new PollOption(1305L, "Zidanes skalle mod Materazzi"), new PollOption(1306L, "Ghana brænder straffe efter Suárez' hånd"), new PollOption(1307L, "Ronaldo dominerer VM 2002"), new PollOption(1308L, "Marokko slår Portugal ud"))));
        polls.add(new Poll(9L, PollCategory.CULTURE, "Hvilke fans skaber den bedste atmosfære?", false, "Åbner tættere på kickoff.",
                List.of(new PollOption(901L, "Mexico"), new PollOption(902L, "Argentina"), new PollOption(903L, "Morocco"), new PollOption(904L, "Colombia"), new PollOption(905L, "Ghana"), new PollOption(906L, "Brazil"), new PollOption(907L, "Japan"), new PollOption(908L, "United States"))));
        polls.add(new Poll(7L, PollCategory.PLAYER, "Turneringens spiller: hvem tager den?", false, "Åbner når knockout-fasen er sat.",
                List.of(new PollOption(701L, "Kylian Mbappe"), new PollOption(702L, "Jude Bellingham"), new PollOption(703L, "Lionel Messi"), new PollOption(704L, "Vinicius Junior"), new PollOption(705L, "Lamine Yamal"), new PollOption(706L, "Jamal Musiala"), new PollOption(707L, "Kevin De Bruyne"), new PollOption(708L, "Federico Valverde"))));
    }
}
