package com.app.tournament;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

@Configuration
public class FirebaseConfig {

    // @PostConstruct
    // public void initialize() {
    //     try {
    //         // Firebase SDK credential file (Updated file path)
    //         FileInputStream serviceAccount = 
    //             new FileInputStream("serviceAccountKey.json");

    //         FirebaseOptions options = FirebaseOptions.builder()
    //             .setCredentials(GoogleCredentials.fromStream(serviceAccount))   
    //             .build();

    //         if (FirebaseApp.getApps().isEmpty()) {
    //             FirebaseApp.initializeApp(options);
    //         }
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    // }

    // @Bean
    // public Firestore firestore() {
    //     FirebaseApp defaultApp = FirebaseApp.getInstance();  // Ensure FirebaseApp is initialized
    //     return FirestoreClient.getFirestore(defaultApp);
    // }

    @Bean
    public FirebaseApp initializeFirebaseApp() throws IOException {
        List<FirebaseApp> firebaseApps = FirebaseApp.getApps();
        if (firebaseApps != null && !firebaseApps.isEmpty()) {
            // Return the existing FirebaseApp instance
            return firebaseApps.get(0);
        } else {
            // Initialize a new FirebaseApp instance

            

            String credentialsPath = "serviceAccountKey.json";

            FileInputStream serviceAccount = new FileInputStream(credentialsPath);

            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

            return FirebaseApp.initializeApp(options);
        }
    }

    @Bean
    public Firestore getFirestore() throws IOException {
        FirebaseApp firebaseApp = initializeFirebaseApp();
        return FirestoreClient.getFirestore(firebaseApp);
    }
}
