package org.example.stockpotrfolio.exception;

/**
 * Exception thrown when there are insufficient shares to complete a transaction.
 * Maps to HTTP 400 Bad Request status code.
 */
public class InsufficientSharesException extends StockPortfolioException {

    private final String symbol;
    private final Integer requested;
    private final Integer available;

    /**
     * Constructs a new InsufficientSharesException with the specified message.
     *
     * @param message the error message
     */
    public InsufficientSharesException(String message) {
        super(message, "INSUFFICIENT_SHARES");
        this.symbol = null;
        this.requested = null;
        this.available = null;
    }

    /**
     * Constructs a new InsufficientSharesException with transaction details.
     *
     * @param symbol    the stock symbol
     * @param requested the number of shares requested to sell
     * @param available the number of shares actually available
     */
    public InsufficientSharesException(String symbol, Integer requested, Integer available) {
        super(String.format("Insufficient shares for %s. Requested: %d, Available: %d",
                symbol, requested, available), "INSUFFICIENT_SHARES");
        this.symbol = symbol;
        this.requested = requested;
        this.available = available;
    }

    public String getSymbol() {
        return symbol;
    }

    public Integer getRequested() {
        return requested;
    }

    public Integer getAvailable() {
        return available;
    }
}
