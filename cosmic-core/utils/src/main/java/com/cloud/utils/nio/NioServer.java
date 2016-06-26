//

//

package com.cloud.utils.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.WeakHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NioServer extends NioConnection {
    private final static Logger s_logger = LoggerFactory.getLogger(NioServer.class);

    protected InetSocketAddress _localAddr;
    protected WeakHashMap<InetSocketAddress, Link> _links;
    private ServerSocketChannel _serverSocket;

    public NioServer(final String name, final int port, final int workers, final HandlerFactory factory) {
        super(name, port, workers, factory);
        _localAddr = null;
        _links = new WeakHashMap<>(1024);
    }

    @Override
    protected void init() throws IOException {
        _selector = SelectorProvider.provider().openSelector();

        _serverSocket = ServerSocketChannel.open();
        _serverSocket.configureBlocking(false);

        _localAddr = new InetSocketAddress(_port);
        _serverSocket.socket().bind(_localAddr);

        _serverSocket.register(_selector, SelectionKey.OP_ACCEPT, null);

        s_logger.info("NioConnection started and listening on " + _localAddr.toString());
    }

    @Override
    protected void registerLink(final InetSocketAddress addr, final Link link) {
        _links.put(addr, link);
    }

    @Override
    protected void unregisterLink(final InetSocketAddress saddr) {
        _links.remove(saddr);
    }

    @Override
    public void cleanUp() throws IOException {
        super.cleanUp();
        if (_serverSocket != null) {
            _serverSocket.close();
        }
        s_logger.info("NioConnection stopped on " + _localAddr.toString());
    }

    /**
     * Sends the data to the address specified.  If address is not already
     * connected, this does nothing and returns null.  If address is already
     * connected, then it returns the attached object so the caller can
     * prepare for any responses.
     *
     * @param saddr
     * @param data
     * @return null if not sent.  attach object in link if sent.
     */
    public Object send(final InetSocketAddress saddr, final byte[] data) throws ClosedChannelException {
        final Link link = _links.get(saddr);
        if (link == null) {
            return null;
        }
        link.send(data);
        return link.attachment();
    }
}
