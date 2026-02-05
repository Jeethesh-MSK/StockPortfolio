package org.example.stockpotrfolio.exception;

/**
 * Exception thrown when database operations fail.
 * Maps to HTTP 500 Internal Server Error status code.
 * Provides user-friendly messages without exposing sensitive database details.
 */
public class DatabaseException extends StockPortfolioException {

    private final String operation;

    /**
     * Constructs a new DatabaseException with the specified message.
     *
     * @param message the error message (user-friendly, no sensitive details)
     */
    public DatabaseException(String message) {
        super(message, "DATABASE_ERROR");
        this.operation = null;
    }

    /**
     * Constructs a new DatabaseException with operation details.
     *
     * @param message   the error message (user-friendly, no sensitive details)
     * @param operation the database operation that failed (e.g., "save", "delete", "find")
     */
    public DatabaseException(String message, String operation) {
        super(message, "DATABASE_ERROR", operation);
        this.operation = operation;
    }

    /**
     * Constructs a new DatabaseException with message and cause.
     * The cause is logged internally but not exposed to users.
     *
     * @param message the user-friendly error message
     * @param cause   the underlying database exception (logged but not exposed)
     */
    public DatabaseException(String message, Throwable cause) {
        super(message, "DATABASE_ERROR", cause);
        this.operation = null;
    }

    /**
     * Constructs a new DatabaseException with full details.
     *
     * @param message   the user-friendly error message
     * @param operation the database operation that failed
     * @param cause     the underlying database exception
     */
    public DatabaseException(String message, String operation, Throwable cause) {
        super(message, "DATABASE_ERROR", cause);
        this.operation = operation;
    }

    public String getOperation() {
        return operation;
    }
}
