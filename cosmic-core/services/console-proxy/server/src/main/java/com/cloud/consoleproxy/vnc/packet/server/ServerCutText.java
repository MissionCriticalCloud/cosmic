package com.cloud.consoleproxy.vnc.packet.server;

import com.cloud.consoleproxy.util.Logger;
import com.cloud.consoleproxy.vnc.RfbConstants;

import java.io.DataInputStream;
import java.io.IOException;

public class ServerCutText {
    private static final Logger s_logger = Logger.getLogger(ServerCutText.class);

    private String content;

    public ServerCutText(final DataInputStream is) throws IOException {
        readPacketData(is);
    }

    private void readPacketData(final DataInputStream is) throws IOException {
        is.skipBytes(3);// Skip padding
        final int length = is.readInt();
        final byte[] buf = new byte[length];
        is.readFully(buf);

        content = new String(buf, RfbConstants.CHARSET);

        /* LOG */
        s_logger.info("Clippboard content: " + content);
    }

    public String getContent() {
        return content;
    }
}
