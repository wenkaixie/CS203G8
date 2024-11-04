package com.app.tournament.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.tournament.DTO.ParticipantDTO;
import com.app.tournament.model.Match;
import com.app.tournament.model.Round;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.WriteBatch;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EliminationService {

    @Autowired
    private Firestore firestore;

    // @Autowired
    // private TournamentService tournamentService;

    // @Autowired
    // private UserService userService;

    // Generate rounds and matches for the tournament
    public void generateRoundsForTournament(String tournamentID) throws ExecutionException, InterruptedException {
        try {
            // Get reference to the Users subcollection of the specified tournament
            CollectionReference usersCollection = firestore.collection("Tournaments").document(tournamentID)
                    .collection("Users");

            // Fetch all users from the Users subcollection
            List<QueryDocumentSnapshot> userDocs = usersCollection.get().get().getDocuments();

            if (userDocs.isEmpty()) {
                log.warn("No users found for tournament {}.", tournamentID);
                throw new RuntimeException("No users found in tournament: " + tournamentID);
            }

            // Extract user details (userID, name, elo, nationality) from each document
            List<ParticipantDTO> participants = new ArrayList<>();
            int numPlayers = userDocs.size();

            for (QueryDocumentSnapshot userDoc : userDocs) {
                String userID = userDoc.getId();
                String name = userDoc.getString("name");
                Long elo = userDoc.getLong("elo");
                String nationality = userDoc.getString("nationality");

                if (name == null || elo == null || nationality == null) {
                    log.warn("User data incomplete for user {} in tournament {}.", userDoc.getId(), tournamentID);
                    throw new RuntimeException("Incomplete user data for user ID: " + userDoc.getId());
                }

                // Create ParticipantDTO and add to list
                participants.add(new ParticipantDTO(
                        null, // id will be set when used in matches (1 or 2)
                        userID,
                        name,
                        "", // resultText will be set when the match is finished
                        elo.intValue(),
                        nationality,
                        false // isWinner will be set later when match results are known
                ));
            }

            // Calculate number of rounds needed based on number of players
            int numRounds = calculateEliminationRounds(numPlayers);

            // Generate rounds for the tournament with the retrieved participants and player
            // count
            generateRounds(tournamentID, participants, numPlayers, numRounds);
        } catch (Exception e) {
            log.error("Failed to generate rounds for tournament {}: {}", tournamentID, e.getMessage());
            throw e;
        }
    }

    private int calculateEliminationRounds(int numPlayers) {
        return (int) Math.ceil(Math.log(numPlayers) / Math.log(2));
    }

    private void generateRounds(String tournamentID, List<ParticipantDTO> participants, int numPlayers, int numRounds)
            throws ExecutionException, InterruptedException {

        CollectionReference roundsCollection = firestore.collection("Tournaments")
                .document(tournamentID)
                .collection("Rounds");

        int matchCounter = 1;
        List<Match> previousRoundMatches = null;

        for (int roundNumber = 1; roundNumber <= numRounds; roundNumber++) {
            int numMatches = numPlayers / 2;
            numPlayers /= 2;

            List<Match> matches = (roundNumber == 1)
                    ? createFirstRoundMatches(participants, matchCounter)
                    : createEmptyMatches(tournamentID, numMatches, roundNumber, matchCounter, previousRoundMatches);

            matchCounter += numMatches;

            Round round = new Round(roundNumber, matches);
            String roundId = String.valueOf(roundNumber);

            DocumentReference roundDocRef = roundsCollection.document(roundId);
            roundDocRef.set(round).get();

            log.info("Round {} created for tournament {} with {} matches.", roundNumber, tournamentID, numMatches);

            previousRoundMatches = matches;
        }
    }

    private List<Match> createFirstRoundMatches(List<ParticipantDTO> participants, int startCounter) {
        List<Match> matches = new ArrayList<>();

        for (int i = 0; i < participants.size(); i += 2) {
            int matchId = startCounter + (i / 2);

            // Correct nextMatchId calculation: Group every two matches (i / 4 to group
            // matches in pairs of 2)
            int nextMatchId = startCounter + (participants.size() / 2) + ((i / 2) / 2);

            List<ParticipantDTO> matchParticipants = new ArrayList<>();

            // Assign the participants to player 1 and player 2 in the match
            participants.get(i).setId("1");
            matchParticipants.add(participants.get(i));

            if (i + 1 < participants.size()) {
                participants.get(i + 1).setId("2");
                matchParticipants.add(participants.get(i + 1));
            }

            Match match = new Match(
                    matchId,
                    "Match " + matchId,
                    nextMatchId,
                    1,
                    Instant.now(),
                    "PENDING",
                    null,
                    matchParticipants);

            matches.add(match);
        }
        return matches;
    }

    private List<Match> createEmptyMatches(String tournamentID, int numMatches, int roundNumber, int startCounter,
            List<Match> previousRoundMatches) throws ExecutionException, InterruptedException {

        List<Match> matches = new ArrayList<>();

        for (int i = 0; i < numMatches; i++) {
            int matchId = startCounter + i;

            // Create the match
            Match match = new Match(
                    matchId,
                    "Match " + matchId, // Name of the match
                    0, // Placeholder for nextMatchId
                    roundNumber, // Round number text
                    Instant.now(), // Start time of the match
                    "PENDING", // Initial state of the match
                    null,
                    new ArrayList<>() // No participants initially
            );

            matches.add(match);

            // Link two matches from the previous round to this match
            if (previousRoundMatches != null && i * 2 < previousRoundMatches.size()) {
                int parent1Id = previousRoundMatches.get(i * 2).getId();
                int parent2Id = previousRoundMatches.get(i * 2 + 1).getId();

                // Call updateNextMatchId to save the nextMatchId in Firestore
                updateNextMatchId(tournamentID, roundNumber - 1, parent1Id, matchId);
                updateNextMatchId(tournamentID, roundNumber - 1, parent2Id, matchId);
            }
        }
        return matches;
    }

    public void updateNextMatchId(String tournamentID, int roundNumber, int matchId, int nextMatchId)
            throws ExecutionException, InterruptedException {

        log.info("Updating nextMatchId for Match {} in Round {} of Tournament {} to {}",
                matchId, roundNumber, tournamentID, nextMatchId);

        // Reference the specific match within a round
        DocumentReference roundDocRef = firestore.collection("Tournaments")
                .document(tournamentID)
                .collection("Rounds")
                .document(String.valueOf(roundNumber));

        // Fetch the round document
        Round round = roundDocRef.get().get().toObject(Round.class);
        if (round == null) {
            throw new RuntimeException("Round " + roundNumber + " not found for tournament " + tournamentID);
        }

        // Find the target match in the round
        List<Match> matches = round.getMatches();
        Match targetMatch = matches.stream()
                .filter(match -> match.getId() == matchId)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Match " + matchId + " not found in round " + roundNumber));

        // Update the nextMatchId
        targetMatch.setNextMatchId(nextMatchId);

        // Save the updated round back to Firestore
        roundDocRef.set(round).get();
        log.info("Successfully updated nextMatchId to {} for Match {}.", nextMatchId, matchId);
    }

    // public void populateNextRoundMatches(String tournamentID, int
    // currentRoundNumber)
    // throws ExecutionException, InterruptedException {

    // log.info("Populating next round matches for tournament {} from round {}.",
    // tournamentID, currentRoundNumber);

    // // Fetch the current round's document
    // DocumentReference currentRoundDocRef = firestore.collection("Tournaments")
    // .document(tournamentID)
    // .collection("Rounds")
    // .document(String.valueOf(currentRoundNumber));

    // Round currentRound = currentRoundDocRef.get().get().toObject(Round.class);
    // if (currentRound == null) {
    // throw new RuntimeException("Current round not found.");
    // }

    // // Fetch the next round's document
    // int nextRoundNumber = currentRoundNumber + 1;
    // DocumentReference nextRoundDocRef = firestore.collection("Tournaments")
    // .document(tournamentID)
    // .collection("Rounds")
    // .document(String.valueOf(nextRoundNumber));

    // Round nextRound = nextRoundDocRef.get().get().toObject(Round.class);
    // if (nextRound == null) {
    // throw new RuntimeException("Next round not found.");
    // }

    // List<Match> nextRoundMatches = new ArrayList<>();

    // // Iterate over each match in the next round
    // for (int i = 0; i < nextRound.getMatches().size(); i++) {
    // Match nextMatch = nextRound.getMatches().get(i);
    // int matchId = nextMatch.getId();

    // // Find the two matches from the current round where nextMatchId matches the
    // // matchId of the next round match
    // List<Match> parentMatches = new ArrayList<>();
    // for (Match currentMatch : currentRound.getMatches()) {
    // if (currentMatch.getNextMatchId() == matchId) {
    // parentMatches.add(currentMatch);
    // }
    // }

    // if (parentMatches.size() != 2) {
    // log.warn("Match {} in round {} has incorrect number of parent matches: {}.",
    // matchId, nextRoundNumber,
    // parentMatches.size());
    // continue;
    // }

    // // Extract winners from the parent matches
    // List<ParticipantDTO> participants = new ArrayList<>();
    // for (Match parentMatch : parentMatches) {
    // parentMatch.getParticipants().stream()
    // .filter(ParticipantDTO::getIsWinner)
    // .findFirst()
    // .ifPresent(winner -> {
    // try {
    // // Retrieve the latest Elo from the specific tournament's Users subcollection
    // DocumentReference userRef = firestore.collection("Tournaments")
    // .document(
    // tournamentID) // replace with the actual tournament ID
    // .collection("Users")
    // .document(winner.getAuthId());

    // // Fetch the user document
    // DocumentSnapshot userDoc = userRef.get().get();
    // Long updatedElo = userDoc.getLong("elo"); // Retrieve the Elo from the
    // Firestore
    // // document

    // if (updatedElo != null) {
    // participants.add(new ParticipantDTO(
    // String.valueOf(participants.size() + 1), // Assign new player ID (1 or 2)
    // winner.getAuthId(), // Carry over the winner's UID
    // winner.getName(), // Carry over the winner's name
    // "", // Result text will be determined in the next match
    // updatedElo.intValue(), // Set the Elo retrieved from Firestore
    // winner.getNationality(), // Carry over the winner's nationality
    // false // Set isWinner as false for now, will be decided in the next match
    // ));
    // } else {
    // log.warn("Elo rating not found for user with authId: {}",
    // winner.getAuthId());
    // }
    // } catch (InterruptedException | ExecutionException e) {
    // log.error("Error retrieving Elo rating for user {}: {}", winner.getAuthId(),
    // e.getMessage());
    // }
    // });
    // }

    // if (participants.size() < 2) {
    // log.warn("Not enough winners found for match {} in round {}.", matchId,
    // nextRoundNumber);
    // continue;
    // }

    // // Create a new match with the participants
    // Match updatedMatch = new Match(
    // matchId,
    // nextMatch.getName(),
    // nextMatch.getNextMatchId(),
    // nextRoundNumber,
    // Instant.now(),
    // "PENDING",
    // participants);

    // nextRoundMatches.add(updatedMatch);

    // log.info("Updated match {} for round {} with participants: {}",
    // matchId, nextRoundNumber,
    // participants.stream().map(ParticipantDTO::getName).toList());
    // }

    // // Save the updated next round matches to Firestore
    // nextRound.setMatches(nextRoundMatches);
    // nextRoundDocRef.set(nextRound).get();

    // log.info("Successfully populated round {} with {} matches.", nextRoundNumber,
    // nextRoundMatches.size());

    // // Increment and update currentRoundNumber in the tournament document
    // DocumentReference tournamentDocRef =
    // firestore.collection("Tournaments").document(tournamentID);
    // tournamentDocRef.update("currentRound", currentRoundNumber + 1).get(); //
    // Update currentRound directly

    // log.info("Updated tournament {} current round to {}.", tournamentID,
    // currentRoundNumber + 1);
    // }

    // public Tournament getTournamentById(String tournamentID) throws
    // ExecutionException, InterruptedException {
    // log.info("Fetching tournament with ID: {}", tournamentID);
    // DocumentSnapshot document =
    // firestore.collection("Tournaments").document(tournamentID).get().get();

    // if (!document.exists()) {
    // log.error("Tournament not found with ID: {}", tournamentID);
    // throw new RuntimeException("Tournament not found with ID: " + tournamentID);
    // }

    // log.info("Tournament {} retrieved successfully.", tournamentID);
    // return document.toObject(Tournament.class);
    // }

    public void populateNextRoundMatches(String tournamentID, int currentRoundNumber)
            throws ExecutionException, InterruptedException {
        log.info("Populating next round matches for tournament {} from round {}.", tournamentID, currentRoundNumber);

        // Batch retrieve all user Elo ratings in the tournament
        CollectionReference usersCollection = firestore.collection("Tournaments").document(tournamentID)
                .collection("Users");
        Map<String, Integer> eloMap = new HashMap<>();

        List<QueryDocumentSnapshot> userDocs = usersCollection.get().get().getDocuments();
        for (QueryDocumentSnapshot userDoc : userDocs) {
            eloMap.put(userDoc.getId(), userDoc.getLong("elo").intValue());
        }

        // Fetch the current and next rounds
        DocumentReference currentRoundDocRef = firestore.collection("Tournaments").document(tournamentID)
                .collection("Rounds").document(String.valueOf(currentRoundNumber));
        Round currentRound = currentRoundDocRef.get().get().toObject(Round.class);

        int nextRoundNumber = currentRoundNumber + 1;
        DocumentReference nextRoundDocRef = firestore.collection("Tournaments").document(tournamentID)
                .collection("Rounds").document(String.valueOf(nextRoundNumber));
        Round nextRound = nextRoundDocRef.get().get().toObject(Round.class);

        if (currentRound == null || nextRound == null) {
            throw new RuntimeException("Current or next round not found.");
        }

        List<Match> nextRoundMatches = new ArrayList<>();
        List<ParticipantDTO> allParticipants = new ArrayList<>(); // Collect all participants for Elo update

        // Process each match in the next round and assign winners
        for (Match nextMatch : nextRound.getMatches()) {
            List<ParticipantDTO> participants = new ArrayList<>();

            for (Match currentMatch : currentRound.getMatches()) {
                if (currentMatch.getNextMatchId() == nextMatch.getId()) {
                    currentMatch.getParticipants().stream()
                            .filter(ParticipantDTO::getIsWinner)
                            .findFirst()
                            .ifPresent(winner -> {
                                Integer updatedElo = eloMap.get(winner.getAuthId());
                                ParticipantDTO participant = new ParticipantDTO(
                                        String.valueOf(participants.size() + 1), // ID within the match
                                        winner.getAuthId(),
                                        winner.getName(),
                                        "", // Result text to be updated post-match
                                        updatedElo != null ? updatedElo : winner.getElo(), // Elo from cache or existing
                                                                                           // Elo
                                        winner.getNationality(),
                                        false // isWinner will be set in the next match
                                );
                                participants.add(participant);
                                allParticipants.add(participant); // Add to allParticipants for batch update
                            });
                }
            }

            if (participants.size() == 2) {
                nextRoundMatches.add(new Match(
                        nextMatch.getId(),
                        nextMatch.getName(),
                        nextMatch.getNextMatchId(),
                        nextRoundNumber,
                        Instant.now(),
                        "PENDING",
                        null,
                        participants));
            }
        }

        nextRound.setMatches(nextRoundMatches);
        nextRoundDocRef.set(nextRound).get();

        // Batch write updated Elo ratings back to Firestore
        WriteBatch batch = firestore.batch();
        for (ParticipantDTO participant : allParticipants) {
            DocumentReference userRef = usersCollection.document(participant.getAuthId());
            batch.update(userRef, "elo", participant.getElo());
        }
        batch.commit().get();

        log.info("Successfully populated round {} with matches and updated user Elo ratings.", nextRoundNumber);
    }

}
