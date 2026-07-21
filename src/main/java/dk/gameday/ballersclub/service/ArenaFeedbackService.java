package dk.gameday.ballersclub.service;

import dk.gameday.ballersclub.model.AppUser;
import dk.gameday.ballersclub.model.ArenaFeedback;
import dk.gameday.ballersclub.repository.ArenaFeedbackRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArenaFeedbackService {

    private final ArenaFeedbackRepository feedbackRepository;

    public ArenaFeedbackService(ArenaFeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    public ArenaFeedback save(String message, AppUser user) {
        String cleanedMessage = message == null ? "" : message.trim();
        if (cleanedMessage.isBlank()) {
            throw new IllegalArgumentException("Skriv en kort feedback først.");
        }
        if (cleanedMessage.length() > 600) {
            throw new IllegalArgumentException("Feedback må maks være 600 tegn.");
        }
        return feedbackRepository.save(new ArenaFeedback(cleanedMessage, user));
    }

    public List<ArenaFeedback> findLatest() {
        return feedbackRepository.findTop30ByOrderByCreatedAtDesc();
    }
}
