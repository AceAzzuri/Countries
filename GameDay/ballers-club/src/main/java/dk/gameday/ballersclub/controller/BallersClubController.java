package dk.gameday.ballersclub.controller;

import dk.gameday.ballersclub.model.AppUser;
import dk.gameday.ballersclub.model.LeaderboardRow;
import dk.gameday.ballersclub.model.PollView;
import dk.gameday.ballersclub.service.AdminAccessService;
import dk.gameday.ballersclub.service.ArenaChatService;
import dk.gameday.ballersclub.service.ArenaUpdateService;
import dk.gameday.ballersclub.service.ArenaFeedbackService;
import dk.gameday.ballersclub.service.BonusService;
import dk.gameday.ballersclub.service.CurrentUserService;
import dk.gameday.ballersclub.service.DataCollectionService;
import dk.gameday.ballersclub.service.LeaderboardService;
import dk.gameday.ballersclub.service.MatchResultService;
import dk.gameday.ballersclub.service.PoolService;
import dk.gameday.ballersclub.service.PollService;
import dk.gameday.ballersclub.service.PredictionService;
import dk.gameday.ballersclub.service.ReminderPreferenceService;
import dk.gameday.ballersclub.service.UserService;
import dk.gameday.ballersclub.util.CountryPlayerUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class BallersClubController {

    private final CurrentUserService currentUserService;
    private final UserService userService;
    private final PredictionService predictionService;
    private final LeaderboardService leaderboardService;
    private final ReminderPreferenceService reminderPreferenceService;
    private final PollService pollService;
    private final DataCollectionService dataCollectionService;
    private final MatchResultService matchResultService;
    private final AdminAccessService adminAccessService;
    private final ArenaFeedbackService arenaFeedbackService;
    private final ArenaChatService arenaChatService;
    private final ArenaUpdateService arenaUpdateService;
    private final PoolService poolService;
    private final BonusService bonusService;

    public BallersClubController(
            CurrentUserService currentUserService,
            UserService userService,
            PredictionService predictionService,
            LeaderboardService leaderboardService,
            ReminderPreferenceService reminderPreferenceService,
            PollService pollService,
            DataCollectionService dataCollectionService,
            MatchResultService matchResultService,
            AdminAccessService adminAccessService,
            ArenaFeedbackService arenaFeedbackService,
            ArenaChatService arenaChatService,
            ArenaUpdateService arenaUpdateService,
            PoolService poolService,
            BonusService bonusService
    ) {
        this.currentUserService = currentUserService;
        this.userService = userService;
        this.predictionService = predictionService;
        this.leaderboardService = leaderboardService;
        this.reminderPreferenceService = reminderPreferenceService;
        this.pollService = pollService;
        this.dataCollectionService = dataCollectionService;
        this.matchResultService = matchResultService;
        this.adminAccessService = adminAccessService;
        this.arenaFeedbackService = arenaFeedbackService;
        this.arenaChatService = arenaChatService;
        this.arenaUpdateService = arenaUpdateService;
        this.poolService = poolService;
        this.bonusService = bonusService;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/arena";
    }

    @GetMapping("/arena")
    public String arena(Model model, HttpSession session) {
        return arenaView(model, session);
    }

    private String arenaView(Model model, HttpSession session) {
        Optional<AppUser> currentUser = addCurrentUser(model, session);
        model.addAttribute("matches", predictionService.findMatches());
        model.addAttribute("matchSections", predictionService.findMatchSections());
        model.addAttribute("predictions", currentUser.map(predictionService::findPredictionMap).orElseGet(java.util.Map::of));
        model.addAttribute("predictionStats", predictionService.findPredictionStats());
        model.addAttribute("dataSummary", dataCollectionService.getSummary());
        model.addAttribute("predictionFeed", dataCollectionService.getPredictionFeed());
        model.addAttribute("countryPlayers", CountryPlayerUtil.playerSuggestions());
        model.addAttribute("countryNotes", CountryPlayerUtil.teamNotes());
        model.addAttribute("topPointRows", leaderboardService.getTopPoints());
        model.addAttribute("chatMessages", arenaChatService.findLatest());
        model.addAttribute("mentionUsernames", userService.findMentionUsernames());
        model.addAttribute("arenaUpdates", arenaUpdateService.getLatestUpdates());
        return "worldcup";
    }

    @PostMapping("/arena/predictions")
    public String savePrediction(
            @RequestParam Long matchId,
            @RequestParam String homeGoals,
            @RequestParam String awayGoals,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        AppUser user = currentUserService.getCurrentUser(session).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Log ind før du gemmer predictions.");
            return "redirect:/login";
        }

        try {
            predictionService.savePrediction(user, matchId, homeGoals, awayGoals);
            redirectAttributes.addFlashAttribute("success", "Prediction gemt.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/arena";
    }

    @PostMapping("/arena/chat")
    public String saveChatMessage(
            @RequestParam String message,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        AppUser user = currentUserService.getCurrentUser(session).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Log ind for at skrive i chatten.");
            return "redirect:/login";
        }
        try {
            arenaChatService.postMessage(message, user);
            redirectAttributes.addFlashAttribute("success", "Chat gemt.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/arena";
    }

    @PostMapping("/arena/chat/react")
    public String reactToChatMessage(
            @RequestParam Long messageId,
            @RequestParam String reaction,
            RedirectAttributes redirectAttributes
    ) {
        try {
            arenaChatService.react(messageId, reaction);
            redirectAttributes.addFlashAttribute("success", "Reaktion gemt.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/arena";
    }

    @PostMapping("/arena/predictions/all")
    public String saveAllPredictions(
            @RequestParam List<Long> matchIds,
            @RequestParam List<String> homeGoals,
            @RequestParam List<String> awayGoals,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        AppUser user = currentUserService.getCurrentUser(session).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Log ind før du gemmer predictions.");
            return "redirect:/login";
        }

        try {
            int saved = predictionService.savePredictions(user, matchIds, homeGoals, awayGoals);
            redirectAttributes.addFlashAttribute("success", saved + " predictions gemt.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/arena";
    }

    @PostMapping("/arena/feedback")
    public String saveFeedback(
            @RequestParam String message,
            @RequestParam(defaultValue = "/arena") String returnTo,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        AppUser user = currentUserService.getCurrentUser(session).orElse(null);
        try {
            arenaFeedbackService.save(message, user);
            redirectAttributes.addFlashAttribute("success", "Feedback gemt. Tak.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:" + safeReturnPath(returnTo);
    }

    @GetMapping("/leaderboard")
    public String leaderboard(Model model, HttpSession session) {
        Optional<AppUser> currentUser = addCurrentUser(model, session);
        List<LeaderboardRow> leaderboardRows = leaderboardService.getEligibleLeaderboard();
        model.addAttribute("rows", leaderboardRows.stream().limit(10).toList());
        model.addAttribute("leaderboardRollDownRows", leaderboardRows.stream().skip(10).toList());
        model.addAttribute("profiles", leaderboardService.getPredictionProfiles());
        model.addAttribute("topHitRows", leaderboardService.getTopHitPercentage());
        model.addAttribute("topExactRows", leaderboardService.getTopExactScores());
        model.addAttribute("topCorrectResultRows", leaderboardService.getTopCorrectResults());
        model.addAttribute("hitRollDownRows", leaderboardService.getHitRollDownRows());
        model.addAttribute("exactRollDownRows", leaderboardService.getExactRollDownRows());
        model.addAttribute("correctResultRollDownRows", leaderboardService.getCorrectResultRollDownRows());
        model.addAttribute("dataSummary", dataCollectionService.getSummary());
        model.addAttribute("predictionFeed", dataCollectionService.getPredictionFeed());
        model.addAttribute("poolLeaderboards", currentUser.map(poolService::findPoolLeaderboards).orElseGet(java.util.List::of));
        model.addAttribute("bonusReviewRows", bonusService.getBonusReviewRows());
        return "leaderboard";
    }

    @PostMapping("/pools")
    public String createPool(
            @RequestParam String name,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        AppUser user = currentUserService.getCurrentUser(session).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Log ind for at oprette en privat liga.");
            return "redirect:/login";
        }
        try {
            var pool = poolService.createPool(user, name);
            redirectAttributes.addFlashAttribute("success", "Privat liga oprettet. Kode: " + pool.getCode());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/leaderboard";
    }

    @PostMapping("/pools/join")
    public String joinPool(
            @RequestParam String code,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        AppUser user = currentUserService.getCurrentUser(session).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Log ind for at joine en privat liga.");
            return "redirect:/login";
        }
        try {
            var pool = poolService.joinPool(user, code);
            redirectAttributes.addFlashAttribute("success", "Du er nu med i " + pool.getName() + ".");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/leaderboard";
    }

    @GetMapping("/polls")
    public String polls(Model model, HttpSession session) {
        Optional<AppUser> currentUser = addCurrentUser(model, session);
        try {
            List<PollView> polls = pollService.getActivePollViews(currentUser.map(AppUser::getUsername).orElse(""));
            model.addAttribute("polls", polls);
            model.addAttribute("pointBonusPolls", polls.stream()
                    .filter(pollView -> switch (pollView.getPoll().getCategory()) {
                        case TOURNAMENT, PLAYER -> true;
                        case CULTURE, DAILY -> false;
                    })
                    .toList());
            model.addAttribute("communityBonusPolls", polls.stream()
                    .filter(pollView -> switch (pollView.getPoll().getCategory()) {
                        case TOURNAMENT, PLAYER -> false;
                        case CULTURE, DAILY -> true;
                    })
                    .toList());
            model.addAttribute("upcomingPolls", pollService.getUpcomingPolls());
            model.addAttribute("bonusReviewRows", bonusService.getBonusReviewRows());
        } catch (RuntimeException e) {
            model.addAttribute("polls", List.of());
            model.addAttribute("pointBonusPolls", List.of());
            model.addAttribute("communityBonusPolls", List.of());
            model.addAttribute("upcomingPolls", List.of());
            model.addAttribute("bonusReviewRows", List.of());
            model.addAttribute("pollsLoadError", "Bonus kunne ikke indlæses lige nu. Prøv igen om lidt.");
        }
        return "polls";
    }

    @PostMapping("/polls/vote")
    public String vote(
            @RequestParam Long pollId,
            @RequestParam Long optionId,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        AppUser user = currentUserService.getCurrentUser(session).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Log ind før du stemmer.");
            return "redirect:/login";
        }
        try {
            pollService.vote(pollId, optionId, user.getUsername());
            redirectAttributes.addFlashAttribute("success", "Stemme gemt.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/polls";
    }

    @GetMapping("/login")
    public String login(Model model, HttpSession session) {
        addCurrentUser(model, session);
        return "login";
    }

    @GetMapping("/signup")
    public String signUp(Model model, HttpSession session) {
        addCurrentUser(model, session);
        return "signup";
    }

    @PostMapping("/login")
    public String doLogin(
            @RequestParam String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String communicationConsent,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        try {
            AppUser user = userService.loginExisting(username, email, communicationConsent != null);
            session.setAttribute(CurrentUserService.USER_ID_SESSION_KEY, user.getId());
            return "redirect:/arena";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/login";
        }
    }

    @PostMapping("/signup")
    public String doSignUp(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam(required = false) String communicationConsent,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        try {
            AppUser user = userService.signUp(username, email, communicationConsent != null);
            session.setAttribute(CurrentUserService.USER_ID_SESSION_KEY, user.getId());
            return "redirect:/arena";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/signup";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/arena";
    }

    @GetMapping("/settings")
    public String settings() {
        return "redirect:/info";
    }

    @PostMapping("/settings/reminders")
    public String saveSettings(
            @RequestParam(required = false) String matchdayReminder,
            @RequestParam(required = false) String oneHourReminder,
            @RequestParam(required = false) String communicationConsent,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        AppUser user = currentUserService.getCurrentUser(session).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Log ind for at styre reminders.");
            return "redirect:/login";
        }
        reminderPreferenceService.update(user, matchdayReminder != null, oneHourReminder != null);
        userService.updateCommunicationConsent(user, communicationConsent != null);
        redirectAttributes.addFlashAttribute("success", "Indstillinger gemt.");
        return "redirect:/info";
    }

    @GetMapping("/admin/results")
    public String adminResults(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        AppUser user = currentUserService.getCurrentUser(session).orElse(null);
        if (!adminAccessService.isAdmin(user)) {
            redirectAttributes.addFlashAttribute("error", "Admin adgang kræves.");
            return "redirect:/login";
        }
        model.addAttribute("currentUser", user);
        model.addAttribute("isAdmin", true);
        model.addAttribute("matches", predictionService.findMatches());
        model.addAttribute("feedbackItems", arenaFeedbackService.findLatest());
        model.addAttribute("latePredictionUpdates", predictionService.findLatePredictionUpdates());
        return "admin-results";
    }

    @PostMapping("/admin/results")
    public String saveResult(
            @RequestParam Long matchId,
            @RequestParam String homeScore,
            @RequestParam String awayScore,
            @RequestParam(required = false) String advancingTeam,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        AppUser user = currentUserService.getCurrentUser(session).orElse(null);
        if (!adminAccessService.isAdmin(user)) {
            redirectAttributes.addFlashAttribute("error", "Admin adgang kræves.");
            return "redirect:/login";
        }
        try {
            matchResultService.updateResult(matchId, homeScore, awayScore, advancingTeam);
            redirectAttributes.addFlashAttribute("success", "Resultat gemt.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/results";
    }

    @PostMapping("/admin/results/all")
    public String saveAllResults(
            @RequestParam List<Long> matchIds,
            @RequestParam List<String> homeScores,
            @RequestParam List<String> awayScores,
            @RequestParam(required = false) List<String> advancingTeams,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        AppUser user = currentUserService.getCurrentUser(session).orElse(null);
        if (!adminAccessService.isAdmin(user)) {
            redirectAttributes.addFlashAttribute("error", "Admin adgang kræves.");
            return "redirect:/login";
        }
        try {
            List<String> safeAdvancingTeams = advancingTeams == null
                    ? java.util.Collections.nCopies(matchIds.size(), "")
                    : advancingTeams;
            int saved = matchResultService.updateResults(matchIds, homeScores, awayScores, safeAdvancingTeams);
            redirectAttributes.addFlashAttribute("success", saved + " resultater gemt.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/results";
    }

    @PostMapping("/admin/results/clear")
    public String clearResult(
            @RequestParam Long matchId,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        AppUser user = currentUserService.getCurrentUser(session).orElse(null);
        if (!adminAccessService.isAdmin(user)) {
            redirectAttributes.addFlashAttribute("error", "Admin adgang kræves.");
            return "redirect:/login";
        }
        matchResultService.clearResult(matchId);
        redirectAttributes.addFlashAttribute("success", "Resultat ryddet.");
        return "redirect:/admin/results";
    }

    @GetMapping("/info")
    public String info(Model model, HttpSession session) {
        Optional<AppUser> currentUser = addCurrentUser(model, session);
        currentUser.ifPresent(user -> model.addAttribute("preference", reminderPreferenceService.getOrCreate(user)));
        return "info";
    }

    @GetMapping("/privacy")
    public String privacy(Model model, HttpSession session) {
        addCurrentUser(model, session);
        return "privacy";
    }

    @GetMapping("/admin/consent-export.csv")
    public ResponseEntity<String> exportConsentUsers(HttpSession session, RedirectAttributes redirectAttributes) {
        AppUser user = currentUserService.getCurrentUser(session).orElse(null);
        if (!adminAccessService.isAdmin(user)) {
            redirectAttributes.addFlashAttribute("error", "Admin adgang kræves.");
            return ResponseEntity.status(302).header(HttpHeaders.LOCATION, "/login").build();
        }

        StringBuilder csv = new StringBuilder("username,email,created_at\n");
        userService.findUsersWithCommunicationConsent().forEach(consentUser -> csv
                .append(csvCell(consentUser.getUsername())).append(',')
                .append(csvCell(consentUser.getEmail())).append(',')
                .append(csvCell(String.valueOf(consentUser.getCreatedAt()))).append('\n'));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ballers-club-consent-users.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv.toString());
    }

    private Optional<AppUser> addCurrentUser(Model model, HttpSession session) {
        Optional<AppUser> currentUser = currentUserService.getCurrentUser(session);
        model.addAttribute("currentUser", currentUser.orElse(null));
        model.addAttribute("isAdmin", currentUser.map(adminAccessService::isAdmin).orElse(false));
        return currentUser;
    }

    private String csvCell(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private String safeReturnPath(String returnTo) {
        if (returnTo == null || returnTo.isBlank() || !returnTo.startsWith("/") || returnTo.startsWith("//")) {
            return "/arena";
        }
        return returnTo;
    }
}
