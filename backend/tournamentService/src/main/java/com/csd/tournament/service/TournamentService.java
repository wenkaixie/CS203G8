package com.csd.tournament.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.csd.shared_library.DTO.MatchResultUpdateRequest;
import com.csd.shared_library.DTO.TournamentDTO;
import com.csd.shared_library.enumerator.MatchResult;
import com.csd.shared_library.enumerator.TournamentType;
import com.csd.shared_library.model.Match;
import com.csd.shared_library.model.Round;
import com.csd.shared_library.model.Tournament;
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
import com.google.cloud.firestore.WriteBatch;
import com.google.cloud.firestore.WriteResult;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TournamentService {

    @Autowired
    private Firestore firestore;

    @Autowired
    private TournamentSchedulerService tournamentSchedulerService;

    @Autowired
    private RoundRobinService roundRobinService;

    @Autowired
    private EliminationService eliminationService;

 

    // refactor attempt
    // Tested working 19 Nov jon
    public String createTournament(TournamentDTO tournamentDTO) throws ExecutionException, InterruptedException {
        log.info("Creating new tournament...");

        DocumentReference docRef = firestore.collection("Tournaments").document();
        String generatedId = docRef.getId();
        log.info("Generated tournament ID: {}", generatedId);

        TournamentType tournamentType = tournamentDTO.getType();
        tournamentSchedulerService.scheduleTournamentRoundGeneration(generatedId,
                tournamentDTO.getStartDatetime(),
                tournamentType);

        Tournament tournament = convertToEntity(tournamentDTO, generatedId);
        ApiFuture<WriteResult> future = docRef.set(tournament);
        future.get();
        log.info("Tournament {} created successfully.", generatedId);

        return generatedId;
    }

    // refactor attempt
    public void deleteTournament(String tournamentID) throws ExecutionException, InterruptedException {
        log.info("Initiating deletion of tournament with ID: {}", tournamentID);

        DocumentReference tournamentRef = firestore.collection("Tournaments").document(tournamentID);

        try {
            // Check if the tournament exists
            if (!tournamentRef.get().get().exists()) {
                log.warn("Tournament with ID {} not found. Deletion aborted.", tournamentID);
                throw new RuntimeException("Tournament not found with ID: " + tournamentID);
            }

            // Perform deletion
            tournamentRef.delete().get();
            log.info("Tournament with ID {} deleted successfully.", tournamentID);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error occurred while deleting tournament with ID {}: {}", tournamentID, e.getMessage(), e);
            throw e; // Rethrow the exception for proper propagation
        } catch (Exception e) {
            log.error("Unexpected error occurred while deleting tournament with ID {}: {}", tournamentID,
                    e.getMessage(), e);
            throw new RuntimeException("Unexpected error occurred during tournament deletion: " + e.getMessage(), e);
        }
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

    // see if need to implement
    public void reinstateTournament(String tournamentID) {
        // Logic to reinstate tournament in Firestore
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
    // Modified by Wenkai 5/11
    public List<Tournament> getAllTournaments() throws ExecutionException, InterruptedException {
        log.info("Fetching all tournaments...");
        CollectionReference tournamentsCollection = firestore.collection("Tournaments");
        QuerySnapshot tournamentsSnapshot = tournamentsCollection.get().get();
        checkAndUpdateStatus(tournamentsSnapshot);

        List<Tournament> tournaments = new ArrayList<>();
        for (QueryDocumentSnapshot document : tournamentsSnapshot) {
            try {
                // Create the Tournament object using toObject()
                Tournament tournament = document.toObject(Tournament.class);

                // Manually convert Timestamp fields to Instant
                if (document.getTimestamp("startDatetime") != null) {
                    tournament.setStartDatetime(document.getTimestamp("startDatetime").toDate().toInstant());
                }
                if (document.getTimestamp("endDatetime") != null) {
                    tournament.setEndDatetime(document.getTimestamp("endDatetime").toDate().toInstant());
                }
                if (document.getTimestamp("createdTimestamp") != null) {
                    tournament.setCreatedTimestamp(document.getTimestamp("createdTimestamp").toDate().toInstant());
                }

                // Add the fully initialized Tournament object to the list
                tournaments.add(tournament);
            } catch (Exception e) {
                log.error("Error deserializing tournament document ID: {} - {}", document.getId(), e.getMessage());
            }
        }

        log.info("Retrieved {} tournaments.", tournaments.size());
        return tournaments;
    }

    // Retrieve all tournaments of a user
    // Updated Wenkai 5/11
    public List<Tournament> getTournamentsOfUser(String authId) throws ExecutionException, InterruptedException {
        log.info("Fetching tournaments of user with ID: {}", authId);

        // Retrieve the user document directly by document ID (authId)
        DocumentReference userDocRef = firestore.collection("Users").document(authId);
        DocumentSnapshot userDoc = userDocRef.get().get();

        if (!userDoc.exists()) {
            log.error("User not found with ID: {}", authId);
            throw new RuntimeException("User not found with ID: " + authId);
        }

        List<String> registrationHistory = (List<String>) userDoc.get("registrationHistory");

        if (registrationHistory == null || registrationHistory.isEmpty()) {
            log.warn("User {} has no registered tournaments.", authId);
            return new ArrayList<>(); // Return an empty list if registrationHistory is empty or null
        }

        List<Tournament> tournaments = new ArrayList<>();
        for (String tournamentId : registrationHistory) {
            DocumentSnapshot tournamentDoc = firestore.collection("Tournaments").document(tournamentId).get().get();
            if (tournamentDoc.exists()) {
                try {
                    // Create the Tournament object
                    Tournament tournament = tournamentDoc.toObject(Tournament.class);

                    // Manually convert Timestamp fields to Instant
                    if (tournamentDoc.getTimestamp("startDatetime") != null) {
                        tournament.setStartDatetime(tournamentDoc.getTimestamp("startDatetime").toDate().toInstant());
                    }
                    if (tournamentDoc.getTimestamp("endDatetime") != null) {
                        tournament.setEndDatetime(tournamentDoc.getTimestamp("endDatetime").toDate().toInstant());
                    }
                    if (tournamentDoc.getTimestamp("createdTimestamp") != null) {
                        tournament.setCreatedTimestamp(
                                tournamentDoc.getTimestamp("createdTimestamp").toDate().toInstant());
                    }

                    tournaments.add(tournament);
                } catch (Exception e) {
                    log.error("Error deserializing tournament document ID: {} - {}", tournamentId, e.getMessage());
                }
            }
        }

        log.info("Retrieved {} tournaments of user with ID: {}", tournaments.size(), authId);
        return tournaments;
    }


    public String addUserToTournament(String tournamentID, String authId)
            throws ExecutionException, InterruptedException {
        log.info("Adding user {} to tournament {}.", authId, tournamentID);

        // Reference to the user in the main Users collection
        DocumentReference userRef = firestore.collection("Users").document(authId);
        DocumentSnapshot userDoc = userRef.get().get();

        if (!userDoc.exists()) {
            log.error("User {} not found in the Users collection.", authId);
            throw new RuntimeException("User not found with ID: " + authId);
        }

        // Retrieve user details from the main Users collection
        String name = userDoc.getString("name");
        String nationality = userDoc.getString("nationality");
        Long elo = userDoc.getLong("elo");

        if (name == null || nationality == null || elo == null) {
            log.error("Missing user data for user {}: name={}, nationality={}, elo={}", authId, name, nationality, elo);
            throw new RuntimeException("Incomplete user data for ID: " + authId);
        }

        // Reference to the tournament
        DocumentReference tournamentRef = firestore.collection("Tournaments").document(tournamentID);
        DocumentSnapshot tournamentDoc = tournamentRef.get().get();

        if (!tournamentDoc.exists()) {
            log.error("Tournament {} not found.", tournamentID);
            throw new RuntimeException("Tournament not found with ID: " + tournamentID);
        }

        // Reference to the Users subcollection within the tournament
        CollectionReference usersCollection = tournamentRef.collection("Users");
        DocumentReference tournamentUserRef = usersCollection.document(authId);

        // Check if the user already exists in the tournament's Users subcollection
        DocumentSnapshot tournamentUserDoc = tournamentUserRef.get().get();
        if (tournamentUserDoc.exists()) {
            log.warn("User {} is already part of the tournament {}.", authId, tournamentID);
            return "User is already part of the tournament.";
        }

        // Prepare the data to be stored in the tournament's Users subcollection
        Map<String, Object> userData = Map.of(
                "authId", authId,
                "name", name,
                "nationality", nationality,
                "elo", elo,
                "score", 0,
                "joinedAt", Instant.now());

        // Add the user to the tournament's Users subcollection
        tournamentUserRef.set(userData).get();
        log.info("User {} added to tournament {} with nationality {} and Elo {}.", authId, tournamentID, nationality,
                elo);

        // Add the tournament ID to the user's registration history
        userRef.update("registrationHistory", FieldValue.arrayUnion(tournamentID)).get();
        log.info("Tournament {} added to registration history of user {}.", tournamentID, authId);

        return "User added successfully.";
    }

    public String removeUserFromTournament(String tournamentID, String authId)
            throws ExecutionException, InterruptedException {
        log.info("Removing user {} from tournament {}.", authId, tournamentID);

        // Reference to the tournament document
        DocumentReference tournamentRef = firestore.collection("Tournaments").document(tournamentID);
        DocumentSnapshot tournamentDoc = tournamentRef.get().get();

        if (!tournamentDoc.exists()) {
            log.error("Tournament {} not found.", tournamentID);
            throw new RuntimeException("Tournament not found with ID: " + tournamentID);
        }

        // Reference to the Users subcollection within the tournament
        CollectionReference usersCollection = tournamentRef.collection("Users");
        DocumentReference userRef = usersCollection.document(authId);

        // Check if the user exists in the tournament's subcollection
        DocumentSnapshot userDoc = userRef.get().get();
        if (!userDoc.exists()) {
            log.warn("User {} not found in tournament {}.", authId, tournamentID);
            return "User not found in the tournament.";
        }

        // Remove the user document from the tournament's Users subcollection
        userRef.delete().get();
        log.info("User {} removed from tournament {}.", authId, tournamentID);

        // Reference to the user document in the main Users collection
        DocumentReference mainUserRef = firestore.collection("Users").document(authId);

        // Remove the tournament ID from the user's registration history
        mainUserRef.update("registrationHistory", FieldValue.arrayRemove(tournamentID)).get();
        log.info("Tournament {} removed from registration history of user {}.", tournamentID, authId);

        return "User removed successfully.";
    }


    public List<Map<String, Object>> getAllUsersFromTournament(String tournamentID)
            throws ExecutionException, InterruptedException {
        log.info("Starting retrieval of users from tournament with ID: {}", tournamentID);

        CollectionReference usersRef = firestore.collection("Tournaments")
                .document(tournamentID)
                .collection("Users");

        ApiFuture<QuerySnapshot> future = usersRef.get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        List<Map<String, Object>> users = new ArrayList<>();
        for (DocumentSnapshot document : documents) {
            users.add(document.getData()); // Retrieve document data as Map
        }

        log.info("Retrieved {} users from tournament with ID: {}", users.size(), tournamentID);
        return users;
    }

    public List<String> getUserIdsForTournament(String tournamentID) throws ExecutionException, InterruptedException {
        CollectionReference usersCollection = firestore.collection("Tournaments").document(tournamentID)
                .collection("Users");
        QuerySnapshot querySnapshot = usersCollection.get().get();
        List<String> userIds = new ArrayList<>();
        for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
            userIds.add(document.getId());
        }
        return userIds;
    }

    public String getAdminIdForTournament(String tournamentID) throws ExecutionException, InterruptedException {
        DocumentReference tournamentRef = firestore.collection("Tournaments").document(tournamentID);
        DocumentSnapshot snapshot = tournamentRef.get().get();
        if (!snapshot.exists()) {
            throw new RuntimeException("Tournament not found with ID: " + tournamentID);
        }
        return snapshot.getString("adminId");
    }

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





    // is this used?
    public List<Tournament> getTournamentsWithPagination(int limit, String lastTournamentID)
            throws InterruptedException, ExecutionException {
        Query query = firestore.collection("Tournaments").limit(limit);

        if (lastTournamentID != null) {
            DocumentSnapshot lastTournament = firestore.collection("Tournaments").document(lastTournamentID).get()
                    .get();
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




    // Function to get eligible tournaments of a user
    // edited wenkai 5/11
    public List<Tournament> getEligibleTournamentsOfUser(String authId)
            throws InterruptedException, ExecutionException {
        System.out.println("Retrieving user document for authId: " + authId);

        // Retrieve the user document directly by document ID (authId)
        DocumentReference userDocRef = firestore.collection("Users").document(authId);
        DocumentSnapshot userDoc = userDocRef.get().get();

        if (!userDoc.exists()) {
            System.out.println("No user found with ID: " + authId);
            return new ArrayList<>(); // Return an empty list if no user with matching authID is found
        }

        Long userElo = userDoc.getLong("elo");
        Instant userDob = userDoc.get("dateOfBirth", Instant.class);
        List<String> registrationHistory = (List<String>) userDoc.get("registrationHistory");

        if (userElo == null || userDob == null) {
            return new ArrayList<>();
        }

        CollectionReference tournamentsCollection = firestore.collection("Tournaments");
        ApiFuture<QuerySnapshot> futureTournamentsQuery = tournamentsCollection.get();
        QuerySnapshot tournamentsSnapshot = futureTournamentsQuery.get();
        checkAndUpdateStatus(tournamentsSnapshot);

        System.out.println("Total tournaments retrieved: " + tournamentsSnapshot.size());

        List<Tournament> initiallyEligibleTournaments = new ArrayList<>();

        // Step 1: Initial eligibility checks
        for (DocumentSnapshot tournamentDoc : tournamentsSnapshot.getDocuments()) {
            // Rule 1: Check if the tournament status is "Open"
            if (!"Open".equals(tournamentDoc.getString("status")))
                continue;

            // Rule 2: Check if the capacity is not full by counting documents in the
            // "Users" subcollection
            CollectionReference usersSubcollection = tournamentDoc.getReference().collection("Users");
            int registeredCount = usersSubcollection.get().get().size();
            Long capacity = tournamentDoc.getLong("capacity");

            System.out.println("Capacity: " + capacity + ", Registered users: " + registeredCount + " for tournament "
                    + tournamentDoc.getId());

            // Check that registered users are less than capacity
            if (capacity == null || registeredCount >= capacity)
                continue;

            // Rule 3: Check if the user's Elo meets the tournament requirement
            Long eloRequirement = tournamentDoc.getLong("eloRequirement");
            if (eloRequirement != null && userElo < eloRequirement)
                continue;

            // Rule 4: Check if the user's age meets the tournament age limit
            Long ageLimit = tournamentDoc.getLong("ageLimit");
            if (ageLimit != null) {
                int userAge = calculateAge(userDob);
                if (userAge < ageLimit)
                    continue;
            }

            // Rule 5: Check if the user is already registered
            if (registrationHistory != null && registrationHistory.contains(tournamentDoc.getId()))
                continue;

            System.out.println("All initial conditions satisfied for tournament " + tournamentDoc.getId());

            // Add to initially eligible tournaments if all conditions are satisfied
            try {
                // Attempt to create the Tournament object
                Tournament tournament = tournamentDoc.toObject(Tournament.class);

                // Manually set the Instant fields if they are not null
                if (tournamentDoc.getTimestamp("startDatetime") != null) {
                    tournament.setStartDatetime(tournamentDoc.getTimestamp("startDatetime").toDate().toInstant());
                }
                if (tournamentDoc.getTimestamp("endDatetime") != null) {
                    tournament.setEndDatetime(tournamentDoc.getTimestamp("endDatetime").toDate().toInstant());
                }
                if (tournamentDoc.getTimestamp("createdTimestamp") != null) {
                    tournament.setCreatedTimestamp(tournamentDoc.getTimestamp("createdTimestamp").toDate().toInstant());
                }

                // Add the Tournament to the list if everything is successful
                initiallyEligibleTournaments.add(tournament);
                System.out.println("Tournament " + tournamentDoc.getId() + " added to the eligible list.");
            } catch (Exception e) {
                // log the error if deserialization or addition fails
                System.out.println("Failed to add tournament " + tournamentDoc.getId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        // see what is initially eligible
        System.out.println("Total initially eligible tournaments found: " + initiallyEligibleTournaments.size());

        // Step 2: Check for date overlaps
        List<Tournament> finalEligibleTournaments = new ArrayList<>();

        // Retrieve dates of registered tournaments
        List<Instant[]> registeredTournamentDates = new ArrayList<>();

        if (registrationHistory != null) {
            System.out.println("Registration history found for user, processing tournaments...");
            for (String tournamentId : registrationHistory) {
                System.out.println("Processing registered tournament ID: " + tournamentId);

                DocumentSnapshot registeredTournamentDoc = firestore.collection("Tournaments").document(tournamentId)
                        .get().get();
                if (registeredTournamentDoc.exists()) {
                    try {
                        Timestamp startTimestamp = registeredTournamentDoc.getTimestamp("startDatetime");
                        Timestamp endTimestamp = registeredTournamentDoc.getTimestamp("endDatetime");

                        if (startTimestamp != null && endTimestamp != null) {
                            Instant startDatetime = startTimestamp.toDate().toInstant();
                            Instant endDatetime = endTimestamp.toDate().toInstant();
                            registeredTournamentDates.add(new Instant[] { startDatetime, endDatetime });
                            System.out.println(
                                    "Added tournament dates: Start - " + startDatetime + ", End - " + endDatetime);
                        } else {
                            System.out
                                    .println("Missing startDatetime or endDatetime for tournament ID: " + tournamentId);
                        }
                    } catch (Exception e) {
                        System.out.println(
                                "Error processing registered tournament " + tournamentId + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Tournament document not found for ID: " + tournamentId);
                }
            }
        } else {
            System.out.println("No registration history found for user.");
        }

        // Filter out tournaments with overlapping dates
        for (Tournament tournament : initiallyEligibleTournaments) {
            Instant startDatetime = tournament.getStartDatetime();
            Instant endDatetime = tournament.getEndDatetime();

            System.out.println("Checking overlap for tournament " + tournament.getTid());

            if (startDatetime != null && endDatetime != null) {
                boolean hasOverlap = false;
                for (Instant[] registeredDates : registeredTournamentDates) {
                    Instant registeredStart = registeredDates[0];
                    Instant registeredEnd = registeredDates[1];

                    // Check for overlap with registered tournaments
                    if ((startDatetime.isBefore(registeredEnd) && startDatetime.isAfter(registeredStart)) ||
                            (endDatetime.isBefore(registeredEnd) && endDatetime.isAfter(registeredStart)) ||
                            (startDatetime.equals(registeredStart) || endDatetime.equals(registeredEnd))) {
                        hasOverlap = true;
                        System.out.println("Tournament " + tournament.getTid() + " has overlapping dates. Skipping.");
                        break;
                    }
                }

                // Add to final list if there’s no overlap
                if (!hasOverlap) {
                    finalEligibleTournaments.add(tournament);
                    System.out.println("Tournament " + tournament.getTid() + " added to final eligible list.");
                }
            } else {
                System.out.println("Tournament " + tournament.getTid() + " missing start or end datetime.");
            }
        }

        System.out.println("Total eligible tournaments found: " + finalEligibleTournaments.size());
        return finalEligibleTournaments;
    }

    // method to get upcoming tournaments of user
    public List<Tournament> getUpcomingTournamentsOfUser(String authId)
            throws ExecutionException, InterruptedException {
        log.info("Fetching upcoming tournaments of user with ID: {}", authId);

        // Retrieve the user document directly by document ID (authId)
        DocumentReference userDocRef = firestore.collection("Users").document(authId);
        DocumentSnapshot userDoc = userDocRef.get().get();

        if (!userDoc.exists()) {
            log.error("User not found with ID: {}", authId);
            throw new RuntimeException("User not found with ID: " + authId);
        }

        List<String> registrationHistory = (List<String>) userDoc.get("registrationHistory");

        if (registrationHistory == null || registrationHistory.isEmpty()) {
            log.warn("User {} has no registered tournaments.", authId);
            return new ArrayList<>(); // Return an empty list if registrationHistory is empty or null
        }

        List<Tournament> tournaments = new ArrayList<>();
        for (String tournamentId : registrationHistory) {
            DocumentSnapshot tournamentDoc = firestore.collection("Tournaments").document(tournamentId).get().get();
            if (tournamentDoc.exists()) {
                try {
                    // Create the Tournament object
                    Tournament tournament = tournamentDoc.toObject(Tournament.class);

                    // Manually convert Timestamp fields to Instant
                    if (tournamentDoc.getTimestamp("startDatetime") != null) {
                        tournament.setStartDatetime(tournamentDoc.getTimestamp("startDatetime").toDate().toInstant());
                    }
                    if (tournamentDoc.getTimestamp("endDatetime") != null) {
                        tournament.setEndDatetime(tournamentDoc.getTimestamp("endDatetime").toDate().toInstant());
                    }
                    if (tournamentDoc.getTimestamp("createdTimestamp") != null) {
                        tournament.setCreatedTimestamp(
                                tournamentDoc.getTimestamp("createdTimestamp").toDate().toInstant());
                    }

                    // Add the fully initialized Tournament object to the list
                    if (tournament.getStartDatetime().isAfter(Instant.now())) {
                        tournaments.add(tournament);
                    }
                } catch (Exception e) {
                    log.error("Error deserializing tournament document ID: {} - {}", tournamentId, e.getMessage());
                }
            }
        }

        log.info("Retrieved {} upcoming tournaments of user with ID: {}", tournaments.size(), authId);
        return tournaments;
    }

    // method to get past tournaments of user
    public List<Tournament> getPastTournamentsOfUser(String authId) throws ExecutionException, InterruptedException {
        log.info("Fetching past tournaments of user with ID: {}", authId);

        // Retrieve the user document directly by document ID (authId)
        DocumentReference userDocRef = firestore.collection("Users").document(authId);
        DocumentSnapshot userDoc = userDocRef.get().get();

        if (!userDoc.exists()) {
            log.error("User not found with ID: {}", authId);
            throw new RuntimeException("User not found with ID: " + authId);
        }

        List<String> registrationHistory = (List<String>) userDoc.get("registrationHistory");

        if (registrationHistory == null || registrationHistory.isEmpty()) {
            log.warn("User {} has no registered tournaments.", authId);
            return new ArrayList<>(); // Return an empty list if registrationHistory is empty or null
        }

        List<Tournament> tournaments = new ArrayList<>();
        for (String tournamentId : registrationHistory) {
            DocumentSnapshot tournamentDoc = firestore.collection("Tournaments").document(tournamentId).get().get();
            if (tournamentDoc.exists()) {
                try {
                    // Create the Tournament object
                    Tournament tournament = tournamentDoc.toObject(Tournament.class);

                    // Manually convert Timestamp fields to Instant
                    if (tournamentDoc.getTimestamp("startDatetime") != null) {
                        tournament.setStartDatetime(tournamentDoc.getTimestamp("startDatetime").toDate().toInstant());
                    }
                    if (tournamentDoc.getTimestamp("endDatetime") != null) {
                        tournament.setEndDatetime(tournamentDoc.getTimestamp("endDatetime").toDate().toInstant());
                    }
                    if (tournamentDoc.getTimestamp("createdTimestamp") != null) {
                        tournament.setCreatedTimestamp(
                                tournamentDoc.getTimestamp("createdTimestamp").toDate().toInstant());
                    }

                    // Add the fully initialized Tournament object to the list
                    if (tournament.getEndDatetime().isBefore(Instant.now())) {
                        tournaments.add(tournament);
                    }
                } catch (Exception e) {
                    log.error("Error deserializing tournament document ID: {} - {}", tournamentId, e.getMessage());
                }
            }
        }

        log.info("Retrieved {} past tournaments of user with ID: {}", tournaments.size(), authId);
        return tournaments;
    }

    // method to get ongoing tournaments of user
    public List<Tournament> getOngoingTournamentsOfUser(String authId) throws ExecutionException, InterruptedException {
        log.info("Fetching ongoing tournaments of user with ID: {}", authId);

        // Retrieve the user document directly by document ID (authId)
        DocumentReference userDocRef = firestore.collection("Users").document(authId);
        DocumentSnapshot userDoc = userDocRef.get().get();

        if (!userDoc.exists()) {
            log.error("User not found with ID: {}", authId);
            throw new RuntimeException("User not found with ID: " + authId);
        }

        List<String> registrationHistory = (List<String>) userDoc.get("registrationHistory");

        if (registrationHistory == null || registrationHistory.isEmpty()) {
            log.warn("User {} has no registered tournaments.", authId);
            return new ArrayList<>(); // Return an empty list if registrationHistory is empty or null
        }

        List<Tournament> tournaments = new ArrayList<>();
        for (String tournamentId : registrationHistory) {
            DocumentSnapshot tournamentDoc = firestore.collection("Tournaments").document(tournamentId).get().get();
            if (tournamentDoc.exists()) {
                try {
                    // Create the Tournament object
                    Tournament tournament = tournamentDoc.toObject(Tournament.class);

                    // Manually convert Timestamp fields to Instant
                    if (tournamentDoc.getTimestamp("startDatetime") != null) {
                        tournament.setStartDatetime(tournamentDoc.getTimestamp("startDatetime").toDate().toInstant());
                    }
                    if (tournamentDoc.getTimestamp("endDatetime") != null) {
                        tournament.setEndDatetime(tournamentDoc.getTimestamp("endDatetime").toDate().toInstant());
                    }
                    if (tournamentDoc.getTimestamp("createdTimestamp") != null) {
                        tournament.setCreatedTimestamp(
                                tournamentDoc.getTimestamp("createdTimestamp").toDate().toInstant());
                    }

                    // Add the fully initialized Tournament object to the list
                    if (tournament.getStartDatetime().isBefore(Instant.now()) &&
                            tournament.getEndDatetime().isAfter(Instant.now())) {
                        tournaments.add(tournament);
                    }
                } catch (Exception e) {
                    log.error("Error deserializing tournament document ID: {} - {}", tournamentId, e.getMessage());
                }
            }
        }

        log.info("Retrieved {} ongoing tournaments of user with ID: {}", tournaments.size(), authId);
        return tournaments;
    }





    
    // implement in orchestrator?
    public void updateMatchResults(String tournamentID, int roundNumber,
            Map<Integer, MatchResultUpdateRequest> matchResults)
            throws ExecutionException, InterruptedException {
        DocumentReference roundDocRef = firestore.collection("Tournaments")
                .document(tournamentID)
                .collection("Rounds")
                .document(String.valueOf(roundNumber));

        Round round = roundDocRef.get().get().toObject(Round.class);
        if (round == null) {
            throw new RuntimeException("Round not found.");
        }

        WriteBatch batch = firestore.batch();

        for (Map.Entry<Integer, MatchResultUpdateRequest> entry : matchResults.entrySet()) {
            int matchId = entry.getKey();
            MatchResult result = entry.getValue().getMatchResult();

            Match targetMatch = round.getMatches().stream()
                    .filter(match -> match.getId() == matchId)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Match not found."));

            // Set the match result and winner flags
            targetMatch.updateResult(result);
            switch (result) {
                case DRAW:
                    targetMatch.getParticipants().forEach(p -> p.setIsWinner(true)); // Draw means no winner
                    break;
                case PLAYER1_WIN:
                    targetMatch.getParticipants().get(0).setIsWinner(true);
                    targetMatch.getParticipants().get(1).setIsWinner(false);
                    break;
                case PLAYER2_WIN:
                    targetMatch.getParticipants().get(0).setIsWinner(false);
                    targetMatch.getParticipants().get(1).setIsWinner(true);
                    break;
                default:
                    break;
            }
        }

        // Save the entire round with updated matches in a single batch operation
        batch.set(roundDocRef, round);

        // Commit the batch for match updates
        batch.commit().get();

        // Increment the current round for the tournament document
        DocumentReference tournamentDocRef = firestore.collection("Tournaments").document(tournamentID);
        tournamentDocRef.update("currentRound", roundNumber + 1).get();

        log.info("Batch updated all matches in round {} for tournament {} and incremented current round.", roundNumber,
                tournamentID);
    }


    // double check owner
    public void captureEloSnapshot(String tournamentID) throws ExecutionException, InterruptedException {
        log.info("Capturing Elo snapshot for tournament: {}", tournamentID);

        // Get the users subcollection for the specific tournament
        CollectionReference usersCollection = firestore.collection("Tournaments").document(tournamentID)
                .collection("Users");

        // Prepare to store Elo snapshots
        Map<String, Integer> eloSnapshot = new HashMap<>();
        List<QueryDocumentSnapshot> userDocs = usersCollection.get().get().getDocuments();

        for (QueryDocumentSnapshot userDoc : userDocs) {
            String userId = userDoc.getId();
            Integer elo = userDoc.getLong("elo").intValue();
            eloSnapshot.put(userId, elo);
        }

        // Store the Elo snapshot in Firestore under the tournament document
        DocumentReference tournamentDoc = firestore.collection("Tournaments").document(tournamentID);
        tournamentDoc.update("eloSnapshot", eloSnapshot).get();

        log.info("Elo snapshot captured successfully for tournament: {}", tournamentID);
    }



    
    private void updateEloInBatch(WriteBatch batch, String tournamentID, String player1Id, String player2Id,
            double elo1, double elo2, double AS1, double AS2) {
        double ES1 = calculateExpectedScore(elo1, elo2);
        double ES2 = calculateExpectedScore(elo2, elo1);

        int K1 = determineKFactor(elo1);
        int K2 = determineKFactor(elo2);

        double newElo1 = Math.max(0, Math.round(elo1 + K1 * (AS1 - ES1)));
        double newElo2 = Math.max(0, Math.round(elo2 + K2 * (AS2 - ES2)));

        DocumentReference player1Ref = firestore.collection("Users").document(player1Id);
        DocumentReference player2Ref = firestore.collection("Users").document(player2Id);
        DocumentReference tournamentPlayer1Ref = firestore.collection("Tournaments").document(tournamentID)
                .collection("Users").document(player1Id);
        DocumentReference tournamentPlayer2Ref = firestore.collection("Tournaments").document(tournamentID)
                .collection("Users").document(player2Id);

        batch.update(player1Ref, "elo", newElo1);
        batch.update(player2Ref, "elo", newElo2);
        batch.update(tournamentPlayer1Ref, "elo", newElo1);
        batch.update(tournamentPlayer2Ref, "elo", newElo2);

        log.info("Prepared batch update for players {} and {}: newElo1={}, newElo2={}", player1Id, player2Id,
                newElo1, newElo2);
    }

    private double calculateExpectedScore(double eloPlayer, double eloOpponent) {
        return 1 / (1 + Math.pow(10, (eloOpponent - eloPlayer) / 400));
    }

    private int determineKFactor(double elo) {
        if (elo < 2100) {
            return 32;
        } else if (elo > 2400) {
            return 16;
        } else {
            return 24;
        }
    }






    // Helper function to calculate the age from the date of birth (using Instant)
    private int calculateAge(Instant birthDate) {
        LocalDate birthLocalDate = birthDate.atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate currentDate = LocalDate.now();
        return Period.between(birthLocalDate, currentDate).getYears();
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
                    int currentRound = tournamentDoc.getLong("currentRound").intValue();

                    if (startTime == null || status == null) {
                        System.out.println("Invalid tournament data: missing startDatetime or status for tournament: "
                                + tournamentDoc.getId());
                        continue; // Skip if the data is invalid
                    }

                    // Calculate the time difference between now and the tournament start time
                    long timeDiff = startTime.getTime() - currentTime.getTime(); // Time difference in milliseconds
                    long oneDayInMilliseconds = 24 * 60 * 60 * 1000; // 1 day in milliseconds

                    // If less than or equal to 1 day is remaining and the tournament is still open
                    if (timeDiff <= oneDayInMilliseconds && "Open".equals(status)) {
                        tournamentDoc.getReference().update("status", "Closed");
                    } else if (endTime.getTime() < currentTime.getTime()) {
                        tournamentDoc.getReference().update("status", "Completed");
                    } else if (timeDiff <= 0 && "Closed".equals(status)) {
                        tournamentDoc.getReference().update("status", "Round 1");
                    } else if (status.equals("Round " + (currentRound - 1))) {
                        tournamentDoc.getReference().update("status", "Round " + currentRound);
                    } else {
                        System.out.println("No update needed for tournament: " + tournamentDoc.getId());
                    }
                } catch (Exception e) {
                    System.err.println("Error processing tournament: " + e);
                }

            }
        }
    }

    // Convert TournamentDTO to Tournament entity
    private Tournament convertToEntity(TournamentDTO dto, String tid) {
        log.info("Converting TournamentDTO to Tournament entity with ID: {}", tid);
        return new Tournament(
                dto.getAdminId(),
                dto.getType(), // Use TournamentType directly
                dto.getAgeLimit(),
                dto.getName(),
                dto.getDescription(),
                dto.getEloRequirement(),
                dto.getLocation(),
                dto.getMinSignups(),
                dto.getCapacity(),
                dto.getPrize(),
                dto.getStartDatetime(),
                dto.getEndDatetime(),
                tid,
                Instant.now(),
                "Open",
                1);
    }
}
