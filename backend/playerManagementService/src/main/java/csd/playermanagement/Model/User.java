package csd.playermanagement.Model;

import java.util.List;

// import java.time.Instant;

import com.google.cloud.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data // Generates getters, setters, toString, equals, and hashCode methods
@NoArgsConstructor // Generates a no-argument constructor
@AllArgsConstructor // Generates an all-arguments constructor
public class User {
    private String authId;
    private Timestamp dateOfBirth;
    private int elo;
    private String email;
    private String name;
    private String nationality;
    private Integer phoneNumber;
    private List<String> registrationHistory;
    private String uid;
    private String username;
    private String chessUsername;
}


