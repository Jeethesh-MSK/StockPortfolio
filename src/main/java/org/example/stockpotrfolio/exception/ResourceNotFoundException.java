package org.example.stockpotrfolio.exception;

/**
 * Exception thrown when a requested resource is not found.
 * Maps to HTTP 404 Not Found status code.
 */
public class ResourceNotFoundException extends StockPortfolioException {

    private final String resourceType;
    private final String resourceId;

    /**
     * Constructs a new ResourceNotFoundException with the specified message.
     *
     * @param message the error message
     */
    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND");
        this.resourceType = null;
        this.resourceId = null;
    }

    /**
     * Constructs a new ResourceNotFoundException with resource details.
     *
     * @param resourceType the type of resource (e.g., "PortfolioItem", "Stock")
     * @param resourceId   the identifier of the resource that was not found
     */
    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(resourceType + " not found: " + resourceId, "RESOURCE_NOT_FOUND");
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    /**
     * Constructs a new ResourceNotFoundException with message and cause.
     *
     * @param message the error message
     * @param cause   the cause of the exception
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, "RESOURCE_NOT_FOUND", cause);
        this.resourceType = null;
        this.resourceId = null;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }
}
