package com.app.tournament.model;

import java.util.List;

import com.app.tournament.DTO.ParticipantDTO;
import com.google.cloud.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoundMatch {

    private String rmid; // Unique match ID
    private String rid; // ID of the round this match belongs to

    private List<ParticipantDTO> participants; // List of participants

    private double user1Score; // Score of player 1 (e.g., 1 = win, 0.5 = draw, 0 = loss)
    private double user2Score; // Score of player 2

    private Timestamp matchDate; // Date the match was played

    private boolean user1IsWhite; // True if player 1 plays as white, false if black

    private String nextMatchId; // Pointer to the next match (can be null)

    private String state; // State of the match (e.g., "PENDING" or "DONE")
}
