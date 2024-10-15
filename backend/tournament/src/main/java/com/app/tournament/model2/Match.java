package com.app.tournament.model2;

import java.time.Instant;
import java.util.List;

import com.app.tournament.DTO2.ParticipantDTO;

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
    private Instant startTime;
    private String state;
    private List<ParticipantDTO> participants; // List of participants

    
}
