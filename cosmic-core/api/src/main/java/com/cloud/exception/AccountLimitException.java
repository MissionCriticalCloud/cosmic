package com.cloud.exception;

import com.cloud.utils.SerialVersionUID;
import com.cloud.utils.exception.CloudRuntimeException;

public class AccountLimitException extends CloudRuntimeException {

    private static final long serialVersionUID = SerialVersionUID.AccountLimitException;

    protected AccountLimitException() {
        super();
    }

    public AccountLimitException(final String msg) {
        super(msg);
    }

    public AccountLimitException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    // TODO: Add the actual thing that causes the exception. Is it ip address, vm, etc?
}
