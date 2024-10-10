package csd.rankingdashboard.Model;

import java.util.List;

import com.google.cloud.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Generates getters, setters, toString, equals, and hashCode methods
@NoArgsConstructor // Generates a no-argument constructor
@AllArgsConstructor // Generates an all-arguments constructor
public class Tournament {
    private int capacity;
    private Timestamp createdLocalDateTime;
    private String description;
    private int eloRequirement;
    private Timestamp endDateime;
    private String location;
    private List<String> participants;
    private Timestamp startDatetime;
    private String tid;
    
}
