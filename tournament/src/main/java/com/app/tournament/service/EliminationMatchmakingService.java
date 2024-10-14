package com.app.tournament.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.app.tournament.DTO.ParticipantDTO;
import com.app.tournament.DTO.RoundMatchDTO;
import com.app.tournament.model.UserInfo;

@Service
public class EliminationMatchmakingService {

    // Generate a full tournament bracket with matches
    public List<RoundMatchDTO> generateBracket(List<UserInfo> users) {
        List<RoundMatchDTO> matches = new ArrayList<>();
        List<UserInfo> advancingPlayers = users;
        int roundNumber = 1;

        // Generate matches for each round until only one winner remains
        while (advancingPlayers.size() > 1) {
            List<RoundMatchDTO> roundMatches = createRoundMatches(advancingPlayers, roundNumber);
            matches.addAll(roundMatches);
            advancingPlayers = extractWinners(roundMatches); // Get winners for the next round
            roundNumber++;
        }

        return matches;
    }

    // Create matches for a specific round
    private List<RoundMatchDTO> createRoundMatches(List<UserInfo> users, int roundNumber) {
        List<RoundMatchDTO> matches = new ArrayList<>();

        for (int i = 0; i < users.size(); i += 2) {
            UserInfo user1 = users.get(i);
            UserInfo user2 = (i + 1 < users.size()) ? users.get(i + 1) : null; // Handle odd number of users

            // Create participants
            List<ParticipantDTO> participants = new ArrayList<>();
            participants.add(new ParticipantDTO(user1.getUid(), user1.getName(), "", false, user1.getElo()));
            if (user2 != null) {
                participants.add(new ParticipantDTO(user2.getUid(), user2.getName(), "", false, user2.getElo()));
            }

            // Create the match DTO
            RoundMatchDTO match = new RoundMatchDTO(
                    generateRoundId(roundNumber),
                    participants,
                    0.0,
                    0.0,
                    Instant.now(),
                    true, // Assume player 1 is white by default
                    null, // Next match ID will be assigned later
                    "PENDING");

            matches.add(match);
        }

        return matches;
    }

    // Extract winners from the matches (for demo purposes, assume the first
    // participant wins)
    private List<UserInfo> extractWinners(List<RoundMatchDTO> matches) {
        return matches.stream()
                .map(match -> match.getParticipants().get(0)) // Assume the first participant wins
                .map(participant -> new UserInfo(participant.getId(), participant.getName(), participant.getElo()))
                .collect(Collectors.toList());
    }

    // Generate a match ID (you can customize the logic)
    private String generateRoundId(int roundNumber) {
        return "R" + roundNumber + "-" + (int) (Math.random() * 1000);
    }
}

