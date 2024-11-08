package elo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EloBatchUpdateRequest {

    @JsonProperty("userIds")
    private List<String> userIds;

    @JsonProperty("results")
    private List<Double> results;
}
