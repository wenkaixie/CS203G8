package com.app.tournament.DTO;


import java.time.Instant;
import java.util.List;

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

    private Instant startDatetime; // Field for start datetime of the tournament
    private Instant endDatetime; // Field for end datetime of the tournament

    private List<String> users;

}