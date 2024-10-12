package csd.matchresult.Model;
import com.google.cloud.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data // Generates getters, setters, toString, equals, and hashCode methods
@NoArgsConstructor // Generates a no-argument constructor
@AllArgsConstructor // Generates an all-arguments constructor
public class Matches {
    private Timestamp matchDate;
    private String rid;
    private String rmid;
    private String uid1;
    private String uid2;
    private int user1Score; // Score of player 1 (e.g., 1 = win, 0.5 = draw, 0 = loss)
    private int user2Score;
    private boolean userIsWhite; // True if player 1 plays as white, false if black
}
