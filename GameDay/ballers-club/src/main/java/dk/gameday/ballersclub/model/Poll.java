package dk.gameday.ballersclub.model;

import java.util.List;

public class Poll {

    private final Long id;
    private final PollCategory category;
    private final String question;
    private final boolean active;
    private final String availabilityNote;
    private final List<PollOption> options;

    public Poll(Long id, PollCategory category, String question, boolean active, String availabilityNote, List<PollOption> options) {
        this.id = id;
        this.category = category;
        this.question = question;
        this.active = active;
        this.availabilityNote = availabilityNote;
        this.options = List.copyOf(options);
    }

    public Long getId() {
        return id;
    }

    public PollCategory getCategory() {
        return category;
    }

    public String getQuestion() {
        return question;
    }

    public boolean isActive() {
        return active;
    }

    public String getAvailabilityNote() {
        return availabilityNote;
    }

    public List<PollOption> getOptions() {
        return options;
    }

    public boolean isLegacyAdjustableAwardPoll() {
        return id != null && (id == 1L || id == 2L || id == 4L);
    }

    public boolean isFreshAwardPoll() {
        return id != null && (id == 7L || id == 10L);
    }
}
