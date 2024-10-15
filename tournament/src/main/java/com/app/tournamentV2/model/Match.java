package com.app.tournamentV2.model;

import java.util.List;

import com.app.tournamentV2.DTO.ParticipantDTO;
import com.google.cloud.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Match {


    private int id; // id of each match from 1 to N
    private String name;
    private int nextMatchId; // pointer to next match
    private int tournamentRoundText;
    private Timestamp startTime;
    private String state;
    private List<ParticipantDTO> participants; // List of participants

    
}
