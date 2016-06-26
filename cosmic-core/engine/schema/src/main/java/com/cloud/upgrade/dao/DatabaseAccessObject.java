package com.cloud.upgrade.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseAccessObject {

    private static Logger s_logger = LoggerFactory.getLogger(DatabaseAccessObject.class);

    // This method exists because DatabaseAccessObjectTest sets the static field s_logger via reflection.
    // Without this method the IDE would make the static field s_logger final, and that would break the tests.
    // TODO: fix this class and the respective test
    public static void setS_logger(final Logger s_logger) {
        DatabaseAccessObject.s_logger = s_logger;
    }

    protected static void closePreparedStatement(final PreparedStatement pstmt, final String errorMessage) {
        try {
            if (pstmt != null) {
                pstmt.close();
            }
        } catch (final SQLException e) {
            s_logger.warn(errorMessage, e);
        }
    }

    public void dropKey(final Connection conn, final String tableName, final String key, final boolean isForeignKey) {
        final String alter_sql_str;
        if (isForeignKey) {
            alter_sql_str = "ALTER TABLE " + tableName + " DROP FOREIGN KEY " + key;
        } else {
            alter_sql_str = "ALTER TABLE " + tableName + " DROP KEY " + key;
        }
        try (PreparedStatement pstmt = conn.prepareStatement(alter_sql_str)) {
            pstmt.executeUpdate();
            s_logger.debug("Key " + key + " is dropped successfully from the table " + tableName);
        } catch (final SQLException e) {
            s_logger.debug("Ignored SQL Exception when trying to drop " + (isForeignKey ? "foreign " : "") + "key " + key + " on table " + tableName + " exception: " + e
                    .getMessage());
        }
    }

    public void dropPrimaryKey(final Connection conn, final String tableName) {
        try (PreparedStatement pstmt = conn.prepareStatement("ALTER TABLE " + tableName + " DROP PRIMARY KEY ")) {
            pstmt.executeUpdate();
            s_logger.debug("Primary key is dropped successfully from the table " + tableName);
        } catch (final SQLException e) {
            s_logger.debug("Ignored SQL Exception when trying to drop primary key on table " + tableName + " exception: " + e.getMessage());
        }
    }

    public void dropColumn(final Connection conn, final String tableName, final String columnName) {
        try (PreparedStatement pstmt = conn.prepareStatement("ALTER TABLE " + tableName + " DROP COLUMN " + columnName)) {
            pstmt.executeUpdate();
            s_logger.debug("Column " + columnName + " is dropped successfully from the table " + tableName);
        } catch (final SQLException e) {
            s_logger.warn("Unable to drop column " + columnName + " due to exception", e);
        }
    }

    public boolean columnExists(final Connection conn, final String tableName, final String columnName) {
        boolean columnExists = false;
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT " + columnName + " FROM " + tableName)) {
            pstmt.executeQuery();
            columnExists = true;
        } catch (final SQLException e) {
            s_logger.debug("Field " + columnName + " doesn't exist in " + tableName + " ignoring exception: " + e.getMessage());
        }
        return columnExists;
    }
}
