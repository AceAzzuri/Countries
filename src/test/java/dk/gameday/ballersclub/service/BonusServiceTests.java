package dk.gameday.ballersclub.service;

import dk.gameday.ballersclub.model.PollVote;
import dk.gameday.ballersclub.repository.PollVoteRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BonusServiceTests {

    @Test
    void bonusReviewShowsAllAnswersGroupedByOption() {
        PollVoteRepository voteRepository = mock(PollVoteRepository.class);
        when(voteRepository.findByPollIdOrderBySubmittedAtDesc(1L)).thenReturn(List.of(
                new PollVote(1L, 103L, "Azzuri", LocalDateTime.of(2026, 7, 1, 12, 0)),
                new PollVote(1L, 102L, "poll tester", LocalDateTime.of(2026, 7, 1, 12, 0)),
                new PollVote(1L, 103L, "Alejandro", LocalDateTime.of(2026, 7, 1, 12, 0))
        ));
        BonusService bonusService = new BonusService(voteRepository);

        var worldCupWinner = bonusService.getBonusReviewRows().stream()
                .filter(row -> row.question().equals("Hvem vinder VM 2026?"))
                .findFirst()
                .orElseThrow();

        assertThat(worldCupWinner.correctUsernames()).containsExactly("Alejandro", "Azzuri");
        assertThat(worldCupWinner.answerGroups())
                .extracting(group -> group.answer() + ": " + group.usernamesLabel())
                .containsExactly("France: poll tester", "Spain: Alejandro, Azzuri");
    }
}
