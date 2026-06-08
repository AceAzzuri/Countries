package dk.gameday.ballersclub.model;

import java.util.List;

public record PoolLeaderboardView(
        Pool pool,
        int memberCount,
        List<LeaderboardRow> rows
) {
}
