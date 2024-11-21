package csd.adminmanagement.consumers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import csd.shared_library.model.Admin;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import csd.adminmanagement.exception.AdminNotFoundException;
import csd.adminmanagement.service.AdminService;

@ExtendWith(MockitoExtension.class)
public class AdminServiceGetAdminTest {
    
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

    public AdminServiceGetAdminTest() {
    }

    @BeforeEach
    void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);
        lenient().when(firestore.collection("Admins")).thenReturn(adminCollection);
        lenient().when(adminCollection.get()).thenReturn(adminQueryFuture);
    }


    // Successful test to get admin by ID
    @Test
    void getAdminById_Pass() throws ExecutionException, InterruptedException {
        // Arrange
        String adminId = "admin123";
        Admin expectedAdmin = new Admin();
        expectedAdmin.setAuthId(adminId);

        Query adminQuery = Mockito.mock(Query.class); // Mock the intermediate Query object

        when(firestore.collection("Admins")).thenReturn(adminCollection); // Mock Admins collection
        when(adminCollection.whereEqualTo("authId", adminId)).thenReturn(adminQuery); // Mock filter query
        when(adminQuery.get()).thenReturn(adminQueryFuture); // Mock get() on Query
        when(adminQueryFuture.get()).thenReturn(adminQuerySnapshot); // Mock get() on ApiFuture<QuerySnapshot>
        when(adminQuerySnapshot.getDocuments()).thenReturn(Collections.singletonList(adminSnapshot)); // Mock documents
        when(adminSnapshot.toObject(Admin.class)).thenReturn(expectedAdmin); // Mock conversion to Admin object

        // Act
        Admin result = adminService.getAdminById(adminId);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(adminId, result.getAuthId(), "Admin ID should match");
    }


    
    // Unsuccessful test to get admin by ID
    @Test
    void getAdminById_Fail() throws ExecutionException, InterruptedException {
        // Arrange
        String adminId = "admin123";
        Query adminQuery = Mockito.mock(Query.class); // Mock the intermediate Query object
    
        when(firestore.collection("Admins")).thenReturn(adminCollection); // Mock Admins collection
        when(adminCollection.whereEqualTo("authId", adminId)).thenReturn(adminQuery); // Mock filter query
        when(adminQuery.get()).thenReturn(adminQueryFuture); // Mock get() on Query
        when(adminQueryFuture.get()).thenReturn(adminQuerySnapshot); // Mock get() on ApiFuture<QuerySnapshot>
        when(adminQuerySnapshot.getDocuments()).thenReturn(Collections.emptyList()); // Simulate no matching documents
    
        // Act & Assert
        assertThrows(AdminNotFoundException.class, () -> adminService.getAdminById(adminId), 
            "AdminNotFoundException should be thrown when no admin is found");
    }

    public Firestore getFirestore() {
        return firestore;
    }

    public void setFirestore(Firestore firestore) {
        this.firestore = firestore;
    }

    public DocumentReference getAdminDocRef() {
        return adminDocRef;
    }

    public void setAdminDocRef(DocumentReference adminDocRef) {
        this.adminDocRef = adminDocRef;
    }

    public ApiFuture<DocumentSnapshot> getAdminDocSnapFuture() {
        return adminDocSnapFuture;
    }

    public void setAdminDocSnapFuture(ApiFuture<DocumentSnapshot> adminDocSnapFuture) {
        this.adminDocSnapFuture = adminDocSnapFuture;
    }

    public DocumentSnapshot getAdminDocSnap() {
        return adminDocSnap;
    }

    public void setAdminDocSnap(DocumentSnapshot adminDocSnap) {
        this.adminDocSnap = adminDocSnap;
    }

    public CollectionReference getAdminCollection() {
        return adminCollection;
    }

    public void setAdminCollection(CollectionReference adminCollection) {
        this.adminCollection = adminCollection;
    }

    public ApiFuture<QuerySnapshot> getAdminQueryFuture() {
        return adminQueryFuture;
    }

    public void setAdminQueryFuture(ApiFuture<QuerySnapshot> adminQueryFuture) {
        this.adminQueryFuture = adminQueryFuture;
    }

    public Query getAdminQuery() {
        return adminQuery;
    }

    public void setAdminQuery(Query adminQuery) {
        this.adminQuery = adminQuery;
    }

    public QuerySnapshot getAdminQuerySnapshot() {
        return adminQuerySnapshot;
    }

    public void setAdminQuerySnapshot(QuerySnapshot adminQuerySnapshot) {
        this.adminQuerySnapshot = adminQuerySnapshot;
    }

    public QueryDocumentSnapshot getAdminSnapshot() {
        return adminSnapshot;
    }

    public void setAdminSnapshot(QueryDocumentSnapshot adminSnapshot) {
        this.adminSnapshot = adminSnapshot;
    }

    public AdminService getAdminService() {
        return adminService;
    }

    public void setAdminService(AdminService adminService) {
        this.adminService = adminService;
    }
    
}
