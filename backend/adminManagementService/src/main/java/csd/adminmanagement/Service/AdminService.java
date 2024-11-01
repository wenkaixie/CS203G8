package csd.adminmanagement.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldPath;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteBatch;
import com.google.cloud.firestore.WriteResult;
import org.springframework.web.client.RestTemplate;

import csd.adminmanagement.Exception.AdminNotFoundException;
import csd.adminmanagement.Exception.TournamentNotFoundException;
import csd.adminmanagement.Model.Admin;
import csd.adminmanagement.Model.MatchResultUpdateRequest;
import csd.adminmanagement.Model.Tournament;

@Service
public class AdminService {

    @Autowired
    private Firestore firestore;

    @Autowired
    private RestTemplate restTemplate;

    // Update Admin Profile
    public Admin updateAdminProfile(String adminID, Admin updatedAdmin) throws AdminNotFoundException {
        DocumentReference docRef = firestore.collection("Admins").document(adminID);
        
        try {
            // Convert Admin object to Map<String, Object>
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> updatedFields = objectMapper.convertValue(updatedAdmin, Map.class);
            
            // Filter out null fields to avoid updating fields to null unintentionally
            Map<String, Object> filteredFields = updatedFields.entrySet().stream()
                .filter(entry -> entry.getValue() != null)  // Exclude null values
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            // Perform the update
            ApiFuture<WriteResult> future = docRef.update(filteredFields);
            future.get();
            
            return updatedAdmin;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new AdminNotFoundException("Admin not found or update failed");
        }
    }


    // Create Admin Profile
    public Admin createAdminProfile(Admin newAdmin) {
        DocumentReference docRef = firestore.collection("Admins").document();
        newAdmin.setAuthId(docRef.getId());
        ApiFuture<WriteResult> result = docRef.set(newAdmin);
        return newAdmin;
    }

    // Retrieve all Admins
    public List<Admin> getAllAdmins() {
        CollectionReference adminRef = firestore.collection("Admins");
        ApiFuture<QuerySnapshot> future = adminRef.get();
        List<Admin> admins = new ArrayList<>();
        try {
            QuerySnapshot querySnapshot = future.get();
            for (QueryDocumentSnapshot document : querySnapshot) {
                System.out.println(document.getId());
                admins.add(document.toObject(Admin.class));
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return admins;
    }

    // Retrieve Admin by ID
    public Admin getAdminById(String adminId) throws AdminNotFoundException {
        CollectionReference adminsRef = firestore.collection("Admins");
        System.out.println("fetching data for" + adminId);

        try {
            ApiFuture<QuerySnapshot> querySnapshot = adminsRef.whereEqualTo("authId", adminId).get();
            List<QueryDocumentSnapshot> adminDocuments = querySnapshot.get().getDocuments();

            if (adminDocuments.isEmpty()) {
                throw new AdminNotFoundException("Admin not found for authId: " + adminId);
            }

            DocumentSnapshot adminSnapshot = adminDocuments.get(0);
            Admin admin = adminSnapshot.toObject(Admin.class);
            return admin;

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching admin data from Firestore: " + e.getMessage(), e);
        }
    }


    // *** RAY ***
    // Retrieve Tournaments for task list
    public List<Tournament> getTaskView(String adminId) {
        CollectionReference tournamentsRef = firestore.collection("Tournaments");
        List<Tournament> taskList = new ArrayList<>();
        // check tournament belong to admin
        try {
            ApiFuture<QuerySnapshot> querySnapshot = tournamentsRef.whereEqualTo("adminId", adminId).get();
            List<QueryDocumentSnapshot> tournamentDocuments = querySnapshot.get().getDocuments();

            if (tournamentDocuments.isEmpty()) {
                throw new TournamentNotFoundException("No tournaments found for adminId: " + adminId);
            }

            for (QueryDocumentSnapshot document : tournamentDocuments) {
                Tournament tournament = document.toObject(Tournament.class);
                // check tournament status !completed then add to return list
                if (!tournament.getStatus().equals("Completed")) {
                    taskList.add(tournament);
                }
            }
            return taskList;

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching tournament data from Firestore: " + e.getMessage(), e);
        }

    }

    // Updating an Array Element: To update an element within an array in Firestore, you need to read the entire document, 
    // modify the array in your application code, and then write back the entire modified array to Firestore.
    // Completion button from frontend will call this method
    public void completeMatch(String tournamentId, String roundNum, int matchId, MatchResultUpdateRequest matchResultUpdateRequest) {
        CollectionReference tournamentsRef = firestore.collection("Tournaments");
    
        try {
            // Fetch the tournament and check existence
            DocumentReference tournamentDocRef = tournamentsRef.document(tournamentId);
            if (!tournamentDocRef.get().get().exists()) {
                throw new TournamentNotFoundException("No tournament found for ID: " + tournamentId);
            }
    
            // Fetch the round and check existence
            DocumentReference roundDocRef = tournamentDocRef.collection("Rounds").document(roundNum);
            DocumentSnapshot roundSnapshot = roundDocRef.get().get();
            if (!roundSnapshot.exists()) {
                throw new RuntimeException("No round found with ID: " + roundNum);
            }
    
            // Fetch the matches and check existence
            List<Map<String, Object>> matches = (List<Map<String, Object>>) roundSnapshot.get("matches");
            if (matches == null || matches.size() <= matchId || matches.get(matchId) == null) {
                throw new RuntimeException("No match found with ID: " + matchId);
            }
    
            Map<String, Object> foundMatch = matches.get(matchId);
            List<Map<String, Object>> participants = (List<Map<String, Object>>) foundMatch.get("participants");
            if (participants == null || participants.isEmpty()) {
                throw new RuntimeException("No participants found in the match.");
            }

            String user1 = (String) participants.get(0).get("uid"); String user2 = (String) participants.get(1).get("uid");
    
            // Update match results
            updateMatchResults(participants, matchResultUpdateRequest.getAS1(), matchResultUpdateRequest.getAS2());

            // Update the entire array in Firestore
            roundDocRef.update("matches", matches);

            // call elo service to update the elo rating
            // Prepare the HttpEntity for the REST call
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            HttpEntity<MatchResultUpdateRequest> requestEntity = new HttpEntity<>(matchResultUpdateRequest, headers);

            // URL for the ELO update service
            String url = "http://localhost:9091/api/elo/update/" + user1 + "/" + user2;
            // RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to update ELO.");
            }

            System.out.println("ELO updated successfully.");

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error completing the match: " + e.getMessage(), e);
        }
    }
    
    private void updateMatchResults(List<Map<String, Object>> participants, double score1, double score2) {
        Map<String, Object> participant1 = participants.get(0);
        Map<String, Object> participant2 = participants.get(1);
    
        // Set winners based on scores
        participant1.put("isWinner", score1 > score2);
        participant2.put("isWinner", score2 > score1);
    
        // Handle the case of a draw
        if (score1 == score2) {
            participant1.put("isWinner", true);
            participant2.put("isWinner", true);
        }
    }

    // generate rounds button
    

    // wenkai 25/10
    public List<Tournament> getAdminTournaments(String adminId) {
        CollectionReference adminsRef = firestore.collection("Admins");
        List<Tournament> tournaments = new ArrayList<>();

        try {
            ApiFuture<QuerySnapshot> querySnapshot = adminsRef.whereEqualTo("authId", adminId).get();
            List<QueryDocumentSnapshot> adminDocuments = querySnapshot.get().getDocuments();

            if (adminDocuments.isEmpty()) {
                throw new AdminNotFoundException("No admin found for adminId: " + adminId);
            }

            List<String> tournamentIDs = adminDocuments.get(0).toObject(Admin.class).getTournamentCreated();
            if (tournamentIDs == null || tournamentIDs.isEmpty()) {
                return tournaments;
            }

            CollectionReference tournamentsRef = firestore.collection("Tournaments");
            ApiFuture<QuerySnapshot> tournamentsQuery = tournamentsRef.whereIn(FieldPath.documentId(), tournamentIDs).get();
            List<QueryDocumentSnapshot> tournamentDocuments = tournamentsQuery.get().getDocuments();

            for (DocumentSnapshot document : tournamentDocuments) {
                if (document.exists()) {
                    tournaments.add(document.toObject(Tournament.class));
                }
            }

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching tournament data from Firestore: " + e.getMessage(), e);
        }

        return tournaments;
    }

    // wenkai 25/10
    // createTournament

    public Tournament createTournament(String adminId, Tournament newTournament) {
        CollectionReference tournamentsRef = firestore.collection("Tournaments");
        DocumentReference docRef = tournamentsRef.document();
        newTournament.setTid(docRef.getId());                   // Set generated tournament ID
        newTournament.setStatus("Registration open");           // Set the status to "Registration open"
        newTournament.setCreatedTimestamp(Instant.now());     // Set the current timestamp
        newTournament.setAdminId(adminId);   
        ApiFuture<WriteResult> result = docRef.set(newTournament);
        return newTournament;
    }
    
   
}
