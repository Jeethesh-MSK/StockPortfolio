package org.example.stockpotrfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Standardized error response DTO for API error messages.
 * Used across all controllers for consistent error handling.
 */
@Getter
@Setter
@AllArgsConstructor
public class ErrorResponse {
    private String error;
    private String message;
}
