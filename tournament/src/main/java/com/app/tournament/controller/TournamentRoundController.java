package com.app.tournament.controller;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;


import com.app.tournament.DTO.TournamentRoundDTO;
import com.app.tournament.model.TournamentRound;
import com.app.tournament.service.TournamentRoundService;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/tournaments") // Base URL for tournament-related operations
public class TournamentRoundController {

    @Autowired
    private TournamentRoundService tournamentRoundService;

    // Create a new round in a tournament
    @PostMapping("/{tournamentId}/rounds")
    public ResponseEntity<String> createTournamentRound(
            @RequestBody TournamentRoundDTO roundDTO) {
        try {
            String roundId = tournamentRoundService.createTournamentRound(roundDTO);
            return ResponseEntity.ok(roundId); // Return the new round ID
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating round: " + e.getMessage());
        }
    }

    // Retrieve a round by its ID
    @GetMapping("/rounds/{roundId}")
    public ResponseEntity<TournamentRound> getTournamentRoundById(
            @PathVariable String roundId) {
        try {
            TournamentRound round = tournamentRoundService.getTournamentRoundById(roundId);
            return ResponseEntity.ok(round); // Return the round details
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null); // Round not found
        }
    }

    // Get all rounds of a specific tournament
    @GetMapping("/{tournamentId}/rounds")
    public ResponseEntity<List<TournamentRound>> getRoundsByTournamentId(
            @PathVariable String tournamentId) {
        try {
            List<TournamentRound> rounds = tournamentRoundService.getRoundsByTournamentId(tournamentId);
            return ResponseEntity.ok(rounds); // Return the list of rounds
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null); // Error occurred
        }
    }

    // Update a specific round
    @PutMapping("/rounds/{roundId}")
    public ResponseEntity<String> updateTournamentRound(
            @PathVariable String roundId,
            @RequestBody TournamentRoundDTO updatedRound) {
        try {
            String result = tournamentRoundService.updateTournamentRound(roundId, updatedRound);
            return ResponseEntity.ok(result); // Return success message
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating round: " + e.getMessage());
        }
    }

    // Delete a specific round
    @DeleteMapping("/rounds/{roundId}")
    public ResponseEntity<Void> deleteTournamentRound(@PathVariable String roundId) {
        try {
            tournamentRoundService.deleteTournamentRound(roundId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // No content on success
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // Error occurred
        }
    }
}
