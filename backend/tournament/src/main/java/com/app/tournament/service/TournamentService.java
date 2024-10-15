package com.app.tournament.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.tournament.DTO.TournamentDTO;
import com.app.tournament.DTO.TournamentRoundDTO;
import com.app.tournament.model.Tournament;
import com.app.tournament.model.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;

@Service("tournamentService1")
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


    // this is bad, should be using create tournament method in controller
    // Populate Tournament object
    public Tournament populateTournament(TournamentDTO dto, String tournamentId) {
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
        tournament.setCreatedTimestamp(Timestamp.now());
        tournament.setAgeLimit(dto.getAgeLimit());
        tournament.setUsers(new ArrayList<>());
        return tournament;
    }

    // Calculate number of rounds needed
    private int calculateRounds(int capacity) {
        int rounds = 0;
        while (capacity > 1) {
            capacity /= 2;
            rounds++;
        }
        return rounds;
    }

    // Create tournament rounds
    public void createTournamentRounds(String tournamentId, int numberOfRounds) throws Exception {
        for (int i = 1; i <= numberOfRounds; i++) {
            TournamentRoundDTO roundDTO = new TournamentRoundDTO();
            roundDTO.setTid(tournamentId);
            roundDTO.setRoundNumber(i);
            roundDTO.setMids(new ArrayList<>());

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
    public String updateTournament(String tournamentID, TournamentDTO updatedTournament)
            throws InterruptedException, ExecutionException {
        DocumentReference tournamentRef = firestore.collection("Tournaments").document(tournamentID);
        Map<String, Object> updates = new HashMap<>();

        updates.put("name", updatedTournament.getName());
        updates.put("description", updatedTournament.getDescription());
        updates.put("eloRequirement", updatedTournament.getEloRequirement());
        updates.put("location", updatedTournament.getLocation());
        updates.put("startDatetime", (updatedTournament.getStartDatetime()));
        updates.put("endDatetime", (updatedTournament.getEndDatetime()));
        updates.put("prize", updatedTournament.getPrize());

        tournamentRef.update(updates).get();
        return "Tournament updated successfully.";
    }

    // Helper method to convert LocalDateTime to Timestamp
    private Timestamp convertToTimestamp(LocalDateTime dateTime) {
        Instant instant = dateTime.atZone(ZoneId.systemDefault()).toInstant();
        return Timestamp.of(Date.from(instant));
    }

    // Delete a tournament
    public void deleteTournament(String tournamentID) throws InterruptedException, ExecutionException {
        firestore.collection("Tournaments").document(tournamentID).delete().get();
    }

    // Add user to tournament
    public String addUserToTournament(String tournamentID, String userID) throws InterruptedException, ExecutionException {
        DocumentReference tournamentRef = firestore.collection("Tournaments").document(tournamentID);
        tournamentRef.update("users", FieldValue.arrayUnion(userID)).get();
        return "Player added successfully.";
    }

    // Remove user from tournament
    public String removeUserFromTournament(String tournamentID, String userID) throws InterruptedException, ExecutionException {
        DocumentReference tournamentRef = firestore.collection("Tournaments").document(tournamentID);
        tournamentRef.update("users", FieldValue.arrayRemove(userID)).get();
        return "Player removed successfully.";
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
    
    // Helper function to calculate the age from the date of birth (using Instant)
    private int calculateAge(Instant birthDate) {
        LocalDate birthLocalDate = birthDate.atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate currentDate = LocalDate.now();
        return Period.between(birthLocalDate, currentDate).getYears();
    }

   
    // Method to add a player to a tournament
    public String addPlayerToTournament(String tournamentID, String playerID)
            throws InterruptedException, ExecutionException {
        // Reference the tournament document in Firestore
        DocumentReference tournamentRef = firestore.collection("Tournaments").document(tournamentID);

        // Add the playerID directly to the players array field in the tournament document
        tournamentRef.update("players", FieldValue.arrayUnion(playerID)).get();

        return "Player added successfully to the tournament.";
    }
    
    public String removePlayerFromTournament(String tournamentID, String playerID)
        throws InterruptedException, ExecutionException {
        DocumentReference tournamentRef = firestore.collection("Tournaments").document(tournamentID);
        tournamentRef.update("players", FieldValue.arrayRemove(playerID)).get();
        return "Player removed successfully from the tournament.";
    }

    
    // Calculate age from birth date
    public int calculateAge(Timestamp birthDate) {
        LocalDate birthLocalDate = birthDate.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return Period.between(birthLocalDate, LocalDate.now()).getYears();
    }

    // Get user info by ID
    public User getUserInfo(String userID) throws InterruptedException, ExecutionException {
        Query query = firestore.collection("Users").whereEqualTo("authId", userID);
        QuerySnapshot snapshot = query.get().get();
        if (snapshot.isEmpty()) return null;
        return snapshot.getDocuments().get(0).toObject(User.class);
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

    // Method to get upcoming tournaments
    public List<Tournament> getUpcomingTournamentsOfUser(String userID) throws InterruptedException, ExecutionException {
        CollectionReference usersCollection = firestore.collection("Users");
        Query query = usersCollection.whereEqualTo("authId", userID);
        ApiFuture<QuerySnapshot> futureQuerySnapshot = query.get();
        QuerySnapshot querySnapshot = futureQuerySnapshot.get();
    
        if (querySnapshot.isEmpty()) {
            return new ArrayList<>(); // Return an empty list if no user with matching authID is found
        }
        
        DocumentSnapshot userDoc = querySnapshot.getDocuments().get(0); // Assuming authID is unique, get the first document
        
        List<String> registrationHistory = (List<String>) userDoc.get("registrationHistory");
        
        if (registrationHistory == null || registrationHistory.isEmpty()) {
            return new ArrayList<>(); // Return an empty list if registrationHistory is empty or null
        }
    
        Instant currentTimestamp = Instant.now();
        List<Tournament> upcomingtTournaments = new ArrayList<>();
    
        for (String tournamentId : registrationHistory) {
            System.out.println("Tournament ID: " + tournamentId);
            // Retrieve each tournament document
            DocumentReference tournamentRef = firestore.collection("Tournaments").document(tournamentId);
            ApiFuture<DocumentSnapshot> futureTournamentDoc = tournamentRef.get();
            DocumentSnapshot tournamentDoc = futureTournamentDoc.get();
    
            if (tournamentDoc.exists()) {
                // Check if the tournament is in the future
                Instant startDatetime = tournamentDoc.get("startDatetime", Instant.class);
                if (startDatetime != null && startDatetime.isAfter(currentTimestamp)) {
                    // Convert the document to a Tournament object and add it to the list
                    upcomingtTournaments.add(tournamentDoc.toObject(Tournament.class));
                }
            }
        }
    
        return upcomingtTournaments; // Return the list of past tournaments
    }    

    // Method to get past tournaments
    public List<Tournament> getPastTournamentsOfUser(String userID) throws InterruptedException, ExecutionException {
        CollectionReference usersCollection = firestore.collection("Users");
        Query query = usersCollection.whereEqualTo("authId", userID);
        ApiFuture<QuerySnapshot> futureQuerySnapshot = query.get();
        QuerySnapshot querySnapshot = futureQuerySnapshot.get();
    
        if (querySnapshot.isEmpty()) {
            return new ArrayList<>(); // Return an empty list if no user with matching authID is found
        }
        
        DocumentSnapshot userDoc = querySnapshot.getDocuments().get(0); // Assuming authID is unique, get the first document
        
        List<String> registrationHistory = (List<String>) userDoc.get("registrationHistory");
        
        if (registrationHistory == null || registrationHistory.isEmpty()) {
            return new ArrayList<>(); // Return an empty list if registrationHistory is empty or null
        }
    
        Instant currentTimestamp = Instant.now();
        List<Tournament> pastTournaments = new ArrayList<>();
    
        for (String tournamentId : registrationHistory) {
            System.out.println("Tournament ID: " + tournamentId);
            // Retrieve each tournament document
            DocumentReference tournamentRef = firestore.collection("Tournaments").document(tournamentId);
            ApiFuture<DocumentSnapshot> futureTournamentDoc = tournamentRef.get();
            DocumentSnapshot tournamentDoc = futureTournamentDoc.get();
    
            if (tournamentDoc.exists()) {
                // Check if the tournament is in the future
                Instant endDatetime = tournamentDoc.get("endDatetime", Instant.class);
                if (endDatetime != null && endDatetime.isBefore(currentTimestamp)) {
                    // Convert the document to a Tournament object and add it to the list
                    pastTournaments.add(tournamentDoc.toObject(Tournament.class));
                }
            }
        }
    
        return pastTournaments; // Return the list of past tournaments
    }    

    
    // Method to get ongoing tournaments
    public List<Tournament> getOngoingTournamentsOfUser(String userID) throws InterruptedException, ExecutionException {
        CollectionReference usersCollection = firestore.collection("Users");
        Query query = usersCollection.whereEqualTo("authId", userID);
        ApiFuture<QuerySnapshot> futureQuerySnapshot = query.get();
        QuerySnapshot querySnapshot = futureQuerySnapshot.get();

        if (querySnapshot.isEmpty()) {
            return new ArrayList<>(); // Return an empty list if no user with matching authID is found
        }
        
        DocumentSnapshot userDoc = querySnapshot.getDocuments().get(0); // Assuming authID is unique, get the first document
        
        List<String> registrationHistory = (List<String>) userDoc.get("registrationHistory");
        
        if (registrationHistory == null || registrationHistory.isEmpty()) {
            return new ArrayList<>(); // Return an empty list if registrationHistory is empty or null
        }

        Instant currentTimestamp = Instant.now();
        List<Tournament> ongoingTournaments = new ArrayList<>();

        for (String tournamentId : registrationHistory) {
            System.out.println("Tournament ID: " + tournamentId);
            // Retrieve each tournament document
            DocumentReference tournamentRef = firestore.collection("Tournaments").document(tournamentId);
            ApiFuture<DocumentSnapshot> futureTournamentDoc = tournamentRef.get();
            DocumentSnapshot tournamentDoc = futureTournamentDoc.get();

            if (tournamentDoc.exists()) {
                // Retrieve startDatetime and endDatetime
                Instant startDatetime = tournamentDoc.get("startDatetime", Instant.class);
                Instant endDatetime = tournamentDoc.get("endDatetime", Instant.class);

                // Check if the tournament is ongoing (current time is between startDatetime and endDatetime)
                if (startDatetime != null && endDatetime != null && 
                    currentTimestamp.isAfter(startDatetime) && currentTimestamp.isBefore(endDatetime)) {
                    // Convert the document to a Tournament object and add it to the list
                    ongoingTournaments.add(tournamentDoc.toObject(Tournament.class));
                }
            }
        }

        return ongoingTournaments; // Return the list of ongoing tournaments
    }
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
