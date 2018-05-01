package com.cloud.framework.rpc;

public class RpcException extends RuntimeException {
    public RpcException() {
        super();
    }

    public RpcException(final String message) {
        super(message);
    }

    public RpcException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
