package csd.adminmanagementservice;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyMap;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.csd.shared_library.model.Admin;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import csd.adminmanagement.Service.AdminService;

@ExtendWith(MockitoExtension.class)
public class AdminServiceUpdateTest {

    @Mock
    private Firestore firestore;

    @Mock
    private DocumentReference adminDocRef;

    @Mock
    private DocumentSnapshot adminDocSnap;

    @Mock
    private CollectionReference adminCollection;

    @Mock
    private Query adminQuery;

    @Mock
    private ApiFuture<QuerySnapshot> adminQueryFuture;

    @Mock
    private QuerySnapshot adminQuerySnapshot;

    @Mock
    private QueryDocumentSnapshot adminSnapshot;

    @InjectMocks
    private AdminService adminService;


    @Test
    void updateAdminProfile_Pass() throws ExecutionException, InterruptedException {
        // Arrange
        String adminId = "admin123";
        Admin updatedAdmin = new Admin();
        updatedAdmin.setAuthId(adminId);
        updatedAdmin.setName("Updated Admin");

        // Mock Firestore behavior
        when(firestore.collection("Admins")).thenReturn(adminCollection); // Return a mock collection
        when(adminCollection.document(adminId)).thenReturn(adminDocRef);  // Return a mock document reference
        when(adminDocRef.update(anyMap())).thenReturn(ApiFutures.immediateFuture(null)); // Simulate successful update

        // Act
        Admin result = adminService.updateAdminProfile(adminId, updatedAdmin);

        // Assert
        assertEquals(updatedAdmin, result);
        verify(adminDocRef).update(anyMap());
    }

    @Test
    void updateAdminProfile_Fail() throws ExecutionException, InterruptedException {
        // Arrange
        String adminId = "admin123";
        Admin updatedAdmin = new Admin();
        updatedAdmin.setAuthId(adminId);
        updatedAdmin.setName("Updated Admin");
    
        // Mock Firestore behavior
        when(firestore.collection("Admins")).thenReturn(adminCollection);
        when(adminCollection.document(adminId)).thenReturn(adminDocRef);
        when(adminDocRef.update(anyMap())).thenThrow(new RuntimeException("Firestore update failed"));
    
        // Act
        Admin result = null;
        String errorMessage = null;
        try {
            result = adminService.updateAdminProfile(adminId, updatedAdmin);
        } catch (RuntimeException e) {
            errorMessage = e.getMessage();
        }
    
        // Assert
        assertEquals(null, result); // Ensure result is null when an exception occurs
        assertEquals("Firestore update failed", errorMessage); // Validate the exception message
        verify(adminDocRef).update(anyMap());
    }
    
}