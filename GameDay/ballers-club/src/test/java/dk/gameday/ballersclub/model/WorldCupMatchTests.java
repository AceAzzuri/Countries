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

    @Test
    void knockoutPredictionIsOpenBeforeKickoff() {
        WorldCupMatch match = new WorldCupMatch(
                "Round of 32",
                "Group A runner-up",
                "Group B runner-up",
                LocalDateTime.now(ZoneId.of("Europe/Copenhagen")).plusDays(1),
                "Los Angeles Stadium"
        );

        assertThat(match.isPredictionOpen()).isTrue();
        assertThat(match.getStatusLabel()).isEqualTo("Åben");
    }

    @Test
    void knockoutMatchCanStoreAdvancingTeamOnDraw() {
        WorldCupMatch match = new WorldCupMatch(
                "Round of 32",
                "Senegal",
                "Belgium",
                LocalDateTime.now(ZoneId.of("Europe/Copenhagen")).plusDays(1),
                "Seattle Stadium"
        );

        match.updateResult(2, 2, "Belgium");

        assertThat(match.hasResult()).isTrue();
        assertThat(match.isDraw()).isTrue();
        assertThat(match.getAdvancingTeam()).isEqualTo("Belgium");

        match.clearResult();

        assertThat(match.getAdvancingTeam()).isNull();
    }
}
