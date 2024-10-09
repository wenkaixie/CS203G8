package com.app.tournament.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.tournament.model.User;
import com.app.tournament.model.RoundMatch;
import com.app.tournament.model.TournamentRound;
import com.app.tournament.DTO.RoundMatchDTO;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class MatchmakingService {

    @Autowired
    private Firestore firestore;

    @Autowired
    private TournamentRoundService tournamentRoundService;

    @Autowired
    private RoundMatchService roundMatchService;

    // Method to create pairings for a round in a tournament
    public List<RoundMatch> createPairingsForRound(String tournamentId, int roundNumber) throws Exception {
        try {
            // Fetch Users for the tournament
            List<User> users = getUsersByTournamentId(tournamentId);

            // Initialize pairings list and paired Users set
            List<RoundMatch> pairings = new ArrayList<>();
            Set<String> pairedUsers = new HashSet<>();

            // For the first round, pair by Elo
            if (roundNumber == 1) {
                // Sort Users by Elo rating
                Collections.sort(users, Comparator.comparing(User::getElo).reversed());

                // Pair top half against the bottom half
                for (int i = 0; i < users.size() / 2; i++) {
                    User User1 = users.get(i); // Top half User
                    User User2 = users.get(i + users.size() / 2); // Bottom half User

                    // Assign white/black randomly or based on historical imbalance
                    boolean User1IsWhite = assignWhiteBlack(User1, User2);

                    // Create and store match
                    RoundMatch match = createMatch(User1, User2, User1IsWhite, roundNumber);
                    pairings.add(match);

                    pairedUsers.add(User1.getUid());
                    pairedUsers.add(User2.getUid());
                }

            } else {
                // For rounds after the first, pair Users by score
                Collections.sort(users, Comparator.comparing(User::getScore).reversed());

                // Get a set of previous matchups to avoid rematches
                Set<String> previousMatchups = getPreviousMatchups(tournamentId, roundNumber);

                // Pair Users by score and avoid rematches
                for (int i = 0; i < users.size(); i++) {
                    User User1 = users.get(i);

                    if (pairedUsers.contains(User1.getUid())) {
                        continue; // Skip if User is already paired
                    }

                    boolean paired = false;

                    for (int j = i + 1; j < users.size(); j++) {
                        User User2 = users.get(j);

                        // Ensure Users haven't been paired before
                        if (!pairedUsers.contains(User2.getUid())
                                && !previousMatchups.contains(User1.getUid() + "_" + User2.getUid())) {
                            // Assign colors and create a match
                            boolean User1IsWhite = assignWhiteBlack(User1, User2);
                            RoundMatchDTO match = createMatch(User1, User2, User1IsWhite, roundNumber);
                            pairings.add(match);

                            // Mark Users as paired
                            pairedUsers.add(User1.getUid());
                            pairedUsers.add(User2.getUid());
                            paired = true;
                            break;
                        }
                    }

                    // If no pair found, assign a bye
                    if (!paired) {
                        assignBye(User1, roundNumber);
                        pairedUsers.add(User1.getUid());
                    }
                }
            }

            // Save all pairings in Firestore
            for (RoundMatch match : pairings) {
                roundMatchService.createRoundMatch(match); // Save each match to Firestore
            }

            return pairings;

        } catch (Exception e) {
            throw new Exception("Error creating pairings for round: " + e.getMessage(), e);
        }
    }

    // Helper method to assign white/black colors based on previous rounds or random
    private boolean assignWhiteBlack(User User1, User User2) {
        // A simple random assignment or use more advanced logic based on color
        // imbalance
        return Math.random() < 0.5; // Randomly assign white/black
    }

    // Helper method to create a match
    private RoundMatch createMatch(User User1, User User2, boolean User1IsWhite, int roundNumber) {
        RoundMatch match = new RoundMatch();
        match.setUid1(User1.getUid());
        match.setUid2(User2.getUid());
        match.setUser1IsWhite(User1IsWhite);
        match.setRoundId(String.valueOf(roundNumber));
        match.setUser1Score(0); // Initialize score to 0
        match.setUser2Score(0); // Initialize score to 0
        match.setMatchDate(new Date().toInstant());
        return match;
    }

    // Helper method to assign a bye
    private void assignBye(User User, int roundNumber) {
        System.out.println("Assigning a bye to User: " + User.getId());
        User.setScore(User.getScore() + 1); // Award 1 point for the bye
    }

    // Fetch previous matchups to avoid rematches
    private Set<String> getPreviousMatchups(String tournamentId, int roundNumber)
            throws ExecutionException, InterruptedException {
        Set<String> previousMatchups = new HashSet<>();
        for (int i = 1; i < roundNumber; i++) {
            ApiFuture<QuerySnapshot> future = firestore.collection("RoundMatches")
                    .whereEqualTo("roundId", String.valueOf(i))
                    .get();
            List<RoundMatch> matches = future.get().toObjects(RoundMatch.class);
            for (RoundMatch match : matches) {
                previousMatchups.add(match.getUid1() + "_" + match.getUid2());
                previousMatchups.add(match.getUid2() + "_" + match.getUid1());
            }
        }
        return previousMatchups;
    }

    // Fetch Users by tournament ID
    private List<User> getUsersByTournamentId(String tournamentId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection("Tournaments")
                .document(tournamentId)
                .collection("Users")
                .get();
        return future.get().toObjects(User.class);
    }
}
