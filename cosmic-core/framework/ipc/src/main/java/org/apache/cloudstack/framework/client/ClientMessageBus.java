package org.apache.cloudstack.framework.client;

import org.apache.cloudstack.framework.messagebus.MessageBusBase;
import org.apache.cloudstack.framework.transport.TransportMultiplexier;

public class ClientMessageBus extends MessageBusBase implements TransportMultiplexier {

    @Override
    public void onTransportMessage(final String senderEndpointAddress, final String targetEndpointAddress, final String multiplexer, final String message) {
        // TODO Auto-generated method stub
    }
}
