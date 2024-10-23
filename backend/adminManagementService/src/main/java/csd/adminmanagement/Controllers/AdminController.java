package csd.adminmanagement.Controllers;

import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import csd.adminmanagement.Model.Admin;
import csd.adminmanagement.Exception.TournamentNotFoundException;
import csd.adminmanagement.Exception.AdminNotFoundException;
import csd.adminmanagement.Model.Tournament;
import csd.adminmanagement.Service.FirestoreService;
import csd.adminmanagement.Service.AdminService;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private AdminService adminService;


    @PutMapping("/updateAdmin/{adminID}")
    public ResponseEntity<Object> updateAdminProfile(@PathVariable String adminID, @RequestBody AdminService updatedAdmin) {
        try {
            AdminService updatedAdminProfile = adminService.updateAdminProfile(adminID, updatedAdmin);
            return ResponseEntity.ok(updatedAdminProfile);
        } catch (AdminNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse("An unexpected error occurred."));
        }
    }
    
    @PostMapping("/createAdmin")
    public ResponseEntity<Admin> createAdmin(@RequestBody Admin newAdmin) {
        try {
            Admin response = adminService.createAdminProfile(newAdmin);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/getAllAdmins")
    public ResponseEntity<List<Admin>> getAllAdmins(){
        try {
            List<Admin> admins = AdminService.getAllAdmins();
            return ResponseEntity.ok(admins);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/getAdmin/{adminID}")
    public ResponseEntity<Object> getAdmin(@PathVariable String adminID) {
        try {
            Admin admin = adminService.getAdminbyId(adminID);
            return ResponseEntity.ok(admin);  
        } catch (AdminNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse("An unexpected error occurred."));
        }
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        return errorResponse;
    }

}
