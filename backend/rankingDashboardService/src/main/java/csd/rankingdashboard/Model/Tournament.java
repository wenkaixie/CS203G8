package csd.rankingdashboard.Model;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Generates getters, setters, toString, equals, and hashCode methods
@NoArgsConstructor // Generates a no-argument constructor
@AllArgsConstructor // Generates an all-arguments constructor
public class Tournament {
    String name;
    String location;   
    String date;
    Map<User, Integer> participantsWithWins; // Stores participants with their corresponding wins
}
