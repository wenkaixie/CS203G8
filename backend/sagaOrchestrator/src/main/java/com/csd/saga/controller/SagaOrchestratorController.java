package com.csd.saga.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.csd.saga.service.SagaOrchestratorService;
import com.csd.shared_library.DTO.MatchResultUpdateRequest;
import com.csd.shared_library.DTO.TournamentDTO;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/saga")
@Slf4j
public class SagaOrchestratorController {

    @Autowired
    private SagaOrchestratorService sagaOrchestratorService;

    @PostMapping("/tournaments")
    public ResponseEntity<String> createTournament(@RequestBody TournamentDTO tournamentDTO) {
        log.info("Received request to create tournament for admin: {}", tournamentDTO.getAdminId());
        try {
            String tournamentId = sagaOrchestratorService.startTournamentCreationSaga(tournamentDTO);
            return ResponseEntity.status(201)
                    .body("Tournament created successfully with ID: " + tournamentId);
        } catch (RuntimeException e) {
            log.error("Error during tournament creation saga: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("Tournament creation saga failed: " + e.getMessage());
        }
    }

    @DeleteMapping("/tournaments/{tournamentID}")
    public ResponseEntity<String> deleteTournament(@PathVariable String tournamentID) {
        log.info("Received request to delete tournament: {}", tournamentID);
        try {
            sagaOrchestratorService.startTournamentDeletionSaga(tournamentID);
            return ResponseEntity.ok("Tournament deletion saga started successfully.");
        } catch (Exception e) {
            log.error("Error initiating tournament deletion saga: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("Error initiating tournament deletion saga: " + e.getMessage());
        }
    }
    
    
    @PostMapping("/tournaments/{tournamentID}/players")
    public ResponseEntity<String> addUserToTournament(@PathVariable String tournamentID,
            @RequestParam String userID) {
        log.info("Received request to add user {} to tournament {}", userID, tournamentID);
        try {
            sagaOrchestratorService.startAddUserToTournamentSaga(tournamentID, userID);
            return ResponseEntity.ok("Add user to tournament saga started successfully.");
        } catch (Exception e) {
            log.error("Error initiating add user to tournament saga: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("Error initiating add user to tournament saga: " + e.getMessage());
        }
    }

    @DeleteMapping("/tournaments/{tournamentID}/players/{userID}")
    public ResponseEntity<String> removeUserFromTournament(@PathVariable String tournamentID,
            @PathVariable String userID) {
        log.info("Received request to remove user {} from tournament {}", userID, tournamentID);
        try {
            sagaOrchestratorService.startRemoveUserFromTournamentSaga(tournamentID, userID);
            return ResponseEntity.ok("Remove user from tournament saga started successfully.");
        } catch (Exception e) {
            log.error("Error initiating remove user from tournament saga: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("Error initiating remove user from tournament saga: " + e.getMessage());
        }
    }

    @PostMapping("/tournaments/{tournamentID}/rounds/{roundNumber}/elo")
    public ResponseEntity<String> updateElo(
            @PathVariable String tournamentID,
            @PathVariable int roundNumber,
            @RequestBody Map<Integer, MatchResultUpdateRequest> matchResults) {
        log.info("Received request to udpate round "  + roundNumber);
        try {
            return sagaOrchestratorService.startUpdateEloSaga(tournamentID, roundNumber, matchResults);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error initiating Elo update saga: " + e.getMessage());
        }
    }

    // @GetMapping("/tournaments/upcoming/{userID}")
    // public ResponseEntity<List<Tournament>> getUpcomingTournamentsOfUser(@PathVariable String userID) {
    //     log.info("Received request to fetch upcoming tournaments for user {}", userID);
    //     try {
    //         List<Tournament> tournaments = sagaOrchestratorService.getUpcomingTournamentsOfUser(userID);
    //         return ResponseEntity.ok(tournaments);
    //     } catch (Exception e) {
    //         log.error("Error fetching upcoming tournaments for user {}: {}", userID, e.getMessage());
    //         return ResponseEntity.internalServerError().build();
    //     }
    // }

    // @GetMapping("/tournaments/past/{userID}")
    // public ResponseEntity<List<Tournament>> getPastTournamentsOfUser(@PathVariable String userID) {
    //     log.info("Received request to fetch past tournaments for user {}", userID);
    //     try {
    //         List<Tournament> tournaments = sagaOrchestratorService.getPastTournamentsOfUser(userID);
    //         return ResponseEntity.ok(tournaments);
    //     } catch (Exception e) {
    //         log.error("Error fetching past tournaments for user {}: {}", userID, e.getMessage());
    //         return ResponseEntity.internalServerError().build();
    //     }
    // }

    // @GetMapping("/tournaments/ongoing/{userID}")
    // public ResponseEntity<List<Tournament>> getOngoingTournamentsOfUser(@PathVariable String userID) {
    //     log.info("Received request to fetch ongoing tournaments for user {}", userID);
    //     try {
    //         List<Tournament> tournaments = sagaOrchestratorService.getOngoingTournamentsOfUser(userID);
    //         return ResponseEntity.ok(tournaments);
    //     } catch (Exception e) {
    //         log.error("Error fetching ongoing tournaments for user {}: {}", userID, e.getMessage());
    //         return ResponseEntity.internalServerError().build();
    //     }
    // }

    // @GetMapping("/tournaments/eligible/{userID}")
    // public ResponseEntity<List<Tournament>> getEligibleTournamentsOfUser(@PathVariable String userID) {
    //     log.info("Received request to fetch eligible tournaments for user {}", userID);
    //     try {
    //         List<Tournament> tournaments = sagaOrchestratorService.getEligibleTournamentsOfUser(userID);
    //         return ResponseEntity.ok(tournaments);
    //     } catch (Exception e) {
    //         log.error("Error fetching eligible tournaments for user {}: {}", userID, e.getMessage());
    //         return ResponseEntity.internalServerError().build();
    //     }
    // }
}
