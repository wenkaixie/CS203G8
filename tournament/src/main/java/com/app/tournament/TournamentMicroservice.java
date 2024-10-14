package com.app.tournament;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
// @EnableScheduling
public class TournamentMicroservice {

	public static void main(String[] args) {
		SpringApplication.run(TournamentMicroservice.class, args);
	}

}
