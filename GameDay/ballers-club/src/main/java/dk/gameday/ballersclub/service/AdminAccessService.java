package dk.gameday.ballersclub.service;

import dk.gameday.ballersclub.model.AppUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminAccessService {

    private final Set<String> adminUsernames;

    public AdminAccessService(@Value("${ballers-club.admin-usernames:Azzuri}") String adminUsernames) {
        this.adminUsernames = Arrays.stream(adminUsernames.split(","))
                .map(String::trim)
                .filter(username -> !username.isBlank())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    public boolean isAdmin(AppUser user) {
        return user != null && adminUsernames.contains(user.getUsername().toLowerCase());
    }
}
