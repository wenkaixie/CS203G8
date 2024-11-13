package com.csd.saga.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.csd.saga.DTO.ParticipantDTO;
import com.csd.saga.model.Match;
import com.csd.saga.model.Round;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
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

    public void generateRoundsForTournament(String tournamentID)
            throws ExecutionException, InterruptedException {
        try {
            CollectionReference usersCollection = firestore.collection("Tournaments").document(tournamentID)
                    .collection("Users");

            List<QueryDocumentSnapshot> userDocs = usersCollection.get().get().getDocuments();

            if (userDocs.isEmpty()) {
                log.warn("No users found for tournament {}.", tournamentID);
                throw new RuntimeException("No users found in tournament: " + tournamentID);
            }

            List<ParticipantDTO> participants = new ArrayList<>();
            int numPlayers = userDocs.size();

            // Populate participants list from user documents
            for (QueryDocumentSnapshot userDoc : userDocs) {
                String userID = userDoc.getId();
                String name = userDoc.getString("name");
                Long elo = userDoc.getLong("elo");
                String nationality = userDoc.getString("nationality");
                Instant joinedAt = userDoc.getTimestamp("joinedAt") != null
                        ? userDoc.getTimestamp("joinedAt").toDate().toInstant()
                        : null;

                if (name == null || elo == null || nationality == null) {
                    log.warn("Incomplete data for user {} in tournament {}.", userDoc.getId(), tournamentID);
                    throw new RuntimeException("Incomplete data for user ID: " + userDoc.getId());
                }

                participants.add(new ParticipantDTO(
                        null, // id to be set when placed in matches
                        userID, // authId
                        name, // name
                        "", // resultText
                        elo.intValue(), // elo
                        nationality, // nationality
                        false // isWinner initialized as false
                ));
            }

            // Calculate nearest power of 2 within bounds of available players
            int closestPowerOfTwo = Math.min(Integer.highestOneBit(numPlayers), numPlayers);

            // Proceed with match generation based on the limited nearest power of 2
            List<ParticipantDTO> selectedParticipants = participants.subList(0, closestPowerOfTwo);
            int numRounds = calculateEliminationRounds(closestPowerOfTwo);
            generateRounds(tournamentID, selectedParticipants, closestPowerOfTwo, numRounds);

            // Remove excess players from Firestore and update their history
            if (closestPowerOfTwo < numPlayers) {
                List<QueryDocumentSnapshot> excessParticipants = userDocs.subList(closestPowerOfTwo, numPlayers);
                for (QueryDocumentSnapshot excessUserDoc : excessParticipants) {
                    String userId = excessUserDoc.getId();

                    // Delete the user from the tournament's user subcollection
                    usersCollection.document(userId).delete().get();
                    log.info("Removed excess player with ID {} from tournament {}", userId, tournamentID);

                    // Remove the tournament from the user's history
                    DocumentReference userDocRef = firestore.collection("Users").document(userId);
                    userDocRef.update("tournamentHistory", FieldValue.arrayRemove(tournamentID)).get();
                    log.info("Removed tournament {} from user {}'s history", tournamentID, userId);
                }
            }

            log.info("Tournament rounds generated and excess players removed successfully for tournament ID: {}",
                    tournamentID);

        } catch (Exception e) {
            log.error("Failed to generate rounds and manage participants for tournament {}: {}", tournamentID,
                    e.getMessage());
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
            int nextMatchId = startCounter + (participants.size() / 2) + ((i / 2) / 2);

            List<ParticipantDTO> matchParticipants = new ArrayList<>();

            // Assign players 1 and 2 to the match
            ParticipantDTO player1 = participants.get(i);
            ParticipantDTO player2 = (i + 1 < participants.size()) ? participants.get(i + 1) : null;

            // Randomly assign "W" or "B" to player1, with the opposite color for player2
            if (Math.random() > 0.5) {
                player1.setResultText("W");
                if (player2 != null)
                    player2.setResultText("B");
            } else {
                player1.setResultText("B");
                if (player2 != null)
                    player2.setResultText("W");
            }

            // Set player IDs and add them to the match participants
            player1.setId("1");
            matchParticipants.add(player1);
            if (player2 != null) {
                player2.setId("2");
                matchParticipants.add(player2);
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

    public void populateNextRoundMatches(String tournamentID, int currentRoundNumber)
            throws ExecutionException, InterruptedException {
        log.info("Populating next round matches for tournament {} from round {}.", tournamentID, currentRoundNumber);

        // 1. Retrieve only the necessary 'elo' field from Users collection
        CollectionReference usersCollection = firestore.collection("Tournaments").document(tournamentID)
                .collection("Users");
        Map<String, Integer> eloMap = new HashMap<>();

        List<QueryDocumentSnapshot> userDocs = usersCollection.select("elo").get().get().getDocuments();
        for (QueryDocumentSnapshot userDoc : userDocs) {
            eloMap.put(userDoc.getId(), userDoc.getLong("elo").intValue());
        }

        // 2. Fetch the current and next rounds in a batch
        DocumentReference currentRoundDocRef = firestore.collection("Tournaments").document(tournamentID)
                .collection("Rounds").document(String.valueOf(currentRoundNumber));
        DocumentReference nextRoundDocRef = firestore.collection("Tournaments").document(tournamentID)
                .collection("Rounds").document(String.valueOf(currentRoundNumber + 1));
        List<DocumentSnapshot> roundDocs = firestore.getAll(currentRoundDocRef, nextRoundDocRef).get();

        Round currentRound = roundDocs.get(0).toObject(Round.class);
        Round nextRound = roundDocs.get(1).toObject(Round.class);

        if (currentRound == null || nextRound == null) {
            throw new RuntimeException("Current or next round not found.");
        }

        List<Match> nextRoundMatches = new ArrayList<>();
        List<ParticipantDTO> allParticipants = new ArrayList<>(); // Collect participants for Elo update

        // 3. Determine winners, using Elo or random tiebreaker if needed
        for (Match nextMatch : nextRound.getMatches()) {
            List<ParticipantDTO> participants = new ArrayList<>();

            for (Match currentMatch : currentRound.getMatches()) {
                if (currentMatch.getNextMatchId() == nextMatch.getId()) {
                    // Check for a draw and determine winner based on Elo or random tiebreaker
                    ParticipantDTO winner = determineWinner(currentMatch, eloMap);
                    if (winner != null) {
                        Integer updatedElo = eloMap.get(winner.getAuthId());
                        winner.setElo(updatedElo != null ? updatedElo : winner.getElo());
                        winner.setIsWinner(false); // Reset winner status for next match
                        participants.add(winner);
                        allParticipants.add(winner);
                    }
                }
            }

            if (participants.size() == 2) {
                nextRoundMatches.add(new Match(
                        nextMatch.getId(),
                        nextMatch.getName(),
                        nextMatch.getNextMatchId(),
                        currentRoundNumber + 1,
                        Instant.now(),
                        "PENDING",
                        null,
                        participants));
            }
        }

        nextRound.setMatches(nextRoundMatches);
        nextRoundDocRef.set(nextRound).get();

        // 4. Use batch to update Elo ratings only if changed
        WriteBatch batch = firestore.batch();
        for (ParticipantDTO participant : allParticipants) {
            DocumentReference userRef = usersCollection.document(participant.getAuthId());
            batch.update(userRef, "elo", participant.getElo());
        }
        batch.commit().get();

        log.info("Successfully populated round {} with matches and updated user Elo ratings.", currentRoundNumber + 1);
    }

    // Helper method for determining the winner with tiebreaker logic
    private ParticipantDTO determineWinner(Match currentMatch, Map<String, Integer> eloMap) {
        List<ParticipantDTO> participants = currentMatch.getParticipants();
        if (participants.size() < 2)
            return null;

        ParticipantDTO participant1 = participants.get(0);
        ParticipantDTO participant2 = participants.get(1);

        // Check for draw and apply Elo-based tiebreaker
        if (currentMatch.isDraw()) {
            int elo1 = eloMap.getOrDefault(participant1.getAuthId(), participant1.getElo());
            int elo2 = eloMap.getOrDefault(participant2.getAuthId(), participant2.getElo());

            if (elo1 > elo2)
                return participant1;
            else if (elo2 > elo1)
                return participant2;
            else
                return Math.random() > 0.5 ? participant1 : participant2; // Random if Elo is tied
        } else {
            // Return the actual winner if not a draw
            return participants.stream().filter(ParticipantDTO::getIsWinner).findFirst().orElse(null);
        }
    }
}
