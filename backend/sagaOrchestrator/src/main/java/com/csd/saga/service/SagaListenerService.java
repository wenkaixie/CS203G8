package com.csd.saga.service;

import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class SagaListenerService {

    @RabbitListener(queues = "deleteTournamentResponseQueue")
    public void handleDeleteTournamentResponse(Map<String, Object> message) {
        String tournamentID = (String) message.get("tournamentID");
        String status = (String) message.get("status");

        if ("SUCCESS".equals(status)) {
            // Proceed with the next step in the saga
            System.out.println("Tournament deletion confirmed for ID: " + tournamentID);
        } else {
            // Trigger compensation if deletion failed
            System.out.println("Failed to delete tournament with ID: " + tournamentID);
        }
    }
}
