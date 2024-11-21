package csd.saga.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import csd.saga.saga.AddUserToTournamentSaga;
import csd.saga.saga.RemoveUserFromTournamentSaga;
import csd.saga.saga.TournamentCreationSaga;
import csd.saga.saga.TournamentDeletionSaga;
import csd.saga.saga.UpdateEloOrchestration;
import csd.shared_library.DTO.MatchResultUpdateRequest;
import csd.shared_library.DTO.TournamentDTO;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SagaOrchestratorService {

    @Autowired
    private TournamentCreationSaga tournamentCreationSaga;

    @Autowired
    private TournamentDeletionSaga tournamentDeletionSaga;

    @Autowired
    private AddUserToTournamentSaga addUserToTournamentSaga;

    @Autowired
    private RemoveUserFromTournamentSaga removeUserFromTournamentSaga;

    @Autowired
    private UpdateEloOrchestration updateEloSaga;

    public String startTournamentCreationSaga(TournamentDTO tournamentDTO) {
        log.info("Starting Tournament Creation Saga for admin: {}", tournamentDTO.getAdminId());
        try {
            String tournamentId = tournamentCreationSaga.createTournament(tournamentDTO);
            log.info("Tournament Creation Saga completed successfully with ID: {}", tournamentId);
            return tournamentId;
        } catch (Exception e) {
            log.error("Error in Tournament Creation Saga: {}", e.getMessage());
            throw new RuntimeException("Tournament Creation Saga failed: " + e.getMessage(), e);
        }
    }

   
    public void startTournamentDeletionSaga(String tournamentID) {
        log.info("Starting Tournament Deletion Saga for tournament ID: {}", tournamentID);
        try {
            tournamentDeletionSaga.deleteTournament(tournamentID);
            log.info("Tournament Deletion Saga completed successfully for tournament ID: {}", tournamentID);
        } catch (Exception e) {
            log.error("Tournament Deletion Saga failed for tournament ID {}: {}", tournamentID, e.getMessage());
            throw new RuntimeException("Tournament Deletion Saga failed: " + e.getMessage(), e);
        }
    }

    public void startAddUserToTournamentSaga(String tournamentID, String userID) {
        addUserToTournamentSaga.addUserToTournament(tournamentID, userID);
    }

    public void startRemoveUserFromTournamentSaga(String tournamentID, String userID) {
        removeUserFromTournamentSaga.removeUserFromTournament(tournamentID, userID);
    }

    public ResponseEntity<String> startUpdateEloSaga(String tournamentID, 
                                                int roundNumber,
                                                Map<Integer, MatchResultUpdateRequest> matchResults) {
            log.info("Starting Elo Update Saga for tournament: {}", tournamentID);
            try {
                updateEloSaga.updateElo(tournamentID, roundNumber, matchResults);
            } catch (Exception e) {
                log.error("Saga failed for round {} in tournament {}: {}", roundNumber, tournamentID, e.getMessage());
                return ResponseEntity.internalServerError()
                        .body("Saga failed for match " + roundNumber + ": " + e.getMessage());
            }

            log.info("Elo Update Saga successfully completed for tournament: {}", tournamentID);
            return ResponseEntity.ok("Elo ratings processed successfully for provided matches.");
    }

}
