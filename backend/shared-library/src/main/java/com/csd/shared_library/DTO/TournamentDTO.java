package com.csd.shared_library.DTO;


import java.time.Instant;

import com.csd.shared_library.enumerator.TournamentType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TournamentDTO {
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
}