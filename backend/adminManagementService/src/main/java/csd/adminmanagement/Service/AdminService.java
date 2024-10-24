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
import csd.adminmanagement.Model.Admin;
import java.util.ArrayList;
import java.util.List;
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

    
}
