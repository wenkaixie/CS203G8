package com.app.tournament.DTO;


import java.util.List;

import com.google.cloud.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoundMatchDTO {

    private String rmid; // ID of the round this match belongs to
    private List<ParticipantDTO> participants; // List of participants

    
    private double user1Score; // Score of player 1
    private double user2Score; // Score of player 2
    private Timestamp matchDate; // Date of the match
    private boolean user1IsWhite; // Whether player 1 plays as white
    private String nextMatchId; // ID of the next match (can be null)
    private String state; // State of the match (e.g., "PENDING" or "DONE")
}
