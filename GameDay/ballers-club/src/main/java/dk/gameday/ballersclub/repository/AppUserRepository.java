package dk.gameday.ballersclub.repository;

import dk.gameday.ballersclub.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsernameIgnoreCase(String username);

    Optional<AppUser> findByEmailIgnoreCase(String email);

    List<AppUser> findByCommunicationConsentTrueAndEmailIsNotNullOrderByCreatedAtDesc();
}
