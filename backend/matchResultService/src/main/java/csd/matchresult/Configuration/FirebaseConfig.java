package csd.matchresult.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@Configuration
public class FirebaseConfig {

    @Bean
    public FirebaseApp initializeFirebaseApp() throws IOException {
        List<FirebaseApp> firebaseApps = FirebaseApp.getApps();
        if (firebaseApps != null && !firebaseApps.isEmpty()) {
            // Return the existing FirebaseApp instance
            return firebaseApps.get(0);
        } else {
            // Initialize a new FirebaseApp instance
            String credentialsPath = System.getenv("FIREBASE_CREDENTIALS");
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