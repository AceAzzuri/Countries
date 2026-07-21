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

        assertThatThrownBy(() -> pollService.vote(3L, 302L, "Azzuri"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Award-indtastning er lukket");
        verify(voteRepository, never()).save(org.mockito.Mockito.argThat(vote -> vote.getPollId().equals(3L) && vote.getOptionId().equals(302L)));
    }

    @Test
    void freshPollCannotBeChangedAfterVote() {
        PollVoteRepository voteRepository = mock(PollVoteRepository.class);
        PollService pollService = new PollService(voteRepository);

        assertThatThrownBy(() -> pollService.vote(7L, 702L, "Azzuri"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Award-indtastning er lukket");

        verify(voteRepository, never()).save(any(dk.gameday.ballersclub.model.PollVote.class));
    }

    @Test
    void legacyAwardPollCannotBeChangedAfterAwardsClose() {
        PollVoteRepository voteRepository = mock(PollVoteRepository.class);
        PollService pollService = new PollService(voteRepository);

        assertThatThrownBy(() -> pollService.vote(1L, 102L, "Azzuri"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Award-indtastning er lukket");

        verify(voteRepository, never()).save(any(dk.gameday.ballersclub.model.PollVote.class));
    }

    @Test
    void clickingSameLegacyAwardOptionIsBlockedAfterAwardsClose() {
        PollVoteRepository voteRepository = mock(PollVoteRepository.class);
        PollService pollService = new PollService(voteRepository);
        var franceVote = new dk.gameday.ballersclub.model.PollVote(1L, 102L, "Azzuri", LocalDateTime.of(2026, 7, 2, 12, 0));

        assertThatThrownBy(() -> pollService.vote(1L, 102L, "Azzuri"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Award-indtastning er lukket");
        assertThat(franceVote.isChangedAfterQuarterFinal()).isFalse();
        assertThat(franceVote.getOptionId()).isEqualTo(102L);
        verify(voteRepository, never()).save(franceVote);
    }

    @Test
    void legacyAwardVoteEnteredAfterReopenIsShownAsTwoPointLateVote() {
        PollVoteRepository voteRepository = mock(PollVoteRepository.class);
        PollService pollService = new PollService(voteRepository);
        var lateVote = new dk.gameday.ballersclub.model.PollVote(4L, 406L, "Azzuri", LocalDateTime.of(2026, 7, 3, 12, 0));

        when(voteRepository.findByPollIdOrderBySubmittedAtDesc(4L)).thenReturn(List.of(lateVote));
        when(voteRepository.findByPollIdAndUsernameIgnoreCase(4L, "Azzuri")).thenReturn(Optional.of(lateVote));

        var goldenGlove = pollService.getActivePollViews("Azzuri").stream()
                .filter(view -> view.getPoll().getId().equals(4L))
                .findFirst()
                .orElseThrow();

        assertThat(goldenGlove.isLegacyLateVote()).isTrue();
        assertThat(goldenGlove.isLegacyOriginalVote()).isFalse();
    }

    @Test
    void legacyAwardVoteEnteredBeforeReopenIsShownAsOriginalFivePointVote() {
        PollVoteRepository voteRepository = mock(PollVoteRepository.class);
        PollService pollService = new PollService(voteRepository);
        var oldVote = new dk.gameday.ballersclub.model.PollVote(4L, 406L, "Azzuri", LocalDateTime.of(2026, 7, 2, 12, 0));

        when(voteRepository.findByPollIdOrderBySubmittedAtDesc(4L)).thenReturn(List.of(oldVote));
        when(voteRepository.findByPollIdAndUsernameIgnoreCase(4L, "Azzuri")).thenReturn(Optional.of(oldVote));

        var goldenGlove = pollService.getActivePollViews("Azzuri").stream()
                .filter(view -> view.getPoll().getId().equals(4L))
                .findFirst()
                .orElseThrow();

        assertThat(goldenGlove.isLegacyOriginalVote()).isTrue();
        assertThat(goldenGlove.isLegacyLateVote()).isFalse();
    }
}
