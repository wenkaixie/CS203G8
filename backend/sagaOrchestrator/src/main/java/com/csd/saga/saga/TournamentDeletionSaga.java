package com.csd.saga.saga;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.csd.saga.AMQP.RabbitMQConfig;
import com.csd.saga.clientInterface.AdminManagementServiceClient;
import com.csd.saga.clientInterface.PlayerManagementServiceClient;
import com.csd.saga.clientInterface.TournamentServiceClient;

@Component
public class TournamentDeletionSaga {

    private static final Logger log = LoggerFactory.getLogger(TournamentDeletionSaga.class);

    @Autowired
    private TournamentServiceClient tournamentServiceClient;

    @Autowired
    private AdminManagementServiceClient adminManagementServiceClient;

    @Autowired
    private PlayerManagementServiceClient playerManagementServiceClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private final String deleteTournamentQueue = RabbitMQConfig.DELETE_TOURNAMENT_QUEUE;
    private final String deleteTournamentResponseQueue = RabbitMQConfig.DELETE_TOURNAMENT_RESPONSE_QUEUE;

    public void deleteTournament(String tournamentID) {
        try {
           
            String adminId = tournamentServiceClient.getAdminIdForTournament(tournamentID);
            List<String> userIds = tournamentServiceClient.getUserIdsForTournament(tournamentID);

            // Step 1: Delete the tournament document
            tournamentServiceClient.deleteTournament(tournamentID);
            log.info("Tournament {} deleted in TournamentService.", tournamentID);


            if (adminId != null) {
                adminManagementServiceClient.removeTournamentFromAdmin(adminId, tournamentID);
                log.info("Removed tournament ID {} from admin {}'s tournamentCreated list.", tournamentID, adminId);
            }

            // Step 3: Delete all users in the tournament's Users subcollection and update
            // their registration history
            for (String userId : userIds) {
                playerManagementServiceClient.removeTournamentFromUser(userId, tournamentID);
                log.info("Removed tournament ID {} from user {}'s registration history.", tournamentID, userId);
            }

            // Step 4: Notify other services asynchronously via RabbitMQ
            rabbitTemplate.convertAndSend(deleteTournamentQueue, tournamentID);
            log.info("Notified other services for deletion of tournament {}.", tournamentID);

        } catch (Exception e) {
            log.error("Error during deletion of tournament {}: {}", tournamentID, e.getMessage());
            initiateRollback(tournamentID);
        }
    }

    private void initiateRollback(String tournamentID) {
        try {
            // Step 1: Reinstate the tournament if it was deleted
            tournamentServiceClient.reinstateTournament(tournamentID);
            log.info("Rollback: Tournament {} reinstated.", tournamentID);

            // Step 2: Restore the tournament reference in admin’s tournamentCreated list if
            // it was removed
            String adminId = tournamentServiceClient.getAdminIdForTournament(tournamentID);
            if (adminId != null) {
                adminManagementServiceClient.addTournamentToAdmin(adminId, tournamentID);
                log.info("Rollback: Tournament ID {} re-added to admin {}'s tournamentCreated list.", tournamentID,
                        adminId);
            }

            // Step 3: Restore the tournament reference in each user’s registration history
            // if it was removed
            List<String> userIds = tournamentServiceClient.getUserIdsForTournament(tournamentID);
            for (String userId : userIds) {
                playerManagementServiceClient.addTournamentToUser(userId, tournamentID);
                log.info("Rollback: Tournament ID {} re-added to user {}'s registration history.", tournamentID,
                        userId);
            }

            log.info("Rollback for tournament {} completed successfully.", tournamentID);

        } catch (Exception rollbackException) {
            log.error("Rollback failed for tournament {}: {}", tournamentID, rollbackException.getMessage());          
        }
    }
}
