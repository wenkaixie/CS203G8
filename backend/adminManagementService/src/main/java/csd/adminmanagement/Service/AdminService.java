package csd.adminmanagement.Service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteResult;
import csd.adminmanagement.Exception.AdminNotFoundException;
import csd.adminmanagement.Exception.TournamentNotFoundException;
import csd.adminmanagement.Model.Admin;
import csd.adminmanagement.Model.Tournament;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    @Autowired
    private Firestore firestore;

    // Update Admin Profile
    public Admin updateAdminProfile(String adminID, Admin updatedAdmin) throws AdminNotFoundException {
        DocumentReference docRef = firestore.collection("Admins").document(adminID);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document;
        try {
            document = future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new AdminNotFoundException("Admin not found");
        }
        if (document.exists()) {
            ApiFuture<WriteResult> result = docRef.set(updatedAdmin, SetOptions.merge());
            return updatedAdmin;
        } else {
            throw new AdminNotFoundException("Admin not found");
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

    // NOT TESTED
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
                        match.put("state", "Completed");
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
   
}
