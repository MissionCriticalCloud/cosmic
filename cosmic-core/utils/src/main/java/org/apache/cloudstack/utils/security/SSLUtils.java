//

//

package org.apache.cloudstack.utils.security;

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
        return SSLContext.getInstance("TLSv1");
    }

    public static SSLContext getSSLContext(final String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        return SSLContext.getInstance("TLSv1", provider);
    }
}
