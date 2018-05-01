package com.cloud.exception;

import com.cloud.utils.exception.CloudRuntimeException;

public class AccountLimitException extends CloudRuntimeException {

    public AccountLimitException(final String msg) {
        super(msg);
    }
}
