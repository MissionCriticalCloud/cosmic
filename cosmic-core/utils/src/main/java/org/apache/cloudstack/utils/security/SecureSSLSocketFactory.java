//

//

package org.apache.cloudstack.utils.security;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecureSSLSocketFactory extends SSLSocketFactory {

    public static final Logger s_logger = LoggerFactory.getLogger(SecureSSLSocketFactory.class);
    private final SSLContext _sslContext;

    public SecureSSLSocketFactory() throws NoSuchAlgorithmException {
        _sslContext = SSLUtils.getSSLContext();
    }

    public SecureSSLSocketFactory(final SSLContext sslContext) throws NoSuchAlgorithmException {
        if (sslContext != null) {
            _sslContext = sslContext;
        } else {
            _sslContext = SSLUtils.getSSLContext();
        }
    }

    public SecureSSLSocketFactory(final KeyManager[] km, final TrustManager[] tm, final SecureRandom random) throws NoSuchAlgorithmException, KeyManagementException, IOException {
        _sslContext = SSLUtils.getSSLContext();
        _sslContext.init(km, tm, random);
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return getSupportedCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        String[] ciphers = null;
        try {
            ciphers = SSLUtils.getSupportedCiphers();
        } catch (final NoSuchAlgorithmException e) {
            s_logger.error("SecureSSLSocketFactory::getDefaultCipherSuites found no cipher suites");
        }
        return ciphers;
    }

    @Override
    public Socket createSocket(final Socket s, final String host, final int port, final boolean autoClose) throws IOException {
        final SSLSocketFactory factory = _sslContext.getSocketFactory();
        final Socket socket = factory.createSocket(s, host, port, autoClose);
        if (socket instanceof SSLSocket) {
            ((SSLSocket) socket).setEnabledProtocols(SSLUtils.getSupportedProtocols(((SSLSocket) socket).getEnabledProtocols()));
        }
        return socket;
    }

    @Override
    public Socket createSocket(final String host, final int port) throws IOException, UnknownHostException {
        final SSLSocketFactory factory = _sslContext.getSocketFactory();
        final Socket socket = factory.createSocket(host, port);
        if (socket instanceof SSLSocket) {
            ((SSLSocket) socket).setEnabledProtocols(SSLUtils.getSupportedProtocols(((SSLSocket) socket).getEnabledProtocols()));
        }
        return socket;
    }

    @Override
    public Socket createSocket(final String host, final int port, final InetAddress inetAddress, final int localPort) throws IOException, UnknownHostException {
        final SSLSocketFactory factory = _sslContext.getSocketFactory();
        final Socket socket = factory.createSocket(host, port, inetAddress, localPort);
        if (socket instanceof SSLSocket) {
            ((SSLSocket) socket).setEnabledProtocols(SSLUtils.getSupportedProtocols(((SSLSocket) socket).getEnabledProtocols()));
        }
        return socket;
    }

    @Override
    public Socket createSocket(final InetAddress inetAddress, final int localPort) throws IOException {
        final SSLSocketFactory factory = _sslContext.getSocketFactory();
        final Socket socket = factory.createSocket(inetAddress, localPort);
        if (socket instanceof SSLSocket) {
            ((SSLSocket) socket).setEnabledProtocols(SSLUtils.getSupportedProtocols(((SSLSocket) socket).getEnabledProtocols()));
        }
        return socket;
    }

    @Override
    public Socket createSocket(final InetAddress address, final int port, final InetAddress localAddress, final int localPort) throws IOException {
        final SSLSocketFactory factory = this._sslContext.getSocketFactory();
        final Socket socket = factory.createSocket(address, port, localAddress, localPort);
        if (socket instanceof SSLSocket) {
            ((SSLSocket) socket).setEnabledProtocols(SSLUtils.getSupportedProtocols(((SSLSocket) socket).getEnabledProtocols()));
        }
        return socket;
    }
}
