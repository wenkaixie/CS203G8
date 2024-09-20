package csd.rankingdashboard.Model;

public class Player {
    private String name;
    private int elo;
    private int age;
    private String nationality;
    private int phoneNumber;
    private String email;

    // Constructors, getters, and setters
    public Player() {}

    public Player(String name, int elo, int age, String nationality, int phoneNumber, String email) {
        this.name = name;
        this.elo = elo;
        this.age = age;
        this.nationality = nationality;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getElo() {
        return elo;
    }

    public void setElo(int elo) {
        this.elo = elo;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getNationality() {
        return this.nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public int getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(int phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}