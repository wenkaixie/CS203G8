package csd.tournament;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import csd.shared_library.DTO.ParticipantDTO;
import csd.shared_library.model.Match;
import csd.shared_library.model.Round;
import csd.tournament.service.RoundRobinService;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;


@ExtendWith(MockitoExtension.class)
public class RoundRobinServiceTest {

    @InjectMocks
    private RoundRobinService roundRobinService;

    @Mock
    private Firestore firestore;

    @Mock
    private CollectionReference tournamentsCollectionMock;

    @Mock
    private DocumentReference tournamentDocRefMock;

    @Mock
    private CollectionReference usersCollectionMock;

    @Mock
    private ApiFuture<QuerySnapshot> querySnapshotFutureMock;

    @Mock
    private QuerySnapshot querySnapshotMock;

    @Mock
    private DocumentSnapshot documentSnapshotMock;

    @Mock
    private CollectionReference roundsCollectionMock;

    @Mock
    private DocumentReference roundDocRefMock;

    @Mock
    private ApiFuture<WriteResult> writeResultFutureMock;

    @Mock
    private WriteBatch writeBatchMock;

    @Mock
    private ApiFuture<Void> writeBatchCommitFutureMock;

    @Mock
    private ApiFuture<DocumentSnapshot> documentSnapshotFutureMock;


    @BeforeEach
    public void setUp() throws ExecutionException, InterruptedException {
        // Common stubbings for all tests

        // Mock firestore.collection("Tournaments")
        when(firestore.collection("Tournaments")).thenReturn(tournamentsCollectionMock);

        // Mock tournamentsCollection.document(tournamentID)
        when(tournamentsCollectionMock.document(anyString())).thenReturn(tournamentDocRefMock);

        // Mock tournamentDocRef.collection("Users")
        when(tournamentDocRefMock.collection("Users")).thenReturn(usersCollectionMock);

        // Mock tournamentDocRef.collection("Rounds")
        lenient().when(tournamentDocRefMock.collection("Rounds")).thenReturn(roundsCollectionMock);

        // Mock writeResultFuture.get()
        WriteResult writeResultMock = mock(WriteResult.class);
        lenient().when(writeResultFutureMock.get()).thenReturn(writeResultMock);

        // Mock collectionRefMock.document().set(...)
        lenient().when(roundsCollectionMock.document(anyString())).thenReturn(roundDocRefMock);
        lenient().when(roundDocRefMock.set(any(Round.class))).thenReturn(writeResultFutureMock);

        // Mock firestore.batch()
        lenient().when(firestore.batch()).thenReturn(writeBatchMock);

        // Mock WriteBatch methods
        lenient().when(writeBatchMock.set(any(DocumentReference.class), any())).thenReturn(writeBatchMock);
        lenient().when(writeBatchMock.update(any(DocumentReference.class), anyString(), any())).thenReturn(writeBatchMock);

        // Mock commit() to complete successfully
        lenient().when(writeBatchCommitFutureMock.get()).thenReturn(null); // Assuming commit returns null on success

        lenient().when(usersCollectionMock.select("elo")).thenReturn(usersCollectionMock);

        lenient().when(roundDocRefMock.get()).thenReturn(documentSnapshotFutureMock);
        lenient().when(documentSnapshotFutureMock.get()).thenReturn(documentSnapshotMock);
    }

    /**
     * Helper method to mock fetching users from Firestore.
     */
    private void mockFetchUsers(String tournamentID, List<ParticipantDTO> participants) throws ExecutionException, InterruptedException {
        // Mock tournamentsCollection.document(tournamentID)
        when(firestore.collection("Tournaments")).thenReturn(tournamentsCollectionMock);
        when(tournamentsCollectionMock.document(tournamentID)).thenReturn(tournamentDocRefMock);
        when(tournamentDocRefMock.collection("Users")).thenReturn(usersCollectionMock);

        // Mock usersCollection.get()
        when(usersCollectionMock.get()).thenReturn(querySnapshotFutureMock);
        when(querySnapshotFutureMock.get()).thenReturn(querySnapshotMock);

        // Mock querySnapshot.getDocuments()
        List<QueryDocumentSnapshot> userDocs = new ArrayList<>();
        for (ParticipantDTO participant : participants) {
            QueryDocumentSnapshot userDoc = mock(QueryDocumentSnapshot.class);
            when(userDoc.getId()).thenReturn(participant.getAuthId());
            when(userDoc.getString("name")).thenReturn(participant.getName());
            when(userDoc.getLong("elo")).thenReturn((long) participant.getElo());
            when(userDoc.getString("nationality")).thenReturn(participant.getNationality());
            userDocs.add(userDoc);
        }
        when(querySnapshotMock.getDocuments()).thenReturn(userDocs);
    }

    /**
     * Helper method to create a sample ParticipantDTO.
     */
    private ParticipantDTO createParticipant(String authId, String name, int elo, String nationality) {
        return new ParticipantDTO(null, authId, name, "", elo, nationality, false);
    }

    /**
     * Helper method to create a sample Round with specified matches.
     */
    private Round createRound(int rid, List<Match> matches) {
        Round round = new Round();
        round.setRid(rid);
        round.setMatches(matches);
        return round;
    }

    /**
     * Helper method to create a sample Match.
     */
    private Match createMatch(int id, String name, int nextMatchId, int roundNumber, List<ParticipantDTO> participants) {
        Match match = new Match();
        match.setId(id);
        match.setName(name);
        match.setNextMatchId(nextMatchId);
        match.setStartTime(Instant.now());
        match.setState("PENDING");
        match.setResult(null);
        match.setParticipants(participants);
        return match;
    }

    @Test
    public void testGenerateRoundsForTournament_NoUsersFound() throws ExecutionException, InterruptedException {
        // Arrange
        String tournamentID = "tournament123";
        List<ParticipantDTO> participants = Collections.emptyList();
        mockFetchUsers(tournamentID, participants);
    
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roundRobinService.generateRoundsForTournament(tournamentID);
        });
    
        assertEquals("No users found in tournament: " + tournamentID, exception.getMessage());
    
        // Verify that no rounds were created
        verify(roundsCollectionMock, never()).document(anyString());
    }

    @Test
    public void testGenerateRoundsForTournament_IncompleteUserData() throws ExecutionException, InterruptedException {
        // Arrange
        String tournamentID = "tournament123";
        List<ParticipantDTO> participants = List.of(
                createParticipant("user1", "Alice", 1500, "USA"),
                createParticipant("user2", null, 1600, "UK") // Missing name
        );
        mockFetchUsers(tournamentID, participants);
    
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roundRobinService.generateRoundsForTournament(tournamentID);
        });
    
        assertEquals("Incomplete user data for user ID: user2", exception.getMessage());
    
        // Verify that no rounds were created
        verify(roundsCollectionMock, never()).document(anyString());
    }

    @Test
    public void testUpdateNextRoundElos_NoNextRoundFound() throws ExecutionException, InterruptedException {
        // Arrange
        String tournamentID = "tournament123";
        int currentRoundNumber = 1;

        // Mock users' Elo data
        Map<String, Integer> eloMap = Map.of(
                "user1", 1500,
                "user2", 1600
        );

        // Mock fetching Elo data
        when(usersCollectionMock.select("elo")).thenReturn(usersCollectionMock);
        ApiFuture<QuerySnapshot> eloQueryFutureMock = mock(ApiFuture.class);
        when(usersCollectionMock.get()).thenReturn(eloQueryFutureMock);
        QuerySnapshot eloQuerySnapshotMock = mock(QuerySnapshot.class);
        when(eloQueryFutureMock.get()).thenReturn(eloQuerySnapshotMock);

        List<QueryDocumentSnapshot> eloUserDocs = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : eloMap.entrySet()) {
            QueryDocumentSnapshot userDoc = mock(QueryDocumentSnapshot.class);
            when(userDoc.getId()).thenReturn(entry.getKey());
            when(userDoc.getLong("elo")).thenReturn(entry.getValue().longValue());
            eloUserDocs.add(userDoc);
        }
        when(eloQuerySnapshotMock.getDocuments()).thenReturn(eloUserDocs);

        // Mock fetching next round which does not exist
        when(roundsCollectionMock.document(String.valueOf(currentRoundNumber + 1))).thenReturn(roundDocRefMock);
        DocumentSnapshot nextRoundSnapshotMock = mock(DocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> nextRoundFutureMock = mock(ApiFuture.class);
        when(roundDocRefMock.get()).thenReturn(nextRoundFutureMock);
        when(nextRoundFutureMock.get()).thenReturn(nextRoundSnapshotMock);
        when(nextRoundSnapshotMock.toObject(Round.class)).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roundRobinService.updateNextRoundElos(tournamentID, currentRoundNumber);
        });

        assertEquals("Next round not found.", exception.getMessage());

        // Verify that set was never called
        verify(roundDocRefMock, never()).set(any(Round.class));

        // Verify that user Elo updates were never called
        verify(usersCollectionMock, never()).document(anyString());
    }
}
