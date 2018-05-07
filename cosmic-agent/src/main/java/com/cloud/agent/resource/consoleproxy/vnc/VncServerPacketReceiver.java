package com.cloud.consoleproxy.vnc;

import com.cloud.agent.resource.consoleproxy.ConsoleProxyClientListener;
import com.cloud.consoleproxy.vnc.packet.server.FramebufferUpdatePacket;
import com.cloud.consoleproxy.vnc.packet.server.ServerCutText;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.DataInputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VncServerPacketReceiver implements Runnable {
    private static final Logger s_logger = LoggerFactory.getLogger(VncServerPacketReceiver.class);

    private final VncScreenDescription screen;
    private final FrameBufferUpdateListener fburListener;
    private final ConsoleProxyClientListener clientListener;
    private final BufferedImageCanvas canvas;
    private final DataInputStream is;
    private boolean connectionAlive = true;
    private final VncClient vncConnection;

    public VncServerPacketReceiver(final DataInputStream is, final BufferedImageCanvas canvas, final VncScreenDescription screen, final VncClient vncConnection,
                                   final FrameBufferUpdateListener fburListener, final ConsoleProxyClientListener clientListener) {
        this.screen = screen;
        this.canvas = canvas;
        this.is = is;
        this.vncConnection = vncConnection;
        this.fburListener = fburListener;
        this.clientListener = clientListener;
    }

    public BufferedImageCanvas getCanvas() {
        return this.canvas;
    }

    @Override
    public void run() {
        try {
            while (this.connectionAlive) {

                // Read server message type
                final int messageType = this.is.readUnsignedByte();

                // Invoke packet handler by packet type.
                switch (messageType) {

                    case RfbConstants.SERVER_FRAMEBUFFER_UPDATE: {
                        // Notify sender that frame buffer update is received,
                        // so it can send another frame buffer update request
                        this.fburListener.frameBufferPacketReceived();
                        // Handle frame buffer update
                        new FramebufferUpdatePacket(this.canvas, this.screen, this.is, this.clientListener);
                        break;
                    }

                    case RfbConstants.SERVER_BELL: {
                        serverBell();
                        break;
                    }

                    case RfbConstants.SERVER_CUT_TEXT: {
                        serverCutText(this.is);
                        break;
                    }

                    default:
                        throw new RuntimeException("Unknown server packet type: " + messageType + ".");
                }
            }
        } catch (final IOException e) {
            s_logger.error("Unexpected exception: ", e);
            if (this.connectionAlive) {
                closeConnection();
            }
        } finally {
            s_logger.info("Receiving thread exit processing, shutdown connection");
            this.vncConnection.shutdown();
        }
    }

    /**
     * Handle server bell packet.
     */
    private void serverBell() {
        Toolkit.getDefaultToolkit().beep();
    }

    /**
     * Handle packet with server clip-board.
     */
    private void serverCutText(final DataInputStream is) throws IOException {
        final ServerCutText clipboardContent = new ServerCutText(is);
        final StringSelection contents = new StringSelection(clipboardContent.getContent());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(contents, null);

        s_logger.info("Server clipboard buffer: " + clipboardContent.getContent());
    }

    public void closeConnection() {
        this.connectionAlive = false;
    }

    public boolean isConnectionAlive() {
        return this.connectionAlive;
    }
}
