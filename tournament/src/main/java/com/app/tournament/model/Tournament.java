package com.app.tournament.model;

import java.time.Instant;
import java.util.List;

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

    // private String adminID;
    // private boolean status; // denotes of torunament is open. however, should toggle based on max capacity and/or cut off based
                                // on dateTime
    // private String type; // classic, blitz etcx




    private String name;
    private String description;   
    private int eloRequirement;
    private String location;
    private int capacity;
    
    private Instant startDatetime;   // Field for start datetime of the tournament
    private Instant endDatetime; // Field for end datetime of the tournament
    
    private String tid; // tournamentID
    private Instant createdTimestamp;

    private String trid; // need to

    
    
    // List to store players in the tournament
    private List<String> participants; // store players uid's

   
}

