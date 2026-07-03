package dk.gameday.ballersclub.service;

import dk.gameday.ballersclub.repository.PollVoteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

    @Test
    void fixedPointPollCannotBeChangedAfterVote() {
        PollVoteRepository voteRepository = mock(PollVoteRepository.class);
        PollService pollService = new PollService(voteRepository);

        when(voteRepository.findByPollIdAndUsernameIgnoreCase(3L, "Azzuri"))
                .thenReturn(Optional.of(new dk.gameday.ballersclub.model.PollVote(3L, 301L, "Azzuri", LocalDateTime.now())));

        assertThatThrownBy(() -> pollService.vote(3L, 302L, "Azzuri"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Du har allerede stemt");
        verify(voteRepository, never()).save(org.mockito.Mockito.argThat(vote -> vote.getPollId().equals(3L) && vote.getOptionId().equals(302L)));
    }

    @Test
    void freshPollCannotBeChangedAfterVote() {
        PollVoteRepository voteRepository = mock(PollVoteRepository.class);
        PollService pollService = new PollService(voteRepository);

        when(voteRepository.findByPollIdAndUsernameIgnoreCase(7L, "Azzuri"))
                .thenReturn(Optional.of(new dk.gameday.ballersclub.model.PollVote(7L, 701L, "Azzuri", LocalDateTime.now())));

        assertThatThrownBy(() -> pollService.vote(7L, 702L, "Azzuri"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Du har allerede stemt");

        verify(voteRepository, never()).save(any(dk.gameday.ballersclub.model.PollVote.class));
    }

    @Test
    void legacyAwardPollCanBeChangedAfterVote() {
        PollVoteRepository voteRepository = mock(PollVoteRepository.class);
        PollService pollService = new PollService(voteRepository);

        when(voteRepository.findByPollIdAndUsernameIgnoreCase(1L, "Azzuri"))
                .thenReturn(Optional.of(new dk.gameday.ballersclub.model.PollVote(1L, 101L, "Azzuri", LocalDateTime.now())));

        pollService.vote(1L, 102L, "Azzuri");

        verify(voteRepository).save(org.mockito.Mockito.argThat(vote -> vote.getPollId().equals(1L) && vote.getOptionId().equals(102L)));
    }
}
