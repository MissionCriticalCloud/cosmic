/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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
