package csd.playermanagement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import csd.playermanagement.DTO.UserDTO;
import csd.playermanagement.Exception.UserNotFoundException;
import csd.playermanagement.Exception.UserTournamentException;
import csd.playermanagement.Model.Tournament;
import csd.playermanagement.Model.User;
import csd.playermanagement.Service.UserService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.concurrent.ExecutionException;

// for Date of birht and timestamp
import com.google.cloud.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;

@ExtendWith(MockitoExtension.class)
public class UserServiceRegisterTest {

    @Mock
    private Firestore firestore;

    @InjectMocks
    private UserService userService;

    @Mock
    private CollectionReference tournamentsCollection;

    @Mock
    private DocumentReference tournamentRef;

    @Mock
    private DocumentSnapshot tournamentSnapshot;

    @Mock
    private CollectionReference usersCollection;

    @Mock
    private Query usersQuery;

    @Mock
    private ApiFuture<QuerySnapshot> userQueryFuture;

    @Mock
    private QuerySnapshot userQuerySnapshot;

    @Mock
    private QueryDocumentSnapshot userSnapshot;

    @Mock
    private DocumentReference userDocRef;

  
    @Test
    void registerUserForTournament_UserSuccessfullyRegistered() throws InterruptedException, ExecutionException {
        // Arrange
        String tournamentId = "tournament123";
        String authId = "user123";

        // Mock Tournament
        when(firestore.collection("Tournaments")).thenReturn(tournamentsCollection);
        when(tournamentsCollection.document(tournamentId)).thenReturn(tournamentRef);
        when(tournamentRef.get()).thenReturn(ApiFutures.immediateFuture(tournamentSnapshot));
        when(tournamentSnapshot.exists()).thenReturn(true); // Tournament exists

        // Mock User
        when(firestore.collection("Users")).thenReturn(usersCollection);
        when(usersCollection.document(authId)).thenReturn(userDocRef);
        when(userDocRef.get()).thenReturn(ApiFutures.immediateFuture(userSnapshot));
        when(userSnapshot.exists()).thenReturn(true); // User exists

        // Mock User Data and Registration History
        User user = new User();
        List<String> registrationHistory = new ArrayList<>();
        user.setRegistrationHistory(registrationHistory); // Empty history initially
        when(userSnapshot.toObject(User.class)).thenReturn(user);
        when(userDocRef.update(eq("registrationHistory"), anyList())).thenReturn(ApiFutures.immediateFuture(null));

        // Act
        String result = userService.registerUserForTournament(tournamentId, authId);

        // Assert
        assertEquals("User successfully registered for the tournament.", result);
        verify(userDocRef).update(eq("registrationHistory"), eq(registrationHistory));
        assertTrue(registrationHistory.contains(tournamentId)); // Ensure tournament ID was added
    }

    @Test
    void registerUserForTournament_TournamentNotFound_ReturnsError() throws Exception {
        // arrange 
        String tournamentId = "invalidTournamentId";
        String authId = "user123";

        // mock 
        when(firestore.collection("Tournaments")).thenReturn(tournamentsCollection);
        when(tournamentsCollection.document(tournamentId)).thenReturn(tournamentRef);
        when(tournamentRef.get()).thenReturn(ApiFutures.immediateFuture(tournamentSnapshot));
        when(tournamentSnapshot.exists()).thenReturn(false);

        // act
        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.registerUserForTournament(tournamentId, authId);
        });

        // assert
        assertEquals("No tournament found with the provided ID.", exception.getMessage());
        verify(tournamentRef).get();
    }


    @Test
    void registerUserForTournament_UserNotFound_ReturnsError() throws InterruptedException, ExecutionException {
        // Arrange
        String tournamentId = "tournament123";
        String authId = "user123";

        // Mock Tournament
        when(firestore.collection("Tournaments")).thenReturn(tournamentsCollection);
        when(tournamentsCollection.document(tournamentId)).thenReturn(tournamentRef);
        when(tournamentRef.get()).thenReturn(ApiFutures.immediateFuture(tournamentSnapshot));
        when(tournamentSnapshot.exists()).thenReturn(true); // Tournament exists

        // Mock User
        when(firestore.collection("Users")).thenReturn(usersCollection);
        when(usersCollection.document(authId)).thenReturn(userDocRef);
        when(userDocRef.get()).thenReturn(ApiFutures.immediateFuture(userSnapshot));
        when(userSnapshot.exists()).thenReturn(false); 

        // act 
        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.registerUserForTournament(tournamentId, authId);
        });

        // Assert: Verify the expected error message is returned
        assertEquals("User not found.", exception.getMessage());
        verify(userDocRef).get();
        
    }

   


    @Test
    void unregisterUserFromTournament_Success() throws InterruptedException, ExecutionException {
        // Arrange
        String tournamentId = "tournament123";
        String authId = "user123";
 
        // Mock Tournament
        when(firestore.collection("Tournaments")).thenReturn(tournamentsCollection);
        when(tournamentsCollection.document(tournamentId)).thenReturn(tournamentRef);
        when(tournamentRef.get()).thenReturn(ApiFutures.immediateFuture(tournamentSnapshot));
        when(tournamentSnapshot.exists()).thenReturn(true); // Tournament exists
 
        // Mock User
        when(firestore.collection("Users")).thenReturn(usersCollection);
        when(usersCollection.document(authId)).thenReturn(userDocRef);
        when(userDocRef.get()).thenReturn(ApiFutures.immediateFuture(userSnapshot));
        when(userSnapshot.exists()).thenReturn(true); // User exists
 
        // Mock User Data and Registration History
        User user = new User();
        List<String> registrationHistory = new ArrayList<>();
        registrationHistory.add(tournamentId); // User is registered for the tournament
        user.setRegistrationHistory(registrationHistory);

        when(userSnapshot.toObject(User.class)).thenReturn(user);
        when(userDocRef.update(eq("registrationHistory"), anyList())).thenReturn(ApiFutures.immediateFuture(null)); // Return a completed Future with a null value, simulating a successful update operation 
                                                                                                                     // in Firestore without interacting with the actual database.
 
        // Act
        String result = userService.unregisterUserForTournament(tournamentId, authId);
 
        // Assert
        assertEquals("User successfully unregistered from the tournament.", result);
        verify(userDocRef).update(eq("registrationHistory"), eq(registrationHistory)); // Verifies that the "registrationHistory" field was updated with the expected list in Firestore.
        assertFalse(registrationHistory.contains(tournamentId)); // Ensure tournament ID was removed
    }

    @Test
    void unregisterUserFromTournament_InvalidUser() throws InterruptedException, ExecutionException {
       // Arrange
       String tournamentId = "tournament123";
       String authId = "user123";

       // Mock Tournament
       when(firestore.collection("Tournaments")).thenReturn(tournamentsCollection);
       when(tournamentsCollection.document(tournamentId)).thenReturn(tournamentRef);
       when(tournamentRef.get()).thenReturn(ApiFutures.immediateFuture(tournamentSnapshot));
       when(tournamentSnapshot.exists()).thenReturn(true); // Tournament exists

       // Mock User
       when(firestore.collection("Users")).thenReturn(usersCollection);
       when(usersCollection.document(authId)).thenReturn(userDocRef);
       when(userDocRef.get()).thenReturn(ApiFutures.immediateFuture(userSnapshot));
       when(userSnapshot.exists()).thenReturn(false); // User does not exists


        // act 
         Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.unregisterUserForTournament(tournamentId, authId);
        });

        // Assert: Verify the expected error message is returned
        assertEquals("User not found.", exception.getMessage());
        verify(userDocRef).get();

    }

    @Test
    void unregisterUserFromTournament_InvalidTournament() throws InterruptedException, ExecutionException {
       // Arrange
       String tournamentId = "tournament123";
       String authId = "user123";

       // Mock Tournament
       when(firestore.collection("Tournaments")).thenReturn(tournamentsCollection);
       when(tournamentsCollection.document(tournamentId)).thenReturn(tournamentRef);
       when(tournamentRef.get()).thenReturn(ApiFutures.immediateFuture(tournamentSnapshot));
       when(tournamentSnapshot.exists()).thenReturn(false); // Tournament does notexists

        // act 
         Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.unregisterUserForTournament(tournamentId, authId);
        });

        // Assert: Verify the expected error message is returned
        assertEquals("No tournament found with the provided ID.", exception.getMessage());
        verify(tournamentRef).get();

    }

}