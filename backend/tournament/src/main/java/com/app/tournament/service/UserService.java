package com.app.tournament.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.tournament.model.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

@Service
public class UserService {

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

}
