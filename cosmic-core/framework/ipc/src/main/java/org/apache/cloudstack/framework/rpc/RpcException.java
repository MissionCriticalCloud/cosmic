package org.apache.cloudstack.framework.rpc;

public class RpcException extends RuntimeException {
    private static final long serialVersionUID = -3164514701087423787L;

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
