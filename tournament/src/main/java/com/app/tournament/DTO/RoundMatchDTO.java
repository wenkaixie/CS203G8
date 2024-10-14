package com.app.tournament.DTO;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoundMatchDTO {
    private String rid; // ID of the round this match belongs to
    private List<ParticipantDTO> participants; // List of participants
    private double user1Score; // Score of player 1
    private double user2Score; // Score of player 2
    private Instant matchDate; // Date of the match
    private boolean user1IsWhite; // Whether player 1 plays as white
    private Integer nextMatchId; // ID of the next match (can be null)
    private String state; // State of the match (e.g., "PENDING" or "DONE")
}
