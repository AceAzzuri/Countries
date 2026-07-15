package dk.gameday.ballersclub.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class PollVoteSchemaMigrator implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public PollVoteSchemaMigrator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("alter table poll_votes add column if not exists original_option_id bigint");
        jdbcTemplate.execute("alter table poll_votes add column if not exists changed_after_quarter_final boolean not null default false");
        jdbcTemplate.execute("alter table poll_votes add column if not exists original_vote_before_reopen boolean not null default false");
        jdbcTemplate.update("update poll_votes set original_option_id = option_id where original_option_id is null");
        jdbcTemplate.update("""
                update poll_votes
                set original_vote_before_reopen = true
                where poll_id in (1, 2, 4)
                  and submitted_at < timestamp '2026-07-03 00:00:00'
                """);
        restoreAzzuriGoldenGloveOriginalPick();
        reopenHyzzPlayerOfTournamentPick();
        backfillAzzuriFranceMoroccoPrediction();
        backfillLateQuarterFinalPredictions();
        backfillAzzuriSpainSemiFinalPrediction();
    }

    private void restoreAzzuriGoldenGloveOriginalPick() {
        String fixId = "restore-azzuri-golden-glove-emiliano-martinez";
        if (isFixApplied(fixId)) {
            return;
        }

        int updated = jdbcTemplate.update("""
                update poll_votes
                set option_id = 407,
                    original_option_id = 407,
                    changed_after_quarter_final = false,
                    original_vote_before_reopen = true
                where poll_id = 4
                  and lower(username) = lower('Azzuri')
                """);
        if (updated > 0) {
            jdbcTemplate.update("insert into data_fixes (id, applied_at) values (?, current_timestamp)", fixId);
        }
    }

    private void reopenHyzzPlayerOfTournamentPick() {
        String fixId = "reopen-hyzz-player-of-tournament";
        if (isFixApplied(fixId)) {
            return;
        }

        int deleted = jdbcTemplate.update("""
                delete from poll_votes
                where poll_id = 7
                  and lower(username) = lower('Hyzz')
                """);
        if (deleted > 0) {
            jdbcTemplate.update("insert into data_fixes (id, applied_at) values (?, current_timestamp)", fixId);
        }
    }

    private void backfillAzzuriFranceMoroccoPrediction() {
        String fixId = "backfill-azzuri-morocco-france-quarter-final-1-2";
        if (isFixApplied(fixId)) {
            return;
        }
        if (!tableExists("predictions") || !tableExists("users") || !tableExists("matches")) {
            return;
        }

        int updated = jdbcTemplate.update("""
                update predictions
                set home_goals = 1,
                    away_goals = 2,
                    updated_at = current_timestamp
                where user_id = (
                        select id
                        from users
                        where lower(username) = lower('Azzuri')
                    )
                  and match_id = (
                        select id
                        from matches
                        where id = 97
                          and lower(home_team) = lower('Morocco')
                          and lower(away_team) = lower('France')
                          and home_score is null
                          and away_score is null
                    )
                """);

        int inserted = 0;
        if (updated == 0) {
            inserted = jdbcTemplate.update("""
                    insert into predictions (user_id, match_id, home_goals, away_goals, updated_at)
                    select users.id, matches.id, 1, 2, current_timestamp
                    from users
                    cross join matches
                    where lower(users.username) = lower('Azzuri')
                      and matches.id = 97
                      and lower(matches.home_team) = lower('Morocco')
                      and lower(matches.away_team) = lower('France')
                      and matches.home_score is null
                      and matches.away_score is null
                      and not exists (
                          select 1
                          from predictions existing
                          where existing.user_id = users.id
                            and existing.match_id = matches.id
                      )
                    """);
        }

        if (updated + inserted > 0) {
            jdbcTemplate.update("insert into data_fixes (id, applied_at) values (?, current_timestamp)", fixId);
        }
    }

    private void backfillLateQuarterFinalPredictions() {
        backfillPredictionByMatchId("backfill-halim-france-morocco-quarter-final-2-1", "Halim", 97, 1, 2);
        backfillPredictionByMatchId("backfill-azzuri-france-morocco-quarter-final-2-1", "Azzuri", 97, 1, 2);
        backfillPredictionByMatchId("backfill-azzuri-argentina-switzerland-quarter-final-1-1", "Azzuri", 100, 1, 1);
        backfillPredictionByMatchId("backfill-azzuri-england-norway-quarter-final-2-1", "Azzuri", 99, 1, 2);
        backfillPredictionByMatchId("backfill-azzuri-spain-belgium-quarter-final-2-0", "Azzuri", 98, 2, 0);
    }

    private void backfillAzzuriSpainSemiFinalPrediction() {
        backfillPredictionByMatchId(
                "backfill-azzuri-spain-semi-final-2-1-before-2026-07-14",
                "Azzuri",
                101,
                1,
                2,
                "Semi-final",
                "timestamp '2026-07-13 12:00:00'"
        );
    }

    private void backfillPredictionByMatchId(String fixId, String username, long matchId, int homeGoals, int awayGoals) {
        backfillPredictionByMatchId(fixId, username, matchId, homeGoals, awayGoals, "Quarter-final", "current_timestamp");
    }

    private void backfillPredictionByMatchId(
            String fixId,
            String username,
            long matchId,
            int homeGoals,
            int awayGoals,
            String roundLabel,
            String updatedAtExpression
    ) {
        if (isFixApplied(fixId)) {
            return;
        }
        if (!tableExists("predictions") || !tableExists("users") || !tableExists("matches")) {
            return;
        }

        int updated = jdbcTemplate.update("""
                update predictions
                set home_goals = ?,
                    away_goals = ?,
                    updated_at = %s
                where user_id = (
                        select id
                        from users
                        where lower(username) = lower(?)
                    )
                  and match_id = (
                        select id
                        from matches
                        where id = ?
                          and lower(round_label) = lower(?)
                    )
                """.formatted(updatedAtExpression),
                homeGoals, awayGoals, username, matchId, roundLabel);

        int inserted = 0;
        if (updated == 0) {
            inserted = jdbcTemplate.update("""
                    insert into predictions (user_id, match_id, home_goals, away_goals, updated_at)
                    select users.id,
                           matches.id,
                           ?,
                           ?,
                           %s
                    from users
                    cross join matches
                    where lower(users.username) = lower(?)
                      and matches.id = ?
                      and lower(matches.round_label) = lower(?)
                      and not exists (
                          select 1
                          from predictions existing
                          where existing.user_id = users.id
                            and existing.match_id = matches.id
                      )
                    """.formatted(updatedAtExpression),
                    homeGoals, awayGoals, username, matchId, roundLabel);
        }

        if (updated + inserted > 0) {
            jdbcTemplate.update("insert into data_fixes (id, applied_at) values (?, current_timestamp)", fixId);
        }
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables where lower(table_name) = lower(?)",
                Integer.class,
                tableName
        );
        return count != null && count > 0;
    }

    private boolean isFixApplied(String fixId) {
        jdbcTemplate.execute("""
                create table if not exists data_fixes (
                    id varchar(120) primary key,
                    applied_at timestamp not null
                )
                """);
        Integer alreadyApplied = jdbcTemplate.queryForObject(
                "select count(*) from data_fixes where id = ?",
                Integer.class,
                fixId
        );
        return alreadyApplied != null && alreadyApplied > 0;
    }
}
