//
// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
//

package com.cloud.utils.cisco.n1kv.vsm;

import com.cloud.utils.Pair;
import com.cloud.utils.cisco.n1kv.vsm.VsmCommand.BindingType;
import com.cloud.utils.cisco.n1kv.vsm.VsmCommand.PortProfileType;
import com.cloud.utils.cisco.n1kv.vsm.VsmCommand.SwitchPortMode;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.ssh.SSHCmdHelper;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetconfHelper {
    private static final Logger s_logger = LoggerFactory.getLogger(NetconfHelper.class);

    private static final String SSH_NETCONF_TERMINATOR = "]]>]]>";

    private final Connection _connection;

    private Session _session;

    public NetconfHelper(String ip, String username, String password) throws CloudRuntimeException {
        _connection = SSHCmdHelper.acquireAuthorizedConnection(ip, username, password);
        if (_connection == null) {
            throw new CloudRuntimeException("Error opening ssh connection.");
        }

        try {
            _session = _connection.openSession();
            _session.startSubSystem("xmlagent");
            exchangeHello();
        } catch (final Exception e) {
            disconnect();
            s_logger.error("Failed to connect to device SSH server: " + e.getMessage());
            throw new CloudRuntimeException("Failed to connect to SSH server: " + _connection.getHostname());
        }
    }

    private void exchangeHello() {
        receive();
        final String hello = VsmCommand.getHello() + SSH_NETCONF_TERMINATOR;
        send(hello);
    }

    public void disconnect() {
        if (_session != null) {
            _session.close();
        }
        SSHCmdHelper.releaseSshConnection(_connection);
    }

    private String receive() {
        String response = new String("");
        final InputStream inputStream = _session.getStdout();

        try {
            final Delimiter delimiter = new Delimiter();
            final byte[] buffer = new byte[1024];
            int count = 0;

            // Read the input stream till we find the end sequence ']]>]]>'.
            while (true) {
                final int data = inputStream.read();
                if (data != -1) {
                    final byte[] dataStream = delimiter.parse(data);
                    if (delimiter.endReached()) {
                        response += new String(buffer, 0, count);
                        break;
                    }

                    if (dataStream != null) {
                        for (final byte element : dataStream) {
                            buffer[count] = element;
                            count++;
                            if (count == 1024) {
                                response += new String(buffer, 0, count);
                                count = 0;
                            }
                        }
                    }
                } else {
                    break;
                }
            }
        } catch (final Exception e) {
            throw new CloudRuntimeException("Error occured while reading from the stream: " + e.getMessage());
        }

        return response;
    }

    private void send(String message) {
        try {
            final OutputStream outputStream = _session.getStdin();
            outputStream.write(message.getBytes());
            outputStream.flush();
        } catch (final Exception e) {
            s_logger.error("Failed to send message: " + e.getMessage());
            throw new CloudRuntimeException("Failed to send message: " + e.getMessage());
        }
    }

    public void queryStatus() throws CloudRuntimeException {
        // This command is used to query the server status.
        final String status =
                "<?xml version=\"1.0\"?>" + "<nc:rpc message-id=\"1\" xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0" + "\"xmlns=\"http://www.cisco.com/nxos:1.0:xml\">" +
                        "  <nc:get>" + "    <nc:filter type=\"subtree\">" + "      <show>" + "        <xml>" + "          <server>" + "            <status/>" +
                        "          </server>" + "        </xml>" + "      </show>" + "    </nc:filter>" + "  </nc:get>" + "</nc:rpc>" + SSH_NETCONF_TERMINATOR;
        send(status);
        // parse the rpc reply.
        parseOkReply(receive());
    }

    private void parseOkReply(String reply) throws CloudRuntimeException {
        final VsmOkResponse response = new VsmOkResponse(reply.trim());
        if (!response.isResponseOk()) {
            throw new CloudRuntimeException(response.toString());
        }
    }

    public void addPortProfile(String name, PortProfileType type, BindingType binding, SwitchPortMode mode, int vlanid, String vdc, String espName)
            throws CloudRuntimeException {
        String command = VsmCommand.getAddPortProfile(name, type, binding, mode, vlanid, vdc, espName);
        if (command != null) {
            command = command.concat(SSH_NETCONF_TERMINATOR);
            parseOkReply(sendAndReceive(command));
        } else {
            throw new CloudRuntimeException("Error generating rpc request for adding port profile.");
        }
    }

    private String sendAndReceive(String command) {
        String received;
        synchronized (NetconfHelper.class) {
            send(command);
            received = receive();
        }
        return received;
    }

    public void addPortProfile(String name, PortProfileType type, BindingType binding, SwitchPortMode mode, int vlanid) throws CloudRuntimeException {
        String command = VsmCommand.getAddPortProfile(name, type, binding, mode, vlanid);
        if (command != null) {
            command = command.concat(SSH_NETCONF_TERMINATOR);
            parseOkReply(sendAndReceive(command));
        } else {
            throw new CloudRuntimeException("Error generating rpc request for adding port profile.");
        }
    }

    public void updatePortProfile(String name, SwitchPortMode mode, List<Pair<VsmCommand.OperationType, String>> params) throws CloudRuntimeException {
        String command = VsmCommand.getUpdatePortProfile(name, mode, params);
        if (command != null) {
            command = command.concat(SSH_NETCONF_TERMINATOR);
            parseOkReply(sendAndReceive(command));
        } else {
            throw new CloudRuntimeException("Error generating rpc request for updating port profile.");
        }
    }

    public void deletePortProfile(String name) throws CloudRuntimeException {
        String command = VsmCommand.getDeletePortProfile(name);
        if (command != null) {
            command = command.concat(SSH_NETCONF_TERMINATOR);
            parseOkReply(sendAndReceive(command));
        } else {
            throw new CloudRuntimeException("Error generating rpc request for deleting port profile.");
        }
    }

    public void deletePolicyMap(String name) throws CloudRuntimeException {
        String command = VsmCommand.getDeletePolicyMap(name);
        if (command != null) {
            command = command.concat(SSH_NETCONF_TERMINATOR);
            parseOkReply(sendAndReceive(command));
        } else {
            throw new CloudRuntimeException("Error generating rpc request for deleting policy map.");
        }
    }

    public void updatePolicyMap(String name, int averageRate, int maxRate, int burstRate) throws CloudRuntimeException {
        // Add and update of policy map work in the exact same way.
        addPolicyMap(name, averageRate, maxRate, burstRate);
    }

    public void addPolicyMap(String name, int averageRate, int maxRate, int burstRate) throws CloudRuntimeException {
        String command = VsmCommand.getAddPolicyMap(name, averageRate, maxRate, burstRate);
        if (command != null) {
            command = command.concat(SSH_NETCONF_TERMINATOR);
            parseOkReply(sendAndReceive(command));
        } else {
            throw new CloudRuntimeException("Error generating rpc request for adding/updating policy map.");
        }
    }

    public void attachServicePolicy(String policyMap, String portProfile) throws CloudRuntimeException {
        String command = VsmCommand.getServicePolicy(policyMap, portProfile, true);
        if (command != null) {
            command = command.concat(SSH_NETCONF_TERMINATOR);
            parseOkReply(sendAndReceive(command));
        } else {
            throw new CloudRuntimeException("Error generating rpc request for adding policy map.");
        }
    }

    public void detachServicePolicy(String policyMap, String portProfile) throws CloudRuntimeException {
        String command = VsmCommand.getServicePolicy(policyMap, portProfile, false);
        if (command != null) {
            command = command.concat(SSH_NETCONF_TERMINATOR);
            parseOkReply(sendAndReceive(command));
        } else {
            throw new CloudRuntimeException("Error generating rpc request for removing policy map.");
        }
    }

    public void addVServiceNode(String vlanId, String ipAddr) throws CloudRuntimeException {
        String command = VsmCommand.getVServiceNode(vlanId, ipAddr);
        if (command != null) {
            command = command.concat(SSH_NETCONF_TERMINATOR);
            parseOkReply(sendAndReceive(command));
        } else {
            throw new CloudRuntimeException("Error generating rpc request for adding vservice node for vlan " + vlanId);
        }
    }

    public PolicyMap getPolicyMapByName(String name) throws CloudRuntimeException {
        String command = VsmCommand.getPolicyMap(name);
        if (command != null) {
            command = command.concat(SSH_NETCONF_TERMINATOR);
            final String received = sendAndReceive(command);
            final VsmPolicyMapResponse response = new VsmPolicyMapResponse(received.trim());
            if (!response.isResponseOk()) {
                throw new CloudRuntimeException(response.toString());
            } else {
                return response.getPolicyMap();
            }
        } else {
            throw new CloudRuntimeException("Error generating rpc request for getting policy map.");
        }
    }

    private static class Delimiter {
        // Used to accumulate response read while searching for end of response.
        private final byte[] _gatherResponse = new byte[6];
        private boolean _endReached = false;
        // Index into number of bytes read.
        private int _offset = 0;

        // True if ']]>]]>' detected.
        boolean endReached() {
            return _endReached;
        }

        // Parses the input stream and checks if end sequence is reached.
        byte[] parse(int input) throws RuntimeException {
            boolean collect = false;
            byte[] streamRead = null;

            // Check if end sequence matched.
            switch (_offset) {
                case 0:
                    if (input == ']') {
                        collect = true;
                    }
                    break;
                case 1:
                    if (input == ']') {
                        collect = true;
                    }
                    break;
                case 2:
                    if (input == '>') {
                        collect = true;
                    }
                    break;
                case 3:
                    if (input == ']') {
                        collect = true;
                    }
                    break;
                case 4:
                    if (input == ']') {
                        collect = true;
                    }
                    break;
                case 5:
                    if (input == '>') {
                        collect = true;
                        _endReached = true;
                    }
                    break;
                default:
                    throw new RuntimeException("Invalid index value: " + _offset);
            }

            if (collect) {
                _gatherResponse[_offset++] = (byte) input;
            } else {
                // End sequence not yet reached. Return the stream of bytes collected so far.
                streamRead = new byte[_offset + 1];
                for (int index = 0; index < _offset; ++index) {
                    streamRead[index] = _gatherResponse[index];
                }

                streamRead[_offset] = (byte) input;
                _offset = 0;
            }

            return streamRead;
        }
    }
}
