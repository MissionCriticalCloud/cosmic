package org.apache.cloudstack.framework.transport;

import org.apache.cloudstack.framework.serializer.OnwireName;

@OnwireName(name = "TransportConnectRequestPdu")
public class TransportConnectRequestPdu extends TransportPdu {
    String _authIdentity;
    String _authCredential;

    public TransportConnectRequestPdu() {
    }

    public String getAuthIdentity() {
        return _authIdentity;
    }

    public void setAuthIdentity(final String authIdentity) {
        _authIdentity = authIdentity;
    }

    public String getAuthCredential() {
        return _authCredential;
    }

    public void setAuthCredential(final String authCredential) {
        _authCredential = authCredential;
    }
}
