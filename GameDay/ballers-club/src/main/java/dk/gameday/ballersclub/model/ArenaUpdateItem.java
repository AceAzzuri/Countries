package dk.gameday.ballersclub.model;

import java.time.LocalDateTime;

public record ArenaUpdateItem(
        String kind,
        String headline,
        String detail,
        LocalDateTime occurredAt
) {
}
