package csd.playermanagement.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteResult;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import csd.playermanagement.DTO.UserDTO;
import csd.playermanagement.Exception.TournamentNotFoundException;
import csd.playermanagement.Exception.UserNotFoundException;
import csd.playermanagement.Exception.UserTournamentException;
import csd.playermanagement.Model.Tournament;
import csd.playermanagement.Model.User;

@Service
public class UserService {

    @Autowired
    private Firestore firestore;

    /**
    * Register User for a tournament.
    *
    * @param tournamentId the tournament Id of a tournament.
    * @param authId the auth Id of a user.
    * @return a String value if user registered successfully.
    */
    public String registerUserForTournament(String tournamentId, String authId) throws InterruptedException, ExecutionException {
        System.out.println("Registering user for tournament...");

        // Checking tournament exist
        DocumentSnapshot tournamentSnapshot = firestore.collection("Tournaments").document(tournamentId).get().get();
        if (!tournamentSnapshot.exists()) {
            throw new TournamentNotFoundException("Tournament not found.");
        }
    
        // Directly reference the user document by authId
        DocumentReference userRef = firestore.collection("Users").document(authId);
    
        // Get the user document
        DocumentSnapshot userSnapshot = userRef.get().get();
        if (!userSnapshot.exists()) {
            throw new UserNotFoundException("User not found.");
        }
    
        // Retrieve current registration history, or initialize if null
        User user = userSnapshot.toObject(User.class);
        List<String> registrationHistory = user.getRegistrationHistory();

        // Add the tournamentId to the list
        registrationHistory.add(tournamentId);
    
        // Update the registrationHistory in Firestore
        ApiFuture<WriteResult> userUpdate = userRef.update("registrationHistory", registrationHistory);
        userUpdate.get(); // Wait for the update to complete

        return "User successfully registered for the tournament.";
    }

    /**
    * Deregister User for a tournament.
    *
    * @param tournamentId the tournament Id of a tournament.
    * @param authId the auth Id of a user.
    * @return a String value if user deregistered successfully.
    */
    public String unregisterUserForTournament(String tournamentId, String authId) throws InterruptedException, ExecutionException {
        System.out.println("Unregistering user from tournament...");

        // Checking tournament exist
        DocumentSnapshot tournamentSnapshot = firestore.collection("Tournaments").document(tournamentId).get().get();
        if (!tournamentSnapshot.exists()) {
            throw new TournamentNotFoundException("Tournament not found.");
        }

        // Directly reference the user document by authId
        DocumentReference userRef = firestore.collection("Users").document(authId);

        // Get the user document
        DocumentSnapshot userSnapshot = userRef.get().get();
        if (!userSnapshot.exists()) {
            throw new UserNotFoundException("User not found.");
        }

        // Retrieve current registration history, or initialize if null
        User user = userSnapshot.toObject(User.class);
        List<String> registrationHistory = user.getRegistrationHistory();
        if (registrationHistory == null || !registrationHistory.contains(tournamentId)) {
            System.out.println("User is not registered for this tournament.");
            return "User is not registered for this tournament.";
        }

        // Remove the tournamentId from the list
        registrationHistory.remove(tournamentId);

        // Update the registrationHistory in Firestore
        ApiFuture<WriteResult> userUpdate = userRef.update("registrationHistory", registrationHistory);
        userUpdate.get(); // Wait for the update to complete

        System.out.println("User's updated Registration History after removal: " + registrationHistory);
        return "User successfully unregistered from the tournament.";
    }
        
    /**
    * updating user profile.
    *
    * @param authId the auth Id of a user.
    * @param updatedUser the userDTO of details that the user changed.
    * @return the user that changed his/her profile.
    */
    public UserDTO updateUserProfile(String authId, UserDTO updatedUser) throws InterruptedException, ExecutionException {
        System.out.println("Updating User profile for user ID: " + authId);
        System.out.println("Received updated user:" + updatedUser);
        
        CollectionReference usersRef = firestore.collection("Users");
        ApiFuture<QuerySnapshot> querySnapshot = usersRef.whereEqualTo("authId", authId).get();
        
        List<QueryDocumentSnapshot> userDocuments = querySnapshot.get().getDocuments();
        System.out.println(userDocuments);
        
        if (userDocuments.isEmpty()) {
            throw new UserNotFoundException("User not found.");
        }
        
        DocumentSnapshot userSnapshot = userDocuments.get(0);
        DocumentReference userRef = userSnapshot.getReference();

                       
        Map<String, Object> changes = new HashMap<>();
        Map<String, Object> changes2 = new HashMap<>();
        
        // Update username if provided
        if (updatedUser.getUsername() != null) {
            changes.put("username", updatedUser.getUsername());
        }
        
        // Update name if provided
        if (updatedUser.getName() != null) {
            changes.put("name", updatedUser.getName());
            changes2.put("name", updatedUser.getName());
        }
        
        // Update phone number if provided
        if (updatedUser.getPhoneNumber() != null) {
            changes.put("phoneNumber", updatedUser.getPhoneNumber());
        }
        
        // Update nationality if provided
        if (updatedUser.getNationality() != null) {
            changes.put("nationality", updatedUser.getNationality());
            changes2.put("nationality", updatedUser.getNationality());
        }

        // Update chessUsername if provided
        if (updatedUser.getChessUsername() != null) {
            changes.put("chessUsername", updatedUser.getChessUsername());
        }
        
        // Handle dateOfBirth if provided
        if (updatedUser.getDateOfBirth() != null && !updatedUser.getDateOfBirth().equals("{\"nanos\":0,\"seconds\":<some value>}")) {
            System.out.println("Received dateOfBirth: " + updatedUser.getDateOfBirth());
            
            try {
                // Parse the date string in YYYY-MM-DD format
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate localDate = LocalDate.parse(updatedUser.getDateOfBirth(), formatter);
                System.out.println("Parsed LocalDate: " + localDate);
                
                // Convert LocalDate to Instant (at the start of the day)
                Instant instant = localDate.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant();
                System.out.println("Converted to Instant: " + instant);
                
                // Convert Instant to Timestamp
                Timestamp timestamp = Timestamp.ofTimeSecondsAndNanos(instant.getEpochSecond(), instant.getNano());
                System.out.println("Timestamp: " + timestamp);
                
                // Update the user's dateOfBirth with the converted Timestamp
                changes.put("dateOfBirth", timestamp);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format for dateOfBirth. Skipping update.");
            }
        }
        
        // Handle chessUsername and update elo
        if (updatedUser.getChessUsername() != null && !updatedUser.getChessUsername().isEmpty()) {
            String chessUsername = updatedUser.getChessUsername();
            int elo = fetchChessElo(chessUsername);
            changes.put("elo", elo);
        } else {
            // Set default elo to 0 if no username provided
            changes.put("elo", 0);
        }
    
        // Changes made to Firebase if any changes exist
        if (!changes.isEmpty()) {
            ApiFuture<WriteResult> writeResult = userRef.update(changes);
            writeResult.get();
        } else {
            System.out.println("No changes to update.");
        }

        System.out.println("NEW CODE FOR UPDATING USER IN TOURNAMENT");
        // For collecting the User subcollection within tournament
        // Reference to the Tournaments collection
        CollectionReference tournamentsRef = firestore.collection("Tournaments");

        // Retrieve all tournaments
        ApiFuture<QuerySnapshot> tournamentsQuery = tournamentsRef.get();
        List<QueryDocumentSnapshot> tournamentDocuments = tournamentsQuery.get().getDocuments();


        // Loop through each tournament
        for (QueryDocumentSnapshot tournamentDoc : tournamentDocuments) {
            // Reference to the Users subcollection within the current tournament
            CollectionReference usersRef2 = tournamentDoc.getReference().collection("Users");

            // Query the Users subcollection for the specific user by userID
            ApiFuture<QuerySnapshot> userQuery = usersRef2.whereEqualTo("authId", authId).get();
            
            List<QueryDocumentSnapshot> userDocuments2 = userQuery.get().getDocuments();

            // Check if the user document exists
            if (!userDocuments2.isEmpty()) {
                // Get the document reference for the user
                DocumentReference userRef2 = userDocuments2.get(0).getReference();
                
                // Update the fields in the user's document within the tournament
                ApiFuture<WriteResult> updateResult = userRef2.update(changes2);
                updateResult.get(); // Wait for the update to complete
            } 
        }
        // End of collecting user within the tournament                 

        
        System.out.println("Retrieving updated user data...");
        DocumentSnapshot updatedUserSnapshot = userRef.get().get();
        User fullUpdatedUser = updatedUserSnapshot.toObject(User.class);
        
        if (fullUpdatedUser == null) {
            throw new RuntimeException("Error retrieving updated user data.");
        }
        
        // Create a new UserDTO for the response
        UserDTO responseDTO = new UserDTO();
        responseDTO.setAuthId(fullUpdatedUser.getAuthId());
        responseDTO.setUsername(fullUpdatedUser.getUsername());
        responseDTO.setEmail(fullUpdatedUser.getEmail());
        responseDTO.setName(fullUpdatedUser.getName());
        responseDTO.setElo(fullUpdatedUser.getElo());
        responseDTO.setChessUsername(fullUpdatedUser.getChessUsername());
        responseDTO.setNationality(fullUpdatedUser.getNationality());
        responseDTO.setPhoneNumber(fullUpdatedUser.getPhoneNumber());

        if (fullUpdatedUser.getDateOfBirth() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
            String formattedDate = formatter.format(fullUpdatedUser.getDateOfBirth().toDate().toInstant());
            responseDTO.setDateOfBirth(formattedDate);
        }

        return responseDTO;  
    }
    
    // New method to fetch chess Elo from Chess.com API
    private int fetchChessElo(String chessUsername) {
        int elo = 0; // Default elo
    
        try {
            URL url = new URL("https://api.chess.com/pub/player/" + chessUsername + "/stats");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
    
            // Check for a successful response code
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                // Read the response
                Scanner scanner = new Scanner(url.openStream());
                StringBuilder response = new StringBuilder();
                while (scanner.hasNext()) {
                    response.append(scanner.nextLine());
                }
                scanner.close();
    
                // Parse the JSON response
                JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
                if (jsonResponse.has("fide")) {
                    elo = jsonResponse.get("fide").getAsInt();
                }
            } else {
                System.out.println("Failed to fetch chess stats: " + responseCode);
            }
        } catch (Exception e) {
            System.out.println("Error fetching chess elo: " + e.getMessage());
        }
    
        return elo;
    }   
    
    /**
    * get all user profile.
    *
    * @return the list user that exist in our app.
    */
    public List<User> getAllUsers() throws InterruptedException, ExecutionException {

        System.out.println("Registering user for tournament...");
 
        CollectionReference usersRef = firestore.collection("Users");
        System.out.println("userRef " + usersRef);

        ApiFuture<QuerySnapshot> querySnapshot = usersRef.get();
        System.out.println("querySnapshot " + querySnapshot);

        List<User> usersList = new ArrayList<>();
        

        List<QueryDocumentSnapshot> userDocuments = querySnapshot.get().getDocuments();
        System.out.println("userDocuments " + userDocuments);

        for (QueryDocumentSnapshot document : userDocuments) {
            try {
                User user = document.toObject(User.class);
                if (user != null) {
                    usersList.add(user);
                }
            } catch (Exception e) {
                System.err.println("Error parsing user document: " + document.getId());
                e.printStackTrace();  // Log the error and continue
            }
        }
        return usersList;
    }

    /**
    * get user based on the user's ID.
    *
    * @param authId the auth Id of a user.
    * @return the user that has the specified authId.
    */
    public User getUserbyId(String userId) throws InterruptedException, ExecutionException {
        CollectionReference usersRef = firestore.collection("Users");
        
        ApiFuture<QuerySnapshot> querySnapshot = usersRef.whereEqualTo("authId", userId).get();
    
        List<QueryDocumentSnapshot> userDocuments = querySnapshot.get().getDocuments();
    
        if (userDocuments.isEmpty()) {
            throw new UserNotFoundException("User not found.");
        }
    
        DocumentSnapshot userSnapshot = userDocuments.get(0);
        System.out.println(userSnapshot.toObject(User.class));
    
        return userSnapshot.toObject(User.class);
    }

    /**
    * Get user's rank based on elo.
    *
    * @param authId the auth Id of a user.
    * @return the rank number out of existing users.
    */
    public int getUserRank(String authId) throws InterruptedException, ExecutionException {
        CollectionReference usersRef = firestore.collection("Users");
        ApiFuture<QuerySnapshot> querySnapshot = usersRef.orderBy("elo", Query.Direction.DESCENDING).get();
        
        List<QueryDocumentSnapshot> userDocuments = querySnapshot.get().getDocuments();
    
        int rank = 1;
    
        for (QueryDocumentSnapshot document : userDocuments) {
            User user = document.toObject(User.class);
    
            // Check if the userId matches
            if (user.getAuthId().equals(authId)) {
                // Check if elo exists for the user
                if (user.getElo() != null) {
                    return rank;  // Return the rank if elo exists
                } else {
                    return -1;  // Return -1 if no elo is found
                }
            }
            rank++;
        }
    
        // If no user with the specified userId is found, throw an exception
        throw new UserNotFoundException("User not found.");
    }

    // NOT USED 
    public User createUserProfile(User newUser) throws InterruptedException, ExecutionException {
        CollectionReference usersRef = firestore.collection("Users");

        Map<String, Object> newUserProfile = new HashMap<>();
        newUserProfile.put("username", newUser.getUsername());
        newUserProfile.put("phoneNumber", newUser.getPhoneNumber());
        newUserProfile.put("nationality", newUser.getNationality());
        newUserProfile.put("email", newUser.getEmail());
        newUserProfile.put("name", newUser.getName());
        newUserProfile.put("elo", newUser.getElo());

        ApiFuture<DocumentReference> writeResult = usersRef.add(newUserProfile);
        DocumentReference documentReference = writeResult.get();

        String generatedUid = documentReference.getId();
        newUser.setAuthId(generatedUid);

        newUserProfile.put("uid", generatedUid);
        documentReference.set(newUserProfile, SetOptions.merge());

        DocumentSnapshot userSnapshot = documentReference.get().get();
        User createdUser = userSnapshot.toObject(User.class);

        return createdUser;
    }
    
    

}
