package csd.tournament;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"csd.tournament", "csd.shared_library"})
public class TournamentMicroservice {

	public static void main(String[] args) {
		SpringApplication.run(TournamentMicroservice.class, args);
	}

}
