package csd.playermanagement;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

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


    @Test
    public void createUser_Success() throws Exception {
        URI uri = new URI(baseUrl + port + "/user/createUser");

        User newUser = new User();
        newUser.setAuthId("auth123");
        newUser.setUsername("testuser");
        newUser.setEmail("testuser@example.com");
        newUser.setName("Test User");
        newUser.setElo(1200);

        ResponseEntity<User> result = restTemplate.postForEntity(uri, newUser, User.class);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(newUser.getUsername(), result.getBody().getUsername());
        assertEquals(newUser.getEmail(), result.getBody().getEmail());
    }

    @Test
    public void getAllUsers_Success() throws Exception {
        // First, create some users
        User user1 = new User();
        user1.setAuthId("auth123");
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setName("User One");
        user1.setElo(1000);

        User user2 = new User();
        user2.setAuthId("auth456");
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setName("User Two");
        user2.setElo(1100);

        // Save users using the service or directly to Firestore
        userService.createUserProfile(user1);
        userService.createUserProfile(user2);

        URI uri = new URI(baseUrl + port + "/user/getAllUsers");

        ResponseEntity<User[]> result = restTemplate.getForEntity(uri, User[].class);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        User[] userList = result.getBody();
        assertNotNull(userList);
        assertEquals(2, userList.length);
        // Further assertions can be made to check the contents
    }

    @Test
    public void getUser_ValidUserId_Success() throws Exception {
        User user = new User();
        user.setAuthId("auth123");
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        user.setName("Test User");
        user.setElo(1200);

        // Save the user
        userService.createUserProfile(user);

        String userId = user.getAuthId();

        URI uri = new URI(baseUrl + port + "/user/getUser/" + userId);

        ResponseEntity<User> result = restTemplate.getForEntity(uri, User.class);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(user.getUsername(), result.getBody().getUsername());
        assertEquals(user.getEmail(), result.getBody().getEmail());
    }

    @Test
    public void getUser_InvalidUserId_Failure() throws Exception {
        String invalidUserId = "nonexistent";

        URI uri = new URI(baseUrl + port + "/user/getUser/" + invalidUserId);

        ResponseEntity<String> result = restTemplate.getForEntity(uri, String.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        // Optionally, check the response body for error message
    }

    @Test
    public void updateUser_ValidUserId_Success() throws Exception {
        User user = new User();
        user.setAuthId("auth123");
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        user.setName("Test User");
        user.setElo(1200);

        // Save the user
        userService.createUserProfile(user);

        String userId = user.getAuthId();

        URI uri = new URI(baseUrl + port + "/user/updateUser/" + userId);

        UserDTO updatedUser = new UserDTO();
        updatedUser.setAuthId(user.getAuthId()); // Set authId
        updatedUser.setUsername("updatedUser");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setName("Updated User");
        updatedUser.setElo(1300);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<UserDTO> requestEntity = new HttpEntity<>(updatedUser, headers);

        ResponseEntity<Map> result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, Map.class);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(updatedUser.getUsername(), result.getBody().get("username"));
        assertEquals(updatedUser.getEmail(), result.getBody().get("email"));
    }

    @Test
    public void updateUser_InvalidUserId_Failure() throws Exception {
        String invalidUserId = "nonexistent";

        URI uri = new URI(baseUrl + port + "/user/updateUser/" + invalidUserId);

        UserDTO updatedUser = new UserDTO();
        updatedUser.setAuthId(invalidUserId); // Set authId to invalidUserId
        updatedUser.setUsername("updatedUser");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setName("Updated User");
        updatedUser.setElo(1300);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<UserDTO> requestEntity = new HttpEntity<>(updatedUser, headers);

        ResponseEntity<String> result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        // Optionally, check the response body for error message
    }

    @Test
    public void registerUserForTournament_Success() throws Exception {
        // Create a user
        User user = new User();
        user.setAuthId("auth123");
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        user.setName("Test User");
        user.setElo(1500);

        // Save the user
        userService.createUserProfile(user);

        // Assume the tournament is already created with ID "tournament123"
        String tournamentId = "tournament123";

        // Prepare the UserDTO
        UserDTO userDto = new UserDTO();
        userDto.setAuthId(user.getAuthId());

        URI uri = new URI(baseUrl + port + "/user/registerTournament/" + tournamentId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<UserDTO> requestEntity = new HttpEntity<>(userDto, headers);

        ResponseEntity<String> result = restTemplate.postForEntity(uri, requestEntity, String.class);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("User successfully registered for the tournament.", result.getBody());
    }

    @Test
    public void registerUserForTournament_InvalidTournamentId_Failure() throws Exception {
        // Create a user
        User user = new User();
        user.setAuthId("auth123");
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        user.setName("Test User");
        user.setElo(1500);

        // Save the user
        userService.createUserProfile(user);

        String invalidTournamentId = "invalidTournament";

        // Prepare the UserDTO
        UserDTO userDto = new UserDTO();
        userDto.setAuthId(user.getAuthId());

        URI uri = new URI(baseUrl + port + "/user/registerTournament/" + invalidTournamentId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<UserDTO> requestEntity = new HttpEntity<>(userDto, headers);

        ResponseEntity<String> result = restTemplate.postForEntity(uri, requestEntity, String.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        // Optionally, check the response body for error message
    }
}