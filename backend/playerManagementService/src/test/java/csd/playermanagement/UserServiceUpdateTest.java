package csd.playermanagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import com.google.cloud.Timestamp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.*;

import csd.playermanagement.DTO.UserDTO;
import csd.playermanagement.Exception.UserNotFoundException;
import csd.playermanagement.Model.User;
import csd.playermanagement.Service.UserService;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserServiceUpdateTest {

    @Mock
    private Firestore firestore;

    @InjectMocks
    private UserService userService;

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

    // mocks to simulate Firestore interactions. Mocks in unit tests don’t automatically handle data changes or state 
    // updates unless you explicitly program them to do so. 
    // This means that when you perform an update in your test, 
    // the mocked Firestore doesn’t automatically update the data it returns on subsequent calls.
    @Test
    void updateUser_Success() throws InterruptedException, ExecutionException {
        // Arrange
        String userAuthId = "userAuthId123";

        // Mock the existing user and its updates
        User User = new User();
        User.setAuthId(userAuthId);

        // Updated UserDTO
        UserDTO UserDto = new UserDTO();
        UserDto.setUsername("newUsername");
        UserDto.setName("New Name");
        UserDto.setPhoneNumber(98765432);
        UserDto.setNationality("New Country");
        UserDto.setChessUsername("newChessUser");
        UserDto.setDateOfBirth("2023-09-15");

        // Mock Firestore interactions for Users collection
        when(firestore.collection("Users")).thenReturn(usersCollection);
        when(usersCollection.whereEqualTo("authId", userAuthId)).thenReturn(usersQuery);
        when(usersQuery.get()).thenReturn(userQueryFuture);
        when(userQueryFuture.get()).thenReturn(userQuerySnapshot);
        when(userQuerySnapshot.isEmpty()).thenReturn(false);
        when(userQuerySnapshot.getDocuments()).thenReturn(Collections.singletonList(userSnapshot));
        when(userSnapshot.toObject(User.class)).thenReturn(User);
        when(userSnapshot.getReference()).thenReturn(userDocRef);

        // Mock update and retrieval
        when(userDocRef.update(anyMap())).thenReturn(ApiFutures.immediateFuture(null));
        when(userDocRef.get()).thenReturn(ApiFutures.immediateFuture(userSnapshot));

        // Mock retrieval after update
        User updatedUser = new User();
        updatedUser.setAuthId(userAuthId);
        updatedUser.setUsername("newUsername");
        updatedUser.setName("New Name");
        updatedUser.setPhoneNumber(98765432);
        updatedUser.setNationality("New Country");
        updatedUser.setChessUsername("newChessUser");
        updatedUser.setDateOfBirth(Timestamp.parseTimestamp("2023-09-15T00:00:00Z"));
        updatedUser.setElo(0); // or set the expected elo value

        // Create a mock for the updated snapshot
        DocumentSnapshot updatedUserSnapshot = Mockito.mock(DocumentSnapshot.class);
        when(updatedUserSnapshot.toObject(User.class)).thenReturn(updatedUser);
        when(userDocRef.get()).thenReturn(ApiFutures.immediateFuture(updatedUserSnapshot));

        // Mock Firestore interactions for Tournaments collection
        CollectionReference tournamentsCollection = Mockito.mock(CollectionReference.class);
        ApiFuture<QuerySnapshot> tournamentsQueryFuture = Mockito.mock(ApiFuture.class);
        QuerySnapshot tournamentsQuerySnapshot = Mockito.mock(QuerySnapshot.class);
        List<QueryDocumentSnapshot> tournamentDocuments = new ArrayList<>();
        QueryDocumentSnapshot tournamentDoc = Mockito.mock(QueryDocumentSnapshot.class);
        tournamentDocuments.add(tournamentDoc);

        when(firestore.collection("Tournaments")).thenReturn(tournamentsCollection);
        when(tournamentsCollection.get()).thenReturn(tournamentsQueryFuture);
        when(tournamentsQueryFuture.get()).thenReturn(tournamentsQuerySnapshot);
        when(tournamentsQuerySnapshot.getDocuments()).thenReturn(tournamentDocuments);

        // Mock interactions within each tournament
        CollectionReference usersSubcollection = Mockito.mock(CollectionReference.class);
        Query userQueryInTournament = Mockito.mock(Query.class);
        ApiFuture<QuerySnapshot> userInTournamentQueryFuture = Mockito.mock(ApiFuture.class);
        QuerySnapshot userInTournamentQuerySnapshot = Mockito.mock(QuerySnapshot.class);
        List<QueryDocumentSnapshot> userInTournamentDocuments = new ArrayList<>();
        QueryDocumentSnapshot userInTournamentDoc = Mockito.mock(QueryDocumentSnapshot.class);
        userInTournamentDocuments.add(userInTournamentDoc);

        DocumentReference userInTournamentDocRef = Mockito.mock(DocumentReference.class);

        // Set up mocks for tournamentDoc
        when(tournamentDoc.getReference()).thenReturn(Mockito.mock(DocumentReference.class));
        when(tournamentDoc.getReference().collection("Users")).thenReturn(usersSubcollection);
        when(usersSubcollection.whereEqualTo("authId", userAuthId)).thenReturn(userQueryInTournament);
        when(userQueryInTournament.get()).thenReturn(userInTournamentQueryFuture);
        when(userInTournamentQueryFuture.get()).thenReturn(userInTournamentQuerySnapshot);
        when(userInTournamentQuerySnapshot.getDocuments()).thenReturn(userInTournamentDocuments);

        when(userInTournamentDoc.getReference()).thenReturn(userInTournamentDocRef);
        when(userInTournamentDocRef.update(anyMap())).thenReturn(ApiFutures.immediateFuture(null));

        // Act
        UserDTO result = userService.updateUserProfile(userAuthId, UserDto);

        // Assert
        assertEquals("newUsername", result.getUsername());
        assertEquals("New Name", result.getName());
        assertEquals("newChessUser", result.getChessUsername());
        assertEquals(98765432, result.getPhoneNumber());
        assertEquals("New Country", result.getNationality());
        assertEquals("2023-09-15", result.getDateOfBirth());

        // Verify Firestore update
        verify(userDocRef).update(argThat(map ->
            map.get("username").equals("newUsername") &&
            map.get("name").equals("New Name") &&
            map.get("chessUsername").equals("newChessUser") &&
            map.get("phoneNumber").equals(98765432) &&
            map.get("nationality").equals("New Country") &&
            map.get("dateOfBirth") instanceof com.google.cloud.Timestamp
        ));
    }

    @Test
    void updateUser_PartialUpdateSuccess() throws InterruptedException, ExecutionException {
        // Arrange
        String userAuthId = "userAuthId123";
    
        // Mock the existing user with initial values
        User existingUser = new User();
        existingUser.setAuthId(userAuthId);
        existingUser.setUsername("oldUsername");
        existingUser.setName("Old Name");
        existingUser.setPhoneNumber(12345678);
        existingUser.setNationality("Old Country");
        existingUser.setChessUsername("oldChessUser");
        existingUser.setDateOfBirth(Timestamp.parseTimestamp("2023-01-01T00:00:00Z"));
        existingUser.setElo(1000);
    
        // Updated UserDTO with partial updates (only username and phoneNumber)
        UserDTO updatedUserDto = new UserDTO();
        updatedUserDto.setUsername("newUsername");
        updatedUserDto.setPhoneNumber(98765432);
        // Other fields are not set (null), so they should not be updated
    
        // Mock Firestore interactions for Users collection
        when(firestore.collection("Users")).thenReturn(usersCollection);
        when(usersCollection.whereEqualTo("authId", userAuthId)).thenReturn(usersQuery);
        when(usersQuery.get()).thenReturn(userQueryFuture);
        when(userQueryFuture.get()).thenReturn(userQuerySnapshot);
        when(userQuerySnapshot.isEmpty()).thenReturn(false); // User exists
        when(userQuerySnapshot.getDocuments()).thenReturn(Collections.singletonList(userSnapshot));
        when(userSnapshot.toObject(User.class)).thenReturn(existingUser);
        when(userSnapshot.getReference()).thenReturn(userDocRef);
    
        // Mock update and retrieval
        when(userDocRef.update(anyMap())).thenReturn(ApiFutures.immediateFuture(null));
        when(userDocRef.get()).thenReturn(ApiFutures.immediateFuture(userSnapshot));
    
        // Mock the updated user after partial update
        User updatedUser = new User();
        updatedUser.setAuthId(userAuthId);
        updatedUser.setUsername("newUsername"); // Updated username
        updatedUser.setName("Old Name"); // Name remains the same
        updatedUser.setPhoneNumber(98765432); // Updated phone number
        updatedUser.setNationality("Old Country"); // Nationality remains the same
        updatedUser.setChessUsername("oldChessUser"); // Chess username remains the same
        updatedUser.setDateOfBirth(Timestamp.parseTimestamp("2023-01-01T00:00:00Z")); // Date of birth remains the same
        updatedUser.setElo(0); // Elo updated to 0 as per method logic
    
        // Create a mock for the updated snapshot
        DocumentSnapshot updatedUserSnapshot = Mockito.mock(DocumentSnapshot.class);
        when(updatedUserSnapshot.toObject(User.class)).thenReturn(updatedUser);
        when(userDocRef.get()).thenReturn(ApiFutures.immediateFuture(updatedUserSnapshot));
    
        // Mock Firestore interactions for Tournaments collection (if necessary)
        CollectionReference tournamentsCollection = Mockito.mock(CollectionReference.class);
        ApiFuture<QuerySnapshot> tournamentsQueryFuture = Mockito.mock(ApiFuture.class);
        QuerySnapshot tournamentsQuerySnapshot = Mockito.mock(QuerySnapshot.class);
        List<QueryDocumentSnapshot> tournamentDocuments = new ArrayList<>();
    
        when(firestore.collection("Tournaments")).thenReturn(tournamentsCollection);
        when(tournamentsCollection.get()).thenReturn(tournamentsQueryFuture);
        when(tournamentsQueryFuture.get()).thenReturn(tournamentsQuerySnapshot);
        when(tournamentsQuerySnapshot.getDocuments()).thenReturn(tournamentDocuments);
    
        // Act
        UserDTO result = userService.updateUserProfile(userAuthId, updatedUserDto);
    
        // Assert
        assertEquals("newUsername", result.getUsername());
        assertEquals("Old Name", result.getName()); // Name should remain unchanged
        assertEquals("oldChessUser", result.getChessUsername()); // Chess username remains the same
        assertEquals(98765432, result.getPhoneNumber()); // Phone number updated
        assertEquals("Old Country", result.getNationality()); // Nationality remains the same
        assertEquals("2023-01-01", result.getDateOfBirth()); // Date of birth remains the same
    
        // Verify that the updated map includes only the intended changes, plus elo = 0
        verify(userDocRef).update(argThat(map ->
            map.size() == 3 &&
            map.get("username").equals("newUsername") &&
            map.get("phoneNumber").equals(98765432) &&
            map.get("elo").equals(0) // Expecting elo to be set to 0
        ));
    }

    @Test
    void updateUser_UserNotFound() throws InterruptedException, ExecutionException {
        // Arrange
        String userAuthId = "nonExistentUserAuthId";

        // Updated UserDTO
        UserDTO updatedUserDto = new UserDTO();
        updatedUserDto.setUsername("newUsername");

        // Mock Firestore interactions for Users collection
        when(firestore.collection("Users")).thenReturn(usersCollection);
        when(usersCollection.whereEqualTo("authId", userAuthId)).thenReturn(usersQuery);
        when(usersQuery.get()).thenReturn(userQueryFuture);
        when(userQueryFuture.get()).thenReturn(userQuerySnapshot);
        when(userQuerySnapshot.isEmpty()).thenReturn(true); // Simulate user not found

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            userService.updateUserProfile(userAuthId, updatedUserDto);
        });

        // Verify that no Firestore interactions were made
        verify(usersCollection, never()).document(userAuthId);

        }

}