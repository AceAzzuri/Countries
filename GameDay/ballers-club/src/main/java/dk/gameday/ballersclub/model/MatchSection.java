package dk.gameday.ballersclub.model;

import java.util.List;

public record MatchSection(
        String title,
        String subtitle,
        boolean open,
        List<WorldCupMatch> matches
) {
}
