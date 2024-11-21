package csd.playermanagement;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;

import csd.playermanagement.DTO.UserDTO;
import csd.playermanagement.service.UserService;
import csd.shared_library.model.Tournament;
import csd.shared_library.model.User;


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class UserIntegrationTest {

    @LocalServerPort
    private int port;

    private final String baseUrl = "http://localhost:";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private Firestore firestore;

    @Autowired
    private UserService userService;

    private List<String> createdUserUids = new ArrayList<>();
    private List<String> createdTournamentIds = new ArrayList<>();


    // WORKAROUND for working with TIMESTAMP for now
    @BeforeEach
    public void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        // Register the mixin
        objectMapper.addMixIn(User.class, IgnoreTimestampFieldsMixin.class);
        objectMapper.addMixIn(Tournament.class, IgnoreTimestampFieldsMixin.class);

        // Configure the RestTemplate to use the custom ObjectMapper
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
        restTemplate.getRestTemplate().getMessageConverters().add(0, converter);
    }

    @JsonIgnoreProperties({"dateOfBirth", "createdTimestamp", "startDatetime", "endDatetime"})
    public abstract class IgnoreTimestampFieldsMixin {
        // No need to include any methods or fields
    }


    @AfterEach
    public void cleanUp() {
        for (String authId : createdUserUids) {
            try {
                firestore.collection("Users").document(authId).delete().get();
            } catch (Exception e) {
                System.err.println("Failed to delete user with UID: " + authId);
                e.printStackTrace();
            }
        }
        // Clear the list after cleanup
        createdUserUids.clear();

        // Clean up tournaments
        for (String tournamentId : createdTournamentIds) {
            try {
                firestore.collection("Tournaments").document(tournamentId).delete().get();
            } catch (Exception e) {
                System.err.println("Failed to delete tournament with ID: " + tournamentId);
                e.printStackTrace();
            }
        }
        createdTournamentIds.clear();
    }


    @Test
    public void updateUser_Success() throws Exception {
        // Arrange
        User user = new User();
        user.setAuthId("auth1");
        user.setEmail("original@example.com");
  
        // Save the user to Firestore
        DocumentReference userRef = firestore.collection("Users").document();
        userRef.set(user).get();
        createdUserUids.add(userRef.getId());
    
        // Prepare updated user data
        UserDTO updatedUser = new UserDTO();
        updatedUser.setUsername("updatedUser");
        updatedUser.setName("Updated Name");
        updatedUser.setPhoneNumber(99988877);
        updatedUser.setNationality("US");
        updatedUser.setChessUsername("updatedChessUsername");
        updatedUser.setDateOfBirth("1995-05-15");
    
        // Prepare the request URL
        URI uri = new URI(baseUrl + port + "/user/updateUser/" + user.getAuthId());
    
        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    
        // Create the request entity
        HttpEntity<UserDTO> requestEntity = new HttpEntity<>(updatedUser, headers);
    
        // Act
        ResponseEntity<UserDTO> response = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, UserDTO.class);
    
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

   
}