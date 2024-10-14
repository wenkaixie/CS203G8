package csd.matchresult.Controllers;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import csd.matchresult.Model.*;
import csd.matchresult.Service.MatchResultService;


@RestController
@RequestMapping("/match")
public class MatchMakingController {
    
    @Autowired 
    private MatchResultService matchResultService;


    @PostMapping("/recordMatchResult/{tournamentId}")
    public ResponseEntity<Matches> recordMatchResult(@RequestBody Matches newMatch) {
        try {
            Matches response = matchResultService.recordMatchResult(newMatch);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
