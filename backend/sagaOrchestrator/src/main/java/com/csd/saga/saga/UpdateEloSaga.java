package com.csd.saga.saga;

import java.util.HashMap;
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

import lombok.extern.slf4j.Slf4j;


@Component
@Slf4j
public class UpdateEloSaga {

    @Autowired
    private PlayerManagementServiceClient playerManagementServiceClient;

    @Autowired
    private TournamentServiceClient tournamentServiceClient;

    public ResponseEntity<String> updateElo(String tournamentID, int roundNumber,
            Map<Integer, MatchResultUpdateRequest> matchResults) {
        log.info("Starting Elo updates for tournament {} and round {}", tournamentID, roundNumber);

        try {
            // Validate the tournament
            ResponseEntity<Tournament> tournamentResponse = tournamentServiceClient.getTournament(tournamentID);
            if (!tournamentResponse.getStatusCode().is2xxSuccessful() || tournamentResponse.getBody() == null) {
                log.error("Tournament not found with ID: {}", tournamentID);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Tournament not found with ID: " + tournamentID);
            }
            log.info("Tournament {} validated successfully.", tournamentID);

            // Collect updates for users and tournaments
            Map<String, Integer> userEloUpdates = new HashMap<>();
            Map<String, Integer> tournamentEloUpdates = new HashMap<>();

            for (Map.Entry<Integer, MatchResultUpdateRequest> entry : matchResults.entrySet()) {
                Integer matchId = entry.getKey();
                MatchResultUpdateRequest resultRequest = entry.getValue();

                String player1Id = resultRequest.getPlayer1Id();
                String player2Id = resultRequest.getPlayer2Id();
                MatchResult matchResult = resultRequest.getMatchResult();

                try {
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

                    int newElo1 = calculateNewElo(elo1, elo2, AS1);
                    int newElo2 = calculateNewElo(elo2, elo1, AS2);

                    // Add updates to batch maps
                    userEloUpdates.put(player1Id, newElo1);
                    userEloUpdates.put(player2Id, newElo2);
                    tournamentEloUpdates.put(player1Id, newElo1);
                    tournamentEloUpdates.put(player2Id, newElo2);

                } catch (Exception e) {
                    log.error("Error processing match {} for players {} and {}: {}",
                            matchId, player1Id, player2Id, e.getMessage(), e);
                }
            }

            // Batch updates via microservices
            playerManagementServiceClient.updatePlayerEloBatch(userEloUpdates);
            tournamentServiceClient.updateTournamentPlayerEloBatch(tournamentID, tournamentEloUpdates);

            log.info("Elo updates successfully completed for tournament {} and round {}", tournamentID, roundNumber);
            return ResponseEntity.ok("Elo ratings processed successfully for provided pairs.");

        } catch (Exception e) {
            log.error("Error updating Elo ratings: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server Error: An error occurred while updating Elo ratings.");
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

    private int calculateNewElo(int elo, int opponentElo, double actualScore) {
        double expectedScore = calculateExpectedScore(elo, opponentElo);
        int kFactor = determineKFactor(elo);
        return Math.max(0, (int) Math.round(elo + kFactor * (actualScore - expectedScore)));
    }

    private double calculateExpectedScore(int eloPlayer, int eloOpponent) {
        return 1 / (1 + Math.pow(10, (eloOpponent - eloPlayer) / 400.0));
    }

    private int determineKFactor(int elo) {
        if (elo < 2100) {
            return 32;
        } else if (elo > 2400) {
            return 16;
        } else {
            return 24;
        }
    }
}
