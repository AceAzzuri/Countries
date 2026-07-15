package dk.gameday.ballersclub.repository;

import dk.gameday.ballersclub.model.PollVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PollVoteRepository extends JpaRepository<PollVote, Long> {

    List<PollVote> findByPollIdOrderBySubmittedAtDesc(Long pollId);

    List<PollVote> findByPollIdAndOptionIdOrderByUsernameAsc(Long pollId, Long optionId);

    Optional<PollVote> findByPollIdAndUsernameIgnoreCase(Long pollId, String username);
}
