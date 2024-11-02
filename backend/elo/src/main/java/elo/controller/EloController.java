package elo.controller;

import elo.model.EloUpdateRequest;
import elo.service.EloService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/elo")
public class EloController {

    private final EloService eloService;
    private static final Logger logger = LoggerFactory.getLogger(EloController.class);

    public EloController(EloService eloService) {
        this.eloService = eloService;
    }

    // Update Elo ratings
    @PutMapping("/update/{userId1}/{userId2}")
    public ResponseEntity<Object> updateElo(
            @PathVariable String userId1,
            @PathVariable String userId2,
            @RequestBody EloUpdateRequest request) {

        Firestore db = FirestoreClient.getFirestore();

        // Initialize variables to store the Elo ratings
        Double elo1;
        Double elo2;

        if (userId1 == null || userId1.isEmpty() || userId2 == null || userId2.isEmpty()) {
            return createErrorResponse("userId1 and userId2 are required.", HttpStatus.BAD_REQUEST);
        }

        try {            
            // Retrieve userId1's document from Firebase and get Elo1
            DocumentSnapshot user1Snapshot = db.collection("Users").document(userId1).get().get();
    
            if (!user1Snapshot.exists()) {
                return createErrorResponse("User 1 does not exist in Firebase.", HttpStatus.NOT_FOUND);
            }
            elo1 = user1Snapshot.getDouble("elo"); 
            if (elo1 == null) {
                return createErrorResponse("User 1's Elo rating is not found.", HttpStatus.BAD_REQUEST);
            }
    
            // Retrieve userId2's document from Firebase and get Elo2
            DocumentSnapshot user2Snapshot = db.collection("Users").document(userId2).get().get();
    
            if (!user2Snapshot.exists()) {
                return createErrorResponse("User 2 does not exist in Firebase.", HttpStatus.NOT_FOUND);
            }
            elo2 = user2Snapshot.getDouble("elo");
            if (elo2 == null) {
                return createErrorResponse("User 2's Elo rating is not found.", HttpStatus.BAD_REQUEST);
            }
    
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error retrieving users from Firebase: {}", e.getMessage(), e);
            return createErrorResponse("Error retrieving users from Firebase: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        logger.info("Received updateElo request: userId1={}, userId2={}, request={}", userId1, userId2, request);

        double AS1 = request.getAS1();
        double AS2 = request.getAS2();
        if (!(AS1 == 0 || AS1 == 0.5 || AS1 == 1) || !(AS2 == 0 || AS2 == 0.5 || AS2 == 1)) {
            return createErrorResponse("AS1 and AS2 must be 0, 0.5, or 1.", HttpStatus.BAD_REQUEST);
        }
        
        if (AS1 == AS2 && AS1 != 0.5) {
            return createErrorResponse("AS1 and AS2 must be different or 0.5 each.", HttpStatus.BAD_REQUEST);
        }

        if (elo1 < 0 || elo2 < 0) {
            return createErrorResponse("Elo1 and Elo2 must be non-negative.", HttpStatus.BAD_REQUEST);
        }

        try {
            eloService.updateElo(userId1, userId2, elo1, elo2, AS1, AS2);
            return new ResponseEntity<>("Elo ratings successfully updated", HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Error updating Elo ratings: {}", e.getMessage(), e);
            return createErrorResponse("Internal Server Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            // Catch any checked exceptions that might have been thrown
            logger.error("Unhandled Exception: {}", e.getMessage(), e);
            return createErrorResponse("Internal Server Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<Object> createErrorResponse(String message, HttpStatus status) {
        return new ResponseEntity<>(message, status);
    }
}
