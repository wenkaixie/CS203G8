package elo.test;

import elo.controller.EloController;
import elo.service.EloService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EloController.class)
class EloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EloService eloService;

    @Test
    void testUpdateElo_Success() throws Exception {
        // Mock the service call to simulate success without database interaction
        doNothing().when(eloService).updateElo(anyString(), anyString(), anyDouble(), anyDouble(), anyDouble(), anyDouble());
    
        String jsonRequest = "{\"Elo1\":1500, \"Elo2\":1600, \"AS1\":1, \"AS2\":0}";
    
        mockMvc.perform(post("/api/elo/update")
                .param("userId1", "user1")  // Valid userId1
                .param("userId2", "user2")  // Valid userId2
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk());  // Expect 200 OK
    
        verify(eloService, times(1)).updateElo(anyString(), anyString(), anyDouble(), anyDouble(), anyDouble(), anyDouble());
    }  

    @Test
    void testUpdateElo_MissingUserIds() throws Exception {
        String jsonRequest = "{\"Elo1\":1500, \"Elo2\":1600, \"AS1\":1, \"AS2\":0}";
    
        mockMvc.perform(post("/api/elo/update")
                .param("userId1", "")  // Empty userId1
                .param("userId2", "")  // Empty userId2
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest());  // Expect 400 Bad Request
    }    

    @Test
    void testUpdateElo_InvalidEloValues() throws Exception {
        String jsonRequest = "{\"Elo1\":0, \"Elo2\":1600, \"AS1\":1, \"AS2\":0}";

        mockMvc.perform(post("/api/elo/update")
                .param("userId1", "user1")
                .param("userId2", "user2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateElo_InvalidScoreValues() throws Exception {
        String jsonRequest = "{\"Elo1\":1500, \"Elo2\":1600, \"AS1\":-1, \"AS2\":0}";

        mockMvc.perform(post("/api/elo/update")
                .param("userId1", "user1")
                .param("userId2", "user2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateElo_FirestoreException() throws Exception {
        // Simulate a Firestore error by throwing a RuntimeException from the service
        doThrow(new RuntimeException("Firestore Error")).when(eloService)
                .updateElo(anyString(), anyString(), anyDouble(), anyDouble(), anyDouble(), anyDouble());
    
        String jsonRequest = "{\"Elo1\":1500, \"Elo2\":1600, \"AS1\":1, \"AS2\":0}";
    
        mockMvc.perform(post("/api/elo/update")
                .param("userId1", "user1")  // Valid userId1
                .param("userId2", "user2")  // Valid userId2
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isInternalServerError());  // Expect 500 Internal Server Error
    }
}