package csd.adminmanagement.consumers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import csd.adminmanagement.service.AdminService;
import csd.shared_library.model.Admin;

import java.util.concurrent.ExecutionException;
import java.util.HashMap;
import java.util.Map;

class AdminServiceUpdateTest {

    @Mock
    private Firestore firestore;

    @Mock
    private DocumentReference adminDocRef;

    @Mock
    private CollectionReference adminCollection;

    @InjectMocks
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void updateAdminProfile_Pass() throws ExecutionException, InterruptedException {
        // Arrange
        String adminId = "admin123";
        Admin updatedAdmin = new Admin();
        updatedAdmin.setAuthId(adminId);
        updatedAdmin.setName("Updated Admin");

        Map<String, Object> updatedFields = new HashMap<>();
        updatedFields.put("authId", adminId);
        updatedFields.put("name", "Updated Admin");

        // Mock Firestore behavior
        when(firestore.collection("Admins")).thenReturn(adminCollection); // Mock the Admins collection
        when(adminCollection.document(adminId)).thenReturn(adminDocRef);  // Mock the document reference
        when(adminDocRef.update(updatedFields)).thenReturn(ApiFutures.immediateFuture(null)); // Simulate successful update

        // Act
        Admin result = adminService.updateAdminProfile(adminId, updatedAdmin);

        // Assert
        assertEquals(updatedAdmin.getAuthId(), result.getAuthId());
        assertEquals(updatedAdmin.getName(), result.getName());
        verify(adminDocRef, times(1)).update(updatedFields);
    }

    @Test
    void updateAdminProfile_Fail() throws ExecutionException, InterruptedException {
        // Arrange
        String adminId = "admin123";
        Admin updatedAdmin = new Admin();
        updatedAdmin.setAuthId(adminId);
        updatedAdmin.setName("Updated Admin");

        Map<String, Object> updatedFields = new HashMap<>();
        updatedFields.put("authId", adminId);
        updatedFields.put("name", "Updated Admin");

        // Mock Firestore behavior to throw an exception
        when(firestore.collection("Admins")).thenReturn(adminCollection);
        when(adminCollection.document(adminId)).thenReturn(adminDocRef);
        when(adminDocRef.update(updatedFields)).thenThrow(new RuntimeException("Admin not found or update failed"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            adminService.updateAdminProfile(adminId, updatedAdmin)
        );
        assertEquals("Admin not found or update failed", exception.getMessage());
        verify(adminDocRef, times(1)).update(updatedFields);
    }
}
