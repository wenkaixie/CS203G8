package csd.playermanagement;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import csd.playermanagement.service.UserService;
import csd.shared_library.model.User;

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