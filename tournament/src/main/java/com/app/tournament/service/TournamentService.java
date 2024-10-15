package com.app.tournament.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.tournament.DTO.TournamentDTO;
import com.app.tournament.DTO.TournamentRoundDTO;
import com.app.tournament.model.Tournament;
import com.app.tournament.model.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;

@Service
public class TournamentService {

    @Autowired
    private Firestore firestore;

    @Autowired
    private TournamentRoundService roundService;

    // Create a new tournament
    public String createTournament(TournamentDTO tournamentDTO) throws Exception {
        try {
            DocumentReference newTournamentRef = firestore.collection("Tournaments").document();
            Tournament tournament = populateTournament(tournamentDTO, newTournamentRef.getId());

            // Save the tournament in Firestore
            ApiFuture<WriteResult> futureTournament = newTournamentRef.set(tournament);
            WriteResult result = futureTournament.get();
            System.out.println("Tournament created at: " + result.getUpdateTime());

            // Create rounds based on tournament capacity
            int numberOfRounds = calculateRounds(tournament.getCapacity());
            createTournamentRounds(tournament.getTid(), numberOfRounds);

            return tournament.getTid();

        } catch (InterruptedException | ExecutionException e) {
            throw new Exception("Error creating tournament: " + e.getMessage(), e);
        }
    }


    // this is bad, should be using create tournament method in controller
    // Populate Tournament object
    public Tournament populateTournament(TournamentDTO dto, String tournamentId) {
        Tournament tournament = new Tournament();
        tournament.setTid(tournamentId);
        tournament.setName(dto.getName());
        tournament.setDescription(dto.getDescription());
        tournament.setEloRequirement(dto.getEloRequirement());
        tournament.setLocation(dto.getLocation());
        tournament.setStartDatetime(dto.getStartDatetime());
        tournament.setEndDatetime(dto.getEndDatetime());
        tournament.setCapacity(dto.getCapacity());
        tournament.setPrize(dto.getPrize());
        tournament.setStatus("Registration Open");
        tournament.setCreatedTimestamp(Timestamp.now());
        tournament.setAgeLimit(dto.getAgeLimit());
        tournament.setUsers(new ArrayList<>());
        return tournament;
    }

    // Calculate number of rounds needed
    private int calculateRounds(int capacity) {
        int rounds = 0;
        while (capacity > 1) {
            capacity /= 2;
            rounds++;
        }
        return rounds;
    }

    // Create tournament rounds
    public void createTournamentRounds(String tournamentId, int numberOfRounds) throws Exception {
        for (int i = 1; i <= numberOfRounds; i++) {
            TournamentRoundDTO roundDTO = new TournamentRoundDTO();
            roundDTO.setTid(tournamentId);
            roundDTO.setRoundNumber(i);
            roundDTO.setMids(new ArrayList<>());

            roundService.createTournamentRound(roundDTO);
        }
    }

    // Retrieve a tournament by ID
    public Tournament getTournamentById(String tournamentID) throws Exception {
        DocumentReference tournamentRef = firestore.collection("Tournaments").document(tournamentID);
        DocumentSnapshot document = tournamentRef.get().get();

        if (document.exists()) {
            return document.toObject(Tournament.class);
        } else {
            throw new Exception("Tournament not found with ID: " + tournamentID);
        }
    }

    // Get all tournaments
    public List<Tournament> getAllTournaments() throws InterruptedException, ExecutionException {
        List<QueryDocumentSnapshot> documents = firestore.collection("Tournaments").get().get().getDocuments();
        return documents.stream().map(doc -> doc.toObject(Tournament.class)).collect(Collectors.toList());
    }

    // Update tournament details
    public String updateTournament(String tournamentID, TournamentDTO updatedTournament)
            throws InterruptedException, ExecutionException {
        DocumentReference tournamentRef = firestore.collection("Tournaments").document(tournamentID);
        Map<String, Object> updates = new HashMap<>();

        updates.put("name", updatedTournament.getName());
        updates.put("description", updatedTournament.getDescription());
        updates.put("eloRequirement", updatedTournament.getEloRequirement());
        updates.put("location", updatedTournament.getLocation());
        updates.put("startDatetime", (updatedTournament.getStartDatetime()));
        updates.put("endDatetime", (updatedTournament.getEndDatetime()));
        updates.put("prize", updatedTournament.getPrize());

        tournamentRef.update(updates).get();
        return "Tournament updated successfully.";
    }

    // Helper method to convert LocalDateTime to Timestamp
    private Timestamp convertToTimestamp(LocalDateTime dateTime) {
        Instant instant = dateTime.atZone(ZoneId.systemDefault()).toInstant();
        return Timestamp.of(Date.from(instant));
    }

    // Delete a tournament
    public void deleteTournament(String tournamentID) throws InterruptedException, ExecutionException {
        firestore.collection("Tournaments").document(tournamentID).delete().get();
    }

    // Add user to tournament
    public String addUserToTournament(String tournamentID, String userID) throws InterruptedException, ExecutionException {
        DocumentReference tournamentRef = firestore.collection("Tournaments").document(tournamentID);
        tournamentRef.update("users", FieldValue.arrayUnion(userID)).get();
        return "Player added successfully.";
    }

    // Remove user from tournament
    public String removeUserFromTournament(String tournamentID, String userID) throws InterruptedException, ExecutionException {
        DocumentReference tournamentRef = firestore.collection("Tournaments").document(tournamentID);
        tournamentRef.update("users", FieldValue.arrayRemove(userID)).get();
        return "Player removed successfully.";
    }

    // Get eligible tournaments for a user
    public List<Tournament> getEligibleTournamentsOfUser(String userID) throws InterruptedException, ExecutionException {
        User user = getUserInfo(userID);
        if (user == null) return new ArrayList<>();

        Timestamp now = Timestamp.now();
        List<QueryDocumentSnapshot> documents = firestore.collection("Tournaments").get().get().getDocuments();

        return documents.stream()
                .map(doc -> doc.toObject(Tournament.class))
                .filter(tournament -> isEligible(user, tournament, now))
                .collect(Collectors.toList());
    }

    // Check if user is eligible for a tournament
   public boolean isEligible(User user, Tournament tournament, Timestamp now) {
    if (now.compareTo(tournament.getStartDatetime()) >= 0) return false;  // Now is after or equal to start time
    if (tournament.getUsers().size() >= tournament.getCapacity()) return false;
    if (user.getElo() < tournament.getEloRequirement()) return false;
    int userAge = calculateAge(user.getDateOfBirth());
    return userAge >= tournament.getAgeLimit();
}


    // Calculate age from birth date
    public int calculateAge(Timestamp birthDate) {
        LocalDate birthLocalDate = birthDate.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return Period.between(birthLocalDate, LocalDate.now()).getYears();
    }

    // Get user info by ID
    public User getUserInfo(String userID) throws InterruptedException, ExecutionException {
        Query query = firestore.collection("Users").whereEqualTo("authId", userID);
        QuerySnapshot snapshot = query.get().get();
        if (snapshot.isEmpty()) return null;
        return snapshot.getDocuments().get(0).toObject(User.class);
    }





    // @Autowired
    // private TaskScheduler taskScheduler;

    // // Method to schedule a task for each tournament
    // public void scheduleCloseRegistration(Tournament tournament) {
    //     Instant startDatetime = tournament.getStartDatetime();
    //     Instant closeRegistrationTime = startDatetime.minus(Duration.ofHours(24)); 
    //     taskScheduler.schedule(() -> closeRegistration(tournament.getTid()), closeRegistrationTime);

    // }

    // // This method will be triggered at the scheduled time to close registration
    // private void closeRegistration(String tournamentID) {
    //     DocumentReference tournamentRef = firestore.collection("Tournaments").document(tournamentID);
    //     tournamentRef.update("status", "Registration Closed").addListener(() -> {
    //         System.out.println("Tournament registration closed for: " + tournamentID);
    //     }, Runnable::run);
    // }

}