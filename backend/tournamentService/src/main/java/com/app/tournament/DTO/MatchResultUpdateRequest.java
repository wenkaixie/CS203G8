package com.app.tournament.DTO;

import com.app.tournament.enumerator.MatchResult;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MatchResultUpdateRequest {
    private MatchResult matchResult;
    private String player1Id;
    private String player2Id;
}
