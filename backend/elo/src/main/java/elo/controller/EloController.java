package elo.controller;

import elo.service.EloService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import java.util.concurrent.ExecutionException;
import elo.model.EloBatchUpdateRequest;


@RestController
@RequestMapping("/api/elo")
public class EloController {

    private final EloService eloService;
    private static final Logger logger = LoggerFactory.getLogger(EloController.class);

    public EloController(EloService eloService) {
        this.eloService = eloService;
    }

    // New endpoint to retrieve and update Elo ratings for a list of users
    @PutMapping("/retrieve/{tournamentId}")
    public ResponseEntity<Object> retrieveResults(
            @PathVariable String tournamentId,
            @RequestBody EloBatchUpdateRequest request) {
        
        logger.info("Received retrieveResults request: tournamentId={}, request={}", tournamentId, request);

        // Call the retrieveResults function in the EloService
        return eloService.retrieveResults(tournamentId, request.getUserIds(), request.getResults());
    }

    private ResponseEntity<Object> createErrorResponse(String message, HttpStatus status) {
        return new ResponseEntity<>(message, status);
    }
}
