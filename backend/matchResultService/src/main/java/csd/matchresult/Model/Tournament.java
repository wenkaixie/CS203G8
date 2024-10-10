package csd.rankingdashboard.Model;

import java.security.Timestamp;
import java.util.Map;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Generates getters, setters, toString, equals, and hashCode methods
@NoArgsConstructor // Generates a no-argument constructor
@AllArgsConstructor // Generates an all-arguments constructor
public class Tournament {
    private int capacity;
    private Timestamp createdTimestamp;
    private String description;
    private int eloRequirement;
    private Timestamp endDateTime;
    private String location;
    private List<String> participants;
    private Timestamp startDateTime;
    private String tid;
    
}
