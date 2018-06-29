package com.cloud.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;

// To maintain independency of console proxy project, we duplicate this class from console proxy project
public class ConsoleProxyPasswordBasedEncryptor {
    private static final Logger s_logger = LoggerFactory.getLogger(ConsoleProxyPasswordBasedEncryptor.class);

    private final Gson gson;

    // key/IV will be set in 128 bit strength
    private final KeyIVPair keyIvPair;

    // Authentication key
    private final byte[] authenticationKey;

    public ConsoleProxyPasswordBasedEncryptor(final String password, final String authkey) {
        gson = new GsonBuilder().create();
        keyIvPair = gson.fromJson(password, KeyIVPair.class);
        this.authenticationKey = Base64.decodeBase64(authkey);
    }

    public <T> String encryptObject(final Class<?> clz, final T obj) {
        if (obj == null) {
            return null;
        }

        final String json = gson.toJson(obj);
        return encryptText(json);
    }

    public String encryptText(final String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        try {
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            final SecretKeySpec keySpec = new SecretKeySpec(keyIvPair.getKeyBytes(), "AES");

            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(keyIvPair.getIvBytes()));

            final byte[] encryptedBytes = cipher.doFinal(text.getBytes());

            final byte[] ivcipher = concat(this.keyIvPair.getIvBytes(), encryptedBytes);
            final byte[] hmac = generateHMAC(this.authenticationKey, ivcipher);

            return Base64.encodeBase64URLSafeString(concat(ivcipher, hmac));
        } catch (Exception e) {
            s_logger.error("Unexpected exception ", e);
            return null;
        }
    }

    public <T> T decryptObject(final Class<?> clz, final String encrypted) {
        if (encrypted == null || encrypted.isEmpty()) {
            return null;
        }

        final String json = decryptText(encrypted);
        return (T) gson.fromJson(json, clz);
    }

    public String decryptText(final String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }

        try {
            final byte[] encryptedBytes = Base64.decodeBase64(encryptedText);
            byte[] iv = Arrays.copyOf(encryptedBytes, this.keyIvPair.getIvSize());
            int maclength = hmacLength(this.authenticationKey);
            byte[] hmac1 = Arrays.copyOfRange(encryptedBytes, encryptedBytes.length - maclength, encryptedBytes.length);
            byte[] ciphertext = Arrays.copyOfRange(encryptedBytes, this.keyIvPair.getIvSize(), encryptedBytes.length - maclength);
            byte[] data = concat(iv, ciphertext);
            byte[] hmac2 = generateHMAC(this.authenticationKey, data);
            if (!Arrays.equals(hmac1, hmac2)) {
                s_logger.error("Incorrect HMAC");
                return null;
            }

            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            final SecretKeySpec keySpec = new SecretKeySpec(keyIvPair.getKeyBytes(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(keyIvPair.getIvBytes()));

            return new String(cipher.doFinal(ciphertext));
        } catch (Exception e) {
            s_logger.error("Unexpected exception ", e);
            return null;
        }
    }

    private byte[] generateHMAC(byte[] skey, byte[] data) throws Exception {
        SecretKeySpec key = new SecretKeySpec(skey, "HmacSHA256");
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        sha256_HMAC.init(key);
        return sha256_HMAC.doFinal(data);
    }

    private int hmacLength(byte[] skey) throws Exception {
        SecretKeySpec key = new SecretKeySpec(skey, "HmacSHA256");
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        sha256_HMAC.init(key);
        return sha256_HMAC.getMacLength();
    }

    private byte[] concat(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public static class KeyIVPair {
        private int IV_SIZE = 16;
        String base64EncodedKeyBytes;
        String base64EncodedIvBytes;

        public KeyIVPair() {
        }

        public KeyIVPair(final String base64EncodedKeyBytes, final String base64EncodedIvBytes) {
            this.base64EncodedKeyBytes = base64EncodedKeyBytes;
            this.base64EncodedIvBytes = base64EncodedIvBytes;
        }

        public byte[] getKeyBytes() {
            return Base64.decodeBase64(base64EncodedKeyBytes);
        }

        public void setKeyBytes(final byte[] keyBytes) {
            base64EncodedKeyBytes = Base64.encodeBase64URLSafeString(keyBytes);
        }

        public byte[] getIvBytes() {
            return Base64.decodeBase64(base64EncodedIvBytes);
        }

        public void setIvBytes(final byte[] ivBytes) {
            base64EncodedIvBytes = Base64.encodeBase64URLSafeString(ivBytes);
        }

        public int getIvSize() {
            return IV_SIZE;
        }
    }
}
