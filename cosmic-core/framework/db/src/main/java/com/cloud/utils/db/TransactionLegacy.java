package com.cloud.utils.db;

import com.cloud.utils.Pair;
import com.cloud.utils.exception.CloudRuntimeException;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.io.Closeable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedList;

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

    private static final Logger logger = LoggerFactory.getLogger(TransactionLegacy.class);

    public static final short CLOUD_DB = 0;
    public static final short USAGE_DB = 1;

    private static final short CONNECTED_DB = -1;
    private static final ThreadLocal<TransactionLegacy> tls = new ThreadLocal<>();
    private static final String START_TXN = "start_txn";
    private static final String CURRENT_TXN = "current_txn";
    private static final String CREATE_CONN = "create_conn";
    private static final String STATEMENT = "statement";
    private static final String ATTACHMENT = "attachment";

    private static DataSource s_ds;
    private static DataSource s_usageDS;

    static {
        try {
            InitialContext cxt = new InitialContext();
            s_ds = (DataSource) cxt.lookup("java:/comp/env/jdbc/cosmic");
            s_usageDS = (DataSource) cxt.lookup("java:/comp/env/jdbc/cosmic_usage");
        } catch (final Exception e) {
            logger.error("Unable to connect to the database", e);
            throw new RuntimeException("Unable to connect to the database", e);
        }
    }

    private final LinkedList<StackElement> _stack = new LinkedList<>();
    private final LinkedList<Pair<String, Long>> _lockTimes = new LinkedList<>();
    private String _name;
    private Connection _conn;
    private boolean _txn;
    private short _dbId;
    private long _txnTime;
    private Statement _stmt;

    private TransactionLegacy(final String name, final short databaseId) {
        _name = name;
        _dbId = databaseId;
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
        return open(name, databaseId, true);
    }

    public static TransactionLegacy open(final String name) {
        return open(name, TransactionLegacy.CLOUD_DB, false);
    }

    public static TransactionLegacy open(final String name, final short databaseId, final boolean forceDbChange) {
        TransactionLegacy txn = tls.get();
        if (txn == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("Creating the transaction: " + name);
            }
            txn = new TransactionLegacy(name, databaseId);
            tls.set(txn);
        } else if (forceDbChange) {
            final short currentDbId = txn.getDatabaseId();
            if (currentDbId != databaseId) {
                txn.close(txn.getName());

                txn = new TransactionLegacy(name, databaseId);
                tls.set(txn);
            }
        }

        txn.checkConnection();
        txn.takeOver(name);
        return txn;
    }

    private void checkConnection() {
        try {
            if (_conn != null && !_conn.isValid(3)) {
                _conn = null;
            }
        } catch (SQLException e) {
            _conn = null;
        }
    }

    public static Connection getStandaloneConnection() {
        try {
            return getStandaloneConnectionWithException();
        } catch (final SQLException e) {
            logger.error("Unexpected exception: ", e);
            return null;
        }
    }

    public static Connection getStandaloneConnectionWithException() throws SQLException {
        final Connection conn = s_ds.getConnection();
        if (logger.isTraceEnabled()) {
            logger.trace("Retrieving a standalone connection: dbconn" + System.identityHashCode(conn));
        }
        return conn;
    }

    static Connection getStandaloneUsageConnection() {
        try {
            final Connection conn = s_usageDS.getConnection();
            if (logger.isTraceEnabled()) {
                logger.trace("Retrieving a standalone connection for usage: dbconn" + System.identityHashCode(conn));
            }
            return conn;
        } catch (final SQLException e) {
            logger.warn("Unexpected exception: ", e);
            return null;
        }
    }

    private StackElement peekInStack(final Object obj) {
        for (final StackElement next : _stack) {
            if (next.type == obj) {
                return next;
            }
        }
        return null;
    }

    protected void attach(final TransactionAttachment value) {
        _stack.push(new StackElement(ATTACHMENT, value));
    }

    protected TransactionAttachment detach(final String name) {
        final Iterator<StackElement> it = _stack.descendingIterator();
        while (it.hasNext()) {
            final StackElement element = it.next();
            if (ATTACHMENT.equals(element.type)) {
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

    public void transitToUserManagedConnection(final Connection conn) {
        if (_conn != null) {
            throw new IllegalStateException("Can't change to a user managed connection unless the db connection is null");
        }

        _conn = conn;
        _dbId = CONNECTED_DB;
    }

    public void transitToAutoManagedConnection(final short dbId) {
        _dbId = dbId;
        _conn = null;
    }

    void registerLock(final String sql) {
        if (_txn && logger.isDebugEnabled()) {
            final Pair<String, Long> time = new Pair<>(sql, System.currentTimeMillis());
            _lockTimes.add(time);
        }
    }

    public boolean dbTxnStarted() {
        return _txn;
    }

    public String getName() {
        return _name;
    }

    Short getDatabaseId() {
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

    @Deprecated
    public void start() {
        if (logger.isTraceEnabled()) {
            logger.trace("txn: start requested by: " + buildName());
        }

        _stack.push(new StackElement(START_TXN, null));

        if (_txn) {
            logger.trace("txn: has already been started.");
            return;
        }

        _txn = true;

        _txnTime = System.currentTimeMillis();
        if (_conn != null) {
            try {
                logger.trace("txn: set auto commit to false");
                _conn.setAutoCommit(false);
            } catch (final SQLException e) {
                logger.warn("Unable to set auto commit: ", e);
                throw new CloudRuntimeException("Unable to set auto commit: ", e);
            }
        }
    }

    private static String buildName() {
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

            str.append("-")
               .append(stacks[i].getClassName().substring(stacks[i].getClassName().lastIndexOf(".") + 1))
               .append(".")
               .append(stacks[i].getMethodName()).append(":").append(stacks[i].getLineNumber());
            j++;
            i++;
        }
        return str.toString();
    }

    public PreparedStatement prepareAutoCloseStatement(final String sql) throws SQLException {
        final PreparedStatement stmt = prepareStatement(sql);
        closePreviousStatement();
        _stmt = stmt;
        return stmt;
    }

    public PreparedStatement prepareStatement(final String sql) throws SQLException {
        final Connection conn = getConnection();
        final PreparedStatement pstmt = conn.prepareStatement(sql);
        if (logger.isTraceEnabled()) {
            logger.trace("Preparing: " + sql);
        }
        return pstmt;
    }

    private void closePreviousStatement() {
        if (_stmt != null) {
            try {
                if (logger.isTraceEnabled()) {
                    logger.trace("Closing: " + _stmt.toString());
                }
                try {
                    final ResultSet rs = _stmt.getResultSet();
                    if (rs != null && _stmt.getResultSetHoldability() != ResultSet.HOLD_CURSORS_OVER_COMMIT) {
                        rs.close();
                    }
                } catch (final Exception e) {
                    logger.trace("Unable to close resultset");
                }
                _stmt.close();
            } catch (final Exception e) {
                logger.trace("Unable to close statement: " + _stmt.toString());
            } finally {
                _stmt = null;
            }
        }
    }

    public Connection getConnection() throws SQLException {
        if (_conn == null) {
            switch (_dbId) {
                case CLOUD_DB:
                    if (s_ds != null) {
                        _conn = s_ds.getConnection();
                    } else {
                        logger.warn("A static-initialized variable becomes null, process is dying?");
                        throw new CloudRuntimeException("Database is not initialized, process is dying?");
                    }
                    break;
                case USAGE_DB:
                    if (s_usageDS != null) {
                        _conn = s_usageDS.getConnection();
                    } else {
                        logger.warn("A static-initialized variable becomes null, process is dying?");
                        throw new CloudRuntimeException("Database is not initialized, process is dying?");
                    }
                    break;
                default:
                    throw new CloudRuntimeException("No database selected for the transaction");
            }
            _conn.setAutoCommit(!_txn);

            _stack.push(new StackElement(CREATE_CONN, null));
            if (logger.isTraceEnabled()) {
                logger.trace("Creating a DB connection with " + (_txn ? " txn: " : " no txn: ") + " for " + _dbId
                        + ": dbconn" + System.identityHashCode(_conn) +
                        ". Stack: " + buildName());
            }
        } else {
            logger.trace("conn: Using existing DB connection");
        }

        return _conn;
    }

    protected void setConnection(final Connection conn) {
        _conn = conn;
    }

    public PreparedStatement prepareAutoCloseStatement(final String sql, final int autoGeneratedKeys)
            throws SQLException {
        final Connection conn = getConnection();
        final PreparedStatement pstmt = conn.prepareStatement(sql, autoGeneratedKeys);
        if (logger.isTraceEnabled()) {
            logger.trace("Preparing: " + sql);
        }
        closePreviousStatement();
        _stmt = pstmt;
        return pstmt;
    }

    private boolean takeOver(final String name) {
        if (_stack.size() != 0) {
            if (logger.isTraceEnabled()) {
                logger.trace("Using current transaction: " + toString());
            }
            mark(name);
            return false;
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Took over the transaction: " + name);
        }
        _stack.push(new StackElement(CURRENT_TXN, name));
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
            logger.trace("Transaction is done");
            cleanup();
        }
    }

    public boolean close(final String name) {
        if (_name == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("Already cleaned up." + buildName());
            }
            return true;
        }

        if (!_name.equals(name)) {
            close();
            return false;
        }

        if (logger.isDebugEnabled() && _stack.size() > 2) {
            logger.debug("Transaction is not closed properly: " + toString() + ".  Called by " + buildName());
        }

        cleanup();

        logger.trace("All done");
        return true;
    }

    public boolean commit() {
        if (!_txn) {
            logger.warn("txn: Commit called when it is not a transaction: " + buildName());
            return false;
        }

        final Iterator<StackElement> it = _stack.iterator();
        while (it.hasNext()) {
            final StackElement st = it.next();
            if (START_TXN.equals(st.type)) {
                it.remove();
                break;
            }
        }

        if (hasTxnInStack()) {
            if (logger.isTraceEnabled()) {
                logger.trace("txn: Not committing because transaction started elsewhere: " + buildName() + " / " + toString());
            }
            return false;
        }

        _txn = false;
        try {
            if (_conn != null) {
                _conn.commit();
                logger.trace("txn: DB Changes committed. Time = " + (System.currentTimeMillis() - _txnTime));
                clearLockTimes();
                closeConnection();
            }
            return true;
        } catch (final SQLException e) {
            rollbackTransaction();
            throw new CloudRuntimeException("Unable to commit or close the connection. ", e);
        }
    }

    private boolean hasTxnInStack() {
        return peekInStack(START_TXN) != null;
    }

    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder(_name != null ? _name : "");
        str.append(" : ");
        for (final StackElement se : _stack) {
            if (CURRENT_TXN.equals(se.type)) {
                str.append(se.ref).append(", ");
            }
        }

        return str.toString();
    }

    private void clearLockTimes() {
        if (logger.isDebugEnabled()) {
            for (final Pair<String, Long> time : _lockTimes) {
                logger.trace("SQL " + time.first() + " took " + (System.currentTimeMillis() - time.second()));
            }
            _lockTimes.clear();
        }
    }

    private void closeConnection() {
        closePreviousStatement();

        if (_conn == null) {
            return;
        }

        if (_txn) {
            logger.trace("txn: Not closing DB connection because we're still in a transaction.");
            return;
        }

        try {
            if (_dbId != CONNECTED_DB) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Closing DB connection: dbconn" + System.identityHashCode(_conn));
                }
                _conn.close();
                _conn = null;
            }
        } catch (final SQLException e) {
            logger.warn("Unable to close connection", e);
        }
    }

    private void rollbackTransaction() {
        closePreviousStatement();
        if (!_txn) {
            if (logger.isTraceEnabled()) {
                logger.trace("Rollback called for " + _name + " when there's no transaction: " + buildName());
            }
            return;
        }
        assert !hasTxnInStack() : "Who's rolling back transaction when there's still txn in stack?";
        _txn = false;
        try {
            if (_conn != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Rolling back the transaction: Time = " + (System.currentTimeMillis() - _txnTime) + " Name =  "
                            + _name + "; called by " + buildName());
                }
                _conn.rollback();
            }
            clearLockTimes();
            closeConnection();
        } catch (final SQLException e) {
            logger.warn("Unable to rollback", e);
        }
    }

    private void removeUpTo(final String type, final Object ref) {
        boolean rollback = false;
        final Iterator<StackElement> it = _stack.iterator();
        while (it.hasNext()) {
            final StackElement item = it.next();

            it.remove();

            try {
                if ((type == null || type.equals(item.type)) && (ref == null || ref.equals(item.ref))) {
                    break;
                }

                if (CURRENT_TXN.equals(item.type)) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Releasing the current txn: " + (item.ref != null ? item.ref : ""));
                    }
                } else if (CREATE_CONN.equals(item.type)) {
                    closeConnection();
                } else if (START_TXN.equals(item.type)) {
                    if (item.ref == null) {
                        rollback = true;
                    } else {
                        try {
                            _conn.rollback((Savepoint) ref);
                            rollback = false;
                        } catch (final SQLException e) {
                            logger.warn("Unable to rollback Txn.", e);
                        }
                    }
                } else if (STATEMENT.equals(item.type)) {
                    try {
                        if (logger.isTraceEnabled()) {
                            logger.trace("Closing: " + ref.toString());
                        }
                        final Statement stmt = (Statement) ref;
                        try {
                            final ResultSet rs = stmt.getResultSet();
                            if (rs != null) {
                                rs.close();
                            }
                        } catch (final SQLException e) {
                            logger.trace("Unable to close resultset");
                        }
                        stmt.close();
                    } catch (final SQLException e) {
                        logger.trace("Unable to close statement: " + item);
                    }
                } else if (ATTACHMENT.equals(item.type)) {
                    final TransactionAttachment att = (TransactionAttachment) item.ref;
                    if (logger.isTraceEnabled()) {
                        logger.trace("Cleaning up " + att.getName());
                    }
                    att.cleanup();
                }
            } catch (final Exception e) {
                logger.error("Unable to clean up " + item, e);
            }
        }

        if (rollback) {
            rollback();
        }
    }

    private void rollbackSavepoint(final Savepoint sp) {
        try {
            if (_conn != null) {
                _conn.rollback(sp);
            }
        } catch (final SQLException e) {
            logger.warn("Unable to rollback to savepoint " + sp);
        }

        if (!hasTxnInStack()) {
            _txn = false;
            closeConnection();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        if (_conn != null || !_stack.isEmpty()) {
            assert false : "Oh Alex oh alex...something is wrong with how we're doing this";
            logger.error("Something went wrong that a transaction is orphaned before db connection is closed");
            cleanup();
        }
    }

    public void rollback() {
        final Iterator<StackElement> it = _stack.iterator();
        while (it.hasNext()) {
            final StackElement st = it.next();
            if (START_TXN.equals(st.type)) {
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

    private void removeTxn(final Savepoint sp) {
        assert hasSavepointInStack(sp) : "Removing a save point that's not in the stack";

        if (!hasSavepointInStack(sp)) {
            return;
        }

        final Iterator<StackElement> it = _stack.iterator();
        while (it.hasNext()) {
            final StackElement se = it.next();
            if (START_TXN.equals(se.type)) {
                it.remove();
                if (se.ref == sp) {
                    return;
                }
            }
        }
    }

    private boolean hasSavepointInStack(final Savepoint sp) {
        for (final StackElement se : _stack) {
            if (START_TXN.equals(se.type) && se.ref == sp) {
                return true;
            }
        }
        return false;
    }

    public void rollback(final Savepoint sp) {
        removeTxn(sp);
        rollbackSavepoint(sp);
    }

    protected class StackElement {
        public String type;
        public Object ref;

        StackElement(final String type, final Object ref) {
            this.type = type;
            this.ref = ref;
        }

        @Override
        public String toString() {
            return type + "-" + ref;
        }
    }
}
