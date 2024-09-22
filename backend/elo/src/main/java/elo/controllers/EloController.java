package elo.controller;

import elo.model.EloUpdateRequest;
import elo.service.EloService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/elo")
public class EloController {

    private final EloService eloService;
    private static final Logger logger = LoggerFactory.getLogger(EloController.class);

    public EloController(EloService eloService) {
        this.eloService = eloService;
    }

    // Update Elo ratings
    @PostMapping("/update")
    public ResponseEntity<Object> updateElo(
            @RequestParam String userId1,
            @RequestParam String userId2,
            @RequestBody EloUpdateRequest request) {

        // Manually validating the request object
        try {
            request.validate(); // Call the manual validation method in EloUpdateRequest
        } catch (IllegalArgumentException e) {
            return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        if (userId1 == null || userId1.isEmpty() || userId2 == null || userId2.isEmpty()) {
            return createErrorResponse("userId1 and userId2 are required.", HttpStatus.BAD_REQUEST);
        }

        logger.info("Received updateElo request: userId1={}, userId2={}, request={}", userId1, userId2, request);

        double AS1 = request.getAS1();
        double AS2 = request.getAS2();
        if (!(AS1 == 0 || AS1 == 0.5 || AS1 == 1) || !(AS2 == 0 || AS2 == 0.5 || AS2 == 1)) {
            return createErrorResponse("AS1 and AS2 must be 0, 0.5, or 1.", HttpStatus.BAD_REQUEST);
        }

        try {
            eloService.updateElo(userId1, userId2, request.getElo1(), request.getElo2(), AS1, AS2);
            return new ResponseEntity<>("Elo ratings successfully updated", HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Error updating Elo ratings: {}", e.getMessage(), e);
            return createErrorResponse("Internal Server Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            // Catch any checked exceptions that might have been thrown
            logger.error("Unhandled Exception: {}", e.getMessage(), e);
            return createErrorResponse("Internal Server Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Retrieve Elo for a specific user
    public ResponseEntity<Object> getElo(String userID) {
        Firestore db = FirestoreClient.getFirestore();
    
        try {
            DocumentSnapshot userSnapshot = db.collection("Users").document(userID).get().get();
    
            if (userSnapshot.exists()) {
                return new ResponseEntity<>(userSnapshot.get("Elo"), HttpStatus.OK);
            } else {
                return createErrorResponse("User does not exist.", HttpStatus.NOT_FOUND);
            }
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error retrieving Elo: {}", e.getMessage(), e);
            return createErrorResponse("Internal Server Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    private ResponseEntity<Object> createErrorResponse(String message, HttpStatus status) {
        return new ResponseEntity<>(message, status);
    }
}



// package elo.controller;

// import elo.model.EloUpdateRequest;
// import elo.service.EloService;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import com.google.cloud.firestore.Firestore;
// import com.google.cloud.firestore.DocumentSnapshot;
// import com.google.firebase.cloud.FirestoreClient;
// import java.util.concurrent.ExecutionException;

// @RestController
// @RequestMapping("/api/elo")
// public class EloController {

//     private final EloService eloService;
//     private static final Logger logger = LoggerFactory.getLogger(EloController.class);

//     public EloController(EloService eloService) {
//         this.eloService = eloService;
//     }

//     // Update Elo ratings
//     @PostMapping("/update")
//     public ResponseEntity<Object> updateElo(
//             @RequestParam String userId1,
//             @RequestParam String userId2,
//             @RequestBody EloUpdateRequest request) {

//         // Manually validating the request object
//         try {
//             request.validate(); // Call the manual validation method in EloUpdateRequest
//         } catch (IllegalArgumentException e) {
//             return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
//         }

//         if (userId1 == null || userId1.isEmpty() || userId2 == null || userId2.isEmpty()) {
//             return createErrorResponse("userId1 and userId2 are required.");
//         }

//         logger.info("Received updateElo request: userId1={}, userId2={}, request={}", userId1, userId2, request);

//         double AS1 = request.getAS1();
//         double AS2 = request.getAS2();
//         if (!(AS1 == 0 || AS1 == 0.5 || AS1 == 1) || !(AS2 == 0 || AS2 == 0.5 || AS2 == 1)) {
//             return createErrorResponse("AS1 and AS2 must be 0, 0.5, or 1.");
//         }

//         try {
//             eloService.updateElo(userId1, userId2, request.getElo1(), request.getElo2(), AS1, AS2);
//             return new ResponseEntity<>("Elo ratings successfully updated", HttpStatus.OK);
//         } catch (Exception e) {
//             logger.error("Error updating Elo ratings: {}", e.getMessage(), e);
//             return createErrorResponse("Error updating Elo ratings: " + e.getMessage());
//         }
//     }

//     public Object getElo(String userID) {
//         Firestore db = FirestoreClient.getFirestore();
    
//         try {
//             DocumentSnapshot userSnapshot = db.collection("Users").document(userID).get().get();
    
//             if (userSnapshot.exists()) {
//                 return userSnapshot.get("Elo");
//             } else {
//                 return "User does not exist.";  // Handle non-existent user case
//             }
//         } catch (ExecutionException | InterruptedException e) {
//             // Handle Firestore operation failures
//             System.err.println("Error retrieving Elo: " + e.getMessage());
//             return null;  // Return null or appropriate fallback in case of error
//         } catch (Exception e) {
//             // General catch-all for other exceptions
//             System.err.println("General error: " + e.getMessage());
//             return null;
//         }
//     }
    
//     private ResponseEntity<Object> createErrorResponse(String message) {
//         return createErrorResponse(message, HttpStatus.BAD_REQUEST);
//     }

//     private ResponseEntity<Object> createErrorResponse(String message, HttpStatus status) {
//         return new ResponseEntity<>(message, status);
//     }
// }
