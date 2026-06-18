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
@Table(name = "arena_chat_messages")
public class ArenaChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 80)
    private String username;

    @Column(nullable = false, length = 240)
    private String message;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private int likes;

    @Column(nullable = false)
    private int fireReactions;

    @Column(nullable = false)
    private int poopReactions;

    protected ArenaChatMessage() {
    }

    public ArenaChatMessage(String message, AppUser user) {
        this.message = message;
        if (user != null) {
            this.username = user.getUsername();
        }
    }

    @PrePersist
    void applyDefaults() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public void react(String reaction) {
        switch (reaction == null ? "" : reaction.trim().toLowerCase()) {
            case "like" -> likes++;
            case "fire" -> fireReactions++;
            case "poop" -> poopReactions++;
            default -> throw new IllegalArgumentException("Ukendt reaktion.");
        }
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public int getLikes() {
        return likes;
    }

    public int getFireReactions() {
        return fireReactions;
    }

    public int getPoopReactions() {
        return poopReactions;
    }
}
