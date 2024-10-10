package com.app.tournament.model;

import com.google.cloud.firestore.annotation.PropertyName;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class User {
    
    
    private String name; // Name of the player
    private String username; // Name of the player

    private String nationality;
    private int phoneNumber;

    
    private int elo;       // Player's rank or ELO rating, if applicable
    private String email; // Contact email for the player
    private String password;
    
    @PropertyName("UID")
    private String uid;   // Unique ID for each player
    
    // Additional fields can be added as necessary
}

