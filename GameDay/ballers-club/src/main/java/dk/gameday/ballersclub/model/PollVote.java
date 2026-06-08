package dk.gameday.ballersclub.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Entity
@Table(
        name = "poll_votes",
        uniqueConstraints = @UniqueConstraint(name = "uk_poll_vote_user", columnNames = {"poll_id", "username"})
)
public class PollVote {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("d. MMM yyyy HH:mm", Locale.ENGLISH);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "poll_id", nullable = false)
    private Long pollId;

    @Column(name = "option_id", nullable = false)
    private Long optionId;

    @Column(nullable = false, length = 80)
    private String username;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    protected PollVote() {
    }

    public PollVote(Long pollId, Long optionId, String username, LocalDateTime submittedAt) {
        this.pollId = pollId;
        this.optionId = optionId;
        this.username = username;
        this.submittedAt = submittedAt;
    }

    @PrePersist
    void applyDefaults() {
        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
    }

    public Long getPollId() {
        return pollId;
    }

    public Long getOptionId() {
        return optionId;
    }

    public String getUsername() {
        return username;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public String getSubmittedAtLabel() {
        return submittedAt.format(TIMESTAMP_FORMAT);
    }
}
