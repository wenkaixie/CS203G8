package com.example.elo.controller;

public class EloUpdateRequest {
    private double Elo1;
    private double Elo2;
    private double AS1;
    private double AS2;

    public double getElo1() {
        return Elo1;
    }

    public void setElo1(double Elo1) {
        this.Elo1 = Elo1;
    }

    public double getElo2() {
        return Elo2;
    }

    public void setElo2(double Elo2) {
        this.Elo2 = Elo2;
    }

    public double getAS1() {
        return AS1;
    }

    public void setAS1(double AS1) {
        this.AS1 = AS1;
    }

    public double getAS2() {
        return AS2;
    }

    public void setAS2(double AS2) {
        this.AS2 = AS2;
    }
}
