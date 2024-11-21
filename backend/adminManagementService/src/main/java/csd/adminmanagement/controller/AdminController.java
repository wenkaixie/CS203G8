package csd.adminmanagement.controller;

import java.util.List;
import java.util.concurrent.ExecutionException;

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

import csd.adminmanagement.model.MatchResultUpdateRequest;
import csd.adminmanagement.service.AdminService;
import csd.shared_library.model.Admin;
import csd.shared_library.model.Tournament;
import lombok.extern.slf4j.Slf4j;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/admin")
@Slf4j
public class AdminController {

    @Autowired
    private AdminService adminService;

    // Create Admin Profile
    @PostMapping("/createAdmin")
    public ResponseEntity<Admin> createAdmin(@RequestBody Admin newAdmin) {
        Admin response = adminService.createAdminProfile(newAdmin);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Update Admin Profile
    @PutMapping("/updateAdmin/{adminID}")
    public ResponseEntity<Object> updateAdminProfile(@PathVariable String adminID, @RequestBody Admin updatedAdmin) {
        Admin updatedAdminProfile = adminService.updateAdminProfile(adminID, updatedAdmin);
        return ResponseEntity.ok(updatedAdminProfile); // Return the updated admin
    }


    // Get all Admins
    @GetMapping("/getAllAdmins")
    public ResponseEntity<List<Admin>> getAllAdmins() {
        List<Admin> admins = adminService.getAllAdmins(); // Correct call to the service
        return ResponseEntity.ok(admins);
    }

    // Get Admin by ID
    @GetMapping("/getAdmin/{adminId}")
    public ResponseEntity<Object> getAdminById(@PathVariable String adminId) {
        Admin admin = adminService.getAdminById(adminId);
        return ResponseEntity.ok(admin);
    }

    // Get Admin Task List
    @GetMapping("/getAdminTaskList/{adminId}")
    public ResponseEntity<List<Tournament>> getAdminTaskList(@PathVariable String adminId) {
        List<Tournament> taskList = adminService.getTaskView(adminId);
        return ResponseEntity.ok(taskList);
    }

    // Match complete endpoint (for testing only)
    // @GetMapping("/completeMatch/{tournamentId}/{roundId}/{matchId}")
    // public ResponseEntity<Object> completeMatch(@PathVariable String tournamentId, @PathVariable String roundId, @PathVariable int matchId, @RequestBody MatchResultUpdateRequest matchResultUpdateRequest) {
    //     adminService.completeMatch(tournamentId, roundId, matchId, matchResultUpdateRequest);
    //     return ResponseEntity.ok("Match completed successfully");

    // }

    // Match complete endpoint
    @PutMapping("/completeMatch/{tournamentId}/{roundId}/{matchId}")
    public ResponseEntity<Object> completeMatch(@PathVariable String tournamentId, @PathVariable String roundId,
            @PathVariable int matchId, @RequestBody MatchResultUpdateRequest matchResultUpdateRequest) {
        adminService.completeMatch(tournamentId, roundId, matchId, matchResultUpdateRequest);
        return ResponseEntity.ok("Match completed successfully");
    }

    // wenkai 25/10
    // Get All Tournaments of Admin
    @GetMapping("/getAdminTournaments/{adminId}")
    public ResponseEntity<Object> getAdminTournaments(@PathVariable String adminId) {
        List<Tournament> tournaments = adminService.getAdminTournaments(adminId);
        return ResponseEntity.ok(tournaments);
    }

    // wenkai 25/10
    // Create Tournament

    // Jon - 19/11 should not be used
    // @PostMapping("/createTournament/{adminId}")
    // public ResponseEntity<Object> createTournament(@PathVariable String adminId, @RequestBody Tournament newTournament) {
    //     Tournament tournament = adminService.createTournament(adminId, newTournament);
    //     return ResponseEntity.ok(tournament);
    // }

    @GetMapping("/{adminID}/validate")
    public ResponseEntity<Boolean> validateAdmin(@PathVariable String adminID) {
        try {
            boolean exists = adminService.validateAdmin(adminID);
            return ResponseEntity.ok(exists);
        } catch (Exception e) {
            log.error("Error validating admin with ID {}: {}", adminID, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    @PostMapping("/{adminID}/add-tournament")
    public ResponseEntity<Void> addTournamentToAdmin(@PathVariable String adminID, @RequestBody String tournamentID) {
        try {
            adminService.addTournamentToAdmin(adminID, tournamentID);
            return ResponseEntity.ok().build(); // Return 200 OK with no body
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error adding tournament ID {} to admin ID {}: {}", tournamentID, adminID, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // Return 500 with no body
        } catch (Exception e) {
            log.error("Unexpected error adding tournament ID {} to admin ID {}: {}", tournamentID, adminID,
                    e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // Return 500 with no body
        }
    }

    @PostMapping("/{adminID}/remove-tournament")
    public ResponseEntity<Void> removeTournamentFromAdmin(@PathVariable String adminID,
            @RequestBody String tournamentID) {
        try {
            adminService.removeTournamentFromAdmin(adminID, tournamentID);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    
}
