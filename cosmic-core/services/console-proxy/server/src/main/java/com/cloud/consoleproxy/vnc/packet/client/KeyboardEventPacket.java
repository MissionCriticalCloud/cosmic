package com.cloud.consoleproxy.vnc.packet.client;

import com.cloud.consoleproxy.vnc.RfbConstants;

import java.io.DataOutputStream;
import java.io.IOException;

public class KeyboardEventPacket implements ClientPacket {

    private final int downFlag, key;

    public KeyboardEventPacket(final int downFlag, final int key) {
        this.downFlag = downFlag;
        this.key = key;
    }

    @Override
    public void write(final DataOutputStream os) throws IOException {
        os.writeByte(RfbConstants.CLIENT_KEYBOARD_EVENT);

        os.writeByte(downFlag);
        os.writeShort(0); // padding
        os.writeInt(key);
    }
}
