package com.cloud.legacymodel.exceptions;

public class AccountLimitException extends CloudRuntimeException {

    public AccountLimitException(final String msg) {
        super(msg);
    }
}
