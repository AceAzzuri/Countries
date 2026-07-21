package dk.gameday.ballersclub.service;

import dk.gameday.ballersclub.model.Prediction;
import dk.gameday.ballersclub.model.WorldCupMatch;
import org.springframework.stereotype.Service;

@Service
public class ScoringService {

    public int calculatePoints(Prediction prediction) {
        WorldCupMatch match = prediction.getMatch();
        if (!match.hasResult()) {
            return 0;
        }
        if (isExactScoreHit(prediction)) {
            return match.isQuarterFinalOrLater() ? 5 : 3;
        }
        return isCorrectOutcomeHit(prediction) ? (match.isQuarterFinalOrLater() ? 3 : 1) : 0;
    }

    public boolean isExactScoreHit(Prediction prediction) {
        WorldCupMatch match = prediction.getMatch();
        return match.hasResult()
                && prediction.getHomeGoals() == match.getHomeScore()
                && prediction.getAwayGoals() == match.getAwayScore();
    }

    public boolean isCorrectOutcomeHit(Prediction prediction) {
        WorldCupMatch match = prediction.getMatch();
        if (!match.hasResult() || isExactScoreHit(prediction)) {
            return false;
        }
        int predictedOutcome = Integer.compare(prediction.getHomeGoals(), prediction.getAwayGoals());
        int actualOutcome = Integer.compare(match.getHomeScore(), match.getAwayScore());
        return predictedOutcome == actualOutcome;
    }
}
