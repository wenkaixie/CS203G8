package com.csd.shared_library.model;

import java.time.Instant;
import java.util.List;

import com.csd.shared_library.DTO.ParticipantDTO;
import com.csd.shared_library.enumerator.MatchResult;

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
    private MatchResult result; // Use the MatchResult enum for match outcome
    private List<ParticipantDTO> participants; 

    /// Method to update the result
    public void updateResult(MatchResult result) {
        this.result = result;
    }

    public boolean isDraw() {
        return this.result == MatchResult.DRAW;
    }
}
