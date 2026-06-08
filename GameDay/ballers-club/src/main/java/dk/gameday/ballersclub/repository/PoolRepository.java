package dk.gameday.ballersclub.repository;

import dk.gameday.ballersclub.model.Pool;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PoolRepository extends JpaRepository<Pool, Long> {

    Optional<Pool> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);
}
