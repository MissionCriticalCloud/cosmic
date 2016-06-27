package com.cloud.exception;

public class ResourceInUseException extends ManagementServerException {

    private static final long serialVersionUID = 1383416910411639324L;
    private String resourceType;
    private String resourceName;

    public ResourceInUseException(final String message) {
        super(message);
    }

    public ResourceInUseException(final String message, final String resourceType, final String resourceName) {
        super(message);
        this.resourceType = resourceType;
        this.resourceName = resourceName;
    }

    public String getResourceType() {
        return this.resourceType;
    }

    public void setResourceType(final String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(final String resourceName) {
        this.resourceName = resourceName;
    }
}
