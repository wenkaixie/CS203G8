// package csd.playermanagement;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertFalse;
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
    
//         // Existing user data
//         User existingUser = new User();
//         existingUser.setUid(userUid);
//         existingUser.setAuthId(userAuthId);
//         existingUser.setEmail("oldemail@example.com");
//         existingUser.setElo(1200);
    
//         // Updated user data
//         UserDTO updatedUserDto = new UserDTO();
//         updatedUserDto.setUid(userUid);
//         updatedUserDto.setAuthId(userAuthId);
//         updatedUserDto.setEmail("newemail@example.com");
//         updatedUserDto.setElo(1300);
    
//         // Mock Firestore interactions
//         when(firestore.collection("Users")).thenReturn(usersCollection);
//         when(usersCollection.whereEqualTo("authId", userAuthId)).thenReturn(usersQuery);
//         when(usersQuery.get()).thenReturn(userQueryFuture);
//         when(userQueryFuture.get()).thenReturn(userQuerySnapshot);
//         when(userQuerySnapshot.isEmpty()).thenReturn(false);
//         when(userQuerySnapshot.getDocuments()).thenReturn(Collections.singletonList(userSnapshot));
//         when(userSnapshot.exists()).thenReturn(true);
//         when(userSnapshot.toObject(User.class)).thenReturn(existingUser);
//         when(userSnapshot.getReference()).thenReturn(userDocRef);
    
//         // Mock userDocRef.get() and update() calls
//         when(userDocRef.get()).thenReturn(ApiFutures.immediateFuture(userSnapshot));
//         when(userDocRef.update(anyMap())).thenReturn(ApiFutures.immediateFuture(null));
    
//         // Update the existingUser object to reflect the changes
//         existingUser.setEmail(updatedUserDto.getEmail());
//         existingUser.setElo(updatedUserDto.getElo());
    
//         // Act
//         Map<String, Object> result = userService.updateUserProfile(userAuthId, updatedUserDto);
    
//         // Assert
//         assertEquals("newemail@example.com", result.get("email"));
//         assertEquals(1300, result.get("elo"));
//         verify(userDocRef).update(anyMap());
//     }

//     @Test
//     void updateUser_PartialUpdate() throws InterruptedException, ExecutionException {
//         // Arrange
//         String userAuthId = "userAuthId123";
//         String userUid = "userUid123";

//         // Existing user data
//         User existingUser = new User();
//         existingUser.setUid(userUid);
//         existingUser.setAuthId(userAuthId);
//         existingUser.setName("Old Name");
//         existingUser.setPhoneNumber(12345678);
//         existingUser.setNationality("Old Country");

//         // Updated user data with only phoneNumber and nationality updated
//         UserDTO updatedUserDto = new UserDTO();
//         updatedUserDto.setUid(userUid);
//         updatedUserDto.setAuthId(userAuthId);
//         updatedUserDto.setPhoneNumber(87654321);
//         updatedUserDto.setNationality("New Country");

//         // Mock Firestore interactions
//         when(firestore.collection("Users")).thenReturn(usersCollection);
//         when(usersCollection.whereEqualTo("authId", userAuthId)).thenReturn(usersQuery);
//         when(usersQuery.get()).thenReturn(userQueryFuture);
//         when(userQueryFuture.get()).thenReturn(userQuerySnapshot);
//         when(userQuerySnapshot.isEmpty()).thenReturn(false);
//         when(userQuerySnapshot.getDocuments()).thenReturn(Collections.singletonList(userSnapshot));
//         when(userSnapshot.exists()).thenReturn(true);
//         when(userSnapshot.toObject(User.class)).thenReturn(existingUser);
//         when(userSnapshot.getReference()).thenReturn(userDocRef);

//         // Mock userRef.get() and update() calls
//         when(userDocRef.get()).thenReturn(ApiFutures.immediateFuture(userSnapshot));
//         when(userDocRef.update(anyMap())).thenReturn(ApiFutures.immediateFuture(null));

//         // Update the existingUser object to reflect the changes
//         existingUser.setPhoneNumber(updatedUserDto.getPhoneNumber());
//         existingUser.setNationality(updatedUserDto.getNationality());

//         // Act
//         UserDTO result = userService.updateUserProfile(userAuthId, updatedUserDto);

//         // Assert
//         assertEquals("Old Name", result.get("name")); // Name remains unchanged
//         assertEquals("87654321", result.get("phoneNumber").toString()); // Updated
//         assertEquals("New Country", result.get("nationality")); // Updated
//         verify(userDocRef).update(anyMap());
//     }

//     @Test  
//     void updateUser_UserNotFound() throws InterruptedException, ExecutionException {
//         // Arrange
//         String userAuthId = "auth123";
//         UserDTO userDto = new UserDTO();

//         userDto.setUsername("testuser");
//         userDto.setEmail("wajnwf@gmail.com");
//         userDto.setName("Test User");
//         userDto.setPhoneNumber(12345678);
//         userDto.setNationality("Singapore");
//         userDto.setChessUsername("chessuser");
//         userDto.setDateOfBirth("1999-01-01");

//         // Mock the user data retrieval
//         when(firestore.collection("Users")).thenReturn(usersCollection);
//         when(usersCollection.whereEqualTo("authId", userAuthId)).thenReturn(usersQuery);
//         when(usersQuery.get()).thenReturn(userQueryFuture);
//         when(userQueryFuture.get()).thenReturn(userQuerySnapshot);
//         when(userQuerySnapshot.isEmpty()).thenReturn(true);

//         // Act
//         Exception exception =  assertThrows(RuntimeException.class, () -> {
//             userService.updateUserProfile(userAuthId, userDto);
//         });

       
//         // assert
//         assertEquals("No user found with the provided authId.", exception.getMessage());
//         verify(userDocRef, Mockito.never()).update(anyMap());
//         verify(userDocRef, Mockito.never()).set(anyMap());

//     }

//     @Test
//     void updateUserProfile_InvalidDateFormat_LogsErrorAndSkipsUpdate() throws InterruptedException, ExecutionException {
//         // Arrange
//         String userAuthId = "userAuthId123";
//         String userUid = "userUid123";

//         // Existing user data
//         User existingUser = new User();
//         existingUser.setUid(userUid);
//         existingUser.setAuthId(userAuthId);

//         // UserDTO with invalid date format
//         UserDTO updatedUserDto = new UserDTO();
//         updatedUserDto.setUid(userUid);
//         updatedUserDto.setAuthId(userAuthId);
//         updatedUserDto.setDateOfBirth("1999-13-32"); // Invalid date

//         // Mock Firestore interactions
//         when(firestore.collection("Users")).thenReturn(usersCollection);
//         when(usersCollection.whereEqualTo("authId", userAuthId)).thenReturn(usersQuery);
//         when(usersQuery.get()).thenReturn(userQueryFuture);
//         when(userQueryFuture.get()).thenReturn(userQuerySnapshot);
//         when(userQuerySnapshot.isEmpty()).thenReturn(false);
//         when(userQuerySnapshot.getDocuments()).thenReturn(Collections.singletonList(userSnapshot));
//         when(userSnapshot.exists()).thenReturn(true);
//         when(userSnapshot.toObject(User.class)).thenReturn(existingUser);
//         when(userSnapshot.getReference()).thenReturn(userDocRef);

//         // Correctly mock userDocRef.get()
//         when(userDocRef.get()).thenReturn(ApiFutures.immediateFuture(userSnapshot));

//         // Correctly mock userDocRef.update() to prevent NPE
//         when(userDocRef.update(anyMap())).thenReturn(ApiFutures.immediateFuture(Mockito.mock(WriteResult.class)));

//         // Act
//         UserDTO result = userService.updateUserProfile(userAuthId, updatedUserDto);

//         // Assert
//         assertFalse(result.containsKey("dateOfBirth")); // Date not added to result
//         verify(userDocRef, never()).update(argThat(map -> map.containsKey("dateOfBirth")));
//     }


// }