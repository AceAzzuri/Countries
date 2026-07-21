package dk.gameday.ballersclub.service;

import dk.gameday.ballersclub.model.AppUser;
import dk.gameday.ballersclub.model.ReminderPreference;
import dk.gameday.ballersclub.repository.ReminderPreferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReminderPreferenceService {

    private final ReminderPreferenceRepository reminderPreferenceRepository;

    public ReminderPreferenceService(ReminderPreferenceRepository reminderPreferenceRepository) {
        this.reminderPreferenceRepository = reminderPreferenceRepository;
    }

    @Transactional
    public ReminderPreference getOrCreate(AppUser user) {
        return reminderPreferenceRepository.findByUser(user)
                .orElseGet(() -> reminderPreferenceRepository.save(new ReminderPreference(user)));
    }

    @Transactional
    public void update(AppUser user, boolean matchdayReminder, boolean oneHourReminder) {
        ReminderPreference preference = getOrCreate(user);
        preference.update(matchdayReminder, oneHourReminder);
        reminderPreferenceRepository.save(preference);
    }
}
