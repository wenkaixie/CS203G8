package com.app.tournament;

import java.io.File;
// Spring imports
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

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
            FileInputStream serviceAccount = findServiceAccountKey("serviceAccountKey.json")
                .map(file -> {
                    try {
                        return new FileInputStream(file);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException("Service account key file not found", e);
                    }
                }).orElseThrow(() -> new RuntimeException("Service account key file not found"));


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

    private Optional<File> findServiceAccountKey(String fileName) {
        // Define the relative path to the directory where the key file is located (relative to the project root)
        String relativePathToConfigDir = "../src/main/resources"; // You can modify this to match your directory structure
        
        // Start from the current working directory (user.dir)
        File currentDir = new File(System.getProperty("user.dir"));
        
        // Create the target directory by appending the relative path
        File targetDir = new File(currentDir, relativePathToConfigDir);
        
        System.out.println("Searching for service account key in directory: " + targetDir.getAbsolutePath());
        
        // Now search for the serviceAccountKey.json in the target directory
        return findFileInDirectory(targetDir, fileName);
    }
    

    /**
     * Recursively search for a file in the given directory and its subdirectories.
     *
     * @param directory The directory to start searching in.
     * @param fileName  The name of the file to search for.
     * @return Optional containing the file if found, or empty if not found.
     */
    private Optional<File> findFileInDirectory(File directory, String fileName) {
        // List all files and directories in the current directory
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // Recursively search in subdirectories
                    Optional<File> foundFile = findFileInDirectory(file, fileName);
                    if (foundFile.isPresent()) {
                        return foundFile;
                    }
                } else if (file.getName().equals(fileName)) {
                    // File found
                    return Optional.of(file);
                }
            }
        }
        // File not found
        return Optional.empty();
    }
}

