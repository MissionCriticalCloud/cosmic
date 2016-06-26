package org.apache.cloudstack.framework.client;

import org.apache.cloudstack.framework.transport.TransportEndpoint;

public class ClientTransportEndpoint implements TransportEndpoint {

    @Override
    public void onAttachConfirm(final boolean bSuccess, final String endpointAddress) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onDetachIndication(final String endpointAddress) {
    }

    @Override
    public void onTransportMessage(final String senderEndpointAddress, final String targetEndpointAddress, final String multiplexer, final String message) {
        // TODO Auto-generated method stub

    }
}
