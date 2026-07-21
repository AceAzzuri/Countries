package dk.gameday.ballersclub.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "reminder_preferences")
public class ReminderPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;

    private boolean matchdayReminder = true;
    private boolean oneHourReminder = true;

    protected ReminderPreference() {
    }

    public ReminderPreference(AppUser user) {
        this.user = user;
    }

    public void update(boolean matchdayReminder, boolean oneHourReminder) {
        this.matchdayReminder = matchdayReminder;
        this.oneHourReminder = oneHourReminder;
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public boolean isMatchdayReminder() {
        return matchdayReminder;
    }

    public boolean isOneHourReminder() {
        return oneHourReminder;
    }
}
