package csd.matchresult.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import csd.matchresult.Model.*;

@Service
public class MatchResultService {
    
    @Autowired
    private Firestore firestore;

    public Matches recordMatchResult(Matches newMatch) throws InterruptedException, ExecutionException {
        System.out.println("Recording match result...");

        // Get a reference to the matches collection
        CollectionReference matchRef = firestore.collection("RoundMatch");
        System.out.println("Match reference: " + matchRef.getPath());

        // Create a map for the new match data
        Map<String, Object> newMatchData = new HashMap<>();
        newMatchData.put("date", newMatch.getMatchDate());  
        newMatchData.put("rid", newMatch.getRid());  
        newMatchData.put("rmid", newMatch.getRmid());  
        newMatchData.put("uid1", newMatch.getUid1());  
        newMatchData.put("uid2", newMatch.getUid2());   
        newMatchData.put("user1Score", newMatch.getUser1Score());
        newMatchData.put("user2Score", newMatch.getUser2Score()); 
        newMatchData.put("userIsWhite", newMatch.isUserIsWhite()); // True if player 1 plays white, false if black 

        // Add the new match to the 'matches' sub-collection (FireStore will generate a random ID for this document)
        ApiFuture<DocumentReference> addedDocRef = matchRef.add(newMatchData);

       // Block until the operation is complete and get the generated document ID
        String generatedDocId = addedDocRef.get().getId();
        System.out.println("New match added with ID: " + generatedDocId);

        return newMatch;
    }

    public List<Matches> getAllMatchResult() throws InterruptedException, ExecutionException {
        System.out.println("Getting match results...");

        // Get a reference to the matches collection
        CollectionReference matchRef = firestore.collection("RoundMatch");
        System.out.println("Match reference: " + matchRef);

        // Get all documents in the 'RoundMatch' collection
        ApiFuture<QuerySnapshot> query = matchRef.get();
        QuerySnapshot querySnapshot = query.get();

        // Create a list to hold the matches
        List<Matches> matchList = new ArrayList<>();

        // Loop through all the documents in the collection
        for (QueryDocumentSnapshot document : querySnapshot) {
            System.out.println("Document ID: " + document.getId());
            System.out.println("Document data: " + document.getData());

            // Convert the document into a Matches object and add it to the list
            Matches match = document.toObject(Matches.class);
            matchList.add(match);
        }

        // Return the list of matches
        return matchList;
    }

    public Matches getMatchByRmid(String rmid) throws InterruptedException, ExecutionException {
        System.out.println("Getting match by RMID: " + rmid);
    
        // Get a reference to the matches collection
        CollectionReference matchRef = firestore.collection("RoundMatch");
    
        // Query the collection to find the document with the given RMID
        ApiFuture<QuerySnapshot> query = matchRef.whereEqualTo("rmid", rmid).get();
    
        // Get the results of the query
        QuerySnapshot querySnapshot = query.get();
    
        // Check if any documents match the query
        if (querySnapshot.isEmpty()) {
            System.out.println("No match found with RMID: " + rmid);
            return null;  // Or throw an exception if you prefer
        }
    
        // Since rmid should be unique, get the first matching document
        QueryDocumentSnapshot document = querySnapshot.getDocuments().get(0);
    
        // Convert the document into a Matches object
        Matches match = document.toObject(Matches.class);
    
        System.out.println("Match found: " + match);
        
        // Return the match
        return match;
    }
}

    