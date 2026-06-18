package dk.gameday.ballersclub.service;

import dk.gameday.ballersclub.model.ArenaChatMessage;
import dk.gameday.ballersclub.model.AppUser;
import dk.gameday.ballersclub.repository.ArenaChatMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ArenaChatService {

    private final ArenaChatMessageRepository chatMessageRepository;

    public ArenaChatService(ArenaChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    @Transactional(readOnly = true)
    public List<ArenaChatMessage> findLatest() {
        return chatMessageRepository.findTop10ByOrderByCreatedAtDesc();
    }

    @Transactional
    public void postMessage(String message, AppUser user) {
        String cleaned = message == null ? "" : message.trim();
        if (cleaned.isBlank()) {
            throw new IllegalArgumentException("Skriv en kommentar først.");
        }
        if (cleaned.length() > 240) {
            throw new IllegalArgumentException("Kommentarer må højst være 240 tegn.");
        }
        chatMessageRepository.save(new ArenaChatMessage(cleaned, user));
    }

    @Transactional
    public void react(Long messageId, String reaction) {
        ArenaChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Ukendt chatindlæg."));
        message.react(reaction);
        chatMessageRepository.save(message);
    }
}
