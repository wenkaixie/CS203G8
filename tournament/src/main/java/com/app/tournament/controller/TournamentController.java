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
import com.app.tournament.model.Tournament;
import com.app.tournament.service.TournamentService;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/tournaments") 
public class TournamentController {

    @Autowired
    private TournamentService tournamentService;

    // Create tournament endpoint
    @PostMapping
    public ResponseEntity<String> createTournament(@RequestBody TournamentDTO tournamentDTO) {
        try {
            String tournamentID = tournamentService.createTournament(tournamentDTO);
            return ResponseEntity.ok(tournamentID); // Return the tournament ID in the response body
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error when creating the tournament: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Return 500 Internal Server Error on failure
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Catching other unexpected errors
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Tournament>> getTournamentsByLocation(@RequestParam String location) {
        try {
            List<Tournament> tournaments = tournamentService.getTournamentsByLocation(location);
            return ResponseEntity.ok(tournaments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    @GetMapping("/paginated")
    public ResponseEntity<List<Tournament>> getTournamentsWithPagination(
            @RequestParam int limit, 
            @RequestParam(required = false) String lastTournamentID) {
        try {
            List<Tournament> tournaments = tournamentService.getTournamentsWithPagination(limit, lastTournamentID);
            return ResponseEntity.ok(tournaments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    // Add player to tournament
    @PostMapping("/{tournamentID}/users")
    public ResponseEntity<String> addUserToTournament(@PathVariable String tournamentID,
            @RequestBody String userID) {
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

    @DeleteMapping("/{tournamentID}/user/{userID}")
    public ResponseEntity<String> removePlayerFromTournament(
            @PathVariable String tournamentID, 
            @PathVariable String userID) {
        try {
            String response = tournamentService.removeUserFromTournament(tournamentID, userID);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}

