package org.apache.cloudstack.framework.client;

import org.apache.cloudstack.framework.transport.TransportEndpoint;
import org.apache.cloudstack.framework.transport.TransportEndpointSite;
import org.apache.cloudstack.framework.transport.TransportProvider;

public class ClientTransportEndpointSite extends TransportEndpointSite {
    private final String _predefinedAddress;
    private int _providerKey;

    public ClientTransportEndpointSite(final TransportProvider provider, final TransportEndpoint endpoint, final String predefinedAddress, final int providerKey) {
        super(provider, endpoint);

        _predefinedAddress = predefinedAddress;
        _providerKey = providerKey;
    }

    public String getPredefinedAddress() {
        return _predefinedAddress;
    }

    public int getProviderKey() {
        return _providerKey;
    }

    public void setProviderKey(final int providerKey) {
        _providerKey = providerKey;
    }
}
