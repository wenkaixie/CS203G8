package com.app.tournament.service;

import java.time.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.tournament.DTO.TournamentDTO;
import com.app.tournament.DTO.TournamentRoundDTO;
import com.app.tournament.model.Tournament;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;

@Service
public class TournamentService {

    @Autowired
    private Firestore firestore;

    @Autowired
    private TournamentRoundService roundService;

    // Create a new tournament
    public String createTournament(TournamentDTO tournamentDTO) throws Exception {
        try {
            DocumentReference newTournamentRef = firestore.collection("Tournaments").document();
            Tournament tournament = populateTournament(tournamentDTO, newTournamentRef.getId());
            
            // Save the tournament in Firestore
            ApiFuture<WriteResult> futureTournament = newTournamentRef.set(tournament);
            WriteResult result = futureTournament.get();
            System.out.println("Tournament created at: " + result.getUpdateTime());

            // Create rounds based on tournament capacity
            int numberOfRounds = calculateRounds(tournament.getCapacity());
            createTournamentRounds(tournament.getTid(), numberOfRounds);

            // Return the ID of the newly created tournament
            return tournament.getTid();

        } catch (InterruptedException | ExecutionException e) {
            throw new Exception("Error creating tournament: " + e.getMessage(), e);
        }
    }

    // Helper to populate the Tournament object
    private Tournament populateTournament(TournamentDTO dto, String tournamentId) {
        Tournament tournament = new Tournament();
        tournament.setTid(tournamentId);
        tournament.setName(dto.getName());
        tournament.setDescription(dto.getDescription());
        tournament.setEloRequirement(dto.getEloRequirement());
        tournament.setLocation(dto.getLocation());
        tournament.setStartDatetime(dto.getStartDatetime());
        tournament.setEndDatetime(dto.getEndDatetime());
        tournament.setCapacity(dto.getCapacity());
        tournament.setPrize(dto.getPrize());
        tournament.setStatus("Registration Open");
        tournament.setCreatedTimestamp(Instant.now());
        tournament.setAgeLimit(dto.getAgeLimit());
        tournament.setUsers(new ArrayList<>());
        return tournament;
    }

    // Calculate the number of rounds needed
    private int calculateRounds(int capacity) {
        int rounds = 0;
        while (capacity > 1) {
            capacity /= 2;
            rounds++;
        }
        return rounds;
    }

    // Create rounds for the tournament
    private void createTournamentRounds(String tournamentId, int numberOfRounds) throws Exception {
        for (int i = 1; i <= numberOfRounds; i++) {
            TournamentRoundDTO roundDTO = new TournamentRoundDTO();
            roundDTO.setTid(tournamentId);
            roundDTO.setRoundNumber(i);
            roundDTO.setMids(new ArrayList<>()); // Initialize empty match list

            roundService.createTournamentRound(roundDTO);
        }
    }

    // Retrieve a tournament by ID
    public Tournament getTournamentById(String tournamentID) throws Exception {
        DocumentReference tournamentRef = firestore.collection("Tournaments").document(tournamentID);
        DocumentSnapshot document = tournamentRef.get().get();

        if (document.exists()) {
            return document.toObject(Tournament.class);
        } else {
            throw new Exception("Tournament not found with ID: " + tournamentID);
        }
    }

    // Get all tournaments
    public List<Tournament> getAllTournaments() throws InterruptedException, ExecutionException {
        List<QueryDocumentSnapshot> documents = firestore.collection("Tournaments").get().get().getDocuments();
        return documents.stream().map(doc -> doc.toObject(Tournament.class)).collect(Collectors.toList());
    }

    // Update tournament details
    public String updateTournament(String tournamentID, TournamentDTO updatedTournament) throws InterruptedException, ExecutionException {
        DocumentReference tournamentRef = firestore.collection("Tournaments").document(tournamentID);
        Map<String, Object> updates = new HashMap<>();
        
        updates.put("name", updatedTournament.getName());
        updates.put("description", updatedTournament.getDescription());
        updates.put("eloRequirement", updatedTournament.getEloRequirement());
        updates.put("location", updatedTournament.getLocation());
        updates.put("startDatetime", updatedTournament.getStartDatetime());
        updates.put("endDatetime", updatedTournament.getEndDatetime());
        updates.put("prize", updatedTournament.getPrize());

        tournamentRef.update(updates).get();
        return "Tournament updated successfully.";
    }

    // Delete a tournament
    public void deleteTournament(String tournamentID) throws InterruptedException, ExecutionException {
        firestore.collection("Tournaments").document(tournamentID).delete().get();
    }

    // Add a user to a tournament
    public String addUserToTournament(String tournamentID, String userID) throws InterruptedException, ExecutionException {
        DocumentReference tournamentRef = firestore.collection("Tournaments").document(tournamentID);
        tournamentRef.update("users", FieldValue.arrayUnion(userID)).get();
        return "Player added successfully.";
    }

    // Remove a user from a tournament
    public String removeUserFromTournament(String tournamentID, String userID) throws InterruptedException, ExecutionException {
        DocumentReference tournamentRef = firestore.collection("Tournaments").document(tournamentID);
        tournamentRef.update("users", FieldValue.arrayRemove(userID)).get();
        return "Player removed successfully.";
    }

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

    // Helper: Check if the user is eligible for a tournament
    private boolean isEligible(User user, Tournament tournament, Instant now) {
        if (!now.isBefore(tournament.getStartDatetime())) return false;
        if (tournament.getUsers().size() >= tournament.getCapacity()) return false;
        if (user.getElo() < tournament.getEloRequirement()) return false;
        int userAge = calculateAge(user.getDateOfBirth());
        return userAge >= tournament.getAgeLimit();
    }

    // Helper: Calculate age from birth date
    private int calculateAge(Instant birthDate) {
        LocalDate birthLocalDate = birthDate.atZone(ZoneId.systemDefault()).toLocalDate();
        return Period.between(birthLocalDate, LocalDate.now()).getYears();
    }

    // Helper: Get user info by ID
    private User getUserInfo(String userID) throws InterruptedException, ExecutionException {
        Query query = firestore.collection("Users").whereEqualTo("authId", userID);
        QuerySnapshot snapshot = query.get().get();
        if (snapshot.isEmpty()) return null;
        return snapshot.getDocuments().get(0).toObject(User.class);
    }



    // @Autowired
    // private TaskScheduler taskScheduler;

    // // Method to schedule a task for each tournament
    // public void scheduleCloseRegistration(Tournament tournament) {
    //     Instant startDatetime = tournament.getStartDatetime();
    //     Instant closeRegistrationTime = startDatetime.minus(Duration.ofHours(24)); 
    //     taskScheduler.schedule(() -> closeRegistration(tournament.getTid()), closeRegistrationTime);

    // }

    // // This method will be triggered at the scheduled time to close registration
    // private void closeRegistration(String tournamentID) {
    //     DocumentReference tournamentRef = firestore.collection("Tournaments").document(tournamentID);
    //     tournamentRef.update("status", "Registration Closed").addListener(() -> {
    //         System.out.println("Tournament registration closed for: " + tournamentID);
    //     }, Runnable::run);
    // }
}
