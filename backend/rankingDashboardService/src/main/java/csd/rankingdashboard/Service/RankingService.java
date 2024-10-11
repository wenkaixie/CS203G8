package csd.rankingdashboard.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

import csd.rankingdashboard.Model.User;

public interface RankingService {
    List<User> getRankings() throws InterruptedException, ExecutionException;
    // List<User> getRankingsByTournament(String tournamentName);
}
