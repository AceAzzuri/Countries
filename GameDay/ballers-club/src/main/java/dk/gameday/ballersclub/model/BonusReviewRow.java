package dk.gameday.ballersclub.model;

import java.util.List;

public record BonusReviewRow(
        String question,
        String correctAnswer,
        int points,
        boolean decided,
        List<String> correctUsernames
) {
    public String correctUsernamesLabel() {
        if (!decided) {
            return "Afventer facit";
        }
        if (correctUsernames.isEmpty()) {
            return "Ingen ramte rigtigt";
        }
        return String.join(", ", correctUsernames);
    }
}
