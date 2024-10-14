package com.app.tournament.service;

import java.util.ArrayList;
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

    public String updateMatchPointer(String matchId, String nextMatchId) throws Exception {
        DocumentReference matchRef = firestore.collection("RoundMatches").document(matchId);

        // Update the match with the next match pointer
        matchRef.update("nextMatchId", nextMatchId).get(); // Block until the update completes

        return "Match pointer updated successfully.";
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
