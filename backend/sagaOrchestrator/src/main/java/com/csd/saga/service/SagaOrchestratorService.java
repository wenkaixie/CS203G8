package com.csd.saga.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.csd.saga.saga.AddUserToTournamentSaga;
import com.csd.saga.saga.RemoveUserFromTournamentSaga;
import com.csd.saga.saga.TournamentCreationSaga;
import com.csd.saga.saga.TournamentDeletionSaga;
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

    // public void startRemoveUserFromTournamentSaga(String tournamentID, String userID) {
    //     throw new UnsupportedOperationException("Not supported yet.");
    // }
}
