package match.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import match.model.MatchModel;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class MatchService {

    // Initialize Firestore
    private final Firestore db = FirestoreClient.getFirestore();

    // Method to get Player Elo map
    public Map<String, Integer> getPlayerEloMap(String tournamentID) {
        Map<String, Integer> playerEloMap = new HashMap<>();

        try {
            // Get players reference
            CollectionReference playersRef = db.collection("Tournaments").document(tournamentID).collection("Players");
            ApiFuture<QuerySnapshot> future = playersRef.get();
            
            // Get the query snapshot
            QuerySnapshot playersSnapshot = future.get();
            
            // Iterate over each player document
            for (DocumentSnapshot playerDoc : playersSnapshot.getDocuments()) {
                String playerID = playerDoc.getId();
                
                // Fetch the user data for each player based on ID
                ApiFuture<DocumentSnapshot> userFuture = db.collection("Users").document(playerID).get();
                DocumentSnapshot userDoc = userFuture.get();

                // If the user exists, retrieve the Elo rating
                if (userDoc.exists()) {
                    Integer playerElo = userDoc.getLong("Elo").intValue();
                    playerEloMap.put(playerID, playerElo);
                } else {
                    System.out.println("User " + playerID + " does not exist in the Users collection.");
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        // Sort the players by Elo and return the sorted map
        return sortByElo(playerEloMap);
    }

    // Helper function to sort the Elo map in descending order
    private Map<String, Integer> sortByElo(Map<String, Integer> playerEloMap) {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(playerEloMap.entrySet());
        list.sort(Map.Entry.<String, Integer>comparingByValue().reversed());

        Map<String, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    // Generate matchups using a seeding system
    public List<MatchModel> generateSeededMatchups(String tournamentID) {
        Map<String, Integer> sortedEloMap = getPlayerEloMap(tournamentID);
        List<String> players = new ArrayList<>(sortedEloMap.keySet());

        List<MatchModel> matchups = new ArrayList<>();
        int i = 0;
        int j = players.size() - 1;

        while (i < j) {
            matchups.add(new MatchModel(players.get(i), players.get(j)));
            i++;
            j--;
        }
        return matchups;
    }

    // Generate round robin matchups where each player plays with everyone
    public List<MatchModel> generateRoundRobinMatchups(String tournamentID) {
        Map<String, Integer> sortedEloMap = getPlayerEloMap(tournamentID);
        List<String> players = new ArrayList<>(sortedEloMap.keySet());

        List<MatchModel> matchups = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            for (int j = i + 1; j < players.size(); j++) {
                matchups.add(new MatchModel(players.get(i), players.get(j)));
            }
        }
        return matchups;
    }

    // Save matchups to Firestore
    public void saveMatchups(String tournamentID, List<MatchModel> matchups) {
        CollectionReference matchesRef = db.collection("Tournaments").document(tournamentID).collection("Matches");

        for (MatchModel matchup : matchups) {
            Map<String, Object> matchupData = new HashMap<>();
            matchupData.put("playerID1", matchup.getPlayerID1());
            matchupData.put("playerID2", matchup.getPlayerID2());

            matchesRef.add(matchupData);
        }
    }
}
