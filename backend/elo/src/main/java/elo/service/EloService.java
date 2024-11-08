package elo.service;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.app.tournament.DTO.MatchResult;
import com.app.tournament.DTO.MatchResultUpdateRequest;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteBatch;

@Service
public class EloService {

    @Autowired
    private Firestore firestore;

    private static final Logger logger = LoggerFactory.getLogger(EloService.class);

    public ResponseEntity<String> updateElo(String tournamentID, int roundNumber,
            Map<Integer, MatchResultUpdateRequest> matchResults)
            throws ExecutionException, InterruptedException {

        DocumentSnapshot tournamentSnapshot;
        try {
            tournamentSnapshot = firestore.collection("Tournaments").document(tournamentID).get().get();
            if (!tournamentSnapshot.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tournament does not exist in Firestore.");
            }
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error retrieving tournament with ID {}: {}", tournamentID, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server Error: Unable to retrieve tournament.");
        }

        WriteBatch batch = firestore.batch();
        for (Map.Entry<Integer, MatchResultUpdateRequest> entry : matchResults.entrySet()) {
            Integer matchId = entry.getKey();
            MatchResultUpdateRequest resultRequest = entry.getValue();

            String player1Id = resultRequest.getPlayer1Id();
            String player2Id = resultRequest.getPlayer2Id();
            MatchResult matchResult = resultRequest.getMatchResult();

            try {
                DocumentSnapshot player1Snapshot = firestore.collection("Users").document(player1Id).get().get();
                DocumentSnapshot player2Snapshot = firestore.collection("Users").document(player2Id).get().get();

                if (!player1Snapshot.exists() || !player2Snapshot.exists()) {
                    logger.warn("One or both players do not exist: player1Id={}, player2Id={}", player1Id, player2Id);
                    continue;
                }

                Double elo1 = player1Snapshot.getDouble("elo");
                Double elo2 = player2Snapshot.getDouble("elo");

                if (elo1 == null || elo2 == null) {
                    logger.warn("One or both players have null Elo ratings: player1Id={}, player2Id={}", player1Id,
                            player2Id);
                    continue;
                }

                Double AS1, AS2;
                switch (matchResult) {
                    case PLAYER1_WIN:
                        AS1 = 1.0;
                        AS2 = 0.0;
                        break;
                    case PLAYER2_WIN:
                        AS1 = 0.0;
                        AS2 = 1.0;
                        break;
                    case DRAW:
                        AS1 = 0.5;
                        AS2 = 0.5;
                        break;
                    default:
                        logger.warn("Invalid match result for match {}: {}", matchId, matchResult);
                        continue;
                }

                updateEloInBatch(batch, tournamentID, player1Id, player2Id, elo1, elo2, AS1, AS2);

            } catch (ExecutionException | InterruptedException e) {
                logger.error("Error processing Elo for players {} and {}: {}", player1Id, player2Id, e.getMessage());
                continue;
            }
        }

        try {
            batch.commit().get();
            logger.info("Batch Elo updates successfully committed.");
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error committing batch updates: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server Error: Unable to commit batch updates.");
        }

        return ResponseEntity.ok("Elo ratings processed successfully for provided pairs.");
    }

    private void updateEloInBatch(WriteBatch batch, String tournamentID, String player1Id, String player2Id,
            double elo1, double elo2, double AS1, double AS2) {
        double ES1 = calculateExpectedScore(elo1, elo2);
        double ES2 = calculateExpectedScore(elo2, elo1);

        int K1 = determineKFactor(elo1);
        int K2 = determineKFactor(elo2);

        double newElo1 = Math.max(0, Math.round(elo1 + K1 * (AS1 - ES1)));
        double newElo2 = Math.max(0, Math.round(elo2 + K2 * (AS2 - ES2)));

        DocumentReference player1Ref = firestore.collection("Users").document(player1Id);
        DocumentReference player2Ref = firestore.collection("Users").document(player2Id);
        DocumentReference tournamentPlayer1Ref = firestore.collection("Tournaments").document(tournamentID)
                .collection("Users").document(player1Id);
        DocumentReference tournamentPlayer2Ref = firestore.collection("Tournaments").document(tournamentID)
                .collection("Users").document(player2Id);

        batch.update(player1Ref, "elo", newElo1);
        batch.update(player2Ref, "elo", newElo2);
        batch.update(tournamentPlayer1Ref, "elo", newElo1);
        batch.update(tournamentPlayer2Ref, "elo", newElo2);

        logger.info("Prepared batch update for players {} and {}: newElo1={}, newElo2={}", player1Id, player2Id,
                newElo1, newElo2);
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
