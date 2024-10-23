package csd.adminmanagement.Model;


import java.util.List;

import com.google.cloud.Timestamp;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Generates getters, setters, toString, equals, and hashCode methods
@NoArgsConstructor // Generates a no-argument constructor
@AllArgsConstructor // Generates an all-arguments constructor
public class Tournament {
    private int ageLimit;
    private int capacity;
    private Timestamp createdTimestamp;
    private String description;
    private int eloRequirement;
    private Timestamp endDatetime;
    private String location;
    private String name;
    private int prize;
    private Timestamp startDatetime;
    private String status;
    private String tid;
    private String trid;
    private List<String> users;
}

