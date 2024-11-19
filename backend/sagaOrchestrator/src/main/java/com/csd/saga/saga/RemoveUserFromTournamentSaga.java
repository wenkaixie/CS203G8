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
public class RemoveUserFromTournamentSaga {

    @Autowired
    private TournamentServiceClient tournamentServiceClient;

    @Autowired
    private PlayerManagementServiceClient playerManagementServiceClient;

    public void removeUserFromTournament(String tournamentID, String authId) {
        log.info("Starting Saga for removing user {} from tournament {}", authId, tournamentID);
        try {
            // Step 1: Validate the user
            ResponseEntity<User> userResponse = playerManagementServiceClient.getUser(authId);
            if (!userResponse.getStatusCode().is2xxSuccessful() || userResponse.getBody() == null) {
                throw new RuntimeException("User not found with ID: " + authId);
            }
            log.info("User {} validated successfully.", authId);

            // Step 2: Validate the tournament
            ResponseEntity<Tournament> tournamentResponse = tournamentServiceClient.getTournament(tournamentID);
            if (!tournamentResponse.getStatusCode().is2xxSuccessful() || tournamentResponse.getBody() == null) {
                throw new RuntimeException("Tournament not found with ID: " + tournamentID);
            }
            log.info("Tournament {} validated successfully.", tournamentID);

            // Step 3: Remove user from tournament's subcollection
            tournamentServiceClient.removeUserFromTournament(tournamentID, authId);

            // Step 4: Update user's registration history
            playerManagementServiceClient.removeTournamentFromUser(authId, tournamentID);

            log.info("Saga for removing user {} from tournament {} completed successfully.", authId, tournamentID);

        } catch (Exception e) {
            log.error("Error during Saga for removing user {} from tournament {}: {}", authId, tournamentID,
                    e.getMessage());
            throw new RuntimeException("Saga failed for removing user from tournament: " + e.getMessage(), e);
        }
    }
}
