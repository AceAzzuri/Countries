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
        if (prediction.getHomeGoals() == match.getHomeScore()
                && prediction.getAwayGoals() == match.getAwayScore()) {
            return 3;
        }
        int predictedOutcome = Integer.compare(prediction.getHomeGoals(), prediction.getAwayGoals());
        int actualOutcome = Integer.compare(match.getHomeScore(), match.getAwayScore());
        return predictedOutcome == actualOutcome ? 1 : 0;
    }
}
