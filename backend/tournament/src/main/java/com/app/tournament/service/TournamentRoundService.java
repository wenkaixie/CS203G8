package com.app.tournament.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.tournament.DTO.ParticipantDTO;
import com.app.tournament.DTO.RoundMatchDTO;
import com.app.tournament.DTO.TournamentRoundDTO;
import com.app.tournament.model.TournamentRound;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;

@Service
public class TournamentRoundService {

    @Autowired
    private Firestore firestore;

    @Autowired
    private RoundMatchService roundMatchService;

    //Create all rounds and matches for the tournament
    public List<Map<String, Object>> createAllRoundsAndMatches(String tournamentId, List<String> playerIds)
            throws Exception {
        int totalPlayers = playerIds.size();
        int totalRounds = calculateRounds(totalPlayers);
        List<String> previousRoundMatchIds = new ArrayList<>();
        List<Map<String, Object>> allMatches = new ArrayList<>();

        for (int roundNumber = 1; roundNumber <= totalRounds; roundNumber++) {
            // Create a new round and get its ID
            String roundId = createTournamentRound(
                    new TournamentRoundDTO(tournamentId, roundNumber, new ArrayList<>()));

            // Generate matches for the current round
            List<String> currentRoundMatchIds = (roundNumber == 1)
                    ? createRoundMatches(roundId, playerIds, roundNumber, allMatches)
                    : createEmptyMatches(roundId, previousRoundMatchIds.size() / 2, roundNumber, allMatches);

            // Set pointers between previous and current round matches
            if (roundNumber > 1) {
                setMatchPointers(previousRoundMatchIds, currentRoundMatchIds);
            }

            previousRoundMatchIds = currentRoundMatchIds;
        }

        return allMatches;
    }

    // Create a new tournament round
    // public String createTournamentRound(TournamentRoundDTO roundDTO) throws Exception {
    //     DocumentReference newRoundRef = firestore.collection("TournamentRounds").document();
    //     TournamentRound round = new TournamentRound(
    //             newRoundRef.getId(),
    //             roundDTO.getTid(),
    //             roundDTO.getRoundNumber(),
    //             new ArrayList<>());

    //     newRoundRef.set(round).get(); // Block until the write completes
    //     return round.getTrid();
    // }

    public List<String> createRoundMatches(String roundId, List<String> playerIds, int roundNumber,
                                       List<Map<String, Object>> allMatches) throws Exception {
    List<String> matchIds = new ArrayList<>();

    for (int i = 0; i < playerIds.size(); i += 2) {
        String player1 = playerIds.get(i);
        String player2 = (i + 1 < playerIds.size()) ? playerIds.get(i + 1) : null;

        // Create participant objects
        List<ParticipantDTO> participants = new ArrayList<>();
        participants.add(new ParticipantDTO(player1, "Player " + player1, "", false, 0));
        if (player2 != null) {
            participants.add(new ParticipantDTO(player2, "Player " + player2, "", false, 0));
        }

        // Use the correct constructor with all parameters
        RoundMatchDTO matchDTO = new RoundMatchDTO(
                null, participants, 0, 0, Timestamp.now(), true, null, "PENDING");

        String matchId = roundMatchService.createRoundMatch(matchDTO);
        matchIds.add(matchId);

        allMatches.add(buildMatchObject(matchId, roundNumber, player1, player2, null));
    }
    return matchIds;
}


    public List<String> createEmptyMatches(String roundId, int matchesToCreate, int roundNumber,
        List<Map<String, Object>> allMatches) throws Exception {
    List<String> matchIds = new ArrayList<>();

    for (int i = 0; i < matchesToCreate; i++) {
        // Create an empty list of participants
        List<ParticipantDTO> participants = new ArrayList<>();

        // Use the correct constructor with all parameters
        RoundMatchDTO matchDTO = new RoundMatchDTO(
                null, participants, 0, 0, Timestamp.now(), true, null, "PENDING");

        String matchId = roundMatchService.createRoundMatch(matchDTO);
        matchIds.add(matchId);

        allMatches.add(buildMatchObject(matchId, roundNumber, null, null, null));
    }
    return matchIds;
}

    // Set match pointers to link rounds
    public void setMatchPointers(List<String> previousRoundMatches, List<String> currentRoundMatches) throws Exception {
        int matchIndex = 0;

        for (int i = 0; i < previousRoundMatches.size(); i += 2) {
            String nextMatchId = currentRoundMatches.get(matchIndex++);
            roundMatchService.updateMatchPointer(previousRoundMatches.get(i), nextMatchId);
            roundMatchService.updateMatchPointer(previousRoundMatches.get(i + 1), nextMatchId);
        }
    }

    // Build a match object in the expected format
    public Map<String, Object> buildMatchObject(String matchId, int roundNumber, String player1, String player2,
            String nextMatchId) {
        Map<String, Object> matchObject = new HashMap<>();
        matchObject.put("id", matchId);
        matchObject.put("name", "Round " + roundNumber + " - Match " + matchId);
        matchObject.put("nextMatchId", nextMatchId);
        matchObject.put("tournamentRoundText", String.valueOf(roundNumber));
        matchObject.put("startTime", "2021-06-01"); // Placeholder date
        matchObject.put("state", "PENDING");

        List<Map<String, Object>> participants = new ArrayList<>();
        if (player1 != null)
            participants.add(buildParticipantObject(player1, false));
        if (player2 != null)
            participants.add(buildParticipantObject(player2, false));
        matchObject.put("participants", participants);

        return matchObject;
    }

    // Build a participant object
    public Map<String, Object> buildParticipantObject(String playerId, boolean isWinner) {
        Map<String, Object> participant = new HashMap<>();
        participant.put("id", playerId);
        participant.put("name", "Player " + playerId); // Placeholder name
        participant.put("resultText", "0");
        participant.put("isWinner", isWinner);
        return participant;
    }

    // Calculate the number of rounds needed
    public int calculateRounds(int n) {
        return (int) Math.ceil(Math.log(n) / Math.log(2));
    }

    // Retrieve a tournament round by ID
    // public TournamentRound getTournamentRoundById(String roundID) throws Exception {
    //     DocumentSnapshot document = firestore.collection("TournamentRounds").document(roundID).get().get();
    //     if (!document.exists())
    //         throw new Exception("Round not found with ID: " + roundID);
    //     return document.toObject(TournamentRound.class);
    // }

    // Get all rounds of a specific tournament
    // public List<TournamentRound> getRoundsByTournamentId(String tournamentID)
    //         throws InterruptedException, ExecutionException {
    //     QuerySnapshot snapshot = firestore.collection("TournamentRounds").whereEqualTo("tid", tournamentID).get().get();
    //     List<TournamentRound> rounds = new ArrayList<>();
    //     for (DocumentSnapshot document : snapshot.getDocuments()) {
    //         rounds.add(document.toObject(TournamentRound.class));
    //     }
    //     return rounds;
    // }

    // Update a tournament round
    // public String updateTournamentRound(String roundID, TournamentRoundDTO updatedRound)
    //         throws InterruptedException, ExecutionException {
    //     firestore.collection("TournamentRounds").document(roundID).update(
    //             "roundNumber", updatedRound.getRoundNumber(),
    //             "matchIds", updatedRound.getMids()).get();
    //     return "Round updated successfully.";
    // }

    // Delete a tournament round
    // public void deleteTournamentRound(String roundID) throws InterruptedException, ExecutionException {
    //     firestore.collection("TournamentRounds").document(roundID).delete().get();
    // }
}
