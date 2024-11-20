// package com.csd.saga.saga;

// import java.util.Map;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.stereotype.Component;

// import com.csd.shared_library.DTO.MatchResultUpdateRequest;
// import com.csd.shared_library.enumerator.MatchResult;

// import lombok.extern.slf4j.Slf4j;

// @Component
// @Slf4j
// public class UpdateEloSaga {
    
//     public ResponseEntity<String> updateElo(String tournamentID, int roundNumber,
//             Map<Integer, MatchResultUpdateRequest> matchResults) {
//         log.info("Starting Elo updates for tournament {} and round {}", tournamentID, roundNumber);

//         try {
//             // Step 1: Validate tournament existence
//             ResponseEntity<Boolean> tournamentExists = tournamentServiceClient.checkTournamentExists(tournamentID);
//             if (!Boolean.TRUE.equals(tournamentExists.getBody())) {
//                 log.warn("Tournament {} does not exist.", tournamentID);
//                 return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tournament does not exist.");
//             }

//             // Step 2: Process match results
//             for (Map.Entry<Integer, MatchResultUpdateRequest> entry : matchResults.entrySet()) {
//                 Integer matchId = entry.getKey();
//                 MatchResultUpdateRequest resultRequest = entry.getValue();

//                 String player1Id = resultRequest.getPlayer1Id();
//                 String player2Id = resultRequest.getPlayer2Id();
//                 MatchResult matchResult = resultRequest.getMatchResult();

//                 try {
//                     // Validate player existence
//                     if (!playerServiceClient.checkPlayerExists(player1Id)
//                             || !playerServiceClient.checkPlayerExists(player2Id)) {
//                         log.warn("One or both players do not exist: player1Id={}, player2Id={}", player1Id,
//                                 player2Id);
//                         continue;
//                     }

//                     // Fetch Elo ratings
//                     int elo1 = playerServiceClient.getPlayerElo(player1Id);
//                     int elo2 = playerServiceClient.getPlayerElo(player2Id);

//                     // Calculate new Elo ratings
//                     int[] updatedElos = calculateElo(elo1, elo2, matchResult);

//                     // Update Elo ratings in User and Tournament contexts
//                     playerServiceClient.updatePlayerElo(player1Id, updatedElos[0]);
//                     playerServiceClient.updatePlayerElo(player2Id, updatedElos[1]);
//                     tournamentServiceClient.updateTournamentPlayerElo(tournamentID, player1Id, updatedElos[0]);
//                     tournamentServiceClient.updateTournamentPlayerElo(tournamentID, player2Id, updatedElos[1]);

//                     log.info(
//                             "Successfully updated Elo for match {} in tournament {}: player1Id={}, newElo1={}, player2Id={}, newElo2={}",
//                             matchId, tournamentID, player1Id, updatedElos[0], player2Id, updatedElos[1]);

//                 } catch (Exception e) {
//                     log.error("Error processing match {} in tournament {}: {}", matchId, tournamentID,
//                             e.getMessage());
//                     // Rollback can be initiated here if necessary
//                 }
//             }

//             return ResponseEntity.ok("Elo ratings processed successfully for all matches.");

//         } catch (Exception e) {
//             log.error("Error updating Elo for tournament {}: {}", tournamentID, e.getMessage());
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                     .body("Internal Server Error: Unable to update Elo.");
//         }
//     }


// }

