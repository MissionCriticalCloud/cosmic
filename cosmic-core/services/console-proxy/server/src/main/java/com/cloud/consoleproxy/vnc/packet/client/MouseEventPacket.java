package com.cloud.consoleproxy.vnc.packet.client;

import com.cloud.consoleproxy.vnc.RfbConstants;

import java.io.DataOutputStream;
import java.io.IOException;

public class MouseEventPacket implements ClientPacket {

    private final int buttonMask, x, y;

    public MouseEventPacket(final int buttonMask, final int x, final int y) {
        this.buttonMask = buttonMask;
        this.x = x;
        this.y = y;
    }

    @Override
    public void write(final DataOutputStream os) throws IOException {
        os.writeByte(RfbConstants.CLIENT_POINTER_EVENT);

        os.writeByte(buttonMask);
        os.writeShort(x);
        os.writeShort(y);
    }
}
