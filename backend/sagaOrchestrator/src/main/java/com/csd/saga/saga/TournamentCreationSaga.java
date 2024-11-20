package com.csd.saga.saga;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.csd.saga.clientInterface.AdminManagementServiceClient;
import com.csd.saga.clientInterface.TournamentServiceClient;
import com.csd.saga.service.TournamentSchedulerService;
import com.csd.shared_library.DTO.TournamentDTO;

import lombok.extern.slf4j.Slf4j;


@Component
@Slf4j
public class TournamentCreationSaga {

    @Autowired
    private TournamentSchedulerService tournamentSchedulerService;

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
            // Step 1: Remove Tournament
            tournamentServiceClient.deleteTournament(tournamentId);
            log.info("Rolled back tournament creation by deleting tournament ID: {}", tournamentId);

            // Step 2: Revert Admin Update
            adminManagementServiceClient.removeTournamentFromAdmin(adminId, tournamentId);
            log.info("Rolled back admin update for admin ID: {}", adminId);
        } catch (Exception rollbackException) {
            log.error("Rollback failed for tournament ID {}: {}", tournamentId, rollbackException.getMessage());
        }

        
    }
    
}
