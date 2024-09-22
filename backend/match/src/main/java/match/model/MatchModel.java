package match.model;

public class MatchModel {
    private String playerID1;
    private String playerID2;

    public MatchModel() {
    }

    public MatchModel(String playerID1, String playerID2) {
        this.playerID1 = playerID1;
        this.playerID2 = playerID2;
    }

    public String getPlayerID1() {
        return playerID1;
    }

    public void setPlayerID1(String playerID1) {
        this.playerID1 = playerID1;
    }

    public String getPlayerID2() {
        return playerID2;
    }

    public void setPlayerID2(String playerID2) {
        this.playerID2 = playerID2;
    }
}
