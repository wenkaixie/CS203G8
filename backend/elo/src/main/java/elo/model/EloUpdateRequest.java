package elo.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EloUpdateRequest {

    @JsonProperty("Elo1")
    private Double Elo1;

    @JsonProperty("Elo2")
    private Double Elo2;

    @JsonProperty("AS1")
    private Double AS1;

    @JsonProperty("AS2")
    private Double AS2;

    // Getters and setters with manual validation

    public Double getElo1() {
        return Elo1;
    }

    public void setElo1(Double Elo1) throws IllegalArgumentException {
        if (Elo1 == null) {
            throw new IllegalArgumentException("Elo1 is required.");
        }
        if (Elo1 <= 0) {  // Fixed condition for Elo1
            throw new IllegalArgumentException("Elo1 must be greater than 0.");
        }
        this.Elo1 = Elo1;
    }

    public Double getElo2() {
        return Elo2;
    }

    public void setElo2(Double Elo2) throws IllegalArgumentException {
        if (Elo2 == null) {
            throw new IllegalArgumentException("Elo2 is required.");
        }
        if (Elo2 <= 0) {  // Fixed condition for Elo2
            throw new IllegalArgumentException("Elo2 must be greater than 0.");
        }
        this.Elo2 = Elo2;
    }

    public Double getAS1() {
        return AS1;
    }

    public void setAS1(Double AS1) throws IllegalArgumentException {
        if (AS1 == null) {
            throw new IllegalArgumentException("AS1 is required.");
        }
        if (!(AS1 == 0 || AS1 == 0.5 || AS1 == 1)) {  // Validation for AS1
            throw new IllegalArgumentException("AS1 must be 0, 0.5, or 1.");
        }
        this.AS1 = AS1;
    }

    public Double getAS2() {
        return AS2;
    }

    public void setAS2(Double AS2) throws IllegalArgumentException {
        if (AS2 == null) {
            throw new IllegalArgumentException("AS2 is required.");
        }
        if (!(AS2 == 0 || AS2 == 0.5 || AS2 == 1)) {  // Validation for AS2
            throw new IllegalArgumentException("AS2 must be 0, 0.5, or 1.");
        }
        this.AS2 = AS2;
    }

    // Optional: Add a method to validate all fields at once
    public void validate() throws IllegalArgumentException {
        setElo1(this.Elo1);
        setElo2(this.Elo2);
        setAS1(this.AS1);
        setAS2(this.AS2);
    }
}
