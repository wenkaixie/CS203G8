package csd.rankingdashboard.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import csd.rankingdashboard.Model.Tournament;
import csd.rankingdashboard.Model.User;

@Service
public class RankingServiceImplt implements RankingService{

    @Autowired
    private RestTemplate restTemplate;

    // first call the calculate service to get all the player ratings
    // sort the list according to rating in descending order
    @Override
    public List<User> getRankings() {
        List<User> allRatings = fetchAllPlayerRatings();

        // Sort players by rating in descending order
        return allRatings.stream()
                         .sorted(Comparator.comparing(User::getElo).reversed())
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

    @Override
   public List<User> getRankingsByTournament(String tournamentName) {
        List<Tournament> allTournaments = fetchAllTournamentsWithParticipants();

        // Find the tournament by name
        Tournament tournament = allTournaments.stream()
                                              .filter(t -> t.getName().equalsIgnoreCase(tournamentName))
                                              .findFirst()
                                              .orElse(null);

        if (tournament == null) {
            // Handle case where the tournament is not found
            return null;
        }

        // Sort users by number of wins in descending order
        return tournament.getParticipantsWithWins().entrySet().stream()
                         .sorted(Map.Entry.<User, Integer>comparingByValue().reversed())
                         .map(Map.Entry::getKey)
                         .collect(Collectors.toList());
    }





    // Dummy data for testing
    private List<User> fetchAllPlayerRatings() {
        // Fake data for testing purposes
        List<User> mockRatings = Arrays.asList(
            new User("Player5", 88, 27, "Germany", "1234567894", "player5@example.com"),
            new User("Player6", 92, 29, "France", "1234567895", "player6@example.com"),
            new User("Player7", 78, 26, "Italy", "1234567896", "player7@example.com"),
            new User("Player8", 85, 24, "Spain", "1234567897", "player8@example.com")
        );
        return mockRatings;
    }
    
     // Dummy data for testing
     private List<Tournament> fetchAllTournamentsWithParticipants() {
        List<Tournament> mockTournaments = Arrays.asList(
            new Tournament("Tournament A", "Location A", "2023-09-01", Map.of(
                new User("Player1", 88, 27, "USA", "1234567890", "player1@example.com"), 10,
                new User("Player2", 85, 25, "UK", "1234567891", "player2@example.com"), 8,
                new User("Player3", 90, 29, "Canada", "1234567892", "player3@example.com"), 15
            )),
            new Tournament("Tournament B", "Location B", "2023-09-05", Map.of(
                new User("Player4", 80, 24, "Germany", "1234567893", "player4@example.com"), 12,
                new User("Player5", 75, 22, "France", "1234567894", "player5@example.com"), 7,
                new User("Player6", 82, 26, "Italy", "1234567895", "player6@example.com"), 9
            )),
            new Tournament("Tournament C", "Location C", "2023-09-10", Map.of(
                new User("Player7", 78, 24, "Spain", "1234567896", "player7@example.com"), 5,
                new User("Player8", 88, 27, "Portugal", "1234567897", "player8@example.com"), 11,
                new User("Player9", 73, 23, "Brazil", "1234567898", "player9@example.com"), 3
            ))
        );
        return mockTournaments;
    }


}
