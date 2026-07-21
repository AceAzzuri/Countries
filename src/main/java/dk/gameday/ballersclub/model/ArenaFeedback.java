package dk.gameday.ballersclub.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "arena_feedback")
public class ArenaFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 600)
    private String message;

    @Column(length = 80)
    private String username;

    @Column(length = 160)
    private String email;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected ArenaFeedback() {
    }

    public ArenaFeedback(String message, AppUser user) {
        this.message = message;
        if (user != null) {
            this.username = user.getUsername();
            this.email = user.getEmail();
        }
    }

    @PrePersist
    void applyDefaults() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
