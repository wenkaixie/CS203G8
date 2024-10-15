package com.app.tournament.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantDTO {
    private String id; // Unique ID of the participant (user ID)
    private String name; // Name of the participant
    private String resultText; // Textual result of the match (e.g., "6" or "Win")
    private boolean isWinner; // Whether the participant won the match
    private int elo; // Elo rating of the participant
}
