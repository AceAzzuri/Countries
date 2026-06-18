package dk.gameday.ballersclub.service;

import dk.gameday.ballersclub.model.WorldCupMatch;
import dk.gameday.ballersclub.repository.WorldCupMatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MatchResultService {

    private final WorldCupMatchRepository matchRepository;

    public MatchResultService(WorldCupMatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    @Transactional
    public void updateResult(Long matchId, String homeScoreValue, String awayScoreValue) {
        WorldCupMatch match = findMatch(matchId);
        int homeScore = parseScore(homeScoreValue);
        int awayScore = parseScore(awayScoreValue);
        match.updateResult(homeScore, awayScore);
        matchRepository.save(match);
    }

    @Transactional
    public void clearResult(Long matchId) {
        WorldCupMatch match = findMatch(matchId);
        match.clearResult();
        matchRepository.save(match);
    }

    @Transactional
    public int updateResults(List<Long> matchIds, List<String> homeScoreValues, List<String> awayScoreValues) {
        if (matchIds == null || homeScoreValues == null || awayScoreValues == null
                || matchIds.size() != homeScoreValues.size() || matchIds.size() != awayScoreValues.size()) {
            throw new IllegalArgumentException("Kunne ikke læse kampene. Prøv at gemme igen.");
        }

        int saved = 0;
        for (int i = 0; i < matchIds.size(); i++) {
            String home = homeScoreValues.get(i);
            String away = awayScoreValues.get(i);
            boolean homeBlank = home == null || home.isBlank();
            boolean awayBlank = away == null || away.isBlank();
            if (homeBlank && awayBlank) {
                continue;
            }
            if (homeBlank || awayBlank) {
                throw new IllegalArgumentException("Indtast begge resultater for hver kamp, du vil gemme.");
            }
            updateResult(matchIds.get(i), home, away);
            saved++;
        }
        if (saved == 0) {
            throw new IllegalArgumentException("Der var ingen udfyldte resultater at gemme.");
        }
        return saved;
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
}
