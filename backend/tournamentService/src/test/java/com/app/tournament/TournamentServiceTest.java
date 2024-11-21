// package com.csd.tournament.service;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.*;

// import com.csd.shared_library.DTO.TournamentDTO;
// import com.csd.shared_library.enumerator.TournamentType;
// import com.csd.shared_library.model.Tournament;
// import com.csd.tournament.service.EliminationService;
// import com.csd.tournament.service.RoundRobinService;
// import com.csd.tournament.service.TournamentSchedulerService;
// import com.csd.tournament.service.TournamentService;
// import com.google.api.core.ApiFuture;
// import com.google.cloud.firestore.*;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.*;
// import org.mockito.junit.jupiter.MockitoExtension;

// import java.time.Instant;
// import java.util.List;
// import java.util.ArrayList;
// import java.util.Map;
// import java.util.concurrent.ExecutionException;

// @ExtendWith(MockitoExtension.class)
// public class TournamentServiceTest {

//     @InjectMocks
//     private TournamentService tournamentService;

//     @Mock
//     private Firestore firestore;

//     @Mock
//     private TournamentSchedulerService tournamentSchedulerService;

//     @Mock
//     private RoundRobinService roundRobinService;

//     @Mock
//     private EliminationService eliminationService;

//     @Mock
//     private CollectionReference tournamentsCollection;

//     @Mock
//     private DocumentReference documentReference;

//     @Mock
//     private ApiFuture<WriteResult> writeResultApiFuture;

//     @BeforeEach
//     public void setUp() {
//         when(firestore.collection("Tournaments")).thenReturn(tournamentsCollection);
//     }

//     @Test
//     public void testCreateTournament_Success() throws ExecutionException, InterruptedException {
//         // Arrange
//         TournamentDTO tournamentDTO = new TournamentDTO();
//         tournamentDTO.setAdminId("admin123");
//         tournamentDTO.setType(TournamentType.ROUND_ROBIN);
//         tournamentDTO.setAgeLimit(18); // Corrected: int
//         tournamentDTO.setName("Championship");
//         tournamentDTO.setDescription("Annual Championship");
//         tournamentDTO.setEloRequirement(1500L);
//         tournamentDTO.setLocation("New York");
//         tournamentDTO.setMinSignups(2);
//         tournamentDTO.setCapacity(16); // Corrected: int
//         tournamentDTO.setPrize(5000L);
//         tournamentDTO.setStartDatetime(Instant.now());
//         tournamentDTO.setEndDatetime(Instant.now().plusSeconds(86400));

//         String generatedId = "tournament123";
        
//         when(tournamentsCollection.document()).thenReturn(documentReference);
//         when(documentReference.getId()).thenReturn(generatedId);
//         when(documentReference.set(any(Tournament.class))).thenReturn(writeResultApiFuture);
//         WriteResult writeResult = mock(WriteResult.class);
//         when(writeResultApiFuture.get()).thenReturn(writeResult);
//         when(writeResult.getUpdateTime()).thenReturn(Timestamp.now());

//         // Act
//         String result = tournamentService.createTournament(tournamentDTO);

//         // Assert
//         assertEquals(generatedId, result);
//         verify(tournamentSchedulerService, times(1)).scheduleTournamentRoundGeneration(
//                 generatedId, 
//                 tournamentDTO.getStartDatetime(), 
//                 tournamentDTO.getType()
//         );
//         verify(documentReference, times(1)).set(any(Tournament.class));
//     }

//     @Test
//     public void testCreateTournament_FirestoreException() throws ExecutionException, InterruptedException {
//         // Arrange
//         TournamentDTO tournamentDTO = new TournamentDTO();
//         tournamentDTO.setAdminId("admin123");
//         tournamentDTO.setType(TournamentType.ROUND_ROBIN);
//         tournamentDTO.setAgeLimit(18); // Corrected: int
//         tournamentDTO.setName("Championship");
//         tournamentDTO.setDescription("Annual Championship");
//         tournamentDTO.setEloRequirement(1500L);
//         tournamentDTO.setLocation("New York");
//         tournamentDTO.setMinSignups(2);
//         tournamentDTO.setCapacity(16); // Corrected: int
//         tournamentDTO.setPrize(5000L);
//         tournamentDTO.setStartDatetime(Instant.now());
//         tournamentDTO.setEndDatetime(Instant.now().plusSeconds(86400));

//         when(tournamentsCollection.document()).thenReturn(documentReference);
//         when(documentReference.getId()).thenReturn("tournament123");
//         when(documentReference.set(any(Tournament.class))).thenReturn(writeResultApiFuture);
//         when(writeResultApiFuture.get()).thenThrow(new ExecutionException(new Throwable("Firestore Error")));

//         // Act & Assert
//         ExecutionException exception = assertThrows(ExecutionException.class, () -> {
//             tournamentService.createTournament(tournamentDTO);
//         });

//         assertTrue(exception.getCause().getMessage().contains("Firestore Error"));
//         verify(tournamentSchedulerService, times(1)).scheduleTournamentRoundGeneration(
//                 "tournament123", 
//                 tournamentDTO.getStartDatetime(), 
//                 tournamentDTO.getType()
//         );
//         verify(documentReference, times(1)).set(any(Tournament.class));
//     }

//     @Test
//     public void testGetTournamentById_Success() throws ExecutionException, InterruptedException {
//         // Arrange
//         String tournamentID = "tournament123";
//         DocumentSnapshot mockDocumentSnapshot = mock(DocumentSnapshot.class);
//         Tournament mockTournament = new Tournament();
//         mockTournament.setTid(tournamentID);
//         when(mockDocumentSnapshot.exists()).thenReturn(true);
//         when(mockDocumentSnapshot.toObject(Tournament.class)).thenReturn(mockTournament);
//         ApiFuture<DocumentSnapshot> apiFuture = mock(ApiFuture.class);
//         when(apiFuture.get()).thenReturn(mockDocumentSnapshot);
//         when(documentReference.get()).thenReturn(apiFuture);
//         when(tournamentsCollection.document(tournamentID)).thenReturn(documentReference);

//         // Act
//         Tournament result = tournamentService.getTournamentById(tournamentID);

//         // Assert
//         assertNotNull(result);
//         assertEquals(tournamentID, result.getTid());
//         verify(firestore.collection("Tournaments").document(tournamentID), times(1)).get();
//     }

//     @Test
//     public void testGetTournamentById_NotFound() throws ExecutionException, InterruptedException {
//         // Arrange
//         String tournamentID = "nonexistent123";
//         DocumentSnapshot mockDocumentSnapshot = mock(DocumentSnapshot.class);
//         when(mockDocumentSnapshot.exists()).thenReturn(false);
//         ApiFuture<DocumentSnapshot> apiFuture = mock(ApiFuture.class);
//         when(apiFuture.get()).thenReturn(mockDocumentSnapshot);
//         when(documentReference.get()).thenReturn(apiFuture);
//         when(tournamentsCollection.document(tournamentID)).thenReturn(documentReference);

//         // Act & Assert
//         RuntimeException exception = assertThrows(RuntimeException.class, () -> {
//             tournamentService.getTournamentById(tournamentID);
//         });

//         assertEquals("Tournament not found with ID: " + tournamentID, exception.getMessage());
//         verify(firestore.collection("Tournaments").document(tournamentID), times(1)).get();
//     }

//     @Test
//     public void testDeleteTournament_Success() throws ExecutionException, InterruptedException {
//         // Arrange
//         String tournamentID = "tournament123";
//         DocumentSnapshot mockDocumentSnapshot = mock(DocumentSnapshot.class);
//         when(mockDocumentSnapshot.exists()).thenReturn(true);
//         ApiFuture<DocumentSnapshot> getFuture = mock(ApiFuture.class);
//         when(getFuture.get()).thenReturn(mockDocumentSnapshot);
//         when(documentReference.get()).thenReturn(getFuture);
//         when(tournamentsCollection.document(tournamentID)).thenReturn(documentReference);
        
//         ApiFuture<WriteResult> deleteFuture = mock(ApiFuture.class);
//         when(documentReference.delete()).thenReturn(deleteFuture);
//         when(deleteFuture.get()).thenReturn(mock(WriteResult.class));

//         // Act
//         assertDoesNotThrow(() -> {
//             tournamentService.deleteTournament(tournamentID);
//         });

//         // Assert
//         verify(firestore.collection("Tournaments").document(tournamentID), times(1)).get();
//         verify(documentReference, times(1)).delete();
//     }

//     @Test
//     public void testDeleteTournament_NotFound() throws ExecutionException, InterruptedException {
//         // Arrange
//         String tournamentID = "nonexistent123";
//         DocumentSnapshot mockDocumentSnapshot = mock(DocumentSnapshot.class);
//         when(mockDocumentSnapshot.exists()).thenReturn(false);
//         ApiFuture<DocumentSnapshot> getFuture = mock(ApiFuture.class);
//         when(getFuture.get()).thenReturn(mockDocumentSnapshot);
//         when(documentReference.get()).thenReturn(getFuture);
//         when(tournamentsCollection.document(tournamentID)).thenReturn(documentReference);

//         // Act & Assert
//         RuntimeException exception = assertThrows(RuntimeException.class, () -> {
//             tournamentService.deleteTournament(tournamentID);
//         });

//         assertEquals("Tournament not found with ID: " + tournamentID, exception.getMessage());
//         verify(firestore.collection("Tournaments").document(tournamentID), times(1)).get();
//         verify(documentReference, never()).delete();
//     }

//     @Test
//     public void testAddUserToTournament_Success() throws ExecutionException, InterruptedException {
//         // Arrange
//         String tournamentID = "tournament123";
//         String authId = "user123";
    
//         // Mock user in main Users collection
//         DocumentReference userRef = mock(DocumentReference.class);
//         when(firestore.collection("Users").document(authId)).thenReturn(userRef);
//         DocumentSnapshot userDoc = mock(DocumentSnapshot.class);
//         ApiFuture<DocumentSnapshot> userFuture = mock(ApiFuture.class);
//         when(userFuture.get()).thenReturn(userDoc);
//         when(userRef.get()).thenReturn(userFuture);
//         when(userDoc.exists()).thenReturn(true);
//         when(userDoc.getString("name")).thenReturn("John Doe");
//         when(userDoc.getString("nationality")).thenReturn("USA");
//         when(userDoc.getLong("elo")).thenReturn(1600L);
//         when(userDoc.get("registrationHistory")).thenReturn(new ArrayList<String>());
    
//         // Mock tournament document
//         DocumentReference tournamentRef = mock(DocumentReference.class);
//         when(firestore.collection("Tournaments").document(tournamentID)).thenReturn(tournamentRef);
//         DocumentSnapshot tournamentDoc = mock(DocumentSnapshot.class);
//         ApiFuture<DocumentSnapshot> tournamentFuture = mock(ApiFuture.class);
//         when(tournamentFuture.get()).thenReturn(tournamentDoc);
//         when(tournamentRef.get()).thenReturn(tournamentFuture);
//         when(tournamentDoc.exists()).thenReturn(true);
    
//         // Mock Users subcollection
//         CollectionReference usersSubcollection = mock(CollectionReference.class);
//         when(tournamentRef.collection("Users")).thenReturn(usersSubcollection);
//         DocumentReference tournamentUserRef = mock(DocumentReference.class);
//         when(usersSubcollection.document(authId)).thenReturn(tournamentUserRef);
//         DocumentSnapshot tournamentUserDoc = mock(DocumentSnapshot.class);
//         ApiFuture<DocumentSnapshot> tournamentUserFuture = mock(ApiFuture.class);
//         when(tournamentUserFuture.get()).thenReturn(tournamentUserDoc);
//         when(tournamentUserRef.get()).thenReturn(tournamentUserFuture);
//         when(tournamentUserDoc.exists()).thenReturn(false);
    
//         // Mock setting user in tournament's Users subcollection
//         ApiFuture<WriteResult> setFuture = mock(ApiFuture.class);
//         when(tournamentUserRef.set(anyMap())).thenReturn(setFuture);
//         when(setFuture.get()).thenReturn(mock(WriteResult.class));
//         ApiFuture<WriteResult> updateFuture = mock(ApiFuture.class);
//         when(userRef.update(eq("registrationHistory"), any())).thenReturn(updateFuture);
//         when(updateFuture.get()).thenReturn(mock(WriteResult.class));
    
//         // Act
//         String result = tournamentService.addUserToTournament(tournamentID, authId);
    
//         // Assert
//         assertEquals("User added successfully.", result);
//         verify(userRef, times(1)).get();
//         verify(tournamentRef, times(1)).get();
//         verify(tournamentUserRef, times(1)).get();
//         verify(tournamentUserRef, times(1)).set(anyMap());
//         verify(userRef, times(1)).update(eq("registrationHistory"), any());
//     }

//     @Test
//     public void testAddUserToTournament_UserAlreadyInTournament() throws ExecutionException, InterruptedException {
//         // Arrange
//         String tournamentID = "tournament123";
//         String authId = "user123";
    
//         // Mock user in main Users collection
//         DocumentReference userRef = mock(DocumentReference.class);
//         when(firestore.collection("Users").document(authId)).thenReturn(userRef);
//         DocumentSnapshot userDoc = mock(DocumentSnapshot.class);
//         ApiFuture<DocumentSnapshot> userFuture = mock(ApiFuture.class);
//         when(userFuture.get()).thenReturn(userDoc);
//         when(userRef.get()).thenReturn(userFuture);
//         when(userDoc.exists()).thenReturn(true);
//         when(userDoc.getString("name")).thenReturn("John Doe");
//         when(userDoc.getString("nationality")).thenReturn("USA");
//         when(userDoc.getLong("elo")).thenReturn(1600L);
//         when(userDoc.get("registrationHistory")).thenReturn(new ArrayList<String>());
    
//         // Mock tournament document
//         DocumentReference tournamentRef = mock(DocumentReference.class);
//         when(firestore.collection("Tournaments").document(tournamentID)).thenReturn(tournamentRef);
//         DocumentSnapshot tournamentDoc = mock(DocumentSnapshot.class);
//         ApiFuture<DocumentSnapshot> tournamentFuture = mock(ApiFuture.class);
//         when(tournamentFuture.get()).thenReturn(tournamentDoc);
//         when(tournamentRef.get()).thenReturn(tournamentFuture);
//         when(tournamentDoc.exists()).thenReturn(true);
    
//         // Mock Users subcollection
//         CollectionReference usersSubcollection = mock(CollectionReference.class);
//         when(tournamentRef.collection("Users")).thenReturn(usersSubcollection);
//         DocumentReference tournamentUserRef = mock(DocumentReference.class);
//         when(usersSubcollection.document(authId)).thenReturn(tournamentUserRef);
//         DocumentSnapshot tournamentUserDoc = mock(DocumentSnapshot.class);
//         ApiFuture<DocumentSnapshot> tournamentUserFuture = mock(ApiFuture.class);
//         when(tournamentUserFuture.get()).thenReturn(tournamentUserDoc);
//         when(tournamentUserRef.get()).thenReturn(tournamentUserFuture);
//         when(tournamentUserDoc.exists()).thenReturn(true);
    
//         // Act
//         String result = tournamentService.addUserToTournament(tournamentID, authId);
    
//         // Assert
//         assertEquals("User is already part of the tournament.", result);
//         verify(tournamentUserRef, times(1)).get();
//         verify(tournamentUserRef, never()).set(anyMap());
//         verify(userRef, never()).update(eq("registrationHistory"), any());
//     }

//     @Test
//     public void testAddUserToTournament_UserNotFound() throws ExecutionException, InterruptedException {
//         // Arrange
//         String tournamentID = "tournament123";
//         String authId = "nonexistentUser";
    
//         // Mock user in main Users collection
//         DocumentReference userRef = mock(DocumentReference.class);
//         when(firestore.collection("Users").document(authId)).thenReturn(userRef);
//         DocumentSnapshot userDoc = mock(DocumentSnapshot.class);
//         ApiFuture<DocumentSnapshot> userFuture = mock(ApiFuture.class);
//         when(userFuture.get()).thenReturn(userDoc);
//         when(userRef.get()).thenReturn(userFuture);
//         when(userDoc.exists()).thenReturn(false);
    
//         // Act & Assert
//         RuntimeException exception = assertThrows(RuntimeException.class, () -> {
//             tournamentService.addUserToTournament(tournamentID, authId);
//         });
    
//         assertEquals("User not found with ID: " + authId, exception.getMessage());
//         verify(userRef, times(1)).get();
//         verify(tournamentSchedulerService, never()).scheduleTournamentRoundGeneration(anyString(), any(), any());
//     }

//     @Test
//     public void testGetEligibleTournamentsOfUser_Success() throws ExecutionException, InterruptedException {
//         // Arrange
//         String authId = "user123";
    
//         // Mock user document
//         DocumentReference userDocRef = mock(DocumentReference.class);
//         when(firestore.collection("Users").document(authId)).thenReturn(userDocRef);
//         DocumentSnapshot userDoc = mock(DocumentSnapshot.class);
//         ApiFuture<DocumentSnapshot> userFuture = mock(ApiFuture.class);
//         when(userFuture.get()).thenReturn(userDoc);
//         when(userDocRef.get()).thenReturn(userFuture);
//         when(userDoc.exists()).thenReturn(true);
//         when(userDoc.getLong("elo")).thenReturn(1600L);
//         when(userDoc.get("dateOfBirth", Instant.class)).thenReturn(Instant.parse("2000-01-01T00:00:00Z"));
//         when(userDoc.get("registrationHistory")).thenReturn(new ArrayList<String>());
    
//         // Mock tournaments collection
//         CollectionReference tournamentsCollection = mock(CollectionReference.class);
//         when(firestore.collection("Tournaments")).thenReturn(tournamentsCollection);
//         ApiFuture<QuerySnapshot> tournamentsFuture = mock(ApiFuture.class);
//         when(tournamentsCollection.get()).thenReturn(tournamentsFuture);
        
//         // Mock QuerySnapshot
//         QuerySnapshot querySnapshot = mock(QuerySnapshot.class);
//         when(tournamentsFuture.get()).thenReturn(querySnapshot);
//         List<DocumentSnapshot> tournamentDocuments = new ArrayList<>();
//         DocumentSnapshot tournament1 = mock(DocumentSnapshot.class);
//         tournamentDocuments.add(tournament1);
//         when(querySnapshot.getDocuments()).thenReturn(tournamentDocuments);
    
//         // Setup tournament1 document
//         when(tournament1.exists()).thenReturn(true);
//         Tournament tournamentObj = new Tournament();
//         tournamentObj.setTid("tournament1");
//         tournamentObj.setStatus("Open");
//         tournamentObj.setCapacity(10); // Corrected: int
//         tournamentObj.setEloRequirement(1500L);
//         tournamentObj.setAgeLimit(18); // Corrected: int
//         tournamentObj.setStartDatetime(Instant.now().plusSeconds(86400)); // 1 day later
//         tournamentObj.setEndDatetime(Instant.now().plusSeconds(172800)); // 2 days later
//         when(tournament1.toObject(Tournament.class)).thenReturn(tournamentObj);
//         when(tournament1.getTimestamp("startDatetime")).thenReturn(Timestamp.ofTimeSecondsAndNanos(
//                 tournamentObj.getStartDatetime().getEpochSecond(),
//                 tournamentObj.getStartDatetime().getNano()));
//         when(tournament1.getTimestamp("endDatetime")).thenReturn(Timestamp.ofTimeSecondsAndNanos(
//                 tournamentObj.getEndDatetime().getEpochSecond(),
//                 tournamentObj.getEndDatetime().getNano()));
//         when(tournament1.getLong("currentRound")).thenReturn(1L);
    
//         // Mock Users subcollection for tournament1
//         CollectionReference usersSubcollection1 = mock(CollectionReference.class);
//         when(tournament1.getReference().collection("Users")).thenReturn(usersSubcollection1);
//         ApiFuture<QuerySnapshot> usersFuture1 = mock(ApiFuture.class);
//         when(usersSubcollection1.get()).thenReturn(usersFuture1);
//         QuerySnapshot usersSnapshot1 = mock(QuerySnapshot.class);
//         when(usersFuture1.get()).thenReturn(usersSnapshot1);
//         when(usersSnapshot1.size()).thenReturn(5);
    
//         // Act
//         List<Tournament> eligibleTournaments = tournamentService.getEligibleTournamentsOfUser(authId);
    
//         // Assert
//         // Depending on the logic, assert that tournament1 is eligible
//         // This example assumes tournament1 meets all eligibility criteria
//         assertEquals(1, eligibleTournaments.size());
//         assertEquals("tournament1", eligibleTournaments.get(0).getTid());
//     }
// }
