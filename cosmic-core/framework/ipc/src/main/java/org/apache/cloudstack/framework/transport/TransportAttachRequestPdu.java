package org.apache.cloudstack.framework.transport;

public class TransportAttachRequestPdu extends TransportPdu {
    private int _endpointProviderKey;

    public TransportAttachRequestPdu() {
    }

    public int getEndpointProviderKey() {
        return _endpointProviderKey;
    }

    public void setEndpointProviderKey(final int endpointProviderKey) {
        _endpointProviderKey = endpointProviderKey;
    }
}
