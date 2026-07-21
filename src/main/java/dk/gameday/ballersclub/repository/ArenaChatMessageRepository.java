package dk.gameday.ballersclub.repository;

import dk.gameday.ballersclub.model.ArenaChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArenaChatMessageRepository extends JpaRepository<ArenaChatMessage, Long> {

    List<ArenaChatMessage> findTop10ByOrderByCreatedAtDesc();
}
