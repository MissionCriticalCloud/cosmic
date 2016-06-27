package com.cloud.consoleproxy.vnc;

import com.cloud.consoleproxy.ConsoleProxyClientListener;
import com.cloud.consoleproxy.util.Logger;
import com.cloud.consoleproxy.vnc.packet.server.FramebufferUpdatePacket;
import com.cloud.consoleproxy.vnc.packet.server.ServerCutText;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.DataInputStream;
import java.io.IOException;

public class VncServerPacketReceiver implements Runnable {
    private static final Logger s_logger = Logger.getLogger(VncServerPacketReceiver.class);

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
        return canvas;
    }

    @Override
    public void run() {
        try {
            while (connectionAlive) {

                // Read server message type
                final int messageType = is.readUnsignedByte();

                // Invoke packet handler by packet type.
                switch (messageType) {

                    case RfbConstants.SERVER_FRAMEBUFFER_UPDATE: {
                        // Notify sender that frame buffer update is received,
                        // so it can send another frame buffer update request
                        fburListener.frameBufferPacketReceived();
                        // Handle frame buffer update
                        new FramebufferUpdatePacket(canvas, screen, is, clientListener);
                        break;
                    }

                    case RfbConstants.SERVER_BELL: {
                        serverBell();
                        break;
                    }

                    case RfbConstants.SERVER_CUT_TEXT: {
                        serverCutText(is);
                        break;
                    }

                    default:
                        throw new RuntimeException("Unknown server packet type: " + messageType + ".");
                }
            }
        } catch (final Throwable e) {
            s_logger.error("Unexpected exception: ", e);
            if (connectionAlive) {
                closeConnection();
            }
        } finally {
            s_logger.info("Receiving thread exit processing, shutdown connection");
            vncConnection.shutdown();
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
        connectionAlive = false;
    }

    public boolean isConnectionAlive() {
        return connectionAlive;
    }
}
