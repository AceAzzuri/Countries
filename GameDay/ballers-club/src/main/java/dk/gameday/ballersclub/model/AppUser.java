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
@Table(name = "users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String username;

    @Column(unique = true)
    private String googleId;

    @Column(unique = true)
    private String email;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean communicationConsent;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected AppUser() {
    }

    public AppUser(String username) {
        this.username = username;
    }

    public AppUser(String username, String email) {
        this.username = username;
        this.email = email;
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

    public String getUsername() {
        return username;
    }

    public String getGoogleId() {
        return googleId;
    }

    public String getEmail() {
        return email;
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public boolean hasCommunicationConsent() {
        return communicationConsent;
    }

    public boolean isCommunicationConsent() {
        return communicationConsent;
    }

    public void updateCommunicationConsent(boolean communicationConsent) {
        this.communicationConsent = communicationConsent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
