package csd.rankingdashboard.Service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class FirestoreService {
    
    private Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }

    public List<String> getAllDocumentsInCollection(String collectionName) throws InterruptedException, ExecutionException {
        Firestore dbFirestore = getFirestore();
        CollectionReference collection = dbFirestore.collection(collectionName);

        // Asynchronously retrieve all documents in the collection
        ApiFuture<QuerySnapshot> future = collection.get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        List<String> documentDataList = new ArrayList<>();
        for (QueryDocumentSnapshot document : documents) {
            documentDataList.add(document.getData().toString());
        }

        return documentDataList;
    }
}