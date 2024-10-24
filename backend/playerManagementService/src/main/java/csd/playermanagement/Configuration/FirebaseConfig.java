package csd.playermanagement.Configuration;

// Spring imports
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import java.io.FileInputStream;
import java.io.IOException;
import javax.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FirebaseConfig {

    // Firebase initialization using @PostConstruct
    @PostConstruct
    public void initialize() {
        try {
            // Updated path to Firebase service account key file
            FileInputStream serviceAccount = 
                new FileInputStream("C:\\Users\\xwkof\\Documents\\SMU CS\\CS203 WK\\CS203G8\\CS203G8\\serviceAccountKey.json");
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))   
                .build();
                
            // Initialize FirebaseApp only if no other instances exist
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Firestore bean to be used throughout the application
    @Bean
    public Firestore firestore() {
        FirebaseApp defaultApp = FirebaseApp.getInstance();  // Ensure FirebaseApp is initialized
        return FirestoreClient.getFirestore(defaultApp);
    }
}
