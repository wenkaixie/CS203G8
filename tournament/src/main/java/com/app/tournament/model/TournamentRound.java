package com.app.tournament.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TournamentRound {

    private String trid; // Unique ID of the round
    private String tid; // ID of the tournament this round belongs to
    private int roundNumber; // Number of the round
    private List<String> mids; // List of match IDs for the round
}
