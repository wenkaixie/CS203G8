package com.app.tournament.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.tournament.DTO.ParticipantDTO;
import com.app.tournament.DTO.RoundMatchDTO;
import com.app.tournament.DTO.TournamentRoundDTO;
import com.app.tournament.model.RoundMatch;
import com.app.tournament.model.UserInfo;
import com.google.cloud.Timestamp;

@Service("eliminationMatchmakingService1")
public class EliminationMatchmakingService {

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private TournamentRoundService tournamentRoundService;

    @Autowired
    private RoundMatchService roundMatchService;

    // Generate a full tournament bracket with matches and save them in Firestore
    public void generateAndSaveBracket(String tournamentId, List<UserInfo> users) throws Exception {
        List<UserInfo> advancingPlayers = users;
        int roundNumber = 1;

        List<String> previousRoundMatchIds = new ArrayList<>();

        // Generate matches for each round until only one winner remains
        while (advancingPlayers.size() > 1) {
            // Create a new round
            String roundId = createTournamentRound(tournamentId, roundNumber);

            // Generate matches for this round and save them to Firestore
            List<String> currentRoundMatchIds = createAndSaveRoundMatches(roundId, advancingPlayers, roundNumber);

            // Set match pointers between rounds if necessary
            if (roundNumber > 1) {
                tournamentRoundService.setMatchPointers(previousRoundMatchIds, currentRoundMatchIds);
            }

            // Update advancing players and prepare for the next round
            advancingPlayers = extractWinners(currentRoundMatchIds);
            previousRoundMatchIds = currentRoundMatchIds;
            roundNumber++;
        }
    }

    // Create a new round in Firestore and return its ID
    private String createTournamentRound(String tournamentId, int roundNumber) throws Exception {
        TournamentRoundDTO roundDTO = new TournamentRoundDTO(tournamentId, roundNumber, new ArrayList<>());
        return tournamentRoundService.createTournamentRound(roundDTO);
    }

    // Generate and save matches for a specific round, returning their IDs
    private List<String> createAndSaveRoundMatches(String roundId, List<UserInfo> players, int roundNumber)
            throws Exception {
        List<String> matchIds = new ArrayList<>();

        for (int i = 0; i < players.size(); i += 2) {
            UserInfo player1 = players.get(i);
            UserInfo player2 = (i + 1 < players.size()) ? players.get(i + 1) : null;

            // Create participants
            List<ParticipantDTO> participants = new ArrayList<>();
            participants.add(new ParticipantDTO(player1.getUid(), player1.getName(), "", false, player1.getElo()));
            if (player2 != null) {
                participants.add(new ParticipantDTO(player2.getUid(), player2.getName(), "", false, player2.getElo()));
            }

            // Create the match DTO
            RoundMatchDTO matchDTO = new RoundMatchDTO(
                    null, // Firestore will generate the match ID
                    participants,
                    0.0, // Default score for player 1
                    0.0, // Default score for player 2
                    Timestamp.now(), // Match date
                    true, // Assume player 1 is white
                    null, // Next match ID assigned later
                    "PENDING" // Initial match state
            );

            // Save the match using RoundMatchService and collect its ID
            String matchId = roundMatchService.createRoundMatch(matchDTO);
            matchIds.add(matchId);
        }

        return matchIds;
    }

    // Extract winners from the matches (for now, assume the first player wins)
    public List<UserInfo> extractWinners(List<String> matchIds) throws Exception {
        List<UserInfo> winners = new ArrayList<>();

        for (String matchId : matchIds) {
            // Retrieve the match from Firestore
            RoundMatch match = roundMatchService.getMatchById(matchId);

            // Convert RoundMatch to RoundMatchDTO
            RoundMatchDTO matchDTO = convertToDTO(match);

            // Assume the first participant wins
            ParticipantDTO winner = matchDTO.getParticipants().get(0);
            winners.add(new UserInfo(winner.getId(), winner.getName(), winner.getElo()));
        }

        return winners;
    }

    // Helper method to convert RoundMatch to RoundMatchDTO
    private RoundMatchDTO convertToDTO(RoundMatch match) {
        return new RoundMatchDTO(
                match.getRmid(),
                match.getParticipants(),
                match.getUser1Score(),
                match.getUser2Score(),
                match.getMatchDate(),
                match.isUser1IsWhite(),
                null, // Next match ID (if needed)
                "PENDING" // Initial match state (can be adjusted)
        );
    }

}

