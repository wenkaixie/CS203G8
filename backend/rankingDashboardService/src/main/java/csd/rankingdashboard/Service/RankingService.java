package csd.rankingdashboard.Service;

import java.util.List;

import csd.rankingdashboard.Model.User;

public interface RankingService {
    List<User> getRankings();
    List<User> getRankingsByTournament(String tournamentName);
}
