package dk.gameday.ballersclub.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(
        name = "predictions",
        uniqueConstraints = @UniqueConstraint(name = "uk_prediction_user_match", columnNames = {"user_id", "match_id"})
)
public class Prediction {

    private static final ZoneId DISPLAY_ZONE = ZoneId.of("Europe/Copenhagen");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private WorldCupMatch match;

    private int homeGoals;
    private int awayGoals;
    private LocalDateTime updatedAt;

    protected Prediction() {
    }

    public Prediction(AppUser user, WorldCupMatch match, int homeGoals, int awayGoals) {
        this.user = user;
        this.match = match;
        updateScore(homeGoals, awayGoals);
    }

    public void updateScore(int homeGoals, int awayGoals) {
        this.homeGoals = homeGoals;
        this.awayGoals = awayGoals;
        this.updatedAt = LocalDateTime.now(DISPLAY_ZONE);
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public WorldCupMatch getMatch() {
        return match;
    }

    public int getHomeGoals() {
        return homeGoals;
    }

    public int getAwayGoals() {
        return awayGoals;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
