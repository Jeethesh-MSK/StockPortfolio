package org.example.stockpotrfolio.exception;

/**
 * Exception thrown when external API calls fail.
 * Maps to HTTP 502 Bad Gateway or 503 Service Unavailable status code.
 */
public class ExternalApiException extends StockPortfolioException {

    private final String apiName;
    private final Integer statusCode;

    /**
     * Constructs a new ExternalApiException with the specified message.
     *
     * @param message the error message
     */
    public ExternalApiException(String message) {
        super(message, "EXTERNAL_API_ERROR");
        this.apiName = null;
        this.statusCode = null;
    }

    /**
     * Constructs a new ExternalApiException with API name.
     *
     * @param message the error message
     * @param apiName the name of the external API (e.g., "Finnhub", "TwelveData")
     */
    public ExternalApiException(String message, String apiName) {
        super(message, "EXTERNAL_API_ERROR", apiName);
        this.apiName = apiName;
        this.statusCode = null;
    }

    /**
     * Constructs a new ExternalApiException with full details.
     *
     * @param message    the error message
     * @param apiName    the name of the external API
     * @param statusCode the HTTP status code returned by the API (if any)
     */
    public ExternalApiException(String message, String apiName, Integer statusCode) {
        super(message, "EXTERNAL_API_ERROR");
        this.apiName = apiName;
        this.statusCode = statusCode;
    }

    /**
     * Constructs a new ExternalApiException with message and cause.
     *
     * @param message the error message
     * @param cause   the underlying exception from the API call
     */
    public ExternalApiException(String message, Throwable cause) {
        super(message, "EXTERNAL_API_ERROR", cause);
        this.apiName = null;
        this.statusCode = null;
    }

    /**
     * Constructs a new ExternalApiException with full details and cause.
     *
     * @param message the error message
     * @param apiName the name of the external API
     * @param cause   the underlying exception
     */
    public ExternalApiException(String message, String apiName, Throwable cause) {
        super(message, "EXTERNAL_API_ERROR", cause);
        this.apiName = apiName;
        this.statusCode = null;
    }

    public String getApiName() {
        return apiName;
    }

    public Integer getStatusCode() {
        return statusCode;
    }
}
