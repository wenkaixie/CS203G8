package com.csd.saga.saga;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.csd.saga.clientInterface.PlayerManagementServiceClient;
import com.csd.saga.clientInterface.TournamentServiceClient;
import com.csd.shared_library.model.Tournament;
import com.csd.shared_library.model.User;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AddUserToTournamentSaga {

    @Autowired
    private TournamentServiceClient tournamentServiceClient;

    @Autowired
    private PlayerManagementServiceClient playerManagementServiceClient;

    public void addUserToTournament(String tournamentID, String authId) {
        log.info("Starting Saga for adding user {} to tournament {}", authId, tournamentID);
        try {
            // // Step 1: Validate the user
            ResponseEntity<User> userResponse = playerManagementServiceClient.getUser(authId);
            if (!userResponse.getStatusCode().is2xxSuccessful() || userResponse.getBody() == null) {
                throw new RuntimeException("User not found with ID: " + authId);
            }
            log.info("User {} validated successfully.", authId);

            // // Step 2: Validate the tournament
            ResponseEntity<Tournament> tournamentResponse = tournamentServiceClient.getTournament(tournamentID);
            if (!tournamentResponse.getStatusCode().is2xxSuccessful() || tournamentResponse.getBody() == null) {
                throw new RuntimeException("Tournament not found with ID: " + tournamentID);
            }
            log.info("Tournament {} validated successfully.", tournamentID);

            // Step 3: Add user to tournament's subcollection
            tournamentServiceClient.addUserToTournament(tournamentID, authId);

            // // Step 4: Update user's registration history
            // playerManagementServiceClient.addTournamentToUser(authId, tournamentID);

            log.info("Saga for adding user {} to tournament {} completed successfully.", authId, tournamentID);

        } catch (Exception e) {
            log.error("Error during Saga for adding user {} to tournament {}: {}", authId, tournamentID,
                    e.getMessage());
            initiateRollback(tournamentID, authId);
            throw new RuntimeException("Saga failed for adding user to tournament: " + e.getMessage(), e);
        }
    }

    private void initiateRollback(String tournamentID, String authId) {
        try {
            log.info("Initiating rollback for Saga of adding user {} to tournament {}", authId, tournamentID);
            tournamentServiceClient.removeUserFromTournament(tournamentID, authId);
            playerManagementServiceClient.removeTournamentFromUser(authId, tournamentID);
            log.info("Rollback for Saga completed.");
        } catch (Exception rollbackException) {
            log.error("Rollback failed for Saga: {}", rollbackException.getMessage());
        }
    }
}
