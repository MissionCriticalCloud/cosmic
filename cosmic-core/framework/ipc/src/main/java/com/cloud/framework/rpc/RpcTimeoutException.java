package com.cloud.framework.rpc;

public class RpcTimeoutException extends RpcException {
    public RpcTimeoutException() {
        super();
    }

    public RpcTimeoutException(final String message) {
        super(message);
    }
}
