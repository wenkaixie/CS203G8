package csd.matchresult.Service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import csd.rankingdashboard.Model.Tournament;
import csd.rankingdashboard.Model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

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

        ApiFuture<QuerySnapshot> future = firestore.collection("Tournament").get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        LOGGER.info("Futuret: " + future);
        LOGGER.info("Documents: " + documents);

        List<Tournament> tournaments = new ArrayList<>();
        for (DocumentSnapshot document : documents) {
            tournaments.add(document.toObject(Tournament.class));
        }
        return tournaments;
    }
}