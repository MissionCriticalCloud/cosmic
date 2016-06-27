package org.apache.cloudstack.framework.transport;

public class TransportAttachResponsePdu extends TransportPdu {
    private int _statusCode;
    private int _endpointProviderKey;

    public TransportAttachResponsePdu() {
    }

    public int getStatusCode() {
        return _statusCode;
    }

    public void setStatusCode(final int statusCode) {
        _statusCode = statusCode;
    }

    public int getEndpointProviderKey() {
        return _endpointProviderKey;
    }

    public void setEndpointProviderKey(final int endpointProviderKey) {
        _endpointProviderKey = endpointProviderKey;
    }
}
