package dk.gameday.ballersclub.repository;

import dk.gameday.ballersclub.model.WorldCupMatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorldCupMatchRepository extends JpaRepository<WorldCupMatch, Long> {

    List<WorldCupMatch> findAllByOrderByKickoffAtAscHomeTeamAsc();
}
