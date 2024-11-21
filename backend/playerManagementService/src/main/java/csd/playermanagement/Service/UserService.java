package csd.playermanagement.service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
import csd.playermanagement.exception.TournamentNotFoundException;
import csd.playermanagement.exception.UserNotFoundException;
import csd.playermanagement.helper.UserMapper;
import csd.shared_library.model.User;
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
     * @param authId       the auth Id of a user.
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
     * @param authId       the auth Id of a user.
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

            // Check if the chessUsername field exists in the Firebase document
            String existingChessUsername = updatedUser.getChessUsername();

            if (existingChessUsername == null || existingChessUsername.isEmpty()) {
                // Allow update if chessUsername doesn't exist
                if (updatedUser.getChessUsername() != null && !updatedUser.getChessUsername().isEmpty()) {
                    String chessUsername = updatedUser.getChessUsername();
                    changes.put("chessUsername", chessUsername);

                    // Fetch elo for the provided chess username
                    int elo = fetchChessElo(chessUsername);
                    changes.put("elo", elo);
                } else {
                    // No username provided, set default elo
                    changes.put("elo", 800);
                }
            } else {
                // Do not allow updating chessUsername if it already exists
                System.out.println("Chess username already exists and cannot be updated.");
            }
            
            if (updatedUser.getNationality() != null) {
                changes.put("nationality", updatedUser.getNationality());
            }
            if (updatedUser.getUsername() != null) {
                changes.put("username", updatedUser.getUsername());
            }
            if (updatedUser.getDateOfBirth() != null) {
                // Convert dateOfBirth to Firestore-compatible Timestamp
                if (updatedUser.getDateOfBirth() instanceof String) {
                    try {
                        String dateString = (String) updatedUser.getDateOfBirth();
                        // Parse the date string using LocalDate
                        LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);
                        // Convert LocalDate to Instant at the start of the day in UTC
                        Instant instant = date.atStartOfDay(ZoneOffset.UTC).toInstant();
                        // Convert Instant to Firestore Timestamp
                        changes.put("dateOfBirth", com.google.cloud.Timestamp.ofTimeSecondsAndNanos(
                                instant.getEpochSecond(), instant.getNano()));
                    } catch (DateTimeParseException e) {
                        log.warn("Invalid dateOfBirth format: {}", updatedUser.getDateOfBirth());
                        throw new IllegalArgumentException("Invalid dateOfBirth format. Use 'yyyy-MM-dd' format.");
                    }
                } else {
                    log.warn("Unexpected dateOfBirth type: {}", updatedUser.getDateOfBirth().getClass());
                    throw new IllegalArgumentException(
                            "Unexpected dateOfBirth type. Use 'yyyy-MM-dd' formatted strings.");
                }
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

    // USED IN TESTING
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
