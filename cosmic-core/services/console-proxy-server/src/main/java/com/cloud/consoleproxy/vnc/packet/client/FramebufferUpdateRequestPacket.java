package com.cloud.consoleproxy.vnc.packet.client;

import com.cloud.consoleproxy.vnc.RfbConstants;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * FramebufferUpdateRequestPacket
 *
 * @author Volodymyr M. Lisivka
 */
public class FramebufferUpdateRequestPacket implements ClientPacket {

    private final int incremental;
    private final int x, y, width, height;

    public FramebufferUpdateRequestPacket(final int incremental, final int x, final int y, final int width, final int height) {
        this.incremental = incremental;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void write(final DataOutputStream os) throws IOException {
        os.writeByte(RfbConstants.CLIENT_FRAMEBUFFER_UPDATE_REQUEST);

        os.writeByte(incremental);
        os.writeShort(x);
        os.writeShort(y);
        os.writeShort(width);
        os.writeShort(height);
    }
}
