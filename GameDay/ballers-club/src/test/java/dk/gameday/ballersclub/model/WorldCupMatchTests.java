package dk.gameday.ballersclub.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

class WorldCupMatchTests {

    @Test
    void predictionClosesAtKickoffInCopenhagenEvenWhenServerRunsUtc() {
        TimeZone originalTimeZone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            LocalDateTime kickoffThirtyMinutesAgoInCopenhagen = LocalDateTime.now(ZoneId.of("Europe/Copenhagen"))
                    .minusMinutes(30);
            WorldCupMatch match = new WorldCupMatch(
                    "Group A",
                    "Mexico",
                    "South Africa",
                    kickoffThirtyMinutesAgoInCopenhagen,
                    "Mexico City Stadium"
            );

            assertThat(match.isPredictionOpen()).isFalse();
        } finally {
            TimeZone.setDefault(originalTimeZone);
        }
    }
}
