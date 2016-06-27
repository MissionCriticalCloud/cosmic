package org.apache.cloudstack.framework.transport;

public interface TransportMultiplexier {
    public void onTransportMessage(String senderEndpointAddress, String targetEndpointAddress, String multiplexer, String message);
}
