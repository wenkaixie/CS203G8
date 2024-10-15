package com.app.tournamentV2;

// Spring imports
import java.io.FileInputStream;
import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        try {
            // Firebase SDK credential file (Updated file path)
            FileInputStream serviceAccount = 
                new FileInputStream("C:\\Users\\ASUS\\Documents\\GitHub\\CS203G8\\serviceAccountKey.json");

            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))   
                .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Bean
    public Firestore firestore() {
        FirebaseApp defaultApp = FirebaseApp.getInstance();  // Ensure FirebaseApp is initialized
        return FirestoreClient.getFirestore(defaultApp);
    }
}
