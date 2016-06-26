package org.apache.cloudstack.framework.transport;

import org.apache.cloudstack.framework.serializer.OnwireName;

@OnwireName(name = "TransportDataPdu")
public class TransportDataPdu extends TransportPdu {

    private String _multiplexier;
    private String _content;

    public TransportDataPdu() {
    }

    public String getMultiplexier() {
        return _multiplexier;
    }

    public void setMultiplexier(final String multiplexier) {
        _multiplexier = multiplexier;
    }

    public String getContent() {
        return _content;
    }

    public void setContent(final String content) {
        _content = content;
    }
}
