package csd.rankingdashboard.Controllers;

import java.util.List;
import java.util.concurrent.ExecutionException;
import org.springframework.http.HttpStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import csd.rankingdashboard.Model.User;
import csd.rankingdashboard.Service.RankingService;

@RestController
@RequestMapping("/rank")
public class RankingController {

	@Autowired
    private RankingService rankingService;
    
    @GetMapping("/globalRank")
    public ResponseEntity<List<User>> getRankings() {
        try {
            List<User> rankings = rankingService.getRankings();
            return ResponseEntity.ok(rankings); // Return HTTP 200 with the list of users
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            // Return HTTP 500 Internal Server Error with a custom message
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    // create a ranking by tournament (most number of wins?)
    // @GetMapping("/tournamentRank")
    // public List<User> getRankingsByTournament(@RequestParam String tournamentName) {
    //     return rankingService.getRankingsByTournament(tournamentName);
    // }
}

// http://localhost:8080/rank/tournamentRank?tournamentName=Tournament A
// http://localhost:8080/rank/tournamentRank?tournamentName=Tournament A

