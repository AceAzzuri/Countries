package dk.gameday.ballersclub.service;

import dk.gameday.ballersclub.model.AppUser;
import dk.gameday.ballersclub.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;

@Service
public class UserService {

    private final AppUserRepository userRepository;

    public UserService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public AppUser login(String rawUsername) {
        String username = normalize(rawUsername);
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseGet(() -> userRepository.save(new AppUser(username)));
    }

    @Transactional
    public AppUser login(String rawUsername, String rawEmail) {
        return login(rawUsername, rawEmail, false);
    }

    @Transactional
    public AppUser login(String rawUsername, String rawEmail, boolean communicationConsent) {
        return signUp(rawUsername, rawEmail, communicationConsent);
    }

    @Transactional
    public AppUser loginExisting(String rawUsername, String rawEmail, boolean communicationConsent) {
        String username = rawUsername == null || rawUsername.isBlank() ? null : normalize(rawUsername);
        String email = normalizeEmail(rawEmail);
        Optional<AppUser> existingUser = Optional.empty();
        if (email != null) {
            existingUser = userRepository.findByEmailIgnoreCase(email);
        }
        if (existingUser.isEmpty() && username != null) {
            existingUser = userRepository.findByUsernameIgnoreCase(username);
        }
        AppUser user = existingUser.orElseThrow(() -> new IllegalArgumentException("Profilen blev ikke fundet. Opret en profil først."));
        if (communicationConsent) {
            user.updateCommunicationConsent(true);
        }
        return user;
    }

    @Transactional
    public AppUser signUp(String rawUsername, String rawEmail, boolean communicationConsent) {
        String username = normalize(rawUsername);
        String email = normalizeEmail(rawEmail);
        if (email != null) {
            Optional<AppUser> existingEmailUser = userRepository.findByEmailIgnoreCase(email);
            if (existingEmailUser.isPresent()) {
                AppUser user = existingEmailUser.get();
                if (communicationConsent) {
                    user.updateCommunicationConsent(true);
                }
                return user;
            }
        }

        String availableUsername = email == null ? username : findAvailableUsername(username);
        Optional<AppUser> existingUsernameUser = userRepository.findByUsernameIgnoreCase(availableUsername);
        AppUser user = existingUsernameUser.orElseGet(() -> userRepository.save(new AppUser(availableUsername, email)));
        if (email != null && user.getEmail() == null) {
            user.updateEmail(email);
        } else if (email != null && !user.getEmail().equalsIgnoreCase(email)) {
            throw new IllegalArgumentException("Brugernavnet findes allerede. Brug en email eller et andet brugernavn.");
        }
        if (communicationConsent) {
            user.updateCommunicationConsent(true);
        }
        return user;
    }

    public Optional<AppUser> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<AppUser> findUsersWithCommunicationConsent() {
        return userRepository.findByCommunicationConsentTrueAndEmailIsNotNullOrderByCreatedAtDesc();
    }

    private String normalize(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Vælg et brugernavn for at komme ind i Arena.");
        }
        String normalized = username.trim().replaceAll("\\s+", " ");
        if (normalized.length() > 40) {
            throw new IllegalArgumentException("Brugernavn må maks være 40 tegn.");
        }
        return normalized;
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        String normalized = email.trim().toLowerCase();
        if (normalized.length() > 120 || !normalized.contains("@")) {
            throw new IllegalArgumentException("Indtast en gyldig email.");
        }
        return normalized;
    }

    private String findAvailableUsername(String username) {
        if (userRepository.findByUsernameIgnoreCase(username).isEmpty()) {
            return username;
        }
        String base = username.length() > 36 ? username.substring(0, 36).trim() : username;
        for (int suffix = 2; suffix < 1000; suffix++) {
            String candidate = base + " " + suffix;
            if (userRepository.findByUsernameIgnoreCase(candidate).isEmpty()) {
                return candidate;
            }
        }
        throw new IllegalArgumentException("Brugernavnet er optaget. Vælg et andet brugernavn.");
    }

    @Transactional
    public void updateCommunicationConsent(AppUser user, boolean communicationConsent) {
        user.updateCommunicationConsent(communicationConsent);
        userRepository.save(user);
    }
}
