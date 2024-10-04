package com.app.tournament.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    // Method to create a round in a tournament
    public String createTournamentRound(TournamentRoundDTO roundDTO) throws Exception {
        try {
            // Create a new round document reference with an auto-generated ID
            DocumentReference newRoundRef = firestore.collection("TournamentRounds").document();

            // Create and populate the TournamentRound object
            TournamentRound round = new TournamentRound();
            round.setTrid(newRoundRef.getId()); // Set generated round ID
            round.setTid(roundDTO.getTid());
            round.setRoundNumber(roundDTO.getRoundNumber());
            round.setMids(new ArrayList<>()); // Initialize empty match list

            // Save the round document in Firestore
            ApiFuture<WriteResult> futureRound = newRoundRef.set(round);
            WriteResult result = futureRound.get(); // Block until the write completes

            System.out.println("Round created at: " + result.getUpdateTime()); // Log the creation time

            return round.getTrid(); // Return the newly created round ID

        } catch (InterruptedException | ExecutionException e) {
            throw new Exception("Error creating tournament round: " + e.getMessage(), e);
        }
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
                "mids", updatedRound.getMids()).get(); // Block until the write completes

        return "Round updated successfully.";
    }

    // Method to delete a specific round
    public void deleteTournamentRound(String roundID) throws InterruptedException, ExecutionException {
        firestore.collection("TournamentRounds").document(roundID).delete().get();
    }
}
