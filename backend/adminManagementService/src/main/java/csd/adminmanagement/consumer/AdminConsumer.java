package csd.adminmanagement.consumer;


import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import csd.adminmanagement.AMQP.RabbitMQConfig;
import csd.adminmanagement.service.AdminService;


@Component
public class AdminConsumer {

    @Autowired
    private AdminService adminService;

    @RabbitListener(queues = RabbitMQConfig.DELETE_TOURNAMENT_QUEUE)
    public void handleDeleteTournament(String message) {
        String[] parts = message.split(",");
        String adminId = parts[0];
        String tournamentId = parts[1];

        System.out.println("Received message: Admin ID " + adminId + ", Tournament ID " + tournamentId);

        try {
            // Call the service to remove the tournament from the admin
            adminService.removeTournamentFromAdmin(adminId, tournamentId);
            System.out.println("Successfully removed Tournament ID: " + tournamentId + " from Admin ID: " + adminId);
        } catch (Exception e) {
            System.err.println("Failed to remove Tournament ID " + tournamentId + " from Admin ID " + adminId);
            e.printStackTrace(); // Log the full stack trace for debugging
        }
    }
}