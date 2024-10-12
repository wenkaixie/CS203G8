package csd.playermanagement.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Period;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.time.format.DateTimeParseException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteResult;

import csd.playermanagement.DTO.UserDTO;
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
            throw new RuntimeException("No tournament found with the provided ID.");
        }
    
        Tournament tournament = tournamentSnapshot.toObject(Tournament.class);
        if (tournament == null) {
            throw new RuntimeException("Error retrieving tournament data.");
        }
    
        Timestamp startTimestamp = tournament.getStartDatetime();
        Date startDate = (startTimestamp != null) ? startTimestamp.toDate() : null;
        Date currentDate = new Date();
    
        // Check if the tournament has already started
        if (currentDate.after(startDate)) {
            return "Cannot register: The tournament has already started.";
        }
    
        List<String> tournamentUsers = tournament.getUsers();

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
            return "User not found.";
        }
    
        List<QueryDocumentSnapshot> userDocuments = querySnapshot.get().getDocuments();
        if (userDocuments.isEmpty()) {
            return "User not found.";
        }
    
        DocumentSnapshot userSnapshot = userDocuments.get(0);
        if (userSnapshot == null) {
            return "Error retrieving user data.";
        }
    
        User user = userSnapshot.toObject(User.class);
        if (user == null) {
            return "Error retrieving user data.";
        }
    
        if (tournamentUsers.contains(user.getAuthId())) {
            return "User already registered for this tournament.";
        }
    
        if (user.getElo() < eloRequirement) {
            return "User does not meet the Elo requirement for this tournament.";
        }
    
        if (tournamentUsers.size() >= capacityCount) {
            return "Tournament is at full capacity.";
        }

        /// Get user's date of birth
        Timestamp dateOfBirthTimestamp = user.getDateOfBirth();
        if (dateOfBirthTimestamp == null) {
            return "User's date of birth is not available.";
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
            return "User does not meet the age requirement for this tournament.";
        }
    
        // If all checks pass, register the user
        tournamentUsers.add(user.getAuthId());
        List<String> registrationHistory = user.getRegistrationHistory();
        registrationHistory.add(tournamentId);
    
        ApiFuture<WriteResult> tournamentUpdate = tournamentRef.update("users", tournamentUsers);
        ApiFuture<WriteResult> userUpdate = usersRef.document(user.getUid()).update("registrationHistory", registrationHistory);
        tournamentUpdate.get();
        userUpdate.get();
    
        return "User successfully registered for the tournament.";
    }

    public Map<String, Object> updateUserProfile(String userId, UserDTO updatedUser) throws InterruptedException, ExecutionException {
        System.out.println("Updating User profile for user ID: " + userId);
        System.out.println("Received updated user:" + updatedUser);
        
        CollectionReference usersRef = firestore.collection("Users");
        ApiFuture<QuerySnapshot> querySnapshot = usersRef.whereEqualTo("authId", userId).get();
        
        List<QueryDocumentSnapshot> userDocuments = querySnapshot.get().getDocuments();
        System.out.println(userDocuments);
        
        if (userDocuments.isEmpty()) {
            throw new RuntimeException("No user found with the provided authId.");
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
        
        Map<String, Object> response = new HashMap<>();
        response.put("authId", fullUpdatedUser.getAuthId());
        
        // To reformat the dateOfBirth back to "yyyy/MM/dd"
        if (fullUpdatedUser.getDateOfBirth() != null) {
            // Create a DateTimeFormatter with the desired format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd").withZone(ZoneId.systemDefault());
            // Convert the Timestamp to Instant and format it
            String formattedDate = formatter.format(fullUpdatedUser.getDateOfBirth().toDate().toInstant());
            // Add the formatted date to the response
            response.put("dateOfBirth", formattedDate);
        }
        
        response.put("elo", fullUpdatedUser.getElo());
        response.put("email", fullUpdatedUser.getEmail());
        response.put("name", fullUpdatedUser.getName());
        response.put("nationality", fullUpdatedUser.getNationality());
        response.put("phoneNumber", fullUpdatedUser.getPhoneNumber());
        response.put("uid", fullUpdatedUser.getUid());
        response.put("username", fullUpdatedUser.getUsername());
        
        return response;
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
            User user = document.toObject(User.class);
            
            if (user != null) {
                usersList.add(user);
            }
        }
        return usersList;
    }

    public User getUserbyId(String userId) throws InterruptedException, ExecutionException {
        CollectionReference usersRef = firestore.collection("Users");
        
        ApiFuture<QuerySnapshot> querySnapshot = usersRef.whereEqualTo("authId", userId).get();
    
        List<QueryDocumentSnapshot> userDocuments = querySnapshot.get().getDocuments();
    
        if (userDocuments.isEmpty()) {
            throw new RuntimeException("No user found with the provided authId.");
        }
    
        DocumentSnapshot userSnapshot = userDocuments.get(0);
        System.out.println(userSnapshot.toObject(User.class));
    
        return userSnapshot.toObject(User.class);
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
        newUser.setUid(generatedUid);
    
        newUserProfile.put("uid", generatedUid);
        documentReference.set(newUserProfile, SetOptions.merge());
    
        DocumentSnapshot userSnapshot = documentReference.get().get();
        User createdUser = userSnapshot.toObject(User.class);
    
        return createdUser;
    }
}
