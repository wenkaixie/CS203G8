package csd.playermanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"csd.playermanagement", "csd.shared_library"})
public class PlayerManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlayerManagementApplication.class, args);
	}

}
