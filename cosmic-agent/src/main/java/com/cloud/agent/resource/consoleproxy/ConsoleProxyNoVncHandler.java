package com.cloud.agent.resource.consoleproxy;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Map;
import java.util.Scanner;

public class ConsoleProxyNoVncHandler implements HttpHandler {
    private static final Logger s_logger = LoggerFactory.getLogger(ConsoleProxyNoVncHandler.class);

    @Override
    public void handle(final HttpExchange httpExchange) throws IOException {
        try {
            if (s_logger.isTraceEnabled()) {
                s_logger.trace("NoVNCHandler " + httpExchange.getRequestURI());
            }

            final long startTick = System.currentTimeMillis();

            doHandle(httpExchange);

            if (s_logger.isTraceEnabled()) {
                s_logger.trace(httpExchange.getRequestURI() + " process time " + (System.currentTimeMillis() - startTick) + " ms");
            }
        } catch (final IllegalArgumentException e) {
            s_logger.warn("Exception, ", e);
            httpExchange.sendResponseHeaders(400, -1);     // bad request
        } finally {
            httpExchange.close();
        }
    }

    private void doHandle(final HttpExchange httpExchange) throws IllegalArgumentException, IOException {
        String queries = httpExchange.getRequestURI().getQuery();
        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Handle WebSocket Console request " + queries);
        }

        if (queries != null) {
            Map<String, String> queryMap = ConsoleProxyHttpHandlerHelper.getQueryMap(queries);
            String host = queryMap.get("host");
            String portStr = queryMap.get("port");
            String tag = queryMap.get("tag");
            ConsoleProxyClientParam param = new ConsoleProxyClientParam();
            param.setClientHostAddress(host);
            param.setClientHostPort(portStr != null ? Integer.parseInt(portStr) : 80);
            param.setClientTag(tag);
            ConsoleProxy.removeViewer(param);
        }

        sendResponse(httpExchange, "text/html", getFile("noVNC/start.html"));
    }

    private void sendResponse(final HttpExchange httpExchange, final String contentType, final String response) throws IOException {
        final Headers headers = httpExchange.getResponseHeaders();
        headers.set("Content-Type", contentType);

        httpExchange.sendResponseHeaders(200, response.length());
        final OutputStream os = httpExchange.getResponseBody();
        try {
            os.write(response.getBytes());
        } finally {
            os.close();
        }
    }

    private String getFile(String fileName) {
        File file;
        StringBuilder result = new StringBuilder();
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(fileName);
        if (resource!=null) {
            file = new File(resource.getFile());

            try (Scanner scanner = new Scanner(file)) {

                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    result.append(line);
                }
            } catch (IOException e) {
                s_logger.error("IO exception occurred", e);
            }
        }
        return result.toString();
    }
}
