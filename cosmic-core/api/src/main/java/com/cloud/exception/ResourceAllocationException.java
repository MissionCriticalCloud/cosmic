package com.cloud.exception;

import com.cloud.configuration.Resource.ResourceType;

public class ResourceAllocationException extends ManagementServerException {

    private static final long serialVersionUID = -2232066904895010203L;
    private final ResourceType resourceType;

    public ResourceAllocationException(final String message, final ResourceType resourceType) {
        super(message);
        this.resourceType = resourceType;
    }

    public ResourceType getResourceType() {
        return this.resourceType;
    }
}
