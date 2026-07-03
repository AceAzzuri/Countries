package dk.gameday.ballersclub.service;

import dk.gameday.ballersclub.repository.PollVoteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PollServiceTests {

    @Test
    void activePollViewsStillRenderWhenVoteLookupFails() {
        PollVoteRepository voteRepository = mock(PollVoteRepository.class);
        when(voteRepository.findByPollIdOrderBySubmittedAtDesc(1L))
                .thenThrow(new DataAccessResourceFailureException("boom"));
        when(voteRepository.findByPollIdAndUsernameIgnoreCase(1L, "Azzuri"))
                .thenThrow(new DataAccessResourceFailureException("boom"));

        PollService pollService = new PollService(voteRepository);

        var views = pollService.getActivePollViews("Azzuri");

        assertThat(views).isNotEmpty();
        assertThat(views.stream().map(view -> view.getTotalVotes())).allMatch(total -> total == 0);
        assertThat(views.stream().map(view -> view.getOptionResults()))
                .allMatch(results -> results.stream().allMatch(result -> result.getPercentage() == 0));
    }
}
