// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package com.cloud.servlet;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// To maintain independency of console proxy project, we duplicate this class from console proxy project
public class ConsoleProxyPasswordBasedEncryptor {
    private static final Logger s_logger = LoggerFactory.getLogger(ConsoleProxyPasswordBasedEncryptor.class);

    private final Gson gson;

    // key/IV will be set in 128 bit strength
    private final KeyIVPair keyIvPair;

    public ConsoleProxyPasswordBasedEncryptor(final String password) {
        gson = new GsonBuilder().create();
        keyIvPair = gson.fromJson(password, KeyIVPair.class);
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
            return Base64.encodeBase64URLSafeString(encryptedBytes);
        } catch (final NoSuchAlgorithmException e) {
            s_logger.error("Unexpected exception ", e);
            return null;
        } catch (final NoSuchPaddingException e) {
            s_logger.error("Unexpected exception ", e);
            return null;
        } catch (final IllegalBlockSizeException e) {
            s_logger.error("Unexpected exception ", e);
            return null;
        } catch (final BadPaddingException e) {
            s_logger.error("Unexpected exception ", e);
            return null;
        } catch (final InvalidKeyException e) {
            s_logger.error("Unexpected exception ", e);
            return null;
        } catch (final InvalidAlgorithmParameterException e) {
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
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            final SecretKeySpec keySpec = new SecretKeySpec(keyIvPair.getKeyBytes(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(keyIvPair.getIvBytes()));

            final byte[] encryptedBytes = Base64.decodeBase64(encryptedText);
            return new String(cipher.doFinal(encryptedBytes));
        } catch (final NoSuchAlgorithmException e) {
            s_logger.error("Unexpected exception ", e);
            return null;
        } catch (final NoSuchPaddingException e) {
            s_logger.error("Unexpected exception ", e);
            return null;
        } catch (final IllegalBlockSizeException e) {
            s_logger.error("Unexpected exception ", e);
            return null;
        } catch (final BadPaddingException e) {
            s_logger.error("Unexpected exception ", e);
            return null;
        } catch (final InvalidKeyException e) {
            s_logger.error("Unexpected exception ", e);
            return null;
        } catch (final InvalidAlgorithmParameterException e) {
            s_logger.error("Unexpected exception ", e);
            return null;
        }
    }

    public static class KeyIVPair {
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
    }
}
