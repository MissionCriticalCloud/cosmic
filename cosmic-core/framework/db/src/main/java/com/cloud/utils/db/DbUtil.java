package com.cloud.utils.db;

import static com.cloud.utils.AutoCloseableUtil.closeAutoCloseable;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Id;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.PrimaryKeyJoinColumns;
import javax.persistence.SecondaryTable;
import javax.persistence.SecondaryTables;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbUtil {
    protected final static Logger s_logger = LoggerFactory.getLogger(DbUtil.class);

    private static Map<String, Connection> s_connectionForGlobalLocks = new HashMap<>();

    // This method exists because in class DbUtilsTest the static field s_connectionForGlobalLocks is set
    // via reflection. Without this method the IDE would want to make s_connectionForGlobalLocks final,
    // and that would break the tests.
    // TODO: fix this class and the respective test class
    private static void setConnectionForGlobalLocks(final Map<String, Connection> map) {
        s_connectionForGlobalLocks = map;
    }

    public static String getColumnName(final Field field, final AttributeOverride[] overrides) {
        if (overrides != null) {
            for (final AttributeOverride override : overrides) {
                if (override.name().equals(field.getName())) {
                    return override.column().name();
                }
            }
        }

        assert (field.getAnnotation(Embedded.class) == null) : "Cannot get column name from embedded field: " + field.getName();

        final Column column = field.getAnnotation(Column.class);
        return column != null ? column.name() : field.getName();
    }

    public static String getColumnName(final Field field) {
        return getColumnName(field, null);
    }

    public static String getReferenceColumn(final PrimaryKeyJoinColumn pkjc) {
        return pkjc.referencedColumnName().length() != 0 ? pkjc.referencedColumnName() : pkjc.name();
    }

    public static PrimaryKeyJoinColumn[] getPrimaryKeyJoinColumns(final Class<?> clazz) {
        final PrimaryKeyJoinColumn pkjc = clazz.getAnnotation(PrimaryKeyJoinColumn.class);
        if (pkjc != null) {
            return new PrimaryKeyJoinColumn[]{pkjc};
        }

        final PrimaryKeyJoinColumns pkjcs = clazz.getAnnotation(PrimaryKeyJoinColumns.class);
        if (pkjcs != null) {
            return pkjcs.value();
        }

        return null;
    }

    public static Field findField(final Class<?> clazz, final String columnName) {
        for (final Field field : clazz.getDeclaredFields()) {
            if (field.getAnnotation(Embedded.class) != null || field.getAnnotation(EmbeddedId.class) != null) {
                findField(field.getClass(), columnName);
            } else {
                if (columnName.equals(DbUtil.getColumnName(field))) {
                    return field;
                }
            }
        }
        return null;
    }

    public static final AttributeOverride[] getAttributeOverrides(final AnnotatedElement ae) {
        AttributeOverride[] overrides = null;

        final AttributeOverrides aos = ae.getAnnotation(AttributeOverrides.class);
        if (aos != null) {
            overrides = aos.value();
        }

        if (overrides == null || overrides.length == 0) {
            final AttributeOverride override = ae.getAnnotation(AttributeOverride.class);
            if (override != null) {
                overrides = new AttributeOverride[1];
                overrides[0] = override;
            } else {
                overrides = new AttributeOverride[0];
            }
        }

        return overrides;
    }

    public static final boolean isPersistable(final Field field) {
        if (field.getAnnotation(Transient.class) != null) {
            return false;
        }

        final int modifiers = field.getModifiers();
        return !(Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers));
    }

    public static final boolean isIdField(final Field field) {
        if (field.getAnnotation(Id.class) != null) {
            return true;
        }

        if (field.getAnnotation(EmbeddedId.class) != null) {
            assert (field.getClass().getAnnotation(Embeddable.class) != null) : "Class " + field.getClass().getName() + " must be Embeddable to be used as Embedded Id";
            return true;
        }

        return false;
    }

    public static final SecondaryTable[] getSecondaryTables(final AnnotatedElement clazz) {
        SecondaryTable[] sts = null;
        final SecondaryTable stAnnotation = clazz.getAnnotation(SecondaryTable.class);
        if (stAnnotation == null) {
            final SecondaryTables stsAnnotation = clazz.getAnnotation(SecondaryTables.class);
            sts = stsAnnotation != null ? stsAnnotation.value() : new SecondaryTable[0];
        } else {
            sts = new SecondaryTable[]{stAnnotation};
        }

        return sts;
    }

    public static final String getTableName(final Class<?> clazz) {
        final Table table = clazz.getAnnotation(Table.class);
        return table != null ? table.name() : clazz.getSimpleName();
    }

    public static boolean getGlobalLock(final String name, final int timeoutSeconds) {
        final Connection conn = getConnectionForGlobalLocks(name, true);
        if (conn == null) {
            s_logger.error("Unable to acquire DB connection for global lock system");
            return false;
        }

        try (PreparedStatement pstmt = conn.prepareStatement("SELECT COALESCE(GET_LOCK(?, ?),0)")) {
            pstmt.setString(1, name);
            pstmt.setInt(2, timeoutSeconds);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs != null && rs.first()) {
                    if (rs.getInt(1) > 0) {
                        return true;
                    } else {
                        if (s_logger.isDebugEnabled()) {
                            s_logger.debug("GET_LOCK() timed out on lock : " + name);
                        }
                    }
                }
            }
        } catch (final SQLException e) {
            s_logger.error("GET_LOCK() throws exception ", e);
        } catch (final Throwable e) {
            s_logger.error("GET_LOCK() throws exception ", e);
        }

        removeConnectionForGlobalLocks(name);
        closeAutoCloseable(conn, "connection for global lock");
        return false;
    }

    public static Connection getConnectionForGlobalLocks(final String name, final boolean forLock) {
        synchronized (s_connectionForGlobalLocks) {
            if (forLock) {
                if (s_connectionForGlobalLocks.get(name) != null) {
                    s_logger.error("Sanity check failed, global lock name " + name + " is already in use");
                    assert (false);
                }

                final Connection connection = TransactionLegacy.getStandaloneConnection();
                if (connection != null) {
                    try {
                        connection.setAutoCommit(true);
                    } catch (final SQLException e) {
                        closeAutoCloseable(connection, "error closing connection for global locks");
                        return null;
                    }
                    s_connectionForGlobalLocks.put(name, connection);
                    return connection;
                }
                return null;
            } else {
                final Connection connection = s_connectionForGlobalLocks.get(name);
                s_connectionForGlobalLocks.remove(name);
                return connection;
            }
        }
    }

    public static void removeConnectionForGlobalLocks(final String name) {
        synchronized (s_connectionForGlobalLocks) {
            s_connectionForGlobalLocks.remove(name);
        }
    }

    public static Class<?> getEntityBeanType(final GenericDao<?, Long> dao) {
        return dao.getEntityBeanType();
    }

    public static boolean releaseGlobalLock(final String name) {
        try (Connection conn = getConnectionForGlobalLocks(name, false)) {
            if (conn == null) {
                s_logger.error("Unable to acquire DB connection for global lock system");
                assert (false);
                return false;
            }

            try (PreparedStatement pstmt = conn.prepareStatement("SELECT COALESCE(RELEASE_LOCK(?), 0)")) {
                pstmt.setString(1, name);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs != null && rs.first()) {
                        return rs.getInt(1) > 0;
                    }
                    s_logger.error("releaseGlobalLock:RELEASE_LOCK() returns unexpected result");
                }
            }
        } catch (final SQLException e) {
            s_logger.error("RELEASE_LOCK() throws exception ", e);
        } catch (final Throwable e) {
            s_logger.error("RELEASE_LOCK() throws exception ", e);
        }
        return false;
    }

    public static void closeResources(final Statement statement, final ResultSet resultSet) {

        closeResources(null, statement, resultSet);
    }

    public static void closeResources(final Connection connection, final Statement statement, final ResultSet resultSet) {

        closeResultSet(resultSet);
        closeStatement(statement);
        closeConnection(connection);
    }

    public static void closeResultSet(final ResultSet resultSet) {
        closeAutoCloseable(resultSet, "exception while closing result set.");
    }

    public static void closeStatement(final Statement statement) {
        closeAutoCloseable(statement, "exception while closing statement.");
    }

    public static void closeConnection(final Connection connection) {
        closeAutoCloseable(connection, "exception while close connection.");
    }
}
