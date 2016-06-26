package org.apache.cloudstack.framework.transport;

public interface TransportEndpoint extends TransportMultiplexier {
    void onAttachConfirm(boolean bSuccess, String endpointAddress);

    void onDetachIndication(String endpointAddress);
}
