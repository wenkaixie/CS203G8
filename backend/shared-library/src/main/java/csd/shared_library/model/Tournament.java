package csd.shared_library.model;

import java.time.Instant;

import csd.shared_library.enumerator.TournamentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
// import player from player service

@Setter 
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Tournament {
    private String adminId;
    private TournamentType type; 
    private int ageLimit;
    private String name;
    private String description;   
    private int eloRequirement;
    private String location;
    private int minSignups;
    private int capacity;
    private int prize;
    private Instant startDatetime;  
    private Instant endDatetime; 
    private String tid; // tournamentID
    private Instant createdTimestamp;
    private String status; 
    private int currentRound;
}

