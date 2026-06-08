package dk.gameday.ballersclub.service;

import dk.gameday.ballersclub.model.WorldCupMatch;
import dk.gameday.ballersclub.repository.PredictionRepository;
import dk.gameday.ballersclub.repository.WorldCupMatchRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final int OFFICIAL_WORLD_CUP_2026_MATCH_COUNT = 104;
    private static final ZoneId DISPLAY_ZONE = ZoneId.of("Europe/Copenhagen");

    private final WorldCupMatchRepository matchRepository;
    private final PredictionRepository predictionRepository;

    public DataInitializer(WorldCupMatchRepository matchRepository, PredictionRepository predictionRepository) {
        this.matchRepository = matchRepository;
        this.predictionRepository = predictionRepository;
    }

    @Override
    public void run(String... args) {
        long matchCount = matchRepository.count();
        if (matchCount == OFFICIAL_WORLD_CUP_2026_MATCH_COUNT) {
            return;
        }
        if (matchCount > 0) {
            predictionRepository.deleteAll();
            matchRepository.deleteAll();
        }

        matchRepository.saveAll(List.of(
                match("Group A", "Mexico", "South Africa", "2026-06-11T19:00:00Z", "Mexico City Stadium"),
                match("Group A", "Korea Republic", "Czechia", "2026-06-12T02:00:00Z", "Guadalajara Stadium"),
                match("Group B", "Canada", "Bosnia and Herzegovina", "2026-06-12T19:00:00Z", "Toronto Stadium"),
                match("Group D", "United States", "Paraguay", "2026-06-13T01:00:00Z", "Los Angeles Stadium"),
                match("Group B", "Qatar", "Switzerland", "2026-06-13T19:00:00Z", "San Francisco Bay Area Stadium"),
                match("Group C", "Brazil", "Morocco", "2026-06-13T22:00:00Z", "New York/New Jersey Stadium"),
                match("Group C", "Haiti", "Scotland", "2026-06-14T01:00:00Z", "Boston Stadium"),
                match("Group D", "Australia", "Türkiye", "2026-06-14T04:00:00Z", "BC Place Vancouver"),
                match("Group E", "Germany", "Curaçao", "2026-06-14T17:00:00Z", "Houston Stadium"),
                match("Group F", "Netherlands", "Japan", "2026-06-14T20:00:00Z", "Dallas Stadium"),
                match("Group E", "Côte d'Ivoire", "Ecuador", "2026-06-14T23:00:00Z", "Philadelphia Stadium"),
                match("Group F", "Sweden", "Tunisia", "2026-06-15T02:00:00Z", "Monterrey Stadium"),
                match("Group H", "Spain", "Cabo Verde", "2026-06-15T16:00:00Z", "Atlanta Stadium"),
                match("Group G", "Belgium", "Egypt", "2026-06-15T19:00:00Z", "Seattle Stadium"),
                match("Group H", "Saudi Arabia", "Uruguay", "2026-06-15T22:00:00Z", "Miami Stadium"),
                match("Group G", "IR Iran", "New Zealand", "2026-06-16T01:00:00Z", "Los Angeles Stadium"),
                match("Group I", "France", "Senegal", "2026-06-16T19:00:00Z", "New York/New Jersey Stadium"),
                match("Group I", "Iraq", "Norway", "2026-06-16T22:00:00Z", "Boston Stadium"),
                match("Group J", "Argentina", "Algeria", "2026-06-17T01:00:00Z", "Kansas City Stadium"),
                match("Group J", "Austria", "Jordan", "2026-06-17T04:00:00Z", "San Francisco Bay Area Stadium"),
                match("Group K", "Portugal", "Congo DR", "2026-06-17T17:00:00Z", "Houston Stadium"),
                match("Group L", "England", "Croatia", "2026-06-17T20:00:00Z", "Dallas Stadium"),
                match("Group L", "Ghana", "Panama", "2026-06-17T23:00:00Z", "Toronto Stadium"),
                match("Group K", "Uzbekistan", "Colombia", "2026-06-18T02:00:00Z", "Mexico City Stadium"),
                match("Group A", "Czechia", "South Africa", "2026-06-18T16:00:00Z", "Atlanta Stadium"),
                match("Group B", "Switzerland", "Bosnia and Herzegovina", "2026-06-18T19:00:00Z", "Los Angeles Stadium"),
                match("Group B", "Canada", "Qatar", "2026-06-18T22:00:00Z", "BC Place Vancouver"),
                match("Group A", "Mexico", "Korea Republic", "2026-06-19T01:00:00Z", "Guadalajara Stadium"),
                match("Group D", "United States", "Australia", "2026-06-19T19:00:00Z", "Seattle Stadium"),
                match("Group C", "Scotland", "Morocco", "2026-06-19T22:00:00Z", "Boston Stadium"),
                match("Group C", "Brazil", "Haiti", "2026-06-20T00:30:00Z", "Philadelphia Stadium"),
                match("Group D", "Türkiye", "Paraguay", "2026-06-20T03:00:00Z", "San Francisco Bay Area Stadium"),
                match("Group F", "Netherlands", "Sweden", "2026-06-20T17:00:00Z", "Houston Stadium"),
                match("Group E", "Germany", "Côte d'Ivoire", "2026-06-20T20:00:00Z", "Toronto Stadium"),
                match("Group E", "Ecuador", "Curaçao", "2026-06-21T00:00:00Z", "Kansas City Stadium"),
                match("Group F", "Tunisia", "Japan", "2026-06-21T04:00:00Z", "Monterrey Stadium"),
                match("Group H", "Spain", "Saudi Arabia", "2026-06-21T16:00:00Z", "Atlanta Stadium"),
                match("Group G", "Belgium", "IR Iran", "2026-06-21T19:00:00Z", "Los Angeles Stadium"),
                match("Group H", "Uruguay", "Cabo Verde", "2026-06-21T22:00:00Z", "Miami Stadium"),
                match("Group G", "New Zealand", "Egypt", "2026-06-22T01:00:00Z", "BC Place Vancouver"),
                match("Group J", "Argentina", "Austria", "2026-06-22T17:00:00Z", "Dallas Stadium"),
                match("Group I", "France", "Iraq", "2026-06-22T21:00:00Z", "Philadelphia Stadium"),
                match("Group I", "Norway", "Senegal", "2026-06-23T00:00:00Z", "New York/New Jersey Stadium"),
                match("Group J", "Jordan", "Algeria", "2026-06-23T03:00:00Z", "San Francisco Bay Area Stadium"),
                match("Group K", "Portugal", "Uzbekistan", "2026-06-23T17:00:00Z", "Houston Stadium"),
                match("Group L", "England", "Ghana", "2026-06-23T20:00:00Z", "Boston Stadium"),
                match("Group L", "Panama", "Croatia", "2026-06-23T23:00:00Z", "Toronto Stadium"),
                match("Group K", "Colombia", "Congo DR", "2026-06-24T02:00:00Z", "Guadalajara Stadium"),
                match("Group B", "Switzerland", "Canada", "2026-06-24T19:00:00Z", "BC Place Vancouver"),
                match("Group B", "Bosnia and Herzegovina", "Qatar", "2026-06-24T19:00:00Z", "Seattle Stadium"),
                match("Group C", "Scotland", "Brazil", "2026-06-24T22:00:00Z", "Miami Stadium"),
                match("Group C", "Morocco", "Haiti", "2026-06-24T22:00:00Z", "Atlanta Stadium"),
                match("Group A", "Czechia", "Mexico", "2026-06-25T01:00:00Z", "Mexico City Stadium"),
                match("Group A", "South Africa", "Korea Republic", "2026-06-25T01:00:00Z", "Monterrey Stadium"),
                match("Group E", "Curaçao", "Côte d'Ivoire", "2026-06-25T20:00:00Z", "Philadelphia Stadium"),
                match("Group E", "Ecuador", "Germany", "2026-06-25T20:00:00Z", "New York/New Jersey Stadium"),
                match("Group F", "Japan", "Sweden", "2026-06-25T23:00:00Z", "Dallas Stadium"),
                match("Group F", "Tunisia", "Netherlands", "2026-06-25T23:00:00Z", "Kansas City Stadium"),
                match("Group D", "Türkiye", "United States", "2026-06-26T02:00:00Z", "Los Angeles Stadium"),
                match("Group D", "Paraguay", "Australia", "2026-06-26T02:00:00Z", "San Francisco Bay Area Stadium"),
                match("Group I", "Norway", "France", "2026-06-26T19:00:00Z", "Boston Stadium"),
                match("Group I", "Senegal", "Iraq", "2026-06-26T19:00:00Z", "Toronto Stadium"),
                match("Group H", "Cabo Verde", "Saudi Arabia", "2026-06-27T00:00:00Z", "Houston Stadium"),
                match("Group H", "Uruguay", "Spain", "2026-06-27T00:00:00Z", "Guadalajara Stadium"),
                match("Group G", "Egypt", "IR Iran", "2026-06-27T03:00:00Z", "Seattle Stadium"),
                match("Group G", "New Zealand", "Belgium", "2026-06-27T03:00:00Z", "BC Place Vancouver"),
                match("Group L", "Panama", "England", "2026-06-27T21:00:00Z", "New York/New Jersey Stadium"),
                match("Group L", "Croatia", "Ghana", "2026-06-27T21:00:00Z", "Philadelphia Stadium"),
                match("Group K", "Colombia", "Portugal", "2026-06-27T23:30:00Z", "Miami Stadium"),
                match("Group K", "Congo DR", "Uzbekistan", "2026-06-27T23:30:00Z", "Atlanta Stadium"),
                match("Group J", "Algeria", "Austria", "2026-06-28T02:00:00Z", "Kansas City Stadium"),
                match("Group J", "Jordan", "Argentina", "2026-06-28T02:00:00Z", "Dallas Stadium"),
                match("Round of 32", "Group A runner-up", "Group B runner-up", "2026-06-28T19:00:00Z", "Los Angeles Stadium"),
                match("Round of 32", "Group C winner", "Group F runner-up", "2026-06-29T17:00:00Z", "Houston Stadium"),
                match("Round of 32", "Group E winner", "Best third-place A/B/C/D/F", "2026-06-29T20:30:00Z", "Boston Stadium"),
                match("Round of 32", "Group F winner", "Group C runner-up", "2026-06-30T01:00:00Z", "Monterrey Stadium"),
                match("Round of 32", "Group E runner-up", "Group I runner-up", "2026-06-30T17:00:00Z", "Dallas Stadium"),
                match("Round of 32", "Group I winner", "Best third-place C/D/F/G/H", "2026-06-30T21:00:00Z", "New York/New Jersey Stadium"),
                match("Round of 32", "Group A winner", "Best third-place C/E/F/H/I", "2026-07-01T01:00:00Z", "Mexico City Stadium"),
                match("Round of 32", "Group L winner", "Best third-place E/H/I/J/K", "2026-07-01T16:00:00Z", "Atlanta Stadium"),
                match("Round of 32", "Group G winner", "Best third-place A/E/H/I/J", "2026-07-01T20:00:00Z", "Seattle Stadium"),
                match("Round of 32", "Group D winner", "Best third-place B/E/F/I/J", "2026-07-02T00:00:00Z", "San Francisco Bay Area Stadium"),
                match("Round of 32", "Group H winner", "Group J runner-up", "2026-07-02T19:00:00Z", "Los Angeles Stadium"),
                match("Round of 32", "Group K runner-up", "Group L runner-up", "2026-07-02T23:00:00Z", "Toronto Stadium"),
                match("Round of 32", "Group B winner", "Best third-place E/F/G/I/J", "2026-07-03T03:00:00Z", "BC Place Vancouver"),
                match("Round of 32", "Group D runner-up", "Group G runner-up", "2026-07-03T18:00:00Z", "Dallas Stadium"),
                match("Round of 32", "Group J winner", "Group H runner-up", "2026-07-03T22:00:00Z", "Miami Stadium"),
                match("Round of 32", "Group K winner", "Best third-place D/E/I/J/L", "2026-07-04T01:30:00Z", "Kansas City Stadium"),
                match("Round of 16", "Winner Match 73", "Winner Match 75", "2026-07-04T17:00:00Z", "Houston Stadium"),
                match("Round of 16", "Winner Match 74", "Winner Match 77", "2026-07-04T21:00:00Z", "Philadelphia Stadium"),
                match("Round of 16", "Winner Match 76", "Winner Match 78", "2026-07-05T20:00:00Z", "New York/New Jersey Stadium"),
                match("Round of 16", "Winner Match 79", "Winner Match 80", "2026-07-06T00:00:00Z", "Mexico City Stadium"),
                match("Round of 16", "Winner Match 83", "Winner Match 84", "2026-07-06T19:00:00Z", "Dallas Stadium"),
                match("Round of 16", "Winner Match 81", "Winner Match 82", "2026-07-07T00:00:00Z", "Seattle Stadium"),
                match("Round of 16", "Winner Match 86", "Winner Match 88", "2026-07-07T16:00:00Z", "Atlanta Stadium"),
                match("Round of 16", "Winner Match 85", "Winner Match 87", "2026-07-07T20:00:00Z", "BC Place Vancouver"),
                match("Quarter-final", "Winner Match 89", "Winner Match 90", "2026-07-09T20:00:00Z", "Boston Stadium"),
                match("Quarter-final", "Winner Match 93", "Winner Match 94", "2026-07-10T19:00:00Z", "Los Angeles Stadium"),
                match("Quarter-final", "Winner Match 91", "Winner Match 92", "2026-07-11T21:00:00Z", "Miami Stadium"),
                match("Quarter-final", "Winner Match 95", "Winner Match 96", "2026-07-12T01:00:00Z", "Kansas City Stadium"),
                match("Semi-final", "Winner Match 97", "Winner Match 98", "2026-07-14T19:00:00Z", "Dallas Stadium"),
                match("Semi-final", "Winner Match 99", "Winner Match 100", "2026-07-15T19:00:00Z", "Atlanta Stadium"),
                match("Play-off for third place", "Runner-up Match 101", "Runner-up Match 102", "2026-07-18T21:00:00Z", "Miami Stadium"),
                match("Final", "Winner Match 101", "Winner Match 102", "2026-07-19T19:00:00Z", "New York/New Jersey Stadium")
        ));
    }

    private static WorldCupMatch match(String roundLabel, String homeTeam, String awayTeam, String kickoffAtUtc, String venue) {
        LocalDateTime kickoffAt = LocalDateTime.ofInstant(Instant.parse(kickoffAtUtc), DISPLAY_ZONE);
        return new WorldCupMatch(roundLabel, homeTeam, awayTeam, kickoffAt, venue);
    }
}
