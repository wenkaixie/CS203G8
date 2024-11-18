package com.csd.shared_library.DTO;


import com.csd.shared_library.enumerator.MatchResult;

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
