package com.csd.saga.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ParticipantDTO {

    private String id; // player 1 or player 2
    private String authId; //players Uid
    private String name; // Name of the participant
    private String resultText; // Textual result of the match (e.g., "6" or "Win")

    private int elo;
    private String nationality;

    @JsonProperty("isWinner") // Ensure the field is serialized as 'isWinner'
    private boolean isWinner; // Whether the participant won the match

    // Explicit getter to match the `@JsonProperty` annotation
    @JsonProperty("isWinner")
    public boolean getIsWinner() {
        return isWinner;
    }

    @JsonProperty("isWinner")
    public void setIsWinner(boolean isWinner) {
        this.isWinner = isWinner;
    }
 
}
