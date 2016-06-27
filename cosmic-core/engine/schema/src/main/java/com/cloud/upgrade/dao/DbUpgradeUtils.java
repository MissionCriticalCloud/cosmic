package com.cloud.upgrade.dao;

import java.sql.Connection;
import java.util.List;

public class DbUpgradeUtils {

    private static DatabaseAccessObject dao = new DatabaseAccessObject();

    // This method exists because DbUpgradeUtilsTest sets the static field dao via reflection.
    // Without this method the IDE would make the static field dao final, and that would break the tests.
    // TODO: fix this class and the respective test
    private static void setDao(final DatabaseAccessObject dao) {
        DbUpgradeUtils.dao = dao;
    }

    public static void dropKeysIfExist(final Connection conn, final String tableName, final List<String> keys, final boolean isForeignKey) {
        for (final String key : keys) {
            dao.dropKey(conn, tableName, key, isForeignKey);
        }
    }

    public static void dropPrimaryKeyIfExists(final Connection conn, final String tableName) {
        dao.dropPrimaryKey(conn, tableName);
    }

    public static void dropTableColumnsIfExist(final Connection conn, final String tableName, final List<String> columns) {
        for (final String columnName : columns) {
            if (dao.columnExists(conn, tableName, columnName)) {
                dao.dropColumn(conn, tableName, columnName);
            }
        }
    }
}
