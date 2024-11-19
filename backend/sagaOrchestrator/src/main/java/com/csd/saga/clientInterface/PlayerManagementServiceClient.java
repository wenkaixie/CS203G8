package com.csd.saga.clientInterface;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.csd.shared_library.model.User;

@FeignClient(name = "player-management-service", url = "${player.service.url}")
public interface PlayerManagementServiceClient {

    @PostMapping("/user/{userID}/remove-tournament")
    ResponseEntity<Void> removeTournamentFromUser(@PathVariable("userID") String userId, @RequestBody String tournamentID);

    @PostMapping("/user/{userID}/add-tournament")
    ResponseEntity<Void> addTournamentToUser(@PathVariable("userID") String userId, @RequestBody String tournamentID);

    @GetMapping("/user/getUser/{userID}")
    ResponseEntity<User> getUser(@PathVariable String userID);
}
