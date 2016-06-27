package com.cloud.hypervisor.ovm3.objects;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.TimeZone;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.TimingOutCallback;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcClientRequestImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Connection extends XmlRpcClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(Connection.class);
    private final XmlRpcClientConfigImpl xmlClientConfig = new XmlRpcClientConfigImpl();
    private final Boolean hostUseSsl = false;
    private final String cert = "";
    private final String key = "";
    /* default to 20 mins ? */
    private final Integer timeoutMs = 1200;
    private final Integer timeoutS = timeoutMs * 1000;
    private XmlRpcClient xmlClient;
    private String hostUser = null;
    private String hostPass = null;
    private String hostIp;
    private String hostName;
    private Integer hostPort = 8899;

    public Connection() {
    }

    public Connection(final String ip, final Integer port, final String username, final String password) {
        hostIp = ip;
        hostPort = port;
        hostUser = username;
        hostPass = password;
        xmlClient = setupXmlClient();
    }

    private XmlRpcClient setupXmlClient() {
        final XmlRpcClient client = new XmlRpcClient();

        final URL url;
        try {
      /* TODO: should add SSL checking here! */
            String prot = "http";
            if (hostUseSsl) {
                prot = "https";
            }
            url = new URL(prot + "://" + hostIp + ":" + hostPort.toString());
            xmlClientConfig.setTimeZone(TimeZone.getTimeZone("UTC"));
            xmlClientConfig.setServerURL(url);
      /* disable, we use asyncexecute to control timeout */
            xmlClientConfig.setReplyTimeout(0);
      /* default to 60 secs */
            xmlClientConfig.setConnectionTimeout(60000);
      /* reply time is 5 mins */
            xmlClientConfig.setReplyTimeout(60 * 15000);
            if (hostUser != null && hostPass != null) {
                LOGGER.debug("Setting username " + hostUser);
                xmlClientConfig.setBasicUserName(hostUser);
                xmlClientConfig.setBasicPassword(hostPass);
            }
            xmlClientConfig.setXmlRpcServer(null);
            client.setConfig(xmlClientConfig);
            client.setTypeFactory(new RpcTypeFactory(client));
        } catch (final MalformedURLException e) {
            LOGGER.info("Incorrect URL: ", e);
        }
        return client;
    }

    public Connection(final String ip, final String username, final String password) {
        hostIp = ip;
        hostUser = username;
        hostPass = password;
        xmlClient = setupXmlClient();
    }

    public Object call(final String method, final List<?> params) throws XmlRpcException {
        return callTimeoutInSec(method, params, timeoutS);
    }

    public Object callTimeoutInSec(final String method, final List<?> params, final int timeout)
            throws XmlRpcException {
        return callTimeoutInSec(method, params, timeout, true);
    }

    public Object callTimeoutInSec(final String method, final List<?> params, final int timeout,
                                   final boolean debug) throws XmlRpcException {
        final TimingOutCallback callback = new TimingOutCallback(timeout * 1000);
        if (debug) {
            LOGGER.debug("Call Ovm3 agent " + hostName + "(" + hostIp + "): " + method
                    + " with " + params);
        }
        final long startTime = System.currentTimeMillis();
        try {
      /* returns actual xml */
            final XmlRpcClientRequestImpl req = new XmlRpcClientRequestImpl(
                    xmlClient.getClientConfig(), method, params);
            xmlClient.executeAsync(req, callback);
            return callback.waitForResponse();
        } catch (final TimingOutCallback.TimeoutException e) {
            LOGGER.info("Timeout: ", e);
            throw new XmlRpcException(e.getMessage());
        } catch (final XmlRpcException e) {
            LOGGER.info("XML RPC Exception occured: ", e);
            throw e;
        } catch (final RuntimeException e) {
            LOGGER.info("Runtime Exception: ", e);
            throw new XmlRpcException(e.getMessage());
        } catch (final Throwable e) {
            LOGGER.error("Holy crap batman!: ", e);
            throw new XmlRpcException(e.getMessage(), e);
        } finally {
            final long endTime = System.currentTimeMillis();
      /* in seconds */
            final float during = (endTime - startTime) / (float) 1000;
            LOGGER.debug("Ovm3 call " + method + " finished in " + during
                    + " secs, on " + hostIp + ":" + hostPort);
        }
    }

    public Object call(final String method, final List<?> params, final boolean debug)
            throws XmlRpcException {
        return callTimeoutInSec(method, params, timeoutS, debug);
    }

    public String getIp() {
        return hostIp;
    }

    public void setIp(final String agentIp) {
        hostIp = agentIp;
    }

    public Integer getPort() {
        return hostPort;
    }

    public String getUserName() {
        return hostUser;
    }

    public void setUserName(final String userName) {
        hostUser = userName;
    }

    public String getPassword() {
        return hostPass;
    }

    public void setPassword(final String agentPassword) {
        hostPass = agentPassword;
    }

    public Boolean getUseSsl() {
        return hostUseSsl;
    }

    public String getCert() {
        return cert;
    }

    public String getKey() {
        return key;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(final String hostName) {
        this.hostName = hostName;
    }
}
