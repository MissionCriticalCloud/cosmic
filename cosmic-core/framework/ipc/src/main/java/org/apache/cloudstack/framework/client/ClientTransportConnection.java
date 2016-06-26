package org.apache.cloudstack.framework.client;

import org.apache.cloudstack.framework.transport.TransportAddress;
import org.apache.cloudstack.framework.transport.TransportAttachResponsePdu;
import org.apache.cloudstack.framework.transport.TransportConnectResponsePdu;
import org.apache.cloudstack.framework.transport.TransportPdu;

import java.util.ArrayList;
import java.util.List;

public class ClientTransportConnection {
    private final ClientTransportProvider _provider;
    // TODO, use state machine
    private State _state = State.Idle;
    private TransportAddress _connectionTpAddress;
    private final List<TransportPdu> _outputQueue = new ArrayList<>();

    public ClientTransportConnection(final ClientTransportProvider provider) {
        _provider = provider;
    }

    public void connect(final String serverAddress, final int serverPort) {
        boolean doConnect = false;
        synchronized (this) {
            if (_state == State.Idle) {
                setState(State.Connecting);
                doConnect = true;
            }
        }

        if (doConnect) {
            // ???
        }
    }

    private void setState(final State state) {
        synchronized (this) {
            if (_state != state) {
                _state = state;
            }
        }
    }

    public void handleConnectResponsePdu(final TransportConnectResponsePdu pdu) {
        // TODO assume it is always succeeds
        _connectionTpAddress = TransportAddress.fromAddressString(pdu.getDestAddress());

        // ???
    }

    public void handleAttachResponsePdu(final TransportAttachResponsePdu pdu) {
        // ???
    }

    enum State {
        Idle, Connecting, Open, Closing
    }
}
