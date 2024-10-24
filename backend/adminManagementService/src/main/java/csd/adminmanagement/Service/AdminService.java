package csd.adminmanagement.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteResult;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
                admins.add(document.toObject(Admin.class));
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return admins;
    }

    // Retrieve Admin by ID
    public Admin getAdminbyId(String adminID) throws AdminNotFoundException {
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
            return document.toObject(Admin.class);
        } else {
            throw new AdminNotFoundException("Admin not found");
        }
    }
}
