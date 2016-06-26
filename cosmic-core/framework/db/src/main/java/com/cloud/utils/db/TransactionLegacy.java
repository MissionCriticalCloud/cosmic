package com.cloud.utils.db;

import com.cloud.utils.Pair;
import com.cloud.utils.PropertiesUtil;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.mgmt.JmxUtil;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.KeyedObjectPoolFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.StackKeyedObjectPoolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transaction abstracts away the Connection object in JDBC. It allows the following things that the Connection object
 * does not.
 * <p>
 * 1. Transaction can be started at an entry point and whether the DB actions should be auto-commit or not determined at
 * that point. 2. DB Connection is allocated only when it is needed. 3. Code does not need to know if a transaction has
 * been started or not. It just starts/ends a transaction and we resolve it correctly with the previous actions.
 * <p>
 * Note that this class is not synchronous but it doesn't need to be because it is stored with TLS and is one per
 * thread. Use appropriately.
 */
public class TransactionLegacy implements Closeable {
    public static final short CLOUD_DB = 0;
    public static final short USAGE_DB = 1;
    public static final short CONNECTED_DB = -1;
    private static final Logger s_logger = LoggerFactory.getLogger(Transaction.class.getName() + "." + "Transaction");
    private static final Logger s_stmtLogger = LoggerFactory.getLogger(Transaction.class.getName() + "." + "Statement");
    private static final Logger s_lockLogger = LoggerFactory.getLogger(Transaction.class.getName() + "." + "Lock");
    private static final Logger s_connLogger = LoggerFactory.getLogger(Transaction.class.getName() + "." + "Connection");
    private static final String DB_CONNECTION_SCHEME = "jdbc:mariadb";
    private static final ThreadLocal<TransactionLegacy> tls = new ThreadLocal<>();
    private static final String START_TXN = "start_txn";
    private static final String CURRENT_TXN = "current_txn";
    private static final String CREATE_TXN = "create_txn";
    private static final String CREATE_CONN = "create_conn";
    private static final String STATEMENT = "statement";
    private static final String ATTACHMENT = "attachment";
    private static final AtomicLong s_id = new AtomicLong();
    private static final TransactionMBeanImpl s_mbean = new TransactionMBeanImpl();
    private static DataSource s_ds;
    private static DataSource s_usageDS;
    private static boolean s_dbHAEnabled;

    static {
        try {
            JmxUtil.registerMBean("Transaction", "Transaction", s_mbean);
        } catch (final Exception e) {
            s_logger.error("Unable to register mbean for transaction", e);
        }
    }

    static {
        // Initialize with assumed db.properties file
        initDataSource(DbProperties.getDbProperties());
    }

    private final LinkedList<StackElement> _stack;
    private final LinkedList<Pair<String, Long>> _lockTimes = new LinkedList<>();
    private long _id;
    private String _name;
    private Connection _conn;
    private boolean _txn;
    private short _dbId;
    private long _txnTime;
    private Statement _stmt;
    private String _creator;

    private TransactionLegacy(final String name, final boolean forLocking, final short databaseId) {
        _name = name;
        _conn = null;
        _stack = new LinkedList<>();
        _txn = false;
        _dbId = databaseId;
        _id = s_id.incrementAndGet();
        _creator = Thread.currentThread().getName();
    }

    private TransactionLegacy() {
        _name = null;
        _conn = null;
        _stack = null;
        _txn = false;
        _dbId = -1;
    }

    public static TransactionLegacy currentTxn() {
        return currentTxn(true);
    }

    protected static TransactionLegacy currentTxn(final boolean check) {
        final TransactionLegacy txn = tls.get();
        if (check) {
            assert txn != null : "No Transaction on stack.  Did you mark the method with @DB?";
        }
        return txn;
    }

    public static TransactionLegacy open(final short databaseId) {
        String name = buildName();
        if (name == null) {
            name = CURRENT_TXN;
        }
        return open(name, databaseId, true);
    }

    public static TransactionLegacy open(final String name) {
        return open(name, TransactionLegacy.CLOUD_DB, false);
    }

    public static TransactionLegacy open(final String name, final short databaseId, final boolean forceDbChange) {
        TransactionLegacy txn = tls.get();
        boolean isNew = false;
        if (txn == null) {
            if (s_logger.isTraceEnabled()) {
                s_logger.trace("Creating the transaction: " + name);
            }
            txn = new TransactionLegacy(name, false, databaseId);
            tls.set(txn);
            isNew = true;
        } else if (forceDbChange) {
            final short currentDbId = txn.getDatabaseId();
            if (currentDbId != databaseId) {
                // we need to end the current transaction and switch databases
                txn.close(txn.getName());

                txn = new TransactionLegacy(name, false, databaseId);
                tls.set(txn);
                isNew = true;
            }
        }

        txn.takeOver(name, false);
        if (isNew) {
            s_mbean.addTransaction(txn);
        }
        return txn;
    }

    public static Connection getStandaloneConnection() {
        try {
            return getStandaloneConnectionWithException();
        } catch (final SQLException e) {
            s_logger.error("Unexpected exception: ", e);
            return null;
        }
    }

    public static Connection getStandaloneConnectionWithException() throws SQLException {
        final Connection conn = s_ds.getConnection();
        if (s_connLogger.isTraceEnabled()) {
            s_connLogger.trace("Retrieving a standalone connection: dbconn" + System.identityHashCode(conn));
        }
        return conn;
    }

    public static Connection getStandaloneUsageConnection() {
        try {
            final Connection conn = s_usageDS.getConnection();
            if (s_connLogger.isTraceEnabled()) {
                s_connLogger.trace("Retrieving a standalone connection for usage: dbconn" + System.identityHashCode(conn));
            }
            return conn;
        } catch (final SQLException e) {
            s_logger.warn("Unexpected exception: ", e);
            return null;
        }
    }

    public static void attachToTxn(final TransactionAttachment value) {
        final TransactionLegacy txn = tls.get();
        assert txn != null && txn.peekInStack(
                CURRENT_TXN) != null : "Come on....how can we attach something to the transaction if you haven't started it?";

        txn.attach(value);
    }

    protected StackElement peekInStack(final Object obj) {
        final Iterator<StackElement> it = _stack.iterator();
        while (it.hasNext()) {
            final StackElement next = it.next();
            if (next.type == obj) {
                return next;
            }
        }
        return null;
    }

    protected void attach(final TransactionAttachment value) {
        _stack.push(new StackElement(ATTACHMENT, value));
    }

    public static TransactionAttachment detachFromTxn(final String name) {
        final TransactionLegacy txn = tls.get();
        assert txn != null : "No Transaction in TLS";
        return txn.detach(name);
    }

    protected TransactionAttachment detach(final String name) {
        final Iterator<StackElement> it = _stack.descendingIterator();
        while (it.hasNext()) {
            final StackElement element = it.next();
            if (element.type == ATTACHMENT) {
                final TransactionAttachment att = (TransactionAttachment) element.ref;
                if (name.equals(att.getName())) {
                    it.remove();
                    return att;
                }
            }
        }
        assert false : "Are you sure you attached this: " + name;
        return null;
    }

    protected static boolean checkAnnotation(int stack, final TransactionLegacy txn) {
        final StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
        final StackElement se = txn.peekInStack(CURRENT_TXN);
        if (se == null) {
            return false;
        }

        final StringBuffer sb = new StringBuffer();
        for (; stack < stacks.length; stack++) {
            final String methodName = stacks[stack].getMethodName();
            sb.append(" ").append(methodName);
            if (methodName.equals(se.ref)) {
                return true;
            }
        }

        // relax stack structure for several places that @DB required injection is not in place
        s_logger.warn(
                "Non-standard stack context that Transaction context is manaully placed into the calling chain. Stack chain: "
                        + sb);
        return true;
    }

    public static void initDataSource(final String propsFileName) throws IOException {
        final Properties dbProps = new Properties();
        final File dbPropsFile = PropertiesUtil.findConfigFile(propsFileName);
        if (dbPropsFile != null && dbPropsFile.exists()) {
            PropertiesUtil.loadFromFile(dbProps, dbPropsFile);
            initDataSource(dbProps);
        }
    }

    public static void initDataSource(final Properties dbProps) {
        loadMariaDbDriver();

        try {
            if (dbProps.size() == 0) {
                return;
            }

            s_dbHAEnabled = Boolean.valueOf(dbProps.getProperty("db.ha.enabled"));
            s_logger.info("Is Data Base High Availiability enabled? Ans : " + s_dbHAEnabled);
            final String loadBalanceStrategy = dbProps.getProperty("db.ha.loadBalanceStrategy");
            // FIXME: If params are missing...default them????
            final int cloudMaxActive = Integer.parseInt(dbProps.getProperty("db.cloud.maxActive"));
            final int cloudMaxIdle = Integer.parseInt(dbProps.getProperty("db.cloud.maxIdle"));
            final long cloudMaxWait = Long.parseLong(dbProps.getProperty("db.cloud.maxWait"));
            final String cloudUsername = dbProps.getProperty("db.cloud.username");
            final String cloudPassword = dbProps.getProperty("db.cloud.password");
            final String cloudHost = dbProps.getProperty("db.cloud.host");
            final int cloudPort = Integer.parseInt(dbProps.getProperty("db.cloud.port"));
            final String cloudDbName = dbProps.getProperty("db.cloud.name");
            final boolean cloudAutoReconnect = Boolean.parseBoolean(dbProps.getProperty("db.cloud.autoReconnect"));
            final String cloudValidationQuery = dbProps.getProperty("db.cloud.validationQuery");
            final String cloudIsolationLevel = dbProps.getProperty("db.cloud.isolation.level");

            int isolationLevel = Connection.TRANSACTION_READ_COMMITTED;
            if (cloudIsolationLevel == null) {
                isolationLevel = Connection.TRANSACTION_READ_COMMITTED;
            } else if (cloudIsolationLevel.equalsIgnoreCase("readcommitted")) {
                isolationLevel = Connection.TRANSACTION_READ_COMMITTED;
            } else if (cloudIsolationLevel.equalsIgnoreCase("repeatableread")) {
                isolationLevel = Connection.TRANSACTION_REPEATABLE_READ;
            } else if (cloudIsolationLevel.equalsIgnoreCase("serializable")) {
                isolationLevel = Connection.TRANSACTION_SERIALIZABLE;
            } else if (cloudIsolationLevel.equalsIgnoreCase("readuncommitted")) {
                isolationLevel = Connection.TRANSACTION_READ_UNCOMMITTED;
            } else {
                s_logger.warn("Unknown isolation level " + cloudIsolationLevel + ".  Using read uncommitted");
            }

            final boolean cloudTestOnBorrow = Boolean.parseBoolean(dbProps.getProperty("db.cloud.testOnBorrow"));
            final boolean cloudTestWhileIdle = Boolean.parseBoolean(dbProps.getProperty("db.cloud.testWhileIdle"));
            final long cloudTimeBtwEvictionRunsMillis = Long.parseLong(
                    dbProps.getProperty("db.cloud.timeBetweenEvictionRunsMillis"));
            final long cloudMinEvcitableIdleTimeMillis = Long.parseLong(
                    dbProps.getProperty("db.cloud.minEvictableIdleTimeMillis"));
            final boolean cloudPoolPreparedStatements = Boolean.parseBoolean(
                    dbProps.getProperty("db.cloud.poolPreparedStatements"));
            final String url = dbProps.getProperty("db.cloud.url.params");

            String cloudDbHAParams = null;
            String cloudSlaves = null;
            if (s_dbHAEnabled) {
                cloudDbHAParams = getDBHAParams("cloud", dbProps);
                cloudSlaves = dbProps.getProperty("db.cloud.slaves");
                s_logger.info("The slaves configured for Cloud Data base is/are : " + cloudSlaves);
            }

            final boolean useSSL = Boolean.parseBoolean(dbProps.getProperty("db.cloud.useSSL"));
            if (useSSL) {
                System.setProperty("javax.net.ssl.keyStore", dbProps.getProperty("db.cloud.keyStore"));
                System.setProperty("javax.net.ssl.keyStorePassword", dbProps.getProperty("db.cloud.keyStorePassword"));
                System.setProperty("javax.net.ssl.trustStore", dbProps.getProperty("db.cloud.trustStore"));
                System.setProperty("javax.net.ssl.trustStorePassword", dbProps.getProperty("db.cloud.trustStorePassword"));
            }

            final GenericObjectPool cloudConnectionPool = new GenericObjectPool(null, cloudMaxActive,
                    GenericObjectPool.DEFAULT_WHEN_EXHAUSTED_ACTION, cloudMaxWait, cloudMaxIdle, cloudTestOnBorrow, false,
                    cloudTimeBtwEvictionRunsMillis, 1, cloudMinEvcitableIdleTimeMillis, cloudTestWhileIdle);

            final ConnectionFactory cloudConnectionFactory = new DriverManagerConnectionFactory(
                    DB_CONNECTION_SCHEME + "://" + cloudHost + (s_dbHAEnabled ? "," + cloudSlaves : "") + ":" + cloudPort + "/" + cloudDbName +
                            "?autoReconnect=" + cloudAutoReconnect + (url != null ? "&" + url : "") + (useSSL ? "&useSSL=true" : "") +
                            (s_dbHAEnabled ? "&" + cloudDbHAParams : "")
                            + (s_dbHAEnabled ? "&loadBalanceStrategy=" + loadBalanceStrategy : ""),
                    cloudUsername, cloudPassword);

            final KeyedObjectPoolFactory poolableObjFactory = cloudPoolPreparedStatements ? new StackKeyedObjectPoolFactory()
                    : null;

            final PoolableConnectionFactory cloudPoolableConnectionFactory = new PoolableConnectionFactory(
                    cloudConnectionFactory, cloudConnectionPool, poolableObjFactory, cloudValidationQuery, false, false,
                    isolationLevel);

            // Default Data Source for CloudStack
            s_ds = new PoolingDataSource(cloudPoolableConnectionFactory.getPool());

            // Configure the usage db
            final int usageMaxActive = Integer.parseInt(dbProps.getProperty("db.usage.maxActive"));
            final int usageMaxIdle = Integer.parseInt(dbProps.getProperty("db.usage.maxIdle"));
            final long usageMaxWait = Long.parseLong(dbProps.getProperty("db.usage.maxWait"));
            final String usageUsername = dbProps.getProperty("db.usage.username");
            final String usagePassword = dbProps.getProperty("db.usage.password");
            final String usageHost = dbProps.getProperty("db.usage.host");
            final int usagePort = Integer.parseInt(dbProps.getProperty("db.usage.port"));
            final String usageDbName = dbProps.getProperty("db.usage.name");
            final boolean usageAutoReconnect = Boolean.parseBoolean(dbProps.getProperty("db.usage.autoReconnect"));
            final String usageUrl = dbProps.getProperty("db.usage.url.params");

            final GenericObjectPool usageConnectionPool = new GenericObjectPool(null, usageMaxActive,
                    GenericObjectPool.DEFAULT_WHEN_EXHAUSTED_ACTION, usageMaxWait, usageMaxIdle);

            final ConnectionFactory usageConnectionFactory = new DriverManagerConnectionFactory(DB_CONNECTION_SCHEME + "://" + usageHost
                    + (s_dbHAEnabled ? "," + dbProps.getProperty("db.cloud.slaves") : "") + ":" + usagePort +
                    "/" + usageDbName + "?autoReconnect=" + usageAutoReconnect + (usageUrl != null ? "&" + usageUrl : "") +
                    (s_dbHAEnabled ? "&" + getDBHAParams("usage", dbProps) : "")
                    + (s_dbHAEnabled ? "&loadBalanceStrategy=" + loadBalanceStrategy : ""), usageUsername,
                    usagePassword);

            final PoolableConnectionFactory usagePoolableConnectionFactory = new PoolableConnectionFactory(
                    usageConnectionFactory, usageConnectionPool, new StackKeyedObjectPoolFactory(), null, false, false);

            // Data Source for usage server
            s_usageDS = new PoolingDataSource(usagePoolableConnectionFactory.getPool());
        } catch (final Exception e) {
            s_ds = getDefaultDataSource("cloud");
            s_usageDS = getDefaultDataSource("cloud_usage");
            s_logger.warn(
                    "Unable to load db configuration, using defaults with 5 connections. Falling back on assumed datasource on localhost:3306 using username:password=cloud:cloud" +
                            ". Please check your configuration",
                    e);
        }
    }

    private static void loadMariaDbDriver() {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            s_logger.debug("MariaDB driver loaded");
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException("Cannot find MariaDB driver in the classpath", e);
        }
    }

    private static String getDBHAParams(final String dbName, final Properties dbProps) {
        final StringBuilder sb = new StringBuilder();
        sb.append("failOverReadOnly=" + dbProps.getProperty("db." + dbName + ".failOverReadOnly"));
        sb.append("&").append("reconnectAtTxEnd=" + dbProps.getProperty("db." + dbName + ".reconnectAtTxEnd"));
        sb.append("&").append("autoReconnectForPools=" + dbProps.getProperty("db." + dbName + ".autoReconnectForPools"));
        sb.append("&").append(
                "secondsBeforeRetryMaster=" + dbProps.getProperty("db." + dbName + ".secondsBeforeRetryMaster"));
        sb.append("&").append(
                "queriesBeforeRetryMaster=" + dbProps.getProperty("db." + dbName + ".queriesBeforeRetryMaster"));
        sb.append("&").append("initialTimeout=" + dbProps.getProperty("db." + dbName + ".initialTimeout"));
        return sb.toString();
    }

    private static DataSource getDefaultDataSource(final String database) {
        final GenericObjectPool connectionPool = new GenericObjectPool(null, 5);
        final ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(DB_CONNECTION_SCHEME + "://localhost:3306/" + database, "cloud", "cloud");
        final PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,
                connectionPool, null, null, false, true);
        return new PoolingDataSource(
        /* connectionPool */poolableConnectionFactory.getPool());
    }

    /**
     * Receives a list of {@link PreparedStatement} and quietly closes all of them, which triggers also closing their
     * dependent objects, like a {@link ResultSet}
     *
     * @param pstmt2Close
     */
    public static void closePstmts(final List<PreparedStatement> pstmt2Close) {
        for (final PreparedStatement pstmt : pstmt2Close) {
            try {
                if (pstmt != null && !pstmt.isClosed()) {
                    pstmt.close();
                }
            } catch (final SQLException e) {
                // It's not possible to recover from this and we need to continue closing
                e.printStackTrace();
            }
        }
    }

    //
    // Usage of this transaction setup should be limited, it will always open a new transaction context regardless of
    // whether or not there is other
    // transaction context in the stack. It is used in special use cases that we want to control DB connection explicitly
    // and in the mean time utilize
    // the existing DAO features
    //
    public void transitToUserManagedConnection(final Connection conn) {
        if (_conn != null) {
            throw new IllegalStateException("Can't change to a user managed connection unless the db connection is null");
        }

        _conn = conn;
        _dbId = CONNECTED_DB;
    }

    public void transitToAutoManagedConnection(final short dbId) {
        // assert(_stack.size() <= 1) : "Can't change to auto managed connection unless your stack is empty";
        _dbId = dbId;
        _conn = null;
    }

    public void registerLock(final String sql) {
        if (_txn && s_lockLogger.isDebugEnabled()) {
            final Pair<String, Long> time = new Pair<>(sql, System.currentTimeMillis());
            _lockTimes.add(time);
        }
    }

    public boolean dbTxnStarted() {
        return _txn;
    }

    public String getCreator() {
        return _creator;
    }

    public long getId() {
        return _id;
    }

    public String getName() {
        return _name;
    }

    public Short getDatabaseId() {
        return _dbId;
    }

    protected void mark(final String name) {
        _stack.push(new StackElement(CURRENT_TXN, name));
    }

    public boolean lock(final String name, final int timeoutSeconds) {
        final Merovingian2 lockMaster = Merovingian2.getLockMaster();
        if (lockMaster == null) {
            throw new CloudRuntimeException("There's no support for locking yet");
        }
        return lockMaster.acquire(name, timeoutSeconds);
    }

    public boolean release(final String name) {
        final Merovingian2 lockMaster = Merovingian2.getLockMaster();
        if (lockMaster == null) {
            throw new CloudRuntimeException("There's no support for locking yet");
        }
        return lockMaster.release(name);
    }

    /**
     * @deprecated Use {@link Transaction} for new code
     */
    @Deprecated
    public void start() {
        if (s_logger.isTraceEnabled()) {
            s_logger.trace("txn: start requested by: " + buildName());
        }

        _stack.push(new StackElement(START_TXN, null));

        if (_txn) {
            s_logger.trace("txn: has already been started.");
            return;
        }

        _txn = true;

        _txnTime = System.currentTimeMillis();
        if (_conn != null) {
            try {
                s_logger.trace("txn: set auto commit to false");
                _conn.setAutoCommit(false);
            } catch (final SQLException e) {
                s_logger.warn("Unable to set auto commit: ", e);
                throw new CloudRuntimeException("Unable to set auto commit: ", e);
            }
        }
    }

    protected static String buildName() {
        if (s_logger.isDebugEnabled()) {
            final StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
            final StringBuilder str = new StringBuilder();
            int i = 3, j = 3;
            while (j < 15 && i < stacks.length) {
                final StackTraceElement element = stacks[i];
                final String filename = element.getFileName();
                final String method = element.getMethodName();
                if (filename != null && filename.equals("<generated>") || method != null && method.equals("invokeSuper")) {
                    i++;
                    continue;
                }

                str.append("-").append(
                        stacks[i].getClassName().substring(stacks[i].getClassName().lastIndexOf(".") + 1)).append(".").append(
                        stacks[i].getMethodName()).append(":").append(stacks[i].getLineNumber());
                j++;
                i++;
            }
            return str.toString();
        }

        return "";
    }

    /**
     * Prepares an auto close statement. The statement is closed automatically if it is retrieved with this method.
     *
     * @param sql sql String
     * @return PreparedStatement
     * @throws SQLException if problem with JDBC layer.
     * @see java.sql.Connection
     */
    public PreparedStatement prepareAutoCloseStatement(final String sql) throws SQLException {
        final PreparedStatement stmt = prepareStatement(sql);
        closePreviousStatement();
        _stmt = stmt;
        return stmt;
    }

    public PreparedStatement prepareStatement(final String sql) throws SQLException {
        final Connection conn = getConnection();
        final PreparedStatement pstmt = conn.prepareStatement(sql);
        if (s_stmtLogger.isTraceEnabled()) {
            s_stmtLogger.trace("Preparing: " + sql);
        }
        return pstmt;
    }

    protected void closePreviousStatement() {
        if (_stmt != null) {
            try {
                if (s_stmtLogger.isTraceEnabled()) {
                    s_stmtLogger.trace("Closing: " + _stmt.toString());
                }
                try {
                    final ResultSet rs = _stmt.getResultSet();
                    if (rs != null && _stmt.getResultSetHoldability() != ResultSet.HOLD_CURSORS_OVER_COMMIT) {
                        rs.close();
                    }
                } catch (final SQLException e) {
                    s_stmtLogger.trace("Unable to close resultset");
                }
                _stmt.close();
            } catch (final SQLException e) {
                s_stmtLogger.trace("Unable to close statement: " + _stmt.toString());
            } finally {
                _stmt = null;
            }
        }
    }

    /**
     * Returns the db connection.
     * <p>
     * Note: that you can call getConnection() but beaware that all prepare statements from the Connection are not garbage
     * collected!
     *
     * @return DB Connection but make sure you understand that you are responsible for closing the PreparedStatement.
     * @throws SQLException
     */
    public Connection getConnection() throws SQLException {
        if (_conn == null) {
            switch (_dbId) {
                case CLOUD_DB:
                    if (s_ds != null) {
                        _conn = s_ds.getConnection();
                    } else {
                        s_logger.warn("A static-initialized variable becomes null, process is dying?");
                        throw new CloudRuntimeException("Database is not initialized, process is dying?");
                    }
                    break;
                case USAGE_DB:
                    if (s_usageDS != null) {
                        _conn = s_usageDS.getConnection();
                    } else {
                        s_logger.warn("A static-initialized variable becomes null, process is dying?");
                        throw new CloudRuntimeException("Database is not initialized, process is dying?");
                    }
                    break;
                default:
                    throw new CloudRuntimeException("No database selected for the transaction");
            }
            _conn.setAutoCommit(!_txn);

            //
            // MySQL default transaction isolation level is REPEATABLE READ,
            // to reduce chances of DB deadlock, we will use READ COMMITED isolation level instead
            // see http://dev.mysql.com/doc/refman/5.0/en/innodb-deadlocks.html
            //
            _stack.push(new StackElement(CREATE_CONN, null));
            if (s_connLogger.isTraceEnabled()) {
                s_connLogger.trace("Creating a DB connection with " + (_txn ? " txn: " : " no txn: ") + " for " + _dbId
                        + ": dbconn" + System.identityHashCode(_conn) +
                        ". Stack: " + buildName());
            }
        } else {
            s_logger.trace("conn: Using existing DB connection");
        }

        return _conn;
    }

    /**
     * Used for unit testing primarily
     *
     * @param conn
     */
    protected void setConnection(final Connection conn) {
        _conn = conn;
    }

    /**
     * Prepares an auto close statement. The statement is closed automatically if it is retrieved with this method.
     *
     * @param sql               sql String
     * @param autoGeneratedKeys keys that are generated
     * @return PreparedStatement
     * @throws SQLException if problem with JDBC layer.
     * @see java.sql.Connection
     */
    public PreparedStatement prepareAutoCloseStatement(final String sql, final int autoGeneratedKeys)
            throws SQLException {
        final Connection conn = getConnection();
        final PreparedStatement pstmt = conn.prepareStatement(sql, autoGeneratedKeys);
        if (s_stmtLogger.isTraceEnabled()) {
            s_stmtLogger.trace("Preparing: " + sql);
        }
        closePreviousStatement();
        _stmt = pstmt;
        return pstmt;
    }

    /**
     * Prepares an auto close statement. The statement is closed automatically if it is retrieved with this method.
     *
     * @param sql         sql String
     * @param columnNames names of the columns
     * @return PreparedStatement
     * @throws SQLException if problem with JDBC layer.
     * @see java.sql.Connection
     */
    public PreparedStatement prepareAutoCloseStatement(final String sql, final String[] columnNames) throws SQLException {
        final Connection conn = getConnection();
        final PreparedStatement pstmt = conn.prepareStatement(sql, columnNames);
        if (s_stmtLogger.isTraceEnabled()) {
            s_stmtLogger.trace("Preparing: " + sql);
        }
        closePreviousStatement();
        _stmt = pstmt;
        return pstmt;
    }

    /**
     * Prepares an auto close statement. The statement is closed automatically if it is retrieved with this method.
     *
     * @param sql sql String
     * @return PreparedStatement
     * @throws SQLException if problem with JDBC layer.
     * @see java.sql.Connection
     */
    public PreparedStatement prepareAutoCloseStatement(final String sql, final int resultSetType, final int resultSetConcurrency,
                                                       final int resultSetHoldability) throws SQLException {
        final Connection conn = getConnection();
        final PreparedStatement pstmt = conn.prepareStatement(sql, resultSetType, resultSetConcurrency,
                resultSetHoldability);
        if (s_stmtLogger.isTraceEnabled()) {
            s_stmtLogger.trace("Preparing: " + sql);
        }
        closePreviousStatement();
        _stmt = pstmt;
        return pstmt;
    }

    protected boolean takeOver(final String name, final boolean create) {
        if (_stack.size() != 0) {
            if (!create) {
                // If it is not a create transaction, then let's just use the current one.
                if (s_logger.isTraceEnabled()) {
                    s_logger.trace("Using current transaction: " + toString());
                }
                mark(name);
                return false;
            }

            final StackElement se = _stack.getFirst();
            if (se.type == CREATE_TXN) {
                // This create is called inside of another create. Which is ok?
                // We will let that create be responsible for cleaning up.
                if (s_logger.isTraceEnabled()) {
                    s_logger.trace("Create using current transaction: " + toString());
                }
                mark(name);
                return false;
            }

            s_logger.warn("Encountered a transaction that has leaked.  Cleaning up. " + toString());
            cleanup();
        }

        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Took over the transaction: " + name);
        }
        _stack.push(new StackElement(create ? CREATE_TXN : CURRENT_TXN, name));
        _name = name;
        return true;
    }

    public void cleanup() {
        closePreviousStatement();

        removeUpTo(null, null);
        if (_txn) {
            rollbackTransaction();
        }
        _txn = false;
        _name = null;

        closeConnection();

        _stack.clear();
        final Merovingian2 lockMaster = Merovingian2.getLockMaster();
        if (lockMaster != null) {
            lockMaster.cleanupThread();
        }
    }

    @Override
    public void close() {
        removeUpTo(CURRENT_TXN, null);

        if (_stack.size() == 0) {
            s_logger.trace("Transaction is done");
            cleanup();
        }
    }

    /**
     * close() is used by endTxn to close the connection. This method only closes the connection if the name is the same
     * as what's stored.
     *
     * @param name
     * @return true if this close actually closes the connection. false if not.
     */
    public boolean close(final String name) {
        if (_name == null) { // Already cleaned up.
            if (s_logger.isTraceEnabled()) {
                s_logger.trace("Already cleaned up." + buildName());
            }
            return true;
        }

        if (!_name.equals(name)) {
            close();
            return false;
        }

        if (s_logger.isDebugEnabled() && _stack.size() > 2) {
            s_logger.debug("Transaction is not closed properly: " + toString() + ".  Called by " + buildName());
        }

        cleanup();

        s_logger.trace("All done");
        return true;
    }

    public boolean commit() {
        if (!_txn) {
            s_logger.warn("txn: Commit called when it is not a transaction: " + buildName());
            return false;
        }

        final Iterator<StackElement> it = _stack.iterator();
        while (it.hasNext()) {
            final StackElement st = it.next();
            if (st.type == START_TXN) {
                it.remove();
                break;
            }
        }

        if (hasTxnInStack()) {
            if (s_logger.isTraceEnabled()) {
                s_logger.trace(
                        "txn: Not committing because transaction started elsewhere: " + buildName() + " / " + toString());
            }
            return false;
        }

        _txn = false;
        try {
            if (_conn != null) {
                _conn.commit();
                s_logger.trace("txn: DB Changes committed. Time = " + (System.currentTimeMillis() - _txnTime));
                clearLockTimes();
                closeConnection();
            }
            return true;
        } catch (final SQLException e) {
            rollbackTransaction();
            throw new CloudRuntimeException("Unable to commit or close the connection. ", e);
        }
    }

    protected boolean hasTxnInStack() {
        return peekInStack(START_TXN) != null;
    }

    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder(_name != null ? _name : "");
        str.append(" : ");
        for (final StackElement se : _stack) {
            if (se.type == CURRENT_TXN) {
                str.append(se.ref).append(", ");
            }
        }

        return str.toString();
    }

    protected void clearLockTimes() {
        if (s_lockLogger.isDebugEnabled()) {
            for (final Pair<String, Long> time : _lockTimes) {
                s_lockLogger.trace("SQL " + time.first() + " took " + (System.currentTimeMillis() - time.second()));
            }
            _lockTimes.clear();
        }
    }

    protected void closeConnection() {
        closePreviousStatement();

        if (_conn == null) {
            return;
        }

        if (_txn) {
            s_connLogger.trace("txn: Not closing DB connection because we're still in a transaction.");
            return;
        }

        try {
            // we should only close db connection when it is not user managed
            if (_dbId != CONNECTED_DB) {
                if (s_connLogger.isTraceEnabled()) {
                    s_connLogger.trace("Closing DB connection: dbconn" + System.identityHashCode(_conn));
                }
                _conn.close();
                _conn = null;
            }
        } catch (final SQLException e) {
            s_logger.warn("Unable to close connection", e);
        }
    }

    protected void rollbackTransaction() {
        closePreviousStatement();
        if (!_txn) {
            if (s_logger.isTraceEnabled()) {
                s_logger.trace("Rollback called for " + _name + " when there's no transaction: " + buildName());
            }
            return;
        }
        assert !hasTxnInStack() : "Who's rolling back transaction when there's still txn in stack?";
        _txn = false;
        try {
            if (_conn != null) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Rolling back the transaction: Time = " + (System.currentTimeMillis() - _txnTime) + " Name =  "
                            + _name + "; called by " + buildName());
                }
                _conn.rollback();
            }
            clearLockTimes();
            closeConnection();
        } catch (final SQLException e) {
            s_logger.warn("Unable to rollback", e);
        }
    }

    protected void removeUpTo(final String type, final Object ref) {
        boolean rollback = false;
        final Iterator<StackElement> it = _stack.iterator();
        while (it.hasNext()) {
            final StackElement item = it.next();

            it.remove();

            try {
                if ((type == null || type.equals(item.type)) && (ref == null || ref.equals(item.ref))) {
                    break;
                }

                if (item.type == CURRENT_TXN) {
                    if (s_logger.isTraceEnabled()) {
                        s_logger.trace("Releasing the current txn: " + (item.ref != null ? item.ref : ""));
                    }
                } else if (item.type == CREATE_CONN) {
                    closeConnection();
                } else if (item.type == START_TXN) {
                    if (item.ref == null) {
                        rollback = true;
                    } else {
                        try {
                            _conn.rollback((Savepoint) ref);
                            rollback = false;
                        } catch (final SQLException e) {
                            s_logger.warn("Unable to rollback Txn.", e);
                        }
                    }
                } else if (item.type == STATEMENT) {
                    try {
                        if (s_stmtLogger.isTraceEnabled()) {
                            s_stmtLogger.trace("Closing: " + ref.toString());
                        }
                        final Statement stmt = (Statement) ref;
                        try {
                            final ResultSet rs = stmt.getResultSet();
                            if (rs != null) {
                                rs.close();
                            }
                        } catch (final SQLException e) {
                            s_stmtLogger.trace("Unable to close resultset");
                        }
                        stmt.close();
                    } catch (final SQLException e) {
                        s_stmtLogger.trace("Unable to close statement: " + item);
                    }
                } else if (item.type == ATTACHMENT) {
                    final TransactionAttachment att = (TransactionAttachment) item.ref;
                    if (s_logger.isTraceEnabled()) {
                        s_logger.trace("Cleaning up " + att.getName());
                    }
                    att.cleanup();
                }
            } catch (final Exception e) {
                s_logger.error("Unable to clean up " + item, e);
            }
        }

        if (rollback) {
            rollback();
        }
    }

    protected void rollbackSavepoint(final Savepoint sp) {
        try {
            if (_conn != null) {
                _conn.rollback(sp);
            }
        } catch (final SQLException e) {
            s_logger.warn("Unable to rollback to savepoint " + sp);
        }

        if (!hasTxnInStack()) {
            _txn = false;
            closeConnection();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        if (!(_conn == null && (_stack == null || _stack.size() == 0))) {
            assert false : "Oh Alex oh alex...something is wrong with how we're doing this";
            s_logger.error("Something went wrong that a transaction is orphaned before db connection is closed");
            cleanup();
        }
    }

    public void rollback() {
        final Iterator<StackElement> it = _stack.iterator();
        while (it.hasNext()) {
            final StackElement st = it.next();
            if (st.type == START_TXN) {
                if (st.ref == null) {
                    it.remove();
                } else {
                    rollback((Savepoint) st.ref);
                    return;
                }
            }
        }

        rollbackTransaction();
    }

    public Savepoint setSavepoint() throws SQLException {
        _txn = true;
        final StackElement st = new StackElement(START_TXN, null);
        _stack.push(st);
        final Connection conn = getConnection();
        final Savepoint sp = conn.setSavepoint();
        st.ref = sp;

        return sp;
    }

    public Savepoint setSavepoint(final String name) throws SQLException {
        _txn = true;
        final StackElement st = new StackElement(START_TXN, null);
        _stack.push(st);
        final Connection conn = getConnection();
        final Savepoint sp = conn.setSavepoint(name);
        st.ref = sp;

        return sp;
    }

    public void releaseSavepoint(final Savepoint sp) throws SQLException {
        removeTxn(sp);
        if (_conn != null) {
            _conn.releaseSavepoint(sp);
        }

        if (!hasTxnInStack()) {
            _txn = false;
            closeConnection();
        }
    }

    protected void removeTxn(final Savepoint sp) {
        assert hasSavepointInStack(sp) : "Removing a save point that's not in the stack";

        if (!hasSavepointInStack(sp)) {
            return;
        }

        final Iterator<StackElement> it = _stack.iterator();
        while (it.hasNext()) {
            final StackElement se = it.next();
            if (se.type == START_TXN) {
                it.remove();
                if (se.ref == sp) {
                    return;
                }
            }
        }
    }

    protected boolean hasSavepointInStack(final Savepoint sp) {
        final Iterator<StackElement> it = _stack.iterator();
        while (it.hasNext()) {
            final StackElement se = it.next();
            if (se.type == START_TXN && se.ref == sp) {
                return true;
            }
        }
        return false;
    }

    public void rollback(final Savepoint sp) {
        removeTxn(sp);

        rollbackSavepoint(sp);
    }

    public Connection getCurrentConnection() {
        return _conn;
    }

    public List<StackElement> getStack() {
        return _stack;
    }

    protected class StackElement {
        public String type;
        public Object ref;

        public StackElement(final String type, final Object ref) {
            this.type = type;
            this.ref = ref;
        }

        @Override
        public String toString() {
            return type + "-" + ref;
        }
    }
}
