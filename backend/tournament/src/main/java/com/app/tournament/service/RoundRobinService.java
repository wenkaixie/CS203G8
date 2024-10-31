package com.app.tournament.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.tournament.DTO.ParticipantDTO;
import com.app.tournament.model.Match;
import com.app.tournament.model.Round;
import com.app.tournament.model.Tournament;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RoundRobinService {

    @Autowired
    private Firestore firestore;

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private UserService userService; // Assuming you have a service to fetch user details

    // Method to generate round-robin rounds and matches
    public void generateRoundsForTournament(String tournamentID) throws ExecutionException, InterruptedException {
        try {
            // Fetch tournament from the tournamentService
            Tournament tournament = tournamentService.getTournamentById(tournamentID);

            // Retrieve the Users subcollection from Firestore
            CollectionReference usersCollection = firestore.collection("Tournaments").document(tournamentID)
                    .collection("Users");

            // Fetch all users from the Users subcollection and convert them to
            // ParticipantDTO
            List<QueryDocumentSnapshot> userDocs = usersCollection.get().get().getDocuments();
            if (userDocs.isEmpty()) {
                log.warn("No users found for tournament {}.", tournamentID);
                throw new RuntimeException("No users found in tournament: " + tournamentID);
            }

            List<ParticipantDTO> participants = new ArrayList<>();
            for (QueryDocumentSnapshot userDoc : userDocs) {
                String uid = userDoc.getId(); // User's unique ID (document ID)
                String name = userDoc.getString("name");
                Long elo = userDoc.getLong("elo");
                String nationality = userDoc.getString("nationality");

                if (name == null || elo == null || nationality == null) {
                    log.warn("User data incomplete for user {} in tournament {}.", uid, tournamentID);
                    throw new RuntimeException("Incomplete user data for user ID: " + uid);
                }

                // Create a new ParticipantDTO with the existing constructor, passing an empty
                // resultText and isWinner as false
                participants.add(new ParticipantDTO(
                        null, // You can assign 'null' here since this will be set later as player 1 or player
                              // 2 in each match
                        uid,
                        name,
                        "", // resultText (empty for now)
                        elo.intValue(),
                        nationality,
                        false // Set isWinner as false for now
                ));
            }

            // Generate round-robin rounds and matches
            generateRounds(tournamentID, participants, participants.size());
        } catch (Exception e) {
            log.error("Failed to generate rounds for tournament {}: {}", tournamentID, e.getMessage());
            throw e;
        }
    }


    // Method to generate rounds for a round-robin tournament
    private void generateRounds(String tournamentID, List<ParticipantDTO> participants, int numPlayers)
            throws ExecutionException, InterruptedException {

        CollectionReference roundsCollection = firestore.collection("Tournaments")
                .document(tournamentID)
                .collection("Rounds");

        List<Match> allMatches = new ArrayList<>();
        int matchCounter = 1;

        // Create matches for every pair of players
        for (int i = 0; i < numPlayers; i++) {
            for (int j = i + 1; j < numPlayers; j++) {
                // Create a match between player i and player j using the ParticipantDTO objects
                Match match = createMatch(participants.get(i), participants.get(j), matchCounter);

                allMatches.add(match);
                matchCounter++;
            }
        }

        // Distribute matches across rounds
        int roundsRequired = (int) Math.ceil((double) numPlayers / 2);
        int roundNumber = 1;
        List<Round> rounds = new ArrayList<>();

        while (!allMatches.isEmpty()) {
            List<Match> roundMatches = new ArrayList<>();

            for (int i = 0; i < roundsRequired && !allMatches.isEmpty(); i++) {
                roundMatches.add(allMatches.remove(0));
            }

            Round round = new Round(roundNumber, roundMatches);
            rounds.add(round);
            roundNumber++;

            // Save the round to Firestore
            String roundId = String.valueOf(round.getRid());
            DocumentReference roundDocRef = roundsCollection.document(roundId);
            roundDocRef.set(round).get();

            log.info("Round {} created for tournament {} with {} matches.", round.getRid(), tournamentID,
                    roundMatches.size());
        }

        // Update tournament object with the generated rounds
        DocumentReference tournamentRef = firestore.collection("Tournaments").document(tournamentID);
        tournamentRef.update("rounds", rounds).get();
    }

    // Helper method to create a match between two players
    private Match createMatch(ParticipantDTO player1, ParticipantDTO player2, int matchCounter) {
        List<ParticipantDTO> participants = new ArrayList<>();

        // Set player 1 and player 2 IDs to "1" and "2" respectively, maintaining other
        // fields
        player1.setId("1");
        player2.setId("2");

        participants.add(player1);
        participants.add(player2);

        return new Match(
                matchCounter,
                "Round Robin Match " + matchCounter,
                0, // No nextMatchId in round-robin
                0, // Round number will be assigned later
                Instant.now(),
                "PENDING",
                participants);
    }

}
