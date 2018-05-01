package com.cloud.resource;

import com.cloud.legacymodel.exceptions.CloudException;

public class UnableDeleteHostException extends CloudException {
    public UnableDeleteHostException(final String msg) {
        super(msg);
    }
}
