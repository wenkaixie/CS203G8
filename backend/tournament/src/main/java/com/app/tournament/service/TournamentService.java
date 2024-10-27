package com.app.tournament.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.app.tournament.DTO.TournamentDTO;
import com.app.tournament.events.TournamentClosedEvent;
import com.app.tournament.model.Match;
import com.app.tournament.model.Round;
import com.app.tournament.model.Tournament;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TournamentService {

    @Autowired
    private Firestore firestore;
    
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher; // Add event publisher

    @Autowired
    private TournamentSchedulerService tournamentSchedulerService;

    // @Autowired
    
    // private EliminationService eliminationService;

    // Create a new tournament and ensure tid matches the document ID
    public String createTournament(TournamentDTO tournamentDTO) throws ExecutionException, InterruptedException {
        log.info("Creating new tournament...");
        DocumentReference docRef = firestore.collection("Tournaments").document();
        String generatedId = docRef.getId();
        log.info("Generated tournament ID: {}", generatedId);
        // what does this do ?

        addTournamentStatusListener(generatedId);

        tournamentSchedulerService.scheduleTournamentRoundGeneration(generatedId, 
                                                                    tournamentDTO.getStartDatetime());

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
        checkAndUpdateStatus(snapshot);
        List<Tournament> tournaments = snapshot.getDocuments().stream()
                .map(doc -> doc.toObject(Tournament.class))
                .collect(Collectors.toList());

        log.info("Retrieved {} tournaments.", tournaments.size());
        return tournaments;
    }

    // Retrieve all tournaments of a user
    public List<Tournament> getTournamentsOfUser(String userID) throws ExecutionException, InterruptedException {
        log.info("Fetching tournaments of user with ID: {}", userID);
        CollectionReference tournamentsCollection = firestore.collection("Tournaments");
        QuerySnapshot tournamentsSnapshot = tournamentsCollection.get().get();

        List<Tournament> tournaments = new ArrayList<>();

        for (QueryDocumentSnapshot tournamentDoc : tournamentsSnapshot) {
            // Reference to the Users subcollection within the tournament
            CollectionReference usersCollection = tournamentDoc.getReference().collection("Users");
            DocumentReference userRef = usersCollection.document(userID);
            DocumentSnapshot userDoc = userRef.get().get();

            if (userDoc.exists()) {
                tournaments.add(tournamentDoc.toObject(Tournament.class));
            }
        }

        log.info("Retrieved {} tournaments for user {}.", tournaments.size(), userID);
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
        DocumentReference tournamentRef = firestore.collection("Tournaments").document(tournamentID);
        DocumentSnapshot tournamentDoc = tournamentRef.get().get();

        if (tournamentDoc.exists()) {
            // Reference to the Users subcollection within the tournament
            CollectionReference usersCollection = tournamentRef.collection("Users");
            DocumentReference userRef = usersCollection.document(userID);

            // Check if the user already exists in the subcollection
            DocumentSnapshot userDoc = userRef.get().get();
            if (!userDoc.exists()) {
                // Add a user document to the subcollection
                Map<String, Object> userData = Map.of("userID", userID, "joinedAt", Instant.now());
                userRef.set(userData).get();
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
        DocumentReference tournamentRef = firestore.collection("Tournaments").document(tournamentID);
        DocumentSnapshot tournamentDoc = tournamentRef.get().get();

        if (tournamentDoc.exists()) {
            // Reference to the Users subcollection within the tournament
            CollectionReference usersCollection = tournamentRef.collection("Users");
            DocumentReference userRef = usersCollection.document(userID);

            // Check if the user exists in the subcollection
            DocumentSnapshot userDoc = userRef.get().get();
            if (userDoc.exists()) {
                // Remove the user document from the subcollection
                userRef.delete().get();
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
        checkAndUpdateStatus(tournamentsSnapshot);
    
        List<Tournament> eligibleTournaments = new ArrayList<>();
    
        for (DocumentSnapshot tournamentDoc : tournamentsSnapshot.getDocuments()) {
            System.out.println("Tournament ID: " + tournamentDoc.getId());
            if (tournamentDoc.exists()) {
                //Rule 1: Check if status is open
                if (tournamentDoc.get("status") == null || !tournamentDoc.get("status").equals("Open")) {
                    continue;
                }
                // Rule 2: Check if the number of users in the "users" array is less than the
                // "capacity"
                List<String> users = (List<String>) tournamentDoc.get("users");
                Long capacity = tournamentDoc.getLong("capacity");
                if (users == null || capacity == null || users.size() >= capacity) {
                    continue; // Tournament is not eligible if capacity is full
                }

                // Rule 3: Check if the user's Elo is >= the tournament's Elo requirement
                Long eloRequirement = tournamentDoc.getLong("eloRequirement");
                if (eloRequirement != null && userElo < eloRequirement) {
                    continue; // Tournament is not eligible if user's Elo is less than the requirement
                }

                // Rule 4: Check if the user's age is >= the tournament's age limit
                Long ageLimit = tournamentDoc.getLong("ageLimit");
                if (ageLimit != null) {
                    int userAge = calculateAge(userDob); // Pass userDob as Instant to calculateAge helper method
                    if (userAge < ageLimit) {
                        continue; // Tournament is not eligible if user's age is less than the limit
                    }
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
        return eligibleTournaments; // Return the list of eligible tournaments
    }
    
    // Helper function to calculate the age from the date of birth (using Instant)
    private int calculateAge(Instant birthDate) {
        LocalDate birthLocalDate = birthDate.atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate currentDate = LocalDate.now();
        return Period.between(birthLocalDate, currentDate).getYears();
    }

    // Convert TournamentDTO to Tournament entity
    private Tournament convertToEntity(TournamentDTO dto, String tid) {
        log.info("Converting TournamentDTO to Tournament entity with ID: {}", tid);
        return new Tournament(
                dto.getAdminId(),
                dto.getType(),
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
                "Open",
                dto.getUsers(),
                new ArrayList<>(),
                0);
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

    // This method adds a listener to a tournament's document
    public void addTournamentStatusListener(String tournamentId) {
        // Reference the specific tournament document in Firestore by tournamentId
        DocumentReference tournamentRef = firestore.collection("Tournaments").document(tournamentId);

        // Attach a snapshot listener to detect changes in the tournament document
        tournamentRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                System.err.println("Error listening to tournament status changes: " + e.getMessage());
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                // Extract the tournament status (assuming there's a 'status' field)
                String status = snapshot.getString("status");
                System.out.println("Tournament status changed: " + status);

                // Add any logic you want to execute on status change here
                if (status.equals("Closed")) {
                    // Get the tournament ID from Firestore snapshot
                    String tournamentID = snapshot.getString("tid");
                    log.info("Tournament status changed to Closed. TID={}", tournamentID);
                    
                    applicationEventPublisher.publishEvent(new TournamentClosedEvent(this, tournamentID));
                }
            } else {
                System.out.println("Tournament document does not exist.");
            }
        });
    }

    private void checkAndUpdateStatus(QuerySnapshot tournamentsSnapshot) {
        // Get the current time and tournament start time
        Date currentTime = new Date();
        // Loop through the tournament documents in the snapshot
        for (DocumentSnapshot tournamentDoc : tournamentsSnapshot.getDocuments()) {
            if (tournamentDoc.exists()) {
                try {
                    // Get the tournament data
                    Map<String, Object> tournamentData = tournamentDoc.getData();
                    String status = (String) tournamentData.get("status");
                    Date startTime = tournamentDoc.getDate("startDatetime");
                    Date endTime = tournamentDoc.getDate("endDatetime");

                    if (startTime == null || status == null) {
                        System.out.println("Invalid tournament data: missing startDatetime or status.");
                        continue; // Skip if the data is invalid
                    }

                    // Calculate the time difference between now and the tournament start time
                    long timeDiff = startTime.getTime() - currentTime.getTime(); // Time difference in milliseconds
                    long oneDayInMilliseconds = 24 * 60 * 60 * 1000; // 1 day in milliseconds

                    // If less than or equal to 1 day is remaining and the tournament is still open
                    if (timeDiff <= oneDayInMilliseconds && "Open".equals(status)) {
                        // Update the status to "Closed"
                        tournamentDoc.getReference().update("status", "Closed");
                    } else if (endTime.getTime() < currentTime.getTime()) {
                        tournamentDoc.getReference().update("status","Completed");
                    } else if (timeDiff <= 0 && "Closed".equals(status)) {
                        tournamentDoc.getReference().update("status","Ongoing");
                    } else {
                        System.out.println("No update needed for tournament: " + tournamentDoc.getId());
                    }
                } catch (Exception e) {
                    System.err.println("Error processing tournament: " + e);
                }
            }
        }
    }
}
