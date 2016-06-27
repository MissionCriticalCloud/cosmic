package org.apache.cloudstack.framework.server;

import org.apache.cloudstack.framework.messagebus.MessageBusBase;
import org.apache.cloudstack.framework.transport.TransportMultiplexier;

public class ServerMessageBus extends MessageBusBase implements TransportMultiplexier {

    @Override
    public void onTransportMessage(final String senderEndpointAddress, final String targetEndpointAddress, final String multiplexer, final String message) {
        // TODO Auto-generated method stub
    }
}
