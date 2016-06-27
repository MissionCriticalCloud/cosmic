//

//

package org.apache.commons.httpclient.contrib.ssl;

import org.apache.cloudstack.utils.security.SSLUtils;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.HttpClientError;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * EasySSLProtocolSocketFactory can be used to create SSL {@link Socket}s
 * that accept self-signed certificates.
 * </p>
 * <p>
 * This socket factory SHOULD NOT be used for productive systems
 * due to security reasons, unless it is a concious decision and
 * you are perfectly aware of security implications of accepting
 * self-signed certificates
 * </p>
 * <p>
 * <p>
 * Example of using custom protocol socket factory for a specific host:
 * <pre>
 *     Protocol easyhttps = new Protocol("https", new EasySSLProtocolSocketFactory(), 443);
 *
 *     URI uri = new URI("https://localhost/", true);
 *     // use relative url only
 *     GetMethod httpget = new GetMethod(uri.getPathQuery());
 *     HostConfiguration hc = new HostConfiguration();
 *     hc.setHost(uri.getHost(), uri.getPort(), easyhttps);
 *     HttpClient client = new HttpClient();
 *     client.executeMethod(hc, httpget);
 *     </pre>
 * </p>
 * <p>
 * Example of using custom protocol socket factory per default instead of the standard one:
 * <pre>
 *     Protocol easyhttps = new Protocol("https", new EasySSLProtocolSocketFactory(), 443);
 *     Protocol.registerProtocol("https", easyhttps);
 *
 *     HttpClient client = new HttpClient();
 *     GetMethod httpget = new GetMethod("https://localhost/");
 *     client.executeMethod(httpget);
 *     </pre>
 * </p>
 *
 * @author <a href="mailto:oleg -at- ural.ru">Oleg Kalnichevski</a>
 *         <p>
 *         <p>
 *         DISCLAIMER: HttpClient developers DO NOT actively support this component.
 *         The component is provided as a reference material, which may be inappropriate
 *         for use without additional customization.
 *         </p>
 */

public class EasySSLProtocolSocketFactory implements ProtocolSocketFactory {

    /**
     * Log object for this class.
     */
    private static final Log LOG = LogFactory.getLog(EasySSLProtocolSocketFactory.class);

    private SSLContext sslcontext = null;

    /**
     * Constructor for EasySSLProtocolSocketFactory.
     */
    public EasySSLProtocolSocketFactory() {
        super();
    }

    /**
     * @see ProtocolSocketFactory#createSocket(java.lang.String, int, java.net.InetAddress, int)
     */
    @Override
    public Socket createSocket(final String host, final int port, final InetAddress clientHost, final int clientPort) throws IOException, UnknownHostException {
        final Socket socket = getSSLContext().getSocketFactory().createSocket(host, port, clientHost, clientPort);
        if (socket instanceof SSLSocket) {
            ((SSLSocket) socket).setEnabledProtocols(SSLUtils.getSupportedProtocols(((SSLSocket) socket).getEnabledProtocols()));
        }
        return socket;
    }

    private SSLContext getSSLContext() {
        if (sslcontext == null) {
            sslcontext = createEasySSLContext();
        }
        return sslcontext;
    }

    private static SSLContext createEasySSLContext() {
        try {
            final SSLContext context = SSLUtils.getSSLContext();
            context.init(null, new TrustManager[]{new EasyX509TrustManager(null)}, null);
            return context;
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw new HttpClientError(e.toString());
        }
    }

    /**
     * Attempts to get a new socket connection to the given host within the given time limit.
     * <p>
     * To circumvent the limitations of older JREs that do not support connect timeout a
     * controller thread is executed. The controller thread attempts to create a new socket
     * within the given limit of time. If socket constructor does not return until the
     * timeout expires, the controller terminates and throws an {@link ConnectTimeoutException}
     * </p>
     *
     * @param host         the host name/IP
     * @param port         the port on the host
     * @param localAddress the local host name/IP to bind the socket to
     * @param localPort    the port on the local machine
     * @param params       {@link HttpConnectionParams Http connection parameters}
     * @return Socket a new socket
     * @throws IOException          if an I/O error occurs while creating the socket
     * @throws UnknownHostException if the IP address of the host cannot be
     *                              determined
     */
    @Override
    public Socket createSocket(final String host, final int port, final InetAddress localAddress, final int localPort, final HttpConnectionParams params)
            throws IOException, UnknownHostException, ConnectTimeoutException {
        if (params == null) {
            throw new IllegalArgumentException("Parameters may not be null");
        }
        final int timeout = params.getConnectionTimeout();
        final SocketFactory socketfactory = getSSLContext().getSocketFactory();
        if (timeout == 0) {
            final Socket socket = socketfactory.createSocket(host, port, localAddress, localPort);
            if (socket instanceof SSLSocket) {
                ((SSLSocket) socket).setEnabledProtocols(SSLUtils.getSupportedProtocols(((SSLSocket) socket).getEnabledProtocols()));
            }
            return socket;
        } else {
            final Socket socket = socketfactory.createSocket();
            if (socket instanceof SSLSocket) {
                ((SSLSocket) socket).setEnabledProtocols(SSLUtils.getSupportedProtocols(((SSLSocket) socket).getEnabledProtocols()));
            }
            final SocketAddress localaddr = new InetSocketAddress(localAddress, localPort);
            final SocketAddress remoteaddr = new InetSocketAddress(host, port);
            socket.bind(localaddr);
            socket.connect(remoteaddr, timeout);
            return socket;
        }
    }

    /**
     * @see ProtocolSocketFactory#createSocket(java.lang.String, int)
     */
    @Override
    public Socket createSocket(final String host, final int port) throws IOException, UnknownHostException {
        final Socket socket = (Socket) getSSLContext().getSocketFactory().createSocket(host, port);
        if (socket instanceof SSLSocket) {
            ((SSLSocket) socket).setEnabledProtocols(SSLUtils.getSupportedProtocols(((SSLSocket) socket).getEnabledProtocols()));
        }
        return socket;
    }

    public Socket createSocket(final Socket socket, final String host, final int port, final boolean autoClose) throws IOException, UnknownHostException {
        final Socket s = getSSLContext().getSocketFactory().createSocket(socket, host, port, autoClose);
        if (s instanceof SSLSocket) {
            ((SSLSocket) s).setEnabledProtocols(SSLUtils.getSupportedProtocols(((SSLSocket) s).getEnabledProtocols()));
        }
        return s;
    }

    @Override
    public int hashCode() {
        return EasySSLProtocolSocketFactory.class.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return ((obj != null) && obj.getClass().equals(EasySSLProtocolSocketFactory.class));
    }
}
