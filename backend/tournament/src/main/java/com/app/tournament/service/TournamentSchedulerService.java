package com.app.tournament.service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.tournament.enumerator.TournamentType;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TournamentSchedulerService implements DisposableBean {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    @Autowired
    private Firestore firestore;

    @Autowired
    private EliminationService eliminationService;

    @Autowired
    private RoundRobinService roundRobinService;

    public void scheduleTournamentRoundGeneration(String tid, Instant startDatetime, TournamentType tournamentType) {
        Instant now = Instant.now();

        // Calculate delay (24 hours before tournament start time)
        long delayInSeconds = Duration.between(now, startDatetime.minus(Duration.ofHours(24))).getSeconds();

        if (delayInSeconds > 0) {
            scheduler.schedule(() -> {
                try {
                    captureEloSnapshot(tid);

                    // Use the appropriate service based on tournament type
                    if (tournamentType == TournamentType.Round_Robin) {
                        roundRobinService.generateRoundsForTournament(tid);
                    } else if (tournamentType == TournamentType.Elimination) {
                        eliminationService.generateRoundsForTournament(tid);
                    }
                    log.info("Rounds generated for tournament: {}", tid);
                } catch (Exception e) {
                    log.error("Error generating rounds for tournament: {}", tid, e);
                }
            }, delayInSeconds, TimeUnit.SECONDS);
        } else {
            log.info("The tournament {} is starting within 24 hours or has already started.", tid);
        }
    }

    public void captureEloSnapshot(String tournamentID) throws ExecutionException, InterruptedException {
        log.info("Capturing Elo snapshot for tournament: {}", tournamentID);

        // Get the users subcollection for the specific tournament
        CollectionReference usersCollection = firestore.collection("Tournaments").document(tournamentID)
                .collection("Users");

        // Prepare to store Elo snapshots
        Map<String, Integer> eloSnapshot = new HashMap<>();
        List<QueryDocumentSnapshot> userDocs = usersCollection.get().get().getDocuments();

        for (QueryDocumentSnapshot userDoc : userDocs) {
            String userId = userDoc.getId();
            Long elo = userDoc.getLong("elo");
            if (elo != null) {
                eloSnapshot.put(userId, elo.intValue());
            } else {
                log.warn("Elo rating missing for user: {}", userId);
            }
        }

        // Store the Elo snapshot in Firestore under the tournament document
        DocumentReference tournamentDoc = firestore.collection("Tournaments").document(tournamentID);
        tournamentDoc.update("eloSnapshot", eloSnapshot).get();

        log.info("Elo snapshot captured successfully for tournament: {}", tournamentID);
    }

    // Properly shut down the scheduler when the Spring application terminates
    @Override
    public void destroy() throws Exception {
        log.info("Shutting down TournamentSchedulerService...");
        scheduler.shutdown();
        if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
            log.warn("Scheduler did not terminate in the specified time.");
            scheduler.shutdownNow();
        }
        log.info("TournamentSchedulerService shut down successfully.");
    }
}
