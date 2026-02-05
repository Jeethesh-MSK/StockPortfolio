package org.example.stockpotrfolio.exception;

/**
 * Base exception class for all Stock Portfolio application exceptions.
 * Provides a common parent for categorized exception handling.
 */
public class StockPortfolioException extends RuntimeException {

    private final String errorCode;
    private final String details;

    /**
     * Constructs a new StockPortfolioException with the specified message.
     *
     * @param message the error message
     */
    public StockPortfolioException(String message) {
        super(message);
        this.errorCode = "STOCK_PORTFOLIO_ERROR";
        this.details = null;
    }

    /**
     * Constructs a new StockPortfolioException with the specified message and error code.
     *
     * @param message   the error message
     * @param errorCode the error code for categorization
     */
    public StockPortfolioException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.details = null;
    }

    /**
     * Constructs a new StockPortfolioException with full details.
     *
     * @param message   the error message
     * @param errorCode the error code for categorization
     * @param details   additional details about the error
     */
    public StockPortfolioException(String message, String errorCode, String details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }

    /**
     * Constructs a new StockPortfolioException with the specified message and cause.
     *
     * @param message the error message
     * @param cause   the cause of the exception
     */
    public StockPortfolioException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "STOCK_PORTFOLIO_ERROR";
        this.details = null;
    }

    /**
     * Constructs a new StockPortfolioException with full details and cause.
     *
     * @param message   the error message
     * @param errorCode the error code for categorization
     * @param cause     the cause of the exception
     */
    public StockPortfolioException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = null;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getDetails() {
        return details;
    }
}
