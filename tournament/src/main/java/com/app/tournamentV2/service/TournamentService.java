package com.app.tournamentV2.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.tournamentV2.DTO.TournamentDTO;
import com.app.tournamentV2.model.Tournament;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;

@Service
public class TournamentService {

    @Autowired
    private Firestore firestore;

    // Create a new tournament and ensure tid matches the document ID
    public String createTournament(TournamentDTO tournamentDTO) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection("Tournaments").document(); // Generate doc ref with new ID
        String generatedId = docRef.getId(); // Use this ID as the tid

        Tournament tournament = convertToEntity(tournamentDTO, generatedId); // Convert DTO with the generated ID
        ApiFuture<WriteResult> future = docRef.set(tournament); // Save tournament with matching tid
        future.get(); // Wait for the write to complete

        return generatedId; // Return the generated tournament ID
    }

    // Retrieve a tournament by ID
    public Tournament getTournamentById(String tournamentID) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = firestore.collection("Tournaments").document(tournamentID).get().get();
        if (!document.exists()) {
            throw new RuntimeException("Tournament not found with ID: " + tournamentID);
        }
        return document.toObject(Tournament.class);
    }

    // Retrieve all tournaments
    public List<Tournament> getAllTournaments() throws ExecutionException, InterruptedException {
        QuerySnapshot snapshot = firestore.collection("Tournaments").get().get();
        return snapshot.getDocuments().stream()
                .map(doc -> doc.toObject(Tournament.class))
                .collect(Collectors.toList());
    }

    // Update an existing tournament
    public String updateTournament(String tournamentID, TournamentDTO updatedTournamentDTO)
            throws ExecutionException, InterruptedException {
        Tournament updatedTournament = convertToEntity(updatedTournamentDTO, tournamentID); // Ensure ID is preserved
        ApiFuture<WriteResult> future = firestore.collection("Tournaments").document(tournamentID)
                .set(updatedTournament);
        return "Tournament updated at: " + future.get().getUpdateTime().toString();
    }

    // Delete a tournament
    public void deleteTournament(String tournamentID) throws ExecutionException, InterruptedException {
        firestore.collection("Tournaments").document(tournamentID).delete().get();
    }

    // Add a user to a tournament
    public String addUserToTournament(String tournamentID, String userID)
            throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection("Tournaments").document(tournamentID);
        DocumentSnapshot document = docRef.get().get();

        if (document.exists()) {
            Tournament tournament = document.toObject(Tournament.class);
            if (!tournament.getUsers().contains(userID)) {
                tournament.getUsers().add(userID);
                docRef.set(tournament).get(); // Save changes
                return "User added successfully.";
            } else {
                return "User is already part of the tournament.";
            }
        } else {
            throw new RuntimeException("Tournament not found with ID: " + tournamentID);
        }
    }

    // Remove a user from a tournament
    public String removeUserFromTournament(String tournamentID, String userID)
            throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection("Tournaments").document(tournamentID);
        DocumentSnapshot document = docRef.get().get();

        if (document.exists()) {
            Tournament tournament = document.toObject(Tournament.class);
            if (tournament.getUsers().remove(userID)) {
                docRef.set(tournament).get(); // Save changes
                return "User removed successfully.";
            } else {
                return "User not found in the tournament.";
            }
        } else {
            throw new RuntimeException("Tournament not found with ID: " + tournamentID);
        }
    }

    // Get eligible tournaments for a user based on userID
    public List<Tournament> getEligibleTournamentsOfUser(String userID)
            throws ExecutionException, InterruptedException {
        QuerySnapshot snapshot = firestore.collection("Tournaments").get().get();
        return snapshot.getDocuments().stream()
                .map(doc -> doc.toObject(Tournament.class))
                .filter(tournament -> !tournament.getUsers().contains(userID)) // Eligibility condition
                .collect(Collectors.toList());
    }

    // Convert TournamentDTO to Tournament entity with the provided ID
    private Tournament convertToEntity(TournamentDTO dto, String tid) {
        return new Tournament(
                dto.getAgeLimit(),
                dto.getName(),
                dto.getDescription(),
                dto.getEloRequirement(),
                dto.getLocation(),
                dto.getCapacity(),
                dto.getStartDatetime(),
                dto.getEndDatetime(),
                tid, // Set the provided tournament ID
                Timestamp.now(), // Set created timestamp
                dto.getPrize(),
                "OPEN", // Default status
                dto.getUsers(),
                new ArrayList<>() // Initialize empty rounds
        );
    }
}
