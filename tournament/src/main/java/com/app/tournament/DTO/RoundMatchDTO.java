package com.app.tournament.DTO;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoundMatchDTO {

    private String rid; // ID of the round this match belongs to
    private String uid1; // ID of player 1
    private String uid2; // ID of player 2
    private double user1Score; // Score of player 1 (e.g., 1 = win, 0.5 = draw, 0 = loss)
    private double user2Score; // Score of player 2
    private Instant matchDate; // Date the match was played

    private boolean user1IsWhite; // True if player 1 plays as white, false if black
}
