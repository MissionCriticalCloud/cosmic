package com.cloud.exception;

import com.cloud.configuration.Resource.ResourceType;

public class ResourceAllocationException extends ManagementServerException {
    private final ResourceType resourceType;

    public ResourceAllocationException(final String message, final ResourceType resourceType) {
        super(message);
        this.resourceType = resourceType;
    }

    public ResourceType getResourceType() {
        return this.resourceType;
    }
}
