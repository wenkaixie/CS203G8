package com.csd.saga.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.csd.saga.saga.AddUserToTournamentSaga;
import com.csd.saga.saga.RemoveUserFromTournamentSaga;
import com.csd.saga.saga.TournamentCreationSaga;
import com.csd.saga.saga.TournamentDeletionSaga;
import com.csd.saga.saga.UpdateEloSaga;
import com.csd.shared_library.DTO.MatchResultUpdateRequest;
import com.csd.shared_library.DTO.TournamentDTO;

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
    private UpdateEloSaga updateEloSaga;

    // @Autowired
    // private TournamentQuerySaga tournamentQuerySaga;

    /**
     * Start the tournament creation saga.
     * 
     * @param tournamentDTO DTO containing tournament details.
     * @return The ID of the created tournament.
     */
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

    // /**
    //  * Start the tournament deletion saga.
    //  * 
    //  * @param tournamentID ID of the tournament to delete.
    //  */
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

    /**
     * Start the add-user-to-tournament saga.
     * 
     * @param tournamentID ID of the tournament.
     * @param userID       ID of the user to add.
     */
    public void startAddUserToTournamentSaga(String tournamentID, String userID) {
        addUserToTournamentSaga.addUserToTournament(tournamentID, userID);
    }

    /**
     * Start the remove-user-from-tournament saga.
     * 
     * @param tournamentID ID of the tournament.
     * @param userID       ID of the user to remove.
     */
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

    // /**
    //  * Get a list of upcoming tournaments for a user.
    //  * 
    //  * @param userID ID of the user.
    //  * @return List of upcoming tournaments.
    //  */
    // public List<Tournament> getUpcomingTournamentsOfUser(String userID) {
    //     return tournamentQuerySaga.getUpcomingTournaments(userID);
    // }

    // /**
    //  * Get a list of past tournaments for a user.
    //  * 
    //  * @param userID ID of the user.
    //  * @return List of past tournaments.
    //  */
    // public List<Tournament> getPastTournamentsOfUser(String userID) {
    //     return tournamentQuerySaga.getPastTournaments(userID);
    // }

    // /**
    //  * Get a list of ongoing tournaments for a user.
    //  * 
    //  * @param userID ID of the user.
    //  * @return List of ongoing tournaments.
    //  */
    // public List<Tournament> getOngoingTournamentsOfUser(String userID) {
    //     return tournamentQuerySaga.getOngoingTournaments(userID);
    // }

    // /**
    //  * Get a list of tournaments a user is eligible for.
    //  * 
    //  * @param userID ID of the user.
    //  * @return List of eligible tournaments.
    //  */
    // public List<Tournament> getEligibleTournamentsOfUser(String userID) {
    //     return tournamentQuerySaga.getEligibleTournaments(userID);
    // }

   
}
