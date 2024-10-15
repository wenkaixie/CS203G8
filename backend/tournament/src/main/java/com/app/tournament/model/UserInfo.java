package com.app.tournament.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {

    private String uid; // Unique ID of the player
    private String name; // Name of the player
    private int elo; // Player's Elo rating at the tournament start

    // private int score;

    // private int whiteGamesCount;


    // private String id; // Unique ID of the participant
    // private String name; // Name of the participant
    // private String resultText; // Match result as text (e.g., "6")
    // private boolean isWinner; // Indicates if the participant is the winner
    // private int elo;

 
    
}
