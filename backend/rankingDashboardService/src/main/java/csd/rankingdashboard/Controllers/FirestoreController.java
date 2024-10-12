package csd.rankingdashboard.Controllers;

import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import csd.rankingdashboard.Model.User;
import csd.rankingdashboard.Service.FirestoreService;
import java.util.List;


@RestController
@RequestMapping("/api/Users") // Lowercase to follow URL convention
public class FirestoreController {

    @Autowired
    private FirestoreService userService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            List<User> players = userService.getAllUsers();
            return ResponseEntity.ok(players);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}