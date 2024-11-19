package com.csd.tournament.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.csd.shared_library.model.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;


@Service
public class UserService {

    @Autowired
    private Firestore firestore;

    public List<String> getUserNamesByIds(List<String> userIds) throws InterruptedException, ExecutionException {
        System.out.println("Retrieving names for user IDs: " + userIds);

        List<String> userNames = new ArrayList<>();

        // Reference to the Users collection
        CollectionReference usersRef = firestore.collection("Users");

        // Iterate through the list of user IDs to retrieve each user's name
        for (String userId : userIds) {
            ApiFuture<QuerySnapshot> querySnapshot = usersRef.whereEqualTo("uid", userId).get();

            List<QueryDocumentSnapshot> userDocuments = querySnapshot.get().getDocuments();
            if (!userDocuments.isEmpty()) {
                User user = userDocuments.get(0).toObject(User.class);
                if (user != null && user.getName() != null) {
                    userNames.add(user.getName());
                    System.out.println("Retrieved name: " + user.getName() + " for user ID: " + userId);
                } else {
                    System.out.println("User found, but name is null for user ID: " + userId);
                    userNames.add("Name not available");
                }
            } else {
                System.out.println("No user found with ID: " + userId);
                userNames.add("User not found");
            }
        }

        System.out.println("Retrieved user names: " + userNames);
        return userNames;
    }
}
