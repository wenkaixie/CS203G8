package csd.matchresult.Controllers;

import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import csd.matchresult.Model.Tournament;
import csd.matchresult.Model.User;
import csd.matchresult.Service.FirestoreService;
import java.util.List;


@RestController
@RequestMapping("/api") // Lowercase to follow URL convention
public class FirestoreController {

    @Autowired
    private FirestoreService firestoreService;

    // @GetMapping("/Users")
    // public ResponseEntity<List<User>> getAllUsers() {
    //     try {
    //         List<User> players = firestoreService.getAllUsers();
    //         return ResponseEntity.ok(players);
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    //     }
    // }

    // @GetMapping("/Tournaments")
    // public ResponseEntity<List<Tournament>> getAllTournaments() {
    //     try {
    //         List<Tournament> tournaments = firestoreService.getAllTournaments();
    //         return ResponseEntity.ok(tournaments);
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    //     }
    // }

}