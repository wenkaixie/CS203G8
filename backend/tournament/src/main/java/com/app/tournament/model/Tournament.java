package com.app.tournament.model;

import java.time.Instant;

import com.app.tournament.enumerator.TournamentType;

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
    private TournamentType type; // classic, blitz etcx1

    private int ageLimit;
    private String name;
    private String description;   
    private int eloRequirement;
    private String location;
    private int minSignups;
    private int capacity;
    private int prize;
    
    private Instant startDatetime;   // Field for start datetime of the tournament
    private Instant endDatetime; // Field for end datetime of the tournament
    
    private String tid; // tournamentID
    private Instant createdTimestamp;
    private String status; // active, completed, cancelled
    private int currentRound;
}

