package csd.adminmanagement.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.google.cloud.firestore.WriteResult;

import csd.adminmanagement.Exception.AdminNotFoundException;
import csd.adminmanagement.Exception.TournamentNotFoundException;
import csd.adminmanagement.Model.Admin;
import csd.adminmanagement.Model.Tournament;

@Service
public class AdminService {

    @Autowired
    private Firestore firestore;

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

    // NEED to perform verification for (tournament belongs to admin)
    // Completion button from frontend will call this method
    public Tournament completeTournament(String tournamentId){
        CollectionReference tournamentsRef = firestore.collection("Tournaments");

        try{
            ApiFuture<QuerySnapshot> querySnapshot = tournamentsRef.whereEqualTo("tid", tournamentId).get();
            List<QueryDocumentSnapshot> tournamentDocuments = querySnapshot.get().getDocuments();

            if (tournamentDocuments.isEmpty()) {
                throw new TournamentNotFoundException("No tournaments found for tournamentId: " + tournamentId);
            }

            DocumentSnapshot tournamentSnapshot = tournamentDocuments.get(0);
            Tournament tournament = tournamentSnapshot.toObject(Tournament.class);

            if (tournament == null) {
                throw new TournamentNotFoundException("Tournament data is null for tournamentId: " + tournamentId);
            }

            // loop through all the matches and change the state of the match to completed
            DocumentReference tournamentDocRef = tournamentsRef.document(tournamentId);
            CollectionReference roundsRef = tournamentDocRef.collection("Rounds");
            ApiFuture<QuerySnapshot> roundsQuerySnapshot = roundsRef.get();
            List<QueryDocumentSnapshot> rounds = roundsQuerySnapshot.get().getDocuments();

            for (QueryDocumentSnapshot round : rounds) {
                List<Map<String, Object>> matches = (List<Map<String, Object>>) round.get("matches");
                if (matches != null) {
                    for (Map<String, Object> match : matches) {
                        match.put("state", "DONE");
                    }
                    // Update the round document with the modified matches
                    round.getReference().update("matches", matches).get();
                }
            }

            // change the status of the tournament to CLOSED/COMPLETED 
            tournament.setStatus("Completed");
            // update the tournament in the database
            ApiFuture<WriteResult> result = tournamentsRef.document(tournamentId).set(tournament, SetOptions.merge());

            return tournament;

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching tournament data from Firestore: " + e.getMessage(), e);
        }
    }

    // LOGIC:
    // loop through all the matches and change the state of the match to completed
    // change the status of the tournament to CLOSED/COMPLETED 
    // tournament will be removed from the task list

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
