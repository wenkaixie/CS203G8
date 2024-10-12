package csd.rankingdashboard.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import csd.rankingdashboard.Model.Tournament;
import csd.rankingdashboard.Model.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;

@Service
public class RankingServiceImplt implements RankingService{

    @Autowired
    private Firestore firestore;

    public List<User> getRankings() throws InterruptedException, ExecutionException {
    // Create a reference to the "Users" collection
    CollectionReference usersRef = firestore.collection("User");
    System.out.println("Collection usersRef: " + usersRef);

    // Get all documents from the "Users" collection
    ApiFuture<QuerySnapshot> querySnapshot = usersRef.get();
    System.out.println("QuerySnapshot: " + querySnapshot);
    
    // Create a list to hold the users
    List<User> usersList = new ArrayList<>();

    // Loop through the query snapshot and add each user to the list
    for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
        // Convert the document to a User object
        User user = document.toObject(User.class);

        // Add the user to the list
        if (user != null) {
            usersList.add(user);
        }
    }

    System.out.println("UsersList: " + usersList);


    // Sort the list of users by "elo" in descending order
    usersList.sort((u1, u2) -> Integer.compare(u2.getElo(), u1.getElo()));

    // Return the sorted list
    return usersList;
}




}
