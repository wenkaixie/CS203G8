package csd.rankingdashboard.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
    public List<User> getRankings() {
        return rankingService.getRankings();
    }
    
    // create a ranking by tournament (most number of wins?)
    @GetMapping("/tournamentRank")
    public List<User> getRankingsByTournament(@RequestParam String tournamentName) {
        return rankingService.getRankingsByTournament(tournamentName);
    }
}

// http://localhost:8080/rank/tournamentRank?tournamentName=Tournament A
// http://localhost:8080/rank/tournamentRank?tournamentName=Tournament A

