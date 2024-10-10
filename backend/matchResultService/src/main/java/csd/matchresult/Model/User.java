package csd.rankingdashboard.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data // Generates getters, setters, toString, equals, and hashCode methods
@NoArgsConstructor // Generates a no-argument constructor
@AllArgsConstructor // Generates an all-arguments constructor
public class User {
    private String Name;
    private int Elo;
    private int Age;
    private String Nationality;
    private String PhoneNumber;
    private String Email;
}