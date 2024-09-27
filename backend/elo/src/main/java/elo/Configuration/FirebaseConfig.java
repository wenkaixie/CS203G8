package elo.configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
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

    @Bean
    public FirebaseApp initializeFirebaseApp() throws IOException {
        String credentialsPath = System.getenv("FIREBASE_CREDENTIALS");

        if (credentialsPath == null || credentialsPath.isEmpty()) {
            logger.error("FIREBASE_CREDENTIALS environment variable is not set.");
            throw new IOException("FIREBASE_CREDENTIALS environment variable is not set.");
        }

        File credentialsFile = new File(credentialsPath);
        if (credentialsFile.exists()) {
            logger.info("Service account file found at path: {}", credentialsPath);
        } else {
            logger.error("Service account file not found at path: {}", credentialsPath);
            throw new IOException("Service account file not found at specified path.");
        }

        logger.info("Firebase credentials path: {}", credentialsPath);

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(new FileInputStream(credentialsPath)))
                .build();

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
