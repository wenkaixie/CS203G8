package com.app.tournament;

// Spring imports
import java.io.InputStream;
import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.util.Optional;


@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        try {
            // Load the service account key from the classpath (src/main/resources)
            InputStream serviceAccount = findServiceAccountKey("serviceAccountKey.json")
                    .orElseThrow(() -> new RuntimeException("Service account key file not found"));

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error initializing Firebase", e);
        }
    }

    @Bean
    public Firestore firestore() {
        FirebaseApp defaultApp = FirebaseApp.getInstance(); // Ensure FirebaseApp is initialized
        return FirestoreClient.getFirestore(defaultApp);
    }

    private Optional<InputStream> findServiceAccountKey(String fileName) {
        try {
            // Load the file from the classpath (src/main/resources)
            Resource resource = new ClassPathResource(fileName);
            if (resource.exists()) {
                return Optional.of(resource.getInputStream());
            } else {
                System.out.println("File not found in classpath: " + fileName);
            }
        } catch (IOException e) {
            System.out.println("Error loading service account key from classpath: " + e.getMessage());
        }
        return Optional.empty();
    }
}


