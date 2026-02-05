package org.example.stockpotrfolio.exception;

/**
 * Exception thrown when rate limits are exceeded for external API calls.
 * Maps to HTTP 429 Too Many Requests status code.
 */
public class RateLimitExceededException extends StockPortfolioException {

    private final String apiName;
    private final Long retryAfterSeconds;

    /**
     * Constructs a new RateLimitExceededException with the specified message.
     *
     * @param message the error message
     */
    public RateLimitExceededException(String message) {
        super(message, "RATE_LIMIT_EXCEEDED");
        this.apiName = null;
        this.retryAfterSeconds = null;
    }

    /**
     * Constructs a new RateLimitExceededException with API name.
     *
     * @param message the error message
     * @param apiName the name of the API with rate limit exceeded
     */
    public RateLimitExceededException(String message, String apiName) {
        super(message, "RATE_LIMIT_EXCEEDED");
        this.apiName = apiName;
        this.retryAfterSeconds = null;
    }

    /**
     * Constructs a new RateLimitExceededException with retry information.
     *
     * @param message           the error message
     * @param apiName           the name of the API
     * @param retryAfterSeconds seconds until the rate limit resets
     */
    public RateLimitExceededException(String message, String apiName, Long retryAfterSeconds) {
        super(message, "RATE_LIMIT_EXCEEDED");
        this.apiName = apiName;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public String getApiName() {
        return apiName;
    }

    public Long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
