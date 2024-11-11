package csd.adminmanagement.Controllers;

import csd.adminmanagement.Exception.AdminNotFoundException;
import csd.adminmanagement.Exception.TournamentNotFoundException;
import csd.adminmanagement.Model.Admin;
import csd.adminmanagement.Model.Tournament;
import csd.adminmanagement.Service.AdminService;
import csd.adminmanagement.Model.MatchResultUpdateRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.checkerframework.checker.units.qual.m;
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
@CrossOrigin(origins = "http://matchup-cs203g8.s3-website-ap-southeast-1.amazonaws.com")
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private AdminService adminService;

    // Update Admin Profile
    @PutMapping("/updateAdmin/{adminID}")
    public ResponseEntity<Object> updateAdminProfile(@PathVariable String adminID, @RequestBody Admin updatedAdmin) {
        Admin updatedAdminProfile = adminService.updateAdminProfile(adminID, updatedAdmin);
        return ResponseEntity.ok(updatedAdminProfile); // Return the updated admin
    }
    
    // Create Admin Profile
    @PostMapping("/createAdmin")
    public ResponseEntity<Admin> createAdmin(@RequestBody Admin newAdmin) {
        Admin response = adminService.createAdminProfile(newAdmin);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get all Admins
    @GetMapping("/getAllAdmins")
    public ResponseEntity<List<Admin>> getAllAdmins(){
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
    public ResponseEntity<Object> completeMatch(@PathVariable String tournamentId, @PathVariable String roundId, @PathVariable int matchId, @RequestBody MatchResultUpdateRequest matchResultUpdateRequest) {
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
    @PostMapping("/createTournament/{adminId}")
    public ResponseEntity<Object> createTournament(@PathVariable String adminId, @RequestBody Tournament newTournament) {
        Tournament tournament = adminService.createTournament(adminId, newTournament);
        return ResponseEntity.ok(tournament);
    }

}
