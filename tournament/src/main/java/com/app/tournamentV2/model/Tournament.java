package com.app.tournamentV2.model;

import java.util.List;

import com.google.cloud.Timestamp;

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



    private int ageLimit;
    private String name;
    private String description;   
    private int eloRequirement;
    private String location;
    private int capacity;
    
    private Timestamp startDatetime;   // Field for start datetime of the tournament
    private Timestamp endDatetime; // Field for end datetime of the tournament
    
    private String tid; // tournamentID
    private Timestamp createdTimestamp;
    private int prize;

    private String status;
    
    // List to store players in the tournament
    private List<String> users; // store players uid's
    private List<Round> rounds; 

}

