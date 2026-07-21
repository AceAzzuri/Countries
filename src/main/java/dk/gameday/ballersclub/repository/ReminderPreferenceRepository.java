package dk.gameday.ballersclub.repository;

import dk.gameday.ballersclub.model.AppUser;
import dk.gameday.ballersclub.model.ReminderPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReminderPreferenceRepository extends JpaRepository<ReminderPreference, Long> {

    Optional<ReminderPreference> findByUser(AppUser user);
}
