package csd.playermanagement.Model;

import java.util.List;

// import java.time.Instant;

import com.google.cloud.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String authId;
    private Timestamp dateOfBirth;
    private Integer elo;
    private String email;
    private String name;
    private String nationality;
    private Integer phoneNumber;
    private List<String> registrationHistory;
    // private String uid; // REMOVED
    private String username;
    private String chessUsername;
}


