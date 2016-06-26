//

//

package com.cloud.utils.crypt;

import javax.crypto.Cipher;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.SecureRandom;
import java.security.Security;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.KeySpec;
import java.security.spec.RSAPublicKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RSAHelper {
    final static Logger s_logger = LoggerFactory.getLogger(RSAHelper.class);

    static {
        final BouncyCastleProvider provider = new BouncyCastleProvider();
        if (Security.getProvider(provider.getName()) == null) {
            Security.addProvider(provider);
        }
    }

    public static String encryptWithSSHPublicKey(final String sshPublicKey, final String content) {
        String returnString = null;
        try {
            final RSAPublicKey publicKey = readKey(sshPublicKey);
            final Cipher cipher = Cipher.getInstance("RSA/None/PKCS1Padding", BouncyCastleProvider.PROVIDER_NAME);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey, new SecureRandom());
            final byte[] encrypted = cipher.doFinal(content.getBytes());
            returnString = Base64.encodeBase64String(encrypted);
        } catch (final Exception e) {
            s_logger.info("[ignored]"
                    + "error during public key encryption: " + e.getLocalizedMessage());
        }

        return returnString;
    }

    private static RSAPublicKey readKey(final String key) throws Exception {
        final byte[] encKey = Base64.decodeBase64(key.split(" ")[1]);
        final DataInputStream dis = new DataInputStream(new ByteArrayInputStream(encKey));

        final byte[] header = readElement(dis);
        final String pubKeyFormat = new String(header);
        if (!pubKeyFormat.equals("ssh-rsa")) {
            throw new RuntimeException("Unsupported format");
        }

        final byte[] publicExponent = readElement(dis);
        final byte[] modulus = readElement(dis);

        final KeySpec spec = new RSAPublicKeySpec(new BigInteger(modulus), new BigInteger(publicExponent));
        final KeyFactory keyFactory = KeyFactory.getInstance("RSA", BouncyCastleProvider.PROVIDER_NAME);
        final RSAPublicKey pubKey = (RSAPublicKey) keyFactory.generatePublic(spec);

        return pubKey;
    }

    private static byte[] readElement(final DataInput dis) throws IOException {
        final int len = dis.readInt();
        final byte[] buf = new byte[len];
        dis.readFully(buf);
        return buf;
    }
}
