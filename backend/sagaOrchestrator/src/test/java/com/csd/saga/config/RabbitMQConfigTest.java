package com.csd.saga.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;

import com.csd.saga.AMQP.RabbitMQConfig;

import static org.junit.jupiter.api.Assertions.*;

public class RabbitMQConfigTest {

    @Test
    public void testDeleteTournamentQueue() {
        // Arrange
        RabbitMQConfig rabbitMQConfig = new RabbitMQConfig();

        // Act
        Queue deleteTournamentQueue = rabbitMQConfig.deleteTournamentQueue();

        // Assert
        assertNotNull(deleteTournamentQueue, "Queue should not be null");
        assertEquals("deleteTournamentQueue", deleteTournamentQueue.getName(), "Queue name should be 'delete-tournament-queue'");
        assertTrue(deleteTournamentQueue.isDurable(), "Queue should be durable");
    }
}