package com.app.tournament.DTO;

import java.util.List;

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
public class TournamentRoundDTO {

    private String tid; // ID of the tournament this round belongs to
    private int roundNumber; // The round number within the tournament
    private List<String> mids; // List of match IDs (can be empty when creating a new round)
}
