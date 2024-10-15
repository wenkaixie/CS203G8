package com.app.tournamentV2.DTO;


import java.util.List;

import com.google.cloud.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TournamentDTO {

    private int ageLimit;
    private String name;
    private String description;
    private int eloRequirement;
    private String location;
    private int capacity;
    private int prize;

    private Timestamp startDatetime; // Field for start datetime of the tournament
    private Timestamp endDatetime; // Field for end datetime of the tournament

    private List<String> users;

}