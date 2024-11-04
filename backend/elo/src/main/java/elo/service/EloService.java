package elo.service;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.List;

@Service
public class EloService {

    private static final Logger logger = LoggerFactory.getLogger(EloService.class);

    public ResponseEntity<Object> retrieveResults(String tournamentId, List<String> userIds, List<Double> results) {
        Firestore db = FirestoreClient.getFirestore();
        if (tournamentId == null || tournamentId.isEmpty()) {
            return createErrorResponse("tournamentId required.", HttpStatus.BAD_REQUEST);
        }
        DocumentSnapshot tournamentSnapshot;
        try {      
            // Retrieve tournament doc
            tournamentSnapshot = db.collection("Tournaments").document(tournamentId).get().get();

            if (!tournamentSnapshot.exists()) {
                return createErrorResponse("Tournament does not exist in Firebase.", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("Error retrieving tournament: {}", e.getMessage());
            return createErrorResponse("Internal Server Error: Unable to retrieve tournament.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    
        for (int i = 0; i < userIds.size(); i += 2) {
            String userId1 = userIds.get(i);
            String userId2 = userIds.get(i + 1);
            Double AS1 = results.get(i);
            Double AS2 = results.get(i + 1);
    
            try {
                // Fetch current Elo ratings for userId1 and userId2 from Firestore
                DocumentSnapshot user1Snapshot = db.collection("Users").document(userId1).get().get();
                DocumentSnapshot user2Snapshot = db.collection("Users").document(userId2).get().get();
    

                if (!user1Snapshot.exists() || !user2Snapshot.exists()) {
                    logger.warn("One or both users do not exist: userId1={}, userId2={}", userId1, userId2);
                    continue;
                }
    
                Double elo1 = user1Snapshot.getDouble("elo");
                Double elo2 = user2Snapshot.getDouble("elo");
    
                if (elo1 == null || elo2 == null) {
                    logger.warn("One or both users have null Elo: userId1={}, userId2={}", userId1, userId2);
                    continue;
                }
    
                if (!(AS1 == 0 || AS1 == 0.5 || AS1 == 1) || !(AS2 == 0 || AS2 == 0.5 || AS2 == 1)) {
                    logger.warn("Invalid AS values: AS1={}, AS2={}", AS1, AS2);
                    continue;
                }
    
                if (AS1 == AS2 && AS1 != 0.5) {
                    logger.warn("AS values conflict: AS1={}, AS2={}", AS1, AS2);
                    continue;
                }
    
                try {
                    updateElo(tournamentId, userId1, userId2, elo1, elo2, AS1, AS2);
                    continue;
                } catch (RuntimeException e) {
                    logger.error("Error updating Elo ratings: {}", e.getMessage(), e);
                    return createErrorResponse("Internal Server Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                } catch (Exception e) {
                    // Catch any checked exceptions that might have been thrown
                    logger.error("Unhandled Exception: {}", e.getMessage(), e);
                    return createErrorResponse("Internal Server Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                }
    
            } catch (Exception e) {
                System.out.println("Error updating Elo for users " + userId1 + " and " + userId2 + ": " + e.getMessage());
            }
        }
        return new ResponseEntity<>("Elo ratings processed for provided pairs", HttpStatus.OK);
    }

    private ResponseEntity<Object> createErrorResponse(String message, HttpStatus status) {
        return new ResponseEntity<>(message, status);
    }

    // Updates the Elo ratings for two users
    public void updateElo(String tournamentId, String userId1, String userId2, double Elo1, double Elo2, double AS1, double AS2) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        
        try {
            // Check if both users exist before updating
            DocumentSnapshot user1Snapshot = db.collection("Users").document(userId1).get().get();
            DocumentSnapshot user2Snapshot = db.collection("Users").document(userId2).get().get();
            
            if (!user1Snapshot.exists() || !user2Snapshot.exists()) {
                throw new Exception("One or both users do not exist.");
            }

            // Calculate expected scores
            double ES1 = 1 / (1 + Math.pow(10, ((Elo2 - Elo1) / 400)));
            double ES2 = 1 / (1 + Math.pow(10, ((Elo1 - Elo2) / 400)));

            // Determine K-factor based on Elo ratings
            int K1 = Elo1 < 2100 ? 32 : (Elo1 > 2400 ? 16 : 24);
            int K2 = Elo2 < 2100 ? 32 : (Elo2 > 2400 ? 16 : 24);

            // Calculate new Elo ratings
            double newElo1 = Math.round(Elo1 + K1 * (AS1 - ES1));
            double newElo2 = Math.round(Elo2 + K2 * (AS2 - ES2));
            if (newElo1 < 0) {newElo1 = 0;};
            if (newElo2 < 0) {newElo2 = 0;};

            // Update Elo ratings in Firestore
            db.collection("Users").document(userId1).update("elo", newElo1);
            db.collection("Users").document(userId2).update("elo", newElo2);
            db.collection("Tournaments").document(tournamentId).collection("Users").document(userId1).update("elo", newElo1);
            db.collection("Tournaments").document(tournamentId).collection("Users").document(userId2).update("elo", newElo2);
        } catch (ExecutionException | InterruptedException e) {
            // Handle Firestore operation failures
            throw new Exception("Error while updating Elo: " + e.getMessage(), e);
        }
    }
    
}
