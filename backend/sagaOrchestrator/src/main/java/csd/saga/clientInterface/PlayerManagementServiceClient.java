package csd.saga.clientInterface;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import csd.shared_library.model.User;

@FeignClient(name = "player-management-service", url = "${player.service.url}")
public interface PlayerManagementServiceClient {


    @GetMapping("/user/getUser/{userID}")
    ResponseEntity<User> getUser(@PathVariable String userID);

    @PostMapping("/user/{userID}/add-tournament")
    ResponseEntity<Void> addTournamentToUser(@PathVariable("userID") String userId, @RequestBody String tournamentID);

    @PostMapping("/user/{userID}/remove-tournament")
    ResponseEntity<Void> removeTournamentFromUser(@PathVariable("userID") String userId, @RequestBody String tournamentID);


    
    @GetMapping("/user/{userID}/elo")
    int getPlayerElo(@PathVariable("playerId") String playerId);

    @PutMapping("/user/{userID}/elo")
    void updatePlayerElo(@PathVariable("userID") String playerId, @RequestBody int newElo);


    @PostMapping("/user/elo/batch")
    ResponseEntity<Void> updatePlayerEloBatch(@RequestBody Map<String, Integer> eloUpdates);
}
