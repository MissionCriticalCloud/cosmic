package com.cloud.resource;

import com.cloud.exception.CloudException;
import com.cloud.utils.SerialVersionUID;

public class UnableDeleteHostException extends CloudException {
    private static final long serialVersionUID = SerialVersionUID.UnableDeleteHostException;

    public UnableDeleteHostException(final String msg) {
        super(msg);
    }
}
