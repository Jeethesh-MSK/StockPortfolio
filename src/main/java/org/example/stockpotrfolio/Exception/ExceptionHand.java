
package org.example.stockpotrfolio.Exception;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionHand {

    // Handles specific Database connection or SQL errors
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<Object> handleDatabaseException(SQLException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", "Database error occurred");
        body.put("details", ex.getMessage());
        
        return new ResponseEntity<>(body, HttpStatus.SERVICE_UNAVAILABLE);
    }

    // A generic fallback handler so the app doesn't crash on unknown errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralException(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", "An unexpected error occurred");
        
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}