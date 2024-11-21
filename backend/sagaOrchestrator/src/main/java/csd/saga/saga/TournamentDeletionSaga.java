package csd.saga.saga;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import csd.saga.clientInterface.AdminManagementServiceClient;
import csd.saga.clientInterface.PlayerManagementServiceClient;
import csd.saga.clientInterface.TournamentServiceClient;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TournamentDeletionSaga {


    @Autowired
    private TournamentServiceClient tournamentServiceClient;

    @Autowired
    private AdminManagementServiceClient adminManagementServiceClient;

    @Autowired
    private PlayerManagementServiceClient playerManagementServiceClient;


    public void deleteTournament(String tournamentID) {
        log.info("Starting Tournament Deletion Saga for tournament ID: {}", tournamentID);

        String adminId = null;
        List<String> userIds = new ArrayList<>();

        try {
            // Step 1: Fetch Admin ID associated with the tournament
            adminId = tournamentServiceClient.getAdminIdForTournament(tournamentID);
            log.info("Admin ID {} associated with tournament ID {}.", adminId, tournamentID);

            // Step 2: Fetch all users registered for the tournament
            userIds = tournamentServiceClient.getUserIdsForTournament(tournamentID);
            log.info("Found {} users registered for tournament ID {}.", userIds.size(), tournamentID);

            // Step 3: Store the snapshot of the tournament
            tournamentServiceClient.storeTournamentSnapshot(tournamentID);

            // Step 4: Delete the tournament
            tournamentServiceClient.deleteTournament(tournamentID);
            log.info("Tournament ID {} deleted successfully.", tournamentID);

            // Step 5: Update Admin Document
            if (adminId != null) {
                adminManagementServiceClient.removeTournamentFromAdmin(adminId, tournamentID);
                log.info("Tournament ID {} removed from admin {}.", tournamentID, adminId);
            }

            // Step 6: Update each user's registration history
            for (String userId : userIds) {
                playerManagementServiceClient.removeTournamentFromUser(userId, tournamentID);
                log.info("Removed tournament ID {} from user {}'s registration history.", tournamentID, userId);
            }

        } catch (Exception e) {
            log.error("Error during tournament deletion saga for tournament ID {}: {}", tournamentID, e.getMessage());
            log.info("Initiating rollback for tournament deletion saga.");
            rollbackTournamentDeletion(tournamentID, adminId, userIds);
            throw new RuntimeException("Tournament deletion failed: " + e.getMessage(), e);
        }

        log.info("Tournament Deletion Saga completed successfully for tournament ID: {}", tournamentID);
    }

    private void rollbackTournamentDeletion(String tournamentID, String adminId, List<String> userIds) {
        try {
            // Step 1: Reinstate the tournament
            tournamentServiceClient.reinstateTournament(tournamentID);
            log.info("Rolled back tournament deletion by reinstating tournament ID: {}", tournamentID);

            // Step 2: Revert Admin Update
            if (adminId != null) {
                adminManagementServiceClient.addTournamentToAdmin(adminId, tournamentID);
                log.info("Rolled back admin update for admin ID: {}", adminId);
            }

            // Step 3: Revert user updates
            for (String userId : userIds) {
                playerManagementServiceClient.addTournamentToUser(userId, tournamentID);
                log.info("Rolled back user update for user ID: {}", userId);
            }

        } catch (Exception rollbackException) {
            log.error("Rollback failed for tournament ID {}: {}", tournamentID, rollbackException.getMessage());
        }
    }
}

