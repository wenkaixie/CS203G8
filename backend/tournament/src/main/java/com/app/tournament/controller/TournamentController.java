package com.app.tournament.controller;

import java.util.List;
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

import com.app.tournament.DTO.TournamentDTO;
import com.app.tournament.model.Match;
import com.app.tournament.model.Tournament;
import com.app.tournament.service.EliminationService;
import com.app.tournament.service.TournamentService;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private EliminationService eliminationService;

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

    @GetMapping("/{tournamentID}")
    public ResponseEntity<Tournament> getTournamentById(@PathVariable String tournamentID) {
        try {
            Tournament tournament = tournamentService.getTournamentById(tournamentID);
            return ResponseEntity.ok(tournament);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping
    public ResponseEntity<List<Tournament>> getAllTournaments() {
        try {
            List<Tournament> tournaments = tournamentService.getAllTournaments();
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

    // Delete tournament endpoint
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

    // Get upcoming tournaments
    @GetMapping("/upcoming/{userID}")
    public ResponseEntity<List<Tournament>>
    getUpcomingTournamentsOfUser(@PathVariable String userID) {
    try {
    List<Tournament> tournaments =
    tournamentService.getUpcomingTournamentsOfUser(userID);
    return ResponseEntity.ok(tournaments);
    } catch (Exception e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
    }

    // Get past tournaments
    @GetMapping("/past/{userID}")
    public ResponseEntity<List<Tournament>>
    getPastTournamentsOfUser(@PathVariable String userID) {
    try {
    List<Tournament> tournaments =
    tournamentService.getPastTournamentsOfUser(userID);
    return ResponseEntity.ok(tournaments);
    } catch (Exception e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
    }

    // Get ongoing tournaments of user
    @GetMapping("/ongoing/{userID}")
    public ResponseEntity<List<Tournament>>
    getOngoingTournamentsOfUser(@PathVariable String userID) {
    try {
    List<Tournament> tournaments =
    tournamentService.getOngoingTournamentsOfUser(userID);
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

    // Add player to tournament
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

    @DeleteMapping("/{tournamentID}/players/{playerID}")
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

    // 1. Generate rounds and matches for the tournament
    @PostMapping("/{tournamentID}/generateRounds")
    public ResponseEntity<String> generateRoundsForTournament(@PathVariable String tournamentID) {
        try {
            eliminationService.generateRoundsForTournament(tournamentID);
            return ResponseEntity.ok("Rounds and matches generated successfully.");
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating rounds: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    // 2. Update the winner of a specific match
    @PutMapping("/{tournamentID}/rounds/{roundNumber}/matches/{matchId}/winner")
    public ResponseEntity<String> updateMatchWinner(
            @PathVariable String tournamentID,
            @PathVariable int roundNumber,
            @PathVariable int matchId,
            @RequestBody String winnerName) {
        try {
            
            eliminationService.updateMatchWinner(tournamentID, roundNumber, matchId, winnerName);
            return ResponseEntity.ok("Winner updated successfully.");
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating match winner: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
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
