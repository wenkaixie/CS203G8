package csd.rankingdashboard.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import csd.rankingdashboard.Model.Player;

@Service
public class RankingServiceImplt implements RankingService{

    @Autowired
    private RestTemplate restTemplate;

    // first call the calculate service to get all the player ratings
    // sort the list according to rating in descending order
    @Override
    public List<Player> getRankings() {
        List<Player> allRatings = fetchAllPlayerRatings();

        // Sort players by rating in descending order
        return allRatings.stream()
                         .sorted(Comparator.comparing(Player::getElo).reversed())
                         .collect(Collectors.toList());
    }

/* UNCOMMENT THIS AFTER CALCULATE SERVICE IS DONE
    // URL for calculate service
    private static final String URL = "http://localhost:8081/calculate"; // can store the url in application.properties(externalize the url)
    
    private List<Player> fetchAllPlayerRatings(){
        // Make a GET request to CalculationService to fetch all player ratings
        ResponseEntity<PlayerRating[]> response = restTemplate.getForEntity(URL, PlayerRating[].class);
        
        // Convert the response body to a list
        return Arrays.asList(response.getBody());
    }
        */

    // Dummy data for testing
    private List<Player> fetchAllPlayerRatings() {
        // Fake data for testing purposes
        List<Player> mockRatings = Arrays.asList(
            new Player("Player1", 85, 25, "USA", 1234567890, "player1@example.com"),
            new Player("Player2", 90, 30, "Canada", 1234567891, "player2@example.com"),
            new Player("Player3", 75, 28, "UK", 1234567892, "player3@example.com"),
            new Player("Player4", 95, 22, "Australia", 1234567893, "player4@example.com")
        );
        return mockRatings;
    }
    
}
