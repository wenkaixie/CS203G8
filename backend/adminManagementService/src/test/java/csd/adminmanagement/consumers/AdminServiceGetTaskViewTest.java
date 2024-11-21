package csd.adminmanagement.consumers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import csd.shared_library.model.Tournament;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import csd.adminmanagement.exception.TournamentNotFoundException;
import csd.adminmanagement.service.AdminService;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

class AdminServiceGetTaskViewTest {

    @Mock
    private Firestore firestore;

    @Mock
    private CollectionReference tournamentsCollection;

    @Mock
    private Query tournamentQuery;

    @Mock
    private ApiFuture<QuerySnapshot> queryFuture;

    @Mock
    private QuerySnapshot querySnapshot;

    @Mock
    private QueryDocumentSnapshot queryDocumentSnapshot;

    @InjectMocks
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        // Initialize mocks and inject into AdminService
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getTaskView_OngoingTournaments() throws ExecutionException, InterruptedException {
        // Arrange
        String adminId = "admin123";
        Tournament ongoingTournament = new Tournament();
        ongoingTournament.setStatus("In Progress");
        ongoingTournament.setAdminId(adminId);

        when(firestore.collection("Tournaments")).thenReturn(tournamentsCollection);
        when(tournamentsCollection.whereEqualTo("adminId", adminId)).thenReturn(tournamentQuery);
        when(tournamentQuery.get()).thenReturn(queryFuture);
        when(queryFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(Collections.singletonList(queryDocumentSnapshot));
        when(queryDocumentSnapshot.toObject(Tournament.class)).thenReturn(ongoingTournament);

        // Act
        List<Tournament> result = adminService.getTaskView(adminId);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.size(), "Result list size should be 1");
        assertEquals("In Progress", result.get(0).getStatus(), "Tournament status should match");
    }

    @Test
    void getTaskView_NoTournaments() throws ExecutionException, InterruptedException {
        // Arrange
        String adminId = "admin123";

        when(firestore.collection("Tournaments")).thenReturn(tournamentsCollection);
        when(tournamentsCollection.whereEqualTo("adminId", adminId)).thenReturn(tournamentQuery);
        when(tournamentQuery.get()).thenReturn(queryFuture);
        when(queryFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(Collections.emptyList());

        // Act & Assert
        TournamentNotFoundException exception = assertThrows(TournamentNotFoundException.class, () ->
            adminService.getTaskView(adminId)
        );
        assertEquals("No tournaments found for adminId: " + adminId, exception.getMessage());
    }

    @Test
    void getTaskView_InvalidAdminId() {
        // Arrange
        String adminId = "invalid123";

        when(firestore.collection("Tournaments")).thenReturn(tournamentsCollection);
        when(tournamentsCollection.whereEqualTo("adminId", adminId)).thenThrow(new RuntimeException("Error fetching tournaments"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            adminService.getTaskView(adminId)
        );
        assertEquals("Error fetching tournaments", exception.getMessage());
    }
}
