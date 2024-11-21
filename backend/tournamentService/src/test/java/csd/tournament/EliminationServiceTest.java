package csd.tournament;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import csd.shared_library.DTO.ParticipantDTO;
import csd.shared_library.model.Match;
import csd.shared_library.model.Round;
import csd.tournament.service.EliminationService;
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
import com.google.cloud.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class EliminationServiceTest {

    @InjectMocks
    private EliminationService eliminationService;

    @Mock
    private Firestore firestore;

    @Mock
    private CollectionReference tournamentsCollection;

    @Mock
    private DocumentReference tournamentDocRef;

    @Mock
    private CollectionReference tournamentUsersCollection;

    @Mock
    private CollectionReference globalUsersCollection;

    @Mock
    private ApiFuture<QuerySnapshot> usersQueryFuture;

    @Mock
    private QuerySnapshot usersQuerySnapshot;

    @Mock
    private CollectionReference roundsCollection;

    @Mock
    private ApiFuture<WriteResult> mockWriteResultFuture;

    @Mock
    private ApiFuture<DocumentSnapshot> mockDocumentSnapshotFuture;

    @Mock
    private DocumentSnapshot mockDocumentSnapshot;

    @Mock
    private DocumentReference mockRoundDocRef;

    @Mock
    private ApiFuture<List<WriteResult>> mockBatchCommitFuture;

    @BeforeEach
    public void setUp() throws Exception {
        // Common stubs for all tests
        when(firestore.collection("Tournaments")).thenReturn(tournamentsCollection);
        when(tournamentsCollection.document(anyString())).thenReturn(tournamentDocRef);
        when(tournamentDocRef.collection("Rounds")).thenReturn(roundsCollection);
        lenient().when(tournamentDocRef.collection("Users")).thenReturn(tournamentUsersCollection);
        lenient().when(firestore.collection("Users")).thenReturn(globalUsersCollection);

        // Mock ApiFuture.get() for set operations
        WriteResult mockWriteResult = mock(WriteResult.class); // Simulate a valid write result
        lenient().when(mockWriteResultFuture.get()).thenReturn(mockWriteResult);
        lenient().when(mockBatchCommitFuture.get()).thenReturn(Arrays.asList(mockWriteResult));

        // Stub tournamentDocRef.get() if your service calls it
        lenient().when(tournamentDocRef.get()).thenReturn(mockDocumentSnapshotFuture);
        lenient().when(mockDocumentSnapshotFuture.get()).thenReturn(mockDocumentSnapshot);
    }

    /**
     * Helper method to mock a DocumentSnapshot containing a specific Round.
     */
    private void mockRoundDocument(DocumentReference roundDocRef, Round round) throws Exception {
        DocumentSnapshot mockSnapshot = mock(DocumentSnapshot.class);
        when(mockSnapshot.toObject(Round.class)).thenReturn(round);
        when(roundDocRef.get()).thenReturn(mockDocumentSnapshotFuture);
        when(mockDocumentSnapshotFuture.get()).thenReturn(mockSnapshot);
    }

    /**
     * Helper method to create a Round with a specified list of matches.
     */
    private Round createRound(int roundNumber, List<Match> matches) {
        return new Round(roundNumber, matches);
    }

    /**
     * Helper method to create a Match.
     */
    private Match createMatch(int id, int nextMatchId, int roundNumber) {
        return new Match(
                id,
                "Match " + id,
                nextMatchId,
                roundNumber,
                Instant.now(),
                "PENDING",
                null,
                new ArrayList<>()
        );
    }

    /**
     * Test updateNextMatchId when the round exists and the match is found.
     */
    @Test
    public void testUpdateNextMatchId_Success() throws Exception {
        String tournamentID = "tournament123";
        int roundNumber = 1;
        int matchId = 1;
        int nextMatchId = 2;

        // Prepare the round with the target match
        Match existingMatch = createMatch(matchId, 0, roundNumber);
        List<Match> matches = new ArrayList<>();
        matches.add(existingMatch);
        Round existingRound = createRound(roundNumber, matches);

        // Mock the round document
        when(roundsCollection.document(String.valueOf(roundNumber))).thenReturn(mockRoundDocRef);
        mockRoundDocument(mockRoundDocRef, existingRound);

        // Mock setting the updated round
        when(mockRoundDocRef.set(any(Round.class))).thenReturn(mockWriteResultFuture);

        // Invoke the method
        eliminationService.updateNextMatchId(tournamentID, roundNumber, matchId, nextMatchId);

        // Verify that the nextMatchId was updated
        assertEquals(nextMatchId, existingMatch.getNextMatchId(), "nextMatchId should be updated to the new value.");

        // Verify that set() was called with the updated round
        verify(mockRoundDocRef, times(1)).set(existingRound);
    }

    /**
     * Test updateNextMatchId when the round is not found.
     */
    @Test
    public void testUpdateNextMatchId_RoundNotFound() throws Exception {
        String tournamentID = "tournament123";
        int roundNumber = 1;
        int matchId = 1;
        int nextMatchId = 2;

        // Simulate round not found
        when(roundsCollection.document(String.valueOf(roundNumber))).thenReturn(mockRoundDocRef);
        when(mockRoundDocRef.get()).thenReturn(mockDocumentSnapshotFuture);
        when(mockDocumentSnapshotFuture.get()).thenReturn(mockDocumentSnapshot);
        when(mockDocumentSnapshot.toObject(Round.class)).thenReturn(null);

        // Invoke and expect exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            eliminationService.updateNextMatchId(tournamentID, roundNumber, matchId, nextMatchId);
        });

        assertEquals("Round " + roundNumber + " not found for tournament " + tournamentID, exception.getMessage());
    }

    /**
     * Test updateNextMatchId when the match is not found in the round.
     */
    @Test
    public void testUpdateNextMatchId_MatchNotFound() throws Exception {
        String tournamentID = "tournament123";
        int roundNumber = 1;
        int matchId = 1;
        int nextMatchId = 2;

        // Prepare the round without the target match
        List<Match> matches = new ArrayList<>();
        matches.add(createMatch(2, 0, roundNumber)); // Different match ID
        Round existingRound = createRound(roundNumber, matches);

        // Mock the round document
        when(roundsCollection.document(String.valueOf(roundNumber))).thenReturn(mockRoundDocRef);
        mockRoundDocument(mockRoundDocRef, existingRound);

        // Invoke and expect exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            eliminationService.updateNextMatchId(tournamentID, roundNumber, matchId, nextMatchId);
        });

        assertEquals("Match " + matchId + " not found in round " + roundNumber, exception.getMessage());
    }

    /**
     * Test populateNextRoundMatches when there is no Elo data for users.
     */
    @Test
    public void testPopulateNextRoundMatches_NoEloData() throws Exception {
        String tournamentID = "tournament123";
        int currentRoundNumber = 1;

        // Mock usersCollection to return empty elo data
        when(tournamentDocRef.collection("Users")).thenReturn(tournamentUsersCollection);
        when(tournamentUsersCollection.select("elo")).thenReturn(tournamentUsersCollection);
        when(tournamentUsersCollection.get()).thenReturn(usersQueryFuture);
        when(usersQueryFuture.get()).thenReturn(usersQuerySnapshot);
        when(usersQuerySnapshot.getDocuments()).thenReturn(new ArrayList<>());

        // Mock rounds
        DocumentReference currentRoundDocRef = mock(DocumentReference.class);
        DocumentReference nextRoundDocRef = mock(DocumentReference.class);
        when(roundsCollection.document(String.valueOf(currentRoundNumber))).thenReturn(currentRoundDocRef);
        when(roundsCollection.document(String.valueOf(currentRoundNumber + 1))).thenReturn(nextRoundDocRef);

        // Mock getting rounds
        ApiFuture<List<DocumentSnapshot>> mockGetAllFuture = mock(ApiFuture.class);
        List<DocumentSnapshot> roundDocs = new ArrayList<>();
        Round currentRound = createRound(currentRoundNumber, new ArrayList<>());
        Round nextRound = createRound(currentRoundNumber + 1, new ArrayList<>());
        DocumentSnapshot currentRoundSnapshot = mock(DocumentSnapshot.class);
        DocumentSnapshot nextRoundSnapshot = mock(DocumentSnapshot.class);
        when(currentRoundSnapshot.toObject(Round.class)).thenReturn(currentRound);
        when(nextRoundSnapshot.toObject(Round.class)).thenReturn(nextRound);
        roundDocs.add(currentRoundSnapshot);
        roundDocs.add(nextRoundSnapshot);
        when(mockGetAllFuture.get()).thenReturn(roundDocs);
        when(firestore.getAll(currentRoundDocRef, nextRoundDocRef)).thenReturn(mockGetAllFuture);

        // Mocking the set operation for the next round
        when(nextRoundDocRef.set(any(Round.class))).thenReturn(mockWriteResultFuture);

        // Invoke and expect exception due to no Elo data
        // Depending on implementation, it might handle it gracefully or throw an exception
        // Here, assuming it throws a RuntimeException
        Exception exception = assertThrows(RuntimeException.class, () -> {
            eliminationService.populateNextRoundMatches(tournamentID, currentRoundNumber);
        });

        assertNotNull(exception.getMessage(), "Exception message should not be null.");
    }

    /**
     * Additional simpler tests can be added here following the same pattern.
     */
}
