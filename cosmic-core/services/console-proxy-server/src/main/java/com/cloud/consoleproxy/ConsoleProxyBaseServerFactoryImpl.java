package com.cloud.consoleproxy;

import javax.net.ssl.SSLServerSocket;
import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleProxyBaseServerFactoryImpl implements ConsoleProxyServerFactory {
    private static final Logger s_logger = LoggerFactory.getLogger(ConsoleProxyBaseServerFactoryImpl.class);

    @Override
    public void init(final byte[] ksBits, final String ksPassword) {
    }

    @Override
    public HttpServer createHttpServerInstance(final int port) throws IOException {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("create HTTP server instance at port: " + port);
        }
        return HttpServer.create(new InetSocketAddress(port), 5);
    }

    @Override
    public SSLServerSocket createSSLServerSocket(final int port) throws IOException {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("SSL server socket is not supported in ConsoleProxyBaseServerFactoryImpl");
        }

        return null;
    }
}
