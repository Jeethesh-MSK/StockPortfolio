package org.example.stockpotrfolio.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Standardized error response DTO for API error messages.
 * Used across all controllers for consistent error handling.
 * Includes timestamp and request ID for debugging and log correlation.
 */
@Getter
@Setter
public class ErrorResponse {
    private String error;
    private String message;
    private LocalDateTime timestamp;
    private String requestId;

    /**
     * Constructs an ErrorResponse with error type and message.
     * Automatically generates timestamp and request ID.
     *
     * @param error   the error type/code
     * @param message the user-friendly error message
     */
    public ErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.requestId = UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Constructs an ErrorResponse with all fields specified.
     *
     * @param error     the error type/code
     * @param message   the user-friendly error message
     * @param timestamp the time of the error
     * @param requestId the request identifier for log correlation
     */
    public ErrorResponse(String error, String message, LocalDateTime timestamp, String requestId) {
        this.error = error;
        this.message = message;
        this.timestamp = timestamp;
        this.requestId = requestId;
    }
}
