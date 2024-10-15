package com.app.tournamentV2.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.tournamentV2.DTO.ParticipantDTO;
import com.app.tournamentV2.model.Match;
import com.app.tournamentV2.model.Round;
import com.app.tournamentV2.model.Tournament;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EliminationService {

    @Autowired
    private Firestore firestore;

    @Autowired
    private TournamentService tournamentService;

    // Main method to generate rounds and matches for the tournament
    public void generateRoundsForTournament(String tournamentID) throws ExecutionException, InterruptedException {
        try {
            // 1. Retrieve tournament and users
            Tournament tournament = tournamentService.getTournamentById(tournamentID);
            List<String> users = tournament.getUsers();

            // 2. Calculate the number of rounds (elimination format)
            int numPlayers = users.size();
            int numRounds = calculateEliminationRounds(numPlayers);

            // 3. Generate and store rounds with matches
            generateRounds(tournamentID, users, numPlayers, numRounds);
        } catch (Exception e) {
            log.error("Failed to generate rounds for tournament {}: {}", tournamentID, e.getMessage());
            throw e;
        }
    }

    // Calculate the number of elimination rounds (log base 2)
    private int calculateEliminationRounds(int numPlayers) {
        return (int) Math.ceil(Math.log(numPlayers) / Math.log(2));
    }

    // Generate rounds and assign players to matches in the first round
    private void generateRounds(String tournamentID, List<String> users, int numPlayers, int numRounds)
            throws ExecutionException, InterruptedException {

        CollectionReference roundsCollection = firestore.collection("Tournaments")
                .document(tournamentID)
                .collection("Rounds");

        int matchCounter = 1; // Unique match IDs across all rounds
        List<Match> previousRoundMatches = null; // Track matches from the previous round

        for (int roundNumber = 1; roundNumber <= numRounds; roundNumber++) {
            int numMatches = numPlayers / 2;
            numPlayers /= 2; // Half the players move to the next round

            // Create matches for this round
            List<Match> matches = (roundNumber == 1)
                    ? createFirstRoundMatches(users, matchCounter) // Assign players to first-round matches
                    : createEmptyMatches(numMatches, roundNumber, matchCounter, previousRoundMatches);

            matchCounter += numMatches; // Update matchCounter for the next round

            // Store the round with matches
            Round round = new Round(roundNumber, matches);
            String roundId = String.valueOf(roundNumber);

            DocumentReference roundDocRef = roundsCollection.document(roundId);
            roundDocRef.set(round).get(); // Store the round

            log.info("Round {} created for tournament {} with {} matches.", roundNumber, tournamentID, numMatches);

            // Keep track of matches from the current round to link them in the next round
            previousRoundMatches = matches;
        }
    }

    // Create first-round matches with participants assigned
    private List<Match> createFirstRoundMatches(List<String> users, int startCounter) {
        List<Match> matches = new ArrayList<>();

        for (int i = 0; i < users.size(); i += 2) {
            int matchId = startCounter + (i / 2);

            // Assign participants to the match as Player 1 and Player 2
            List<ParticipantDTO> participants = new ArrayList<>();
            participants.add(new ParticipantDTO(
                    "1", // Player 1's ID in this match
                    users.get(i), // Player 1's name
                    "", // Result text placeholder
                    false // Not yet winner
            ));

            if (i + 1 < users.size()) {
                participants.add(new ParticipantDTO(
                        "2", // Player 2's ID in this match
                        users.get(i + 1), // Player 2's name
                        "", // Result text placeholder
                        false // Not yet winner
                ));
            }

            // Create the match with participants
            Match match = new Match(
                    matchId,
                    "Quarterfinal - Match " + matchId, // Match name
                    0, // Placeholder for nextMatchId
                    1, // Round number text
                    Timestamp.now(), // Start time
                    "PENDING", // Initial state
                    participants // Participants list
            );

            matches.add(match);
        }
        return matches;
    }

    // Create empty matches for subsequent rounds with nextMatchId linking
    private List<Match> createEmptyMatches(int numMatches, int roundNumber, int startCounter,
            List<Match> previousRoundMatches) {
        List<Match> matches = new ArrayList<>();

        for (int i = 0; i < numMatches; i++) {
            int matchId = startCounter + i;

            // Create the match
            Match match = new Match(
                    matchId,
                    "Match " + matchId, // Name of the match
                    0, // Placeholder for nextMatchId
                    roundNumber, // Round number text
                    Timestamp.now(), // Start time of the match
                    "PENDING", // Initial state of the match
                    new ArrayList<>() // No participants initially
            );

            matches.add(match);

            // Link two matches from the previous round to this match
            if (previousRoundMatches != null && i * 2 < previousRoundMatches.size()) {
                previousRoundMatches.get(i * 2).setNextMatchId(matchId);
                previousRoundMatches.get(i * 2 + 1).setNextMatchId(matchId);
            }
        }
        return matches;
    }

    // 1. Update the winner of a specific match
    public void updateMatchWinner(String tournamentID, int roundNumber, int matchId, String winnerName)
            throws ExecutionException, InterruptedException {

        // Get the match document from Firestore
        DocumentReference matchDocRef = firestore.collection("Tournaments")
                .document(tournamentID)
                .collection("Rounds")
                .document(String.valueOf(roundNumber))
                .collection("Matches")
                .document(String.valueOf(matchId));

        // Fetch the match document
        Match match = matchDocRef.get().get().toObject(Match.class);
        if (match == null) {
            throw new RuntimeException("Match not found: " + matchId);
        }

        // Update the winner within the match participants
        for (ParticipantDTO participant : match.getParticipants()) {
            participant.setWinner(participant.getName().equals(winnerName));
        }

        // Update the match document in Firestore
        matchDocRef.set(match).get();
        log.info("Updated winner for match {} in round {}: {}", matchId, roundNumber, winnerName);
    }

    // 2. Populate next round's matches with winners
    public void populateNextRoundMatches(String tournamentID, int currentRoundNumber)
            throws ExecutionException, InterruptedException {

        // Fetch all matches from the current round
        CollectionReference currentRoundMatches = firestore.collection("Tournaments")
                .document(tournamentID)
                .collection("Rounds")
                .document(String.valueOf(currentRoundNumber))
                .collection("Matches");

        List<Match> matches = currentRoundMatches.get().get().getDocuments().stream()
                .map(doc -> doc.toObject(Match.class))
                .toList();

        // Collect the winners from the current round
        List<String> winners = new ArrayList<>();
        for (Match match : matches) {
            match.getParticipants().stream()
                    .filter(ParticipantDTO::isWinner)
                    .findFirst()
                    .ifPresent(winner -> winners.add(winner.getName()));
        }

        // Ensure the next round exists
        int nextRoundNumber = currentRoundNumber + 1;
        CollectionReference nextRoundMatches = firestore.collection("Tournaments")
                .document(tournamentID)
                .collection("Rounds")
                .document(String.valueOf(nextRoundNumber))
                .collection("Matches");

        if (winners.size() < 2) {
            throw new RuntimeException("Not enough winners to proceed to the next round.");
        }

        // Populate the next round's matches with winners
        for (int i = 0; i < winners.size(); i += 2) {
            int nextMatchId = (i / 2) + 1;

            List<ParticipantDTO> participants = new ArrayList<>();
            participants.add(new ParticipantDTO("1", winners.get(i), "", false));

            if (i + 1 < winners.size()) {
                participants.add(new ParticipantDTO("2", winners.get(i + 1), "", false));
            }

            // Create and store the next round's match
            Match nextMatch = new Match(
                    nextMatchId,
                    "Match " + nextMatchId,
                    0, // Placeholder for nextMatchId
                    nextRoundNumber,
                    Timestamp.now(),
                    "PENDING",
                    participants);

            nextRoundMatches.document(String.valueOf(nextMatchId)).set(nextMatch).get();
            log.info("Populated next round {} with match {}", nextRoundNumber, nextMatchId);
        }
    }
}
