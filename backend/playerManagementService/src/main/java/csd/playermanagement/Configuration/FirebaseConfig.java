package csd.playermanagement.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.key.path}")
    private String firebaseKeyPath;

    // Firebase initialization using @PostConstruct
    @PostConstruct
    public void initialize() {
        try {
            // Debug log to verify path
            System.out.println("Firebase Key Path: " + firebaseKeyPath);
            
            FileInputStream serviceAccount = new FileInputStream(firebaseKeyPath);

            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))   
                .build();
                
            // Initialize FirebaseApp only if no other instances exist
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (IOException e) {
            System.err.println("Failed to initialize Firebase with key path: " + firebaseKeyPath);
            e.printStackTrace();
        }
    }

    // Firestore bean to be used throughout the application
    @Bean
    public Firestore firestore() {
        if (FirebaseApp.getApps().isEmpty()) {
            throw new IllegalStateException("FirebaseApp initialization failed; no instance found.");
        }
        return FirestoreClient.getFirestore();
    }
}