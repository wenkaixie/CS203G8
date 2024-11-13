package com.csd.saga.clientInterface;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "tournamentServiceClient", url = "${tournament.service.url}")
public interface TournamentServiceClient {
    
    @DeleteMapping("/tournaments/{tournamentID}")
    ResponseEntity<Void> deleteTournament(@PathVariable("tournamentID") String tournamentID);

    @GetMapping("/tournaments/{tournamentID}/admin")
    String getAdminIdForTournament(@PathVariable("tournamentID") String tournamentID);

    @GetMapping("/tournaments/{tournamentID}/users")
    List<String> getUserIdsForTournament(@PathVariable("tournamentID") String tournamentID);

    @PostMapping("/tournaments/{tournamentID}/reinstate")
    void reinstateTournament(@PathVariable("tournamentID") String tournamentID);
}
