package csd.adminmanagement.Controllers;

import csd.adminmanagement.Exception.AdminNotFoundException;
import csd.adminmanagement.Model.Admin;
import csd.adminmanagement.Service.AdminService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private AdminService adminService;

    // Update Admin Profile
    @PutMapping("/updateAdmin/{adminID}")
    public ResponseEntity<Object> updateAdminProfile(@PathVariable String adminID, @RequestBody Admin updatedAdmin) {
        try {
            Admin updatedAdminProfile = adminService.updateAdminProfile(adminID, updatedAdmin);
            return ResponseEntity.ok(updatedAdminProfile); // Return the updated admin
        } catch (AdminNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse("An unexpected error occurred."));
        }
    }
    
    // Create Admin Profile
    @PostMapping("/createAdmin")
    public ResponseEntity<Admin> createAdmin(@RequestBody Admin newAdmin) {
        try {
            Admin response = adminService.createAdminProfile(newAdmin);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Get all Admins
    @GetMapping("/getAllAdmins")
    public ResponseEntity<List<Admin>> getAllAdmins(){
        try {
            List<Admin> admins = adminService.getAllAdmins(); // Correct call to the service
            return ResponseEntity.ok(admins);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

// Get Admin by ID
    @GetMapping("/getAdmin/{adminId}")
    public ResponseEntity<Object> getAdminById(@PathVariable String adminId) {
        try {
            Admin admin = adminService.getAdminById(adminId);
            return ResponseEntity.ok(admin);
        } catch (AdminNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse("An unexpected error occurred."));
        }
    }

    // Helper method to create error responses
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        return errorResponse;
    }

}
