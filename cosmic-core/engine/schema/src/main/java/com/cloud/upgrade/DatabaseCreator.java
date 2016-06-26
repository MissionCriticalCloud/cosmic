package com.cloud.upgrade;

import com.cloud.utils.PropertiesUtil;
import com.cloud.utils.component.ComponentContext;
import com.cloud.utils.component.SystemIntegrityChecker;
import com.cloud.utils.db.ScriptRunner;
import com.cloud.utils.db.TransactionLegacy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.springframework.context.support.ClassPathXmlApplicationContext;

// Creates the CloudStack Database by using the 4.0 schema and apply
// upgrade steps to it.
public class DatabaseCreator {

    public static void main(final String[] args) {

        final ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(new String[]{"/com/cloud/upgrade/databaseCreatorContext.xml"});
        appContext.getBean(ComponentContext.class);

        String dbPropsFile = "";
        final List<String> sqlFiles = new ArrayList<>();
        final List<String> upgradeClasses = new ArrayList<>();
        String[] databases = new String[]{};
        String rootPassword = "";
        boolean verbosity = false;
        boolean dryRun = false;

        // Process opts
        for (final String arg : args) {
            if (arg.equals("--help") || arg.equals("-h")) {
                printHelp("DatabaseCreator");
                System.exit(0);
            } else if (arg.equals("--verbose") || arg.equals("-v")) {
                verbosity = true;
            } else if (arg.equals("--dry") || arg.equals("-d")) {
                dryRun = true;
            } else if (arg.startsWith("--rootpassword=")) {
                rootPassword = arg.substring(arg.lastIndexOf("=") + 1, arg.length());
            } else if (arg.startsWith("--database=")) {
                databases = arg.substring(arg.lastIndexOf("=") + 1, arg.length()).split(",");
            } else if (arg.endsWith(".sql")) {
                sqlFiles.add(arg);
            } else if (arg.endsWith(".sql.override")) {
                if (fileExists(arg)) {
                    final int index = arg.lastIndexOf(".override");
                    final String fileToOverride = arg.substring(0, index);
                    sqlFiles.remove(fileToOverride);
                    sqlFiles.add(arg);
                }
            } else if (arg.endsWith(".properties")) {
                if (!dbPropsFile.endsWith("properties.override") && fileExists(arg)) {
                    dbPropsFile = arg;
                }
            } else if (arg.endsWith("properties.override")) {
                if (fileExists(arg)) {
                    dbPropsFile = arg;
                }
            } else {
                upgradeClasses.add(arg);
            }
        }

        if ((dbPropsFile.isEmpty()) || (sqlFiles.size() == 0) && upgradeClasses.size() == 0) {
            printHelp("DatabaseCreator");
            System.exit(1);
        }

        try {
            TransactionLegacy.initDataSource(dbPropsFile);
        } catch (final IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        initDB(dbPropsFile, rootPassword, databases, dryRun);

        // Process sql files
        for (final String sqlFile : sqlFiles) {
            final File sqlScript = PropertiesUtil.findConfigFile(sqlFile);
            if (sqlScript == null) {
                System.err.println("Unable to find " + sqlFile);
                printHelp("DatabaseCreator");
                System.exit(1);
            }

            System.out.println("========> Processing SQL file at " + sqlScript.getAbsolutePath());

            try (Connection conn = TransactionLegacy.getStandaloneConnection();
                 FileReader reader = new FileReader(sqlScript)
            ) {
                if (!dryRun) {
                    runScript(conn, reader, sqlFile, verbosity);
                }
            } catch (final SQLException e) {
                System.err.println("Sql Exception:" + e.getMessage());
                System.exit(1);
            } catch (final IOException e) {
                System.err.println("File IO Exception : " + e.getMessage());
                System.exit(1);
            }
        }

        final TransactionLegacy txn = TransactionLegacy.open(TransactionLegacy.CLOUD_DB);
        try {
            // Process db upgrade classes
            for (final String upgradeClass : upgradeClasses) {
                System.out.println("========> Processing upgrade: " + upgradeClass);
                Class<?> clazz = null;
                try {
                    clazz = Class.forName(upgradeClass);
                    if (!SystemIntegrityChecker.class.isAssignableFrom(clazz)) {
                        System.err.println("The class must be of SystemIntegrityChecker: " + clazz.getName());
                        System.exit(1);
                    }
                    final SystemIntegrityChecker checker = (SystemIntegrityChecker) clazz.newInstance();
                    checker.check();
                } catch (final ClassNotFoundException e) {
                    System.err.println("Unable to find " + upgradeClass + ": " + e.getMessage());
                    System.exit(1);
                } catch (final InstantiationException e) {
                    System.err.println("Unable to instantiate " + upgradeClass + ": " + e.getMessage());
                    System.exit(1);
                } catch (final IllegalAccessException e) {
                    System.err.println("Unable to access " + upgradeClass + ": " + e.getMessage());
                    System.exit(1);
                }
            }
        } finally {
            txn.close();
        }
    }

    protected static void printHelp(final String cmd) {
        System.out.println("\nDatabaseCreator creates the database schema by removing the \n" + "previous schema, creating the schema, and running \n"
                + "through the database updaters.");
        System.out.println("Usage: " + cmd + " [options] [db.properties file] [schema.sql files] [database upgrade class]\nOptions:" +
                "\n   --database=a,b comma separate databases to initialize, use the db name in db.properties defined as db.xyz.host, xyz should be passed" +
                "\n   --rootpassword=password, by default it will try with an empty password" + "\n   --dry or -d, this would not run any process, just does a dry run" +
                "\n   --verbose or -v to print running sql commands, by default it won't print them" + "\n   --help or -h for help");
    }

    private static boolean fileExists(final String file) {
        final File f = new File(file);
        if (!f.exists()) {
            System.out.println("========> WARNING: Provided file does not exist: " + file);
        }
        return f.exists();
    }

    private static void initDB(final String dbPropsFile, final String rootPassword, final String[] databases, final boolean dryRun) {
        final Properties dbProperties = new Properties();
        try (FileInputStream f_stream = new FileInputStream(new File(dbPropsFile))) {
            dbProperties.load(f_stream);
        } catch (final IOException e) {
            System.out.println("IOError: unable to load/read db properties file: " + e);
            System.exit(1);
        }

        for (final String database : databases) {
            final String host = dbProperties.getProperty(String.format("db.%s.host", database));
            final String port = dbProperties.getProperty(String.format("db.%s.port", database));
            final String username = dbProperties.getProperty(String.format("db.%s.username", database));
            final String password = dbProperties.getProperty(String.format("db.%s.password", database));
            final String dbName = dbProperties.getProperty(String.format("db.%s.name", database));
            System.out.println(String.format("========> Initializing database=%s with host=%s port=%s username=%s password=%s", dbName, host, port, username, password));

            final List<String> queries = new ArrayList<>();
            queries.add(String.format("drop database if exists `%s`", dbName));
            queries.add(String.format("create database `%s`", dbName));
            queries.add(String.format("GRANT ALL ON %s.* to '%s'@`localhost` identified by '%s'", dbName, username, password));
            queries.add(String.format("GRANT ALL ON %s.* to '%s'@`%%` identified by '%s'", dbName, username, password));

            for (final String query : queries) {
                runQuery(host, port, rootPassword, query, dryRun);
            }
        }
    }

    private static void runScript(final Connection conn, final Reader reader, final String filename, final boolean verbosity) {
        final ScriptRunner runner = new ScriptRunner(conn, false, true, verbosity);
        try {
            runner.runScript(reader);
        } catch (final IOException e) {
            System.err.println("Unable to read " + filename + ": " + e.getMessage());
            System.exit(1);
        } catch (final SQLException e) {
            System.err.println("Unable to execute " + filename + ": " + e.getMessage());
            System.exit(1);
        }
    }

    private static void runQuery(final String host, final String port, final String rootPassword, final String query, final boolean dryRun) {
        System.out.println("============> Running query: " + query);
        try (Connection conn = DriverManager.getConnection(String.format("jdbc:mysql://%s:%s/", host, port), "root", rootPassword);
             Statement stmt = conn.createStatement()) {
            if (!dryRun) {
                stmt.executeUpdate(query);
            }
        } catch (final SQLException e) {
            System.out.println("SQL exception in trying initDB: " + e);
            System.exit(1);
        }
    }
}
