package com.cloud.agent.resource.consoleproxy.vnc.packet.client;

import java.io.DataOutputStream;
import java.io.IOException;

public interface ClientPacket {

    void write(DataOutputStream os) throws IOException;
}
