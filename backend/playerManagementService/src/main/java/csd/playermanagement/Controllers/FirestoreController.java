package csd.playermanagement.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.csd.shared_library.model.Tournament;
import com.csd.shared_library.model.User;

// import csd.playermanagement.Model.Tournament;
import csd.playermanagement.Service.FirestoreService;


@RestController
@RequestMapping("/api") // Lowercase to follow URL convention
public class FirestoreController {

    @Autowired
    private FirestoreService firestoreService;

    @GetMapping("/Users")
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            List<User> players = firestoreService.getAllUsers();
            return ResponseEntity.ok(players);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/Tournaments")
    public ResponseEntity<List<Tournament>> getAllTournaments() {
        System.out.println("calling getAllTournaments");
        try {
            List<Tournament> tournaments = firestoreService.getAllTournaments();
            return ResponseEntity.ok(tournaments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}