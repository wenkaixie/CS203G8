package csd.playermanagement.Service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import csd.playermanagement.Model.Tournament;
import csd.playermanagement.Model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

@Service
public class FirestoreService {

    @Autowired
    private Firestore firestore;
    
     public List<User> getAllUsers() throws InterruptedException, ExecutionException {
        ApiFuture<QuerySnapshot> future = firestore.collection("User").get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        List<User> users = new ArrayList<>();
        for (DocumentSnapshot document : documents) {
            users.add(document.toObject(User.class));
        }
        return users;
    }

    public List<Tournament> getAllTournaments() throws InterruptedException, ExecutionException {
        List<Tournament> tournaments = new ArrayList<>();
        try{
            ApiFuture<QuerySnapshot> future = firestore.collection("Tournaments").get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            // System.out.println("Future: " + future);// remove for production?
            // System.out.println("Documents: " + documents);

            for (DocumentSnapshot document : documents) {
                tournaments.add(document.toObject(Tournament.class));
            }
            }
        catch(Exception e){
            System.out.println("Error: " + e);
        }

        return tournaments;
    }
}