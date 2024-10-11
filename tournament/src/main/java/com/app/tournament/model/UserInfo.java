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

    private String UserId; // Unique ID of the player
    private String name; // Name of the player
    private double elo; // Player's Elo rating at the tournament start
    
}
