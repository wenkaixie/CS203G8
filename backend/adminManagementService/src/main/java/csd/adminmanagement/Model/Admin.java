package csd.adminmanagement.Model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Admin {
    private String authId;
    private String email;
    private String name;
    private String country;
    private Integer phoneNumber;
    private List<String> tournamentCreated;
    private String username;
}


