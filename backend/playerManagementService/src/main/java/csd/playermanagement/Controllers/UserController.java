package csd.playermanagement.Controllers;

import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.util.concurrent.ExecutionException;
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
@CrossOrigin(origins = "http://matchup-cs203g8.s3-website-ap-southeast-1.amazonaws.com")
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    private UserService userService;

    @PutMapping("/registerTournament/{tournamentId}/{authId}")
    public ResponseEntity<Object> registerUserForTournament(@PathVariable String tournamentId, @PathVariable String authId) throws InterruptedException, ExecutionException {
        Map<String, String> response = new HashMap<>();
        response.put("message", userService.registerUserForTournament(tournamentId, authId));
        return ResponseEntity.ok(response);  // Will be serialized to JSON
    
    }

    @PutMapping("/unregisterTournament/{tournamentId}/{authId}")
    public ResponseEntity<Object> unregisterUserFromTournament(@PathVariable String tournamentId, @PathVariable String authId) throws InterruptedException, ExecutionException{
        Map<String, String> response = new HashMap<>();
        response.put("message", userService.unregisterUserForTournament(tournamentId, authId));
        return ResponseEntity.ok(response); // Will be serialized to JSON
       
    }

    @PutMapping("/updateUser/{userID}")
    public ResponseEntity<Object> updateUserProfile(@PathVariable String userID, @RequestBody UserDTO updatedUser) throws InterruptedException, ExecutionException{
        UserDTO updatedUserProfile = userService.updateUserProfile(userID, updatedUser);
        return ResponseEntity.ok(updatedUserProfile);  // Return the updated User object
       
    }
    
    @PostMapping("/createUser")
    public ResponseEntity<User> createUser(@RequestBody User newUser) throws InterruptedException, ExecutionException {
        User response = userService.createUserProfile(newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
       
    }

    @GetMapping("/getAllUsers")
    public ResponseEntity<List<User>> getAllUsers()throws InterruptedException, ExecutionException {  
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    
    }

    @GetMapping("/getUser/{userID}")
    public ResponseEntity<Object> getUser(@PathVariable String userID) throws InterruptedException, ExecutionException{
        User user = userService.getUserbyId(userID);
        return ResponseEntity.ok(user);  
      
    }
    
    @GetMapping("/getUserRank/{userID}")
    public ResponseEntity<Object> getUserRank(@PathVariable String userID) throws InterruptedException, ExecutionException{
        int rank = userService.getUserRank(userID);
        return ResponseEntity.ok(rank);
      
    }
    


}
