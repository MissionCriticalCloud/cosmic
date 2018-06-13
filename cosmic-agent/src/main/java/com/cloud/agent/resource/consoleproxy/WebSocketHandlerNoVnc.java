package com.cloud.agent.resource.consoleproxy;

import com.cloud.consoleproxy.util.RawHTTP;
import com.cloud.consoleproxy.vnc.RfbConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.spec.KeySpec;
import java.util.Map;
import java.util.Objects;

public class WebSocketHandlerNoVnc extends BinaryWebSocketHandler {
    private static final Logger s_logger = LoggerFactory.getLogger(WebSocketHandlerNoVnc.class);
    private Socket vncSocket;
    private DataInputStream is;
    private DataOutputStream os;
    private WebSocketSession session;
    private String hostPassword;
    private double rfbVersion;

    private enum VncState {
        SERVER_VERSION_SENT, AUTH_TYPES_SENT, AUTH_RESULT_SENT, UNKNOWN
    }

    private static final byte[] M_VNC_AUTH_OK = new byte[]{0, 0, 0, 0};
    private static final byte[] M_VNC_AUTH_TYPE_NOAUTH = new byte[]{1, 1};
    private VncState clientState;

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
    private static byte flipByte(byte b) {
        int b1_8 = (b & 0x1) << 7;
        int b2_7 = (b & 0x2) << 5;
        int b3_6 = (b & 0x4) << 3;
        int b4_5 = (b & 0x8) << 1;
        int b5_4 = (b & 0x10) >>> 1;
        int b6_3 = (b & 0x20) >>> 3;
        int b7_2 = (b & 0x40) >>> 5;
        int b8_1 = (b & 0x80) >>> 7;
        return (byte) (b1_8 | b2_7 | b3_6 | b4_5 | b5_4 | b6_3 | b7_2 | b8_1);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        s_logger.info("Connect: " + session.getRemoteAddress());

        String queries = Objects.requireNonNull(session.getUri()).getQuery();
        Map<String, String> queryMap = ConsoleProxyHttpHandlerHelper.getQueryMap(queries);
        String host = queryMap.get("host");
        String portStr = queryMap.get("port");
        String sid = queryMap.get("sid");

        String ticket = queryMap.get("ticket");
        String console_url = queryMap.get("consoleurl");
        String console_host_session = queryMap.get("sessionref");
        int port;

        if (host == null || portStr == null || sid == null)
            throw new IllegalArgumentException();

        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            s_logger.warn("Invalid number parameter in query string: " + portStr);
            throw new IllegalArgumentException(e);
        }

        try {
            ConsoleProxyClientParam param = new ConsoleProxyClientParam();
            param.setClientHostAddress(host);
            param.setClientHostPort(port);
            param.setTicket(ticket);
            param.setClientTunnelUrl(console_url);
            param.setClientTunnelSession(console_host_session);
            this.hostPassword = sid;
            proxynoVNC(session, param);
        } catch (Exception e) {

            s_logger.error("Failed to create viewer due to " + e.getMessage(), e);

            String[] content =
                    new String[]{"<html><head></head><body>", "<div id=\"main_panel\" tabindex=\"1\">",
                            "<p>Access is denied for the console session check. Please close the window and retry again</p>", "</div></body></html>"};

            StringBuilder sb = new StringBuilder();
            for (String aContent : content) sb.append(aContent);

            sendResponseString(session, sb.toString());
        }
    }

    private void proxynoVNC(WebSocketSession session, ConsoleProxyClientParam param) {
        this.session = session;
        String tunnelUrl = param.getClientTunnelUrl();
        String tunnelSession = param.getClientTunnelSession();

        try {
            if (tunnelUrl != null && !tunnelUrl.isEmpty() && tunnelSession != null && !tunnelSession.isEmpty()) {
                URI uri = new URI(tunnelUrl);
                s_logger.info("Connect to VNC server via tunnel. url: " + tunnelUrl + ", session: " + tunnelSession);
                ConsoleProxy.ensureRoute(uri.getHost());
                connectTo(
                        uri.getHost(), uri.getPort(),
                        uri.getPath() + "?" + uri.getQuery(),
                        tunnelSession, "https".equalsIgnoreCase(uri.getScheme()));
            } else {
                s_logger.info("Connect to VNC server directly. host: " + param.getClientHostAddress() + ", port: " + param.getClientHostPort());
                vncSocket = new Socket(param.getClientHostAddress(), param.getClientHostPort());
                doConnect(vncSocket);
            }
        } catch (Throwable e) {
            s_logger.error("Unexpected exception", e);
        }
    }

    private void connectTo(String host, int port, String path, String session, boolean useSSL) throws IOException {
        if (port < 0) {
            port = useSSL ? 443 : 80;
        }

        RawHTTP tunnel = new RawHTTP("CONNECT", host, port, path, session, useSSL);
        vncSocket = tunnel.connect();
        doConnect(vncSocket);
    }

    private void startProxyThread() {
        byte[] b = new byte[1500];
        int readBytes = -1;
        while (true) {
            try {
                vncSocket.setSoTimeout(0);
                readBytes = is.read(b);

            } catch (IOException e) {
                e.printStackTrace();
            }

            if (readBytes == -1) {
                break;
            }

            System.out.printf("read bytes %d\n", readBytes);
            if (readBytes > 0) {
                s_logger.warn("sending bytes of size" + readBytes + " from sender thread");
                sendResponseBytes(session, b, readBytes);
            }
        }
    }

    private void sendResponseString(WebSocketSession session, String s) {
        try {
            session.sendMessage(new TextMessage(s));
        } catch (IOException e) {
            s_logger.error("unable to send response", e);
        }
    }

    private void sendResponseBytes(WebSocketSession session, byte[] bytes, int size) {
        try {
            session.sendMessage(new BinaryMessage(ByteBuffer.wrap(bytes, 0, size)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        shutdown();
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable throwable) {
        s_logger.error("Error in WebSocket Connection : ", throwable);
    }

    private void doConnect(Socket socket) throws IOException {
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        initClient();
        handshakeServer(is, os);
        authenticateServer(is, os);
        new Thread(this::startProxyThread).start();
    }

    private void initClient() {
        byte[] buf = (RfbConstants.RFB_WEBSOCKETS + "\n").getBytes();
        sendResponseBytes(session, buf, 12);
        this.clientState = VncState.SERVER_VERSION_SENT;
    }

    private void authenticateServer(DataInputStream is, DataOutputStream os) {
        // Read security type
        int readAuthTypeCount;
        int authType;
        try {
            if (rfbVersion >= 3.7) {
                readAuthTypeCount = is.read();

                if (readAuthTypeCount == 0) {
                    authType = 0;
                } else {
                    authType = is.read();
                }

                os.write(authType);
                os.flush();
            } else {
                authType = is.readInt();
            }

            switch (authType) {
                case RfbConstants.CONNECTION_FAILED: {
                    // Server forbids to connect. Read reason and throw exception
                    int length = is.readInt();
                    byte[] buf = new byte[length];
                    is.readFully(buf);
                    sendResponseBytes(session, buf, length);
                    String reason = new String(buf, RfbConstants.CHARSET);

                    s_logger.error("Authentication to VNC server is failed. Reason: " + reason);
                    throw new RuntimeException("Authentication to VNC server is failed. Reason: " + reason);
                }

                case RfbConstants.NO_AUTH: {
                    // Client can connect without authorization. Nothing to do.
                    break;
                }

                case RfbConstants.VNC_AUTH: {
                    s_logger.info("VNC server requires password authentication");
                    doVncAuth(this.hostPassword);
                    break;
                }

                default:
                    s_logger.error("Unsupported VNC protocol authorization scheme, scheme code: " + authType + ".");
                    throw new RuntimeException("Unsupported VNC protocol authorization scheme, scheme code: " + authType + ".");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        if (is != null) {
            try {
                is.close();
            } catch (Throwable e) {
                s_logger.info("[ignored]"
                        + "failed to close resource for input: " + e.getLocalizedMessage());
            }
        }

        if (os != null) {
            try {
                os.close();
            } catch (Throwable e) {
                s_logger.info("[ignored]"
                        + "failed to get close resource for output: " + e.getLocalizedMessage());
            }
        }

        if (vncSocket != null) {
            try {
                vncSocket.close();
            } catch (Throwable e) {
                s_logger.info("[ignored]"
                        + "failed to get close resource for socket: " + e.getLocalizedMessage());
            }
        }

    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage byteBuffer) throws Exception {
        System.out.printf("Frame: %d\n", byteBuffer.getPayloadLength());
        byte[] data = new byte[byteBuffer.getPayloadLength()];

        switch (this.clientState) {
            case SERVER_VERSION_SENT: {
                if (byteBuffer.getPayloadLength() == 12) {
                    s_logger.debug("received noVNC handshakeServer");
                }
                sendResponseBytes(session, M_VNC_AUTH_TYPE_NOAUTH, 2);
                this.clientState = VncState.AUTH_TYPES_SENT;
                break;
            }

            case AUTH_TYPES_SENT: {
                // 1 for send auth type count
                // 1 for sending auth type used i.e no auth required
                s_logger.warn("sending auth types and response");
                sendResponseBytes(session, M_VNC_AUTH_OK, 4);
                this.clientState = VncState.AUTH_RESULT_SENT;
                break;
            }

            case AUTH_RESULT_SENT: {
                os.write(data);
                os.flush();
                break;
            }

            default: {
                os.write(data);
                os.flush();
                break;
            }
        }
    }

    /**
     * Encode client password and send it to server.
     */
    private void doVncAuth(String password) throws IOException {

        // Read challenge
        byte[] challenge = new byte[16];
        is.readFully(challenge);
        // Encode challenge with password
        byte[] response;
        try {
            response = encodePassword(challenge, password);
        } catch (Exception e) {
            s_logger.error("Cannot encrypt client password to send to server: " + e.getMessage());
            throw new RuntimeException("Cannot encrypt client password to send to server: " + e.getMessage());
        }

        // Send encoded challenge
        os.write(response);
        os.flush();

        // Read security result
        int authResult = is.readInt();
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
     * Handshake with VNC server.
     */
    private void handshakeServer(DataInputStream is, DataOutputStream os) throws IOException {

        // Read protocol version
        byte[] buf = new byte[12];
        is.readFully(buf);
        String rfbProtocol = new String(buf);
        String protocol = rfbProtocol.substring(4, 11);
        switch (protocol) {
            case "003.003":
            case "003.006":  // UltraVNC
            case "003.889":  // Apple Remote Desktop
                rfbVersion = 3.3;
                break;
            case "003.007":
                rfbVersion = 3.7;
                break;
            case "003.008":
            case "004.000":  // Intel AMT KVM
            case "004.001":  // RealVNC 4.6
                rfbVersion = 3.8;
                break;
            default:
        }

        // Server should use RFB protocol 3.x
        if (!rfbProtocol.contains(RfbConstants.RFB_PROTOCOL_VERSION_MAJOR)) {
            s_logger.error("Cannot handshakeServer with VNC server. Unsupported protocol version: \"" + rfbProtocol + "\".");
            throw new RuntimeException("Cannot handshakeServer with VNC server. Unsupported protocol version: \"" + rfbProtocol + "\".");
        }

        os.write(buf);
        os.flush();
    }

    /**
     * Encode password using DES encryption with given challenge.
     *
     * @param challenge a random set of bytes.
     * @param password  a password
     * @return DES hash of password and challenge
     */
    public byte[] encodePassword(byte[] challenge, String password) throws Exception {
        // VNC password consist of up to eight ASCII characters.
        byte[] key = {0, 0, 0, 0, 0, 0, 0, 0}; // Padding
        byte[] passwordAsciiBytes = password.getBytes(RfbConstants.CHARSET);
        System.arraycopy(passwordAsciiBytes, 0, key, 0, Math.min(password.length(), 8));

        // Flip bytes (reverse bits) in key
        for (int i = 0; i < key.length; i++) {
            key[i] = flipByte(key[i]);
        }

        KeySpec desKeySpec = new DESKeySpec(key);
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey secretKey = secretKeyFactory.generateSecret(desKeySpec);
        Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] response = cipher.doFinal(challenge);
        return response;
    }

//    private void initialize() throws IOException {
//        s_logger.warn("asking for exclusive access");
//        os.writeByte(RfbConstants.EXCLUSIVE_ACCESS);
//        os.flush();
//
//        //   getting initializer parameter and sending them to server
//        byte[] b = new byte[1500];
//        int readBytes = -1;
//        vncSocket.setSoTimeout(0);
//        readBytes = is.read(b);
//        sendResponseBytes(session, b, readBytes);
//    }
}
