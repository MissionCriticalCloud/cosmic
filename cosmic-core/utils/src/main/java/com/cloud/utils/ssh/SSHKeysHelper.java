//

//

package com.cloud.utils.ssh;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import org.apache.commons.codec.binary.Base64;

public class SSHKeysHelper {

    private static final char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private KeyPair keyPair;

    public SSHKeysHelper() {
        try {
            keyPair = KeyPair.genKeyPair(new JSch(), KeyPair.RSA);
        } catch (final JSchException e) {
            e.printStackTrace();
        }
    }

    public static String getPublicKeyFromKeyMaterial(String keyMaterial) {
        if (!keyMaterial.contains(" ")) {
            keyMaterial = new String(Base64.decodeBase64(keyMaterial.getBytes()));
        }

        if ((!keyMaterial.startsWith("ssh-rsa") && !keyMaterial.startsWith("ssh-dss")) || !keyMaterial.contains(" ")) {
            return null;
        }

        final String[] key = keyMaterial.split(" ");
        if (key.length < 2) {
            return null;
        }

        return key[0].concat(" ").concat(key[1]);
    }

    public String getPublicKeyFingerPrint() {
        return getPublicKeyFingerprint(getPublicKey());
    }

    public static String getPublicKeyFingerprint(final String publicKey) {
        final String[] key = publicKey.split(" ");
        if (key.length < 2) {
            throw new RuntimeException("Incorrect public key is passed in");
        }
        final byte[] keyBytes = Base64.decodeBase64(key[1]);

        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (final NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        String rString = "";
        String sumString = "";
        if (md5 != null) {
            sumString = toHexString(md5.digest(keyBytes));
        }

        for (int i = 2; i <= sumString.length(); i += 2) {
            rString += sumString.substring(i - 2, i);
            if (i != sumString.length()) {
                rString += ":";
            }
        }

        return rString;
    }

    public String getPublicKey() {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        keyPair.writePublicKey(baos, "");

        return baos.toString();
    }

    private static String toHexString(final byte[] b) {
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            sb.append(hexChars[(b[i] >> 4) & 0x0f]);
            sb.append(hexChars[(b[i]) & 0x0f]);
        }
        return sb.toString();
    }

    public String getPrivateKey() {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        keyPair.writePrivateKey(baos);

        return baos.toString();
    }
}
