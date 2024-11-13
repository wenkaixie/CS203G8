package com.csd.saga.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.csd.saga.saga.TournamentDeletionSaga;
import com.csd.saga.service.SagaSenderService;

public class SagaOrchestratorController {
    
    @Autowired
    private TournamentDeletionSaga tournamentDeletionSaga;

    @Autowired
    private SagaSenderService sagaSenderService;

    @DeleteMapping("/deleteTournament/{tournamentID}")
    public ResponseEntity<String> deleteTournament(@PathVariable String tournamentID) {
        try {
            tournamentDeletionSaga.deleteTournament(tournamentID);
            return ResponseEntity.ok("Tournament deletion started successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error initiating tournament deletion saga: " + e.getMessage());
        }
    }
}
