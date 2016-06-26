package org.apache.cloudstack.framework.transport;

import org.apache.cloudstack.framework.serializer.MessageSerializer;

public interface TransportProvider {
    MessageSerializer getMessageSerializer();

    void setMessageSerializer(MessageSerializer messageSerializer);

    TransportEndpointSite attach(TransportEndpoint endpoint, String predefinedAddress);

    boolean detach(TransportEndpoint endpoint);

    void requestSiteOutput(TransportEndpointSite site);

    void sendMessage(String soureEndpointAddress, String targetEndpointAddress, String multiplexier, String message);
}
