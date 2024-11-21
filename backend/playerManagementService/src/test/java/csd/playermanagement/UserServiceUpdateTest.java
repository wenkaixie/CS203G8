package csd.playermanagement;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.argThat;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import csd.playermanagement.DTO.UserDTO;
import csd.playermanagement.exception.UserNotFoundException;
import csd.playermanagement.service.UserService;
import csd.shared_library.model.User;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserServiceUpdateTest {

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

    @Mock
    private Firestore firestore;

    @InjectMocks
    private UserService userService;


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