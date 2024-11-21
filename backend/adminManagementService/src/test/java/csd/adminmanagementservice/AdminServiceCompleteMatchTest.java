package csd.adminmanagementservice;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;

import csd.adminmanagement.Model.MatchResultUpdateRequest;
import csd.adminmanagement.Service.AdminService;

@ExtendWith(MockitoExtension.class)
public class AdminServiceCompleteMatchTest {
    
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

    @Mock 
    private CollectionReference tournamentsRef;

    @Mock 
    private DocumentReference tournamentDocRef;

    @Mock 
    private DocumentSnapshot tournamentSnapshot;

    @Mock 
    private DocumentReference roundDocRef;

    @Mock 
    private DocumentSnapshot roundSnapshot;

    @Mock 
    private ApiFuture<DocumentSnapshot> tournamentFuture;

    @Mock 
    private ApiFuture<DocumentSnapshot> roundFuture;

    @Mock
    private CollectionReference roundsRef;

    @Mock 
    private ApiFuture<WriteResult> writeResultFuture;

    @Mock 
    private RestTemplate restTemplate;

    @Test
    void completeMatch_ValidInput() throws ExecutionException, InterruptedException {
        // Arrange
        String tournamentId = "tournament123";
        String roundNum = "round1";
        int matchId = 0;
        MatchResultUpdateRequest request = new MatchResultUpdateRequest();
        request.setAS1(2.0);
        request.setAS2(1.0);
    
        // Mock Firestore interactions
        when(firestore.collection("Tournaments")).thenReturn(tournamentsRef); // Mock Tournaments collection
        when(tournamentsRef.document(tournamentId)).thenReturn(tournamentDocRef); // Mock tournament document
        when(tournamentDocRef.get()).thenReturn(tournamentFuture); // Mock future for tournament document
        when(tournamentFuture.get()).thenReturn(tournamentSnapshot); // Mock tournament snapshot
        when(tournamentSnapshot.exists()).thenReturn(true); // Simulate tournament exists
    
        // Mock the Rounds sub-collection
        when(tournamentDocRef.collection("Rounds")).thenReturn(roundsRef); // Mock Rounds collection
        when(roundsRef.document(roundNum)).thenReturn(roundDocRef); // Mock specific round document
    
        // Mock the round document snapshot
        when(roundDocRef.get()).thenReturn(roundFuture); // Mock future for round document
        when(roundFuture.get()).thenReturn(roundSnapshot); // Mock round snapshot
        when(roundSnapshot.exists()).thenReturn(true); // Simulate round exists
    
        // Mock matches in the round
        when(roundSnapshot.get("matches")).thenReturn(Collections.singletonList(Map.of(
            "participants", List.of(
                Map.of("authId", "user1"),
                Map.of("authId", "user2")
            )
        )));
    
        // Act & Assert
        assertDoesNotThrow(() -> adminService.completeMatch(tournamentId, roundNum, matchId, request));
    }    


    @Test
    void completeMatch_InvalidInput() throws ExecutionException, InterruptedException {
        // Arrange
        String tournamentId = "invalid123";
        String roundNum = "round1";
        int matchId = 0;
        MatchResultUpdateRequest request = new MatchResultUpdateRequest();

        when(firestore.collection("Tournaments").document(tournamentId)).thenThrow(new RuntimeException("Tournament not found"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> adminService.completeMatch(tournamentId, roundNum, matchId, request));
    }

    @Test
    void completeMatch_Draw() throws ExecutionException, InterruptedException {
        // Arrange
        String tournamentId = "tournament123";
        String roundNum = "round1";
        int matchId = 0;
        MatchResultUpdateRequest request = new MatchResultUpdateRequest();
        request.setAS1(2.0);
        request.setAS2(2.0);

        DocumentReference tournamentDocRef = Mockito.mock(DocumentReference.class);
        DocumentReference roundDocRef = Mockito.mock(DocumentReference.class);

        when(firestore.collection("Tournaments").document(tournamentId)).thenReturn(tournamentDocRef);
        when(tournamentDocRef.get()).thenReturn(adminDocSnapFuture);
        when(adminDocSnapFuture.get()).thenReturn(adminDocSnap);
        when(adminDocSnap.exists()).thenReturn(true);

        when(tournamentDocRef.collection("Rounds").document(roundNum)).thenReturn(roundDocRef);
        when(roundDocRef.get()).thenReturn(adminDocSnapFuture);
        when(adminDocSnapFuture.get()).thenReturn(adminDocSnap);
        when(adminDocSnap.exists()).thenReturn(true);

        when(adminDocSnap.get("matches")).thenReturn(Collections.singletonList(Map.of(
            "participants", List.of(Map.of("authId", "user1"), Map.of("authId", "user2"))
        )));

        // Act & Assert
        assertDoesNotThrow(() -> adminService.completeMatch(tournamentId, roundNum, matchId, request));
    }
}

