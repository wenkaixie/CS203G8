package com.csd.saga.clientInterface;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "player-management-service", url = "${player.service.url}")
public interface PlayerManagementServiceClient {

    @PostMapping("/users/{userID}/remove-tournament")
    void removeTournamentFromUser(@PathVariable("userID") String userId, @RequestBody String tournamentID);

    @PostMapping("/users/{userID}/add-tournament")
    void addTournamentToUser(@PathVariable("userID") String userId, @RequestBody String tournamentID);
}
