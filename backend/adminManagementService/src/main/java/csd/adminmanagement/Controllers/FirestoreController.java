package csd.adminmanagement.Controllers;

import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import csd.adminmanagement.Model.User;
import csd.adminmanagement.Model.Tournament;
import csd.adminmanagement.Service.FirestoreService;
import java.util.List;


@RestController
@RequestMapping("/api") // Lowercase to follow URL convention
public class FirestoreController {

    @Autowired
    private FirestoreService firestoreService;

    @GetMapping("/Admins")
    public ResponseEntity<List<User>> getAllAdmins() {
        try {
            List<User> players = firestoreService.getAllAdmins();
            return ResponseEntity.ok(players);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/Tournaments")
    public ResponseEntity<List<Tournament>> getAllTournaments() {
        try {
            List<Tournament> tournaments = firestoreService.getAllTournaments();
            return ResponseEntity.ok(tournaments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}