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
    }
}
