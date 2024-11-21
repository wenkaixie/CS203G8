package csd.adminmanagementservice;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.csd.shared_library.model.Tournament;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import csd.adminmanagement.Exception.TournamentNotFoundException;
import csd.adminmanagement.Service.AdminService;

@ExtendWith(MockitoExtension.class)
public class AdminServiceGetTaskViewTest {
    
    @Mock
    private Firestore firestore;

    @Mock
    private DocumentReference adminDocRef;

    @Mock
    private ApiFuture<DocumentSnapshot> adminDocSnapFuture;

    @Mock
    private DocumentSnapshot adminDocSnap;

    @Mock
    private CollectionReference adminCollection;

    @Mock
    private ApiFuture<QuerySnapshot> adminQueryFuture;

    @Mock
    private Query adminQuery;

    @Mock
    private QuerySnapshot adminQuerySnapshot;

    @Mock
    private QueryDocumentSnapshot adminSnapshot;

    @InjectMocks
    private AdminService adminService;

    @Test
    void getTaskView_OngoingTournaments() throws ExecutionException, InterruptedException {
        // Arrange
        String adminId = "admin123";
        Tournament ongoingTournament = new Tournament();
        ongoingTournament.setStatus("In Progress");
        ongoingTournament.setAdminId(adminId);

        Query query = Mockito.mock(Query.class);

        when(firestore.collection("Tournaments")).thenReturn(adminCollection);
        when(adminCollection.whereEqualTo("adminId", adminId)).thenReturn(query);
        when(query.get()).thenReturn(adminQueryFuture);
        when(adminQueryFuture.get()).thenReturn(adminQuerySnapshot);
        when(adminQuerySnapshot.getDocuments()).thenReturn(Collections.singletonList(adminSnapshot));
        when(adminSnapshot.toObject(Tournament.class)).thenReturn(ongoingTournament);

        // Act
        List<Tournament> result = adminService.getTaskView(adminId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("In Progress", result.get(0).getStatus());
    }

    @Test
    void getTaskView_NoTournaments() throws ExecutionException, InterruptedException {
        // Arrange
        String adminId = "admin123";
        Query query = Mockito.mock(Query.class);

        when(firestore.collection("Tournaments")).thenReturn(adminCollection);
        when(adminCollection.whereEqualTo("adminId", adminId)).thenReturn(query);
        when(query.get()).thenReturn(adminQueryFuture);
        when(adminQueryFuture.get()).thenReturn(adminQuerySnapshot);
        when(adminQuerySnapshot.getDocuments()).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(TournamentNotFoundException.class, () -> adminService.getTaskView(adminId));
    }

    @Test
    void getTaskView_InvalidAdminId() throws ExecutionException, InterruptedException {
        // Arrange
        String adminId = "invalid123";
        Query query = Mockito.mock(Query.class);

        when(firestore.collection("Tournaments")).thenReturn(adminCollection);
        when(adminCollection.whereEqualTo("adminId", adminId)).thenReturn(query);
        when(query.get()).thenThrow(new RuntimeException("Admin not found"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> adminService.getTaskView(adminId));
    }

}
