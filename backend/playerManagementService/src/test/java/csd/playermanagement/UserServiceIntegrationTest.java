package csd.playermanagement;

// UserServiceIntegrationTest.java
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import csd.playermanagement.Model.User;
import csd.playermanagement.Service.UserService;

import java.util.List;

@SpringBootTest
public class UserServiceIntegrationTest {

//    ForTournament_Success() throws Exception {
//         // Arrange
//         String tournamentId = "validTournamentId";
//         String userId = "validUserId";

//         // Act
//         String result = userService.registerUserForTournament(tournamentId, userId);

//         // Assert
//         assertEquals("User successfully registered for the tournament.", result);
//     }

//     @Test
//     public void testCreateUserPro @Autowired
//     private UserService userService;

//     @BeforeEach
//     public void setUp() throws Exception {
//         // Optionally, set up any required initial data for the integration tests
//     }

//     @Test
//     public void testRegisterUserfile_Success() throws Exception {
//         // Arrange
//         User newUser = new User();
//         newUser.setUsername("integrationUser");
//         newUser.setEmail("integration@example.com");

//         // Act
//         User createdUser = userService.createUserProfile(newUser);

//         // Assert
//         assertNotNull(createdUser);
//         assertEquals("integrationUser", createdUser.getUsername());
//     }

//     @Test
//     public void testGetAllUsers() throws Exception {
//         // Act
//         List<User> users = userService.getAllUsers();

//         // Assert
//         assertNotNull(users);
//         assertTrue(users.size() >= 0); // This verifies that the service returns a list without errors
//     }
}