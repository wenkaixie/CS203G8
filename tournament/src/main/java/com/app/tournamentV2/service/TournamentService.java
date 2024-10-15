package com.app.tournamentV2.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.tournamentV2.DTO.TournamentDTO;
import com.app.tournamentV2.model.Match;
import com.app.tournamentV2.model.Round;
import com.app.tournamentV2.model.Tournament;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TournamentService {

    @Autowired
    private Firestore firestore;

    // Create a new tournament and ensure tid matches the document ID
    public String createTournament(TournamentDTO tournamentDTO) throws ExecutionException, InterruptedException {
        log.info("Creating new tournament...");
        DocumentReference docRef = firestore.collection("Tournaments").document();
        String generatedId = docRef.getId();
        log.info("Generated tournament ID: {}", generatedId);

        Tournament tournament = convertToEntity(tournamentDTO, generatedId);
        ApiFuture<WriteResult> future = docRef.set(tournament);
        future.get();
        log.info("Tournament {} created successfully.", generatedId);

        return generatedId;
    }

    // Retrieve a tournament by ID
    public Tournament getTournamentById(String tournamentID) throws ExecutionException, InterruptedException {
        log.info("Fetching tournament with ID: {}", tournamentID);
        DocumentSnapshot document = firestore.collection("Tournaments").document(tournamentID).get().get();

        if (!document.exists()) {
            log.error("Tournament not found with ID: {}", tournamentID);
            throw new RuntimeException("Tournament not found with ID: " + tournamentID);
        }

        log.info("Tournament {} retrieved successfully.", tournamentID);
        return document.toObject(Tournament.class);
    }

    // Retrieve all tournaments
    public List<Tournament> getAllTournaments() throws ExecutionException, InterruptedException {
        log.info("Fetching all tournaments...");
        QuerySnapshot snapshot = firestore.collection("Tournaments").get().get();
        List<Tournament> tournaments = snapshot.getDocuments().stream()
                .map(doc -> doc.toObject(Tournament.class))
                .collect(Collectors.toList());

        log.info("Retrieved {} tournaments.", tournaments.size());
        return tournaments;
    }

    // Update an existing tournament
    public String updateTournament(String tournamentID, TournamentDTO updatedTournamentDTO)
            throws ExecutionException, InterruptedException {
        log.info("Updating tournament with ID: {}", tournamentID);
        Tournament updatedTournament = convertToEntity(updatedTournamentDTO, tournamentID);
        ApiFuture<WriteResult> future = firestore.collection("Tournaments").document(tournamentID)
                .set(updatedTournament);
        log.info("Tournament {} updated successfully at {}.", tournamentID, future.get().getUpdateTime());

        return "Tournament updated at: " + future.get().getUpdateTime().toString();
    }

    // Delete a tournament
    public void deleteTournament(String tournamentID) throws ExecutionException, InterruptedException {
        log.info("Deleting tournament with ID: {}", tournamentID);
        firestore.collection("Tournaments").document(tournamentID).delete().get();
        log.info("Tournament {} deleted successfully.", tournamentID);
    }

    // Add a user to a tournament
    public String addUserToTournament(String tournamentID, String userID)
            throws ExecutionException, InterruptedException {
        log.info("Adding user {} to tournament {}.", userID, tournamentID);
        DocumentReference docRef = firestore.collection("Tournaments").document(tournamentID);
        DocumentSnapshot document = docRef.get().get();

        if (document.exists()) {
            Tournament tournament = document.toObject(Tournament.class);
            if (!tournament.getUsers().contains(userID)) {
                tournament.getUsers().add(userID);
                docRef.set(tournament).get();
                log.info("User {} added to tournament {}.", userID, tournamentID);
                return "User added successfully.";
            } else {
                log.warn("User {} is already part of the tournament {}.", userID, tournamentID);
                return "User is already part of the tournament.";
            }
        } else {
            log.error("Tournament {} not found.", tournamentID);
            throw new RuntimeException("Tournament not found with ID: " + tournamentID);
        }
    }

    // Remove a user from a tournament
    public String removeUserFromTournament(String tournamentID, String userID)
            throws ExecutionException, InterruptedException {
        log.info("Removing user {} from tournament {}.", userID, tournamentID);
        DocumentReference docRef = firestore.collection("Tournaments").document(tournamentID);
        DocumentSnapshot document = docRef.get().get();

        if (document.exists()) {
            Tournament tournament = document.toObject(Tournament.class);
            if (tournament.getUsers().remove(userID)) {
                docRef.set(tournament).get();
                log.info("User {} removed from tournament {}.", userID, tournamentID);
                return "User removed successfully.";
            } else {
                log.warn("User {} not found in tournament {}.", userID, tournamentID);
                return "User not found in the tournament.";
            }
        } else {
            log.error("Tournament {} not found.", tournamentID);
            throw new RuntimeException("Tournament not found with ID: " + tournamentID);
        }
    }

    // Get eligible tournaments for a user
    public List<Tournament> getEligibleTournamentsOfUser(String userID)
            throws ExecutionException, InterruptedException {
        QuerySnapshot snapshot = firestore.collection("Tournaments").get().get();
        return snapshot.getDocuments().stream()
                .map(doc -> doc.toObject(Tournament.class))
                .filter(tournament -> !tournament.getUsers().contains(userID))
                .collect(Collectors.toList());
    }

    // Convert TournamentDTO to Tournament entity
    private Tournament convertToEntity(TournamentDTO dto, String tid) {
        log.info("Converting TournamentDTO to Tournament entity with ID: {}", tid);
        return new Tournament(
                dto.getAgeLimit(),
                dto.getName(),
                dto.getDescription(),
                dto.getEloRequirement(),
                dto.getLocation(),
                dto.getCapacity(),
                dto.getStartDatetime(), // Use toInstant() from Firestore Timestamp
                dto.getEndDatetime(),
                tid,
                Instant.now(), // Use Instant for the created timestamp
                dto.getPrize(),
                "OPEN",
                dto.getUsers(),
                new ArrayList<>());
    }

    // Retrieve all matches from a specific tournament
    public List<Match> getAllMatchesFromTournament(String tournamentID)
            throws ExecutionException, InterruptedException {
        log.info("Fetching all matches from tournament {}.", tournamentID);
        List<Match> allMatches = new ArrayList<>();

        CollectionReference roundsCollection = firestore.collection("Tournaments")
                .document(tournamentID)
                .collection("Rounds");

        List<DocumentReference> roundDocs = new ArrayList<>();
        roundsCollection.listDocuments().forEach(roundDocs::add);

        for (DocumentReference roundDoc : roundDocs) {
            Round round = roundDoc.get().get().toObject(Round.class);
            if (round != null && round.getMatches() != null) {
                log.info("Fetched {} matches from round {}.", round.getMatches().size(), round.getRid());
                allMatches.addAll(round.getMatches());
            } else {
                log.warn("Round {} in tournament {} has no matches.", roundDoc.getId(), tournamentID);
            }
        }

        log.info("Fetched a total of {} matches from tournament {}.", allMatches.size(), tournamentID);
        return allMatches;
    }
}
