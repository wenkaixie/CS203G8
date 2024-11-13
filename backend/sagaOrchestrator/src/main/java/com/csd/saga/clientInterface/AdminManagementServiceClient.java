package com.csd.saga.clientInterface;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "admin-management-service", url = "${admin.service.url}")
public interface AdminManagementServiceClient {
    

    @PostMapping("/admins/{adminID}/add-tournament")
    ResponseEntity<Void> addTournamentToAdmin(@PathVariable("adminID") String adminID, @RequestBody String tournamentID);

    @PostMapping("/admins/{adminID}/remove-tournament")
    ResponseEntity<Void> removeTournamentFromAdmin(@PathVariable("adminID") String adminID, @RequestBody String tournamentID);
}
