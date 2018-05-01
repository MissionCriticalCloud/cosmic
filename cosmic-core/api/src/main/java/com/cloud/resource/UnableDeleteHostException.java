package com.cloud.resource;

import com.cloud.exception.CloudException;

public class UnableDeleteHostException extends CloudException {
    public UnableDeleteHostException(final String msg) {
        super(msg);
    }
}
