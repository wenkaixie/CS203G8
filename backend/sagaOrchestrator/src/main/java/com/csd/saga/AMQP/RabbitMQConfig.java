package com.csd.saga.AMQP;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    public static final String DELETE_TOURNAMENT_QUEUE = "deleteTournamentQueue";
    public static final String DELETE_TOURNAMENT_RESPONSE_QUEUE = "deleteTournamentResponseQueue";

    @Bean
    public Queue deleteTournamentQueue() {
        return new Queue(DELETE_TOURNAMENT_QUEUE, true); // 'true' for durable queue
    }

    @Bean
    public Queue deleteTournamentResponseQueue() {
        return new Queue(DELETE_TOURNAMENT_RESPONSE_QUEUE, true); // 'true' for durable queue
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }

}
