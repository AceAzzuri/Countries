package dk.gameday.ballersclub.service;

import dk.gameday.ballersclub.model.WorldCupMatch;
import dk.gameday.ballersclub.repository.WorldCupMatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MatchResultService {

    private final WorldCupMatchRepository matchRepository;
    private final DataInitializer dataInitializer;

    public MatchResultService(WorldCupMatchRepository matchRepository, DataInitializer dataInitializer) {
        this.matchRepository = matchRepository;
        this.dataInitializer = dataInitializer;
    }

    @Transactional
    public void updateResult(Long matchId, String homeScoreValue, String awayScoreValue) {
        updateResult(matchId, homeScoreValue, awayScoreValue, null);
    }

    @Transactional
    public void updateResult(Long matchId, String homeScoreValue, String awayScoreValue, String advancingTeamValue) {
        WorldCupMatch match = findMatch(matchId);
        String advancingTeam = resolveAdvancingTeam(match, homeScoreValue, awayScoreValue, advancingTeamValue);
        if (isBlank(homeScoreValue) && isBlank(awayScoreValue)) {
            match.updateResult(null, null, advancingTeam);
        } else {
            int homeScore = parseScore(homeScoreValue);
            int awayScore = parseScore(awayScoreValue);
            match.updateResult(homeScore, awayScore, advancingTeam);
        }
        matchRepository.save(match);
        dataInitializer.syncKnockoutFixtures();
    }

    @Transactional
    public void clearResult(Long matchId) {
        WorldCupMatch match = findMatch(matchId);
        match.clearResult();
        matchRepository.save(match);
        dataInitializer.syncKnockoutFixtures();
    }

    @Transactional
    public int updateResults(List<Long> matchIds, List<String> homeScoreValues, List<String> awayScoreValues, List<String> advancingTeamValues) {
        if (matchIds == null || homeScoreValues == null || awayScoreValues == null || advancingTeamValues == null
                || matchIds.size() != homeScoreValues.size()
                || matchIds.size() != awayScoreValues.size()
                || matchIds.size() != advancingTeamValues.size()) {
            throw new IllegalArgumentException("Kunne ikke læse kampene. Prøv at gemme igen.");
        }

        int saved = 0;
        for (int i = 0; i < matchIds.size(); i++) {
            String home = homeScoreValues.get(i);
            String away = awayScoreValues.get(i);
            String advancingTeam = advancingTeamValues.get(i);
            boolean homeBlank = home == null || home.isBlank();
            boolean awayBlank = away == null || away.isBlank();
            boolean advancingBlank = advancingTeam == null || advancingTeam.isBlank();
            if (homeBlank && awayBlank && advancingBlank) {
                continue;
            }
            if (homeBlank != awayBlank) {
                throw new IllegalArgumentException("Indtast begge resultater for hver kamp, du vil gemme.");
            }
            WorldCupMatch match = findMatch(matchIds.get(i));
            String resolvedAdvancingTeam = resolveAdvancingTeam(match, home, away, advancingTeam);
            int homeScore = parseScore(home);
            int awayScore = parseScore(away);
            match.updateResult(homeScore, awayScore, resolvedAdvancingTeam);
            matchRepository.save(match);
            saved++;
        }
        if (saved == 0) {
            throw new IllegalArgumentException("Der var ingen udfyldte resultater at gemme.");
        }
        dataInitializer.syncKnockoutFixtures();
        return saved;
    }

    private String resolveAdvancingTeam(WorldCupMatch match, String homeScoreValue, String awayScoreValue, String advancingTeamValue) {
        if (match.isGroupMatch()) {
            return null;
        }

        boolean homeBlank = isBlank(homeScoreValue);
        boolean awayBlank = isBlank(awayScoreValue);
        if (homeBlank != awayBlank) {
            throw new IllegalArgumentException("Indtast begge resultater eller lad begge stå tomme, når du kun vælger hvem der går videre.");
        }

        if (!homeBlank) {
            int homeScore = parseScore(homeScoreValue);
            int awayScore = parseScore(awayScoreValue);
            if (homeScore != awayScore) {
                return homeScore > awayScore ? match.getHomeTeam() : match.getAwayTeam();
            }
        }

        String advancingTeam = advancingTeamValue == null ? null : advancingTeamValue.trim();
        if (advancingTeam == null || advancingTeam.isBlank()) {
            throw new IllegalArgumentException("Vælg hvem der går videre, når kampen ender uafgjort eller voides.");
        }
        if (!advancingTeam.equals(match.getHomeTeam()) && !advancingTeam.equals(match.getAwayTeam())) {
            throw new IllegalArgumentException("Vælg et af de to hold, der faktisk spiller kampen.");
        }
        return advancingTeam;
    }

    private WorldCupMatch findMatch(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Ukendt kamp."));
    }

    private int parseScore(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Indtast begge resultater.");
        }
        try {
            int score = Integer.parseInt(value.trim());
            if (score < 0 || score > 30) {
                throw new IllegalArgumentException("Resultater skal være mellem 0 og 30.");
            }
            return score;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Resultater skal være hele tal.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
