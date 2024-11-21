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

    // @Test
    // public void registerUserForTournament_Success() throws Exception {
    //     // Arrange
    //     DocumentReference tournamentRef = firestore.collection("Tournaments").document();
    //     String tournamentId = tournamentRef.getId();

    //     Tournament tournament = new Tournament();
    //     tournament.setTid(tournamentId);
    //     tournament.setName("Test Tournament");
    //     tournament.setCapacity(10);
    //     tournament.setEloRequirement(800); // Ensure this is set correctly
    //     tournament.setAgeLimit(18);        // Set an age limit to verify
    //     tournament.setDescription("A test tournament");
    //     tournament.setPrize(500);
    //     tournament.setLocation("Test Location");
    //     tournament.setStatus("Registration Open");

    //     // Set timestamps for start and end times
    //     Timestamp now = Timestamp.now();
    //     tournament.setCreatedTimestamp(now);
    //     tournament.setStartDatetime(Timestamp.ofTimeSecondsAndNanos(now.getSeconds() + 3600, 0)); // 1 hour later
    //     tournament.setEndDatetime(Timestamp.ofTimeSecondsAndNanos(now.getSeconds() + 7200, 0));   // 2 hours later
    //     tournament.setUsers(new ArrayList<>());  // No users initially

    //     // Save tournament to Firestore
    //     tournamentRef.set(tournament).get();
    //     createdTournamentIds.add(tournamentId);

    //     // Create a user in Firestore
    //     User user = new User();
    //     user.setAuthId("auth1");
    //     user.setEmail("testuser@example.com");
    //     user.setElo(1000);  // Set Elo to match the requirement

    //     // User is 20 years old
    //     LocalDate dateOfBirth = LocalDate.now().minusYears(20);
    //     Instant dobInstant = dateOfBirth.atStartOfDay(ZoneId.systemDefault()).toInstant();
    //     user.setDateOfBirth(Timestamp.ofTimeSecondsAndNanos(dobInstant.getEpochSecond(), dobInstant.getNano()));
    //     user.setRegistrationHistory(new ArrayList<>());  // No history initially

    //     // Save user to Firestore
    //     DocumentReference userRef = firestore.collection("Users").document();
    //     user.setUid(userRef.getId());
    //     userRef.set(user).get();
    //     createdUserUids.add(userRef.getId());

    //     // Prepare request to register the user for the tournament
    //     URI uri = new URI(baseUrl + port + "/user/registerTournament/" + tournamentId);

    //     // Create DTO for the request
    //     UserDTO userDto = new UserDTO();
    //     userDto.setAuthId(user.getAuthId());

    //     HttpHeaders headers = new HttpHeaders();
    //     headers.setContentType(MediaType.APPLICATION_JSON);
    //     headers.setAccept(List.of(MediaType.APPLICATION_JSON));  // Expect JSON response

    //     HttpEntity<UserDTO> requestEntity = new HttpEntity<>(userDto, headers);

    //     // Act
    //     ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, Map.class);

    //     // Assert HTTP Status
    //     assertEquals(HttpStatus.OK, response.getStatusCode(), "Expected HTTP 200 OK");
    //     Map<String, String> responseBody = response.getBody();
    //     assertNotNull(responseBody, "Response body should not be null");

    //     // Validate the message in the response
    //     assertEquals("User successfully registered for the tournament.", responseBody.get("message").trim());

    //     // Verify the user has been added to the tournament's user list
    //     Tournament updatedTournament = tournamentRef.get().get().toObject(Tournament.class);
    //     assertNotNull(updatedTournament, "Tournament should not be null");
    //     assertTrue(updatedTournament.getUsers().contains(user.getAuthId()), "User should be registered in the tournament");

    //     // Verify the tournament ID is in the user's registration history
    //     User updatedUser = userRef.get().get().toObject(User.class);
    //     assertNotNull(updatedUser, "User should not be null");

    // }

    // @Test
    // public void registerUserForTournament_Failure_InvalidTournamentId() throws Exception {
    //     // Arrange
    //     User user = new User();
    //     user.setAuthId("auth1");
    //     user.setEmail("testuser@example.com");

    //     // User is 20 years old
    //     LocalDate dateOfBirth = LocalDate.now().minusYears(20);
    //     Instant dobInstant = dateOfBirth.atStartOfDay(ZoneId.systemDefault()).toInstant();
    //     user.setDateOfBirth(Timestamp.ofTimeSecondsAndNanos(dobInstant.getEpochSecond(), dobInstant.getNano()));
    //     user.setRegistrationHistory(new ArrayList<>());

    //     // Save the user to Firestore
    //     DocumentReference userRef = firestore.collection("Users").document();
    //     user.setUid(userRef.getId());
    //     userRef.set(user).get();  // Ensure synchronous Firestore write
    //     createdUserUids.add(userRef.getId());  // Add the created user ID to cleanup list

    //     // Act: Use a non-existent tournament ID
    //     String invalidTournamentId = "invalid_tournament_id";

    //     // Create the registration URI with the invalid tournament ID
    //     URI uri = new URI(baseUrl + port + "/user/registerTournament/" + invalidTournamentId);

    //     // Create a UserDTO object for the request
    //     UserDTO userDto = new UserDTO();
    //     userDto.setAuthId(user.getAuthId());

    //     // Create request headers
    //     HttpHeaders headers = new HttpHeaders();
    //     headers.setContentType(MediaType.APPLICATION_JSON);
    //     headers.setAccept(List.of(MediaType.APPLICATION_JSON));  // Expect JSON response

    //     // Prepare the request entity
    //     HttpEntity<UserDTO> requestEntity = new HttpEntity<>(userDto, headers);

    //     // Act
    //     ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, Map.class);

    //     // Assert
    //     assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Expected HTTP 404 Not Found");

    //     // Assert: Ensure the response body contains the correct error message in the Map
    //     Map<String, String> responseBody = response.getBody();
    //     assertNotNull(responseBody, "Response body should not be null");
    //     assertEquals("No tournament found with the provided ID.", responseBody.get("error").trim());

    //     // Assert: Ensure the user's registration history has not been updated
    //     User updatedUser = userRef.get().get().toObject(User.class);
    //     assertNotNull(updatedUser, "User should not be null");
    //     assertTrue(updatedUser.getRegistrationHistory().isEmpty(), "User's registration history should remain empty");
    // }

  
    // @Test
    // public void unregisterUserFromTournament_Success() throws Exception {
    //     // Arrange
    //     DocumentReference tournamentRef = firestore.collection("Tournaments").document();
    //     String tournamentId = tournamentRef.getId();
    
    //     Tournament tournament = new Tournament();
    //     tournament.setTid(tournamentId);
    //     tournament.setName("Test Tournament");
    //     tournament.setCapacity(10);
    //     tournament.setEloRequirement(0);
    //     tournament.setAgeLimit(0);
    //     tournament.setDescription("A test tournament");
    //     tournament.setPrize(500);
    //     tournament.setLocation("Test Location");
    //     tournament.setStatus("Registration Open");
    //     tournament.setUsers(new ArrayList<>());  // No users initially
    
    //     // Save tournament to Firestore
    //     tournamentRef.set(tournament).get();
    //     createdTournamentIds.add(tournamentId);
    
    //     // Arrange: Create a user in Firestore and register them for the tournament
    //     User user = new User();
    //     user.setAuthId("auth1");
    //     user.setEmail("testuser@example.com");
    
    //     // User is 20 years old
    //     LocalDate dateOfBirth = LocalDate.now().minusYears(20);
    //     Instant dobInstant = dateOfBirth.atStartOfDay(ZoneId.systemDefault()).toInstant();
    //     user.setDateOfBirth(Timestamp.ofTimeSecondsAndNanos(dobInstant.getEpochSecond(), dobInstant.getNano()));
    
    //     List<String> registrationHistory = new ArrayList<>();
    //     registrationHistory.add(tournamentId);  // Add tournament ID to the user's registration history
    //     user.setRegistrationHistory(registrationHistory);
    
    //     // Save the user to Firestore
    //     DocumentReference userRef = firestore.collection("Users").document();
    //     user.setUid(userRef.getId());
    //     userRef.set(user).get();
    //     createdUserUids.add(userRef.getId());
    
    //     // Add the user to the tournament's user list
    //     List<String> tournamentUsers = new ArrayList<>();
    //     tournamentUsers.add(user.getAuthId());
    //     tournamentRef.update("users", tournamentUsers).get();
    
    //     // Act: Prepare request to unregister the user from the tournament
    //     URI uri = new URI(baseUrl + port + "/user/unregisterTournament/" + tournamentId);
    
    //     UserDTO userDto = new UserDTO();
    //     userDto.setAuthId(user.getAuthId());
    
    //     HttpHeaders headers = new HttpHeaders();
    //     headers.setContentType(MediaType.APPLICATION_JSON);
    //     headers.setAccept(List.of(MediaType.APPLICATION_JSON));  // Expect JSON response
    
    //     HttpEntity<UserDTO> requestEntity = new HttpEntity<>(userDto, headers);
    
    //     // Act: Make the API request to unregister the user using PUT
    //     ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, Map.class);
    
    //     // Assert: Ensure the response is 200 OK
    //     assertEquals(HttpStatus.OK, response.getStatusCode(), "Expected HTTP 200 OK");
    
    //     Map<String, String> responseBody = response.getBody();
    //     assertNotNull(responseBody, "Response body should not be null");
    //     assertEquals("User successfully unregistered from the tournament.", responseBody.get("message").trim());
    
    //     // Assert: Ensure the user's registration history no longer contains the tournament ID
    //     User updatedUser = userRef.get().get().toObject(User.class);
    //     assertNotNull(updatedUser, "User should not be null");
    //     assertFalse(updatedUser.getRegistrationHistory().contains(tournamentId),
    //             "User's registration history should no longer contain the tournament ID");
    
    //     // Assert: Ensure the tournament's user list no longer contains the user's auth ID
    //     Tournament updatedTournament = tournamentRef.get().get().toObject(Tournament.class);
    //     assertNotNull(updatedTournament, "Tournament should not be null");
    //     assertFalse(updatedTournament.getUsers().contains(user.getAuthId()),
    //             "Tournament's user list should no longer contain the user's auth ID");
    // }
}