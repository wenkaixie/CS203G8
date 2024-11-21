package com.app.tournament;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;

import csd.shared_library.DTO.TournamentDTO;
import csd.shared_library.model.Tournament;
import csd.tournament.service.TournamentSchedulerService;
import csd.tournament.service.TournamentService;

@ExtendWith(MockitoExtension.class)
public class TournamentServiceCRUDTest {

    // Mocks for Firestore and its related classes
    @Mock
    private Firestore firestore;

    @Mock
    private CollectionReference collectionRefMock;

    @Mock
    private DocumentReference documentRefMock;

    // Mock for TournamentSchedulerService
    @Mock
    private TournamentSchedulerService tournamentSchedulerService;

    // InjectMocks to inject the above mocks into TournamentService
    @InjectMocks
    private TournamentService tournamentService;

    // Mocks for DocumentSnapshot and ApiFuture
    @Mock
    private DocumentSnapshot documentSnapshot;

    @Mock
    private ApiFuture<WriteResult> writeResultFutureMock;

    @Mock
    private QuerySnapshot querySnapshot;

    @Mock
    private ApiFuture<QuerySnapshot> querySnapshotFuture;

    @Mock
    private ApiFuture<DocumentSnapshot> documentSnapshotFuture;

    @BeforeEach
    public void setUp() throws ExecutionException, InterruptedException, TimeoutException {
        // Set up Firestore to return the mocked collection reference
        lenient().when(firestore.collection("Tournaments")).thenReturn(collectionRefMock);

        // Mock document retrieval by any tournament ID
        lenient().when(collectionRefMock.document(anyString())).thenReturn(documentRefMock);
        lenient().when(collectionRefMock.document()).thenReturn(documentRefMock); // For document() with no args

        // Mock the document ID to return a specific ID
        lenient().when(documentRefMock.getId()).thenReturn("testTournamentId");

        // Mock the set operation to return a successful WriteResult
        WriteResult mockWriteResult = mock(WriteResult.class);
        lenient().when(writeResultFutureMock.get()).thenReturn(mockWriteResult);
        lenient().when(writeResultFutureMock.get(anyLong(), any(TimeUnit.class))).thenReturn(mockWriteResult);
        lenient().when(documentRefMock.set(any(Tournament.class))).thenReturn(writeResultFutureMock);

        // Mock the TournamentSchedulerService to do nothing when called
        lenient().doNothing().when(tournamentSchedulerService).scheduleTournamentRoundGeneration(anyString(), any(Instant.class), any());
    }

    /**
     * Helper method to create a Tournament object for testing.
     */
    private Tournament createSampleTournament() {
        Tournament tournament = new Tournament();
        // Initialize tournament fields as needed
        return tournament;
    }

    /**
     * Test the creation of a tournament.
     */
    @Test
    public void testCreateTournament() throws ExecutionException, InterruptedException {
        // Prepare a TournamentDTO object with sample data
        TournamentDTO tournamentDTO = new TournamentDTO();
        tournamentDTO.setAgeLimit(18);
        tournamentDTO.setName("Championship");
        tournamentDTO.setDescription("Annual Championship Tournament");
        tournamentDTO.setEloRequirement(1200);
        tournamentDTO.setLocation("New York");
        tournamentDTO.setCapacity(100);
        tournamentDTO.setPrize(5000);
        tournamentDTO.setStartDatetime(Instant.now());
        tournamentDTO.setEndDatetime(Instant.now().plusSeconds(86400)); // 1 day later

        // Execute the createTournament method
        String createdTournamentId = tournamentService.createTournament(tournamentDTO);

        // Assertions to verify that the outcome is as expected
        assertNotNull(createdTournamentId, "The tournament ID should not be null after creation.");
        assertEquals("testTournamentId", createdTournamentId, "The tournament ID should match the mocked ID.");
    }

    /**
     * Test the updating of a tournament.
     */
    @Test
    public void testUpdateTournament() throws ExecutionException, InterruptedException {
        // Mock the update result
        WriteResult writeResultMock = mock(WriteResult.class);
        Instant updateTime = Instant.now();
        when(writeResultMock.getUpdateTime()).thenReturn(Timestamp.ofTimeSecondsAndNanos(
                updateTime.getEpochSecond(), updateTime.getNano()));

        // Configure the Firestore mock to return writeResultFutureMock when updating the document
        when(writeResultFutureMock.get()).thenReturn(writeResultMock);
        when(firestore.collection("Tournaments").document("testTournamentId").set(any(Tournament.class)))
                .thenReturn(writeResultFutureMock);

        // Prepare a TournamentDTO with updated data
        TournamentDTO updatedTournamentDTO = new TournamentDTO();
        updatedTournamentDTO.setAgeLimit(21);
        updatedTournamentDTO.setName("Updated Championship");
        updatedTournamentDTO.setDescription("Updated description");
        updatedTournamentDTO.setEloRequirement(1300);
        updatedTournamentDTO.setLocation("Los Angeles");
        updatedTournamentDTO.setCapacity(120);
        updatedTournamentDTO.setPrize(6000);
        updatedTournamentDTO.setStartDatetime(Instant.now().plusSeconds(3600)); // 1 hour later
        updatedTournamentDTO.setEndDatetime(Instant.now().plusSeconds(90000)); // 1 day + 1 hour later

        // Execute the updateTournament method
        String updateResult = tournamentService.updateTournament("testTournamentId", updatedTournamentDTO);

        // Assertions to verify that the outcome is as expected
        assertNotNull(updateResult, "The update result should not be null.");
        assertTrue(updateResult.contains(updateTime.toString()), "The update result should contain the update time.");
    }

    // /**
    //  * Test the deletion of a tournament.
    //  */
    // @Test
    // public void testDeleteTournament() throws ExecutionException, InterruptedException {
    //     // Configure the Firestore mock to handle the delete operation
    //     ApiFuture<WriteResult> deleteFutureMock = mock(ApiFuture.class);
    //     when(firestore.collection("Tournaments").document("testTournamentId").delete())
    //             .thenReturn(deleteFutureMock);

    //     // Execute the deleteTournament method
    //     assertDoesNotThrow(() -> tournamentService.deleteTournament("testTournamentId"),
    //             "Deleting the tournament should not throw any exceptions.");

    //     // Verify that the delete operation was called exactly once
    //     verify(firestore.collection("Tournaments").document("testTournamentId"), times(1)).delete();
    // }

    /**
     * Test retrieving a tournament by ID when it exists.
     */
    @Test
    void testGetTournamentById_TournamentExists() throws ExecutionException, InterruptedException {
        // Arrange
        String tournamentID = "existingID";
        Tournament expectedTournament = createSampleTournament();
        ApiFuture<DocumentSnapshot> documentSnapshotFutureMock = mock(ApiFuture.class);
        when(documentRefMock.get()).thenReturn(documentSnapshotFutureMock);
        when(documentSnapshotFutureMock.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.toObject(Tournament.class)).thenReturn(expectedTournament);

        // Act
        Tournament result = tournamentService.getTournamentById(tournamentID);

        // Assert
        assertNotNull(result, "The retrieved tournament should not be null.");
        assertEquals(expectedTournament, result, "The retrieved tournament should match the expected tournament.");
        verify(documentRefMock, times(1)).get();
    }

    /**
     * Test retrieving a tournament by ID when it does not exist.
     */
    @Test
    void testGetTournamentById_TournamentNotFound() throws ExecutionException, InterruptedException {
        // Arrange
        String tournamentID = "nonExistingID";
        ApiFuture<DocumentSnapshot> documentSnapshotFutureMock = mock(ApiFuture.class);
        when(documentRefMock.get()).thenReturn(documentSnapshotFutureMock);
        when(documentSnapshotFutureMock.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tournamentService.getTournamentById(tournamentID);
        });
        assertEquals("Tournament not found with ID: " + tournamentID, exception.getMessage(),
                "Exception message should match the expected message.");
        verify(documentRefMock, times(1)).get();
    }

    // /**
    //  * Test retrieving all tournaments when tournaments exist.
    //  */
    // @Test
    // void testGetAllTournaments_Success() throws ExecutionException, InterruptedException {
    //     // Arrange
    //     Tournament tournament1 = createSampleTournament();
    //     Tournament tournament2 = createSampleTournament();
    //     QueryDocumentSnapshot docSnapshot1 = mock(QueryDocumentSnapshot.class);
    //     QueryDocumentSnapshot docSnapshot2 = mock(QueryDocumentSnapshot.class);

    //     // Mock ApiFuture and DocumentSnapshots
    //     when(collectionRefMock.get()).thenReturn(querySnapshotFuture);
    //     when(querySnapshotFuture.get()).thenReturn(querySnapshot);
    //     when(querySnapshot.getDocuments()).thenReturn(List.of(docSnapshot1, docSnapshot2));
    //     when(docSnapshot1.toObject(Tournament.class)).thenReturn(tournament1);
    //     when(docSnapshot2.toObject(Tournament.class)).thenReturn(tournament2);

    //     // Act
    //     List<Tournament> result = tournamentService.getAllTournaments();

    //     // Assert
    //     assertNotNull(result, "The result list should not be null.");
    //     assertEquals(2, result.size(), "There should be 2 tournaments in the list.");
    //     assertEquals(tournament1, result.get(0), "The first tournament should match tournament1.");
    //     assertEquals(tournament2, result.get(1), "The second tournament should match tournament2.");
    //     verify(collectionRefMock, times(1)).get();
    // }

    // /**
    //  * Test retrieving all tournaments when no tournaments exist.
    //  */
    // @Test
    // void testGetAllTournaments_NoTournaments() throws ExecutionException, InterruptedException {
    //     // Arrange
    //     when(collectionRefMock.get()).thenReturn(querySnapshotFuture);
    //     when(querySnapshotFuture.get()).thenReturn(querySnapshot);
    //     when(querySnapshot.getDocuments()).thenReturn(List.of()); // Empty list for no tournaments

    //     // Act
    //     List<Tournament> result = tournamentService.getAllTournaments();

    //     // Assert
    //     assertNotNull(result, "The result list should not be null.");
    //     assertTrue(result.isEmpty(), "The result list should be empty when no tournaments exist.");
    //     verify(collectionRefMock, times(1)).get();
    // }
}
