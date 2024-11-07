package com.app.tournament.controller;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.app.tournament.DTO.TournamentDTO;
import com.app.tournament.enumerator.MatchResult;
import com.app.tournament.enumerator.TournamentType;
import com.app.tournament.model.Match;
import com.app.tournament.model.Tournament;
import com.app.tournament.service.EliminationService;
import com.app.tournament.service.RoundRobinService;
import com.app.tournament.service.TournamentService;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private EliminationService eliminationService;

    @Autowired
    private RoundRobinService roundRobinService;

    private static final Logger logger = LoggerFactory.getLogger(TournamentService.class);

    // Create tournament endpoint
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

    // checked wenkai 5/11
    @GetMapping("/{tournamentID}")
    public ResponseEntity<Tournament> getTournamentById(@PathVariable String tournamentID) {
        try {
            Tournament tournament = tournamentService.getTournamentById(tournamentID);
            return ResponseEntity.ok(tournament);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // checked wenkai 5/11
    @GetMapping("/all")
    public ResponseEntity<List<Tournament>> getAllTournaments() {
        try {
            List<Tournament> tournaments = tournamentService.getAllTournaments();
            return ResponseEntity.ok(tournaments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // checked wenkai 5/11
    @GetMapping("/user/{userID}")
    public ResponseEntity<List<Tournament>> getAllTournamentsOfUser(@PathVariable String userID) {
        try {
            List<Tournament> tournaments = tournamentService.getTournamentsOfUser(userID);
            return ResponseEntity.ok(tournaments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

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

    // @GetMapping("/search")
    // public ResponseEntity<List<Tournament>>
    // getTournamentsByLocation(@RequestParam String location) {
    // try {
    // List<Tournament> tournaments =
    // tournamentService.getTournamentsByLocation(location);
    // return ResponseEntity.ok(tournaments);
    // } catch (Exception e) {
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    // }
    // }

    // @GetMapping("/paginated")
    // public ResponseEntity<List<Tournament>> getTournamentsWithPagination(
    // @RequestParam int limit,
    // @RequestParam(required = false) String lastTournamentID) {
    // try {
    // List<Tournament> tournaments =
    // tournamentService.getTournamentsWithPagination(limit, lastTournamentID);
    // return ResponseEntity.ok(tournaments);
    // } catch (Exception e) {
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    // }
    // }

    // checked wenkai 5/11
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

    // checked wenkai 5/11
    // Get upcoming tournaments
    @GetMapping("/upcoming/{userID}")
    public ResponseEntity<List<Tournament>>getUpcomingTournamentsOfUser(@PathVariable String userID) {
        try {
            List<Tournament> tournaments =tournamentService.getUpcomingTournamentsOfUser(userID);
            return ResponseEntity.ok(tournaments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // checked wenkai 5/11
    // Get past tournaments
    @GetMapping("/past/{userID}")
    public ResponseEntity<List<Tournament>>getPastTournamentsOfUser(@PathVariable String userID) {
        try {
            List<Tournament> tournaments =tournamentService.getPastTournamentsOfUser(userID);
            return ResponseEntity.ok(tournaments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // checked wenkai 5/11    
    // Get ongoing tournaments of user
    @GetMapping("/ongoing/{userID}")
    public ResponseEntity<List<Tournament>>getOngoingTournamentsOfUser(@PathVariable String userID) {
        try {
            List<Tournament> tournaments =tournamentService.getOngoingTournamentsOfUser(userID);
            return ResponseEntity.ok(tournaments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Get eligible tournaments of user
    @GetMapping("/eligible/{userID}")
    public ResponseEntity<List<Tournament>> getEligibleTournamentsOfUser(@PathVariable String userID) {
        try {
            List<Tournament> tournaments = tournamentService.getEligibleTournamentsOfUser(userID);
            return ResponseEntity.ok(tournaments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

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
                    logger.info("Generating rounds for round-robin tournament ID: {}", tournamentID);
                    roundRobinService.generateRoundsForTournament(tournamentID);
                    break;

                case ELIMINATION:
                    logger.info("Generating rounds for elimination tournament ID: {}", tournamentID);
                    eliminationService.generateRoundsForTournament(tournamentID);
                    break;

                default:
                    logger.error("Invalid tournament type specified: {}", tournamentType);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Invalid tournament type specified.");
            }

            return ResponseEntity.ok("Rounds and matches generated successfully.");

        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error generating rounds for tournament {}: {}", tournamentID, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating rounds: " + e.getMessage());
        } catch (Exception e) {
            logger.error("An unexpected error occurred while generating rounds for tournament {}: {}", tournamentID,
                    e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    // Batch update for multiple match results in the same round
    @PutMapping("/{tournamentID}/rounds/{roundNumber}/matches/results")
    public ResponseEntity<String> updateMatchResultsBatch(
            @PathVariable String tournamentID,
            @PathVariable int roundNumber,
            @RequestBody Map<Integer, MatchResult> matchResults) {
        try {
            tournamentService.updateMatchResults(tournamentID, roundNumber, matchResults);
            return ResponseEntity.ok("Batch match results updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating batch match results: " + e.getMessage());
        }
    }

    // 3. Populate the next round's matches with winners
    @PostMapping("/{tournamentID}/rounds/{currentRoundNumber}/populateNextRound")
    public ResponseEntity<String> populateNextRoundMatches(
            @PathVariable String tournamentID,
            @PathVariable int currentRoundNumber) {
        try {
            eliminationService.populateNextRoundMatches(tournamentID, currentRoundNumber);
            return ResponseEntity.ok("Next round populated with winners.");
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error populating next round: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

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
