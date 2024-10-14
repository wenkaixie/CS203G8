package com.app.tournament.controller;


import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.tournament.DTO.TournamentRoundDTO;
import com.app.tournament.model.TournamentRound;
import com.app.tournament.service.TournamentRoundService;

@RestController
@RequestMapping("/api/tournamentRounds")
public class TournamentRoundController {

    @Autowired
    private TournamentRoundService roundService;

    // Endpoint to create a new tournament round
    @PostMapping
    public ResponseEntity<String> createTournamentRound(@RequestBody TournamentRoundDTO roundDTO) {
        try {
            String roundId = roundService.createTournamentRound(roundDTO);
            return ResponseEntity.ok("Round created with ID: " + roundId);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error creating round: " + e.getMessage());
        }
    }

    // Endpoint to retrieve a specific round by ID
    @GetMapping("/{roundId}")
    public ResponseEntity<TournamentRound> getTournamentRoundById(@PathVariable String roundId) {
        try {
            TournamentRound round = roundService.getTournamentRoundById(roundId);
            return ResponseEntity.ok(round);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(null);
        }
    }

    // Endpoint to get all rounds of a specific tournament
    @GetMapping("/tournament/{tournamentId}")
    public ResponseEntity<List<TournamentRound>> getRoundsByTournamentId(@PathVariable String tournamentId) {
        try {
            List<TournamentRound> rounds = roundService.getRoundsByTournamentId(tournamentId);
            return ResponseEntity.ok(rounds);
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    // Endpoint to update a specific round
    @PutMapping("/{roundId}")
    public ResponseEntity<String> updateTournamentRound(
            @PathVariable String roundId, @RequestBody TournamentRoundDTO updatedRound) {
        try {
            String result = roundService.updateTournamentRound(roundId, updatedRound);
            return ResponseEntity.ok(result);
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(500).body("Error updating round: " + e.getMessage());
        }
    }

    // Endpoint to delete a specific round
    @DeleteMapping("/{roundId}")
    public ResponseEntity<String> deleteTournamentRound(@PathVariable String roundId) {
        try {
            roundService.deleteTournamentRound(roundId);
            return ResponseEntity.ok("Round deleted successfully.");
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(500).body("Error deleting round: " + e.getMessage());
        }
    }
}
