// UserServiceUnitTest.java
package csd.playermanagement;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.ExecutionException;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import csd.playermanagement.Model.User;
import csd.playermanagement.Service.UserService;
import csd.playermanagement.Model.Tournament;

public class UserServiceTest {

    // @Mock
    // private Firestore firestore;

    // @Mock
    // private CollectionReference collectionReference;

    // @Mock
    // private DocumentReference documentReference;

    // @Mock
    // private DocumentSnapshot documentSnapshot;

    // @Mock
    // private QuerySnapshot querySnapshot;

    // @Mock
    // private QueryDocumentSnapshot queryDocumentSnapshot;

    // @Mock
    // private ApiFuture<DocumentSnapshot> documentSnapshotFuture;

    // @Mock
    // private ApiFuture<QuerySnapshot> querySnapshotFuture;

    // @Mock
    // private ApiFuture<WriteResult> writeResultFuture;

    // @InjectMocks
    // private UserService userService;

    // @BeforeEach
    // public void setUp() {
    //     MockitoAnnotations.openMocks(this);
    // }

    // @Test
    // public void testRegisterUserForTournament_TournamentNotFound() throws Exception {
    //     // Arrange
    //     String tournamentId = "tournament123";
    //     String userId = "user123";
    //     when(firestore.collection("Tournaments")).thenReturn(collectionReference);
    //     when(collectionReference.document(tournamentId)).thenReturn(documentReference);
    //     when(documentReference.get()).thenReturn(documentSnapshotFuture);
    //     when(documentSnapshotFuture.get()).thenReturn(documentSnapshot);
    //     when(documentSnapshot.exists()).thenReturn(false);

    //     // Act
    //     String result = userService.registerUserForTournament(tournamentId, userId);

    //     // Assert & Verify
    //     assertEquals("Tournament not found.", result);
    //     verify(documentReference, times(1)).get();
    // }

    // @Test
    // public void testUpdateUserProfile_UserNotFound() throws Exception {
    //     // Arrange
    //     String userId = "user123";
    //     // Use setters like this:
    //     User updatedUser = new User();
    //     updatedUser.setUsername("newUser");
    //     updatedUser.setElo(0);
    //     updatedUser.setEmail("newEmail@example.com");
    //     updatedUser.setUid(userId);
    //     updatedUser.setName("John Doe");
    //     updatedUser.setPhoneNumber(123456789);
    //     updatedUser.setNationality("USA");
    //     updatedUser.setUid("newUserId");

    //     when(firestore.collection("Users")).thenReturn(collectionReference);
    //     when(collectionReference.whereEqualTo("uid", userId)).thenReturn(null);
    //     when(querySnapshotFuture.get()).thenReturn(querySnapshot);
    //     when(querySnapshot.getDocuments()).thenReturn(Collections.emptyList());

    //     // Act & Assert
    //     assertThrows(RuntimeException.class, () -> {
    //         userService.updateUserProfile(userId, updatedUser);
    //     });

    //     // Verify
    //     verify(collectionReference, times(1)).whereEqualTo("uid", userId);
    // }
}