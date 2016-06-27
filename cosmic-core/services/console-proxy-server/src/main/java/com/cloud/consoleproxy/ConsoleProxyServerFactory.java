package com.cloud.consoleproxy;

import javax.net.ssl.SSLServerSocket;
import java.io.IOException;

import com.sun.net.httpserver.HttpServer;

public interface ConsoleProxyServerFactory {
    void init(byte[] ksBits, String ksPassword);

    HttpServer createHttpServerInstance(int port) throws IOException;

    SSLServerSocket createSSLServerSocket(int port) throws IOException;
}
