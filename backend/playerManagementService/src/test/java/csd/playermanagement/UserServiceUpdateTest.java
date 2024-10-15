package csd.playermanagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Test
    void updateUser_Success() throws InterruptedException, ExecutionException {
        // Arrange
        String userId = "userAuthId123";
        String userUid = "userUid123";
    
        // Existing user data
        User existingUser = new User();
        existingUser.setUid(userUid);
        existingUser.setAuthId(userId);
        existingUser.setEmail("oldemail@example.com");
        existingUser.setElo(1200);
    
        // Updated user data
        UserDTO updatedUserDto = new UserDTO();
        updatedUserDto.setUid(userUid);
        updatedUserDto.setAuthId(userId);
        updatedUserDto.setEmail("newemail@example.com");
        updatedUserDto.setElo(1300);
    
        // Mock existing user retrieval
        when(firestore.collection("Users")).thenReturn(usersCollection);
        when(usersCollection.whereEqualTo("authID", userId)).thenReturn(usersQuery);
        when(usersQuery.get()).thenReturn(userQueryFuture);
        when(userQueryFuture.get()).thenReturn(userQuerySnapshot);
        when(userQuerySnapshot.isEmpty()).thenReturn(false);
        when(userQuerySnapshot.getDocuments()).thenReturn(Collections.singletonList(userSnapshot));
        when(userSnapshot.exists()).thenReturn(true);
        when(userSnapshot.toObject(User.class)).thenReturn(existingUser);
        when(userSnapshot.getReference()).thenReturn(userDocRef);
    
        // Mock userRef.get()
        ApiFuture<DocumentSnapshot> userRefGetFuture = Mockito.mock(ApiFuture.class);
        DocumentSnapshot updatedUserSnapshot = Mockito.mock(DocumentSnapshot.class);
    
        when(userDocRef.get()).thenReturn(userRefGetFuture);
        when(userRefGetFuture.get()).thenReturn(updatedUserSnapshot);
        when(updatedUserSnapshot.toObject(User.class)).thenReturn(existingUser);
    
        // Mock user update
        when(userDocRef.update(anyMap())).thenReturn(ApiFutures.immediateFuture(null));
    
        // Update the existingUser object to reflect the changes
        existingUser.setEmail(updatedUserDto.getEmail());
        existingUser.setElo(updatedUserDto.getElo());
    
        // Act
        UserDTO result = userService.updateUserProfile(userId, updatedUserDto);
    
        // Assert
        assertEquals("newemail@example.com", result.getEmail());
        assertEquals(1300, result.getElo());
        verify(userDocRef).update(anyMap());
    }

    @Test
    void updateUser_PartialUpdate() throws InterruptedException, ExecutionException {
        // Arrange
        String userId = "userAuthId123";
        String userUid = "userUid123";

        // Existing user data
        User existingUser = new User();
        existingUser.setUid(userUid);
        existingUser.setAuthId(userId);
        existingUser.setName("Old Name");
        existingUser.setPhoneNumber(12345678);
        existingUser.setNationality("Old Country");

        // Updated user data with only phoneNumber and nationality updated
        UserDTO updatedUserDto = new UserDTO();
        updatedUserDto.setUid(userUid);
        updatedUserDto.setAuthId(userId);
        updatedUserDto.setPhoneNumber(87654321);
        updatedUserDto.setNationality("New Country");

        // Mock existing user retrieval
        when(firestore.collection("Users")).thenReturn(usersCollection);
        when(usersCollection.whereEqualTo("authID", userId)).thenReturn(usersQuery);
        when(usersQuery.get()).thenReturn(userQueryFuture);
        when(userQueryFuture.get()).thenReturn(userQuerySnapshot);
        when(userQuerySnapshot.isEmpty()).thenReturn(false);
        when(userQuerySnapshot.getDocuments()).thenReturn(Collections.singletonList(userSnapshot));
        when(userSnapshot.exists()).thenReturn(true);
        when(userSnapshot.toObject(User.class)).thenReturn(existingUser);
        when(userSnapshot.getReference()).thenReturn(userDocRef);

        // Mock userRef.get()
        ApiFuture<DocumentSnapshot> userRefGetFuture = Mockito.mock(ApiFuture.class);
        DocumentSnapshot updatedUserSnapshot = Mockito.mock(DocumentSnapshot.class);

        when(userDocRef.get()).thenReturn(userRefGetFuture);
        when(userRefGetFuture.get()).thenReturn(updatedUserSnapshot);
        when(updatedUserSnapshot.toObject(User.class)).thenReturn(existingUser);

        // Mock user update
        when(userDocRef.update(anyMap())).thenReturn(ApiFutures.immediateFuture(null));

        // Update the existingUser object to reflect the changes
        existingUser.setPhoneNumber(updatedUserDto.getPhoneNumber());
        existingUser.setNationality(updatedUserDto.getNationality());

        // Act
        UserDTO result = userService.updateUserProfile(userId, updatedUserDto);

        // Assert
        assertEquals("Old Name", result.getName()); // Name remains unchanged
        assertEquals("87654321", result.getPhoneNumber().toString()); // Updated
        assertEquals("New Country", result.getNationality()); // Updated
        verify(userDocRef).update(anyMap());
    }
}