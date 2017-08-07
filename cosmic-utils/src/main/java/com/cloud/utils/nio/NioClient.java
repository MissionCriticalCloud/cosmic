package com.cloud.utils.nio;

import com.cloud.utils.security.SSLUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.GeneralSecurityException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NioClient extends NioConnection {
    private static final Logger s_logger = LoggerFactory.getLogger(NioClient.class);

    protected String _host;
    protected String _bindAddress;
    protected SocketChannel _clientConnection;

    public NioClient(final String name, final String host, final int port, final int workers, final HandlerFactory factory) {
        super(name, port, workers, factory);
        _host = host;
    }

    public void setBindAddress(final String ipAddress) {
        _bindAddress = ipAddress;
    }

    @Override
    protected void init() throws IOException {
        _selector = Selector.open();
        Task task = null;

        try {
            _clientConnection = SocketChannel.open();
            _clientConnection.configureBlocking(true);
            s_logger.info("Connecting to " + _host + ":" + _port);

            if (_bindAddress != null) {
                s_logger.info("Binding outbound interface at " + _bindAddress);

                final InetSocketAddress bindAddr = new InetSocketAddress(_bindAddress, 0);
                _clientConnection.socket().bind(bindAddr);
            }

            final InetSocketAddress peerAddr = new InetSocketAddress(_host, _port);
            _clientConnection.connect(peerAddr);

            SSLEngine sslEngine = null;
            // Begin SSL handshake in BLOCKING mode
            _clientConnection.configureBlocking(true);

            final SSLContext sslContext = Link.initSSLContext(true);
            sslEngine = sslContext.createSSLEngine(_host, _port);
            sslEngine.setUseClientMode(true);
            sslEngine.setEnabledProtocols(SSLUtils.getSupportedProtocols(sslEngine.getEnabledProtocols()));

            Link.doHandshake(_clientConnection, sslEngine, true);
            s_logger.info("SSL: Handshake done");
            s_logger.info("Connected to " + _host + ":" + _port);

            _clientConnection.configureBlocking(false);
            final Link link = new Link(peerAddr, this);
            link.setSSLEngine(sslEngine);
            final SelectionKey key = _clientConnection.register(_selector, SelectionKey.OP_READ);
            link.setKey(key);
            key.attach(link);
            // Notice we've already connected due to the handshake, so let's get the
            // remaining task done
            task = _factory.create(Task.Type.CONNECT, link, null);
        } catch (final GeneralSecurityException e) {
            _selector.close();
            throw new IOException("Failed to initialise security", e);
        } catch (final IOException e) {
            _selector.close();
            throw e;
        }
        _executor.submit(task);
    }

    @Override
    protected void registerLink(final InetSocketAddress saddr, final Link link) {
        // don't do anything.
    }

    @Override
    protected void unregisterLink(final InetSocketAddress saddr) {
        // don't do anything.
    }

    @Override
    public void cleanUp() throws IOException {
        super.cleanUp();
        if (_clientConnection != null) {
            _clientConnection.close();
        }
        s_logger.info("NioClient connection closed");
    }
}
