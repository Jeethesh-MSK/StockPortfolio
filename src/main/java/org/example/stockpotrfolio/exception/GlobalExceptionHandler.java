package org.example.stockpotrfolio.exception;

import lombok.extern.slf4j.Slf4j;
import org.example.stockpotrfolio.dto.ErrorResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.validation.ConstraintViolationException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.stream.Collectors;

/**
 * Global exception handler for the Stock Portfolio application.
 * Provides centralized exception handling across all controllers with:
 * - Proper HTTP status codes for different error types
 * - User-friendly error messages
 * - Detailed logging with stack traces for debugging
 * - Protection of sensitive information (e.g., database details)
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ========================
    // Custom Exception Handlers
    // ========================

    /**
     * Handles ValidationException - returns 400 Bad Request.
     *
     * @param ex the ValidationException
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex) {
        log.warn("Validation error: {} - Details: {}", ex.getMessage(), ex.getDetails());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Validation Error", ex.getMessage()));
    }

    /**
     * Handles ResourceNotFoundException - returns 404 Not Found.
     *
     * @param ex the ResourceNotFoundException
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Resource not found: {} - Type: {}, ID: {}",
                ex.getMessage(), ex.getResourceType(), ex.getResourceId());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("Not Found", ex.getMessage()));
    }

    /**
     * Handles DatabaseException - returns 500 Internal Server Error.
     * Logs full details but returns user-friendly message without sensitive info.
     *
     * @param ex the DatabaseException
     * @return ResponseEntity with sanitized error message
     */
    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseException(DatabaseException ex) {
        log.error("Database error during operation '{}': {}", ex.getOperation(), ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Database Error",
                        "A database error occurred. Please try again later."));
    }

    /**
     * Handles ExternalApiException - returns 502 Bad Gateway.
     *
     * @param ex the ExternalApiException
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ErrorResponse> handleExternalApiException(ExternalApiException ex) {
        log.error("External API error ({}): {} - Status Code: {}",
                ex.getApiName(), ex.getMessage(), ex.getStatusCode(), ex);
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResponse("External Service Error",
                        "Unable to connect to external service. Please try again later."));
    }

    /**
     * Handles RateLimitExceededException - returns 429 Too Many Requests.
     *
     * @param ex the RateLimitExceededException
     * @return ResponseEntity with retry information
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceededException(RateLimitExceededException ex) {
        log.warn("Rate limit exceeded for API '{}': {} - Retry after: {} seconds",
                ex.getApiName(), ex.getMessage(), ex.getRetryAfterSeconds());

        String message = ex.getRetryAfterSeconds() != null
                ? String.format("Rate limit exceeded. Please retry after %d seconds.", ex.getRetryAfterSeconds())
                : "Rate limit exceeded. Please try again later.";

        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ErrorResponse("Rate Limit Exceeded", message));
    }

    /**
     * Handles InsufficientSharesException - returns 400 Bad Request.
     *
     * @param ex the InsufficientSharesException
     * @return ResponseEntity with transaction details
     */
    @ExceptionHandler(InsufficientSharesException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientSharesException(InsufficientSharesException ex) {
        log.warn("Insufficient shares for symbol '{}': Requested {} but only {} available",
                ex.getSymbol(), ex.getRequested(), ex.getAvailable());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Insufficient Shares", ex.getMessage()));
    }

    /**
     * Handles base StockPortfolioException - returns 500 Internal Server Error.
     *
     * @param ex the StockPortfolioException
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(StockPortfolioException.class)
    public ResponseEntity<ErrorResponse> handleStockPortfolioException(StockPortfolioException ex) {
        log.error("Stock portfolio error [{}]: {}", ex.getErrorCode(), ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
    }

    // ========================
    // Spring Validation Handlers
    // ========================

    /**
     * Handles validation errors from @Valid annotations - returns 400 Bad Request.
     *
     * @param ex the MethodArgumentNotValidException
     * @return ResponseEntity with validation error details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("Validation failed: {}", errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Validation Error", errors));
    }

    /**
     * Handles constraint violation exceptions - returns 400 Bad Request.
     *
     * @param ex the ConstraintViolationException
     * @return ResponseEntity with constraint violation details
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        String errors = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));

        log.warn("Constraint violation: {}", errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Validation Error", errors));
    }

    /**
     * Handles IllegalArgumentException - returns 400 Bad Request.
     *
     * @param ex the IllegalArgumentException
     * @return ResponseEntity with error message
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Invalid Argument", ex.getMessage()));
    }

    // ========================
    // HTTP Request Handlers
    // ========================

    /**
     * Handles missing request parameters - returns 400 Bad Request.
     *
     * @param ex the MissingServletRequestParameterException
     * @return ResponseEntity with parameter details
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex) {
        log.warn("Missing request parameter: {}", ex.getParameterName());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Missing Parameter",
                        "Required parameter '" + ex.getParameterName() + "' is missing"));
    }

    /**
     * Handles type mismatch in request parameters - returns 400 Bad Request.
     *
     * @param ex the MethodArgumentTypeMismatchException
     * @return ResponseEntity with type mismatch details
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex) {
        String message = String.format("Parameter '%s' should be of type '%s'",
                ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        log.warn("Type mismatch: {}", message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Invalid Parameter Type", message));
    }

    /**
     * Handles malformed JSON in request body - returns 400 Bad Request.
     *
     * @param ex the HttpMessageNotReadableException
     * @return ResponseEntity with parsing error message
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex) {
        log.warn("Malformed request body: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Invalid Request Body",
                        "Request body is malformed or missing required fields"));
    }

    /**
     * Handles unsupported HTTP methods - returns 405 Method Not Allowed.
     *
     * @param ex the HttpRequestMethodNotSupportedException
     * @return ResponseEntity with supported methods info
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex) {
        log.warn("Method not supported: {} for this endpoint", ex.getMethod());
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new ErrorResponse("Method Not Allowed",
                        "HTTP method '" + ex.getMethod() + "' is not supported for this endpoint"));
    }

    /**
     * Handles unsupported media types - returns 415 Unsupported Media Type.
     *
     * @param ex the HttpMediaTypeNotSupportedException
     * @return ResponseEntity with media type info
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException ex) {
        log.warn("Unsupported media type: {}", ex.getContentType());
        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(new ErrorResponse("Unsupported Media Type",
                        "Content type '" + ex.getContentType() + "' is not supported"));
    }

    /**
     * Handles 404 Not Found for non-existent endpoints.
     *
     * @param ex the NoHandlerFoundException
     * @return ResponseEntity with endpoint info
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        log.warn("No handler found: {} {}", ex.getHttpMethod(), ex.getRequestURL());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("Endpoint Not Found",
                        "No endpoint found for " + ex.getHttpMethod() + " " + ex.getRequestURL()));
    }

    // ========================
    // Database Exception Handlers
    // ========================

    /**
     * Handles SQL exceptions - returns 500 Internal Server Error.
     * Logs full details but returns sanitized message.
     *
     * @param ex the SQLException
     * @return ResponseEntity with sanitized error message
     */
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ErrorResponse> handleSQLException(SQLException ex) {
        log.error("SQL Exception - State: {}, Code: {}, Message: {}",
                ex.getSQLState(), ex.getErrorCode(), ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Database Error",
                        "A database error occurred. Please try again later."));
    }

    /**
     * Handles Spring Data access exceptions - returns 500 Internal Server Error.
     *
     * @param ex the DataAccessException
     * @return ResponseEntity with sanitized error message
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException ex) {
        log.error("Data access exception: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Database Error",
                        "Unable to access database. Please try again later."));
    }

    /**
     * Handles data integrity violations (e.g., unique constraint) - returns 409 Conflict.
     *
     * @param ex the DataIntegrityViolationException
     * @return ResponseEntity with conflict message
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex) {
        log.error("Data integrity violation: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("Data Conflict",
                        "The operation conflicts with existing data. Please verify your input."));
    }

    // ========================
    // Network/IO Exception Handlers
    // ========================

    /**
     * Handles IO exceptions - returns 500 Internal Server Error.
     *
     * @param ex the IOException
     * @return ResponseEntity with error message
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> handleIOException(IOException ex) {
        log.error("IO Exception occurred: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("IO Error",
                        "An input/output error occurred. Please try again later."));
    }

    /**
     * Handles socket timeout exceptions - returns 504 Gateway Timeout.
     *
     * @param ex the SocketTimeoutException
     * @return ResponseEntity with timeout message
     */
    @ExceptionHandler(SocketTimeoutException.class)
    public ResponseEntity<ErrorResponse> handleSocketTimeoutException(SocketTimeoutException ex) {
        log.error("Socket timeout: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.GATEWAY_TIMEOUT)
                .body(new ErrorResponse("Request Timeout",
                        "The request timed out. Please try again later."));
    }

    /**
     * Handles REST client connection issues - returns 503 Service Unavailable.
     *
     * @param ex the ResourceAccessException
     * @return ResponseEntity with connection error message
     */
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErrorResponse> handleResourceAccessException(ResourceAccessException ex) {
        log.error("Resource access exception (connection issue): {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse("Service Unavailable",
                        "Unable to connect to external service. Please try again later."));
    }

    /**
     * Handles general REST client exceptions - returns 502 Bad Gateway.
     *
     * @param ex the RestClientException
     * @return ResponseEntity with external service error message
     */
    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ErrorResponse> handleRestClientException(RestClientException ex) {
        log.error("REST client exception: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResponse("External Service Error",
                        "Error communicating with external service. Please try again later."));
    }

    // ========================
    // Common Exception Handlers
    // ========================

    /**
     * Handles NullPointerException - returns 500 Internal Server Error.
     *
     * @param ex the NullPointerException
     * @return ResponseEntity with error message
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorResponse> handleNullPointerException(NullPointerException ex) {
        log.error("NullPointerException occurred at: {}",
                ex.getStackTrace().length > 0 ? ex.getStackTrace()[0] : "unknown", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Error",
                        "An unexpected error occurred. Please try again later."));
    }

    // ========================
    // Catch-All Handler
    // ========================

    /**
     * Handles all other unhandled exceptions - returns 500 Internal Server Error.
     * Acts as a safety net for any exceptions not caught by more specific handlers.
     *
     * @param ex the Exception
     * @return ResponseEntity with generic error message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unhandled exception of type {}: {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error",
                        "An unexpected error occurred. Please try again later."));
    }
}
