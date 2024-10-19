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

import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers.TimestampDeserializer;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

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

    @BeforeEach
    public void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        // Register the mixin
        objectMapper.addMixIn(User.class, IgnoreDateOfBirthMixin.class);
        objectMapper.addMixIn(Tournament.class, IgnoreTimestampFieldsMixin.class);

        // Configure the RestTemplate to use the custom ObjectMapper
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
        restTemplate.getRestTemplate().getMessageConverters().add(0, converter);
        restTemplate.getRestTemplate().getMessageConverters().add(0, new StringHttpMessageConverter());
    }

    // Define the mixin within your test class
    @JsonIgnoreProperties({"dateOfBirth"})
    public abstract class IgnoreDateOfBirthMixin {
        // No need to include any methods or fields
    }
    @JsonIgnoreProperties({"dateOfBirth", "createdTimestamp", "startDatetime", "endDatetime"})
    public abstract class IgnoreTimestampFieldsMixin {
        // No need to include any methods or fields
    }


    @AfterEach
    public void cleanUp() {
        for (String uid : createdUserUids) {
            try {
                firestore.collection("Users").document(uid).delete().get();
            } catch (Exception e) {
                System.err.println("Failed to delete user with UID: " + uid);
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

    // @Test
    // public void createUser_Success() throws Exception {
    //     URI uri = new URI(baseUrl + port + "/user/createUser");

    //     User user1 = new User();
    //     user1.setAuthId("auth123");
    //     user1.setUsername("testuser");


    //     ResponseEntity<User> result = restTemplate.postForEntity(uri, user1, User.class);

    //     assertEquals(HttpStatus.CREATED, result.getStatusCode());
    //     assertNotNull(result.getBody());
    //     assertEquals(user1.getUsername(), result.getBody().getUsername());
    //     assertEquals(user1.getEmail(), result.getBody().getEmail());

    //     // for teardown
    //     // Keep track of the created user's UID
    //     String createdUid = result.getBody().getUid();
    //     System.out.println("Created UID: " + createdUid); // Debugging line
    //     assertNotNull(createdUid, "Created UID should not be null");
    //     createdUserUids.add(createdUid);
    // }


    @Test
    public void getAllUsers_Success() throws Exception {
        // Arrange
        // Create users
        User user1 = new User();
        user1.setAuthId("auth1");
        user1.setUsername("user1");

        User user2 = new User();
        user2.setAuthId("auth2");
        user2.setUsername("user2");

        // Save users to Firestore
        DocumentReference userRef1 = firestore.collection("Users").document();
        user1.setUid(userRef1.getId());
        userRef1.set(user1).get();
        createdUserUids.add(userRef1.getId());

        DocumentReference userRef2 = firestore.collection("Users").document();
        user2.setUid(userRef2.getId());
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
        // Create a user directly in Firestore
        User user1 = new User();
        user1.setAuthId("auth1");
        user1.setUsername("testuser");
        user1.setEmail("testuser@example.com");
    
        // Save the user to Firestore
        DocumentReference userRef = firestore.collection("Users").document();
        user1.setUid(userRef.getId());
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
        assertEquals(user1.getUid(), retrievedUser.getUid());
        assertEquals(user1.getUsername(), retrievedUser.getUsername());
        assertEquals(user1.getEmail(), retrievedUser.getEmail());
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
    public void updateUser_Success() throws Exception {
        // Arrange
        // Create a user directly in Firestore
        User user = new User();
        user.setAuthId("auth1");
        user.setEmail("original@example.com");
  
        // Save the user to Firestore
        DocumentReference userRef = firestore.collection("Users").document();
        user.setUid(userRef.getId());
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
        String invalidUserId = "nonexistent_user_id_";

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

        // Act
        ResponseEntity<Map> response = null;
        try {
            response = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, Map.class);
            // If no exception, check for unexpected status code
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "Expected 404 Not Found");
        } catch (HttpClientErrorException ex) {
            // Capture the error response
            response = new ResponseEntity<>(ex.getResponseBodyAs(Map.class), ex.getStatusCode());
        }

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

    }

    @Test
    public void registerUserForTournament_Success() throws Exception {
        // Arrange

        // Step 1: Create a tournament in Firestore
        DocumentReference tournamentRef = firestore.collection("Tournaments").document();
        String tournamentId = tournamentRef.getId();

        Tournament tournament = new Tournament();
        tournament.setTid(tournamentId);
        tournament.setName("Test Tournament");
        tournament.setCapacity(10);
        tournament.setEloRequirement(0);
        tournament.setAgeLimit(0);
        tournament.setDescription("A test tournament");
        tournament.setPrize(500);
        tournament.setLocation("Test Location");
        tournament.setStatus("Registration Open");

        Timestamp now = Timestamp.now();
        tournament.setCreatedTimestamp(now);
        tournament.setStartDatetime(Timestamp.ofTimeSecondsAndNanos(now.getSeconds() + 3600, 0));
        tournament.setEndDatetime(Timestamp.ofTimeSecondsAndNanos(now.getSeconds() + 7200, 0));
        tournament.setUsers(new ArrayList<>());

        tournamentRef.set(tournament).get();
        createdTournamentIds.add(tournamentId);

        // Step 2: Create a user in Firestore
        User user = new User();
        user.setAuthId("auth1");
        user.setEmail("testuser@example.com");

        LocalDate dateOfBirth = LocalDate.now().minusYears(20); // User is 20 years old
        Instant dobInstant = dateOfBirth.atStartOfDay(ZoneId.systemDefault()).toInstant();
        user.setDateOfBirth(Timestamp.ofTimeSecondsAndNanos(dobInstant.getEpochSecond(), dobInstant.getNano()));
        user.setRegistrationHistory(new ArrayList<>());

        DocumentReference userRef = firestore.collection("Users").document();
        user.setUid(userRef.getId());
        userRef.set(user).get();
        createdUserUids.add(userRef.getId());

        URI uri = new URI(baseUrl + port + "/user/registerTournament/" + tournamentId);

        UserDTO userDto = new UserDTO();
        userDto.setAuthId(user.getAuthId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));  // Accept both JSON and plain text
        
        HttpEntity<UserDTO> requestEntity = new HttpEntity<>(userDto, headers);
        
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class);

        // Assert status and body
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Expected HTTP 200 OK");
        assertNotNull(response.getBody(), "Response body should not be null");

        // Check if the response looks like JSON
        String responseBody = response.getBody();
        if (responseBody.trim().startsWith("{")) {
            // Attempt to parse the response as JSON
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> jsonResponse = objectMapper.readValue(responseBody, Map.class);
            assertEquals("User successfully registered for the tournament.", jsonResponse.get("message"));
        } else {
            // Handle plain text response
            System.out.println("headers: " + headers);
            System.out.println("response: " + response);
            System.out.println("response body: " + response.getBody());
            System.out.println("Received plain text response: " + responseBody);
            assertEquals("User successfully registered for the tournament.", responseBody.trim());
        }

        // Validate the message in the response
        assertEquals("User successfully registered for the tournament.", responseBody.trim());
        // Verify the user has been added to the tournament's user list
        Tournament updatedTournament = tournamentRef.get().get().toObject(Tournament.class);
        assertNotNull(updatedTournament, "Tournament should not be null");
        assertTrue(updatedTournament.getUsers().contains(user.getAuthId()), "User should be registered in the tournament");

        // Verify the tournament ID is in the user's registration history
        User updatedUser = userRef.get().get().toObject(User.class);
        assertNotNull(updatedUser, "User should not be null");
        assertTrue(updatedUser.getRegistrationHistory().contains(tournamentId), 
                "Tournament ID should be in user's registration history");
    }

    @Test
    public void registerUserForTournament_Failure_InvalidTournamentId() throws Exception {
        // Arrange: Create a user in Firestore
        User user = new User();
        user.setAuthId("auth1");
        user.setEmail("testuser@example.com");

        LocalDate dateOfBirth = LocalDate.now().minusYears(20); // User is 20 years old
        Instant dobInstant = dateOfBirth.atStartOfDay(ZoneId.systemDefault()).toInstant();
        user.setDateOfBirth(Timestamp.ofTimeSecondsAndNanos(dobInstant.getEpochSecond(), dobInstant.getNano()));
        user.setRegistrationHistory(new ArrayList<>());

        DocumentReference userRef = firestore.collection("Users").document();
        user.setUid(userRef.getId());
        userRef.set(user).get();
        createdUserUids.add(userRef.getId());

        // Act: Use a non-existent tournament ID
        String invalidTournamentId = "invalid_tournament_id";

        URI uri = new URI(baseUrl + port + "/user/registerTournament/" + invalidTournamentId);

        UserDTO userDto = new UserDTO();
        userDto.setAuthId(user.getAuthId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));  // Accept both JSON and plain text

        HttpEntity<UserDTO> requestEntity = new HttpEntity<>(userDto, headers);

        // Act: Make the API request
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class);

        // Debug: Print the response for troubleshooting
        System.out.println("Response Status Code: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody());

        // Assert: Ensure the response is 500 INTERNAL_SERVER_ERROR
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "Expected HTTP 500 INTERNAL SERVER ERROR");

        // Optionally, check if the response body contains the expected error message
        assertNull(response.getBody(), "Response body should be null");

        // Verify the user's registration history was not updated
        User updatedUser = userRef.get().get().toObject(User.class);
        assertNotNull(updatedUser, "User should not be null");
        assertTrue(updatedUser.getRegistrationHistory().isEmpty(), 
                "User's registration history should remain empty");
    }
}