//

//

package com.cloud.utils.crypt;

import com.cloud.utils.db.DbProperties;
import com.cloud.utils.exception.CloudRuntimeException;

import java.util.Properties;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBEncryptionUtil {

    public static final Logger s_logger = LoggerFactory.getLogger(DBEncryptionUtil.class);
    private static StandardPBEStringEncryptor s_encryptor = null;

    public static String encrypt(final String plain) {
        if (!EncryptionSecretKeyChecker.useEncryption() || (plain == null) || plain.isEmpty()) {
            return plain;
        }
        if (s_encryptor == null) {
            initialize();
        }
        String encryptedString = null;
        try {
            encryptedString = s_encryptor.encrypt(plain);
        } catch (final EncryptionOperationNotPossibleException e) {
            s_logger.debug("Error while encrypting: " + plain);
            throw e;
        }
        return encryptedString;
    }

    private static void initialize() {
        final Properties dbProps = DbProperties.getDbProperties();

        if (EncryptionSecretKeyChecker.useEncryption()) {
            final String dbSecretKey = dbProps.getProperty("db.cloud.encrypt.secret");
            if (dbSecretKey == null || dbSecretKey.isEmpty()) {
                throw new CloudRuntimeException("Empty DB secret key in db.properties");
            }

            s_encryptor = new StandardPBEStringEncryptor();
            s_encryptor.setAlgorithm("PBEWithMD5AndDES");
            s_encryptor.setPassword(dbSecretKey);
        } else {
            throw new CloudRuntimeException("Trying to encrypt db values when encrytion is not enabled");
        }
    }

    public static String decrypt(final String encrypted) {
        if (!EncryptionSecretKeyChecker.useEncryption() || (encrypted == null) || encrypted.isEmpty()) {
            return encrypted;
        }
        if (s_encryptor == null) {
            initialize();
        }

        String plain = null;
        try {
            plain = s_encryptor.decrypt(encrypted);
        } catch (final EncryptionOperationNotPossibleException e) {
            s_logger.debug("Error while decrypting: " + encrypted);
            throw e;
        }
        return plain;
    }
}
