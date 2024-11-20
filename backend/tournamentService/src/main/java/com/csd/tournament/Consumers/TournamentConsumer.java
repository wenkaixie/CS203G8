package com.csd.tournament.Consumers;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.csd.tournament.AMQP.RabbitMQConfig;
import com.csd.tournament.service.TournamentService;

@Component
public class TournamentConsumer {

    @Autowired
    private TournamentService tournamentService;

    @RabbitListener(queues = RabbitMQConfig.DELETE_TOURNAMENT_QUEUE)
    public void handleDeleteTournament(String message) {
        String[] parts = message.split(",");
        String adminId = parts[0];
        String tournamentId = parts[1];

        System.out.println("Received message: Admin ID " + adminId + ", Tournament ID " + tournamentId);

        try {
            // Call the service to delete the tournament
            tournamentService.deleteTournament(tournamentId);
            System.out.println("Successfully deleted Tournament ID: " + tournamentId);
        } catch (Exception e) {
            System.err.println("Failed to delete Tournament ID: " + tournamentId);
            e.printStackTrace(); // Log the stack trace for debugging
        }
    }
}