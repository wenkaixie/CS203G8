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

    private int score;

    private int whiteGamesCount;

   
    
}
