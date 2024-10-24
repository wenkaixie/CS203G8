package csd.playermanagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.google.cloud.Timestamp;


import java.util.Collections;
import java.util.Map;
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
        String userUid = "userUid123";

        // Mock the existing user and its updates
        User User = new User();
        User.setUid(userUid);
        User.setAuthId(userAuthId);

        // Updated UserDTO
        UserDTO UserDto = new UserDTO();
        UserDto.setUsername("newUsername");
        UserDto.setName("New Name");
        UserDto.setPhoneNumber(98765432);
        UserDto.setNationality("New Country");
        UserDto.setChessUsername("newChessUser");
        UserDto.setDateOfBirth("2023-09-15");

        // Mock Firestore interactions
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
        updatedUser.setUid(userUid);
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
    void updateUser_PartialUpdate() throws InterruptedException, ExecutionException {
        // Arrange
        String userAuthId = "userAuthId123";
        String userUid = "userUid123";

        // Existing user data
        User existingUser = new User();
        existingUser.setUid(userUid);
        existingUser.setAuthId(userAuthId);
        existingUser.setName("Old Name");
        existingUser.setPhoneNumber(12345678);
        existingUser.setNationality("Old Country");

        // Updated user data with only phoneNumber and nationality updated
        UserDTO userDto = new UserDTO();
        userDto.setUid(userUid);
        userDto.setAuthId(userAuthId);
        userDto.setPhoneNumber(87654321);
        userDto.setNationality("New Country");

        // Mock Firestore interactions
        when(firestore.collection("Users")).thenReturn(usersCollection);
        when(usersCollection.whereEqualTo("authId", userAuthId)).thenReturn(usersQuery);
        when(usersQuery.get()).thenReturn(userQueryFuture);
        when(userQueryFuture.get()).thenReturn(userQuerySnapshot);
        when(userQuerySnapshot.isEmpty()).thenReturn(false);
        when(userQuerySnapshot.getDocuments()).thenReturn(Collections.singletonList(userSnapshot));
        when(userSnapshot.exists()).thenReturn(true);
        when(userSnapshot.toObject(User.class)).thenReturn(existingUser);
        when(userSnapshot.getReference()).thenReturn(userDocRef);

        // Mock update operation
        when(userDocRef.update(anyMap())).thenReturn(ApiFutures.immediateFuture(null));

        // Create an updated User object
        User updatedUser = new User();
        updatedUser.setUid(userUid);
        updatedUser.setAuthId(userAuthId);
        updatedUser.setName("Old Name"); // Name remains unchanged
        updatedUser.setPhoneNumber(87654321); // Phone number updated
        updatedUser.setNationality("New Country"); // Nationality updated
        updatedUser.setElo(0);

        // Create a mock for the updated DocumentSnapshot
        DocumentSnapshot updatedUserSnapshot = Mockito.mock(DocumentSnapshot.class);
        when(updatedUserSnapshot.toObject(User.class)).thenReturn(updatedUser);
        when(userDocRef.get()).thenReturn(ApiFutures.immediateFuture(updatedUserSnapshot));

        // Act
        UserDTO result = userService.updateUserProfile(userAuthId, userDto);

        // Assert
        assertNotNull(result);
        assertEquals("Old Name", result.getName()); // Name remains unchanged
        assertEquals(87654321, result.getPhoneNumber()); // Phone number updated
        assertEquals("New Country", result.getNationality()); // Nationality updated
        verify(userDocRef).update(anyMap());
    }

    @Test  
    void updateUser_UserNotFound() throws InterruptedException, ExecutionException {
        // Arrange
        String userAuthId = "auth123";
        UserDTO userDto = new UserDTO();

        userDto.setUsername("testuser");
        userDto.setEmail("wajnwf@gmail.com");
        userDto.setName("Test User");
        userDto.setPhoneNumber(12345678);
        userDto.setNationality("Singapore");
        userDto.setChessUsername("chessuser");
        userDto.setDateOfBirth("1999-01-01");

        // Mock the user data retrieval
        when(firestore.collection("Users")).thenReturn(usersCollection);
        when(usersCollection.whereEqualTo("authId", userAuthId)).thenReturn(usersQuery);
        when(usersQuery.get()).thenReturn(userQueryFuture);
        when(userQueryFuture.get()).thenReturn(userQuerySnapshot);
        when(userQuerySnapshot.isEmpty()).thenReturn(true);

        // Act
        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.updateUserProfile(userAuthId, userDto);
        });

        // Assert
        assertEquals("User not found.", exception.getMessage());
        verify(userDocRef, Mockito.never()).update(anyMap());
        verify(userDocRef, Mockito.never()).set(anyMap());
    }

    @Test
    void updateUser_InvalidDateFormat_ShouldProceedWithoutDateUpdate() throws InterruptedException, ExecutionException {
        // Arrange
        String userAuthId = "userAuthId123";
        String userUid = "userUid123";

        // Existing user data
        User existingUser = new User();
        existingUser.setUid(userUid);
        existingUser.setAuthId(userAuthId);
        existingUser.setName("Old Name");
        existingUser.setPhoneNumber(12345678);
        existingUser.setNationality("Old Country");
        existingUser.setDateOfBirth(Timestamp.parseTimestamp("2020-01-01T00:00:00Z")); // existing dateOfBirth

        // Updated user data with invalid dateOfBirth
        UserDTO userDto = new UserDTO();
        userDto.setUid(userUid);
        userDto.setAuthId(userAuthId);
        userDto.setPhoneNumber(87654321);
        userDto.setNationality("New Country");
        userDto.setDateOfBirth("invalid-date-format"); // Invalid date format

        // Mock Firestore interactions
        when(firestore.collection("Users")).thenReturn(usersCollection);
        when(usersCollection.whereEqualTo("authId", userAuthId)).thenReturn(usersQuery);
        when(usersQuery.get()).thenReturn(userQueryFuture);
        when(userQueryFuture.get()).thenReturn(userQuerySnapshot);
        when(userQuerySnapshot.isEmpty()).thenReturn(false);
        when(userQuerySnapshot.getDocuments()).thenReturn(Collections.singletonList(userSnapshot));
        when(userSnapshot.exists()).thenReturn(true);
        when(userSnapshot.toObject(User.class)).thenReturn(existingUser);
        when(userSnapshot.getReference()).thenReturn(userDocRef);

        // Mock update operation
        when(userDocRef.update(anyMap())).thenReturn(ApiFutures.immediateFuture(null));

        // Create an updated User object representing the state after update
        User updatedUser = new User();
        updatedUser.setUid(userUid);
        updatedUser.setAuthId(userAuthId);
        updatedUser.setName("Old Name"); // Name remains unchanged
        updatedUser.setPhoneNumber(87654321); // Phone number updated
        updatedUser.setNationality("New Country"); // Nationality updated
        updatedUser.setDateOfBirth(existingUser.getDateOfBirth()); // dateOfBirth remains unchanged
        updatedUser.setElo(0);

        // Mock retrieval after update
        DocumentSnapshot updatedUserSnapshot = Mockito.mock(DocumentSnapshot.class);
        when(updatedUserSnapshot.toObject(User.class)).thenReturn(updatedUser);
        when(userDocRef.get()).thenReturn(ApiFutures.immediateFuture(updatedUserSnapshot));

        // Act
        UserDTO result = userService.updateUserProfile(userAuthId, userDto);

        // Assert
        assertNotNull(result);
        assertEquals("Old Name", result.getName()); // Name remains unchanged
        assertEquals(87654321, result.getPhoneNumber()); // Phone number updated
        assertEquals("New Country", result.getNationality()); // Nationality updated
        // Check that dateOfBirth remains unchanged
        assertEquals("2020-01-01", result.getDateOfBirth()); // Adjust date format as needed

        // Verify that update was called with the correct data, excluding dateOfBirth
        verify(userDocRef).update(argThat(map ->
            map.containsKey("phoneNumber") &&
            map.get("phoneNumber").equals(87654321) &&
            map.containsKey("nationality") &&
            map.get("nationality").equals("New Country") &&
            !map.containsKey("dateOfBirth")
        ));
    }


}