package com.cloud.consoleproxy;

import org.apache.cloudstack.utils.security.SSLUtils;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyStore;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import org.apache.log4j.Logger;

public class ConsoleProxySecureServerFactoryImpl implements ConsoleProxyServerFactory {
    private static final Logger s_logger = Logger.getLogger(ConsoleProxySecureServerFactoryImpl.class);

    private SSLContext sslContext = null;

    public ConsoleProxySecureServerFactoryImpl() {
    }

    @Override
    public void init(final byte[] ksBits, final String ksPassword) {
        s_logger.info("Start initializing SSL");

        if (ksBits == null) {
            // this should not be the case
            s_logger.info("No certificates passed, recheck global configuration and certificates");
        } else {
            final char[] passphrase = ksPassword != null ? ksPassword.toCharArray() : null;
            try {
                s_logger.info("Initializing SSL from passed-in certificate");

                final KeyStore ks = KeyStore.getInstance("JKS");
                ks.load(new ByteArrayInputStream(ksBits), passphrase);

                final KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                kmf.init(ks, passphrase);
                s_logger.info("Key manager factory is initialized");

                final TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
                tmf.init(ks);
                s_logger.info("Trust manager factory is initialized");

                sslContext = SSLUtils.getSSLContext();
                sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
                s_logger.info("SSL context is initialized");
            } catch (final Exception e) {
                s_logger.error("Unable to init factory due to exception ", e);
            }
        }
    }

    @Override
    public HttpServer createHttpServerInstance(final int port) throws IOException {
        try {
            final HttpsServer server = HttpsServer.create(new InetSocketAddress(port), 5);
            server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                @Override
                public void configure(final HttpsParameters params) {

                    // get the remote address if needed
                    final InetSocketAddress remote = params.getClientAddress();
                    final SSLContext c = getSSLContext();

                    // get the default parameters
                    final SSLParameters sslparams = c.getDefaultSSLParameters();

                    params.setSSLParameters(sslparams);
                    // statement above could throw IAE if any params invalid.
                    // eg. if app has a UI and parameters supplied by a user.
                }
            });

            s_logger.info("create HTTPS server instance on port: " + port);
            return server;
        } catch (final Exception ioe) {
            s_logger.error(ioe.toString(), ioe);
        }
        return null;
    }

    @Override
    public SSLServerSocket createSSLServerSocket(final int port) throws IOException {
        try {
            SSLServerSocket srvSock = null;
            final SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();
            srvSock = (SSLServerSocket) ssf.createServerSocket(port);
            srvSock.setEnabledProtocols(SSLUtils.getSupportedProtocols(srvSock.getEnabledProtocols()));

            s_logger.info("create SSL server socket on port: " + port);
            return srvSock;
        } catch (final Exception ioe) {
            s_logger.error(ioe.toString(), ioe);
        }
        return null;
    }
}
