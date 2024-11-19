package com.csd.saga.clientInterface;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.csd.shared_library.DTO.TournamentDTO;
import com.csd.shared_library.model.Tournament;



@FeignClient(name = "tournamentServiceClient", url = "${tournament.service.url}")
public interface TournamentServiceClient {
    
    @PostMapping("/api/tournaments")
    ResponseEntity<String> createTournament(@RequestBody TournamentDTO tournamentDTO);
    
    @DeleteMapping("/api/tournaments/{tournamentID}")
    ResponseEntity<Void> deleteTournament(@PathVariable("tournamentID") String tournamentID);

    @GetMapping("/api/tournaments/{tournamentID}")
    ResponseEntity<Tournament> getTournament(@PathVariable String tournamentID);

    @GetMapping("/api/tournaments/{tournamentID}/admin")
    String getAdminIdForTournament(@PathVariable("tournamentID") String tournamentID);

    @GetMapping("/api/tournaments/{tournamentID}/userIDs")
    List<String> getUserIdsForTournament(@PathVariable("tournamentID") String tournamentID);

    @PostMapping("/api/tournaments/{tournamentID}/players")
    ResponseEntity<String> addUserToTournament(@PathVariable String tournamentID,@RequestParam String userID);

    @DeleteMapping("/api/tournaments/{tournamentID}/players/{userID}")
    ResponseEntity<Void> removeUserFromTournament(@PathVariable String tournamentID,@PathVariable String userID);



    @PostMapping("/api/tournaments/{tournamentID}/reinstate")
    void reinstateTournament(@PathVariable("tournamentID") String tournamentID);
}