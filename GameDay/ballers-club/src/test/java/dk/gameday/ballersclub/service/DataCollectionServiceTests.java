package dk.gameday.ballersclub.service;

import dk.gameday.ballersclub.model.PollVote;
import dk.gameday.ballersclub.model.WorldCupMatch;
import dk.gameday.ballersclub.repository.PollVoteRepository;
import dk.gameday.ballersclub.repository.PredictionRepository;
import dk.gameday.ballersclub.repository.WorldCupMatchRepository;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DataCollectionServiceTests {

    @Test
    void feedShowsUsersWhoPickedSpainWhenSpainReachesFinal() {
        DataCollectionService dataCollectionService = service(
                Map.of(
                        104L, match("Final", "Spain", "Winner Match 102"),
                        102L, match("Semi-final", "England", "Argentina")
                ),
                Map.of(103L, List.of(
                        vote("Azzuri", 103L),
                        vote("Benni", 103L)
                ))
        );

        var feed = dataCollectionService.getPredictionFeed();

        assertThat(feed)
                .extracting(item -> item.headline() + " | " + item.detail())
                .contains("Spain er i finalen | VM-vinder-picks på Spain: Azzuri, Benni");
    }

    @Test
    void feedShowsUsersWhoPickedEnglandWhenEnglandArgentinaSemiFinalIsDecided() {
        WorldCupMatch semiFinal = match("Semi-final", "England", "Argentina");
        semiFinal.updateResult(2, 1);

        DataCollectionService dataCollectionService = service(
                Map.of(
                        104L, match("Final", "Spain", "Winner Match 102"),
                        102L, semiFinal
                ),
                Map.of(
                        103L, List.of(),
                        105L, List.of(vote("Hyzz", 105L))
                )
        );

        var feed = dataCollectionService.getPredictionFeed();

        assertThat(feed)
                .extracting(item -> item.headline() + " | " + item.detail())
                .contains("England er i finalen | VM-vinder-picks på England: Hyzz");
    }

    private WorldCupMatch match(String roundLabel, String homeTeam, String awayTeam) {
        return new WorldCupMatch(roundLabel, homeTeam, awayTeam, LocalDateTime.of(2026, 7, 15, 21, 0), "Arena");
    }

    private PollVote vote(String username, long optionId) {
        return new PollVote(1L, optionId, username, LocalDateTime.of(2026, 7, 1, 12, 0));
    }

    private DataCollectionService service(Map<Long, WorldCupMatch> matches, Map<Long, List<PollVote>> votesByOptionId) {
        PredictionRepository predictionRepository = proxy(PredictionRepository.class, Map.of(
                "findAllWithUserAndMatch", List.of()
        ));
        PollVoteRepository pollVoteRepository = proxy(PollVoteRepository.class, Map.of(
                "findByPollIdAndOptionIdOrderByUsernameAsc", new HashMap<>(votesByOptionId)
        ));
        WorldCupMatchRepository matchRepository = proxy(WorldCupMatchRepository.class, Map.of(
                "findById", new HashMap<>(matches)
        ));
        return new DataCollectionService(predictionRepository, pollVoteRepository, matchRepository, new ScoringService());
    }

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> repositoryType, Map<String, Object> responses) {
        return (T) Proxy.newProxyInstance(
                repositoryType.getClassLoader(),
                new Class<?>[]{repositoryType},
                (proxy, method, args) -> {
                    if (method.getName().equals("findAllWithUserAndMatch")) {
                        return responses.get("findAllWithUserAndMatch");
                    }
                    if (method.getName().equals("findById")) {
                        Map<Long, WorldCupMatch> matches = (Map<Long, WorldCupMatch>) responses.get("findById");
                        return Optional.ofNullable(matches.get((Long) args[0]));
                    }
                    if (method.getName().equals("findByPollIdAndOptionIdOrderByUsernameAsc")) {
                        Map<Long, List<PollVote>> votes = (Map<Long, List<PollVote>>) responses.get(
                                "findByPollIdAndOptionIdOrderByUsernameAsc"
                        );
                        return votes.getOrDefault((Long) args[1], List.of());
                    }
                    if (method.getName().equals("toString")) {
                        return repositoryType.getSimpleName() + " test proxy";
                    }
                    throw new UnsupportedOperationException(method.getName());
                }
        );
    }
}
