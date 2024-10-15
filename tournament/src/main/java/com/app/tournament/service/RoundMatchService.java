package com.app.tournament.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.tournament.DTO.ParticipantDTO;
import com.app.tournament.DTO.RoundMatchDTO;
import com.app.tournament.model.RoundMatch;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query.Direction;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;

@Service
public class RoundMatchService {

    @Autowired
    private Firestore firestore;

    // Create a new match within a round
    public String createRoundMatch(RoundMatchDTO matchDTO) throws Exception {
        try {
            DocumentReference newMatchRef = firestore.collection("RoundMatches").document();

            // Populate the RoundMatch object
            RoundMatch match = new RoundMatch(
                    newMatchRef.getId(),
                    matchDTO.getRmid(),
                    matchDTO.getParticipants(),
                    matchDTO.getUser1Score(),
                    matchDTO.getUser2Score(),
                    matchDTO.getMatchDate(),
                    matchDTO.isUser1IsWhite(),
                    matchDTO.getNextMatchId(),
                    matchDTO.getState());

            // Save the match to Firestore
            WriteResult result = newMatchRef.set(match).get();
            System.out.println("Match created at: " + result.getUpdateTime());

            return match.getRmid();

        } catch (InterruptedException | ExecutionException e) {
            throw new Exception("Error creating match: " + e.getMessage(), e);
        }
    }

    // Update the pointer to the next match
    public String updateMatchPointer(String matchId, String nextMatchId) throws Exception {
        try {
            DocumentReference matchRef = firestore.collection("RoundMatches").document(matchId);
            matchRef.update("nextMatchId", nextMatchId).get();
            return "Match pointer updated successfully.";
        } catch (InterruptedException | ExecutionException e) {
            throw new Exception("Error updating match pointer: " + e.getMessage(), e);
        }
    }

    // Retrieve a match by ID
    public RoundMatch getMatchById(String matchID) throws Exception {
        DocumentReference matchRef = firestore.collection("RoundMatches").document(matchID);
        DocumentSnapshot document = matchRef.get().get();

        if (document.exists()) {
            return document.toObject(RoundMatch.class);
        } else {
            throw new Exception("Match not found with ID: " + matchID);
        }
    }

    // Get all matches in a specific round
    public List<RoundMatch> getMatchesByRoundId(String roundID) throws InterruptedException, ExecutionException {
        ApiFuture<QuerySnapshot> future = firestore.collection("RoundMatches")
                .whereEqualTo("rid", roundID)
                .get();

        List<RoundMatch> matches = new ArrayList<>();
        for (DocumentSnapshot document : future.get().getDocuments()) {
            matches.add(document.toObject(RoundMatch.class));
        }
        return matches;
    }

    // Get the latest match played by a user
    public HashMap<String, Object> getLatestMatchByUserId(String userID)
            throws InterruptedException, ExecutionException {
        System.out.println("Fetching latest match for UserID: " + userID);

        List<RoundMatch> matches = queryMatchesByUser(userID);
        if (matches.isEmpty()) {
            return new HashMap<>();
        }

        RoundMatch latestMatch = matches.get(0);
        HashMap<String, Object> matchDetails = new HashMap<>();

        matchDetails.put("user1Score", latestMatch.getUser1Score());
        matchDetails.put("user2Score", latestMatch.getUser2Score());
        matchDetails.put("user1IsWhite", latestMatch.isUser1IsWhite());
        matchDetails.put("matchDate", latestMatch.getMatchDate());

        populateParticipantDetails(latestMatch.getParticipants(), matchDetails);
        return matchDetails;
    }

    // Query matches where the user is a participant
    private List<RoundMatch> queryMatchesByUser(String userID) throws InterruptedException, ExecutionException {
        ApiFuture<QuerySnapshot> future = firestore.collection("RoundMatches")
                .whereArrayContains("participants.id", userID)
                .orderBy("matchDate", Direction.DESCENDING)
                .limit(1)
                .get();

        List<RoundMatch> matches = new ArrayList<>();
        for (DocumentSnapshot document : future.get().getDocuments()) {
            matches.add(document.toObject(RoundMatch.class));
        }
        return matches;
    }

    // Populate participant details into the match details map
    private void populateParticipantDetails(List<ParticipantDTO> participants, HashMap<String, Object> matchDetails) {
        if (participants.size() >= 1) {
            ParticipantDTO participant1 = participants.get(0);
            matchDetails.put("uid1", participant1.getId());
            matchDetails.put("uid1Name", participant1.getName());
            matchDetails.put("uid1Elo", participant1.getElo());
            matchDetails.put("uid1ResultText", participant1.getResultText());
        }

        if (participants.size() >= 2) {
            ParticipantDTO participant2 = participants.get(1);
            matchDetails.put("uid2", participant2.getId());
            matchDetails.put("uid2Name", participant2.getName());
            matchDetails.put("uid2Elo", participant2.getElo());
            matchDetails.put("uid2ResultText", participant2.getResultText());
        }
    }

    // Update match details
    public String updateMatch(String matchID, RoundMatchDTO updatedMatch)
            throws InterruptedException, ExecutionException {
        DocumentReference matchRef = firestore.collection("RoundMatches").document(matchID);

        matchRef.update(
                "user1Score", updatedMatch.getUser1Score(),
                "user2Score", updatedMatch.getUser2Score(),
                "state", updatedMatch.getState()).get();

        return "Match updated successfully.";
    }

    // Delete a match
    public void deleteMatch(String matchID) throws InterruptedException, ExecutionException {
        firestore.collection("RoundMatches").document(matchID).delete().get();
    }
}
