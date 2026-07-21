package dk.gameday.ballersclub.repository;

import dk.gameday.ballersclub.model.ArenaFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArenaFeedbackRepository extends JpaRepository<ArenaFeedback, Long> {

    List<ArenaFeedback> findTop30ByOrderByCreatedAtDesc();
}
