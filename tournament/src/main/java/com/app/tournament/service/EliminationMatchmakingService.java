package com.app.tournament.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.app.tournament.DTO.RoundMatchDTO;
import com.app.tournament.model.User;


public class EliminationMatchmakingService {


    @Autowired
    private TournamentRoundService tournamentRoundService;

    @Autowired
    private RoundMatchService roundMatchService;

    public List<RoundMatchDTO> generateBracket(List<User> users) {
        List<RoundMatchDTO> matches = new ArrayList<>();

        int roundNumber = 1;
        List<User> advancingPlayers = users;

        // Generate matches for each round until only one winner is left
        while (advancingPlayers.size() > 1) {
            List<RoundMatchDTO> roundMatches = createRoundMatches(advancingPlayers, roundNumber);
            matches.addAll(roundMatches);
            advancingPlayers = extractWinners(roundMatches);
            roundNumber++;
        }

        return matches;
    }

    private List<RoundMatchDTO> createRoundMatches(List<User> users, int roundNumber) {
        List<RoundMatchDTO> matches = new ArrayList<>();

        for (int i = 0; i < users.size(); i += 2) {
            Player player1 = users.get(i);
            Player player2 = users.get(i + 1);

            RoundMatchDTO match = new RoundMatchDTO(
                generateMatchId(),
                "Round " + roundNumber + " - Match " + (i / 2 + 1),
                roundNumber + 1,  // nextMatchId based on the next round
                String.valueOf(roundNumber),
                LocalDate.now().toString(),
                "PENDING",
                Arrays.asList(
                    new ParticipantDTO(player1.getId(), player1.getName(), "", false),
                    new ParticipantDTO(player2.getId(), player2.getName(), "", false)
                )
            );

            matches.add(match);
        }

        return matches;
    }

    private List<Player> extractWinners(List<RoundMatchDTO> matches) {
        // For demo purposes, randomly select winners
        return matches.stream()
            .map(match -> match.getParticipants().get(0))  // Assume first player wins
            .map(participant -> new Player(participant.getId(), participant.getName()))
            .collect(Collectors.toList());
    }

    private int generateMatchId() {
        return (int) (Math.random() * 1000);  // Example ID generator, replace with your logic
    }
}
}
