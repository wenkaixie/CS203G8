package csd.playermanagement.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data // Generates getters, setters, toString, equals, and hashCode methods
@NoArgsConstructor // Generates a no-argument constructor
@AllArgsConstructor // Generates an all-arguments constructor
public class User {
    private String age;
    private int elo;
    private String email;
    private String name;
    private String nationality;
    private String phoneNumber;
    private String uid;
    private String username;
}