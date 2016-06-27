package com.cloud.consoleproxy.vnc;

import com.cloud.consoleproxy.ConsoleProxyClientListener;
import com.cloud.consoleproxy.util.Logger;
import com.cloud.consoleproxy.util.RawHTTP;
import com.cloud.consoleproxy.vnc.packet.client.KeyboardEventPacket;
import com.cloud.consoleproxy.vnc.packet.client.MouseEventPacket;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.awt.Frame;
import java.awt.ScrollPane;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.spec.KeySpec;

public class VncClient {
    private static final Logger s_logger = Logger.getLogger(VncClient.class);
    private final VncScreenDescription screen = new VncScreenDescription();
    private Socket socket;
    private DataInputStream is;
    private DataOutputStream os;
    private VncClientPacketSender sender;
    private VncServerPacketReceiver receiver;

    private boolean noUI = false;
    private ConsoleProxyClientListener clientListener = null;

    public VncClient(final ConsoleProxyClientListener clientListener) {
        noUI = true;
        this.clientListener = clientListener;
    }

    public VncClient(final String host, final int port, final String password, final boolean noUI, final ConsoleProxyClientListener clientListener) throws UnknownHostException,
            IOException {

        this.noUI = noUI;
        this.clientListener = clientListener;
        connectTo(host, port, password);
    }

    public void connectTo(final String host, final int port, final String password) throws UnknownHostException, IOException {
        // Connect to server
        s_logger.info("Connecting to VNC server " + host + ":" + port + "...");
        socket = new Socket(host, port);
        doConnect(password);
    }

    private void doConnect(final String password) throws IOException {
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());

        // Initialize connection
        handshake();
        authenticate(password);
        initialize();

        s_logger.info("Connecting to VNC server succeeded, start session");

        // Run client-to-server packet sender
        sender = new VncClientPacketSender(os, screen, this);

        // Create buffered image canvas
        final BufferedImageCanvas canvas = new BufferedImageCanvas(sender, screen.getFramebufferWidth(), screen.getFramebufferHeight());

        // Subscribe packet sender to various events
        canvas.addMouseListener(sender);
        canvas.addMouseMotionListener(sender);
        canvas.addKeyListener(sender);

        Frame frame = null;
        if (!noUI) {
            frame = createVncClientMainWindow(canvas, screen.getDesktopName());
        }

        new Thread(sender).start();

        // Run server-to-client packet receiver
        receiver = new VncServerPacketReceiver(is, canvas, screen, this, sender, clientListener);
        try {
            receiver.run();
        } finally {
            if (frame != null) {
                frame.setVisible(false);
                frame.dispose();
            }
            shutdown();
        }
    }

    /**
     * Handshake with VNC server.
     */
    private void handshake() throws IOException {

        // Read protocol version
        final byte[] buf = new byte[12];
        is.readFully(buf);
        final String rfbProtocol = new String(buf);

        // Server should use RFB protocol 3.x
        if (!rfbProtocol.contains(RfbConstants.RFB_PROTOCOL_VERSION_MAJOR)) {
            s_logger.error("Cannot handshake with VNC server. Unsupported protocol version: \"" + rfbProtocol + "\".");
            throw new RuntimeException("Cannot handshake with VNC server. Unsupported protocol version: \"" + rfbProtocol + "\".");
        }

        // Send response: we support RFB 3.3 only
        final String ourProtocolString = RfbConstants.RFB_PROTOCOL_VERSION + "\n";
        os.write(ourProtocolString.getBytes());
        os.flush();
    }

    /**
     * VNC authentication.
     */
    private void authenticate(final String password) throws IOException {
        // Read security type
        final int authType = is.readInt();

        switch (authType) {
            case RfbConstants.CONNECTION_FAILED: {
                // Server forbids to connect. Read reason and throw exception

                final int length = is.readInt();
                final byte[] buf = new byte[length];
                is.readFully(buf);
                final String reason = new String(buf, RfbConstants.CHARSET);

                s_logger.error("Authentication to VNC server is failed. Reason: " + reason);
                throw new RuntimeException("Authentication to VNC server is failed. Reason: " + reason);
            }

            case RfbConstants.NO_AUTH: {
                // Client can connect without authorization. Nothing to do.
                break;
            }

            case RfbConstants.VNC_AUTH: {
                s_logger.info("VNC server requires password authentication");
                doVncAuth(password);
                break;
            }

            default:
                s_logger.error("Unsupported VNC protocol authorization scheme, scheme code: " + authType + ".");
                throw new RuntimeException("Unsupported VNC protocol authorization scheme, scheme code: " + authType + ".");
        }
    }

    private void initialize() throws IOException {
        // Send client initialization message
        {
            // Send shared flag
            os.writeByte(RfbConstants.EXCLUSIVE_ACCESS);
            os.flush();
        }

        // Read server initialization message
        {
            // Read frame buffer size
            final int framebufferWidth = is.readUnsignedShort();
            final int framebufferHeight = is.readUnsignedShort();
            screen.setFramebufferSize(framebufferWidth, framebufferHeight);
            if (clientListener != null) {
                clientListener.onFramebufferSizeChange(framebufferWidth, framebufferHeight);
            }
        }

        // Read pixel format
        {
            final int bitsPerPixel = is.readUnsignedByte();
            final int depth = is.readUnsignedByte();

            final int bigEndianFlag = is.readUnsignedByte();
            final int trueColorFlag = is.readUnsignedByte();

            final int redMax = is.readUnsignedShort();
            final int greenMax = is.readUnsignedShort();
            final int blueMax = is.readUnsignedShort();

            final int redShift = is.readUnsignedByte();
            final int greenShift = is.readUnsignedByte();
            final int blueShift = is.readUnsignedByte();

            // Skip padding
            is.skipBytes(3);

            screen.setPixelFormat(bitsPerPixel, depth, bigEndianFlag, trueColorFlag, redMax, greenMax, blueMax, redShift, greenShift, blueShift);
        }

        // Read desktop name
        {
            final int length = is.readInt();
            final byte[] buf = new byte[length];
            is.readFully(buf);
            final String desktopName = new String(buf, RfbConstants.CHARSET);
            screen.setDesktopName(desktopName);
        }
    }

    private Frame createVncClientMainWindow(final BufferedImageCanvas canvas, final String title) {
        // Create AWT windows
        final Frame frame = new Frame(title + " - VNCle");

        // Use scrolling pane to support screens, which are larger than ours
        final ScrollPane scroller = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
        scroller.add(canvas);
        scroller.setSize(screen.getFramebufferWidth(), screen.getFramebufferHeight());

        frame.add(scroller);
        frame.pack();
        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent evt) {
                frame.setVisible(false);
                shutdown();
            }
        });

        return frame;
    }

    public void shutdown() {
        if (sender != null) {
            sender.closeConnection();
        }

        if (receiver != null) {
            receiver.closeConnection();
        }

        if (is != null) {
            try {
                is.close();
            } catch (final Throwable e) {
                s_logger.info("[ignored]"
                        + "failed to close resource for input: " + e.getLocalizedMessage());
            }
        }

        if (os != null) {
            try {
                os.close();
            } catch (final Throwable e) {
                s_logger.info("[ignored]"
                        + "failed to get close resource for output: " + e.getLocalizedMessage());
            }
        }

        if (socket != null) {
            try {
                socket.close();
            } catch (final Throwable e) {
                s_logger.info("[ignored]"
                        + "failed to get close resource for socket: " + e.getLocalizedMessage());
            }
        }
    }

    /**
     * Encode client password and send it to server.
     */
    private void doVncAuth(final String password) throws IOException {

        // Read challenge
        final byte[] challenge = new byte[16];
        is.readFully(challenge);

        // Encode challenge with password
        final byte[] response;
        try {
            response = encodePassword(challenge, password);
        } catch (final Exception e) {
            s_logger.error("Cannot encrypt client password to send to server: " + e.getMessage());
            throw new RuntimeException("Cannot encrypt client password to send to server: " + e.getMessage());
        }

        // Send encoded challenge
        os.write(response);
        os.flush();

        // Read security result
        final int authResult = is.readInt();

        switch (authResult) {
            case RfbConstants.VNC_AUTH_OK: {
                // Nothing to do
                break;
            }

            case RfbConstants.VNC_AUTH_TOO_MANY:
                s_logger.error("Connection to VNC server failed: too many wrong attempts.");
                throw new RuntimeException("Connection to VNC server failed: too many wrong attempts.");

            case RfbConstants.VNC_AUTH_FAILED:
                s_logger.error("Connection to VNC server failed: wrong password.");
                throw new RuntimeException("Connection to VNC server failed: wrong password.");

            default:
                s_logger.error("Connection to VNC server failed, reason code: " + authResult);
                throw new RuntimeException("Connection to VNC server failed, reason code: " + authResult);
        }
    }

    /**
     * Encode password using DES encryption with given challenge.
     *
     * @param challenge a random set of bytes.
     * @param password  a password
     * @return DES hash of password and challenge
     */
    public byte[] encodePassword(final byte[] challenge, final String password) throws Exception {
        // VNC password consist of up to eight ASCII characters.
        final byte[] key = {0, 0, 0, 0, 0, 0, 0, 0}; // Padding
        final byte[] passwordAsciiBytes = password.getBytes(RfbConstants.CHARSET);
        System.arraycopy(passwordAsciiBytes, 0, key, 0, Math.min(password.length(), 8));

        // Flip bytes (reverse bits) in key
        for (int i = 0; i < key.length; i++) {
            key[i] = flipByte(key[i]);
        }

        final KeySpec desKeySpec = new DESKeySpec(key);
        final SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("DES");
        final SecretKey secretKey = secretKeyFactory.generateSecret(desKeySpec);
        final Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        final byte[] response = cipher.doFinal(challenge);
        return response;
    }

    /**
     * Reverse bits in byte, so least significant bit will be most significant
     * bit. E.g. 01001100 will become 00110010.
     * <p>
     * See also: http://www.vidarholen.net/contents/junk/vnc.html ,
     * http://bytecrafter
     * .blogspot.com/2010/09/des-encryption-as-used-in-vnc.html
     *
     * @param b a byte
     * @return byte in reverse order
     */
    private static byte flipByte(final byte b) {
        final int b1_8 = (b & 0x1) << 7;
        final int b2_7 = (b & 0x2) << 5;
        final int b3_6 = (b & 0x4) << 3;
        final int b4_5 = (b & 0x8) << 1;
        final int b5_4 = (b & 0x10) >>> 1;
        final int b6_3 = (b & 0x20) >>> 3;
        final int b7_2 = (b & 0x40) >>> 5;
        final int b8_1 = (b & 0x80) >>> 7;
        final byte c = (byte) (b1_8 | b2_7 | b3_6 | b4_5 | b5_4 | b6_3 | b7_2 | b8_1);
        return c;
    }

    public static void main(final String[] args) {
        if (args.length < 3) {
            printHelpMessage();
            System.exit(1);
        }

        final String host = args[0];
        final String port = args[1];
        final String password = args[2];

        try {
            new VncClient(host, Integer.parseInt(port), password, false, null);
        } catch (final NumberFormatException e) {
            s_logger.error("Incorrect VNC server port number: " + port + ".");
            System.exit(1);
        } catch (final UnknownHostException e) {
            s_logger.error("Incorrect VNC server host name: " + host + ".");
            System.exit(1);
        } catch (final IOException e) {
            s_logger.error("Cannot communicate with VNC server: " + e.getMessage());
            System.exit(1);
        } catch (final Throwable e) {
            s_logger.error("An error happened: " + e.getMessage());
            System.exit(1);
        }
        System.exit(0);
    }

    private static void printHelpMessage() {
        /* LOG */
        s_logger.info("Usage: HOST PORT PASSWORD.");
    }

    public ConsoleProxyClientListener getClientListener() {
        return clientListener;
    }

    public void connectTo(final String host, int port, final String path, final String session, final boolean useSSL, final String sid) throws UnknownHostException, IOException {
        if (port < 0) {
            if (useSSL) {
                port = 443;
            } else {
                port = 80;
            }
        }

        final RawHTTP tunnel = new RawHTTP("CONNECT", host, port, path, session, useSSL);
        socket = tunnel.connect();
        doConnect(sid);
    }

    public FrameBufferCanvas getFrameBufferCanvas() {
        if (receiver != null) {
            return receiver.getCanvas();
        }

        return null;
    }

    public void requestUpdate(final boolean fullUpdate) {
        if (fullUpdate) {
            sender.requestFullScreenUpdate();
        } else {
            sender.imagePaintedOnScreen();
        }
    }

    public void sendClientKeyboardEvent(final int event, final int code, final int modifiers) {
        sender.sendClientPacket(new KeyboardEventPacket(event, code));
    }

    public void sendClientMouseEvent(final int event, final int x, final int y, final int code, final int modifiers) {
        sender.sendClientPacket(new MouseEventPacket(event, x, y));
    }

    public boolean isHostConnected() {
        return receiver != null && receiver.isConnectionAlive();
    }
}
