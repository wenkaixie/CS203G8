package com.csd.tournament.controller;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.csd.shared_library.DTO.MatchResultUpdateRequest;
import com.csd.shared_library.DTO.TournamentDTO;
import com.csd.shared_library.enumerator.TournamentType;
import com.csd.shared_library.model.Match;
import com.csd.shared_library.model.Tournament;
import com.csd.tournament.service.EliminationService;
import com.csd.tournament.service.RoundRobinService;
import com.csd.tournament.service.TournamentService;

import lombok.extern.slf4j.Slf4j;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/tournaments")
@Slf4j
public class TournamentController {

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private EliminationService eliminationService;

    @Autowired
    private RoundRobinService roundRobinService;

    @PostMapping
    public ResponseEntity<String> createTournament(@RequestBody TournamentDTO tournamentDTO) {

        try {
            String tournamentID = tournamentService.createTournament(tournamentDTO);
            return ResponseEntity.ok(tournamentID); // Return the tournament ID in the response body
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error when creating the tournament: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @DeleteMapping("/{tournamentID}")
    public ResponseEntity<Void> deleteTournament(@PathVariable String tournamentID) {
        try {
            tournamentService.deleteTournament(tournamentID);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // Return 204 No Content on success
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Return 500 Internal Server
                                                                                       // Error on failure
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Catching other unexpected
                                                                                       // errors
        }
    }

    @GetMapping("/{tournamentID}/admin")
    public ResponseEntity<String> getAdminIdForTournament(@PathVariable String tournamentID) {
        try {
            String adminId = tournamentService.getAdminIdForTournament(tournamentID);
            return ResponseEntity.ok(adminId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving admin ID: " + e.getMessage());
        }
    }

    @GetMapping("/{tournamentID}/userIDs")
    public ResponseEntity<List<String>> getUserIdsForTournament(@PathVariable String tournamentID) {
        try {
            List<String> userIds = tournamentService.getUserIdsForTournament(tournamentID);
            return ResponseEntity.ok(userIds);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // tested working 8 Nov
    @GetMapping("/{tournamentID}/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsersFromTournament(@PathVariable String tournamentID) {
        try {
            List<Map<String, Object>> users = tournamentService.getAllUsersFromTournament(tournamentID);
            return ResponseEntity.ok(users);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // tested working 8 Nov
    @PostMapping("/{tournamentID}/players")
    public ResponseEntity<String> addUserToTournament(@PathVariable String tournamentID,
            @RequestParam String userID) {
        try {
            String response = tournamentService.addUserToTournament(tournamentID, userID);
            return ResponseEntity.ok(response);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error adding player: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    // tested working 8 Nov
    @DeleteMapping("/{tournamentID}/players/{userID}")
    public ResponseEntity<String> removeUserFromTournament(
            @PathVariable String tournamentID,
            @PathVariable String userID) {
        try {
            String response = tournamentService.removeUserFromTournament(tournamentID, userID);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    // make sure it is used
    @PostMapping("/{tournamentID}/reinstate")
    public ResponseEntity<Void> reinstateTournament(@PathVariable String tournamentID) {
        try {
            tournamentService.reinstateTournament(tournamentID);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }





    // tested working 8 Nov
    @GetMapping("/{tournamentID}")
    public ResponseEntity<Tournament> getTournamentById(@PathVariable String tournamentID) {
        try {
            Tournament tournament = tournamentService.getTournamentById(tournamentID);
            return ResponseEntity.ok(tournament);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // tested working 8 Nov
    @GetMapping("/all")
    public ResponseEntity<List<Tournament>> getAllTournaments() {
        try {
            List<Tournament> tournaments = tournamentService.getAllTournaments();
            return ResponseEntity.ok(tournaments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

       // tested working 8 Nov
    @PutMapping("/{tournamentID}")
    public ResponseEntity<String> updateTournament(
            @PathVariable String tournamentID,
            @RequestBody TournamentDTO updatedTournament) {
        try {
            String response = tournamentService.updateTournament(tournamentID, updatedTournament);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // tested working 8 Nov
    // Generate rounds and matches for the tournament based on type
    @PostMapping("/{tournamentID}/generateRounds")
    public ResponseEntity<String> generateRoundsForTournament(@PathVariable String tournamentID) {
        try {
            // Retrieve the tournament type to determine which service to use
            Tournament tournament = tournamentService.getTournamentById(tournamentID);
            TournamentType tournamentType = tournament.getType();

            // Use switch to handle different tournament types
            switch (tournamentType) {
                case ROUND_ROBIN:
                    log.info("Generating rounds for round-robin tournament ID: {}", tournamentID);
                    roundRobinService.generateRoundsForTournament(tournamentID);
                    break;

                case ELIMINATION:
                    log.info("Generating rounds for elimination tournament ID: {}", tournamentID);
                    eliminationService.generateRoundsForTournament(tournamentID);
                    break;

                default:
                    log.error("Invalid tournament type specified: {}", tournamentType);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Invalid tournament type specified.");
            }

            return ResponseEntity.ok("Rounds and matches generated successfully.");

        } catch (ExecutionException | InterruptedException e) {
            log.error("Error generating rounds for tournament {}: {}", tournamentID, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating rounds: " + e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred while generating rounds for tournament {}: {}", tournamentID,
                    e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    // tested working 8 Nov
    // Batch update for multiple match results in the same round
    @PutMapping("/{tournamentID}/rounds/{roundNumber}/matches/results")
    public ResponseEntity<String> updateMatchResults(
            @PathVariable String tournamentID,
            @PathVariable int roundNumber,
            @RequestBody Map<Integer, MatchResultUpdateRequest> matchResults) {
        try {
            tournamentService.updateMatchResults(tournamentID, roundNumber, matchResults);
            return ResponseEntity.ok("Batch match results updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating batch match results: " + e.getMessage());
        }
    }

    // tested working 8 Nov
    // 3. Populate the next round's matches with winners
    @PostMapping("/{tournamentID}/rounds/{currentRoundNumber}/populateNextRound")
    public ResponseEntity<String> populateNextRoundMatches(
            @PathVariable String tournamentID,
            @PathVariable int currentRoundNumber) {
        try {
            // Retrieve the tournament type to determine which service to use
            Tournament tournament = tournamentService.getTournamentById(tournamentID);
            TournamentType tournamentType = tournament.getType();

            switch (tournamentType) {
                case ROUND_ROBIN:
                    log.info("Generating rounds for round-robin tournament ID: {}", tournamentID);
                    roundRobinService.updateNextRoundElos(tournamentID, currentRoundNumber);
                    break;

                case ELIMINATION:
                    log.info("Generating rounds for elimination tournament ID: {}", tournamentID);
                    eliminationService.populateNextRoundMatches(tournamentID, currentRoundNumber);
                    break;

                default:
                    log.error("Invalid tournament type specified: {}", tournamentType);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Invalid tournament type specified.");
            }
            return ResponseEntity.ok("Next rounds populated successfully.");

        } catch (ExecutionException | InterruptedException e) {
            log.error("Error generating rounds for tournament {}: {}", tournamentID, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error generating rounds: " + e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred while populating matches for next round for tournament {}: {}", tournamentID,
                    e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }

    }

    // // tested working 8 Nov
    // 4. Retrieve all matches from a specific tournament
    @GetMapping("/{tournamentID}/matches")
    public ResponseEntity<List<Match>> getAllMatchesFromTournament(@PathVariable String tournamentID) {
        try {
            List<Match> matches = tournamentService.getAllMatchesFromTournament(tournamentID);
            return ResponseEntity.ok(matches);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

}
