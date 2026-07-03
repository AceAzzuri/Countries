package dk.gameday.ballersclub;

import dk.gameday.ballersclub.repository.PoolRepository;
import dk.gameday.ballersclub.repository.ArenaChatMessageRepository;
import dk.gameday.ballersclub.repository.WorldCupMatchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class BallersClubPageTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PoolRepository poolRepository;

    @Autowired
    private ArenaChatMessageRepository arenaChatMessageRepository;

    @Autowired
    private WorldCupMatchRepository worldCupMatchRepository;

    @Test
    void publicPagesRender() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/arena"));
        mockMvc.perform(get("/arena")).andExpect(status().isOk()).andExpect(view().name("worldcup"));
        mockMvc.perform(get("/polls")).andExpect(status().isOk()).andExpect(view().name("polls"));
        mockMvc.perform(get("/leaderboard")).andExpect(status().isOk()).andExpect(view().name("leaderboard"));
        mockMvc.perform(get("/info")).andExpect(status().isOk()).andExpect(view().name("info"));
        mockMvc.perform(get("/privacy")).andExpect(status().isOk()).andExpect(view().name("privacy"));
        mockMvc.perform(get("/signup")).andExpect(status().isOk()).andExpect(view().name("signup"));
    }

    @Test
    void accidentalMatchPlanningRoutesStayUnavailable() throws Exception {
        mockMvc.perform(get("/matches")).andExpect(status().isNotFound());
        mockMvc.perform(get("/matches/1")).andExpect(status().isNotFound());
    }

    @Test
    void signUpRedirectsToArena() throws Exception {
        mockMvc.perform(post("/signup")
                        .param("username", "culture captain")
                        .param("email", "culture@example.com")
                .param("communicationConsent", "on"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/arena"));
    }

    @Test
    void existingEmailCanLoginEvenIfUsernameIsTypedDifferently() throws Exception {
        mockMvc.perform(post("/signup")
                        .param("username", "culture captain")
                .param("email", "same-profile@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/arena"));

        mockMvc.perform(post("/login")
                        .param("username", "different display name")
                .param("email", "same-profile@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/arena"));
    }

    @Test
    void bonusPollOptionsMatchCurrentSetup() throws Exception {
        mockMvc.perform(get("/polls"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Mikel Oyarzabal")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Iran")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Kun <strong>Hvem vinder VM 2026?</strong>, <strong>Golden Boot</strong> og <strong>Golden Glove</strong> bruger den nye regel")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Turneringens spiller og Turneringens unge spiller er nye, friske bonus-polls")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("NY")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Gianluigi Donnarumma"))));
    }

    @Test
    void pointPollVoteCanBeChangedAndRendered() throws Exception {
        MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/signup")
                        .param("username", "poll tester")
                        .param("email", "poll@example.com"))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getRequest()
                .getSession(false);

        mockMvc.perform(post("/polls/vote")
                        .session(session)
                        .param("pollId", "1")
                        .param("optionId", "102"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/polls"));

        mockMvc.perform(get("/polls").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("polls"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("France")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("5 point")));

        mockMvc.perform(post("/polls/vote")
                        .session(session)
                        .param("pollId", "1")
                        .param("optionId", "103"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/polls"));

        mockMvc.perform(get("/polls").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("polls"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("France")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Spain")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("2 point")));
    }

    @Test
    void leaderboardRendersAfterPrediction() throws Exception {
        openPredictionMatch(65L);

        MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/signup")
                        .param("username", "table tester")
                        .param("email", "table@example.com"))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getRequest()
                .getSession(false);

        mockMvc.perform(post("/arena/predictions")
                        .session(session)
                        .param("matchId", "65")
                        .param("homeGoals", "2")
                        .param("awayGoals", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/arena"));

        mockMvc.perform(get("/leaderboard").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("leaderboard"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Spillerkort")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("table tester")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Pick 2-1")));
    }

    @Test
    void arenaChatCanStoreMessagesAndReactions() throws Exception {
        MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/signup")
                        .param("username", "Chat User")
                        .param("email", "chat-user@example.com"))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getRequest()
                .getSession(false);

        mockMvc.perform(post("/arena/chat")
                        .session(session)
                        .param("message", "Arena er varm"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/arena"));

        var message = arenaChatMessageRepository.findTop10ByOrderByCreatedAtDesc().get(0);
        mockMvc.perform(post("/arena/chat/react")
                        .param("messageId", message.getId().toString())
                        .param("reaction", "fire"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/arena"));

        var updated = arenaChatMessageRepository.findById(message.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(1, updated.getFireReactions());
        org.junit.jupiter.api.Assertions.assertEquals("Arena er varm", updated.getMessage());
        org.junit.jupiter.api.Assertions.assertEquals("Chat User", updated.getUsername());
    }

    @Test
    void arenaPageShowsChatRoomWhileFutureFeaturesStayHidden() throws Exception {
        MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/signup")
                        .param("username", "Mention Alpha")
                        .param("email", "mention-alpha@example.com"))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getRequest()
                .getSession(false);
        mockMvc.perform(post("/signup")
                        .param("username", "Mention Beta")
                        .param("email", "mention-beta@example.com"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/arena").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Chatrum")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Arena chat")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("bc-chat-new")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("@alle")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("@Mention Alpha")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("@Mention Beta")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("data-mention-picker"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Opdateringer"))))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("bc-leaderboard-preview")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("href=\"/leaderboard\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Gå til Leaderboard")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-match-section-id=\"gruppekampe-3\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-match-section-id=\"gruppekampe-3\" open")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("data-match-section-id=\"gruppekampe-2\" open"))))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-match-section-id=\"knockout-vejen\" open")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Knockout predictions åbner"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("data-country-details"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("data-country-modal"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("href=\"/matches\""))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Opret kamp"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Kampkontrakt"))));

        mockMvc.perform(get("/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Top 10 med mindst 15 spillede kampe vises her")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Flest udfald gættet rigtigt")));
    }

    @Test
    void adminCanSaveMultipleResultsAtOnce() throws Exception {
        MockHttpSession adminSession = (MockHttpSession) mockMvc.perform(post("/signup")
                        .param("username", "Azzuri")
                        .param("email", "azzuri@example.com"))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getRequest()
                .getSession(false);

        mockMvc.perform(post("/admin/results/all")
                .session(adminSession)
                .param("matchIds", "13", "14")
                .param("homeScores", "2", "1")
                .param("awayScores", "1", "0")
                .param("advancingTeams", "", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/results"));

        var match13 = worldCupMatchRepository.findById(13L).orElseThrow();
        var match14 = worldCupMatchRepository.findById(14L).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(2, match13.getHomeScore());
        org.junit.jupiter.api.Assertions.assertEquals(1, match13.getAwayScore());
        org.junit.jupiter.api.Assertions.assertEquals(1, match14.getHomeScore());
        org.junit.jupiter.api.Assertions.assertEquals(0, match14.getAwayScore());
    }

    @Test
    void adminCanSaveKnockoutResultsAndUpdateNextRound() throws Exception {
        MockHttpSession adminSession = (MockHttpSession) mockMvc.perform(post("/signup")
                        .param("username", "Azzuri")
                        .param("email", "azzuri@example.com"))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getRequest()
                .getSession(false);

        try {
            mockMvc.perform(post("/admin/results")
                            .session(adminSession)
                            .param("matchId", "73")
                            .param("homeScore", "")
                            .param("awayScore", "")
                            .param("advancingTeam", "Canada"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/results"));

            mockMvc.perform(post("/admin/results")
                            .session(adminSession)
                            .param("matchId", "75")
                            .param("homeScore", "0")
                            .param("awayScore", "1"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/results"));

            var match89 = worldCupMatchRepository.findById(89L).orElseThrow();
            org.junit.jupiter.api.Assertions.assertEquals("Canada", match89.getHomeTeam());
            org.junit.jupiter.api.Assertions.assertEquals("Morocco", match89.getAwayTeam());

            mockMvc.perform(post("/admin/results")
                            .session(adminSession)
                            .param("matchId", "82")
                            .param("homeScore", "2")
                            .param("awayScore", "2")
                            .param("advancingTeam", "Belgium"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/results"));

            var match82 = worldCupMatchRepository.findById(82L).orElseThrow();
            org.junit.jupiter.api.Assertions.assertEquals("Belgium", match82.getAdvancingTeam());
            org.junit.jupiter.api.Assertions.assertEquals(2, match82.getHomeScore());
            org.junit.jupiter.api.Assertions.assertEquals(2, match82.getAwayScore());
        } finally {
            worldCupMatchRepository.findById(73L).ifPresent(match -> {
                match.clearResult();
                worldCupMatchRepository.save(match);
            });
            worldCupMatchRepository.findById(75L).ifPresent(match -> {
                match.clearResult();
                worldCupMatchRepository.save(match);
            });
            worldCupMatchRepository.findById(82L).ifPresent(match -> {
                match.clearResult();
                worldCupMatchRepository.save(match);
            });
        }
    }

    @Test
    void allEnteredPredictionsCanBeSavedTogether() throws Exception {
        openPredictionMatch(65L);
        openPredictionMatch(67L);

        MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/signup")
                        .param("username", "bulk tester")
                        .param("email", "bulk@example.com"))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getRequest()
                .getSession(false);

        mockMvc.perform(post("/arena/predictions/all")
                        .session(session)
                        .param("matchIds", "65", "66", "67")
                        .param("homeGoals", "2", "", "1")
                        .param("awayGoals", "1", "", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/arena"));

        mockMvc.perform(get("/leaderboard").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("bulk tester")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Pick 2-1")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Pick 1-1")));
    }

    @Test
    void knockoutPredictionCanBeSavedBeforeKickoff() throws Exception {
        openPredictionMatch(73L);

        MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/signup")
                        .param("username", "knockout tester")
                        .param("email", "knockout@example.com"))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getRequest()
                .getSession(false);

        mockMvc.perform(post("/arena/predictions")
                        .session(session)
                        .param("matchId", "73")
                        .param("homeGoals", "2")
                        .param("awayGoals", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/arena"));

        mockMvc.perform(get("/leaderboard").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("knockout tester")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Pick 2-1")));
    }

    @Test
    void adminCanSaveResultAndTriggerScoringFeed() throws Exception {
        openPredictionMatch(64L);

        MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/signup")
                        .param("username", "Azzuri")
                        .param("email", "azzuri@example.com"))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getRequest()
                .getSession(false);
        MockHttpSession outcomeSession = (MockHttpSession) mockMvc.perform(post("/signup")
                        .param("username", "Outcome User")
                        .param("email", "outcome-user@example.com"))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getRequest()
                .getSession(false);

        mockMvc.perform(post("/arena/predictions")
                        .session(session)
                        .param("matchId", "64")
                        .param("homeGoals", "2")
                        .param("awayGoals", "1"))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(post("/arena/predictions")
                        .session(outcomeSession)
                        .param("matchId", "64")
                        .param("homeGoals", "3")
                        .param("awayGoals", "1"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/admin/results")
                        .session(session)
                        .param("matchId", "64")
                        .param("homeScore", "2")
                        .param("awayScore", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/results"));

        mockMvc.perform(get("/leaderboard").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Azzuri")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("3 pts")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("2 ramte")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Præcis: Azzuri")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Udfald: Outcome User")));
    }

    @Test
    void adminResultsPageShowsAdvancingTeamInstruction() throws Exception {
        MockHttpSession adminSession = (MockHttpSession) mockMvc.perform(post("/signup")
                        .param("username", "Azzuri")
                        .param("email", "azzuri@example.com"))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getRequest()
                .getSession(false);

        mockMvc.perform(get("/admin/results").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Hvis ordinær tid ender uafgjort eller kampen voides, skal du vælge hvem der går videre")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Hold der går videre")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Ved uafgjort eller void kan du vælge holdet, der går videre efter forlængelse, straffe eller uden resultat.")));
    }

    private void openPredictionMatch(Long matchId) {
        var match = worldCupMatchRepository.findById(matchId).orElseThrow();
        match.clearResult();
        ReflectionTestUtils.setField(match, "kickoffAt", LocalDateTime.now().plusDays(30));
        worldCupMatchRepository.save(match);
    }

    @Test
    void adminCanExportConsentEmails() throws Exception {
        mockMvc.perform(post("/signup")
                        .param("username", "consent user")
                        .param("email", "consent@example.com")
                        .param("communicationConsent", "on"))
                .andExpect(status().is3xxRedirection());

        MockHttpSession adminSession = (MockHttpSession) mockMvc.perform(post("/signup")
                        .param("username", "Azzuri")
                        .param("email", "azzuri@example.com"))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getRequest()
                .getSession(false);

        mockMvc.perform(get("/admin/consent-export.csv").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("username,email,created_at")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("consent@example.com")));
    }

    @Test
    void feedbackCanBeSavedAndSeenByAdmin() throws Exception {
        MockHttpSession playerSession = (MockHttpSession) mockMvc.perform(post("/signup")
                        .param("username", "feedback user")
                        .param("email", "feedback@example.com"))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getRequest()
                .getSession(false);

        mockMvc.perform(post("/arena/feedback")
                        .session(playerSession)
                        .param("message", "Leaderboard skal være endnu tydeligere"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/arena"));

        MockHttpSession adminSession = (MockHttpSession) mockMvc.perform(post("/signup")
                        .param("username", "Azzuri")
                        .param("email", "azzuri@example.com"))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getRequest()
                .getSession(false);

        mockMvc.perform(get("/admin/results").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Arena kommentarer")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("feedback user")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Leaderboard skal være endnu tydeligere")));
    }

    @Test
    void privatePoolCanBeCreatedAndJoined() throws Exception {
        MockHttpSession ownerSession = (MockHttpSession) mockMvc.perform(post("/signup")
                        .param("username", "pool owner")
                        .param("email", "pool-owner@example.com"))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getRequest()
                .getSession(false);

        mockMvc.perform(post("/pools")
                        .session(ownerSession)
                        .param("name", "Kongen af kontoret"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/leaderboard"));

        String code = poolRepository.findAll().stream()
                .filter(pool -> pool.getName().equals("Kongen af kontoret"))
                .findFirst()
                .orElseThrow()
                .getCode();

        MockHttpSession challengerSession = (MockHttpSession) mockMvc.perform(post("/signup")
                        .param("username", "pool challenger")
                        .param("email", "pool-challenger@example.com"))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getRequest()
                .getSession(false);

        mockMvc.perform(post("/pools/join")
                        .session(challengerSession)
                        .param("code", code))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/leaderboard"));

        mockMvc.perform(get("/leaderboard").session(challengerSession))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Kongen af kontoret")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(code)))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("2 medlemmer")));
    }
}
