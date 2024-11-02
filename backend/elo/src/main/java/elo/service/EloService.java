package elo.service;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class EloService {

    // Updates the Elo ratings for two users
    public void updateElo(String tournamentID, String userId1, String userId2, double Elo1, double Elo2, double AS1, double AS2) throws Exception {
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
            db.collection("Tournaments").document(tournamentID).collection("Users").document(userId1).update("elo", newElo1);
            db.collection("Tournaments").document(tournamentID).collection("Users").document(userId2).update("elo", newElo2);

        } catch (ExecutionException | InterruptedException e) {
            // Handle Firestore operation failures
            throw new Exception("Error while updating Elo: " + e.getMessage(), e);
        }
    }
    
}
