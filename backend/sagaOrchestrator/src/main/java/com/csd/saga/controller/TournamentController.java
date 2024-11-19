package com.csd.saga.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.csd.saga.service.EliminationService;
import com.csd.saga.service.RoundRobinService;
import com.csd.saga.service.TournamentService;
import com.csd.shared_library.model.Tournament;

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



    // tested working 8 Nov

    // MOVE TO USER SERVICE 
    @GetMapping("/user/{userID}")
    public ResponseEntity<List<Tournament>> getAllTournamentsOfUser(@PathVariable String userID) {
        try {
            List<Tournament> tournaments = tournamentService.getTournamentsOfUser(userID);
            return ResponseEntity.ok(tournaments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    // tested working 8 Nov
    // Get upcoming tournaments
    @GetMapping("/upcoming/{userID}")
    public ResponseEntity<List<Tournament>> getUpcomingTournamentsOfUser(@PathVariable String userID) {
        try {
            List<Tournament> tournaments = tournamentService.getUpcomingTournamentsOfUser(userID);
            return ResponseEntity.ok(tournaments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // tested working 8 Nov
    // Get past tournaments
    @GetMapping("/past/{userID}")
    public ResponseEntity<List<Tournament>> getPastTournamentsOfUser(@PathVariable String userID) {
        try {
            List<Tournament> tournaments = tournamentService.getPastTournamentsOfUser(userID);
            return ResponseEntity.ok(tournaments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // tested working 8 Nov
    // Get ongoing tournaments of user
    @GetMapping("/ongoing/{userID}")
    public ResponseEntity<List<Tournament>> getOngoingTournamentsOfUser(@PathVariable String userID) {
        try {
            List<Tournament> tournaments = tournamentService.getOngoingTournamentsOfUser(userID);
            return ResponseEntity.ok(tournaments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // tested working 8 Nov
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

    
}
