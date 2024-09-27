package elo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EloUpdateRequest {

    // @JsonProperty("Elo1")
    // private Double Elo1;

    // @JsonProperty("Elo2")
    // private Double Elo2;

    @JsonProperty("AS1")
    private int AS1;

    @JsonProperty("AS2")
    private int AS2;
}
