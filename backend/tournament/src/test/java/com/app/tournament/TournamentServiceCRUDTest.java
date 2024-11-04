package com.app.tournament;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.app.tournament.DTO.TournamentDTO;
import com.app.tournament.model.Tournament;
import com.app.tournament.service.TournamentSchedulerService;
import com.app.tournament.service.TournamentService;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;

@SpringBootTest
public class TournamentServiceCRUDTest {

    @Mock
    private Firestore firestore;

    @Mock
    private CollectionReference collectionRefMock;

    @Mock
    private DocumentReference documentRefMock;

    @MockBean
    private TournamentSchedulerService tournamentSchedulerService;

    @InjectMocks
    private TournamentService tournamentService;

    @Mock
    private DocumentSnapshot documentSnapshot;

    @Mock
    private ApiFuture<WriteResult> writeResultFutureMock;

    @Mock
    private QuerySnapshot querySnapshot;

    @Mock
    private ApiFuture<QuerySnapshot> apiFuture;

    @BeforeEach
    public void setUp() throws ExecutionException, InterruptedException, TimeoutException {
        MockitoAnnotations.openMocks(this);

        // Mock WriteResult and ApiFuture for get() behavior
        WriteResult writeResultMock = mock(WriteResult.class);
        when(writeResultFutureMock.get()).thenReturn(writeResultMock);
        when(writeResultFutureMock.get(anyLong(), any(TimeUnit.class))).thenReturn(writeResultMock);

        // Set up Firestore to return mocked collection reference
        when(firestore.collection("Tournaments")).thenReturn(collectionRefMock);

        // Ensure documentRefMock will return a specific ID
        when(collectionRefMock.document(anyString())).thenReturn(documentRefMock);
        when(collectionRefMock.document()).thenReturn(documentRefMock); // For document() with no args
        when(documentRefMock.getId()).thenReturn("testTournamentId");

        // Mock documentRefMock.set(...) to return the non-null writeResultFutureMock
        when(documentRefMock.set(any(Tournament.class))).thenReturn(writeResultFutureMock);

        // Mock scheduler service with doNothing() for specific parameters
        doNothing().when(tournamentSchedulerService).scheduleTournamentRoundGeneration(anyString(), any(Instant.class),
                any());
    }

    @Test
    public void testCreateTournament() throws ExecutionException, InterruptedException {
        // Prepare a TournamentDTO object with sample data for testing
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

    @Test
    public void testUpdateTournament() throws ExecutionException, InterruptedException {
        // Mock the update result
        WriteResult writeResultMock = mock(WriteResult.class);
        Instant updateTime = Instant.now();
        when(writeResultMock.getUpdateTime()).thenReturn(Timestamp.ofTimeSecondsAndNanos(updateTime.getEpochSecond(), updateTime.getNano()));
    
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

    @Test
    public void testDeleteTournament() throws ExecutionException, InterruptedException {
        // Configure the Firestore mock to handle the delete operation
        ApiFuture<WriteResult> deleteFutureMock = mock(ApiFuture.class);
        when(firestore.collection("Tournaments").document("testTournamentId").delete()).thenReturn(deleteFutureMock);
    
        // Execute the deleteTournament method
        assertDoesNotThrow(() -> tournamentService.deleteTournament("testTournamentId"),
                "Deleting the tournament should not throw any exceptions.");
    
        // Verify that the delete operation was called exactly once
        verify(firestore.collection("Tournaments").document("testTournamentId"), times(1)).delete();
    }

    @Test
    void testGetTournamentById_TournamentExists() throws ExecutionException, InterruptedException {
        try {
            // Arrange
            String tournamentID = "existingID";
            Tournament expectedTournament = new Tournament(); // Initialize if necessary
            ApiFuture<DocumentSnapshot> apiFuture = mock(ApiFuture.class); // Mock ApiFuture separately
            when(documentRefMock.get()).thenReturn(apiFuture);
            when(apiFuture.get()).thenReturn(documentSnapshot);
            when(documentSnapshot.exists()).thenReturn(true);
            when(documentSnapshot.toObject(Tournament.class)).thenReturn(expectedTournament);

            // Act
            Tournament result = tournamentService.getTournamentById(tournamentID);

            // Assert
            assertNotNull(result);
            assertEquals(expectedTournament, result);
            verify(documentRefMock, times(1)).get();
        } catch (ExecutionException | InterruptedException e) {
            fail("Test failed due to unexpected exception: " + e.getMessage());
        }
    }

    @Test
    void testGetTournamentById_TournamentNotFound() {
        try {
            // Arrange
            String tournamentID = "nonExistingID";
            ApiFuture<DocumentSnapshot> apiFuture = mock(ApiFuture.class); // Mock ApiFuture separately
            when(documentRefMock.get()).thenReturn(apiFuture);
            when(apiFuture.get()).thenReturn(documentSnapshot);
            when(documentSnapshot.exists()).thenReturn(false);

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                tournamentService.getTournamentById(tournamentID);
            });
            assertEquals("Tournament not found with ID: " + tournamentID, exception.getMessage());
            verify(documentRefMock, times(1)).get();
        } catch (ExecutionException | InterruptedException e) {
            fail("Test failed due to unexpected exception: " + e.getMessage());
        }
    }

    @Test
    void testGetAllTournaments_Success() {
        try {
            // Arrange
            Tournament tournament1 = new Tournament(); // Initialize with test values if necessary
            Tournament tournament2 = new Tournament();
            QueryDocumentSnapshot docSnapshot1 = mock(QueryDocumentSnapshot.class);
            QueryDocumentSnapshot docSnapshot2 = mock(QueryDocumentSnapshot.class);

            // Mock ApiFuture and DocumentSnapshots
            when(collectionRefMock.get()).thenReturn(apiFuture);
            when(apiFuture.get()).thenReturn(querySnapshot);
            when(querySnapshot.getDocuments()).thenReturn(List.of(docSnapshot1, docSnapshot2));
            when(docSnapshot1.toObject(Tournament.class)).thenReturn(tournament1);
            when(docSnapshot2.toObject(Tournament.class)).thenReturn(tournament2);

            // Act
            List<Tournament> result = tournamentService.getAllTournaments();

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(tournament1, result.get(0));
            assertEquals(tournament2, result.get(1));
            verify(collectionRefMock, times(1)).get();
        } catch (ExecutionException | InterruptedException e) {
            fail("Test failed due to unexpected exception: " + e.getMessage());
        }
    }

    @Test
    void testGetAllTournaments_NoTournaments() {
        try {
            // Arrange
            when(collectionRefMock.get()).thenReturn(apiFuture);
            when(apiFuture.get()).thenReturn(querySnapshot);
            when(querySnapshot.getDocuments()).thenReturn(List.of()); // Empty list for no tournaments

            // Act
            List<Tournament> result = tournamentService.getAllTournaments();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(collectionRefMock, times(1)).get();
        } catch (ExecutionException | InterruptedException e) {
            fail("Test failed due to unexpected exception: " + e.getMessage());
        }
    }
}


