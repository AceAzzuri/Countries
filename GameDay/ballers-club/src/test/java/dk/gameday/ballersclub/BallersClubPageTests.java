package dk.gameday.ballersclub;

import dk.gameday.ballersclub.repository.PoolRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

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
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Gianluigi Donnarumma"))));
    }

    @Test
    void pollVoteIsSavedAndRendered() throws Exception {
        MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/signup")
                        .param("username", "poll tester")
                        .param("email", "poll@example.com"))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getRequest()
                .getSession(false);

        mockMvc.perform(post("/polls/vote")
                        .session(session)
                        .param("pollId", "6")
                        .param("optionId", "602"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/polls"));

        mockMvc.perform(get("/polls").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("polls"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Du stemte: Japan")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("100%")));
    }

    @Test
    void leaderboardRendersAfterPrediction() throws Exception {
        MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/signup")
                        .param("username", "table tester")
                        .param("email", "table@example.com"))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getRequest()
                .getSession(false);

        mockMvc.perform(post("/arena/predictions")
                        .session(session)
                        .param("matchId", "13")
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
    void allEnteredPredictionsCanBeSavedTogether() throws Exception {
        MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/signup")
                        .param("username", "bulk tester")
                        .param("email", "bulk@example.com"))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getRequest()
                .getSession(false);

        mockMvc.perform(post("/arena/predictions/all")
                        .session(session)
                        .param("matchIds", "13", "14", "15")
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
    void adminCanSaveResultAndTriggerScoringFeed() throws Exception {
        MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/signup")
                        .param("username", "Azzuri")
                        .param("email", "azzuri@example.com"))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getRequest()
                .getSession(false);

        mockMvc.perform(post("/arena/predictions")
                        .session(session)
                        .param("matchId", "13")
                        .param("homeGoals", "2")
                        .param("awayGoals", "1"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/admin/results")
                        .session(session)
                        .param("matchId", "13")
                        .param("homeScore", "2")
                        .param("awayScore", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/results"));

        mockMvc.perform(get("/leaderboard").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Azzuri")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("3 pts")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Azzuri ramte")));
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
