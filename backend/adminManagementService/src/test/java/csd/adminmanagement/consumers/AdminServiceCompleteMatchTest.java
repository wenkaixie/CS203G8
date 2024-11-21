package csd.adminmanagement.consumers;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;

import csd.adminmanagement.model.MatchResultUpdateRequest;
import csd.adminmanagement.service.AdminService;

class AdminServiceCompleteMatchTest {

    @Mock
    private Firestore firestore;

    @Mock
    private CollectionReference tournamentsRef;

    @Mock
    private DocumentReference tournamentDocRef;

    @Mock
    private DocumentSnapshot tournamentSnapshot;

    @Mock
    private CollectionReference roundsRef;

    @Mock
    private DocumentReference roundDocRef;

    @Mock
    private DocumentSnapshot roundSnapshot;

    @Mock
    private ApiFuture<DocumentSnapshot> tournamentFuture;

    @Mock
    private ApiFuture<DocumentSnapshot> roundFuture;

    @Mock
    private ApiFuture<WriteResult> writeResultFuture;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCompleteMatch_ValidInput() throws ExecutionException, InterruptedException {
        // Arrange
        String tournamentId = "tournament123";
        String roundNum = "round1";
        int matchId = 0;
        MatchResultUpdateRequest request = new MatchResultUpdateRequest();
        request.setAS1(2.0);
        request.setAS2(1.0);

        // Mock Firestore interactions
        when(firestore.collection("Tournaments")).thenReturn(tournamentsRef);
        when(tournamentsRef.document(tournamentId)).thenReturn(tournamentDocRef);
        when(tournamentDocRef.get()).thenReturn(tournamentFuture);
        when(tournamentFuture.get()).thenReturn(tournamentSnapshot);
        when(tournamentSnapshot.exists()).thenReturn(true);

        when(tournamentDocRef.collection("Rounds")).thenReturn(mock(CollectionReference.class));
        when(tournamentDocRef.collection("Rounds").document(roundNum)).thenReturn(roundDocRef);
        when(roundDocRef.get()).thenReturn(roundFuture);
        when(roundFuture.get()).thenReturn(roundSnapshot);
        when(roundSnapshot.exists()).thenReturn(true);

        // Mock matches and participants
        Map<String, Object> participant1 = new HashMap<>();
        participant1.put("authId", "user1");
        Map<String, Object> participant2 = new HashMap<>();
        participant2.put("authId", "user2");

        Map<String, Object> match = new HashMap<>();
        match.put("participants", List.of(participant1, participant2));
        List<Map<String, Object>> matches = List.of(match);

        when(roundSnapshot.get("matches")).thenReturn(matches);
        when(roundDocRef.update(eq("matches"), any())).thenReturn(writeResultFuture);

        // Mock REST call
        ResponseEntity<String> responseEntity = mock(ResponseEntity.class);
        when(responseEntity.getStatusCodeValue()).thenReturn(200);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(String.class)))
            .thenReturn(responseEntity);

        // Act & Assert
        assertDoesNotThrow(() -> adminService.completeMatch(tournamentId, roundNum, matchId, request));
    }

    @Test
    void testCompleteMatch_InvalidTournament() throws ExecutionException, InterruptedException {
        // Arrange
        String tournamentId = "invalid123";
        String roundNum = "round1";
        int matchId = 0;
        MatchResultUpdateRequest request = new MatchResultUpdateRequest();

        when(firestore.collection("Tournaments")).thenReturn(tournamentsRef);
        when(tournamentsRef.document(tournamentId)).thenReturn(tournamentDocRef);
        when(tournamentDocRef.get()).thenReturn(tournamentFuture);
        when(tournamentFuture.get()).thenReturn(tournamentSnapshot);
        when(tournamentSnapshot.exists()).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> adminService.completeMatch(tournamentId, roundNum, matchId, request));
        assertEquals("No tournament found for ID: invalid123", exception.getMessage());
    }

    @Test
    void testCompleteMatch_InvalidRound() throws ExecutionException, InterruptedException {
        // Arrange
        String tournamentId = "tournament123";
        String roundNum = "invalidRound";
        int matchId = 0;
        MatchResultUpdateRequest request = new MatchResultUpdateRequest();

        when(firestore.collection("Tournaments")).thenReturn(tournamentsRef);
        when(tournamentsRef.document(tournamentId)).thenReturn(tournamentDocRef);
        when(tournamentDocRef.get()).thenReturn(tournamentFuture);
        when(tournamentFuture.get()).thenReturn(tournamentSnapshot);
        when(tournamentSnapshot.exists()).thenReturn(true);

        when(tournamentDocRef.collection("Rounds")).thenReturn(roundsRef);
        when(roundsRef.document(roundNum)).thenReturn(roundDocRef);
        when(roundDocRef.get()).thenReturn(roundFuture);
        when(roundFuture.get()).thenReturn(roundSnapshot);
        when(roundSnapshot.exists()).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> adminService.completeMatch(tournamentId, roundNum, matchId, request));
        assertEquals("No round found with ID: invalidRound", exception.getMessage());
    }

    @Test
    void testCompleteMatch_NoMatchFound() throws ExecutionException, InterruptedException {
        // Arrange
        String tournamentId = "tournament123";
        String roundNum = "round1";
        int matchId = 1; // Invalid match ID
        MatchResultUpdateRequest request = new MatchResultUpdateRequest();

        when(firestore.collection("Tournaments")).thenReturn(tournamentsRef);
        when(tournamentsRef.document(tournamentId)).thenReturn(tournamentDocRef);
        when(tournamentDocRef.get()).thenReturn(tournamentFuture);
        when(tournamentFuture.get()).thenReturn(tournamentSnapshot);
        when(tournamentSnapshot.exists()).thenReturn(true);

        when(tournamentDocRef.collection("Rounds")).thenReturn(roundsRef);
        when(roundsRef.document(roundNum)).thenReturn(roundDocRef);
        when(roundDocRef.get()).thenReturn(roundFuture);
        when(roundFuture.get()).thenReturn(roundSnapshot);
        when(roundSnapshot.exists()).thenReturn(true);

        when(roundSnapshot.get("matches")).thenReturn(Collections.singletonList(Map.of(
            "participants", List.of(Map.of("authId", "user1"))
        )));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> adminService.completeMatch(tournamentId, roundNum, matchId, request));
        assertEquals("No match found with ID: 1", exception.getMessage());
    }
}
