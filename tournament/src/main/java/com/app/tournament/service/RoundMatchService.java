package com.app.tournament.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.tournament.DTO.RoundMatchDTO;
import com.app.tournament.model.RoundMatch;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.cloud.firestore.Query.Direction;
import com.google.firebase.internal.NonNull;
import com.google.cloud.firestore.Query;


@Service
public class RoundMatchService {

    @Autowired
    private Firestore firestore;

    // Method to create a match within a round
    public String createRoundMatch(RoundMatchDTO matchDTO) throws Exception {
        try {
            // Create a new match document reference
            DocumentReference newMatchRef = firestore.collection("RoundMatches").document();

            // Populate the RoundMatch object
            RoundMatch match = new RoundMatch();
            match.setRmid(newMatchRef.getId());
            match.setRid(matchDTO.getRid());
            match.setUid1(matchDTO.getUid1());
            match.setUid2(matchDTO.getUid2());
            match.setUser1Score(matchDTO.getUser1Score());
            match.setUser2Score(matchDTO.getUser2Score());
            match.setMatchDate(matchDTO.getMatchDate());

            // Save the match document to Firestore
            ApiFuture<WriteResult> futureMatch = newMatchRef.set(match);
            WriteResult result = futureMatch.get(); // Block until the write completes

            System.out.println("Match created at: " + result.getUpdateTime()); // Log the creation time

            return match.getRmid(); // Return the newly created match ID

        } catch (InterruptedException | ExecutionException e) {
            throw new Exception("Error creating match: " + e.getMessage(), e);
        }
    }

    // Method to retrieve a match by its ID
    public RoundMatch getMatchById(String matchID) throws Exception {
        DocumentReference matchRef = firestore.collection("RoundMatches").document(matchID);
        ApiFuture<DocumentSnapshot> future = matchRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            return document.toObject(RoundMatch.class);
        } else {
            throw new Exception("Match not found with ID: " + matchID);
        }
    }

    // Method to get all matches in a specific round
    public List<RoundMatch> getMatchesByRoundId(String roundID) throws InterruptedException, ExecutionException {
        ApiFuture<QuerySnapshot> future = firestore.collection("RoundMatches")
                .whereEqualTo("roundId", roundID)
                .get();

        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<RoundMatch> matches = new ArrayList<>();
        for (DocumentSnapshot document : documents) {
            matches.add(document.toObject(RoundMatch.class));
        }
        return matches;
    }

    // Method to get latest match by user id
    public HashMap<String, Object> getLatestMatchByUserId(String userID) throws InterruptedException, ExecutionException {
        try {
            // Log the userID
            System.out.println("UserID: " + userID);
    
            // Fetch matches where the user is either uid1 or uid2
            ApiFuture<QuerySnapshot> future1 = firestore.collection("RoundMatches")
                .whereEqualTo("uid1", userID)
                .orderBy("matchDate", Query.Direction.DESCENDING)
                .limit(1)
                .get();
    
            ApiFuture<QuerySnapshot> future2 = firestore.collection("RoundMatches")
                .whereEqualTo("uid2", userID)
                .orderBy("matchDate", Query.Direction.DESCENDING)
                .limit(1)
                .get();
    
            // Fetch the documents from both queries
            List<QueryDocumentSnapshot> documents1 = future1.get().getDocuments();
            List<QueryDocumentSnapshot> documents2 = future2.get().getDocuments();
    
            // If no documents are found, return an empty RoundMatch
            if (documents1.isEmpty() && documents2.isEmpty()) {
                return new HashMap<>();
            }
    
            // Find the latest match based on matchDate
            RoundMatch latestMatch1 = documents1.isEmpty() ? null : documents1.get(0).toObject(RoundMatch.class);
            RoundMatch latestMatch2 = documents2.isEmpty() ? null : documents2.get(0).toObject(RoundMatch.class);

            RoundMatch latestMatch;
            if (latestMatch1 == null) {
                latestMatch = latestMatch2 != null ? latestMatch2 : new RoundMatch();
            } else if (latestMatch2 == null) {
                latestMatch = latestMatch1;
            } else {
                latestMatch = latestMatch1.getMatchDate().isAfter(latestMatch2.getMatchDate()) ? latestMatch1 : latestMatch2;
            }

            // Create a HashMap to store the match details
            HashMap<String, Object> matchDetails = new HashMap<>();

            // Add match details to the HashMap
            matchDetails.put("user1Score", latestMatch.getUser1Score());
            matchDetails.put("user2Score", latestMatch.getUser2Score());
            matchDetails.put("user1IsWhite", latestMatch.isUser1IsWhite());
            matchDetails.put("matchDate", latestMatch.getMatchDate());

            // Fetch the user IDs from the match (uid1 and uid2)
            String uid1 = latestMatch.getUid1();
            String uid2 = latestMatch.getUid2();

            // Query user 1 based on authId (which is the same as uid)
            ApiFuture<QuerySnapshot> userFuture1 = firestore.collection("Users")
                    .whereEqualTo("authId", uid1)
                    .limit(1)  // Ensuring that we get only one document
                    .get();

            // Query user 2 based on authId (which is the same as uid)
            ApiFuture<QuerySnapshot> userFuture2 = firestore.collection("Users")
                    .whereEqualTo("authId", uid2)
                    .limit(1)  // Ensuring that we get only one document
                    .get();

            // Fetch user 1 data
            QuerySnapshot userQuerySnapshot1 = userFuture1.get();
            if (!userQuerySnapshot1.getDocuments().isEmpty()) {
                DocumentSnapshot userDocument1 = userQuerySnapshot1.getDocuments().get(0);  // Get the first document
                String name1 = userDocument1.getString("name");
                String nationality1 = userDocument1.getString("nationality");
                matchDetails.put("uid1Name", name1);
                matchDetails.put("uid1Nationality", nationality1);
            }

            // Fetch user 2 data
            QuerySnapshot userQuerySnapshot2 = userFuture2.get();
            if (!userQuerySnapshot2.getDocuments().isEmpty()) {
                DocumentSnapshot userDocument2 = userQuerySnapshot2.getDocuments().get(0);  // Get the first document
                String name2 = userDocument2.getString("name");
                String nationality2 = userDocument2.getString("nationality");
                matchDetails.put("uid2Name", name2);
                matchDetails.put("uid2Nationality", nationality2);
            }

            // Fetch the round ID from the match
            String roundID = latestMatch.getRid();

            // Fetch the round document
            DocumentSnapshot roundDoc = firestore.collection("TournamentRounds").document(roundID).get().get();
            String tid = roundDoc.getString("tid");

            // Fetch the tournament document
            if (tid != null) {
                DocumentSnapshot tournamentDoc = firestore.collection("Tournaments").document(tid).get().get();
                matchDetails.put("tournamentId", tid);
                matchDetails.put("tournamentName", tournamentDoc.getString("name"));
            }

            return matchDetails;
            
        } catch (Exception e) {
            System.err.println("Error fetching matches: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
            return new HashMap<>(); // Return empty match in case of error
        }
    }    

    // Method to update match details
    public String updateMatch(String matchID, RoundMatchDTO updatedMatch)
            throws InterruptedException, ExecutionException {
        DocumentReference matchRef = firestore.collection("RoundMatches").document(matchID);

        // Update fields that were changed
        matchRef.update(
                "player1Score", updatedMatch.getUser1Score(),
                "player2Score", updatedMatch.getUser2Score()).get(); // Block until the write completes

        return "Match updated successfully.";
    }
    

    // Method to delete a match
    public void deleteMatch(String matchID) throws InterruptedException, ExecutionException {
        firestore.collection("RoundMatches").document(matchID).delete().get();
    }
}
