package com.cloud.utils.db;

import com.cloud.utils.PropertiesUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class DbTestUtils {

    public static void executeScript(final String file, final boolean autoCommit, final boolean stopOnError) {
        final File cleanScript = PropertiesUtil.findConfigFile(file);
        if (cleanScript == null) {
            throw new RuntimeException("Unable to clean the database because I can't find " + file);
        }
        final Connection conn = TransactionLegacy.getStandaloneConnection();
        final ScriptRunner runner = new ScriptRunner(conn, autoCommit, stopOnError);
        try (FileReader reader = new FileReader(cleanScript)) {
            runner.runScript(reader);
            conn.close();
        } catch (final FileNotFoundException e) {
            throw new RuntimeException("Unable to read " + file, e);
        } catch (final IOException e) {
            throw new RuntimeException("Unable to read " + file, e);
        } catch (final SQLException e) {
            throw new RuntimeException("Unable to close DB connection", e);
        }
    }

    public static void executeUsageScript(final String file, final boolean autoCommit, final boolean stopOnError) {
        final File cleanScript = PropertiesUtil.findConfigFile(file);
        if (cleanScript == null) {
            throw new RuntimeException("Unable to clean the database because I can't find " + file);
        }
        final Connection conn = TransactionLegacy.getStandaloneUsageConnection();
        final ScriptRunner runner = new ScriptRunner(conn, autoCommit, stopOnError);
        try (FileReader reader = new FileReader(cleanScript)) {
            runner.runScript(reader);
        } catch (final IOException e) {
            throw new RuntimeException("executeUsageScript:Exception:" + e.getMessage(), e);
        } catch (final SQLException e) {
            throw new RuntimeException("executeUsageScript:Exception:" + e.getMessage(), e);
        }
        try {
            conn.close();
        } catch (final SQLException e) {
            throw new RuntimeException("Unable to close DB connection", e);
        }
    }
}
