package csd.rankingdashboard.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import csd.rankingdashboard.Service.FirestoreService;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
public class FirebaseController {

    @Autowired
    private FirestoreService firestoreService;

    @GetMapping("/getAllDocuments")
    public String getAllDocuments(@RequestParam String collectionName) {
        try {
            List<String> documents = firestoreService.getAllDocumentsInCollection(collectionName);
            return String.join("\n", documents);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return "Error reading documents: " + e.getMessage();
        }
    }
}