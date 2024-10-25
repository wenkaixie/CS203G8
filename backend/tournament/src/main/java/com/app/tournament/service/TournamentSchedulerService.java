package com.app.tournament.service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TournamentSchedulerService implements DisposableBean {

    // can increase thead pool size as needed
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    public void scheduleTournamentRoundGeneration(String tid, Instant startDatetime,
            EliminationService eliminationService) {
        Instant now = Instant.now();

        // Calculate the delay (24 hours before tournament start time)
        long delayInSeconds = Duration.between(now, startDatetime.minus(Duration.ofHours(24))).getSeconds();

        if (delayInSeconds > 0) {
            scheduler.schedule(() -> {
                try {
                    eliminationService.generateRoundsForTournament(tid);
                    log.info("Rounds generated for tournament: {}", tid);
                } catch (Exception e) {
                    log.error("Error generating rounds for tournament: {}", tid, e);
                }
            }, delayInSeconds, TimeUnit.SECONDS);
        } else {
            log.info("The tournament {} is starting within 24 hours or has already started.", tid);
        }
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
