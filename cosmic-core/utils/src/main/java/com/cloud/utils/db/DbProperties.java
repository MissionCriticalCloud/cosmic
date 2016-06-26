//

//

package com.cloud.utils.db;

import com.cloud.utils.PropertiesUtil;
import com.cloud.utils.crypt.EncryptionSecretKeyChecker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.properties.EncryptableProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbProperties {

    private static final Logger log = LoggerFactory.getLogger(DbProperties.class);

    private static Properties properties = new Properties();
    private static boolean loaded = false;

    public synchronized static Properties getDbProperties() {
        if (!loaded) {
            Properties dbProps = new Properties();
            InputStream is = null;
            try {
                final File props = PropertiesUtil.findConfigFile("db.properties");
                if (props != null && props.exists()) {
                    is = new FileInputStream(props);
                }

                if (is == null) {
                    is = PropertiesUtil.openStreamFromURL("db.properties");
                }

                if (is == null) {
                    System.err.println("Failed to find db.properties");
                    log.error("Failed to find db.properties");
                }

                if (is != null) {
                    dbProps.load(is);
                }

                final EncryptionSecretKeyChecker checker = new EncryptionSecretKeyChecker();
                checker.check(dbProps);

                if (EncryptionSecretKeyChecker.useEncryption()) {
                    final StandardPBEStringEncryptor encryptor = EncryptionSecretKeyChecker.getEncryptor();
                    final EncryptableProperties encrDbProps = new EncryptableProperties(encryptor);
                    encrDbProps.putAll(dbProps);
                    dbProps = encrDbProps;
                }
            } catch (final IOException e) {
                throw new IllegalStateException("Failed to load db.properties", e);
            } finally {
                IOUtils.closeQuietly(is);
            }

            properties = dbProps;
            loaded = true;
        }

        return properties;
    }

    public synchronized static Properties setDbProperties(final Properties props) throws IOException {
        if (loaded) {
            throw new IllegalStateException("DbProperties has already been loaded");
        }
        properties = wrapEncryption(props);
        loaded = true;
        return properties;
    }

    protected static Properties wrapEncryption(final Properties dbProps) throws IOException {
        final EncryptionSecretKeyChecker checker = new EncryptionSecretKeyChecker();
        checker.check(dbProps);

        if (EncryptionSecretKeyChecker.useEncryption()) {
            return dbProps;
        } else {
            final EncryptableProperties encrProps = new EncryptableProperties(EncryptionSecretKeyChecker.getEncryptor());
            encrProps.putAll(dbProps);
            return encrProps;
        }
    }
}
