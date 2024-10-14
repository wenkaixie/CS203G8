package com.app.tournament.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.tournament.DTO.RoundMatchDTO;
import com.app.tournament.DTO.TournamentRoundDTO;
import com.app.tournament.model.TournamentRound;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;

@Service
public class TournamentRoundService {

    @Autowired
    private Firestore firestore;

    @Autowired
    private RoundMatchService roundMatchService; // Inject the RoundMatchService

    // Method to create rounds and matches in the correct format
    public List<Map<String, Object>> createAllRoundsAndMatches(String tournamentId, List<String> playerIds)
            throws Exception {
        int totalPlayers = playerIds.size();
        int totalRounds = calculateRounds(totalPlayers);
        List<String> previousRoundMatchIds = new ArrayList<>();
        List<Map<String, Object>> allMatches = new ArrayList<>();

        // Create all rounds and matches
        for (int roundNumber = 1; roundNumber <= totalRounds; roundNumber++) {
            TournamentRoundDTO roundDTO = new TournamentRoundDTO();
            roundDTO.setTid(tournamentId);
            roundDTO.setRoundNumber(roundNumber);

            // Create the round and get its ID
            String roundId = createTournamentRound(roundDTO);

            // Generate matches for the round
            List<String> currentRoundMatchIds;
            if (roundNumber == 1) {
                // Pre-seed Round 1 with player IDs
                currentRoundMatchIds = createRoundMatches(roundId, playerIds, roundNumber, allMatches);
            } else {
                // Create empty matches for later rounds
                int matchesToCreate = previousRoundMatchIds.size() / 2;
                currentRoundMatchIds = createEmptyMatches(roundId, matchesToCreate, roundNumber, allMatches);
                setMatchPointers(previousRoundMatchIds, currentRoundMatchIds);
            }

            previousRoundMatchIds = currentRoundMatchIds;
        }

        return allMatches;
    }
    
    // Create a tournament round and return its ID
    private String createTournamentRound(TournamentRoundDTO roundDTO) throws Exception {
        try {
            // Create a new round document reference with an auto-generated ID
            DocumentReference newRoundRef = firestore.collection("TournamentRounds").document();

            // Create and populate the TournamentRound object
            TournamentRound round = new TournamentRound();
            round.setTrid(newRoundRef.getId()); // Set the generated round ID
            round.setTid(roundDTO.getTid()); // Set the tournament ID
            round.setRoundNumber(roundDTO.getRoundNumber());
            round.setMids(new ArrayList<>()); // Initialize an empty list for match IDs

            // Save the round to Firestore and block until the write completes
            ApiFuture<WriteResult> futureRound = newRoundRef.set(round);
            WriteResult result = futureRound.get();

            System.out.println("Round created at: " + result.getUpdateTime());

            // Return the newly created round ID
            return round.getTrid();
        } catch (InterruptedException | ExecutionException e) {
            throw new Exception("Error creating tournament round: " + e.getMessage(), e);
        }
    }

    // Create matches for Round 1 and return their IDs
    private List<String> createRoundMatches(String roundId, List<String> playerIds, int roundNumber, List<Map<String, Object>> allMatches) throws Exception {
        List<String> matchIds = new ArrayList<>();

        for (int i = 0; i < playerIds.size(); i += 2) {
            String player1 = playerIds.get(i);
            String player2 = (i + 1 < playerIds.size()) ? playerIds.get(i + 1) : null;

            RoundMatchDTO matchDTO = new RoundMatchDTO();
            matchDTO.setRid(roundId);
            matchDTO.setUid1(player1);
            matchDTO.setUid2(player2);
            matchDTO.setUser1Score(0);
            matchDTO.setUser2Score(0);

            String matchId = roundMatchService.createRoundMatch(matchDTO);
            matchIds.add(matchId);

            // Add match to the list in the expected format
            allMatches.add(buildMatchObject(matchId, roundNumber, player1, player2, null));
        }

        return matchIds;
    }

    // Create empty matches for later rounds
    private List<String> createEmptyMatches(String roundId, int matchesToCreate, int roundNumber, List<Map<String, Object>> allMatches) throws Exception {
        List<String> matchIds = new ArrayList<>();

        for (int i = 0; i < matchesToCreate; i++) {
            RoundMatchDTO matchDTO = new RoundMatchDTO();
            matchDTO.setRid(roundId);
            matchDTO.setUid1(null); // Placeholder for player 1
            matchDTO.setUid2(null); // Placeholder for player 2
            matchDTO.setUser1Score(0);
            matchDTO.setUser2Score(0);

            String matchId = roundMatchService.createRoundMatch(matchDTO);
            matchIds.add(matchId);

            // Add empty match to the list in the expected format
            allMatches.add(buildMatchObject(matchId, roundNumber, null, null, null));
        }

        return matchIds;
    }

    // Set pointers between matches in different rounds
    private void setMatchPointers(List<String> previousRoundMatches, List<String> currentRoundMatches) throws Exception {
        int matchIndex = 0;

        for (int i = 0; i < previousRoundMatches.size(); i += 2) {
            String nextMatchId = currentRoundMatches.get(matchIndex);

            // Update match pointer
            roundMatchService.updateMatchPointer(previousRoundMatches.get(i), nextMatchId);
            roundMatchService.updateMatchPointer(previousRoundMatches.get(i + 1), nextMatchId);

            matchIndex++;
        }
    }

    // Build a match object in the expected format
    private Map<String, Object> buildMatchObject(String matchId, int roundNumber, String player1, String player2, String nextMatchId) {
        Map<String, Object> matchObject = new HashMap<>();
        matchObject.put("id", matchId);
        matchObject.put("name", "Round " + roundNumber + " - Match " + matchId);
        matchObject.put("nextMatchId", nextMatchId);
        matchObject.put("tournamentRoundText", String.valueOf(roundNumber));
        matchObject.put("startTime", "2021-06-01"); // Placeholder date
        matchObject.put("state", "PENDING");

        List<Map<String, Object>> participants = new ArrayList<>();
        if (player1 != null) {
            participants.add(buildParticipantObject(player1, false));
        }
        if (player2 != null) {
            participants.add(buildParticipantObject(player2, false));
        }

        matchObject.put("participants", participants);
        return matchObject;
    }

    // Build a participant object
    private Map<String, Object> buildParticipantObject(String playerId, boolean isWinner) {
        Map<String, Object> participant = new HashMap<>();
        participant.put("id", playerId);
        participant.put("name", "Player " + playerId); // Placeholder name
        participant.put("resultText", "0");
        participant.put("isWinner", isWinner);
        return participant;
    }

    // Calculate the total number of rounds needed
    private int calculateRounds(int n) {
        int rounds = 0;
        while (n > 1) {
            n /= 2;
            rounds++;
        }
        return rounds;
    }


    // Method to retrieve a round by ID
    public TournamentRound getTournamentRoundById(String roundID) throws Exception {
        DocumentReference roundRef = firestore.collection("TournamentRounds").document(roundID);
        ApiFuture<DocumentSnapshot> future = roundRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            return document.toObject(TournamentRound.class);
        } else {
            throw new Exception("Round not found with ID: " + roundID);
        }
    }

    // Method to get all rounds of a specific tournament
    public List<TournamentRound> getRoundsByTournamentId(String tournamentID)
            throws InterruptedException, ExecutionException {
        ApiFuture<QuerySnapshot> future = firestore.collection("TournamentRounds")
                .whereEqualTo("tournamentId", tournamentID)
                .get();

        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<TournamentRound> rounds = new ArrayList<>();
        for (DocumentSnapshot document : documents) {
            rounds.add(document.toObject(TournamentRound.class));
        }
        return rounds;
    }

    // Method to update a specific round
    public String updateTournamentRound(String roundID, TournamentRoundDTO updatedRound)
            throws InterruptedException, ExecutionException {
        DocumentReference roundRef = firestore.collection("TournamentRounds").document(roundID);

        // Update fields that were changed
        roundRef.update(
                "roundNumber", updatedRound.getRoundNumber(),
                "matchIds", updatedRound.getMids()).get(); // Block until the write completes

        return "Round updated successfully.";
    }

    // Method to delete a specific round
    public void deleteTournamentRound(String roundID) throws InterruptedException, ExecutionException {
        firestore.collection("TournamentRounds").document(roundID).delete().get();
    }
}
