package com.app.tournamentV2.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
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
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
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

        // Helper function to calculate the age from the date of birth (using Instant)
    private int calculateAge(Instant birthDate) {
        LocalDate birthLocalDate = birthDate.atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate currentDate = LocalDate.now();
        return Period.between(birthLocalDate, currentDate).getYears();
    }

    // // Get eligible tournaments for a user based on userID
    // public List<Tournament> getEligibleTournamentsOfUser(String userID)
    //         throws ExecutionException, InterruptedException {
    //     QuerySnapshot snapshot = firestore.collection("Tournaments").get().get();
    //     return snapshot.getDocuments().stream()
    //             .map(doc -> doc.toObject(Tournament.class))
    //             .filter(tournament -> !tournament.getUsers().contains(userID)) // Eligibility condition
    //             .collect(Collectors.toList());
    // }

    // Get eligible tournaments for a user
    public List<Tournament> getEligibleTournamentsOfUser(String userID) throws InterruptedException, ExecutionException {
        // Query the Users collection where the authID field matches the provided userID
        CollectionReference usersCollection = firestore.collection("Users");
        Query query = usersCollection.whereEqualTo("authId", userID);
        ApiFuture<QuerySnapshot> futureQuerySnapshot = query.get();
        QuerySnapshot querySnapshot = futureQuerySnapshot.get();
        
        if (querySnapshot.isEmpty()) {
            return new ArrayList<>(); // Return an empty list if no user with matching authID is found
        }
        
        DocumentSnapshot userDoc = querySnapshot.getDocuments().get(0); // Assuming authID is unique, get the first document
        Long userElo = userDoc.getLong("elo"); // Fetch user's Elo once for later comparisons
        Instant userDob = userDoc.get("dateOfBirth", Instant.class); // Fetch user's date of birth as an Instant
    
        if (userElo == null || userDob == null) {
            return new ArrayList<>(); // If user elo or DOB is missing, return empty list
        }
        
        Instant currentTimestamp = Instant.now();
        
        // Query all the tournaments in the Tournaments collection
        CollectionReference tournamentsCollection = firestore.collection("Tournaments");
        ApiFuture<QuerySnapshot> futureTournamentsQuery = tournamentsCollection.get();
        QuerySnapshot tournamentsSnapshot = futureTournamentsQuery.get();
        System.out.println("Tournaments count: " + tournamentsSnapshot.size());
    
        List<Tournament> eligibleTournaments = new ArrayList<>();
    
        for (DocumentSnapshot tournamentDoc : tournamentsSnapshot.getDocuments()) {
            System.out.println("Tournament ID: " + tournamentDoc.getId());
            if (tournamentDoc.exists()) {
                // Check if the tournament is an upcoming tournament
                Instant startDatetime = tournamentDoc.get("startDatetime", Instant.class);
                if (startDatetime != null && startDatetime.isAfter(currentTimestamp)) {
                    // Rule 1: Check if the number of users in the "users" array is less than the "capacity"
                    List<String> users = (List<String>) tournamentDoc.get("users");
                    Long capacity = tournamentDoc.getLong("capacity");
                    if (users == null || capacity == null || users.size() >= capacity) {
                        continue; // Tournament is not eligible if capacity is full
                    }
    
                    // Rule 2: Check if the user's Elo is >= the tournament's Elo requirement
                    Long eloRequirement = tournamentDoc.getLong("eloRequirement");
                    if (eloRequirement != null && userElo < eloRequirement) {
                        continue; // Tournament is not eligible if user's Elo is less than the requirement
                    }
    
                    // Rule 3: Check if the user's age is >= the tournament's age limit
                    Long ageLimit = tournamentDoc.getLong("ageLimit");
                    if (ageLimit != null) {
                        int userAge = calculateAge(userDob); // Pass userDob as Instant to calculateAge helper method
                        if (userAge < ageLimit) {
                            continue; // Tournament is not eligible if user's age is less than the limit
                        }
                    }

                    // Rule 4: Check if registration is not open
                    String status = tournamentDoc.getString("status");
                    if (status == null || !status.equals("Registration Open")) {
                        continue;
                    }

                    // Rule 5: Check if registered
                    List<String> registered = (List<String>) userDoc.get("registrationHistory");
                    if (registered != null && registered.contains(tournamentDoc.getId())) {
                        continue; // Tournament is not eligible if the user is already registered
                    }
    
                    // Add the tournament to the eligible list if all conditions are satisfied
                    eligibleTournaments.add(tournamentDoc.toObject(Tournament.class));
                }
            }
        }
    
        return eligibleTournaments; // Return the list of eligible tournaments
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
