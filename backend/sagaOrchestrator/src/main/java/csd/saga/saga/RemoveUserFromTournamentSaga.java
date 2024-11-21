package csd.saga.saga;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import csd.saga.clientInterface.PlayerManagementServiceClient;
import csd.saga.clientInterface.TournamentServiceClient;
import csd.shared_library.model.Tournament;
import csd.shared_library.model.User;
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
            validateUser(authId);

            // Step 2: Validate the tournament
            validateTournament(tournamentID);

            // Step 3: Remove user from tournament's subcollection
            tournamentServiceClient.removeUserFromTournament(tournamentID, authId);
            log.info("User {} removed from tournament {} successfully.", authId, tournamentID);

            // Step 4: Update user's registration history
            playerManagementServiceClient.removeTournamentFromUser(authId, tournamentID);
            log.info("Removed tournament {} from user {}'s registration history.", tournamentID, authId);

            log.info("Saga for removing user {} from tournament {} completed successfully.", authId, tournamentID);

        } catch (Exception e) {
            log.error("Error during Saga for removing user {} from tournament {}: {}", authId, tournamentID,
                    e.getMessage());
            log.info("Initiating rollback for removing user from tournament saga.");
            rollbackUserRemoval(tournamentID, authId);
            throw new RuntimeException("Saga failed for removing user from tournament: " + e.getMessage(), e);
        }
    }

    private void validateUser(String authId) {
        log.info("Validating user with ID: {}", authId);
        ResponseEntity<User> userResponse = playerManagementServiceClient.getUser(authId);

        if (!userResponse.getStatusCode().is2xxSuccessful() || userResponse.getBody() == null) {
            log.error("User validation failed for ID: {}", authId);
            throw new RuntimeException("User not found with ID: " + authId);
        }
        log.info("User {} validated successfully.", authId);
    }

    private void validateTournament(String tournamentID) {
        log.info("Validating tournament with ID: {}", tournamentID);
        ResponseEntity<Tournament> tournamentResponse = tournamentServiceClient.getTournament(tournamentID);

        if (!tournamentResponse.getStatusCode().is2xxSuccessful() || tournamentResponse.getBody() == null) {
            log.error("Tournament validation failed for ID: {}", tournamentID);
            throw new RuntimeException("Tournament not found with ID: " + tournamentID);
        }
        log.info("Tournament {} validated successfully.", tournamentID);
    }

    private void rollbackUserRemoval(String tournamentID, String authId) {
        try {
            // Step 1: Re-add user to the tournament's subcollection
            tournamentServiceClient.addUserToTournament(tournamentID, authId);
            log.info("Rolled back user removal by reinstating user {} to tournament ID: {}", authId, tournamentID);

            // Step 2: Revert user's registration history
            playerManagementServiceClient.addTournamentToUser(authId, tournamentID);
            log.info("Rolled back user registration history for user {} and tournament ID: {}", authId, tournamentID);

        } catch (Exception rollbackException) {
            log.error("Rollback failed for user {} and tournament ID {}: {}", authId, tournamentID,
                    rollbackException.getMessage());
        }
    }
}

