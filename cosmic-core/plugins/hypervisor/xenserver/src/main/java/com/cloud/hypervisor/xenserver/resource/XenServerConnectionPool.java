package com.cloud.hypervisor.xenserver.resource;

import com.cloud.utils.NumbersUtil;
import com.cloud.utils.PropertiesUtil;
import com.cloud.utils.exception.CloudRuntimeException;
import org.apache.cloudstack.utils.security.SSLUtils;
import org.apache.cloudstack.utils.security.SecureSSLSocketFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;

import com.xensource.xenapi.APIVersion;
import com.xensource.xenapi.Connection;
import com.xensource.xenapi.Host;
import com.xensource.xenapi.Pool;
import com.xensource.xenapi.Session;
import com.xensource.xenapi.Types;
import com.xensource.xenapi.Types.BadServerResponse;
import com.xensource.xenapi.Types.XenAPIException;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XenServerConnectionPool {
    private static final Logger s_logger = LoggerFactory.getLogger(XenServerConnectionPool.class);
    private static final XenServerConnectionPool s_instance = new XenServerConnectionPool();
    protected static long s_sleepOnError = 10 * 1000; // in ms

    static {
        final File file = PropertiesUtil.findConfigFile("environment.properties");
        if (file == null) {
            s_logger.debug("Unable to find environment.properties");
        } else {
            try {
                final Properties props = PropertiesUtil.loadFromFile(file);
                final String search = props.getProperty("sleep.interval.on.error");
                if (search != null) {
                    s_sleepOnError = NumbersUtil.parseInterval(search, 10) * 1000;
                }
                s_logger.info("XenServer Connection Pool Configs: sleep.interval.on.error=" + s_sleepOnError);
            } catch (final FileNotFoundException e) {
                s_logger.debug("File is not found", e);
            } catch (final IOException e) {
                s_logger.debug("IO Exception while reading file", e);
            }
        }
        try {
            final javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
            final javax.net.ssl.TrustManager tm = new TrustAllManager();
            trustAllCerts[0] = tm;
            final javax.net.ssl.SSLContext sc = SSLUtils.getSSLContext();
            sc.init(null, trustAllCerts, null);
            javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(new SecureSSLSocketFactory(sc));
            final HostnameVerifier hv = new HostnameVerifier() {
                @Override
                public boolean verify(final String hostName, final SSLSession session) {
                    return true;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(hv);
        } catch (final NoSuchAlgorithmException e) {
            //ignore this
        } catch (final KeyManagementException e) {
            s_logger.debug("Init SSLContext failed ", e);
        }
    }

    protected HashMap<String /* poolUuid */, XenServerConnection> _conns = new HashMap<>();
    protected int _retries;
    protected int _interval;
    protected int _connWait = 5;

    protected XenServerConnectionPool() {
        _retries = 1;
        _interval = 3;
    }

    static void forceSleep(final long sec) {
        final long firetime = System.currentTimeMillis() + (sec * 1000);
        long msec = sec * 1000;
        while (true) {
            if (msec < 100) {
                break;
            }
            try {
                Thread.sleep(msec);
                return;
            } catch (final InterruptedException e) {
                msec = firetime - System.currentTimeMillis();
            }
        }
    }

    static public Pool.Record getPoolRecord(final Connection conn) throws XmlRpcException, XenAPIException {
        final Map<Pool, Pool.Record> pools = Pool.getAllRecords(conn);
        assert pools.size() == 1 : "Pool size is not one....hmmm....wth? " + pools.size();

        return pools.values().iterator().next();
    }

    public static XenServerConnectionPool getInstance() {
        return s_instance;
    }

    public Connection getConnect(final String ip, final String username, final Queue<String> password) {
        Connection conn = new Connection(getURL(ip), 10, _connWait);
        try {
            loginWithPassword(conn, username, password, APIVersion.latest().toString());
        } catch (final Types.HostIsSlave e) {
            final String maddress = e.masterIPAddress;
            conn = new Connection(getURL(maddress), 10, _connWait);
            try {
                loginWithPassword(conn, username, password, APIVersion.latest().toString());
            } catch (final Exception e1) {
                final String msg = "Unable to create master connection to host(" + maddress + ") , due to " + e1.toString();
                s_logger.debug(msg);
                throw new CloudRuntimeException(msg, e1);
            }
        } catch (final Exception e) {
            final String msg = "Unable to create master connection to host(" + ip + ") , due to " + e.toString();
            s_logger.debug(msg);
            throw new CloudRuntimeException(msg, e);
        }
        return conn;
    }

    public URL getURL(final String ip) {
        try {
            return new URL("https://" + ip);
        } catch (final Exception e) {
            final String msg = "Unable to convert IP " + ip + " to URL due to " + e.toString();
            if (s_logger.isDebugEnabled()) {
                s_logger.debug(msg);
            }
            throw new CloudRuntimeException(msg, e);
        }
    }

    protected Session loginWithPassword(final Connection conn, final String username, final Queue<String> password, final String version) throws BadServerResponse, XenAPIException,
            XmlRpcException {
        Session s = null;
        boolean logged_in = false;
        Exception ex = null;
        while (!logged_in) {
            try {
                s = Session.loginWithPassword(conn, username, password.peek(), APIVersion.latest().toString());
                logged_in = true;
            } catch (final BadServerResponse e) {
                logged_in = false;
                ex = e;
            } catch (final XenAPIException e) {
                logged_in = false;
                ex = e;
            } catch (final XmlRpcException e) {
                logged_in = false;
                ex = e;
            }

            if (logged_in && conn != null) {
                break;
            } else {
                if (password.size() > 1) {
                    password.remove();
                    continue;
                } else {
                    // the last password did not work leave it and flag error
                    if (ex instanceof BadServerResponse) {
                        throw (BadServerResponse) ex;
                    } else if (ex instanceof XmlRpcException) {
                        throw (XmlRpcException) ex;
                    } else if (ex instanceof Types.SessionAuthenticationFailed) {
                        throw (Types.SessionAuthenticationFailed) ex;
                    } else if (ex instanceof XenAPIException) {
                        throw (XenAPIException) ex;
                    }
                }
            }
        }
        return s;
    }

    public Connection connect(final String hostUuid, final String poolUuid, final String ipAddress,
                              final String username, final Queue<String> password, final int wait) {
        XenServerConnection mConn = null;
        if (hostUuid == null || poolUuid == null || ipAddress == null || username == null || password == null) {
            final String msg = "Connect some parameter are null hostUuid:" + hostUuid + " ,poolUuid:" + poolUuid
                    + " ,ipAddress:" + ipAddress;
            s_logger.debug(msg);
            throw new CloudRuntimeException(msg);
        }
        synchronized (poolUuid.intern()) {
            mConn = getConnect(poolUuid);
            if (mConn != null) {
                try {
                    final Host host = Host.getByUuid(mConn, hostUuid);
                    if (!host.getEnabled(mConn)) {
                        final String msg = "Cannot connect this host " + ipAddress + " due to the host is not enabled";
                        s_logger.debug(msg);
                        if (mConn.getIp().equalsIgnoreCase(ipAddress)) {
                            removeConnect(poolUuid);
                            mConn = null;
                        }
                        throw new CloudRuntimeException(msg);
                    }
                    return mConn;
                } catch (final CloudRuntimeException e) {
                    throw e;
                } catch (final Exception e) {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("connect through IP(" + mConn.getIp() + " for pool(" + poolUuid + ") is broken due to " + e.toString());
                    }
                    removeConnect(poolUuid);
                    mConn = null;
                }
            }

            if (mConn == null) {
                try {
                    final Connection conn = new Connection(getURL(ipAddress), 5, _connWait);
                    final Session sess = loginWithPassword(conn, username, password, APIVersion.latest().toString());
                    final Host host = sess.getThisHost(conn);
                    final Boolean hostenabled = host.getEnabled(conn);
                    if (sess != null) {
                        try {
                            Session.logout(conn);
                        } catch (final Exception e) {
                            s_logger.debug("Caught exception during logout", e);
                        }
                        conn.dispose();
                    }
                    if (!hostenabled) {
                        final String msg = "Unable to create master connection, due to master Host " + ipAddress + " is not enabled";
                        s_logger.debug(msg);
                        throw new CloudRuntimeException(msg);
                    }
                    mConn = new XenServerConnection(getURL(ipAddress), ipAddress, username, password, _retries, _interval, wait, _connWait);
                    loginWithPassword(mConn, username, password, APIVersion.latest().toString());
                } catch (final Types.HostIsSlave e) {
                    final String maddress = e.masterIPAddress;
                    mConn = new XenServerConnection(getURL(maddress), maddress, username, password, _retries, _interval, wait, _connWait);
                    try {
                        final Session session = loginWithPassword(mConn, username, password, APIVersion.latest().toString());
                        final Host host = session.getThisHost(mConn);
                        if (!host.getEnabled(mConn)) {
                            final String msg = "Unable to create master connection, due to master Host " + maddress + " is not enabled";
                            s_logger.debug(msg);
                            throw new CloudRuntimeException(msg);
                        }
                    } catch (final Exception e1) {
                        final String msg = "Unable to create master connection to host(" + maddress + ") , due to " + e1.toString();
                        s_logger.debug(msg);
                        throw new CloudRuntimeException(msg, e1);
                    }
                } catch (final CloudRuntimeException e) {
                    throw e;
                } catch (final Exception e) {
                    final String msg = "Unable to create master connection to host(" + ipAddress + ") , due to " + e.toString();
                    s_logger.debug(msg);
                    throw new CloudRuntimeException(msg, e);
                }
                addConnect(poolUuid, mConn);
            }
        }
        return mConn;
    }

    private XenServerConnection getConnect(final String poolUuid) {
        if (poolUuid == null) {
            return null;
        }
        synchronized (_conns) {
            return _conns.get(poolUuid);
        }
    }

    private void removeConnect(final String poolUuid) {
        if (poolUuid == null) {
            return;
        }
        XenServerConnection conn = null;
        synchronized (_conns) {
            conn = _conns.remove(poolUuid);
        }
        if (conn != null) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Remove master connection through " + conn.getIp() + " for pool(" + conn.getPoolUuid() + ")");
            }
        }
    }

    private void addConnect(final String poolUuid, final XenServerConnection conn) {
        if (poolUuid == null) {
            return;
        }
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Add master connection through " + conn.getIp() + " for pool(" + conn.getPoolUuid() + ")");
        }
        synchronized (_conns) {
            _conns.put(poolUuid, conn);
        }
    }

    protected Session slaveLocalLoginWithPassword(final Connection conn, final String username, final Queue<String> password) throws BadServerResponse, XenAPIException,
            XmlRpcException {
        Session s = null;
        boolean logged_in = false;
        Exception ex = null;
        while (!logged_in) {
            try {
                s = Session.slaveLocalLoginWithPassword(conn, username, password.peek());
                logged_in = true;
            } catch (final BadServerResponse e) {
                logged_in = false;
                ex = e;
            } catch (final XenAPIException e) {
                logged_in = false;
                ex = e;
            } catch (final XmlRpcException e) {
                logged_in = false;
                ex = e;
            }
            if (logged_in && conn != null) {
                break;
            } else {
                if (password.size() > 1) {
                    password.remove();
                    continue;
                } else {
                    // the last password did not work leave it and flag error
                    if (ex instanceof BadServerResponse) {
                        throw (BadServerResponse) ex;
                    } else if (ex instanceof XmlRpcException) {
                        throw (XmlRpcException) ex;
                    } else if (ex instanceof Types.SessionAuthenticationFailed) {
                        throw (Types.SessionAuthenticationFailed) ex;
                    } else if (ex instanceof XenAPIException) {
                        throw (XenAPIException) ex;
                    }
                    break;
                }
            }
        }
        return s;
    }

    protected void join(final Connection conn, final String masterIp, final String username, final Queue<String> password) throws BadServerResponse, XenAPIException,
            XmlRpcException,
            Types.JoiningHostCannotContainSharedSrs {

        boolean logged_in = false;
        Exception ex = null;
        while (!logged_in) {
            try {
                Pool.join(conn, masterIp, username, password.peek());
                logged_in = true;
            } catch (final BadServerResponse e) {
                logged_in = false;
                ex = e;
            } catch (final XenAPIException e) {
                logged_in = false;
                ex = e;
            } catch (final XmlRpcException e) {
                logged_in = false;
                ex = e;
            }
            if (logged_in && conn != null) {
                break;
            } else {
                if (password.size() > 1) {
                    password.remove();
                    continue;
                } else {
                    // the last password did not work leave it and flag error
                    if (ex instanceof BadServerResponse) {
                        throw (BadServerResponse) ex;
                    } else if (ex instanceof XmlRpcException) {
                        throw (XmlRpcException) ex;
                    } else if (ex instanceof Types.SessionAuthenticationFailed) {
                        throw (Types.SessionAuthenticationFailed) ex;
                    } else if (ex instanceof XenAPIException) {
                        throw (XenAPIException) ex;
                    }
                    break;
                }
            }
        }
    }

    public static class TrustAllManager implements javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager {
        public boolean isServerTrusted(final java.security.cert.X509Certificate[] certs) {
            return true;
        }

        public boolean isClientTrusted(final java.security.cert.X509Certificate[] certs) {
            return true;
        }

        @Override
        public void checkClientTrusted(final java.security.cert.X509Certificate[] certs, final String authType) throws java.security.cert.CertificateException {
            return;
        }

        @Override
        public void checkServerTrusted(final java.security.cert.X509Certificate[] certs, final String authType) throws java.security.cert.CertificateException {
            return;
        }

        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

    public class XenServerConnection extends Connection {
        long _interval;
        int _retries;
        String _ip;
        String _username;
        Queue<String> _password;
        String _poolUuid;

        public XenServerConnection(final URL url, final String ip, final String username, final Queue<String> password, final int retries, final int interval, final int wait,
                                   final int connwait) {
            super(url, wait, connwait);
            _ip = ip;
            _retries = retries;
            _username = username;
            _password = password;
            _interval = (long) interval * 1000;
        }

        public String getPoolUuid() {
            return _poolUuid;
        }

        public String getUsername() {
            return _username;
        }

        public Queue<String> getPassword() {
            return _password;
        }

        public String getIp() {
            return _ip;
        }

        @Override
        protected Map dispatch(final String methodcall, final Object[] methodparams) throws XmlRpcException, XenAPIException {
            if (methodcall.equals("session.local_logout")
                    || methodcall.equals("session.slave_local_login_with_password")
                    || methodcall.equals("session.logout")
                    || methodcall.equals("session.login_with_password")) {
                return super.dispatch(methodcall, methodparams);
            }

            try {
                return super.dispatch(methodcall, methodparams);
            } catch (final Types.SessionInvalid e) {
                s_logger.debug("Session is invalid for method: " + methodcall + " due to " + e.toString());
                removeConnect(_poolUuid);
                throw e;
            } catch (final XmlRpcClientException e) {
                s_logger.debug("XmlRpcClientException for method: " + methodcall + " due to " + e.toString());
                removeConnect(_poolUuid);
                throw e;
            } catch (final XmlRpcException e) {
                s_logger.debug("XmlRpcException for method: " + methodcall + " due to " + e.toString());
                removeConnect(_poolUuid);
                throw e;
            } catch (final Types.HostIsSlave e) {
                s_logger.debug("HostIsSlave Exception for method: " + methodcall + " due to " + e.toString());
                removeConnect(_poolUuid);
                throw e;
            }
        }
    }
}
