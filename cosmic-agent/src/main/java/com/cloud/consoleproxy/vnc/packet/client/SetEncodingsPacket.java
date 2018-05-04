package com.cloud.consoleproxy.vnc.packet.client;

import com.cloud.consoleproxy.vnc.RfbConstants;

import java.io.DataOutputStream;
import java.io.IOException;

public class SetEncodingsPacket implements ClientPacket {

    private final int[] encodings;

    public SetEncodingsPacket(final int[] encodings) {
        this.encodings = encodings;
    }

    @Override
    public void write(final DataOutputStream os) throws IOException {
        os.writeByte(RfbConstants.CLIENT_SET_ENCODINGS);

        os.writeByte(0);// padding

        os.writeShort(encodings.length);

        for (int i = 0; i < encodings.length; i++) {
            os.writeInt(encodings[i]);
        }
    }
}
