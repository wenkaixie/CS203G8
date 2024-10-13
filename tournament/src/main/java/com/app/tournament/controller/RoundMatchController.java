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

import com.app.tournament.DTO.RoundMatchDTO;    
import com.app.tournament.model.RoundMatch;
import com.app.tournament.service.RoundMatchService;

import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/rounds") // Base URL for round-related operations
public class RoundMatchController {

    @Autowired
    private RoundMatchService roundMatchService;

    // Create a new match within a round
    @PostMapping("/{roundId}/matches")
    public ResponseEntity<String> createRoundMatch(@RequestBody RoundMatchDTO matchDTO) {
        try {
            String matchId = roundMatchService.createRoundMatch(matchDTO);
            return ResponseEntity.ok(matchId); // Return the match ID
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating match: " + e.getMessage());
        }
    }

    // Retrieve a match by its ID
    @GetMapping("/matches/{matchId}")
    public ResponseEntity<RoundMatch> getMatchById(@PathVariable String matchId) {
        try {
            RoundMatch match = roundMatchService.getMatchById(matchId);
            return ResponseEntity.ok(match); // Return the match details
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null); // Match not found
        }
    }

    // Get all matches within a specific round
    @GetMapping("/{roundId}/matches")
    public ResponseEntity<List<RoundMatch>> getMatchesByRoundId(@PathVariable String roundId) {
        try {
            List<RoundMatch> matches = roundMatchService.getMatchesByRoundId(roundId);
            return ResponseEntity.ok(matches); // Return the list of matches
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null); // Error occurred
        }
    }

    // Get latest match played by a user
    @GetMapping("/latest/{userId}")
    public ResponseEntity<RoundMatch> getLatestMatchByUserId(@PathVariable String userId) {
        try {
            RoundMatch match = roundMatchService.getLatestMatchByUserId(userId);
            return ResponseEntity.ok(match); // Return the latest match
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null); // Match not found
        }
    }

    // Update a match's score
    @PutMapping("/matches/{matchId}")
    public ResponseEntity<String> updateMatch(
            @PathVariable String matchId,
            @RequestBody RoundMatchDTO updatedMatch) {
        try {
            String result = roundMatchService.updateMatch(matchId, updatedMatch);
            return ResponseEntity.ok(result); // Return success message
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating match: " + e.getMessage());
        }
    }

    // Delete a match by ID
    @DeleteMapping("/matches/{matchId}")
    public ResponseEntity<Void> deleteMatch(@PathVariable String matchId) {
        try {
            roundMatchService.deleteMatch(matchId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // No content on success
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // Error occurred
        }
    }
}
