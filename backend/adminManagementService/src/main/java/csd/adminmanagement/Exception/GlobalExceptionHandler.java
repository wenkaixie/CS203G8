package csd.adminmanagement.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TournamentNotFoundException.class)
    public ResponseEntity<Object> handleTournamentNotFound(TournamentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(AdminNotFoundException.class)
    public ResponseEntity<Object> handleAdminNotFound(AdminNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse("An unexpected error occurred"));
    }

    // Helper method to create error responses
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        return errorResponse;
    }
}
