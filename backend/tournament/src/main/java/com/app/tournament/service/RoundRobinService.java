package com.app.tournament.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.tournament.DTO.ParticipantDTO;
import com.app.tournament.model.Match;
import com.app.tournament.model.Round;
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
public class RoundRobinService {

    @Autowired
    private Firestore firestore;

    private static final Logger logger = LoggerFactory.getLogger(RoundRobinService.class);

    // Generate rounds for round-robin tournament

    public void generateRoundsForTournament(String tournamentID) throws ExecutionException, InterruptedException {
        try {
            CollectionReference usersCollection = firestore.collection("Tournaments").document(tournamentID)
                    .collection("Users");

            // Fetch users and convert to ParticipantDTO
            List<QueryDocumentSnapshot> userDocs = usersCollection.get().get().getDocuments();
            if (userDocs.isEmpty()) {
                log.warn("No users found for tournament {}.", tournamentID);
                throw new RuntimeException("No users found in tournament: " + tournamentID);
            }

            List<ParticipantDTO> participants = new ArrayList<>();
            for (QueryDocumentSnapshot userDoc : userDocs) {
                String authId = userDoc.getId();
                String name = userDoc.getString("name");
                Long elo = userDoc.getLong("elo");
                String nationality = userDoc.getString("nationality");

                if (name == null || elo == null || nationality == null) {
                    log.warn("User data incomplete for user {} in tournament {}.", authId, tournamentID);
                    throw new RuntimeException("Incomplete user data for user ID: " + authId);
                }

                participants.add(new ParticipantDTO(null, authId, name, "", elo.intValue(), nationality, false));
            }

            // Check if the number of participants is odd
            if (participants.size() % 2 != 0) {
                // Remove the last participant to make the count even
                ParticipantDTO excessParticipant = participants.remove(participants.size() - 1);

                // Remove the excess player from Firestore
                usersCollection.document(excessParticipant.getAuthId()).delete().get();
                log.info("Removed excess player with ID {} from tournament {}", excessParticipant.getAuthId(),
                        tournamentID);

                // Also remove the tournament from the user's history
                DocumentReference userDocRef = firestore.collection("Users").document(excessParticipant.getAuthId());
                userDocRef.update("tournamentHistory", FieldValue.arrayRemove(tournamentID)).get();
                log.info("Removed tournament {} from user {}'s history", tournamentID, excessParticipant.getAuthId());
            }

            // Proceed with round generation for the remaining participants
            generateRounds(tournamentID, participants);

        } catch (Exception e) {
            log.error("Failed to generate rounds for tournament {}: {}", tournamentID, e.getMessage());
            throw e;
        }
    }

    private void generateRounds(String tournamentID, List<ParticipantDTO> participants)
            throws ExecutionException, InterruptedException {

        CollectionReference roundsCollection = firestore.collection("Tournaments")
                .document(tournamentID)
                .collection("Rounds");

        List<Round> rounds = new ArrayList<>();
        int numPlayers = participants.size();

        // If odd number of participants, add a bye (null player)
        if (numPlayers % 2 != 0) {
            participants.add(new ParticipantDTO(null, "bye", "Bye", "", 0, "", false));
            numPlayers++;
        }

        int numRounds = numPlayers - 1;

        // Perform round-robin scheduling using the circle method
        for (int roundNumber = 1; roundNumber <= numRounds; roundNumber++) {
            List<Match> roundMatches = new ArrayList<>();
            for (int i = 0; i < numPlayers / 2; i++) {
                ParticipantDTO player1 = participants.get(i);
                ParticipantDTO player2 = participants.get(numPlayers - 1 - i);

                // Only create a match if neither player is a bye
                if (!"bye".equals(player1.getAuthId()) && !"bye".equals(player2.getAuthId())) {
                    Match match = createMatch(player1, player2, (roundNumber - 1) * (numPlayers / 2) + i + 1,
                            roundNumber);
                    roundMatches.add(match);
                    logger.info("Match created in Round {}: {} (ID: {}) vs {} (ID: {})",
                            roundNumber, player1.getName(), player1.getAuthId(), player2.getName(),
                            player2.getAuthId());
                }
            }

            // Rotate participants, keeping the first participant fixed
            Collections.rotate(participants.subList(1, numPlayers), 1);

            Round round = new Round(roundNumber, roundMatches);
            rounds.add(round);

            // Save each round to Firestore
            String roundId = String.valueOf(round.getRid());
            DocumentReference roundDocRef = roundsCollection.document(roundId);
            roundDocRef.set(round).get();
            log.info("Round {} created for tournament {} with {} matches.", roundNumber, tournamentID,
                    roundMatches.size());
        }

        // Update the tournament object with generated rounds
        DocumentReference tournamentRef = firestore.collection("Tournaments").document(tournamentID);
        tournamentRef.update("rounds", rounds).get();
    }

    private Match createMatch(ParticipantDTO player1, ParticipantDTO player2, int matchId, int roundNumber) {
        // Set player 1 and player 2's `id` for position within the match
        player1.setId("1");
        player2.setId("2");

        List<ParticipantDTO> participants = List.of(player1, player2);

        return new Match(
                matchId,
                "Round Robin Match " + matchId,
                0, // No nextMatchId in round-robin
                roundNumber,
                Instant.now(),
                "PENDING",
                null,
                participants);
    }

    public void updateNextRoundElos(String tournamentID, int currentRoundNumber)
            throws ExecutionException, InterruptedException {
        log.info("Populating next round matches for tournament {} from round {}.", tournamentID, currentRoundNumber);

        // 1. Retrieve only the necessary 'elo' field from the Users subcollection
        CollectionReference usersCollection = firestore.collection("Tournaments").document(tournamentID)
                .collection("Users");
        Map<String, Integer> eloMap = new HashMap<>();

        List<QueryDocumentSnapshot> userDocs = usersCollection.select("elo").get().get().getDocuments();
        for (QueryDocumentSnapshot userDoc : userDocs) {
            eloMap.put(userDoc.getId(), userDoc.getLong("elo").intValue());
        }

        // 2. Fetch the next round document
        DocumentReference nextRoundDocRef = firestore.collection("Tournaments").document(tournamentID)
                .collection("Rounds").document(String.valueOf(currentRoundNumber + 1));
        DocumentSnapshot nextRoundSnapshot = nextRoundDocRef.get().get();
        Round nextRound = nextRoundSnapshot.toObject(Round.class);

        if (nextRound == null) {
            throw new RuntimeException("Next round not found.");
        }

        // 3. Update each match with participants' updated Elo ratings from `eloMap`
        List<Match> updatedMatches = new ArrayList<>();
        for (Match nextMatch : nextRound.getMatches()) {
            List<ParticipantDTO> updatedParticipants = new ArrayList<>();

            for (ParticipantDTO participant : nextMatch.getParticipants()) {
                Integer updatedElo = eloMap.get(participant.getAuthId());
                if (updatedElo != null) {
                    int oldElo = participant.getElo();
                    participant.setElo(updatedElo); // Set the updated Elo for the participant
                    logger.info("Updating Elo for participant {} in match {}: old Elo = {}, new Elo = {}",
                            participant.getAuthId(), nextMatch.getId(), oldElo, updatedElo);
                }
                updatedParticipants.add(participant);
            }

            // Create updated match with updated participants
            Match updatedMatch = new Match(
                    nextMatch.getId(),
                    nextMatch.getName(),
                    nextMatch.getNextMatchId(),
                    currentRoundNumber + 1,
                    nextMatch.getStartTime(),
                    nextMatch.getState(),
                    nextMatch.getResult(),
                    updatedParticipants);
            updatedMatches.add(updatedMatch);
        }

        // 4. Set the updated matches in the next round and batch save the changes
        nextRound.setMatches(updatedMatches);
        WriteBatch batch = firestore.batch();
        batch.set(nextRoundDocRef, nextRound);

        // Update user Elo ratings in the Users subcollection, if needed
        for (ParticipantDTO participant : updatedMatches.stream()
                .flatMap(match -> match.getParticipants().stream())
                .toList()) {
            DocumentReference userRef = usersCollection.document(participant.getAuthId());
            Integer oldElo = eloMap.get(participant.getAuthId());
            if (oldElo != null && !oldElo.equals(participant.getElo())) {
                logger.info("Updating Elo in Users collection for participant {}: old Elo = {}, new Elo = {}",
                        participant.getAuthId(), oldElo, participant.getElo());
            }
            batch.update(userRef, "elo", participant.getElo());
        }

        batch.commit().get();

        log.info("Successfully populated round {} with updated match Elo ratings for tournament {}.",
                currentRoundNumber + 1, tournamentID);
    }

}
