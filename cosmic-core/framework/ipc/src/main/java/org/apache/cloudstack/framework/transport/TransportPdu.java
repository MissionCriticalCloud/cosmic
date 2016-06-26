package org.apache.cloudstack.framework.transport;

public class TransportPdu {
    protected String _sourceAddress;
    protected String _destAddress;

    public TransportPdu() {
    }

    public String getSourceAddress() {
        return _sourceAddress;
    }

    public void setSourceAddress(final String sourceAddress) {
        _sourceAddress = sourceAddress;
    }

    public String getDestAddress() {
        return _destAddress;
    }

    public void setDestAddress(final String destAddress) {
        _destAddress = destAddress;
    }
}
