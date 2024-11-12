package csd.playermanagement;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;


import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;

import csd.playermanagement.Model.Tournament;
import csd.playermanagement.Model.User;
import csd.playermanagement.DTO.UserDTO;
import csd.playermanagement.Service.UserService;

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

    // NOT USED
    @Test
    public void createUser_Success() throws Exception {
        URI uri = new URI(baseUrl + port + "/user/createUser");

        User user1 = new User();
        user1.setAuthId("auth123");
        user1.setUsername("testuser");


        ResponseEntity<User> result = restTemplate.postForEntity(uri, user1, User.class);

        assertEquals(HttpStatus.CREATED, result.getStatusCode()); // check corrected created status code
        assertNotNull(result.getBody());
        assertEquals(user1.getUsername(), result.getBody().getUsername());
        assertEquals(user1.getEmail(), result.getBody().getEmail());

        // for teardown
        // Keep track of the created user's UID
        System.out.println("result body" + result.getBody());
        String createdAuthId = result.getBody().getAuthId();
        System.out.println("Created authID: " + createdAuthId); // Debugging line
        assertNotNull(createdAuthId, "Created AuthId should not be null");
        createdUserUids.add(createdAuthId);
    }


    @Test
    public void getAllUsers_Success() throws Exception {
        // Arrange
        User user1 = new User();
        user1.setAuthId("auth1");
        user1.setUsername("user1");

        User user2 = new User();
        user2.setAuthId("auth2");
        user2.setUsername("user2");

        // Save users to Firestore
        DocumentReference userRef1 = firestore.collection("Users").document();
        userRef1.set(user1).get();
        createdUserUids.add(userRef1.getId());

        DocumentReference userRef2 = firestore.collection("Users").document();
        userRef2.set(user2).get();
        createdUserUids.add(userRef2.getId());

        // Prepare request
        URI uri = new URI(baseUrl + port + "/user/getAllUsers");

        // Act
        ResponseEntity<User[]> response = restTemplate.getForEntity(uri, User[].class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        User[] users = response.getBody();
        assertNotNull(users);
        assertTrue(users.length >= 2); // At least the two users we added

    }

    @Test
    public void getUser_ValidUserId_Success() throws Exception {
        // Arrange
        User user1 = new User();
        user1.setAuthId("auth1");
        user1.setUsername("testuser");
        user1.setEmail("testuser@example.com");
    
        // Save the user to Firestore
        DocumentReference userRef = firestore.collection("Users").document();
        userRef.set(user1).get();
        createdUserUids.add(userRef.getId()); // Add to cleanup list
    
        // Prepare the request URL with the user's UID
        URI uri = new URI(baseUrl + port + "/user/getUser/" + user1.getAuthId());
    
        // Act
        ResponseEntity<User> response = restTemplate.getForEntity(uri, User.class);
    
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        User retrievedUser = response.getBody();
        assertNotNull(retrievedUser);
        assertEquals(user1.getUsername(), retrievedUser.getUsername());
        assertEquals(user1.getEmail(), retrievedUser.getEmail());
    }

    @Test
    public void getUser_InvalidUserId_Failure() throws Exception {
        // Arrange
        String invalidUserId = "nonExistent";

        // Prepare the URI
        URI uri = new URI(baseUrl + port + "/user/getUser/" + invalidUserId);

        // Act: Perform the GET request, expecting a Map (JSON response)
        ResponseEntity<Map> result = restTemplate.getForEntity(uri, Map.class);

        // Assert: Check the status code is 404 NOT_FOUND
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());

        // Assert: Check the response body contains an error message
        Map<String, String> responseBody = result.getBody();
        assertNotNull(responseBody, "Response body should not be null");
        assertTrue(responseBody.containsKey("error"), "Response should contain 'error' key");
        assertEquals("User not found.", responseBody.get("error").trim(), "Error message should match 'User not found'");
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

    @Test
    public void updateUser_InvalidUserId_Failure() throws Exception {
        // Arrange
        String invalidUserId = "nonexistent_user_id";

        // Prepare updated user data
        UserDTO updatedUser = new UserDTO();
        updatedUser.setUsername("updatedUser");
        updatedUser.setName("Updated Name");
        updatedUser.setPhoneNumber(99988877);
        updatedUser.setNationality("US");
        updatedUser.setChessUsername("updatedChessUsername");
        updatedUser.setDateOfBirth("1995-05-15");

        // Prepare the request URL
        URI uri = new URI(baseUrl + port + "/user/updateUser/" + invalidUserId);

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create the request entity
        HttpEntity<UserDTO> requestEntity = new HttpEntity<>(updatedUser, headers);

        // Act: Expect a Map (JSON response) since we're testing an error case
        ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, Map.class);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        // Assert the response body contains an error message
        Map<String, String> responseBody = response.getBody();
        assertNotNull(responseBody, "Response body should not be null");
        assertTrue(responseBody.containsKey("error"), "Response should contain 'error' key");
        assertEquals("User not found.", responseBody.get("error").trim(), "Error message should match 'User not found'");
    }

    @Test
    public void registerUserForTournament_Success() throws Exception {
        // Arrange
        // Create a new, unique document reference in the Tournaments collection in Firebase Firestore
        DocumentReference tournamentRef = firestore.collection("Tournaments").document();
        String tournamentId = tournamentRef.getId();

        Tournament tournament = new Tournament();
        tournament.setTid(tournamentId);
        tournament.setName("Test Tournament");
        // ... Set other tournament properties as needed
        tournamentRef.set(tournament).get();
        createdTournamentIds.add(tournamentId);

        // Create and save a user
        DocumentReference userRef = firestore.collection("Users").document();
        String authId = userRef.getId();

        User user = new User();
        user.setAuthId(authId);
        user.setUsername("testuser");
        user.setRegistrationHistory(new ArrayList<>());
        userRef.set(user).get();
        createdUserUids.add(authId);

        // Prepare request to register the user for the tournament
        URI uri = new URI(baseUrl + port + "/user/registerTournament/" + tournamentId + "/" + authId);

        // Create DTO for the request
        UserDTO userDto = new UserDTO();
        userDto.setAuthId(authId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<UserDTO> requestEntity = new HttpEntity<>(userDto, headers);

        // Act
        ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, Map.class);

        // Assert HTTP Status
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Expected HTTP 200 OK");
        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody, "Response body should not be null");

        // Validate the message in the response
        assertEquals("User successfully registered for the tournament.", responseBody.get("message").toString().trim());

        // Verify the user's registration history includes the tournamentId
        DocumentSnapshot updatedUserSnapshot = userRef.get().get();
        User updatedUser = updatedUserSnapshot.toObject(User.class);
        assertNotNull(updatedUser, "User should not be null");
        assertNotNull(updatedUser.getRegistrationHistory(), "User's registration history should not be null");
        assertTrue(updatedUser.getRegistrationHistory().contains(tournamentId), "User's registration history should contain the tournamentId");

    }

    @Test
    public void registerUserForTournament_TournamentNotFound() throws Exception {
        // Arrange
        // Create and save a user, but do not create a tournament
        DocumentReference userRef = firestore.collection("Users").document();
        String authId = userRef.getId();

        User user = new User();
        user.setAuthId(authId);
        user.setUsername("testuser");
        user.setRegistrationHistory(new ArrayList<>());
        userRef.set(user).get();
        createdUserUids.add(authId);

        // Prepare a non-existent tournament ID
        String nonExistentTournamentId = "nonExistentTournamentId";

        // Prepare request to register the user for the non-existent tournament
        URI uri = new URI(baseUrl + port + "/user/registerTournament/" + nonExistentTournamentId + "/" + authId);

        // Create DTO for the request
        UserDTO userDto = new UserDTO();
        userDto.setAuthId(authId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<UserDTO> requestEntity = new HttpEntity<>(userDto, headers);

        // Act and Assert
        ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, Map.class);

        // Assert HTTP Status
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Expected HTTP 404 NOT FOUND");

        // Check that the response body contains an error message
        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody, "Response body should not be null");
        assertEquals("No tournament found with the provided ID.", responseBody.get("error").toString().trim(), "Error message should match");
    }
  
    @Test
    public void registerUserForTournament_UserNotFound() throws Exception {
        // Arrange
        // Create and save a tournament, but do not create a user
        DocumentReference tournamentRef = firestore.collection("Tournaments").document();
        String tournamentId = tournamentRef.getId();

        Tournament tournament = new Tournament();
        tournament.setTid(tournamentId);
        tournament.setName("Test Tournament");
        tournamentRef.set(tournament).get();
        createdTournamentIds.add(tournamentId);

        // Prepare a non-existent user ID
        String nonExistentAuthId = "nonExistentAuthId";

        // Prepare request to register the non-existent user for the tournament
        URI uri = new URI(baseUrl + port + "/user/registerTournament/" + tournamentId + "/" + nonExistentAuthId);

        // Create DTO for the request
        UserDTO userDto = new UserDTO();
        userDto.setAuthId(nonExistentAuthId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<UserDTO> requestEntity = new HttpEntity<>(userDto, headers);

        // Act and Assert
        ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, Map.class);

        // Assert HTTP Status
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Expected HTTP 404 NOT FOUND");

        // Check that the response body contains an error message
        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody, "Response body should not be null");
        assertEquals("User not found.", responseBody.get("error").toString().trim(), "Error message should match");
    }
}