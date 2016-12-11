package com.cloud.framework.transport;

public interface TransportEndpoint extends TransportMultiplexier {
    void onAttachConfirm(boolean bSuccess, String endpointAddress);

    void onDetachIndication(String endpointAddress);
}
