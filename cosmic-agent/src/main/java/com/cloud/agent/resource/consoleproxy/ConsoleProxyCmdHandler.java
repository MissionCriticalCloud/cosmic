package com.cloud.agent.resource.consoleproxy;

import java.io.IOException;
import java.io.OutputStreamWriter;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleProxyCmdHandler implements HttpHandler {
    private static final Logger s_logger = LoggerFactory.getLogger(ConsoleProxyCmdHandler.class);

    @Override
    public void handle(final HttpExchange t) throws IOException {
        try {
            Thread.currentThread().setName("Cmd Thread " + Thread.currentThread().getId() + " " + t.getRemoteAddress());
            s_logger.info("CmdHandler " + t.getRequestURI());
            doHandle(t);
        } finally {
            t.close();
        }
    }

    public void doHandle(final HttpExchange t) throws IOException {
        final String path = t.getRequestURI().getPath();
        final int i = path.indexOf("/", 1);
        final String cmd = path.substring(i + 1);
        s_logger.info("Get CMD request for " + cmd);
        if (cmd.equals("getstatus")) {
            final ConsoleProxyClientStatsCollector statsCollector = ConsoleProxy.getStatsCollector();

            final Headers hds = t.getResponseHeaders();
            hds.set("Content-Type", "text/plain");
            hds.set("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
            t.sendResponseHeaders(200, 0);
            final OutputStreamWriter os = new OutputStreamWriter(t.getResponseBody(), "UTF-8");
            statsCollector.getStatsReport(os);
            os.close();
        }
    }
}
