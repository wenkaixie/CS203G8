package csd.rankingdashboard.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import csd.rankingdashboard.Model.Player;
import csd.rankingdashboard.Service.RankingService;

@RestController
@RequestMapping("/ranking")
public class RankingController {

	@Autowired
    private RankingService rankingService;
    
    @RequestMapping("/getRanking")
    public List<Player> getRankings() {
        return rankingService.getRankings();
    }
    
}


