// package csd.playermanagement;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertFalse;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import static org.junit.jupiter.api.Assertions.assertNull;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.mockito.ArgumentMatchers.anyMap;
// import static org.mockito.ArgumentMatchers.argThat;
// import static org.mockito.Mockito.never;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// import java.util.Collections;
// import java.util.Map;
// import java.util.concurrent.ExecutionException;

// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.Mockito;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.mockito.junit.jupiter.MockitoSettings;
// import org.mockito.quality.Strictness;

// import com.google.api.core.ApiFuture;
// import com.google.api.core.ApiFutures;
// import com.google.cloud.firestore.*;

// import csd.playermanagement.DTO.UserDTO;
// import csd.playermanagement.Model.User;
// import csd.playermanagement.Service.UserService;


// @ExtendWith(MockitoExtension.class)
// @MockitoSettings(strictness = Strictness.LENIENT)
// public class UserServiceUpdateTest {

//     @Mock
//     private Firestore firestore;

//     @InjectMocks
//     private UserService userService;

//     @Mock
//     private CollectionReference usersCollection;

//     @Mock
//     private Query usersQuery;

//     @Mock
//     private ApiFuture<QuerySnapshot> userQueryFuture;

//     @Mock
//     private QuerySnapshot userQuerySnapshot;

//     @Mock
//     private QueryDocumentSnapshot userSnapshot;

//     @Mock
//     private DocumentReference userDocRef;

//     @Test
//     void updateUser_Success() throws InterruptedException, ExecutionException {
//         // Arrange
//         String userAuthId = "userAuthId123";
//         String userUid = "userUid123";

//         // Mock the existing user and its updates
//         User User = new User();
//         User.setUid(userUid);
//         User.setAuthId(userAuthId);

//         // Updated UserDTO
//         UserDTO UserDto = new UserDTO();
//         UserDto.setUsername("newUsername");
//         UserDto.setName("New Name");
//         UserDto.setPhoneNumber(98765432);
//         UserDto.setNationality("New Country");
//         UserDto.setChessUsername("newChessUser");
//         UserDto.setDateOfBirth("2023-09-15");

//         // Mock Firestore interactions
//         when(firestore.collection("Users")).thenReturn(usersCollection);
//         when(usersCollection.whereEqualTo("authId", userAuthId)).thenReturn(usersQuery);
//         when(usersQuery.get()).thenReturn(userQueryFuture);
//         when(userQueryFuture.get()).thenReturn(userQuerySnapshot);
//         when(userQuerySnapshot.isEmpty()).thenReturn(false);
//         when(userQuerySnapshot.getDocuments()).thenReturn(Collections.singletonList(userSnapshot));
//         when(userSnapshot.toObject(User.class)).thenReturn(User);
//         when(userSnapshot.getReference()).thenReturn(userDocRef);

//         // Mock update and retrieval
//         when(userDocRef.update(anyMap())).thenReturn(ApiFutures.immediateFuture(null));
//         when(userDocRef.get()).thenReturn(ApiFutures.immediateFuture(userSnapshot));

//         // Act
//         UserDTO result = userService.updateUserProfile(userAuthId, UserDto);

//         // Assert
//         assertEquals("newUsername", result.getUsername());
//         assertEquals("New Name", result.getName());
//         assertEquals("newChessUser", result.getChessUsername());
//         assertEquals(98765432, result.getPhoneNumber());
//         assertEquals("New Country", result.getNationality());
//         assertEquals("2023/09/15", result.getDateOfBirth());

//         // Verify Firestore update
//         verify(userDocRef).update(argThat(map ->
//             map.get("username").equals("newUsername") &&
//             map.get("name").equals("New Name") &&
//             map.get("chessUsername").equals("newChessUser") &&
//             map.get("phoneNumber").equals(98765432) &&
//             map.get("nationality").equals("New Country") &&
//             map.get("dateOfBirth") instanceof com.google.cloud.Timestamp
//         ));
//     }

//     // @Test
//     // void updateUser_PartialUpdate() throws InterruptedException, ExecutionException {
//     //     // Arrange
//     //     String userAuthId = "userAuthId123";
//     //     String userUid = "userUid123";

//     //     // Existing user data
//     //     User existingUser = new User();
//     //     existingUser.setUid(userUid);
//     //     existingUser.setAuthId(userAuthId);
//     //     existingUser.setName("Old Name");
//     //     existingUser.setPhoneNumber(12345678);
//     //     existingUser.setNationality("Old Country");

//     //     // Updated user data with only phoneNumber and nationality updated
//     //     UserDTO UserDto = new UserDTO();
//     //     UserDto.setUid(userUid);
//     //     UserDto.setAuthId(userAuthId);
//     //     UserDto.setPhoneNumber(87654321);
//     //     UserDto.setNationality("New Country");

//     //     // Mock Firestore interactions
//     //     when(firestore.collection("Users")).thenReturn(usersCollection);
//     //     when(usersCollection.whereEqualTo("authId", userAuthId)).thenReturn(usersQuery);
//     //     when(usersQuery.get()).thenReturn(userQueryFuture);
//     //     when(userQueryFuture.get()).thenReturn(userQuerySnapshot);
//     //     when(userQuerySnapshot.isEmpty()).thenReturn(false);
//     //     when(userQuerySnapshot.getDocuments()).thenReturn(Collections.singletonList(userSnapshot));
//     //     when(userSnapshot.exists()).thenReturn(true);
//     //     when(userSnapshot.toObject(User.class)).thenReturn(existingUser);
//     //     when(userSnapshot.getReference()).thenReturn(userDocRef);

//     //     // Mock userRef.get() and update() calls
//     //     when(userDocRef.get()).thenReturn(ApiFutures.immediateFuture(userSnapshot));
//     //     when(userDocRef.update(anyMap())).thenReturn(ApiFutures.immediateFuture(null));

//     //     // Act
//     //     UserDTO result = userService.updateUserProfile(userAuthId, UserDto);

//     //     // Assert
//     //     assertNotNull(result);
//     //     assertEquals("Old Name", result.getName()); // Name remains unchanged
//     //     assertEquals(87654321, result.getPhoneNumber()); // Phone number updated
//     //     assertEquals("New Country", result.getNationality()); // Nationality updated
//     //     verify(userDocRef).update(anyMap());
//     // }

//     // @Test  
//     // void updateUser_UserNotFound() throws InterruptedException, ExecutionException {
//     //     // Arrange
//     //     String userAuthId = "auth123";
//     //     UserDTO userDto = new UserDTO();

//     //     userDto.setUsername("testuser");
//     //     userDto.setEmail("wajnwf@gmail.com");
//     //     userDto.setName("Test User");
//     //     userDto.setPhoneNumber(12345678);
//     //     userDto.setNationality("Singapore");
//     //     userDto.setChessUsername("chessuser");
//     //     userDto.setDateOfBirth("1999-01-01");

//     //     // Mock the user data retrieval
//     //     when(firestore.collection("Users")).thenReturn(usersCollection);
//     //     when(usersCollection.whereEqualTo("authId", userAuthId)).thenReturn(usersQuery);
//     //     when(usersQuery.get()).thenReturn(userQueryFuture);
//     //     when(userQueryFuture.get()).thenReturn(userQuerySnapshot);
//     //     when(userQuerySnapshot.isEmpty()).thenReturn(true);

//     //     // Act
//     //     Exception exception = assertThrows(RuntimeException.class, () -> {
//     //         userService.updateUserProfile(userAuthId, userDto);
//     //     });

//     //     // Assert
//     //     assertEquals("No user found with the provided authId.", exception.getMessage());
//     //     verify(userDocRef, Mockito.never()).update(anyMap());
//     //     verify(userDocRef, Mockito.never()).set(anyMap());
//     // }

//     // @Test
//     // void updateUserProfile_InvalidDateFormat_LogsErrorAndSkipsUpdate() throws InterruptedException, ExecutionException {
//     //     // Arrange
//     //     String userAuthId = "userAuthId123";
//     //     String userUid = "userUid123";

//     //     // Existing user data
//     //     User existingUser = new User();
//     //     existingUser.setUid(userUid);
//     //     existingUser.setAuthId(userAuthId);

//     //     // UserDTO with invalid date format
//     //     UserDTO UserDto = new UserDTO();
//     //     UserDto.setUid(userUid);
//     //     UserDto.setAuthId(userAuthId);
//     //     UserDto.setDateOfBirth("1999-13-32"); // Invalid date

//     //     // Mock Firestore interactions
//     //     when(firestore.collection("Users")).thenReturn(usersCollection);
//     //     when(usersCollection.whereEqualTo("authId", userAuthId)).thenReturn(usersQuery);
//     //     when(usersQuery.get()).thenReturn(userQueryFuture);
//     //     when(userQueryFuture.get()).thenReturn(userQuerySnapshot);
//     //     when(userQuerySnapshot.isEmpty()).thenReturn(false);
//     //     when(userQuerySnapshot.getDocuments()).thenReturn(Collections.singletonList(userSnapshot));
//     //     when(userSnapshot.exists()).thenReturn(true);
//     //     when(userSnapshot.toObject(User.class)).thenReturn(existingUser);
//     //     when(userSnapshot.getReference()).thenReturn(userDocRef);

//     //     // Mock userDocRef.get()
//     //     when(userDocRef.get()).thenReturn(ApiFutures.immediateFuture(userSnapshot));

//     //     // Mock userDocRef.update() to prevent NPE
//     //     when(userDocRef.update(anyMap())).thenReturn(ApiFutures.immediateFuture(Mockito.mock(WriteResult.class)));

//     //     // Act: Call the service method
//     //     UserDTO result = userService.updateUserProfile(userAuthId, UserDto);

//     //     // Assert: Validate the UserDTO response
//     //     assertNotNull(result);
//     //     assertEquals(userAuthId, result.getAuthId());
//     //     assertEquals(userUid, result.getUid());
//     //     assertNull(result.getDateOfBirth()); // DateOfBirth should be null due to invalid input

//     //     // Verify that the update() method was called, but not with "dateOfBirth"
//     //     verify(userDocRef, never()).update(argThat(map -> map.containsKey("dateOfBirth")));
//     // }


// }