package dk.gameday.ballersclub.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "pool_memberships",
        uniqueConstraints = @UniqueConstraint(name = "uk_pool_membership_user", columnNames = {"pool_id", "user_id"})
)
public class PoolMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pool_id", nullable = false)
    private Pool pool;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    private LocalDateTime joinedAt;

    protected PoolMembership() {
    }

    public PoolMembership(Pool pool, AppUser user) {
        this.pool = pool;
        this.user = user;
    }

    @PrePersist
    void applyDefaults() {
        if (joinedAt == null) {
            joinedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Pool getPool() {
        return pool;
    }

    public AppUser getUser() {
        return user;
    }
}
