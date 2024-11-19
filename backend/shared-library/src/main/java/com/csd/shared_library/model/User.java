package com.csd.shared_library.model;

import java.time.Instant;
import java.util.List;

import com.google.cloud.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String authId;
    private Instant dateOfBirth;
    private int elo;
    private String email;
    private String name;
    private String nationality;
    private Integer phoneNumber;
    private List<String> registrationHistory;
    private String username;
    private String chessUsername;

    public void setDateOfBirthFromTimestamp(Timestamp timestamp) {
        if (timestamp != null) {
            this.dateOfBirth = timestamp.toDate().toInstant();
        }
    }
}

