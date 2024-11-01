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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.tournament.DTO.ParticipantDTO;
import com.app.tournament.DTO.TournamentDTO;
import com.app.tournament.model.Match;
import com.app.tournament.model.Round;
import com.app.tournament.model.Tournament;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
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
    private TournamentSchedulerService tournamentSchedulerService;

    private static final Logger logger = LoggerFactory.getLogger(TournamentService.class);

    // @Autowired

    // private EliminationService eliminationService;

    // Create a new tournament and ensure tid matches the document ID
    public String createTournament(TournamentDTO tournamentDTO) throws ExecutionException, InterruptedException {
        log.info("Creating new tournament...");
        DocumentReference docRef = firestore.collection("Tournaments").document();
        String generatedId = docRef.getId();
        log.info("Generated tournament ID: {}", generatedId);

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

    public String addUserToTournament(String tournamentID, String userID)
            throws ExecutionException, InterruptedException {
        log.info("Adding user {} to tournament {}.", userID, tournamentID);

        // Reference to the user in the main Users collection
        DocumentReference userRef = firestore.collection("Users").document(userID);
        DocumentSnapshot userDoc = userRef.get().get();

        if (!userDoc.exists()) {
            log.error("User {} not found in the Users collection.", userID);
            throw new RuntimeException("User not found with ID: " + userID);
        }

        // Retrieve user details from the main Users collection
        String name = userDoc.getString("name");
        String nationality = userDoc.getString("nationality");
        Long elo = userDoc.getLong("elo");

        if (name == null || nationality == null || elo == null) {
            log.error("Missing user data for user {}: name={}, nationality={}, elo={}", userID, name, nationality, elo);
            throw new RuntimeException("Incomplete user data for ID: " + userID);
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
        DocumentReference tournamentUserRef = usersCollection.document(userID);

        // Check if the user already exists in the tournament's Users subcollection
        DocumentSnapshot tournamentUserDoc = tournamentUserRef.get().get();
        if (tournamentUserDoc.exists()) {
            log.warn("User {} is already part of the tournament {}.", userID, tournamentID);
            return "User is already part of the tournament.";
        }

        // Prepare the data to be stored in the tournament's Users subcollection
        Map<String, Object> userData = Map.of(
                "userID", userID,
                "name", name,
                "nationality", nationality,
                "elo", elo,
                "joinedAt", Instant.now());

        // Add the user to the tournament's Users subcollection
        tournamentUserRef.set(userData).get();
        log.info("User {} added to tournament {} with nationality {} and Elo {}.", userID, tournamentID, nationality,
                elo);

        // Add the tournament ID to the user's registration history
        userRef.update("registrationHistory", FieldValue.arrayUnion(tournamentID)).get();
        log.info("Tournament {} added to registration history of user {}.", tournamentID, userID);

        return "User added successfully.";
    }

    // Remove a user from a tournament
    public String removeUserFromTournament(String tournamentID, String userID)
            throws ExecutionException, InterruptedException {
        log.info("Removing user {} from tournament {}.", userID, tournamentID);

        // Reference to the tournament document
        DocumentReference tournamentRef = firestore.collection("Tournaments").document(tournamentID);
        DocumentSnapshot tournamentDoc = tournamentRef.get().get();

        if (!tournamentDoc.exists()) {
            log.error("Tournament {} not found.", tournamentID);
            throw new RuntimeException("Tournament not found with ID: " + tournamentID);
        }

        // Reference to the Users subcollection within the tournament
        CollectionReference usersCollection = tournamentRef.collection("Users");
        DocumentReference userRef = usersCollection.document(userID);

        // Check if the user exists in the tournament's subcollection
        DocumentSnapshot userDoc = userRef.get().get();
        if (!userDoc.exists()) {
            log.warn("User {} not found in tournament {}.", userID, tournamentID);
            return "User not found in the tournament.";
        }

        // Remove the user document from the tournament's Users subcollection
        userRef.delete().get();
        log.info("User {} removed from tournament {}.", userID, tournamentID);

        // Reference to the user document in the main Users collection
        DocumentReference mainUserRef = firestore.collection("Users").document(userID);

        // Remove the tournament ID from the user's registration history
        mainUserRef.update("registrationHistory", FieldValue.arrayRemove(tournamentID)).get();
        log.info("Tournament {} removed from registration history of user {}.", tournamentID, userID);

        return "User removed successfully.";
    }

    public List<Tournament> getEligibleTournamentsOfUser(String userID)
            throws InterruptedException, ExecutionException {
        // Retrieve the user document directly by document ID (authId)
        DocumentReference userDocRef = firestore.collection("Users").document(userID);
        DocumentSnapshot userDoc = userDocRef.get().get();

        if (!userDoc.exists()) {
            return new ArrayList<>(); // Return an empty list if no user with matching authID is found
        }

        Long userElo = userDoc.getLong("elo"); // Fetch user's Elo once for comparisons
        Instant userDob = userDoc.get("dateOfBirth", Instant.class); // Fetch user's date of birth as an Instant
        List<String> registrationHistory = (List<String>) userDoc.get("registrationHistory");

        if (userElo == null || userDob == null) {
            return new ArrayList<>(); // If user Elo or DOB is missing, return an empty list
        }

        Instant currentTimestamp = Instant.now();

        // Retrieve all tournaments asynchronously
        CollectionReference tournamentsCollection = firestore.collection("Tournaments");
        ApiFuture<QuerySnapshot> futureTournamentsQuery = tournamentsCollection.get();
        QuerySnapshot tournamentsSnapshot = futureTournamentsQuery.get();

        // Run additional status checks if needed
        checkAndUpdateStatus(tournamentsSnapshot);

        List<Tournament> eligibleTournaments = new ArrayList<>();

        // Check each tournament for eligibility
        for (DocumentSnapshot tournamentDoc : tournamentsSnapshot.getDocuments()) {
            if (!tournamentDoc.exists())
                continue;

            // Rule 1: Check if the tournament status is "Open"
            if (!"Open".equals(tournamentDoc.getString("status")))
                continue;

            // Rule 2: Check if the capacity is not full
            List<String> users = (List<String>) tournamentDoc.get("users");
            Long capacity = tournamentDoc.getLong("capacity");
            if (users == null || capacity == null || users.size() >= capacity)
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

            // Add the tournament if all conditions are satisfied
            eligibleTournaments.add(tournamentDoc.toObject(Tournament.class));
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
                // new ArrayList<>(),
                // new ArrayList<>(),
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

    // Method to retrieve all users from the user subcollection of a specific
    // tournament
    public List<Map<String, Object>> getAllUsersFromTournament(String tournamentID)
            throws ExecutionException, InterruptedException {
        logger.info("Starting retrieval of users from tournament with ID: {}", tournamentID);

        CollectionReference usersRef = firestore.collection("Tournaments")
                .document(tournamentID)
                .collection("Users");

        ApiFuture<QuerySnapshot> future = usersRef.get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        List<Map<String, Object>> users = new ArrayList<>();
        for (DocumentSnapshot document : documents) {
            users.add(document.getData()); // Retrieve document data as Map
        }

        logger.info("Retrieved {} users from tournament with ID: {}", users.size(), tournamentID);
        return users;
    }

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

    // Method to get upcoming tournaments
    public List<Tournament> getUpcomingTournamentsOfUser(String userID)
            throws InterruptedException, ExecutionException {
        // Retrieve the user document directly by document ID (authId)
        DocumentReference userDocRef = firestore.collection("Users").document(userID);
        DocumentSnapshot userDoc = userDocRef.get().get();

        if (!userDoc.exists()) {
            return new ArrayList<>(); // Return an empty list if no user with matching authID is found
        }

        List<String> registrationHistory = (List<String>) userDoc.get("registrationHistory");

        if (registrationHistory == null || registrationHistory.isEmpty()) {
            return new ArrayList<>(); // Return an empty list if registrationHistory is empty or null
        }

        Instant currentTimestamp = Instant.now();
        List<ApiFuture<DocumentSnapshot>> tournamentFutures = new ArrayList<>();

        // Initiate asynchronous retrieval of tournament documents
        for (String tournamentId : registrationHistory) {
            DocumentReference tournamentRef = firestore.collection("Tournaments").document(tournamentId);
            tournamentFutures.add(tournamentRef.get());
        }

        List<Tournament> upcomingTournaments = new ArrayList<>();

        // Wait for all futures to complete and process results
        for (ApiFuture<DocumentSnapshot> future : tournamentFutures) {
            DocumentSnapshot tournamentDoc = future.get();
            if (tournamentDoc.exists()) {
                Instant startDatetime = tournamentDoc.get("startDatetime", Instant.class);

                // Check if the tournament is in the future
                if (startDatetime != null && startDatetime.isAfter(currentTimestamp)) {
                    upcomingTournaments.add(tournamentDoc.toObject(Tournament.class));
                }
            }
        }

        return upcomingTournaments; // Return the list of upcoming tournaments
    }

    public List<Tournament> getPastTournamentsOfUser(String userID) throws InterruptedException, ExecutionException {
        // Retrieve the user document directly by document ID (authId)
        DocumentReference userDocRef = firestore.collection("Users").document(userID);
        DocumentSnapshot userDoc = userDocRef.get().get();

        if (!userDoc.exists()) {
            return new ArrayList<>(); // Return an empty list if no user with matching authID is found
        }

        List<String> registrationHistory = (List<String>) userDoc.get("registrationHistory");

        if (registrationHistory == null || registrationHistory.isEmpty()) {
            return new ArrayList<>(); // Return an empty list if registrationHistory is empty or null
        }

        Instant currentTimestamp = Instant.now();
        List<ApiFuture<DocumentSnapshot>> tournamentFutures = new ArrayList<>();

        // Initiate asynchronous retrieval of tournament documents
        for (String tournamentId : registrationHistory) {
            DocumentReference tournamentRef = firestore.collection("Tournaments").document(tournamentId);
            tournamentFutures.add(tournamentRef.get());
        }

        List<Tournament> pastTournaments = new ArrayList<>();

        // Wait for all futures to complete and process results
        for (ApiFuture<DocumentSnapshot> future : tournamentFutures) {
            DocumentSnapshot tournamentDoc = future.get();
            if (tournamentDoc.exists()) {
                Instant endDatetime = tournamentDoc.get("endDatetime", Instant.class);

                // Check if the tournament has ended
                if (endDatetime != null && endDatetime.isBefore(currentTimestamp)) {
                    pastTournaments.add(tournamentDoc.toObject(Tournament.class));
                }
            }
        }

        return pastTournaments; // Return the list of past tournaments
    }

    public List<Tournament> getOngoingTournamentsOfUser(String userID) throws InterruptedException, ExecutionException {
        // Retrieve the user document directly by document ID (authId)
        DocumentReference userDocRef = firestore.collection("Users").document(userID);
        DocumentSnapshot userDoc = userDocRef.get().get();

        if (!userDoc.exists()) {
            return new ArrayList<>(); // Return an empty list if no user with matching authID is found
        }

        List<String> registrationHistory = (List<String>) userDoc.get("registrationHistory");

        if (registrationHistory == null || registrationHistory.isEmpty()) {
            return new ArrayList<>(); // Return an empty list if registrationHistory is empty or null
        }

        Instant currentTimestamp = Instant.now();
        List<ApiFuture<DocumentSnapshot>> tournamentFutures = new ArrayList<>();

        // Initiate asynchronous retrieval of tournament documents
        for (String tournamentId : registrationHistory) {
            DocumentReference tournamentRef = firestore.collection("Tournaments").document(tournamentId);
            tournamentFutures.add(tournamentRef.get());
        }

        List<Tournament> ongoingTournaments = new ArrayList<>();

        // Wait for all futures to complete and process results
        for (ApiFuture<DocumentSnapshot> future : tournamentFutures) {
            DocumentSnapshot tournamentDoc = future.get();
            if (tournamentDoc.exists()) {
                Instant startDatetime = tournamentDoc.get("startDatetime", Instant.class);
                Instant endDatetime = tournamentDoc.get("endDatetime", Instant.class);

                // Check if the tournament is ongoing
                if (startDatetime != null && endDatetime != null &&
                        currentTimestamp.isAfter(startDatetime) && currentTimestamp.isBefore(endDatetime)) {
                    ongoingTournaments.add(tournamentDoc.toObject(Tournament.class));
                }
            }
        }

        return ongoingTournaments; // Return the list of ongoing tournaments
    }
    public void updateMatchWinner(String tournamentID, int roundNumber, int matchId, String winnerName)
            throws ExecutionException, InterruptedException {

        log.info("Updating winner for match {} in round {} of tournament {}.", matchId, roundNumber, tournamentID);

        DocumentReference roundDocRef = firestore.collection("Tournaments")
                .document(tournamentID)
                .collection("Rounds")
                .document(String.valueOf(roundNumber));

        Round round = roundDocRef.get().get().toObject(Round.class);
        if (round == null) {
            log.error("Round {} not found in tournament {}.", roundNumber, tournamentID);
            throw new RuntimeException("Round not found: " + roundNumber);
        }

        List<Match> matches = round.getMatches();
        Match targetMatch = matches.stream()
                .filter(match -> match.getId() == matchId)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Match not found: " + matchId));

        log.info("Participants in match {}: {}", matchId,
                targetMatch.getParticipants().stream().map(ParticipantDTO::getName).toList());

        boolean winnerSet = false;
        for (ParticipantDTO participant : targetMatch.getParticipants()) {
            boolean isWinner = participant.getName().equals(winnerName);
            participant.setIsWinner(isWinner);
            if (isWinner) {
                winnerSet = true;
                log.info("Participant {} set as the winner for match {}.", participant.getName(), matchId);
            }
        }

        if (!winnerSet) {
            throw new RuntimeException("No participant found with name: " + winnerName);
        }

        roundDocRef.set(round).get();
        log.info("Successfully updated winner for match {} in round {} of tournament {}.", matchId, roundNumber,
                tournamentID);
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
                        System.out.println("Invalid tournament data: missing startDatetime or status.");
                        continue; // Skip if the data is invalid
                    }

                    // Calculate the time difference between now and the tournament start time
                    long timeDiff = startTime.getTime() - currentTime.getTime(); // Time difference in milliseconds
                    long oneDayInMilliseconds = 24 * 60 * 60 * 1000; // 1 day in milliseconds

                    // If less than or equal to 1 day is remaining and the tournament is still open
                    if (timeDiff <= oneDayInMilliseconds && "Open".equals(status)) {
                        tournamentDoc.getReference().update("status", "Closed");
                    } else if (endTime.getTime() < currentTime.getTime()) {
                        tournamentDoc.getReference().update("status","Completed");
                    } else if (timeDiff <= 0 && "Closed".equals(status)) {
                        tournamentDoc.getReference().update("status","Round 1");
                    } else if (status.equals("Round " + (currentRound - 1))) {
                        tournamentDoc.getReference().update("status","Round " + currentRound);
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
