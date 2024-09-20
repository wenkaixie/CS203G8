package com.example.elo.controller;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/elo")
public class EloController {

    // Update Elo ratings
    @PostMapping("/update")
    public ResponseEntity<Object> updateElo(
            @RequestParam String userId1,
            @RequestParam String userId2,
            @RequestBody EloUpdateRequest request) {

        double Elo1 = request.getElo1();
        double Elo2 = request.getElo2();
        double AS1 = request.getAS1();
        double AS2 = request.getAS2();

        if (userId1 == null || userId2 == null || Elo1 == 0 || Elo2 == 0) {
            return new ResponseEntity<>("userId1, userId2, Elo1, and Elo2 are required.", HttpStatus.BAD_REQUEST);
        }

        if (!(AS1 == 0 || AS1 == 0.5 || AS1 == 1) || !(AS2 == 0 || AS2 == 0.5 || AS2 == 1)) {
            return new ResponseEntity<>("AS1 and AS2 must be 0, 0.5, or 1.", HttpStatus.BAD_REQUEST);
        }

        double ES1 = 1 / (1 + Math.pow(10, ((Elo2 - Elo1) / 400)));
        double ES2 = 1 / (1 + Math.pow(10, ((Elo1 - Elo2) / 400)));
        int K1 = Elo1 < 2100 ? 32 : (Elo1 > 2400 ? 16 : 24);
        int K2 = Elo2 < 2100 ? 32 : (Elo2 > 2400 ? 16 : 24);
        double newElo1 = Elo1 + K1 * (AS1 - ES1);
        double newElo2 = Elo2 + K2 * (AS2 - ES2);

        Firestore db = FirestoreClient.getFirestore();

        try {
            db.collection("Users").document(userId1).update("Elo", newElo1);
            db.collection("Users").document(userId2).update("Elo", newElo2);

            return new ResponseEntity<>("Elo ratings successfully updated", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error updating Elo ratings: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get Elo rating for a user
    @GetMapping("/get")
    public ResponseEntity<Object> getElo(@RequestParam String userID) {
        Firestore db = FirestoreClient.getFirestore();

        try {
            DocumentSnapshot user = db.collection("Users").document(userID).get().get();
            if (!user.exists()) {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }

            Object elo = user.get("Elo");
            return new ResponseEntity<>(elo, HttpStatus.OK);
        } catch (InterruptedException | ExecutionException e) {
            return new ResponseEntity<>("Error getting user: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
