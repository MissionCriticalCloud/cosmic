package com.cloud.exception;

public class NetworkRuleConflictException extends ManagementServerException {

    private static final long serialVersionUID = -294905017911859479L;

    public NetworkRuleConflictException(final String message) {
        super(message);
    }
}
