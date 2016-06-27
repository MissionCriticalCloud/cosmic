package com.cloud.consoleproxy;

import com.cloud.consoleproxy.util.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ConsoleProxyCmdHandler implements HttpHandler {
    private static final Logger s_logger = Logger.getLogger(ConsoleProxyCmdHandler.class);

    @Override
    public void handle(final HttpExchange t) throws IOException {
        try {
            Thread.currentThread().setName("Cmd Thread " + Thread.currentThread().getId() + " " + t.getRemoteAddress());
            s_logger.info("CmdHandler " + t.getRequestURI());
            doHandle(t);
        } catch (final Exception e) {
            s_logger.error(e.toString(), e);
            final String response = "Not found";
            t.sendResponseHeaders(404, response.length());
            final OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } catch (final OutOfMemoryError e) {
            s_logger.error("Unrecoverable OutOfMemory Error, exit and let it be re-launched");
            System.exit(1);
        } catch (final Throwable e) {
            s_logger.error(e.toString(), e);
        } finally {
            t.close();
        }
    }

    public void doHandle(final HttpExchange t) throws Exception {
        final String path = t.getRequestURI().getPath();
        final int i = path.indexOf("/", 1);
        final String cmd = path.substring(i + 1);
        s_logger.info("Get CMD request for " + cmd);
        if (cmd.equals("getstatus")) {
            final ConsoleProxyClientStatsCollector statsCollector = ConsoleProxy.getStatsCollector();

            final Headers hds = t.getResponseHeaders();
            hds.set("Content-Type", "text/plain");
            t.sendResponseHeaders(200, 0);
            final OutputStreamWriter os = new OutputStreamWriter(t.getResponseBody(), "UTF-8");
            statsCollector.getStatsReport(os);
            os.close();
        }
    }
}
