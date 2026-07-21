package dk.gameday.ballersclub.model;

import java.util.List;

public record BonusReviewRow(
        String question,
        String correctAnswer,
        int points,
        boolean decided,
        boolean pointScored,
        List<String> correctUsernames,
        List<AnswerGroup> answerGroups
) {
    public String correctUsernamesLabel() {
        if (!decided) {
            return "Ikke sat";
        }
        if (!pointScored) {
            return "Ikke pointgivet";
        }
        if (correctUsernames.isEmpty()) {
            return "Ingen ramte rigtigt";
        }
        return String.join(", ", correctUsernames);
    }

    public record AnswerGroup(String answer, List<String> usernames) {
        public String usernamesLabel() {
            if (usernames.isEmpty()) {
                return "Ingen";
            }
            return String.join(", ", usernames);
        }
    }
}
