package com.csd.saga.integration;

import com.csd.saga.AMQP.RabbitMQConfig;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SagaIntegrationTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    void testMessageSentToQueue() {
        // Test message
        String message = "admin123,tournament456";

        // Send the message
        rabbitTemplate.convertAndSend(RabbitMQConfig.DELETE_TOURNAMENT_QUEUE, message);

        // Verify the message is sent to the queue
        String receivedMessage = (String) rabbitTemplate.receiveAndConvert(RabbitMQConfig.DELETE_TOURNAMENT_QUEUE);
        assertNotNull(receivedMessage);
        assertEquals(message, receivedMessage);
    }
}