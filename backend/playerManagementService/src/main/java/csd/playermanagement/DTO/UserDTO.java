package csd.playermanagement.DTO;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data 
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String authId;
    private String dateOfBirth; 
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


