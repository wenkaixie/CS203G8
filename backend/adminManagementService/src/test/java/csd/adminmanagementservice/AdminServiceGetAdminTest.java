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

import com.csd.shared_library.model.Admin;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import csd.adminmanagement.Exception.AdminNotFoundException;
import csd.adminmanagement.Service.AdminService;

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

    // Successful test to get all admins
    @Test
    void getAdmins_Pass() throws ExecutionException, InterruptedException {
        // Arrange
        Admin mockAdmin = new Admin();
        mockAdmin.setAuthId("admin123");
    
        when(firestore.collection("Admins")).thenReturn(adminCollection); // Mock Admins collection
        when(adminCollection.get()).thenReturn(adminQueryFuture); // Mock get() call on CollectionReference
        when(adminQueryFuture.get()).thenReturn(adminQuerySnapshot); // Mock get() on ApiFuture<QuerySnapshot>
        
        // Ensure getDocuments() returns a non-null list
        when(adminQuerySnapshot.getDocuments()).thenReturn(Collections.singletonList(adminSnapshot)); // Mock documents
        when(adminSnapshot.toObject(Admin.class)).thenReturn(mockAdmin); // Mock conversion to Admin object
    
        // Act
        List<Admin> result = adminService.getAllAdmins();
    
        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.size(), "Result list size should be 1");
        assertEquals("admin123", result.get(0).getAuthId(), "Admin ID should match");
    }
    
    
    
    @Test
    void getAdmins_Fail() throws ExecutionException, InterruptedException {
        // Arrange
        when(firestore.collection("Admins")).thenReturn(adminCollection);
        when(adminCollection.get()).thenReturn(adminQueryFuture);
        when(adminQueryFuture.get()).thenReturn(adminQuerySnapshot);
        when(adminQuerySnapshot.getDocuments()).thenReturn(Collections.emptyList()); // Simulate no documents
    
        // Act
        List<Admin> result = adminService.getAllAdmins();
    
        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(0, result.size(), "Result list should be empty");
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
    
}
