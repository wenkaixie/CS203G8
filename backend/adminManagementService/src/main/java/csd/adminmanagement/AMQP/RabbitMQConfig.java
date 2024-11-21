package csd.adminmanagement.AMQP;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    public static final String DELETE_TOURNAMENT_QUEUE = "deleteTournamentQueue";

    @Bean
    public Queue deleteTournamentQueue() {
        return new Queue(DELETE_TOURNAMENT_QUEUE, true); // Durable queue
    }
}