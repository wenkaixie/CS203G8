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


    public String registerUserForTournament(String tournamentId, UserDTO userDto) throws InterruptedException, ExecutionException {
        System.out.println("Registering user for tournament...");
    
        DocumentReference tournamentRef = firestore.collection("Tournaments").document(tournamentId);
        DocumentSnapshot tournamentSnapshot = tournamentRef.get().get();
    
        if (!tournamentSnapshot.exists()) {
            throw new TournamentNotFoundException("No tournament found with the provided ID.");
        }
    
        Tournament tournament = tournamentSnapshot.toObject(Tournament.class);
        if (tournament == null) {
            throw new RuntimeException("Error retrieving tournament data.");
        }
    
        // Timestamp startTimestamp = tournament.getStartDatetime();
        // Date startDate = (startTimestamp != null) ? startTimestamp.toDate() : null;
        // Date currentDate = new Date();
    
        // Check if the tournament has already started
        // if (currentDate.after(startDate)) {
        //     return "Cannot register: The tournament has already started.";
        // }

        String status = tournamentSnapshot.getString("status");
        if (status == null) {
            throw new RuntimeException("Error retrieving tournament status.");
        }else if (status.equals("Registration Closed")) {
            throw new RuntimeException("Cannot register: The tournament registration is closed.");
        }
    
        List<String> tournamentUsers = tournament.getUsers();
        System.out.println("Tournament size: " + tournamentUsers.size());

        Long capacityCountLong = tournamentSnapshot.getLong("capacity");
        int capacityCount = (capacityCountLong != null) ? capacityCountLong.intValue() : 0;

        Long eloRequirementLong = tournamentSnapshot.getLong("eloRequirement");
        int eloRequirement = (eloRequirementLong != null) ? eloRequirementLong.intValue() : 0;

        Long ageLimitLomg = tournamentSnapshot.getLong("ageLimit");
        int ageLimit = (ageLimitLomg != null) ? ageLimitLomg.intValue() : 0;
    
        System.out.println("userDto: " + userDto);
        System.out.println("eloRequirement: " + eloRequirement + " capacityCount: " + capacityCount);
    
        CollectionReference usersRef = firestore.collection("Users");
        ApiFuture<QuerySnapshot> querySnapshot = usersRef.whereEqualTo("authId", userDto.getAuthId()).get();
    
        if (querySnapshot.get().isEmpty()) {
            throw new UserNotFoundException("User not found.");
        }
    
        List<QueryDocumentSnapshot> userDocuments = querySnapshot.get().getDocuments();
        if (userDocuments.isEmpty()) {
            throw new RuntimeException("Error retrieving user data.");
        }
    
        DocumentSnapshot userSnapshot = userDocuments.get(0);
        if (userSnapshot == null) {
            throw new RuntimeException("Error retrieving user data.");
        }
    
        User user = userSnapshot.toObject(User.class);
        if (user == null) {
            throw new RuntimeException("Error retrieving user data.");
        }

        // Print user's registration history to the logs
        List<String> registrationHistory = user.getRegistrationHistory();
        System.out.println("User's Registration History: " + registrationHistory);
    
        if (tournamentUsers.contains(user.getAuthId()) || registrationHistory.contains(tournamentId)) {
            throw new UserTournamentException("User already registered for this tournament.");
        }
    
        if (user.getElo() < eloRequirement) {
            throw new UserTournamentException("User does not meet the Elo requirement for this tournament.");
        }
    
        if (tournamentUsers.size() >= capacityCount) {
            throw new UserTournamentException("Tournament is at full capacity.");
        }

        /// Get user's date of birth
        Timestamp dateOfBirthTimestamp = user.getDateOfBirth();
        if (dateOfBirthTimestamp == null) {
            throw new RuntimeException("User's date of birth is not available.");
        }

        // Calculate user's age
        Date dateOfBirthDate = dateOfBirthTimestamp.toDate();
        Instant dobInstant = dateOfBirthDate.toInstant();
        LocalDate dobLocalDate = dobInstant.atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate currentDateLocal = LocalDate.now();

        int age = Period.between(dobLocalDate, currentDateLocal).getYears();

        System.out.println("User's age: " + age);

        // Check if user's age meets the tournament's age requirements
        if (age < ageLimit) {
            throw new UserTournamentException("User does not meet the age requirement for this tournament.");
        }
    
        // If all checks pass, register the user
        tournamentUsers.add(user.getUid());
        registrationHistory.add(tournamentId);
    
        ApiFuture<WriteResult> tournamentUpdate = tournamentRef.update("users", tournamentUsers);
        ApiFuture<WriteResult> userUpdate = usersRef.document(user.getUid()).update("registrationHistory", registrationHistory);
        tournamentUpdate.get();
        userUpdate.get();
    
        return "User successfully registered for the tournament.";
    }

    public String unregisterUserFromTournament(String tournamentId, UserDTO userDto) throws InterruptedException, ExecutionException {
        System.out.println("Unregistering user from tournament...");
        
        // Fetch the tournament reference and snapshot
        DocumentReference tournamentRef = firestore.collection("Tournaments").document(tournamentId);
        DocumentSnapshot tournamentSnapshot = tournamentRef.get().get();
        
        if (!tournamentSnapshot.exists()) {
            throw new TournamentNotFoundException("No tournament found with the provided ID.");
        }
        
        Tournament tournament = tournamentSnapshot.toObject(Tournament.class);
        if (tournament == null) {
            throw new RuntimeException("Error retrieving tournament data.");
        }
        
        CollectionReference usersRef = firestore.collection("Users");
        ApiFuture<QuerySnapshot> querySnapshot = usersRef.whereEqualTo("authId", userDto.getAuthId()).get();
        
        if (querySnapshot.get().isEmpty()) {
            throw new UserNotFoundException("User not found.");
        }
    
        DocumentSnapshot userSnapshot = querySnapshot.get().getDocuments().get(0);  // First matching user
        User user = userSnapshot.toObject(User.class);
    
        if (user == null) {
            throw new RuntimeException("Error retrieving user data.");
        }
        
        // Check if the user is registered in the tournament
        if (!user.getRegistrationHistory().contains(tournamentId)) {
            throw new UserTournamentException("User is not registered for this tournament.");
        }
        
        // Remove tournamentId from the user's registration history
        List<String> updatedRegistrationHistory = new ArrayList<>(user.getRegistrationHistory());
        updatedRegistrationHistory.remove(tournamentId);
        
        // Update the user's document in Firestore
        ApiFuture<WriteResult> userUpdateFuture = userSnapshot.getReference().update("registrationHistory", updatedRegistrationHistory);
        
        // Remove user from the tournament's users list
        List<String> updatedTournamentUsers = new ArrayList<>(tournament.getUsers());
        updatedTournamentUsers.remove(userDto.getUid());
        
        // Update the tournament's document in Firestore
        ApiFuture<WriteResult> tournamentUpdateFuture = tournamentRef.update("users", updatedTournamentUsers);
        
        // Wait for both updates to complete
        userUpdateFuture.get();  // handle any potential exceptions here
        tournamentUpdateFuture.get();  // handle any potential exceptions here
        System.out.println("User successfully unregistered from the tournament.");
        return "User successfully unregistered from the tournament.";
    }

    public UserDTO updateUserProfile(String userId, UserDTO updatedUser) throws InterruptedException, ExecutionException {
        System.out.println("Updating User profile for user ID: " + userId);
        System.out.println("Received updated user:" + updatedUser);
        
        CollectionReference usersRef = firestore.collection("Users");
        ApiFuture<QuerySnapshot> querySnapshot = usersRef.whereEqualTo("authId", userId).get();
        
        List<QueryDocumentSnapshot> userDocuments = querySnapshot.get().getDocuments();
        System.out.println(userDocuments);
        
        if (userDocuments.isEmpty()) {
            throw new UserNotFoundException("User not found.");
        }
        
        DocumentSnapshot userSnapshot = userDocuments.get(0);
        DocumentReference userRef = userSnapshot.getReference();
        
        Map<String, Object> changes = new HashMap<>();
        
        // Update username if provided
        if (updatedUser.getUsername() != null) {
            changes.put("username", updatedUser.getUsername());
        }
        
        // Update name if provided
        if (updatedUser.getName() != null) {
            changes.put("name", updatedUser.getName());
        }
        
        // Update phone number if provided
        if (updatedUser.getPhoneNumber() != null) {
            changes.put("phoneNumber", updatedUser.getPhoneNumber());
        }
        
        // Update nationality if provided
        if (updatedUser.getNationality() != null) {
            changes.put("nationality", updatedUser.getNationality());
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

    public User getUserbyId(String userId) throws InterruptedException, ExecutionException {
        CollectionReference usersRef = firestore.collection("Users");
        
        ApiFuture<QuerySnapshot> querySnapshot = usersRef.whereEqualTo("authId", userId).get();
    
        List<QueryDocumentSnapshot> userDocuments = querySnapshot.get().getDocuments();
    
        if (userDocuments.isEmpty()) {
            throw new UserNotFoundException("User not found.");
        }
    
        DocumentSnapshot userSnapshot = userDocuments.get(0);
        //System.out.println(userSnapshot.toObject(User.class));
    
        return userSnapshot.toObject(User.class);
    }

    // Method to get user's rank based on elo
    public int getUserRank(String userId) throws InterruptedException, ExecutionException {
        System.out.println("Getting user rank..." + userId);
        CollectionReference usersRef = firestore.collection("Users");
        ApiFuture<QuerySnapshot> querySnapshot = usersRef.orderBy("elo", Query.Direction.DESCENDING).get();
        
        List<QueryDocumentSnapshot> userDocuments = querySnapshot.get().getDocuments();
    
        int rank = 1;
    
        for (QueryDocumentSnapshot document : userDocuments) {
            User user = document.toObject(User.class);
            System.out.println("User: " + user);
    
            // Check if the userId matches
            //System.out.println("Auth id is: " + user.getAuthId());
            if (user.getAuthId().equals(userId)) {
                System.out.println("User found: " + user);
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


    // public int getUserRank(String userId) throws InterruptedException, ExecutionException {
    //     System.out.println("Getting user rank for userId: " + userId);
    
    //     // Firestore reference to the 'Users' collection
    //     CollectionReference usersRef = firestore.collection("Users");
    
    //     // Query users ordered by 'elo' in descending order, and by 'username' in ascending order if 'elo' is the same
    //     ApiFuture<QuerySnapshot> querySnapshot = usersRef
    //         .orderBy("elo", Query.Direction.DESCENDING)  // Primary sort by elo in descending order
    //         .orderBy("username", Query.Direction.ASCENDING)  // Secondary sort by username in ascending order
    //         .get();
    
    //     List<QueryDocumentSnapshot> userDocuments = querySnapshot.get().getDocuments();
    
    //     int rank = 1;  // Start rank at 1
    //     int previousRank = 1;  // Keep track of the previous rank
    //     Integer previousElo = null;  // Keep track of the previous elo for comparison
    
    //     // Loop through all the user documents
    //     for (QueryDocumentSnapshot document : userDocuments) {
    //         User user = document.toObject(User.class);
    //         System.out.println("Processing user: " + user.getUsername() + " with elo: " + user.getElo());
    
    //         // Check if the user has a valid 'authId' and 'elo'
    //         if (user.getAuthId() != null && user.getElo() != null) {
    //             // If the current user's elo is different from the previous user's elo, increment the rank
    //             if (previousElo != null && !user.getElo().equals(previousElo)) {
    //                 rank = previousRank + 1;  // Increment rank only if elo is different
    //             }
    //             // Set previousElo and previousRank for the next iteration
    //             previousElo = user.getElo();
    //             previousRank = rank;
    
    //             // Check if the current user's authId matches the input userId
    //             if (user.getAuthId().equals(userId)) {
    //                 System.out.println("User found: " + user.getUsername() + " with rank: " + rank);
    //                 return rank;  // Return the rank when the user is found
    //             }
    //         } else {
    //             // Log the case where the user has a null elo or authId
    //             System.err.println("Error: User has null authId or elo. Skipping user: " + user.getUsername());
    //         }
    //     }
    
    //     // If no user with the specified userId is found, throw an exception
    //     throw new UserNotFoundException("User not found.");
    // }
    
    
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
        newUser.setUid(generatedUid);

        newUserProfile.put("uid", generatedUid);
        documentReference.set(newUserProfile, SetOptions.merge());

        DocumentSnapshot userSnapshot = documentReference.get().get();
        User createdUser = userSnapshot.toObject(User.class);

        return createdUser;
    }
    
    

}
