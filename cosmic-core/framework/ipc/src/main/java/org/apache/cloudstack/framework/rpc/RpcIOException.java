package org.apache.cloudstack.framework.rpc;

public class RpcIOException extends RpcException {

    private static final long serialVersionUID = -6108039302920641533L;

    public RpcIOException() {
        super();
    }

    public RpcIOException(final String message) {
        super(message);
    }

    public RpcIOException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
