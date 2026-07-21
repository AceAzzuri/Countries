package dk.gameday.ballersclub.service;

import dk.gameday.ballersclub.model.AppUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CurrentUserService {

    public static final String USER_ID_SESSION_KEY = "ballersClubUserId";

    private final UserService userService;

    public CurrentUserService(UserService userService) {
        this.userService = userService;
    }

    public Optional<AppUser> getCurrentUser(HttpSession session) {
        Object userId = session.getAttribute(USER_ID_SESSION_KEY);
        if (!(userId instanceof Long id)) {
            return Optional.empty();
        }
        return userService.findById(id);
    }
}
