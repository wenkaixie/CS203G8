package com.app.tournament.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.app.tournament.DTO.ParticipantDTO;
import com.app.tournament.events.TournamentClosedEvent;
import com.app.tournament.model.Match;
import com.app.tournament.model.Round;
import com.app.tournament.model.Tournament;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EliminationService {

    @Autowired
    private Firestore firestore;

    // @Autowired
    // private TournamentService tournamentService;

    @Autowired
    private UserService userService;

    @EventListener
    public void handleTournamentClosedEvent(TournamentClosedEvent event) {
        String tournamentId = event.getTournamentId();
        log.info("Handling closed tournament with ID: {}", tournamentId);

        try {
            generateRoundsForTournament(tournamentId);
        } catch (Exception e) {
            log.error("Error generating rounds for tournament {}: {}", tournamentId, e.getMessage());
        }
    }

    // Generate rounds and matches for the tournament
    public void generateRoundsForTournament(String tournamentID) throws ExecutionException, InterruptedException {
        try {
            Tournament tournament = getTournamentById(tournamentID);
            List<String> users = tournament.getUsers();

            List<String> names = userService.getUserNamesByIds(users);

            int numPlayers = users.size();
            int numRounds = calculateEliminationRounds(numPlayers);

            generateRounds(tournamentID, names, numPlayers, numRounds);
        } catch (Exception e) {
            log.error("Failed to generate rounds for tournament {}: {}", tournamentID, e.getMessage());
            throw e;
        }
    }

    private int calculateEliminationRounds(int numPlayers) {
        return (int) Math.ceil(Math.log(numPlayers) / Math.log(2));
    }

    private void generateRounds(String tournamentID, List<String> users, int numPlayers, int numRounds)
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
                    ? createFirstRoundMatches(users, matchCounter)
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

    private List<Match> createFirstRoundMatches(List<String> users, int startCounter) {
        List<Match> matches = new ArrayList<>();
    
        for (int i = 0; i < users.size(); i += 2) {
            int matchId = startCounter + (i / 2);
    
            // Correct nextMatchId calculation: Group every two matches (i / 4 to group matches in pairs of 2)
            int nextMatchId = startCounter + (users.size() / 2) + ((i / 2) / 2);
    
            List<ParticipantDTO> participants = new ArrayList<>();
            participants.add(new ParticipantDTO("1", users.get(i), "W", false));
    
            if (i + 1 < users.size()) {
                participants.add(new ParticipantDTO("2", users.get(i + 1), "B", false));
            }
    
            Match match = new Match(
                    matchId,
                    "Match " + matchId,
                    nextMatchId,
                    1,
                    Instant.now(),
                    "PENDING",
                    participants
            );
    
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

    public void populateNextRoundMatches(String tournamentID, int currentRoundNumber)
            throws ExecutionException, InterruptedException {

        log.info("Populating next round matches for tournament {} from round {}.", tournamentID, currentRoundNumber);

        // Fetch the current round's document
        DocumentReference currentRoundDocRef = firestore.collection("Tournaments")
                .document(tournamentID)
                .collection("Rounds")
                .document(String.valueOf(currentRoundNumber));

        Round currentRound = currentRoundDocRef.get().get().toObject(Round.class);
        if (currentRound == null) {
            throw new RuntimeException("Current round not found.");
        }

        // Fetch the next round's document
        int nextRoundNumber = currentRoundNumber + 1;
        DocumentReference nextRoundDocRef = firestore.collection("Tournaments")
                .document(tournamentID)
                .collection("Rounds")
                .document(String.valueOf(nextRoundNumber));

        Round nextRound = nextRoundDocRef.get().get().toObject(Round.class);
        if (nextRound == null) {
            throw new RuntimeException("Next round not found.");
        }

        List<Match> nextRoundMatches = new ArrayList<>();

        // Iterate over each match in the next round
        for (int i = 0; i < nextRound.getMatches().size(); i++) {
            Match nextMatch = nextRound.getMatches().get(i);
            int matchId = nextMatch.getId();

            // Find the two matches from the current round where nextMatchId matches the
            // matchId of the next round match
            List<Match> parentMatches = new ArrayList<>();
            for (Match currentMatch : currentRound.getMatches()) {
                if (currentMatch.getNextMatchId() == matchId) {
                    parentMatches.add(currentMatch);
                }
            }

            if (parentMatches.size() != 2) {
                log.warn("Match {} in round {} has incorrect number of parent matches: {}.", matchId, nextRoundNumber,
                        parentMatches.size());
                continue;
            }

            // Extract winners from the parent matches
            List<ParticipantDTO> participants = new ArrayList<>();
            for (Match parentMatch : parentMatches) {
                parentMatch.getParticipants().stream()
                        .filter(ParticipantDTO::getIsWinner)
                        .findFirst()
                        .ifPresent(winner -> participants.add(new ParticipantDTO(
                                String.valueOf(participants.size() + 1),
                                winner.getName(),
                                "",
                                false)));
            }

            if (participants.size() < 2) {
                log.warn("Not enough winners found for match {} in round {}.", matchId, nextRoundNumber);
                continue;
            }

            // Create a new match with the participants
            Match updatedMatch = new Match(
                    matchId,
                    nextMatch.getName(),
                    nextMatch.getNextMatchId(),
                    nextRoundNumber,
                    Instant.now(),
                    "PENDING",
                    participants);

            nextRoundMatches.add(updatedMatch);

            log.info("Updated match {} for round {} with participants: {}",
                    matchId, nextRoundNumber, participants.stream().map(ParticipantDTO::getName).toList());
        }

        // Save the updated next round matches to Firestore
        nextRound.setMatches(nextRoundMatches);
        nextRoundDocRef.set(nextRound).get();

        log.info("Successfully populated round {} with {} matches.", nextRoundNumber, nextRoundMatches.size());

        // Increment and update currentRoundNumber in the tournament document
        DocumentReference tournamentDocRef = firestore.collection("Tournaments").document(tournamentID);
        tournamentDocRef.update("currentRound", currentRoundNumber + 1).get(); // Update currentRound directly

        log.info("Updated tournament {} current round to {}.", tournamentID, currentRoundNumber + 1);
    }

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

}
