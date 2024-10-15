// package csd.playermanagement;

// import static org.junit.jupiter.api.Assertions.*;

// import java.net.URI;
// import java.util.List;
// import java.util.Map;
// import java.util.concurrent.ExecutionException;

// import org.junit.jupiter.api.AfterEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
// import org.springframework.boot.test.web.client.TestRestTemplate;
// import org.springframework.boot.test.web.server.LocalServerPort;
// import org.springframework.http.*;

// import com.google.cloud.firestore.Firestore;
// import com.google.cloud.firestore.QueryDocumentSnapshot;
// import com.google.cloud.firestore.QuerySnapshot;

// import csd.playermanagement.Model.User;
// import csd.playermanagement.DTO.UserDTO;
// import csd.playermanagement.Service.UserService;

// @SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
// public class UserIntegrationTest {

//     @LocalServerPort
//     private int port;

//     private final String baseUrl = "http://localhost:";

//     @Autowired
//     private TestRestTemplate restTemplate;

//     @Autowired
//     private Firestore firestore;

//     @Autowired
//     private UserService userService;


//     // @Test
//     // public void createUser_Success() throws Exception {
//     //     URI uri = new URI(baseUrl + port + "/user/createUser");

//     //     User newUser = new User();
//     //     newUser.setAuthId("auth123");
//     //     newUser.setUsername("testuser");


//     //     ResponseEntity<User> result = restTemplate.postForEntity(uri, newUser, User.class);

//     //     assertEquals(HttpStatus.CREATED, result.getStatusCode());
//     //     assertNotNull(result.getBody());
//     //     assertEquals(newUser.getUsername(), result.getBody().getUsername());
//     //     assertEquals(newUser.getEmail(), result.getBody().getEmail());
//     // }

//     // @Test
//     // public void getupdateUsers_Success() throws Exception {

//     //     // Arrange users
//     //     User user1 = new User();
//     //     user1.setAuthId("auth123");
//     //     user1.setEmail("user1@gmail.com");

//     //     // Arrange Dto
//     //     UserDTO user1Dto = new UserDTO();
//     //     user1Dto.setUsername("user1");
//     //     user1Dto.setName("User One");
//     //     user1Dto.setChessUsername("chessUser1");
//     //     user1Dto.setNationality("SG");
//     //     user1Dto.setPhoneNumber(12345678);
//     //     user1Dto.setDateOfBirth("2002-12-12");


//     //     // Act: Use the UserService to add users
//     //     userService.updateUserProfile(user1.getAuthId(), user1Dto);

//     //     URI uri = new URI(baseUrl + port + "/user/getAllUsers");

//     //     // Act: Send a PUT request with the updated user data
//     //     HttpHeaders headers = new HttpHeaders();
//     //     headers.setContentType(MediaType.APPLICATION_JSON);
//     //     HttpEntity<UserDTO> requestEntity = new HttpEntity<>(user1Dto, headers);

//     //     ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, Map.class);

//     //     assertEquals(200, response.getStatusCode().value());
// 	// 	assertEquals(user1.getEmail(), response.getBody().get("email"));
//     //     // Further assertions can be made to check the contents
//     // }

// //     @Test
// //     public void getUser_ValidUserId_Success() throws Exception {
// //         User user = new User();
// //         user.setAuthId("auth123");
// //         user.setUsername("testuser");
// //         user.setEmail("testuser@example.com");
// //         user.setName("Test User");
// //         user.setElo(1200);

// //         // Save the user
// //         userService.createUserProfile(user);

// //         String userId = user.getAuthId();

// //         URI uri = new URI(baseUrl + port + "/user/getUser/" + userId);

// //         ResponseEntity<User> result = restTemplate.getForEntity(uri, User.class);

// //         assertEquals(HttpStatus.OK, result.getStatusCode());
// //         assertNotNull(result.getBody());
// //         assertEquals(user.getUsername(), result.getBody().getUsername());
// //         assertEquals(user.getEmail(), result.getBody().getEmail());
// //     }

// //     @Test
// //     public void getUser_InvalidUserId_Failure() throws Exception {
// //         String invalidUserId = "nonexistent";

// //         URI uri = new URI(baseUrl + port + "/user/getUser/" + invalidUserId);

// //         ResponseEntity<String> result = restTemplate.getForEntity(uri, String.class);

// //         assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
// //         // Optionally, check the response body for error message
// //     }

// //     @Test
// //     public void updateUser_ValidUserId_Success() throws Exception {
// //         User user = new User();
// //         user.setAuthId("auth123");
// //         user.setUsername("testuser");
// //         user.setEmail("testuser@example.com");
// //         user.setName("Test User");
// //         user.setElo(1200);

// //         // Save the user
// //         userService.createUserProfile(user);

// //         String userId = user.getAuthId();

// //         URI uri = new URI(baseUrl + port + "/user/updateUser/" + userId);

// //         UserDTO updatedUser = new UserDTO();
// //         updatedUser.setAuthId(user.getAuthId()); // Set authId
// //         updatedUser.setUsername("updatedUser");
// //         updatedUser.setEmail("updated@example.com");
// //         updatedUser.setName("Updated User");
// //         updatedUser.setElo(1300);

// //         HttpHeaders headers = new HttpHeaders();
// //         headers.setContentType(MediaType.APPLICATION_JSON);

// //         HttpEntity<UserDTO> requestEntity = new HttpEntity<>(updatedUser, headers);

// //         ResponseEntity<Map> result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, Map.class);

// //         assertEquals(HttpStatus.OK, result.getStatusCode());
// //         assertNotNull(result.getBody());
// //         assertEquals(updatedUser.getUsername(), result.getBody().get("username"));
// //         assertEquals(updatedUser.getEmail(), result.getBody().get("email"));
// //     }

// //     @Test
// //     public void updateUser_InvalidUserId_Failure() throws Exception {
// //         String invalidUserId = "nonexistent";

// //         URI uri = new URI(baseUrl + port + "/user/updateUser/" + invalidUserId);

// //         UserDTO updatedUser = new UserDTO();
// //         updatedUser.setAuthId(invalidUserId); // Set authId to invalidUserId
// //         updatedUser.setUsername("updatedUser");
// //         updatedUser.setEmail("updated@example.com");
// //         updatedUser.setName("Updated User");
// //         updatedUser.setElo(1300);

// //         HttpHeaders headers = new HttpHeaders();
// //         headers.setContentType(MediaType.APPLICATION_JSON);

// //         HttpEntity<UserDTO> requestEntity = new HttpEntity<>(updatedUser, headers);

// //         ResponseEntity<String> result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String.class);

// //         assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
// //         // Optionally, check the response body for error message
// //     }

// //     @Test
// //     public void registerUserForTournament_Success() throws Exception {
// //         // Create a user
// //         User user = new User();
// //         user.setAuthId("auth123");
// //         user.setUsername("testuser");
// //         user.setEmail("testuser@example.com");
// //         user.setName("Test User");
// //         user.setElo(1500);

// //         // Save the user
// //         userService.createUserProfile(user);

// //         // Assume the tournament is already created with ID "tournament123"
// //         String tournamentId = "tournament123";

// //         // Prepare the UserDTO
// //         UserDTO userDto = new UserDTO();
// //         userDto.setAuthId(user.getAuthId());

// //         URI uri = new URI(baseUrl + port + "/user/registerTournament/" + tournamentId);

// //         HttpHeaders headers = new HttpHeaders();
// //         headers.setContentType(MediaType.APPLICATION_JSON);

// //         HttpEntity<UserDTO> requestEntity = new HttpEntity<>(userDto, headers);

// //         ResponseEntity<String> result = restTemplate.postForEntity(uri, requestEntity, String.class);

// //         assertEquals(HttpStatus.OK, result.getStatusCode());
// //         assertEquals("User successfully registered for the tournament.", result.getBody());
// //     }

// //     @Test
// //     public void registerUserForTournament_InvalidTournamentId_Failure() throws Exception {
// //         // Create a user
// //         User user = new User();
// //         user.setAuthId("auth123");
// //         user.setUsername("testuser");
// //         user.setEmail("testuser@example.com");
// //         user.setName("Test User");
// //         user.setElo(1500);

// //         // Save the user
// //         userService.createUserProfile(user);

// //         String invalidTournamentId = "invalidTournament";

// //         // Prepare the UserDTO
// //         UserDTO userDto = new UserDTO();
// //         userDto.setAuthId(user.getAuthId());

// //         URI uri = new URI(baseUrl + port + "/user/registerTournament/" + invalidTournamentId);

// //         HttpHeaders headers = new HttpHeaders();
// //         headers.setContentType(MediaType.APPLICATION_JSON);

// //         HttpEntity<UserDTO> requestEntity = new HttpEntity<>(userDto, headers);

// //         ResponseEntity<String> result = restTemplate.postForEntity(uri, requestEntity, String.class);

// //         assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
// //         // Optionally, check the response body for error message
// //     }
// }