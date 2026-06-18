package dk.gameday.ballersclub.repository;

import dk.gameday.ballersclub.model.AppUser;
import dk.gameday.ballersclub.model.Prediction;
import dk.gameday.ballersclub.model.WorldCupMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PredictionRepository extends JpaRepository<Prediction, Long> {

    Optional<Prediction> findByUserAndMatch(AppUser user, WorldCupMatch match);

    List<Prediction> findByUserOrderByMatchKickoffAtAsc(AppUser user);

    @Query("""
            select prediction
            from Prediction prediction
            join fetch prediction.user user
            join fetch prediction.match match
            order by match.kickoffAt asc, user.username asc
            """)
    List<Prediction> findAllForLeaderboard();

    @Query("""
            select prediction
            from Prediction prediction
            join fetch prediction.user user
            join fetch prediction.match match
            order by match.kickoffAt asc, user.username asc
            """)
    List<Prediction> findAllWithUserAndMatch();

    @Query("""
            select prediction
            from Prediction prediction
            join fetch prediction.user user
            join fetch prediction.match match
            where prediction.updatedAt is not null
              and prediction.updatedAt > match.kickoffAt
            order by prediction.updatedAt desc, user.username asc
            """)
    List<Prediction> findLateUpdates();
}
