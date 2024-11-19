package elo.controller;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.csd.shared_library.DTO.MatchResultUpdateRequest;

import elo.service.EloService;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/elo")
public class EloController {

    @Autowired
    private EloService eloService;
    private static final Logger logger = LoggerFactory.getLogger(EloController.class);

    @PutMapping("/tournaments/{tournamentID}/rounds/{roundNumber}/matches/updateElo")
    public ResponseEntity<String> updateElo(
            @PathVariable String tournamentID,
            @PathVariable int roundNumber,
            @RequestBody Map<Integer, MatchResultUpdateRequest> matchResults) {

        logger.info("Received retrieveResults request: tournamentId={}, request={}", tournamentID, matchResults);

        try {
            return eloService.updateElo(tournamentID, roundNumber, matchResults);

        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error during Elo retrieval and update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing Elo retrieval and update.");
        }
    }
}
