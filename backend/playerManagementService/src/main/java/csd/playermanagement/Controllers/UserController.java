package csd.playermanagement.Controllers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.csd.shared_library.model.User;

import csd.playermanagement.DTO.UserDTO;
import csd.playermanagement.Service.UserService;
import lombok.extern.slf4j.Slf4j;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/createUser")
    public ResponseEntity<User> createUser(@RequestBody User newUser) {
        log.info("Received request to create a new user: {}", newUser);
        try {
            User response = userService.createUserProfile(newUser);
            log.info("User created successfully with ID: {}", response.getAuthId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null); // Return appropriate response
        }
    }

    @PostMapping("/{userID}/add-tournament")
    public ResponseEntity<Void> addTournamentToUser(@PathVariable String userID, @RequestBody String tournamentID) {
        log.info("Received request to add tournament {} to user {}", tournamentID, userID);
        try {
            userService.addTournamentToUser(userID, tournamentID);
            log.info("Tournament {} added to user {} successfully.", tournamentID, userID);
            return ResponseEntity.ok().build();
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error adding tournament {} to user {}: {}", tournamentID, userID, e.getMessage(), e);
            // Optionally propagate error for rollback
            throw new RuntimeException("Error adding tournament to user: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error adding tournament {} to user {}: {}", tournamentID, userID, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{userID}/remove-tournament")
    public ResponseEntity<Void> removeTournamentFromUser(@PathVariable String userID,
            @RequestBody String tournamentID) {
        log.info("Received request to remove tournament {} from user {}", tournamentID, userID);
        try {
            userService.removeTournamentFromUser(userID, tournamentID);
            log.info("Tournament {} removed from user {} successfully.", tournamentID, userID);
            return ResponseEntity.ok().build();
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error removing tournament {} from user {}: {}", tournamentID, userID, e.getMessage(), e);
            // Optionally propagate error for rollback
            throw new RuntimeException("Error removing tournament from user: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error removing tournament {} from user {}: {}", tournamentID, userID, e.getMessage(),
                    e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/elo/batch")
    public ResponseEntity<Void> updatePlayerEloBatch(@RequestBody Map<String, Integer> eloUpdates) {
        try {
            userService.updatePlayerEloBatch(eloUpdates);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    // @PutMapping("/registerTournament/{tournamentId}/{authId}")
    // public ResponseEntity<Object> registerUserForTournament(@PathVariable String tournamentId,
    //         @PathVariable String authId) throws InterruptedException,
    //         ExecutionException {
    //     Map<String, String> response = new HashMap<>();
    //     response.put("message", userService.registerUserForTournament(tournamentId,
    //             authId));
    //     return ResponseEntity.ok(response); // Will be serialized to JSON

    // }

    // @PutMapping("/unregisterTournament/{tournamentId}/{authId}")
    // public ResponseEntity<Object> unregisterUserFromTournament(@PathVariable String tournamentId,
    //         @PathVariable String authId) throws InterruptedException, ExecutionException {
    //     Map<String, String> response = new HashMap<>();
    //     response.put("message", userService.unregisterUserForTournament(tournamentId,
    //             authId));
    //     return ResponseEntity.ok(response); // Will be serialized to JSON

    // }

    @PutMapping("/updateUser/{userID}")
    public ResponseEntity<Object> updateUserProfile(@PathVariable String userID, @RequestBody UserDTO updatedUser) {
        log.info("Received request to update user profile for user ID: {}", userID);
        try {
            UserDTO updatedUserProfile = userService.updateUserProfile(userID, updatedUser);
            log.info("User profile for user ID {} updated successfully.", userID);
            return ResponseEntity.ok(updatedUserProfile);
        } catch (Exception e) {
            log.error("Error updating user profile for user ID {}: {}", userID, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating user profile: " + e.getMessage());
        }
    }

    @GetMapping("/getAllUsers")
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("Received request to fetch all users.");
        try {
            List<User> users = userService.getAllUsers();
            log.info("Successfully fetched all users. Total users: {}", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error fetching all users: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // why object ?
    @GetMapping("/getUser/{userID}")
    public ResponseEntity<Object> getUser(@PathVariable String userID) {
        log.info("Received request to fetch user with ID: {}", userID);
        try {
            User user = userService.getUserbyId(userID);
            log.info("User fetched successfully for ID: {}", userID);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Error fetching user with ID {}: {}", userID, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching user: " + e.getMessage());
        }
    }

    @GetMapping("/getUserRank/{userID}")
    public ResponseEntity<Object> getUserRank(@PathVariable String userID) {
        log.info("Received request to fetch rank for user ID: {}", userID);
        try {
            int rank = userService.getUserRank(userID);
            log.info("Rank fetched successfully for user ID {}: {}", userID, rank);
            return ResponseEntity.ok(rank);
        } catch (Exception e) {
            log.error("Error fetching rank for user ID {}: {}", userID, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching user rank: " + e.getMessage());
        }
    }
}

// @PutMapping("/registerTournament/{tournamentId}/{authId}")
// public ResponseEntity<Object> registerUserForTournament(@PathVariable String
// tournamentId, @PathVariable String authId) throws InterruptedException,
// ExecutionException {
// Map<String, String> response = new HashMap<>();
// response.put("message", userService.registerUserForTournament(tournamentId,
// authId));
// return ResponseEntity.ok(response); // Will be serialized to JSON

// }

// @PutMapping("/unregisterTournament/{tournamentId}/{authId}")
// public ResponseEntity<Object> unregisterUserFromTournament(@PathVariable
// String tournamentId, @PathVariable String authId) throws
// InterruptedException, ExecutionException{
// Map<String, String> response = new HashMap<>();
// response.put("message", userService.unregisterUserForTournament(tournamentId,
// authId));
// return ResponseEntity.ok(response); // Will be serialized to JSON

// }