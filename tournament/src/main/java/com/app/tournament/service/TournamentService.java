package com.app.tournament.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.tournament.DTO.TournamentDTO;
import com.app.tournament.DTO.TournamentRoundDTO;
import com.app.tournament.model.Tournament;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;


@Service
public class TournamentService {

    @Autowired
    private Firestore firestore;

    @Autowired
    private TournamentRoundService roundService; // Inject the TournamentRoundService

    public String createTournament(TournamentDTO tournamentDTO) throws Exception {
        try {
            // Create a reference to a new tournament document
            DocumentReference newTournamentRef = firestore.collection("Tournaments").document();

            // Populate the Tournament object
            Tournament tournament = new Tournament();
            tournament.setTid(newTournamentRef.getId());
            tournament.setName(tournamentDTO.getName());
            tournament.setDescription(tournamentDTO.getDescription());
            tournament.setEloRequirement(tournamentDTO.getEloRequirement());
            tournament.setLocation(tournamentDTO.getLocation());
            tournament.setStartDatetime(tournamentDTO.getStartDatetime());
            tournament.setEndDatetime(tournamentDTO.getEndDatetime());
            tournament.setCapacity(tournamentDTO.getCapacity());
            tournament.setCreatedTimestamp(Instant.now());

            // Save the tournament to Firestore
            ApiFuture<WriteResult> futureTournament = newTournamentRef.set(tournament);
            WriteResult result = futureTournament.get();

            System.out.println("Tournament created at: " + result.getUpdateTime());

            // Automatically calculate the number of rounds and create them
            int numberOfRounds = calculateRounds(tournament.getCapacity());
            createTournamentRounds(tournament.getTid(), numberOfRounds);

            return tournament.getTid();

        } catch (InterruptedException | ExecutionException e) {
            throw new Exception("Error creating the tournament: " + e.getMessage(), e);
        }
    }

    // Method to calculate the number of rounds needed
    private int calculateRounds(int n) {
        int rounds = 0;
        while (n > 1) {
            n /= 2;
            rounds++;
        }
        return rounds;
    }

    // Method to create the rounds for a tournament
    private void createTournamentRounds(String tournamentId, int numberOfRounds) throws Exception {
        for (int i = 1; i <= numberOfRounds; i++) {
            TournamentRoundDTO roundDTO = new TournamentRoundDTO();
            roundDTO.setTid(tournamentId);
            roundDTO.setRoundNumber(i);
            roundDTO.setMids(new ArrayList<>()); // Initialize empty match list

            // Use the TournamentRoundService to create each round
            roundService.createTournamentRound(roundDTO);
        }
    }

    public Tournament getTournamentById(String tournamentID) throws Exception {
        DocumentReference tournamentRef = firestore.collection("Tournaments").document(tournamentID);
        ApiFuture<DocumentSnapshot> future = tournamentRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            return document.toObject(Tournament.class);
        } else {
            throw new Exception("Tournament not found with ID: " + tournamentID);
        }
    }

    public List<Tournament> getAllTournaments() throws InterruptedException, ExecutionException {
        ApiFuture<QuerySnapshot> future = firestore.collection("Tournaments").get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        List<Tournament> tournaments = new ArrayList<>();
        for (DocumentSnapshot document : documents) {
            tournaments.add(document.toObject(Tournament.class));
        }
        return tournaments;
    }

    public String updateTournament(String tournamentID, TournamentDTO updatedTournament)
            throws InterruptedException, ExecutionException {
        DocumentReference tournamentRef = firestore.collection("Tournaments").document(tournamentID);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", updatedTournament.getName());
        updates.put("description", updatedTournament.getDescription());
        updates.put("eloRequirements", updatedTournament.getEloRequirement());
        updates.put("location", updatedTournament.getLocation());
        updates.put("startDatetime", updatedTournament.getStartDatetime());
        updates.put("endDatetime", updatedTournament.getEndDatetime());

        tournamentRef.update(updates).get();

        return "Tournament updated successfully.";
    }

    // Method to delete a tournament
    public void deleteTournament(String tournamentID) throws InterruptedException, ExecutionException {
        // Delete the tournament document from Firestore
        firestore.collection("Tournaments").document(tournamentID).delete().get();
    }
    
    public List<Tournament> getTournamentsByLocation(String location) throws InterruptedException, ExecutionException {
        ApiFuture<QuerySnapshot> future = firestore.collection("Tournaments").whereEqualTo("location", location).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        List<Tournament> tournaments = new ArrayList<>();
        for (DocumentSnapshot document : documents) {
            tournaments.add(document.toObject(Tournament.class));
        }
        return tournaments;
    }
    
    public List<Tournament> getTournamentsWithPagination(int limit, String lastTournamentID) throws InterruptedException, ExecutionException {
        Query query = firestore.collection("Tournaments").limit(limit);
        
        if (lastTournamentID != null) {
            DocumentSnapshot lastTournament = firestore.collection("Tournaments").document(lastTournamentID).get().get();
            query = query.startAfter(lastTournament);
        }
        
        ApiFuture<QuerySnapshot> future = query.get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        
        List<Tournament> tournaments = new ArrayList<>();
        for (DocumentSnapshot document : documents) {
            tournaments.add(document.toObject(Tournament.class));
        }
        return tournaments;
    }
   
    // Method to add a player to a tournament
    public String addUserToTournament(String tournamentID, String userID)
            throws InterruptedException, ExecutionException {
        // Reference the tournament document in Firestore
        DocumentReference tournamentRef = firestore.collection("Tournaments").document(tournamentID);

        // Add the userID directly to the players array field in the tournament document
        tournamentRef.update("users", FieldValue.arrayUnion(userID)).get();

        return "Player added successfully to the tournament.";
    }
    
    public String removeUserFromTournament(String tournamentID, String userID)
        throws InterruptedException, ExecutionException {
        DocumentReference tournamentRef = firestore.collection("Tournaments").document(tournamentID);
        tournamentRef.update("users", FieldValue.arrayRemove(userID)).get();
        return "Player removed successfully from the tournament.";
    }
}
