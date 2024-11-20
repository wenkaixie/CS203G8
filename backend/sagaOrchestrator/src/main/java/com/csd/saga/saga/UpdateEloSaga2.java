package com.csd.saga.saga;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.csd.saga.clientInterface.PlayerManagementServiceClient;
import com.csd.saga.clientInterface.TournamentServiceClient;
import com.csd.shared_library.DTO.MatchResultUpdateRequest;
import com.csd.shared_library.enumerator.MatchResult;
import com.csd.shared_library.model.Tournament;
import com.csd.shared_library.model.User;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteBatch;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UpdateEloSaga2 {

    @Autowired
    private Firestore firestore;

    @Autowired
    private TournamentServiceClient tournamentServiceClient;

    @Autowired
    private PlayerManagementServiceClient playerManagementServiceClient;

    public ResponseEntity<String> updateElo(String tournamentID, int roundNumber,
            Map<Integer, MatchResultUpdateRequest> matchResults) {
        log.info("Starting Elo updates for tournament {} and round {}", tournamentID, roundNumber);

        try {
            // Step 1: Validate the tournament
            ResponseEntity<Tournament> tournamentResponse = tournamentServiceClient.getTournament(tournamentID);
            if (!tournamentResponse.getStatusCode().is2xxSuccessful() || tournamentResponse.getBody() == null) {
                log.error("Tournament not found with ID: {}", tournamentID);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Tournament not found with ID: " + tournamentID);
            }
            log.info("Tournament {} validated successfully.", tournamentID);

            // Step 2: Process match results in batch
            WriteBatch batch = firestore.batch();
            processMatchResults(matchResults, tournamentID, batch);

            // Step 3: Commit batch updates
            batch.commit().get();
            log.info("Batch Elo updates successfully committed.");
            return ResponseEntity.ok("Elo ratings processed successfully for provided pairs.");

        } catch (Exception e) {
            log.error("Error updating Elo ratings: {}", e.getMessage(), e);
            // Consider adding rollback logic if required
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server Error: An error occurred while updating Elo ratings.");
        }
    }

    private void processMatchResults(Map<Integer, MatchResultUpdateRequest> matchResults,
            String tournamentID, WriteBatch batch) {
        for (Map.Entry<Integer, MatchResultUpdateRequest> entry : matchResults.entrySet()) {
            Integer matchId = entry.getKey();
            MatchResultUpdateRequest resultRequest = entry.getValue();

            String player1Id = resultRequest.getPlayer1Id();
            String player2Id = resultRequest.getPlayer2Id();
            MatchResult matchResult = resultRequest.getMatchResult();

            try {
                // Fetch players from the Player Management Service
                User user1 = fetchUser(player1Id);
                User user2 = fetchUser(player2Id);

                int elo1 = user1.getElo();
                int elo2 = user2.getElo();

                Double AS1, AS2;
                switch (matchResult) {
                    case PLAYER1_WIN -> {
                        AS1 = 1.0;
                        AS2 = 0.0;
                    }
                    case PLAYER2_WIN -> {
                        AS1 = 0.0;
                        AS2 = 1.0;
                    }
                    case DRAW -> {
                        AS1 = 0.5;
                        AS2 = 0.5;
                    }
                    default -> {
                        log.warn("Invalid match result for match {}: {}", matchId, matchResult);
                        continue;
                    }
                }

                // Prepare the batch updates for Elo calculation
                updateEloInBatch(batch, tournamentID, player1Id, player2Id, elo1, elo2, AS1, AS2);

            } catch (Exception e) {
                log.error("Error processing match {} for players {} and {}: {}",
                        matchId, player1Id, player2Id, e.getMessage(), e);
                // Skip this match and continue with others
            }
        }
    }

    private User fetchUser(String playerId) {
        ResponseEntity<User> userResponse = playerManagementServiceClient.getUser(playerId);
        if (!userResponse.getStatusCode().is2xxSuccessful() || userResponse.getBody() == null) {
            log.error("Player {} not found.", playerId);
            throw new RuntimeException("Player not found with ID: " + playerId);
        }
        return userResponse.getBody();
    }

    private void updateEloInBatch(WriteBatch batch, String tournamentID, String player1Id, String player2Id,
            double elo1, double elo2, double AS1, double AS2) {
        double ES1 = calculateExpectedScore(elo1, elo2);
        double ES2 = calculateExpectedScore(elo2, elo1);

        int K1 = determineKFactor(elo1);
        int K2 = determineKFactor(elo2);

        double newElo1 = Math.max(0, Math.round(elo1 + K1 * (AS1 - ES1)));
        double newElo2 = Math.max(0, Math.round(elo2 + K2 * (AS2 - ES2)));

        // Update Elo in the main Users collection
        updateEloInUserCollection(batch, player1Id, player2Id, newElo1, newElo2);

        // Update Elo in the Users subcollection within the Tournament collection
        updateEloInTournamentUserSubcollection(batch, tournamentID, player1Id, player2Id, newElo1, newElo2);

        log.info("Prepared batch update for players {} and {}: newElo1={}, newElo2={}", player1Id, player2Id, newElo1,
                newElo2);
    }

    private void updateEloInUserCollection(WriteBatch batch, String player1Id, String player2Id,
            double newElo1, double newElo2) {
        DocumentReference player1Ref = firestore.collection("Users").document(player1Id);
        DocumentReference player2Ref = firestore.collection("Users").document(player2Id);

        batch.update(player1Ref, "elo", newElo1);
        batch.update(player2Ref, "elo", newElo2);

        log.info("Added batch updates for Users collection: player1={}, player2={}", player1Id, player2Id);
    }

    private void updateEloInTournamentUserSubcollection(WriteBatch batch, String tournamentID,
            String player1Id, String player2Id,
            double newElo1, double newElo2) {
        DocumentReference tournamentPlayer1Ref = firestore.collection("Tournaments").document(tournamentID)
                .collection("Users").document(player1Id);
        DocumentReference tournamentPlayer2Ref = firestore.collection("Tournaments").document(tournamentID)
                .collection("Users").document(player2Id);

        batch.update(tournamentPlayer1Ref, "elo", newElo1);
        batch.update(tournamentPlayer2Ref, "elo", newElo2);

        log.info("Added batch updates for Tournament's Users subcollection: tournamentID={}, player1={}, player2={}",
                tournamentID, player1Id, player2Id);
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
}


