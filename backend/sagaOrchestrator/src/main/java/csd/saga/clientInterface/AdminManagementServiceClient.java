package csd.saga.clientInterface;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "admin-management-service", url = "${admin.service.url}")
public interface AdminManagementServiceClient {

    @PostMapping("/admin/{adminID}/add-tournament")
    ResponseEntity<Void> addTournamentToAdmin(@PathVariable("adminID") String adminID,
            @RequestBody String tournamentID);

    @PostMapping("/admin/{adminID}/remove-tournament")
    ResponseEntity<Void> removeTournamentFromAdmin(@PathVariable("adminID") String adminID,
            @RequestBody String tournamentID);

    // Validate if an admin exists by admin ID
    @GetMapping("/admin/{adminID}/validate")
    ResponseEntity<Boolean> validateAdmin(@PathVariable("adminID") String adminID);

    // Fetch all tournaments created by an admin
    @GetMapping("/admin/{adminID}/tournaments")
    ResponseEntity<List<String>> getTournamentsForAdmin(@PathVariable("adminID") String adminID);

    // Reinstate a tournament in the admin's record (for rollback scenarios)
    @PostMapping("/admin/{adminID}/reinstate-tournament")
    ResponseEntity<Void> reinstateTournamentForAdmin(@PathVariable("adminID") String adminID,
            @RequestBody String tournamentID);
}
