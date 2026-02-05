package org.example.stockpotrfolio.exception;

/**
 * Exception thrown when validation fails for user input.
 * Maps to HTTP 400 Bad Request status code.
 */
public class ValidationException extends StockPortfolioException {

    /**
     * Constructs a new ValidationException with the specified message.
     *
     * @param message the validation error message
     */
    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR");
    }

    /**
     * Constructs a new ValidationException with message and details.
     *
     * @param message the validation error message
     * @param details additional details about what failed validation
     */
    public ValidationException(String message, String details) {
        super(message, "VALIDATION_ERROR", details);
    }

    /**
     * Constructs a new ValidationException with message and cause.
     *
     * @param message the validation error message
     * @param cause   the cause of the validation failure
     */
    public ValidationException(String message, Throwable cause) {
        super(message, "VALIDATION_ERROR", cause);
    }
}
