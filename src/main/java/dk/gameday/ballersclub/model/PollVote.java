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
    private static final LocalDateTime LEGACY_AWARD_REOPENED_AT = LocalDateTime.of(2026, 7, 3, 0, 0);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "poll_id", nullable = false)
    private Long pollId;

    @Column(name = "option_id", nullable = false)
    private Long optionId;

    @Column(name = "original_option_id")
    private Long originalOptionId;

    @Column(nullable = false)
    private boolean changedAfterQuarterFinal;

    @Column(name = "original_vote_before_reopen", nullable = false)
    private boolean originalVoteBeforeReopen;

    @Column(nullable = false, length = 80)
    private String username;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    protected PollVote() {
    }

    public PollVote(Long pollId, Long optionId, String username, LocalDateTime submittedAt) {
        this.pollId = pollId;
        this.optionId = optionId;
        this.originalOptionId = optionId;
        this.username = username;
        this.submittedAt = submittedAt;
        this.originalVoteBeforeReopen = submittedAt != null && submittedAt.isBefore(LEGACY_AWARD_REOPENED_AT);
    }

    public void updateOption(Long optionId, LocalDateTime submittedAt) {
        if (this.originalOptionId == null) {
            this.originalOptionId = this.optionId;
        }
        if (this.optionId.equals(optionId)) {
            return;
        }
        if (!this.optionId.equals(optionId)) {
            this.changedAfterQuarterFinal = true;
        }
        this.optionId = optionId;
        this.submittedAt = submittedAt;
    }

    @PrePersist
    void applyDefaults() {
        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
        if (originalOptionId == null) {
            originalOptionId = optionId;
        }
    }

    public Long getPollId() {
        return pollId;
    }

    public Long getOptionId() {
        return optionId;
    }

    public Long getOriginalOptionId() {
        return originalOptionId == null ? optionId : originalOptionId;
    }

    public boolean isChangedAfterQuarterFinal() {
        return changedAfterQuarterFinal;
    }

    public boolean isOriginalVoteBeforeReopen() {
        return originalVoteBeforeReopen;
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
