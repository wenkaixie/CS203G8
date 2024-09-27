package elo.service;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class EloService {

    // Updates the Elo ratings for two users
    public void updateElo(String userId1, String userId2, double Elo1, double Elo2, double AS1, double AS2) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        
        try {
            // Check if both users exist before updating
            DocumentSnapshot user1Snapshot = db.collection("User").document(userId1).get().get();
            DocumentSnapshot user2Snapshot = db.collection("User").document(userId2).get().get();
            
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
            double newElo1 = Elo1 + K1 * (AS1 - ES1);
            double newElo2 = Elo2 + K2 * (AS2 - ES2);

            // Update Elo ratings in Firestore
            db.collection("User").document(userId1).update("elo", newElo1);
            db.collection("User").document(userId2).update("elo", newElo2);

        } catch (ExecutionException | InterruptedException e) {
            // Handle Firestore operation failures
            throw new Exception("Error while updating Elo: " + e.getMessage(), e);
        }
    }

    public Object getElo(String userID) {
        Firestore db = FirestoreClient.getFirestore();
    
        try {
            DocumentSnapshot userSnapshot = db.collection("User").document(userID).get().get();
    
            if (userSnapshot.exists()) {
                return userSnapshot.get("elo");
            } else {
                return "User does not exist.";  // Handle non-existent user case
            }
        } catch (ExecutionException | InterruptedException e) {
            // Handle Firestore operation failures
            System.err.println("Error retrieving Elo: " + e.getMessage());
            return null;  // Return null or appropriate fallback in case of error
        } catch (Exception e) {
            // General catch-all for other exceptions
            System.err.println("General error: " + e.getMessage());
            return null;
        }
    }
    
}
