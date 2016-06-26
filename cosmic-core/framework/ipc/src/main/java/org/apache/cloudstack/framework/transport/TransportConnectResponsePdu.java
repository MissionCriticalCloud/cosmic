package org.apache.cloudstack.framework.transport;

import org.apache.cloudstack.framework.serializer.OnwireName;

@OnwireName(name = "TransportConnectRequestPdu")
public class TransportConnectResponsePdu extends TransportPdu {
    private int _statusCode;

    public TransportConnectResponsePdu() {
    }

    public int getStatusCode() {
        return _statusCode;
    }

    public void setStatusCode(final int statusCode) {
        _statusCode = statusCode;
    }
}
