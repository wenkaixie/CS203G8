package elo.test;

import elo.controller.EloController;
import elo.model.EloUpdateRequest;
import elo.service.EloService;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EloController.class)
public class EloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EloService eloService;

    @MockBean
    private Firestore firestore;  // Mock Firestore

    private MockedStatic<FirestoreClient> firestoreClientMock;  // Static mock for FirestoreClient

    @BeforeEach
    public void setUp() throws Exception {
        firestoreClientMock = Mockito.mockStatic(FirestoreClient.class);
        firestoreClientMock.when(FirestoreClient::getFirestore).thenReturn(firestore);

        // Mock Firestore behavior to return a mocked CollectionReference
        CollectionReference mockCollectionReference = mock(CollectionReference.class);
        when(firestore.collection(anyString())).thenReturn(mockCollectionReference);

        // Mock DocumentReference for both users
        DocumentReference mockDocumentReference1 = mock(DocumentReference.class);
        DocumentReference mockDocumentReference2 = mock(DocumentReference.class);
        when(mockCollectionReference.document("user1")).thenReturn(mockDocumentReference1);
        when(mockCollectionReference.document("user2")).thenReturn(mockDocumentReference2);

        // Mock DocumentReference.get() to return a mock ApiFuture<DocumentSnapshot>
        ApiFuture<DocumentSnapshot> mockApiFuture1 = mock(ApiFuture.class);
        ApiFuture<DocumentSnapshot> mockApiFuture2 = mock(ApiFuture.class);
        when(mockDocumentReference1.get()).thenReturn(mockApiFuture1);
        when(mockDocumentReference2.get()).thenReturn(mockApiFuture2);

        // Mock valid DocumentSnapshot with Elo ratings for both users
        DocumentSnapshot mockDocumentSnapshot1 = mock(DocumentSnapshot.class);
        DocumentSnapshot mockDocumentSnapshot2 = mock(DocumentSnapshot.class);
        when(mockApiFuture1.get()).thenReturn(mockDocumentSnapshot1);
        when(mockApiFuture2.get()).thenReturn(mockDocumentSnapshot2);

        // Return valid Elo ratings from Firebase
        when(mockDocumentSnapshot1.getDouble("elo")).thenReturn(1000.0);  // Elo for user1
        when(mockDocumentSnapshot2.getDouble("elo")).thenReturn(1000.0);  // Elo for user2

        // Mock EloService to do nothing (for success case)
        doNothing().when(eloService).updateElo(anyString(), anyString(), anyDouble(), anyDouble(), anyDouble(), anyDouble());
    }

    @AfterEach
    public void tearDown() {
        firestoreClientMock.close();  // Make sure the static mock is unregistered after each test
    }

    @Test
    public void testUpdateElo() throws Exception {
        String userId1 = "user1";
        String userId2 = "user2";
        EloUpdateRequest request = new EloUpdateRequest(1, 0);

        // Mock Firestore response to simulate that both users exist
        DocumentSnapshot user1Snapshot = mock(DocumentSnapshot.class);
        DocumentSnapshot user2Snapshot = mock(DocumentSnapshot.class);
        
        when(user1Snapshot.exists()).thenReturn(true);
        when(user2Snapshot.exists()).thenReturn(true);
        
        // Mock Elo values for the users
        when(user1Snapshot.getDouble("elo")).thenReturn(1200.0);
        when(user2Snapshot.getDouble("elo")).thenReturn(1300.0);

        // Mock Firestore interactions to return the mocked user snapshots
        when(firestore.collection("Users").document(userId1).get().get()).thenReturn(user1Snapshot);
        when(firestore.collection("Users").document(userId2).get().get()).thenReturn(user2Snapshot);

        // Mock the service layer's updateElo method
        doNothing().when(eloService).updateElo(anyString(), anyString(), anyDouble(), anyDouble(), anyDouble(), anyDouble());

        // Perform the PUT request and check for a 200 OK response
        MvcResult result = mockMvc.perform(put("/api/elo/update/{userId1}/{userId2}", userId1, userId2)
                .contentType("application/json")
                .content("{\"AS1\": 0.5, \"AS2\": 0.5}"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertEquals("Elo ratings successfully updated", content);
    }

    @Test
    public void invalidElo() throws Exception {
        String userId1 = "user1";
        String userId2 = "user2";
        EloUpdateRequest request = new EloUpdateRequest(1, 0);

        DocumentSnapshot user1Snapshot = mock(DocumentSnapshot.class);
        DocumentSnapshot user2Snapshot = mock(DocumentSnapshot.class);
        
        when(user1Snapshot.exists()).thenReturn(true);
        when(user2Snapshot.exists()).thenReturn(true);
        
        when(user1Snapshot.getDouble("elo")).thenReturn(-1.0);
        when(user2Snapshot.getDouble("elo")).thenReturn(1300.0);

        when(firestore.collection("Users").document(userId1).get().get()).thenReturn(user1Snapshot);
        when(firestore.collection("Users").document(userId2).get().get()).thenReturn(user2Snapshot);

        doNothing().when(eloService).updateElo(anyString(), anyString(), anyDouble(), anyDouble(), anyDouble(), anyDouble());

        MvcResult result = mockMvc.perform(put("/api/elo/update/{userId1}/{userId2}", userId1, userId2)
                .contentType("application/json")
                .content("{\"AS1\": 1, \"AS2\": 0}"))
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertEquals("Elo1 and Elo2 must be non-negative.", content);
    }

    @Test
    public void user1NotFound() throws Exception {
        String userId1 = "user1"; 
        String userId2 = "user2"; 
        EloUpdateRequest request = new EloUpdateRequest(1, 0); 

        DocumentSnapshot user1Snapshot = mock(DocumentSnapshot.class);
        DocumentSnapshot user2Snapshot = mock(DocumentSnapshot.class);

        when(user1Snapshot.exists()).thenReturn(false); 
        when(user2Snapshot.exists()).thenReturn(true);  

        when(user2Snapshot.getDouble("elo")).thenReturn(1300.0);

        when(firestore.collection("Users").document(userId1).get().get()).thenReturn(user1Snapshot);
        when(firestore.collection("Users").document(userId2).get().get()).thenReturn(user2Snapshot);

        doNothing().when(eloService).updateElo(anyString(), anyString(), anyDouble(), anyDouble(), anyDouble(), anyDouble());

        MvcResult result = mockMvc.perform(put("/api/elo/update/{userId1}/{userId2}", userId1, userId2)
                .contentType("application/json")
                .content("{\"AS1\": 1, \"AS2\": 0}"))
                .andExpect(status().isNotFound())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertEquals("User 1 does not exist in Firebase.", content); 
    }

    @Test
    public void invalidAS() throws Exception {
        String userId1 = "user1";
        String userId2 = "user2";
        EloUpdateRequest request = new EloUpdateRequest(1, 0);

        // Mock Firestore response to simulate that both users exist
        DocumentSnapshot user1Snapshot = mock(DocumentSnapshot.class);
        DocumentSnapshot user2Snapshot = mock(DocumentSnapshot.class);
        
        when(user1Snapshot.exists()).thenReturn(true);
        when(user2Snapshot.exists()).thenReturn(true);
        
        // Mock Elo values for the users
        when(user1Snapshot.getDouble("elo")).thenReturn(1200.0);
        when(user2Snapshot.getDouble("elo")).thenReturn(1300.0);

        // Mock Firestore interactions to return the mocked user snapshots
        when(firestore.collection("Users").document(userId1).get().get()).thenReturn(user1Snapshot);
        when(firestore.collection("Users").document(userId2).get().get()).thenReturn(user2Snapshot);

        // Mock the service layer's updateElo method
        doNothing().when(eloService).updateElo(anyString(), anyString(), anyDouble(), anyDouble(), anyDouble(), anyDouble());

        MvcResult result = mockMvc.perform(put("/api/elo/update/{userId1}/{userId2}", userId1, userId2)
                .contentType("application/json")
                .content("{\"AS1\": -1, \"AS2\": 0}"))
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertEquals("AS1 and AS2 must be 0, 0.5, or 1.", content); 
    }

    @Test
    public void noSameAS() throws Exception {
        String userId1 = "user1";
        String userId2 = "user2";
        EloUpdateRequest request = new EloUpdateRequest(1, 0);

        // Mock Firestore response to simulate that both users exist
        DocumentSnapshot user1Snapshot = mock(DocumentSnapshot.class);
        DocumentSnapshot user2Snapshot = mock(DocumentSnapshot.class);
        
        when(user1Snapshot.exists()).thenReturn(true);
        when(user2Snapshot.exists()).thenReturn(true);
        
        // Mock Elo values for the users
        when(user1Snapshot.getDouble("elo")).thenReturn(1200.0);
        when(user2Snapshot.getDouble("elo")).thenReturn(1300.0);

        // Mock Firestore interactions to return the mocked user snapshots
        when(firestore.collection("Users").document(userId1).get().get()).thenReturn(user1Snapshot);
        when(firestore.collection("Users").document(userId2).get().get()).thenReturn(user2Snapshot);

        // Mock the service layer's updateElo method
        doNothing().when(eloService).updateElo(anyString(), anyString(), anyDouble(), anyDouble(), anyDouble(), anyDouble());

        MvcResult result = mockMvc.perform(put("/api/elo/update/{userId1}/{userId2}", userId1, userId2)
                .contentType("application/json")
                .content("{\"AS1\": 0, \"AS2\": 0}"))
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertEquals("AS1 and AS2 must be different or 0.5 each.", content); 
    }
}
