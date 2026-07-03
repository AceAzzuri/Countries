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
