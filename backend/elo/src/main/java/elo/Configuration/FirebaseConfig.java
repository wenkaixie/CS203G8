package elo.configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    // Use @Value to inject the file path from application.properties
    @Value("${firebase.credentials.path}")
    private String credentialsPath;

    @Bean
    public FirebaseApp initializeFirebaseApp() throws IOException {
        // Log the credentials path for debugging purposes
        logger.info("Firebase credentials path from application.properties: {}", credentialsPath);

        // Check if the file exists
        File credentialsFile = new File(credentialsPath);
        if (!credentialsFile.exists()) {
            logger.error("Service account file not found at path: {}", credentialsPath);
            throw new IOException("Service account file not found at specified path.");
        }

        // Initialize Firebase with the credentials from the file
        try (FileInputStream serviceAccount = new FileInputStream(credentialsFile)) {
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

            if (FirebaseApp.getApps().isEmpty()) {
                logger.info("Initializing Firebase...");
                FirebaseApp app = FirebaseApp.initializeApp(options);
                logger.info("Firebase initialized successfully.");
                return app;
            } else {
                logger.info("FirebaseApp already initialized.");
                return FirebaseApp.getInstance();
            }
        }
    }
}
