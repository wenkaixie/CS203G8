package com.csd.saga.DTO;


import com.csd.saga.enumerator.MatchResult;

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
