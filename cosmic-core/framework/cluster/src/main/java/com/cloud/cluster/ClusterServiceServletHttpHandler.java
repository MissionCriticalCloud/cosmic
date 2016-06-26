package com.cloud.cluster;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLDecoder;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusterServiceServletHttpHandler implements HttpRequestHandler {
    private static final Logger s_logger = LoggerFactory.getLogger(ClusterServiceServletHttpHandler.class);

    private final ClusterManager manager;

    public ClusterServiceServletHttpHandler(final ClusterManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(final HttpRequest request, final HttpResponse response, final HttpContext context) throws HttpException, IOException {

        try {
            if (s_logger.isTraceEnabled()) {
                s_logger.trace("Start Handling cluster HTTP request");
            }

            parseRequest(request);
            handleRequest(request, response);

            if (s_logger.isTraceEnabled()) {
                s_logger.trace("Handle cluster HTTP request done");
            }
        } catch (final Throwable e) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Exception " + e.toString());
            }

            try {
                writeResponse(response, HttpStatus.SC_INTERNAL_SERVER_ERROR, null);
            } catch (final Throwable e2) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Exception " + e2.toString());
                }
            }
        }
    }

    private void parseRequest(final HttpRequest request) throws IOException {
        if (request instanceof HttpEntityEnclosingRequest) {
            final HttpEntityEnclosingRequest entityRequest = (HttpEntityEnclosingRequest) request;

            final String body = EntityUtils.toString(entityRequest.getEntity());
            if (body != null) {
                final String[] paramArray = body.split("&");
                if (paramArray != null) {
                    for (final String paramEntry : paramArray) {
                        final String[] paramValue = paramEntry.split("=");
                        if (paramValue.length != 2) {
                            continue;
                        }

                        final String name = URLDecoder.decode(paramValue[0]);
                        final String value = URLDecoder.decode(paramValue[1]);

                        if (s_logger.isTraceEnabled()) {
                            s_logger.trace("Parsed request parameter " + name + "=" + value);
                        }
                        request.getParams().setParameter(name, value);
                    }
                }
            }
        }
    }

    protected void handleRequest(final HttpRequest req, final HttpResponse response) {
        final String method = (String) req.getParams().getParameter("method");

        int nMethod = RemoteMethodConstants.METHOD_UNKNOWN;
        String responseContent = null;
        try {
            if (method != null) {
                nMethod = Integer.parseInt(method);
            }

            switch (nMethod) {
                case RemoteMethodConstants.METHOD_DELIVER_PDU:
                    responseContent = handleDeliverPduMethodCall(req);
                    break;

                case RemoteMethodConstants.METHOD_PING:
                    responseContent = handlePingMethodCall(req);
                    break;

                case RemoteMethodConstants.METHOD_UNKNOWN:
                default:
                    assert (false);
                    s_logger.error("unrecognized method " + nMethod);
                    break;
            }
        } catch (final Throwable e) {
            s_logger.error("Unexpected exception when processing cluster service request : ", e);
        }

        if (responseContent != null) {
            if (s_logger.isTraceEnabled()) {
                s_logger.trace("Write reponse with HTTP OK " + responseContent);
            }

            writeResponse(response, HttpStatus.SC_OK, responseContent);
        } else {
            if (s_logger.isTraceEnabled()) {
                s_logger.trace("Write reponse with HTTP Bad request");
            }

            writeResponse(response, HttpStatus.SC_BAD_REQUEST, null);
        }
    }

    private void writeResponse(final HttpResponse response, final int statusCode, String content) {
        if (content == null) {
            content = "";
        }
        response.setStatusCode(statusCode);
        final BasicHttpEntity body = new BasicHttpEntity();
        body.setContentType("text/html; charset=UTF-8");

        final byte[] bodyData = content.getBytes();
        body.setContent(new ByteArrayInputStream(bodyData));
        body.setContentLength(bodyData.length);
        response.setEntity(body);
    }

    private String handleDeliverPduMethodCall(final HttpRequest req) {

        final String pduSeq = (String) req.getParams().getParameter("pduSeq");
        final String pduAckSeq = (String) req.getParams().getParameter("pduAckSeq");
        final String sourcePeer = (String) req.getParams().getParameter("sourcePeer");
        final String destPeer = (String) req.getParams().getParameter("destPeer");
        final String agentId = (String) req.getParams().getParameter("agentId");
        final String gsonPackage = (String) req.getParams().getParameter("gsonPackage");
        final String stopOnError = (String) req.getParams().getParameter("stopOnError");
        final String pduType = (String) req.getParams().getParameter("pduType");

        final ClusterServicePdu pdu = new ClusterServicePdu();
        pdu.setSourcePeer(sourcePeer);
        pdu.setDestPeer(destPeer);
        pdu.setAgentId(Long.parseLong(agentId));
        pdu.setSequenceId(Long.parseLong(pduSeq));
        pdu.setAckSequenceId(Long.parseLong(pduAckSeq));
        pdu.setJsonPackage(gsonPackage);
        pdu.setStopOnError("1".equals(stopOnError));
        pdu.setPduType(Integer.parseInt(pduType));

        manager.OnReceiveClusterServicePdu(pdu);
        return "true";
    }

    private String handlePingMethodCall(final HttpRequest req) {
        final String callingPeer = (String) req.getParams().getParameter("callingPeer");

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Handle ping request from " + callingPeer);
        }

        return "true";
    }
}
