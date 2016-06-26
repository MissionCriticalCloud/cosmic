package com.cloud.utils.crypt;

import com.cloud.utils.PropertiesUtil;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.exception.CloudRuntimeException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.properties.EncryptableProperties;

/*
 * EncryptionSecretKeyChanger updates Management Secret Key / DB Secret Key or both.
 * DB secret key is validated against the key in db.properties
 * db.properties is updated with values encrypted using new MS secret key
 * DB data migrated using new DB secret key
 */
public class EncryptionSecretKeyChanger {

    private static final String keyFile = "/etc/cloudstack/management/key";
    private final StandardPBEStringEncryptor oldEncryptor = new StandardPBEStringEncryptor();
    private final StandardPBEStringEncryptor newEncryptor = new StandardPBEStringEncryptor();

    public static void main(final String[] args) {
        final List<String> argsList = Arrays.asList(args);
        final Iterator<String> iter = argsList.iterator();
        String oldMSKey = null;
        String oldDBKey = null;
        String newMSKey = null;
        String newDBKey = null;

        //Parse command-line args
        while (iter.hasNext()) {
            final String arg = iter.next();
            // Old MS Key
            if (arg.equals("-m")) {
                oldMSKey = iter.next();
            }
            // Old DB Key
            if (arg.equals("-d")) {
                oldDBKey = iter.next();
            }
            // New MS Key
            if (arg.equals("-n")) {
                newMSKey = iter.next();
            }
            // New DB Key
            if (arg.equals("-e")) {
                newDBKey = iter.next();
            }
        }

        if (oldMSKey == null || oldDBKey == null) {
            System.out.println("Existing MS secret key or DB secret key is not provided");
            usage();
            return;
        }

        if (newMSKey == null && newDBKey == null) {
            System.out.println("New MS secret key and DB secret are both not provided");
            usage();
            return;
        }

        final File dbPropsFile = PropertiesUtil.findConfigFile("db.properties");
        final Properties dbProps;
        final EncryptionSecretKeyChanger keyChanger = new EncryptionSecretKeyChanger();
        final StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        keyChanger.initEncryptor(encryptor, oldMSKey);
        dbProps = new EncryptableProperties(encryptor);
        PropertiesConfiguration backupDBProps = null;

        System.out.println("Parsing db.properties file");
        try (FileInputStream db_prop_fstream = new FileInputStream(dbPropsFile)) {
            dbProps.load(db_prop_fstream);
            backupDBProps = new PropertiesConfiguration(dbPropsFile);
        } catch (final FileNotFoundException e) {
            System.out.println("db.properties file not found while reading DB secret key" + e.getMessage());
        } catch (final IOException e) {
            System.out.println("Error while reading DB secret key from db.properties" + e.getMessage());
        } catch (final ConfigurationException e) {
            e.printStackTrace();
        }

        String dbSecretKey = null;
        try {
            dbSecretKey = dbProps.getProperty("db.cloud.encrypt.secret");
        } catch (final EncryptionOperationNotPossibleException e) {
            System.out.println("Failed to decrypt existing DB secret key from db.properties. " + e.getMessage());
            return;
        }

        if (!oldDBKey.equals(dbSecretKey)) {
            System.out.println("Incorrect MS Secret Key or DB Secret Key");
            return;
        }

        System.out.println("Secret key provided matched the key in db.properties");
        final String encryptionType = dbProps.getProperty("db.cloud.encryption.type");

        if (newMSKey == null) {
            System.out.println("No change in MS Key. Skipping migrating db.properties");
        } else {
            if (!keyChanger.migrateProperties(dbPropsFile, dbProps, newMSKey, newDBKey)) {
                System.out.println("Failed to update db.properties");
                return;
            } else {
                //db.properties updated successfully
                if (encryptionType.equals("file")) {
                    //update key file with new MS key
                    try (FileWriter fwriter = new FileWriter(keyFile);
                         BufferedWriter bwriter = new BufferedWriter(fwriter)) {
                        bwriter.write(newMSKey);
                    } catch (final IOException e) {
                        System.out.println("Failed to write new secret to file. Please update the file manually");
                    }
                }
            }
        }

        boolean success = false;
        if (newDBKey == null || newDBKey.equals(oldDBKey)) {
            System.out.println("No change in DB Secret Key. Skipping Data Migration");
        } else {
            EncryptionSecretKeyChecker.initEncryptorForMigration(oldMSKey);
            try {
                success = keyChanger.migrateData(oldDBKey, newDBKey);
            } catch (final Exception e) {
                System.out.println("Error during data migration");
                e.printStackTrace();
                success = false;
            }
        }

        if (success) {
            System.out.println("Successfully updated secret key(s)");
        } else {
            System.out.println("Data Migration failed. Reverting db.properties");
            //revert db.properties
            try {
                backupDBProps.save();
            } catch (final ConfigurationException e) {
                e.printStackTrace();
            }
            if (encryptionType.equals("file")) {
                //revert secret key in file
                try (FileWriter fwriter = new FileWriter(keyFile);
                     BufferedWriter bwriter = new BufferedWriter(fwriter)) {
                    bwriter.write(oldMSKey);
                } catch (final IOException e) {
                    System.out.println("Failed to revert to old secret to file. Please update the file manually");
                }
            }
        }
    }

    private static void usage() {
        System.out.println("Usage: \tEncryptionSecretKeyChanger \n" + "\t\t-m <Mgmt Secret Key> \n" + "\t\t-d <DB Secret Key> \n" + "\t\t-n [New Mgmt Secret Key] \n"
                + "\t\t-e [New DB Secret Key]");
    }

    private void initEncryptor(final StandardPBEStringEncryptor encryptor, final String secretKey) {
        encryptor.setAlgorithm("PBEWithMD5AndDES");
        final SimpleStringPBEConfig stringConfig = new SimpleStringPBEConfig();
        stringConfig.setPassword(secretKey);
        encryptor.setConfig(stringConfig);
    }

    private boolean migrateProperties(final File dbPropsFile, final Properties dbProps, final String newMSKey, final String newDBKey) {
        System.out.println("Migrating db.properties..");
        final StandardPBEStringEncryptor msEncryptor = new StandardPBEStringEncryptor();
        initEncryptor(msEncryptor, newMSKey);

        try {
            final PropertiesConfiguration newDBProps = new PropertiesConfiguration(dbPropsFile);
            if (newDBKey != null && !newDBKey.isEmpty()) {
                newDBProps.setProperty("db.cloud.encrypt.secret", "ENC(" + msEncryptor.encrypt(newDBKey) + ")");
            }
            String prop = dbProps.getProperty("db.cloud.password");
            if (prop != null && !prop.isEmpty()) {
                newDBProps.setProperty("db.cloud.password", "ENC(" + msEncryptor.encrypt(prop) + ")");
            }
            prop = dbProps.getProperty("db.usage.password");
            if (prop != null && !prop.isEmpty()) {
                newDBProps.setProperty("db.usage.password", "ENC(" + msEncryptor.encrypt(prop) + ")");
            }
            newDBProps.save(dbPropsFile.getAbsolutePath());
        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        }
        System.out.println("Migrating db.properties Done.");
        return true;
    }

    private boolean migrateData(final String oldDBKey, final String newDBKey) {
        System.out.println("Begin Data migration");
        initEncryptor(oldEncryptor, oldDBKey);
        initEncryptor(newEncryptor, newDBKey);
        System.out.println("Initialised Encryptors");

        final TransactionLegacy txn = TransactionLegacy.open("Migrate");
        txn.start();
        try {
            final Connection conn;
            try {
                conn = txn.getConnection();
            } catch (final SQLException e) {
                throw new CloudRuntimeException("Unable to migrate encrypted data in the database", e);
            }

            migrateConfigValues(conn);
            migrateHostDetails(conn);
            migrateVNCPassword(conn);
            migrateUserCredentials(conn);

            txn.commit();
        } finally {
            txn.close();
        }
        System.out.println("End Data migration");
        return true;
    }

    private void migrateConfigValues(final Connection conn) {
        System.out.println("Begin migrate config values");
        try (PreparedStatement select_pstmt = conn.prepareStatement("select name, value from configuration where category in ('Hidden', 'Secure')");
             ResultSet rs = select_pstmt.executeQuery();
             PreparedStatement update_pstmt = conn.prepareStatement("update configuration set value=? where name=?")
        ) {
            while (rs.next()) {
                final String name = rs.getString(1);
                final String value = rs.getString(2);
                if (value == null || value.isEmpty()) {
                    continue;
                }
                final String encryptedValue = migrateValue(value);
                update_pstmt.setBytes(1, encryptedValue.getBytes("UTF-8"));
                update_pstmt.setString(2, name);
                update_pstmt.executeUpdate();
            }
        } catch (final SQLException e) {
            throw new CloudRuntimeException("Unable to update configuration values ", e);
        } catch (final UnsupportedEncodingException e) {
            throw new CloudRuntimeException("Unable to update configuration values ", e);
        }
        System.out.println("End migrate config values");
    }

    private void migrateHostDetails(final Connection conn) {
        System.out.println("Begin migrate host details");

        try (PreparedStatement sel_pstmt = conn.prepareStatement("select id, value from host_details where name = 'password'");
             ResultSet rs = sel_pstmt.executeQuery();
             PreparedStatement pstmt = conn.prepareStatement("update host_details set value=? where id=?")
        ) {
            while (rs.next()) {
                final long id = rs.getLong(1);
                final String value = rs.getString(2);
                if (value == null || value.isEmpty()) {
                    continue;
                }
                final String encryptedValue = migrateValue(value);
                pstmt.setBytes(1, encryptedValue.getBytes("UTF-8"));
                pstmt.setLong(2, id);
                pstmt.executeUpdate();
            }
        } catch (final SQLException e) {
            throw new CloudRuntimeException("Unable update host_details values ", e);
        } catch (final UnsupportedEncodingException e) {
            throw new CloudRuntimeException("Unable update host_details values ", e);
        }
        System.out.println("End migrate host details");
    }

    private void migrateVNCPassword(final Connection conn) {
        System.out.println("Begin migrate VNC password");
        try (PreparedStatement select_pstmt = conn.prepareStatement("select id, vnc_password from vm_instance");
             ResultSet rs = select_pstmt.executeQuery();
             PreparedStatement pstmt = conn.prepareStatement("update vm_instance set vnc_password=? where id=?")
        ) {
            while (rs.next()) {
                final long id = rs.getLong(1);
                final String value = rs.getString(2);
                if (value == null || value.isEmpty()) {
                    continue;
                }
                final String encryptedValue = migrateValue(value);

                pstmt.setBytes(1, encryptedValue.getBytes("UTF-8"));
                pstmt.setLong(2, id);
                pstmt.executeUpdate();
            }
        } catch (final SQLException e) {
            throw new CloudRuntimeException("Unable update vm_instance vnc_password ", e);
        } catch (final UnsupportedEncodingException e) {
            throw new CloudRuntimeException("Unable update vm_instance vnc_password ", e);
        }
        System.out.println("End migrate VNC password");
    }

    private void migrateUserCredentials(final Connection conn) {
        System.out.println("Begin migrate user credentials");
        try (PreparedStatement select_pstmt = conn.prepareStatement("select id, secret_key from user");
             ResultSet rs = select_pstmt.executeQuery();
             PreparedStatement pstmt = conn.prepareStatement("update user set secret_key=? where id=?")
        ) {
            while (rs.next()) {
                final long id = rs.getLong(1);
                final String secretKey = rs.getString(2);
                if (secretKey == null || secretKey.isEmpty()) {
                    continue;
                }
                final String encryptedSecretKey = migrateValue(secretKey);
                pstmt.setBytes(1, encryptedSecretKey.getBytes("UTF-8"));
                pstmt.setLong(2, id);
                pstmt.executeUpdate();
            }
        } catch (final SQLException e) {
            throw new CloudRuntimeException("Unable update user secret key ", e);
        } catch (final UnsupportedEncodingException e) {
            throw new CloudRuntimeException("Unable update user secret key ", e);
        }
        System.out.println("End migrate user credentials");
    }

    private String migrateValue(final String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        final String decryptVal = oldEncryptor.decrypt(value);
        return newEncryptor.encrypt(decryptVal);
    }
}
