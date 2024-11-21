package csd.saga.service;
// package com.csd.saga.service;

// import java.util.HashMap;
// import java.util.Map;

// import org.springframework.amqp.rabbit.core.RabbitTemplate;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import com.csd.saga.AMQP.RabbitMQConfig;

// @Service
// public class SagaSenderService {

//     @Autowired
//     private RabbitTemplate rabbitTemplate;

//     public void sendDeleteTournamentEvent(String tournamentID) {
//         Map<String, Object> message = new HashMap<>();
//         message.put("tournamentID", tournamentID);
//         rabbitTemplate.convertAndSend(RabbitMQConfig.DELETE_TOURNAMENT_QUEUE, message);
//     }
// }
