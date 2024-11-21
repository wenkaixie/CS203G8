package csd.saga.saga;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import csd.saga.AMQP.RabbitMQConfig;
import csd.saga.clientInterface.AdminManagementServiceClient;
import csd.saga.clientInterface.TournamentServiceClient;
import csd.shared_library.DTO.TournamentDTO;
import lombok.extern.slf4j.Slf4j;


@Component
@Slf4j
public class TournamentCreationSaga {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AdminManagementServiceClient adminManagementServiceClient;

    @Autowired
    private TournamentServiceClient tournamentServiceClient;
    

    public String createTournament(TournamentDTO tournamentDTO) {
        log.info("Creating new tournament...");

        String adminId = tournamentDTO.getAdminId();
        String generatedId = null;

        try {
            // Step 1: Validate Admin Existence
            ResponseEntity<Boolean> response = adminManagementServiceClient.validateAdmin(adminId);
            boolean isAdminValid = Boolean.TRUE.equals(response.getBody());
            if (!isAdminValid) {
                log.warn("Admin with ID {} not found.", adminId);
                throw new IllegalArgumentException("Admin not found with ID: " + adminId);
            }

            // Step 2: Create Tournament
            ResponseEntity<String> creationResponse = tournamentServiceClient.createTournament(tournamentDTO);
            generatedId = creationResponse.getBody();
            log.info("Tournament {} created successfully.", generatedId);

            // Step 3: Update Admin Document
            adminManagementServiceClient.addTournamentToAdmin(adminId, generatedId);
            log.info("Tournament ID {} added to admin {}.", generatedId, adminId);

        } catch (Exception e) {
            log.error("Error during tournament creation: {}", e.getMessage());
            if (generatedId != null) {
                log.info("Initiating rollback for tournament ID: {}", generatedId);
                rollbackTournamentCreation(adminId, generatedId);
            }
            throw new RuntimeException("Tournament creation failed: " + e.getMessage(), e);
        }

        return generatedId;
    }

    private void rollbackTournamentCreation(String adminId, String tournamentId) {
        try {
            // Step 1: Send Delete Tournament Message
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.DELETE_TOURNAMENT_QUEUE,
                adminId + "," + tournamentId
            );
            log.info("Sent rollback message to delete tournament ID: {}", tournamentId);

        } catch (Exception rollbackException) {
            log.error("Rollback failed for tournament ID {}: {}", tournamentId, rollbackException.getMessage());
        }

        
    }
}
