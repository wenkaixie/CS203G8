package com.csd.tournament.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.csd.shared_library.DTO.ParticipantDTO;
import com.csd.shared_library.enumerator.MatchResult;
import com.csd.shared_library.model.Match;
import com.csd.shared_library.model.Round;
import com.csd.tournament.service.EliminationService;
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

import static org.junit.jupiter.api.Assertions.*;
import com.google.cloud.Timestamp;

import com.google.api.core.ApiFutures;


@ExtendWith(MockitoExtension.class)
public class EliminationServiceTest {

    @InjectMocks
    private EliminationService eliminationService;

    @Mock(lenient = true)
    private Firestore firestore;

    @Mock(lenient = true)
    private CollectionReference tournamentsCollection;

    @Mock(lenient = true)
    private DocumentReference tournamentDocRef;

    @Mock(lenient = true)
    private CollectionReference tournamentUsersCollection;

    @Mock(lenient = true)
    private CollectionReference globalUsersCollection;

    @Mock(lenient = true)
    private ApiFuture<QuerySnapshot> usersQueryFuture;

    @Mock(lenient = true)
    private QuerySnapshot usersQuerySnapshot;

    @Mock(lenient = true)
    private CollectionReference roundsCollection;

    @Mock
    private ApiFuture<WriteResult> mockApiFuture;

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
        // Stubbing Firestore collections and documents
        lenient().when(firestore.collection("Tournaments")).thenReturn(tournamentsCollection);
        lenient().when(tournamentsCollection.document(anyString())).thenReturn(tournamentDocRef);
        lenient().when(tournamentDocRef.collection("Rounds")).thenReturn(roundsCollection);
        lenient().when(tournamentDocRef.collection("Users")).thenReturn(tournamentUsersCollection);
        lenient().when(firestore.collection("Users")).thenReturn(globalUsersCollection);

        // Mock ApiFuture.get() for set operations
        WriteResult mockWriteResult = mock(WriteResult.class); // Simulate a valid write result
        lenient().when(mockApiFuture.get()).thenReturn(mockWriteResult);

        // Stub tournamentDocRef.get() if your service calls it
        lenient().when(tournamentDocRef.get()).thenReturn(mockDocumentSnapshotFuture);
        lenient().when(mockDocumentSnapshotFuture.get()).thenReturn(mockDocumentSnapshot);

        List<WriteResult> mockWriteResults = Arrays.asList(mock(WriteResult.class));
        lenient().when(mockBatchCommitFuture.get()).thenReturn(mockWriteResults);
    }

    /**
     * Helper method to create a mock Timestamp that returns a Date from Instant.
     */
    private Timestamp createMockTimestamp(Instant instant) {
        return Timestamp.of(java.sql.Timestamp.from(instant));
    }

    // /**
    //  * Test generateRoundsForTournament when the number of users is a power of two.
    //  */
    // @Test
    // public void testGenerateRoundsForTournament_PowerOfTwoUsers() throws Exception {
    //     String tournamentID = "tournament123";
    
    //     when(tournamentsCollection.document(tournamentID)).thenReturn(tournamentDocRef);
    //     when(tournamentDocRef.collection("Users")).thenReturn(tournamentUsersCollection);
    //     when(tournamentUsersCollection.get()).thenReturn(usersQueryFuture);
    //     when(usersQueryFuture.get()).thenReturn(usersQuerySnapshot);
    
    //     // Create mock user documents with complete data
    //     List<QueryDocumentSnapshot> userDocs = new ArrayList<>();
    //     for (int i = 1; i <= 8; i++) {
    //         QueryDocumentSnapshot userDoc = mock(QueryDocumentSnapshot.class);
    //         when(userDoc.getId()).thenReturn("user" + i);
    //         when(userDoc.getString("name")).thenReturn("User " + i);
    //         when(userDoc.getLong("elo")).thenReturn(1500L + i); // different elo for each user
    //         when(userDoc.getString("nationality")).thenReturn("Country" + i);
    //         Instant joinedAt = Instant.now();
    //         when(userDoc.getTimestamp("joinedAt")).thenReturn(Timestamp.of(Date.from(joinedAt)));
    //         userDocs.add(userDoc);
    //     }
    //     when(usersQuerySnapshot.getDocuments()).thenReturn(userDocs);
    
    //     Map<String, DocumentReference> roundDocRefs = new HashMap<>();
    //     List<Round> capturedRounds = new ArrayList<>();
    
    //     when(roundsCollection.document(anyString())).thenAnswer(invocation -> {
    //         String docId = invocation.getArgument(0, String.class);
    //         DocumentReference mockRoundDocRef = mock(DocumentReference.class);
    
    //         doAnswer(setInvocation -> {
    //             Round round = setInvocation.getArgument(0, Round.class);
    //             capturedRounds.add(round);
    //             return mockApiFuture;
    //         }).when(mockRoundDocRef).set(any(Round.class));
    
    //         // Stub get() for updateNextMatchId
    //         ApiFuture<DocumentSnapshot> mockRoundDocSnapshotFuture = mock(ApiFuture.class);
    //         DocumentSnapshot mockRoundDocSnapshot = mock(DocumentSnapshot.class);
    //         when(mockRoundDocRef.get()).thenReturn(mockRoundDocSnapshotFuture);
    //         when(mockRoundDocSnapshotFuture.get()).thenReturn(mockRoundDocSnapshot);
    //         when(mockRoundDocSnapshot.toObject(Round.class)).thenAnswer(toObjectInvocation -> {
    //             return capturedRounds.stream()
    //                     .filter(r -> String.valueOf(r.getRid()).equals(docId))
    //                     .findFirst()
    //                     .orElse(null);
    //         });
    
    //         roundDocRefs.put(docId, mockRoundDocRef);
    //         return mockRoundDocRef;
    //     });
    
    //     // Now invoke the method
    //     eliminationService.generateRoundsForTournament(tournamentID);

    //     // Verify that rounds were created
    //     // For 8 players, number of rounds is 3
    //     assertEquals(3, capturedRounds.size());

    //     // Check the rounds
    //     for (int i = 0; i < capturedRounds.size(); i++) {
    //         Round round = capturedRounds.get(i);
    //         int roundNumber = i + 1;
    //         assertEquals(roundNumber, round.getRid());

    //         List<Match> matches = round.getMatches();

    //         int expectedNumMatches = (int) Math.pow(2, capturedRounds.size() - roundNumber);
    //         assertEquals(expectedNumMatches, matches.size());

    //         for (Match match : matches) {
    //             // Check match properties if necessary
    //             assertNotNull(match.getId());
    //             assertEquals(roundNumber, match.getTournamentRoundText());
    //             assertEquals("PENDING", match.getState());
    //             assertNotNull(match.getParticipants());

    //             // For the first round, participants should be present
    //             if (roundNumber == 1) {
    //                 assertEquals(2, match.getParticipants().size());
    //             } else {
    //                 // For subsequent rounds, participants may not be set yet
    //                 assertTrue(match.getParticipants().isEmpty() || match.getParticipants().size() == 2);
    //             }
    //         }
    //     }

    //     // Verify that roundsCollection.document() was called 3 times
    //     verify(roundsCollection, times(3)).document(anyString());

    //     // Verify that set() was called on each round document
    //     for (DocumentReference docRef : roundDocRefs.values()) {
    //         verify(docRef, times(1)).set(any(Round.class));
    //     }
    // }

    // /**
    //  * Test generateRoundsForTournament when the number of users is not a power of two.
    //  */
    // @Test
    // public void testGenerateRoundsForTournament_NotPowerOfTwoUsers() throws Exception {
    //     String tournamentID = "tournament123";
    
    //     when(tournamentsCollection.document(tournamentID)).thenReturn(tournamentDocRef);
    //     when(tournamentDocRef.collection("Users")).thenReturn(tournamentUsersCollection);
    //     when(tournamentUsersCollection.get()).thenReturn(usersQueryFuture);
    //     when(usersQueryFuture.get()).thenReturn(usersQuerySnapshot);
    
    //     // Create mock user documents with complete data
    //     List<QueryDocumentSnapshot> userDocs = new ArrayList<>();
    //     for (int i = 1; i <= 10; i++) {
    //         QueryDocumentSnapshot userDoc = mock(QueryDocumentSnapshot.class);
    //         when(userDoc.getId()).thenReturn("user" + i);
    //         when(userDoc.getString("name")).thenReturn("User " + i);
    //         when(userDoc.getLong("elo")).thenReturn(1500L + i); // different elo for each user
    //         when(userDoc.getString("nationality")).thenReturn("Country" + i);
    //         Instant joinedAt = Instant.now();
    //         when(userDoc.getTimestamp("joinedAt")).thenReturn(Timestamp.of(Date.from(joinedAt)));
    //         userDocs.add(userDoc);
    //     }
    //     when(usersQuerySnapshot.getDocuments()).thenReturn(userDocs);
    
    //     Map<String, DocumentReference> roundDocRefs = new HashMap<>();
    //     List<Round> capturedRounds = new ArrayList<>();
    
    //     when(roundsCollection.document(anyString())).thenAnswer(invocation -> {
    //         String docId = invocation.getArgument(0, String.class);
    //         DocumentReference mockRoundDocRef = mock(DocumentReference.class);
    
    //         doAnswer(setInvocation -> {
    //             Round round = setInvocation.getArgument(0, Round.class);
    //             capturedRounds.add(round);
    //             return mockApiFuture;
    //         }).when(mockRoundDocRef).set(any(Round.class));
    
    //         // Stub get() for updateNextMatchId
    //         ApiFuture<DocumentSnapshot> mockRoundDocSnapshotFuture = mock(ApiFuture.class);
    //         DocumentSnapshot mockRoundDocSnapshot = mock(DocumentSnapshot.class);
    //         when(mockRoundDocRef.get()).thenReturn(mockRoundDocSnapshotFuture);
    //         when(mockRoundDocSnapshotFuture.get()).thenReturn(mockRoundDocSnapshot);
    //         when(mockRoundDocSnapshot.toObject(Round.class)).thenAnswer(toObjectInvocation -> {
    //             return capturedRounds.stream()
    //                     .filter(r -> String.valueOf(r.getRid()).equals(docId))
    //                     .findFirst()
    //                     .orElse(null);
    //         });
    
    //         roundDocRefs.put(docId, mockRoundDocRef);
    //         return mockRoundDocRef;
    //     });
    
    //     Map<String, DocumentReference> tournamentUserDocRefs = new HashMap<>();
    //     Map<String, DocumentReference> globalUserDocRefs = new HashMap<>();
    
    //     when(tournamentUsersCollection.document(anyString())).thenAnswer(invocation -> {
    //         String userId = invocation.getArgument(0, String.class);
    //         DocumentReference userDocRef = mock(DocumentReference.class);
    //         when(userDocRef.delete()).thenReturn(mockApiFuture);
    //         tournamentUserDocRefs.put(userId, userDocRef);
    //         return userDocRef;
    //     });
    
    //     when(globalUsersCollection.document(anyString())).thenAnswer(invocation -> {
    //         String userId = invocation.getArgument(0, String.class);
    //         DocumentReference userDocRef = mock(DocumentReference.class);
    //         when(userDocRef.update(anyString(), any())).thenReturn(mockApiFuture);
    //         globalUserDocRefs.put(userId, userDocRef);
    //         return userDocRef;
    //     });

    //     // Now invoke the method
    //     eliminationService.generateRoundsForTournament(tournamentID);

    //     // Verify that rounds were created for 8 players
    //     // Number of rounds should be 3 (log2(8) = 3)
    //     assertEquals(3, capturedRounds.size());

    //     // Verify that excess players were removed
    //     // Excess players are users 9 and 10

    //     // Verify that delete() was called on tournament user documents for user9 and user10
    //     verify(tournamentUserDocRefs.get("user9"), times(1)).delete();
    //     verify(tournamentUserDocRefs.get("user10"), times(1)).delete();

    //     // Verify that update() was called on global user documents for user9 and user10
    //     verify(globalUserDocRefs.get("user9"), times(1)).update(eq("tournamentHistory"), any());
    //     verify(globalUserDocRefs.get("user10"), times(1)).update(eq("tournamentHistory"), any());

    //     // Verify that set() was called on each round document
    //     for (DocumentReference docRef : roundDocRefs.values()) {
    //         verify(docRef, times(1)).set(any(Round.class));
    //     }
    // }

    /**
     * Test updateNextMatchId when the round is not found.
     */
    @Test
    public void testUpdateNextMatchId_RoundNotFound() throws Exception {
        String tournamentID = "tournament123";
        int roundNumber = 1;
        int matchId = 1;
        int nextMatchId = 2;

        // Mock Firestore references
        when(firestore.collection("Tournaments")).thenReturn(tournamentsCollection);
        when(tournamentsCollection.document(tournamentID)).thenReturn(tournamentDocRef);
        when(tournamentDocRef.collection("Rounds")).thenReturn(roundsCollection);
        when(roundsCollection.document(String.valueOf(roundNumber))).thenReturn(mockRoundDocRef);

        // Simulate round not found
        when(mockRoundDocRef.get()).thenReturn(mockDocumentSnapshotFuture);
        when(mockDocumentSnapshotFuture.get()).thenReturn(mockDocumentSnapshot);
        when(mockDocumentSnapshot.toObject(Round.class)).thenReturn(null);

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

        // Mock Firestore references
        when(firestore.collection("Tournaments")).thenReturn(tournamentsCollection);
        when(tournamentsCollection.document(tournamentID)).thenReturn(tournamentDocRef);
        when(tournamentDocRef.collection("Rounds")).thenReturn(roundsCollection);
        when(roundsCollection.document(String.valueOf(roundNumber))).thenReturn(mockRoundDocRef);

        // Simulate round with no matches
        Round round = new Round(roundNumber, new ArrayList<>());
        when(mockRoundDocRef.get()).thenReturn(mockDocumentSnapshotFuture);
        when(mockDocumentSnapshotFuture.get()).thenReturn(mockDocumentSnapshot);
        when(mockDocumentSnapshot.toObject(Round.class)).thenReturn(round);

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
        when(firestore.getAll(currentRoundDocRef, nextRoundDocRef)).thenReturn(mock(ApiFuture.class));

        // Simulate exception due to no Elo data
        Exception exception = assertThrows(Exception.class, () -> {
            eliminationService.populateNextRoundMatches(tournamentID, currentRoundNumber);
        });

        assertNotNull(exception.getMessage());
    }

    // /**
    //  * Test populateNextRoundMatches with tie-breaker logic.
    //  */
    // @Test
    // public void testPopulateNextRoundMatches_TieBreaker() throws Exception {
    //     String tournamentID = "tournament123";
    //     int currentRoundNumber = 1;
    
    //     // Mock usersCollection with equal Elo data
    //     when(tournamentDocRef.collection("Users")).thenReturn(tournamentUsersCollection);
    //     when(tournamentUsersCollection.select("elo")).thenReturn(tournamentUsersCollection);
    //     when(tournamentUsersCollection.get()).thenReturn(usersQueryFuture);
    
    //     List<QueryDocumentSnapshot> userDocs = new ArrayList<>();
    //     QueryDocumentSnapshot userDoc1 = mock(QueryDocumentSnapshot.class);
    //     when(userDoc1.getId()).thenReturn("user1");
    //     when(userDoc1.getLong("elo")).thenReturn(1500L);
    //     QueryDocumentSnapshot userDoc2 = mock(QueryDocumentSnapshot.class);
    //     when(userDoc2.getId()).thenReturn("user2");
    //     when(userDoc2.getLong("elo")).thenReturn(1500L);
    //     userDocs.add(userDoc1);
    //     userDocs.add(userDoc2);
    
    //     when(usersQueryFuture.get()).thenReturn(usersQuerySnapshot);
    //     when(usersQuerySnapshot.getDocuments()).thenReturn(userDocs);
    
    //     // Mock rounds and matches
    //     DocumentReference currentRoundDocRef = mock(DocumentReference.class);
    //     DocumentReference nextRoundDocRef = mock(DocumentReference.class);
    //     when(roundsCollection.document(String.valueOf(currentRoundNumber))).thenReturn(currentRoundDocRef);
    //     when(roundsCollection.document(String.valueOf(currentRoundNumber + 1))).thenReturn(nextRoundDocRef);
    
    //     Match currentMatch = new Match(1, "Match 1", 2, currentRoundNumber, Instant.now(), "COMPLETED", MatchResult.DRAW, Arrays.asList(
    //             new ParticipantDTO("1", "user1", "User 1", "", 1500, "Country1", false),
    //             new ParticipantDTO("2", "user2", "User 2", "", 1500, "Country2", false)
    //     ));
    //     Round currentRound = new Round(currentRoundNumber, Arrays.asList(currentMatch));
    //     Round nextRound = new Round(currentRoundNumber + 1, new ArrayList<>());
    
    //     // Mock getting rounds
    //     ApiFuture<DocumentSnapshot> currentRoundFuture = mock(ApiFuture.class);
    //     DocumentSnapshot currentRoundSnapshot = mock(DocumentSnapshot.class);
    //     when(currentRoundDocRef.get()).thenReturn(currentRoundFuture);
    //     when(currentRoundFuture.get()).thenReturn(currentRoundSnapshot);
    //     when(currentRoundSnapshot.toObject(Round.class)).thenReturn(currentRound);
    
    //     ApiFuture<DocumentSnapshot> nextRoundFuture = mock(ApiFuture.class);
    //     DocumentSnapshot nextRoundSnapshot = mock(DocumentSnapshot.class);
    //     when(nextRoundDocRef.get()).thenReturn(nextRoundFuture);
    //     when(nextRoundFuture.get()).thenReturn(nextRoundSnapshot);
    //     when(nextRoundSnapshot.toObject(Round.class)).thenReturn(nextRound);
    
    //     // Mock batch commit
    //     WriteBatch mockBatch = mock(WriteBatch.class);
    //     when(firestore.batch()).thenReturn(mockBatch);
    //     when(mockBatch.update(any(DocumentReference.class), anyString(), any())).thenReturn(mockBatch);
    //     when(mockBatch.commit()).thenReturn(mockBatchCommitFuture);
    
    //     // Stub nextRoundDocRef.set()
    //     when(nextRoundDocRef.set(any())).thenReturn(mockApiFuture);
    
    //     // Invoke method
    //     eliminationService.populateNextRoundMatches(tournamentID, currentRoundNumber);
    
    //     // Verify that a winner was selected randomly due to tie
    //     assertFalse(nextRound.getMatches().isEmpty());
    // }    
}
