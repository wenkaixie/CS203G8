package csd.adminmanagement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"csd.adminmanagement", "csd.shared_library"})
public class AdminManagementService {
        
    public static void main(String[] args) {
        SpringApplication.run(AdminManagementService.class, args);
    }
}
