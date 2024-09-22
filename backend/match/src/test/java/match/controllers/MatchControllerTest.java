package match.controllers;

import match.model.MatchModel;
import match.service.MatchService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MatchController.class)  // Focused test on the controller layer
public class MatchControllerTest {

    @Autowired
    private MockMvc mockMvc;  // Autowire MockMvc

    @MockBean  // Use @MockBean to mock the service layer
    private MatchService matchService;

    @Test
    public void testGetSeededMatchups() throws Exception {
        // Arrange mock behavior for the service layer
        when(matchService.generateSeededMatchups("testTournament"))
                .thenReturn(List.of(new MatchModel("player1", "player2")));

        // Perform the request and assert results
        mockMvc.perform(get("/api/matches/seed/testTournament")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].playerID1").value("player1"))
                .andExpect(jsonPath("$[0].playerID2").value("player2"));
    }

    @Test
    public void testGetPlayerEloMap() throws Exception {
        // Arrange mock behavior for the service layer
        when(matchService.getPlayerEloMap("testTournament"))
                .thenReturn(Map.of("player1", 2000, "player2", 1800));

        // Perform the request and assert results
        mockMvc.perform(get("/api/matches/elo/testTournament")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.player1").value(2000))
                .andExpect(jsonPath("$.player2").value(1800));
    }
}
