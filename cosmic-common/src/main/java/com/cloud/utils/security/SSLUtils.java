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
            if (s.equals("SSLv3") || s.equals("SSLv2Hello")) {
                continue;
            }
            set.add(s);
        }
        return (String[]) set.toArray(new String[set.size()]);
    }

    public static String[] getSupportedCiphers() throws NoSuchAlgorithmException {
        final String[] availableCiphers = getSSLContext().getSocketFactory().getSupportedCipherSuites();
        Arrays.sort(availableCiphers);
        return availableCiphers;
    }

    public static SSLContext getSSLContext() throws NoSuchAlgorithmException {
        return SSLContext.getInstance("TLSv1.2");
    }

    public static SSLContext getSSLContext(final String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        return SSLContext.getInstance("TLSv1.2", provider);
    }

    public static String[] getRecommendedProtocols() {
        return new String[] { "TLSv1", "TLSv1.1", "TLSv1.2" };
    }

    public static String[] getRecommendedCiphers() {
        return new String[] { "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256", "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256",
                "TLS_RSA_WITH_AES_128_GCM_SHA256", "TLS_RSA_WITH_AES_128_CBC_SHA256", "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256", "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
                "TLS_DHE_RSA_WITH_AES_256_CBC_SHA", "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA", "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384", "TLS_DHE_RSA_WITH_AES_256_CBC_SHA256",
                "TLS_RSA_WITH_AES_256_GCM_SHA384", "TLS_RSA_WITH_AES_256_CBC_SHA256", "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384", "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384" };

        }
}
