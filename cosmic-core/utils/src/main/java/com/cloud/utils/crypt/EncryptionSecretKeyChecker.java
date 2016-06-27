//

//

package com.cloud.utils.crypt;

import com.cloud.utils.db.DbProperties;
import com.cloud.utils.exception.CloudRuntimeException;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncryptionSecretKeyChecker {

    private static final Logger s_logger = LoggerFactory.getLogger(EncryptionSecretKeyChecker.class);

    // Two possible locations with the new packaging naming
    private static final String s_altKeyFile = "key";
    private static final String s_keyFile = "key";
    private static final String s_envKey = "CLOUD_SECRET_KEY";
    private static final StandardPBEStringEncryptor s_encryptor = new StandardPBEStringEncryptor();
    private static boolean s_useEncryption = false;

    public static StandardPBEStringEncryptor getEncryptor() {
        return s_encryptor;
    }

    public static boolean useEncryption() {
        return s_useEncryption;
    }

    //Initialize encryptor for migration during secret key change
    public static void initEncryptorForMigration(final String secretKey) {
        s_encryptor.setAlgorithm("PBEWithMD5AndDES");
        final SimpleStringPBEConfig stringConfig = new SimpleStringPBEConfig();
        stringConfig.setPassword(secretKey);
        s_encryptor.setConfig(stringConfig);
        s_useEncryption = true;
    }

    @PostConstruct
    public void init() {
        /* This will call DbProperties, which will call this to initialize the encryption. Yep,
         * round about and annoying */
        DbProperties.getDbProperties();
    }

    public void check(final Properties dbProps) throws IOException {
        final String encryptionType = dbProps.getProperty("db.cloud.encryption.type");

        s_logger.debug("Encryption Type: " + encryptionType);

        if (encryptionType == null || encryptionType.equals("none")) {
            return;
        }

        if (s_useEncryption) {
            s_logger.warn("Encryption already enabled, is check() called twice?");
            return;
        }

        s_encryptor.setAlgorithm("PBEWithMD5AndDES");
        String secretKey = null;

        final SimpleStringPBEConfig stringConfig = new SimpleStringPBEConfig();

        if (encryptionType.equals("file")) {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(s_keyFile);
            if (is == null) {
                is = this.getClass().getClassLoader().getResourceAsStream(s_altKeyFile);
            }
            if (is == null) {  //This is means we are not able to load key file from the classpath.
                throw new CloudRuntimeException(s_keyFile + " File containing secret key not found in the classpath: ");
            }

            try (BufferedReader in = new BufferedReader(new InputStreamReader(is))) {
                secretKey = in.readLine();
                //Check for null or empty secret key
            } catch (final IOException e) {
                throw new CloudRuntimeException("Error while reading secret key from: " + s_keyFile, e);
            }

            if (secretKey == null || secretKey.isEmpty()) {
                throw new CloudRuntimeException("Secret key is null or empty in file " + s_keyFile);
            }
        } else if (encryptionType.equals("env")) {
            secretKey = System.getenv(s_envKey);
            if (secretKey == null || secretKey.isEmpty()) {
                throw new CloudRuntimeException("Environment variable " + s_envKey + " is not set or empty");
            }
        } else if (encryptionType.equals("web")) {
            final int port = 8097;
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                s_logger.info("Waiting for admin to send secret key on port " + port);
                try (
                        Socket clientSocket = serverSocket.accept();
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
                ) {
                    final String inputLine;
                    if ((inputLine = in.readLine()) != null) {
                        secretKey = inputLine;
                    }
                } catch (final IOException e) {
                    throw new CloudRuntimeException("Accept failed on " + port);
                }
            } catch (final IOException ioex) {
                throw new CloudRuntimeException("Error initializing secret key reciever", ioex);
            }
        } else {
            throw new CloudRuntimeException("Invalid encryption type: " + encryptionType);
        }

        stringConfig.setPassword(secretKey);
        s_encryptor.setConfig(stringConfig);
        s_useEncryption = true;
    }
}
