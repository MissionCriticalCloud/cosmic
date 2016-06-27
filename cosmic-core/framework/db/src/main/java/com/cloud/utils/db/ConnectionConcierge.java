package com.cloud.utils.db;

import com.cloud.utils.concurrency.NamedThreadFactory;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.mgmt.JmxUtil;
import org.apache.cloudstack.managed.context.ManagedContextRunnable;

import javax.management.StandardMBean;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ConnectionConcierge keeps stand alone database connections alive.  This is
 * needs someone to keep that database connection from being garbage collected
 */
public class ConnectionConcierge {

    static final Logger s_logger = LoggerFactory.getLogger(ConnectionConcierge.class);

    static final ConnectionConciergeManager s_mgr = new ConnectionConciergeManager();

    Connection _conn;
    String _name;
    boolean _keepAlive;
    boolean _autoCommit;
    int _isolationLevel;
    int _holdability;

    public ConnectionConcierge(final String name, final Connection conn, final boolean keepAlive) {
        _name = name + s_mgr.getNextId();
        _keepAlive = keepAlive;
        try {
            _autoCommit = conn.getAutoCommit();
            _isolationLevel = conn.getTransactionIsolation();
            _holdability = conn.getHoldability();
        } catch (final SQLException e) {
            throw new CloudRuntimeException("Unable to get information from the connection object", e);
        }
        reset(conn);
    }

    public void reset(final Connection conn) {
        try {
            release();
        } catch (final Throwable th) {
            s_logger.error("Unable to release a connection", th);
        }
        _conn = conn;
        try {
            _conn.setAutoCommit(_autoCommit);
            _conn.setHoldability(_holdability);
            _conn.setTransactionIsolation(_isolationLevel);
        } catch (final SQLException e) {
            s_logger.error("Unable to release a connection", e);
        }
        s_mgr.register(_name, this);
        s_logger.debug("Registering a database connection for " + _name);
    }

    public void release() {
        s_mgr.unregister(_name);
        try {
            if (_conn != null) {
                _conn.close();
            }
            _conn = null;
        } catch (final SQLException e) {
            throw new CloudRuntimeException("Problem in closing a connection", e);
        }
    }

    public final Connection conn() {
        return _conn;
    }

    @Override
    protected void finalize() throws Exception {
        if (_conn != null) {
            release();
        }
    }

    public boolean keepAlive() {
        return _keepAlive;
    }

    protected static class ConnectionConciergeManager extends StandardMBean implements ConnectionConciergeMBean {
        final ConcurrentHashMap<String, ConnectionConcierge> _conns = new ConcurrentHashMap<>();
        final AtomicInteger _idGenerator = new AtomicInteger();
        ScheduledExecutorService _executor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("ConnectionKeeper"));

        ConnectionConciergeManager() {
            super(ConnectionConciergeMBean.class, false);
            resetKeepAliveTask(20);
            try {
                JmxUtil.registerMBean("DB Connections", "DB Connections", this);
            } catch (final Exception e) {
                s_logger.error("Unable to register mbean", e);
            }
        }

        protected String testValidity(final String name, final Connection conn) {
            if (conn != null) {
                synchronized (conn) {
                    try (PreparedStatement pstmt = conn.prepareStatement("SELECT 1")) {
                        pstmt.executeQuery();
                    } catch (final Throwable th) {
                        s_logger.error("Unable to keep the db connection for " + name, th);
                        return th.toString();
                    }
                }
            }
            return null;
        }

        public Integer getNextId() {
            return _idGenerator.incrementAndGet();
        }

        public void register(final String name, final ConnectionConcierge concierge) {
            _conns.put(name, concierge);
        }

        public void unregister(final String name) {
            _conns.remove(name);
        }

        @Override
        public List<String> testValidityOfConnections() {
            final ArrayList<String> results = new ArrayList<>(_conns.size());
            for (final Map.Entry<String, ConnectionConcierge> entry : _conns.entrySet()) {
                final String result = testValidity(entry.getKey(), entry.getValue().conn());
                results.add(entry.getKey() + "=" + (result == null ? "OK" : result));
            }
            return results;
        }

        @Override
        public String resetConnection(final String name) {
            final ConnectionConcierge concierge = _conns.get(name);
            if (concierge == null) {
                return "Not Found";
            }

            final Connection conn = TransactionLegacy.getStandaloneConnection();
            if (conn == null) {
                return "Unable to get anotehr db connection";
            }

            concierge.reset(conn);
            return "Done";
        }

        @Override
        public String resetKeepAliveTask(final int seconds) {
            if (_executor != null) {
                try {
                    _executor.shutdown();
                } catch (final Exception e) {
                    s_logger.error("Unable to shutdown executor", e);
                }
            }

            _executor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("ConnectionConcierge"));

            _executor.scheduleAtFixedRate(new ManagedContextRunnable() {
                @Override
                protected void runInContext() {
                    s_logger.trace("connection concierge keep alive task");
                    for (final Map.Entry<String, ConnectionConcierge> entry : _conns.entrySet()) {
                        final ConnectionConcierge concierge = entry.getValue();
                        if (concierge.keepAlive()) {
                            testValidity(entry.getKey(), entry.getValue().conn());
                        }
                    }
                }
            }, 0, seconds, TimeUnit.SECONDS);

            return "As you wish.";
        }

        @Override
        public List<String> getConnectionsNotPooled() {
            return new ArrayList<>(_conns.keySet());
        }
    }
}
