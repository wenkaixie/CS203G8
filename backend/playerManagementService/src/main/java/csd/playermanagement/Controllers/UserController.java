package csd.playermanagement.Controllers;

import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import csd.playermanagement.Model.User;
import csd.playermanagement.DTO.UserDTO;
import csd.playermanagement.Exception.TournamentNotFoundException;
import csd.playermanagement.Exception.UserNotFoundException;
import csd.playermanagement.Exception.UserTournamentException;
import csd.playermanagement.Model.Tournament;
import csd.playermanagement.Service.FirestoreService;
import csd.playermanagement.Service.UserService;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    private UserService userService;

    @PostMapping("/registerTournament/{tournamentId}")
    public ResponseEntity<Object> registerUserForTournament(@PathVariable String tournamentId, @RequestBody UserDTO userDto) {
        try {
            Map<String, String> response = new HashMap<>();
            response.put("message", userService.registerUserForTournament(tournamentId, userDto));
            return ResponseEntity.ok(response);  // Will be serialized to JSON
        } catch (TournamentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        } catch(UserTournamentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse("An unexpected error occurred."));
        }
    }

    @PutMapping("/unregisterTournament/{tournamentId}")
    public ResponseEntity<Object> unregisterUserFromTournament(@PathVariable String tournamentId, @RequestBody UserDTO userDto) {
        try {
            Map<String, String> response = new HashMap<>();
            response.put("message", userService.unregisterUserFromTournament(tournamentId, userDto));
            return ResponseEntity.ok(response); // Will be serialized to JSON
        } catch (TournamentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        } catch(UserTournamentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse("An unexpected error occurred."));
        }
    }

    @PutMapping("/updateUser/{userID}")
    public ResponseEntity<Object> updateUserProfile(@PathVariable String userID, @RequestBody UserDTO updatedUser) {
        try {
            UserDTO updatedUserProfile = userService.updateUserProfile(userID, updatedUser);
            return ResponseEntity.ok(updatedUserProfile);  // Return the updated User object
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse("An unexpected error occurred."));
        }
    }
    
    @PostMapping("/createUser")
    public ResponseEntity<User> createUser(@RequestBody User newUser) {
        try {
            User response = userService.createUserProfile(newUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/getAllUsers")
    public ResponseEntity<List<User>> getAllUsers(){
        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/getUser/{userID}")
    public ResponseEntity<Object> getUser(@PathVariable String userID) {
        try {
            User user = userService.getUserbyId(userID);
            return ResponseEntity.ok(user);  
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse("An unexpected error occurred."));
        }
    }
    
    @GetMapping("/getUserRank/{userID}")
    public ResponseEntity<Object> getUserRank(@PathVariable String userID) {
        try {
            int rank = userService.getUserRank(userID);
            return ResponseEntity.ok(rank);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse("An unexpected error occurred."));
        }
    }
    
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        return errorResponse;
    }

}
