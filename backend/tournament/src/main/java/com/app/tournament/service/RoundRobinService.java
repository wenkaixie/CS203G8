package com.app.tournament.service;

import com.app.tournament.model.Match;
import com.app.tournament.DTO.ParticipantDTO;
import com.app.tournament.model.Round;
import com.app.tournament.model.Tournament;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class RoundRobinService {

    @Autowired
    private Firestore firestore;

    @Autowired
    private TournamentService tournamentService;

    // Method to generate round-robin rounds and matches
    public void generateRoundsForTournament(String tournamentID) throws ExecutionException, InterruptedException {
        try {
            // Retrieve tournament and users
            Tournament tournament = tournamentService.getTournamentById(tournamentID);
            List<String> users = tournament.getUsers();
            int numPlayers = users.size();

            // Generate round-robin rounds and matches
            generateRounds(tournamentID, users, numPlayers);
        } catch (Exception e) {
            log.error("Failed to generate rounds for tournament {}: {}", tournamentID, e.getMessage());
            throw e;
        }
    }

    // Method to generate rounds for a round-robin tournament
    private void generateRounds(String tournamentID, List<String> users, int numPlayers)
            throws ExecutionException, InterruptedException {

        CollectionReference roundsCollection = firestore.collection("Tournaments")
                .document(tournamentID)
                .collection("Rounds");

        List<Match> allMatches = new ArrayList<>();
        int matchCounter = 1;

        // Create matches for every pair of players
        for (int i = 0; i < numPlayers; i++) {
            for (int j = i + 1; j < numPlayers; j++) {
                Match match = createMatch(users.get(i), users.get(j), matchCounter);
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
    private Match createMatch(String player1, String player2, int matchCounter) {
        List<ParticipantDTO> participants = new ArrayList<>();
        participants.add(new ParticipantDTO("1", player1, "", false));
        participants.add(new ParticipantDTO("2", player2, "", false));

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
