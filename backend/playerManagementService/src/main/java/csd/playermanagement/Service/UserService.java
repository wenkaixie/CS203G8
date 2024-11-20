package csd.playermanagement.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.csd.shared_library.model.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteBatch;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import csd.playermanagement.DTO.UserDTO;
import csd.playermanagement.Exception.TournamentNotFoundException;
import csd.playermanagement.Exception.UserNotFoundException;
import csd.playermanagement.helper.UserMapper;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class UserService {

    @Autowired
    private Firestore firestore;


    public void addTournamentToUser(String userID, String tournamentID)
            throws ExecutionException, InterruptedException {
        log.info("Adding tournament {} to user {}", tournamentID, userID);
        try {
            DocumentReference userRef = firestore.collection("Users").document(userID);
            userRef.update("registrationHistory", FieldValue.arrayUnion(tournamentID)).get();
            log.info("Successfully added tournament {} to user {}", tournamentID, userID);
        } catch (Exception e) {
            log.error("Error adding tournament {} to user {}: {}", tournamentID, userID, e.getMessage(), e);
            throw e; // Propagate the exception for Saga rollback handling
        }
    }

    public void removeTournamentFromUser(String userID, String tournamentID)
            throws ExecutionException, InterruptedException {
        log.info("Removing tournament {} from user {}", tournamentID, userID);
        try {
            DocumentReference userRef = firestore.collection("Users").document(userID);
            userRef.update("registrationHistory", FieldValue.arrayRemove(tournamentID)).get();
            log.info("Successfully removed tournament {} from user {}", tournamentID, userID);
        } catch (Exception e) {
            log.error("Error removing tournament {} from user {}: {}", tournamentID, userID, e.getMessage(), e);
            throw e; // Propagate for rollback
        }
    }
    

    // test complaints
    /**
    * Register User for a tournament.
    *
    * @param tournamentId the tournament Id of a tournament.
    * @param authId the auth Id of a user.
    * @return a String value if user registered successfully.
    */
    public String registerUserForTournament(String tournamentId, String authId)
            throws InterruptedException, ExecutionException {
        log.info("Registering user {} for tournament {}", authId, tournamentId);
        try {
            DocumentSnapshot tournamentSnapshot = firestore.collection("Tournaments").document(tournamentId).get()
                    .get();
            if (!tournamentSnapshot.exists()) {
                log.warn("Tournament {} does not exist", tournamentId);
                throw new TournamentNotFoundException("No tournament found with the provided ID.");
            }

            DocumentSnapshot userSnapshot = firestore.collection("Users").document(authId).get().get();
            if (!userSnapshot.exists()) {
                log.warn("User {} does not exist", authId);
                throw new UserNotFoundException("User not found.");
            }

            User user = (User) UserMapper.mapDocumentToUser(userSnapshot);
            List<String> registrationHistory = user.getRegistrationHistory();
            if (registrationHistory == null) {
                registrationHistory = new ArrayList<>();
            }
            registrationHistory.add(tournamentId);
            firestore.collection("Users").document(authId).update("registrationHistory", registrationHistory).get();

            log.info("User {} successfully registered for tournament {}", authId, tournamentId);
            return "User successfully registered for the tournament.";
        } catch (Exception e) {
            log.error("Error registering user {} for tournament {}: {}", authId, tournamentId, e.getMessage(), e);
            throw e; // Propagate for rollback
        }
    }

    /**
    * Deregister User for a tournament.
    *
    * @param tournamentId the tournament Id of a tournament.
    * @param authId the auth Id of a user.
    * @return a String value if user deregistered successfully.
    */
    public String unregisterUserForTournament(String tournamentId, String authId)
            throws InterruptedException, ExecutionException {
        log.info("Unregistering user {} from tournament {}", authId, tournamentId);
        try {
            // Check if the tournament exists
            DocumentSnapshot tournamentSnapshot = firestore.collection("Tournaments").document(tournamentId).get()
                    .get();
            if (!tournamentSnapshot.exists()) {
                log.warn("Tournament {} does not exist", tournamentId);
                throw new TournamentNotFoundException("No tournament found with the provided ID.");
            }

            // Fetch the user document from Firestore
            DocumentSnapshot userSnapshot = firestore.collection("Users").document(authId).get().get();
            if (!userSnapshot.exists()) {
                log.warn("User {} does not exist", authId);
                throw new UserNotFoundException("User not found.");
            }

            // Map the Firestore document to a User object using the helper method
            User user = (User) UserMapper.mapDocumentToUser(userSnapshot);

            // Retrieve the user's registration history
            List<String> registrationHistory = user.getRegistrationHistory();
            if (registrationHistory == null || !registrationHistory.contains(tournamentId)) {
                log.warn("User {} is not registered for tournament {}", authId, tournamentId);
                return "User is not registered for this tournament.";
            }

            // Remove the tournamentId from the registration history
            registrationHistory.remove(tournamentId);

            // Update the user's registration history in Firestore
            firestore.collection("Users").document(authId).update("registrationHistory", registrationHistory).get();

            log.info("User {} successfully unregistered from tournament {}", authId, tournamentId);
            return "User successfully unregistered from the tournament.";
        } catch (Exception e) {
            log.error("Error unregistering user {} from tournament {}: {}", authId, tournamentId, e.getMessage(), e);
            throw e; // Propagate for rollback or further handling
        }
    }
        
    
    public UserDTO updateUserProfile(String authId, UserDTO updatedUser)
            throws InterruptedException, ExecutionException {
        log.info("Updating profile for user {}", authId);
        try {
            CollectionReference usersRef = firestore.collection("Users");
            ApiFuture<QuerySnapshot> querySnapshot = usersRef.whereEqualTo("authId", authId).get();
            List<QueryDocumentSnapshot> userDocuments = querySnapshot.get().getDocuments();

            if (userDocuments.isEmpty()) {
                log.warn("User {} not found for profile update", authId);
                throw new UserNotFoundException("User not found.");
            }

            DocumentReference userRef = userDocuments.get(0).getReference();
            Map<String, Object> changes = new HashMap<>();

            if (updatedUser.getName() != null) {
                changes.put("name", updatedUser.getName());
            }
            if (updatedUser.getPhoneNumber() != null) {
                changes.put("phoneNumber", updatedUser.getPhoneNumber());
            }

            if (!changes.isEmpty()) {
                userRef.update(changes).get();
                log.info("User profile updated for user {}", authId);
            } else {
                log.info("No changes detected for user {}", authId);
            }

            DocumentSnapshot updatedUserSnapshot = userRef.get().get();
            User fullUpdatedUser = (User) UserMapper.mapDocumentToUser(updatedUserSnapshot);

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
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        .withZone(ZoneId.systemDefault());
                String formattedDate = formatter.format(fullUpdatedUser.getDateOfBirth());
                responseDTO.setDateOfBirth(formattedDate);
            }

            return responseDTO;
        } catch (Exception e) {
            log.error("Error updating profile for user {}: {}", authId, e.getMessage(), e);
            throw e; // Propagate for rollback or further handling
        }
    }
    
    // New method to fetch chess Elo from Chess.com API
    public int fetchChessElo(String chessUsername) {
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
        log.info("Fetching all users...");
        try {
            CollectionReference usersRef = firestore.collection("Users");
            List<QueryDocumentSnapshot> userDocuments = usersRef.get().get().getDocuments();
            List<User> usersList = new ArrayList<>();

            for (QueryDocumentSnapshot document : userDocuments) {
                User user = (User) UserMapper.mapDocumentToUser(document);
                if (user != null) {
                    usersList.add(user);
                }
            }

            log.info("Successfully fetched {} users", usersList.size());
            return usersList;
        } catch (Exception e) {
            log.error("Error fetching all users: {}", e.getMessage(), e);
            throw e; // Propagate or handle the exception as needed
        }
    }
    

    /**
    * get user based on the user's ID.
    *
    * @param authId the auth Id of a user.
    * @return the user that has the specified authId.
    */
    public User getUserbyId(String userId) throws InterruptedException, ExecutionException {
        log.info("Fetching user with ID: {}", userId);

        DocumentSnapshot document = firestore.collection("Users").document(userId).get().get();
        if (!document.exists()) {
            log.error("User not found with ID: {}", userId);
            throw new UserNotFoundException("User not found with ID: " + userId);
        }

        return (User) UserMapper.mapDocumentToUser(document);
}

    /**
    * Get user's rank based on elo.
    *
    * @param authId the auth Id of a user.
    * @return the rank number out of existing users.
    */
    public int getUserRank(String authId) throws InterruptedException, ExecutionException {
        log.info("Fetching rank for user {}", authId);
        try {
            CollectionReference usersRef = firestore.collection("Users");
            List<QueryDocumentSnapshot> userDocuments = usersRef.orderBy("elo", Query.Direction.DESCENDING).get().get()
                    .getDocuments();

            int rank = 1;
            for (QueryDocumentSnapshot document : userDocuments) {
                User user = (User) UserMapper.mapDocumentToUser(document);
                if (user.getAuthId().equals(authId)) {
                    log.info("Rank for user {} is {}", authId, rank);
                    return rank;
                }
                rank++;
            }

            throw new UserNotFoundException("User not found.");
        } catch (Exception e) {
            log.error("Error fetching rank for user {}: {}", authId, e.getMessage(), e);
            throw e; // Propagate for rollback or further handling
        }
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

        String generatedAuthId = documentReference.getId();
        newUser.setAuthId(generatedAuthId);

        // Add authId to the profile map
        newUserProfile.put("authId", generatedAuthId);

        // Merge authId into Firestore and wait until the operation completes
        documentReference.set(newUserProfile, SetOptions.merge()).get(); // Ensures it's completed

        DocumentSnapshot userSnapshot = documentReference.get().get();
        User createdUser = (User) UserMapper.mapDocumentToUser(userSnapshot);

        return createdUser;
    }
    
    public void updatePlayerEloBatch(Map<String, Integer> eloUpdates) {
        WriteBatch batch = firestore.batch();
        eloUpdates.forEach((playerId, newElo) -> {
            DocumentReference playerRef = firestore.collection("Users").document(playerId);
            batch.update(playerRef, "elo", newElo);
        });
        try {
            batch.commit().get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to update player Elo in batch", e);
        }
    }
    
    

}


// /**
// * updating user profile.
// *
// * @param authId the auth Id of a user.
// * @param updatedUser the userDTO of details that the user changed.
// * @return the user that changed his/her profile.
// */
// public UserDTO updateUserProfile(String authId, UserDTO updatedUser)
// throws InterruptedException, ExecutionException {
// System.out.println("Updating User profile for user ID: " + authId);
// System.out.println("Received updated user:" + updatedUser);

// CollectionReference usersRef = firestore.collection("Users");
// ApiFuture<QuerySnapshot> querySnapshot = usersRef.whereEqualTo("authId",
// authId).get();

// List<QueryDocumentSnapshot> userDocuments =
// querySnapshot.get().getDocuments();
// System.out.println(userDocuments);

// if (userDocuments.isEmpty()) {
// throw new UserNotFoundException("User not found.");
// }

// DocumentSnapshot userSnapshot = userDocuments.get(0);
// DocumentReference userRef = userSnapshot.getReference();

// Map<String, Object> changes = new HashMap<>();
// Map<String, Object> changes2 = new HashMap<>();

// // Update username if provided
// if (updatedUser.getUsername() != null) {
// changes.put("username", updatedUser.getUsername());
// }

// // Update name if provided
// if (updatedUser.getName() != null) {
// changes.put("name", updatedUser.getName());
// changes2.put("name", updatedUser.getName());
// }

// // Update phone number if provided
// if (updatedUser.getPhoneNumber() != null) {
// changes.put("phoneNumber", updatedUser.getPhoneNumber());
// }

// // Update nationality if provided
// if (updatedUser.getNationality() != null) {
// changes.put("nationality", updatedUser.getNationality());
// changes2.put("nationality", updatedUser.getNationality());
// }

// // Update chessUsername if provided
// if (updatedUser.getChessUsername() != null) {
// changes.put("chessUsername", updatedUser.getChessUsername());
// }

// // Handle dateOfBirth if provided
// if (updatedUser.getDateOfBirth() != null
// && !updatedUser.getDateOfBirth().equals("{\"nanos\":0,\"seconds\":<some
// value>}")) {
// System.out.println("Received dateOfBirth: " + updatedUser.getDateOfBirth());

// try {
// // Parse the date string in YYYY-MM-DD format
// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
// LocalDate localDate = LocalDate.parse(updatedUser.getDateOfBirth(),
// formatter);
// System.out.println("Parsed LocalDate: " + localDate);

// // Convert LocalDate to Instant (at the start of the day)
// Instant instant =
// localDate.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant();
// System.out.println("Converted to Instant: " + instant);

// // Convert Instant to Timestamp
// Timestamp timestamp =
// Timestamp.ofTimeSecondsAndNanos(instant.getEpochSecond(), instant.getNano());
// System.out.println("Timestamp: " + timestamp);

// // Update the user's dateOfBirth with the converted Timestamp
// changes.put("dateOfBirth", timestamp);
// } catch (DateTimeParseException e) {
// System.out.println("Invalid date format for dateOfBirth. Skipping update.");
// }
// }

// // Handle chessUsername and update elo
// if (updatedUser.getChessUsername() != null &&
// !updatedUser.getChessUsername().isEmpty()) {
// String chessUsername = updatedUser.getChessUsername();
// int elo = fetchChessElo(chessUsername);
// changes.put("elo", elo);
// } else {
// // Set default elo to 0 if no username provided
// changes.put("elo", 0);
// }

// // Changes made to Firebase if any changes exist
// if (!changes.isEmpty()) {
// ApiFuture<WriteResult> writeResult = userRef.update(changes);
// writeResult.get();
// } else {
// System.out.println("No changes to update.");
// }

// System.out.println("NEW CODE FOR UPDATING USER IN TOURNAMENT");
// // For collecting the User subcollection within tournament
// // Reference to the Tournaments collection
// CollectionReference tournamentsRef = firestore.collection("Tournaments");

// // Retrieve all tournaments
// ApiFuture<QuerySnapshot> tournamentsQuery = tournamentsRef.get();
// List<QueryDocumentSnapshot> tournamentDocuments =
// tournamentsQuery.get().getDocuments();

// // Loop through each tournament
// for (QueryDocumentSnapshot tournamentDoc : tournamentDocuments) {
// // Reference to the Users subcollection within the current tournament
// CollectionReference usersRef2 =
// tournamentDoc.getReference().collection("Users");

// // Query the Users subcollection for the specific user by userID
// ApiFuture<QuerySnapshot> userQuery = usersRef2.whereEqualTo("authId",
// authId).get();

// List<QueryDocumentSnapshot> userDocuments2 = userQuery.get().getDocuments();

// // Check if the user document exists
// if (!userDocuments2.isEmpty()) {
// // Get the document reference for the user
// DocumentReference userRef2 = userDocuments2.get(0).getReference();

// // Update the fields in the user's document within the tournament
// ApiFuture<WriteResult> updateResult = userRef2.update(changes2);
// updateResult.get(); // Wait for the update to complete
// }
// }
// // End of collecting user within the tournament

// System.out.println("Retrieving updated user data...");
// DocumentSnapshot updatedUserSnapshot = userRef.get().get();
// User fullUpdatedUser = updatedUserSnapshot.toObject(User.class);

// if (fullUpdatedUser == null) {
// throw new RuntimeException("Error retrieving updated user data.");
// }

// // Create a new UserDTO for the response
// UserDTO responseDTO = new UserDTO();
// responseDTO.setAuthId(fullUpdatedUser.getAuthId());
// responseDTO.setUsername(fullUpdatedUser.getUsername());
// responseDTO.setEmail(fullUpdatedUser.getEmail());
// responseDTO.setName(fullUpdatedUser.getName());
// responseDTO.setElo(fullUpdatedUser.getElo());
// responseDTO.setChessUsername(fullUpdatedUser.getChessUsername());
// responseDTO.setNationality(fullUpdatedUser.getNationality());
// responseDTO.setPhoneNumber(fullUpdatedUser.getPhoneNumber());

// if (fullUpdatedUser.getDateOfBirth() != null) {
// DateTimeFormatter formatter =
// DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
// String formattedDate =
// formatter.format(fullUpdatedUser.getDateOfBirth().toDate().toInstant());
// responseDTO.setDateOfBirth(formattedDate);
// }

// return responseDTO;
// }