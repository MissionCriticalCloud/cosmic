package com.cloud.utils.security;

import javax.net.ssl.SSLContext;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SSLUtils {
    public static final Logger s_logger = LoggerFactory.getLogger(SSLUtils.class);

    public static String[] getSupportedProtocols(final String[] protocols) {
        final Set<String> set = new HashSet<>();
        for (final String s : protocols) {
            if (s.equals("SSLv3") || s.equals("SSLv2Hello") || s.equals("TLSv1") || s.equals("TLSv1.1")) {
                continue;
            }
            set.add(s);
        }
        return set.toArray(new String[0]);
    }

    public static String[] getSupportedCiphers() throws NoSuchAlgorithmException {
        final String[] availableCiphers = getSSLContext().getSocketFactory().getSupportedCipherSuites();
        Arrays.sort(availableCiphers);
        return availableCiphers;
    }

    public static SSLContext getSSLContext() throws NoSuchAlgorithmException {
        try {
            return SSLContext.getInstance("TLSv1.3");
        } catch (NoSuchAlgorithmException e) {
            return SSLContext.getInstance("TLSv1.2");
        }
    }

    public static SSLContext getSSLContext(final String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        try {
            return SSLContext.getInstance("TLSv1.3", provider);
        } catch (NoSuchAlgorithmException e) {
            return SSLContext.getInstance("TLSv1.2", provider);
        }
    }

    public static String[] getRecommendedProtocols() {
        return new String[] { "TLSv1.3", "TLSv1.2" };
    }

    public static String[] getRecommendedCiphers() {
        return new String[]{
                "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
                "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
                "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
                "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
                "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384",
                "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256"
        };
    }
}
