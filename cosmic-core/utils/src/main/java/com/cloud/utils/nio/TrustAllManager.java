//

//

package com.cloud.utils.nio;

public class TrustAllManager implements javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager {
    public boolean isServerTrusted(final java.security.cert.X509Certificate[] certs) {
        return true;
    }

    public boolean isClientTrusted(final java.security.cert.X509Certificate[] certs) {
        return true;
    }

    @Override
    public void checkClientTrusted(final java.security.cert.X509Certificate[] certs, final String authType) throws java.security.cert.CertificateException {
        return;
    }

    @Override
    public void checkServerTrusted(final java.security.cert.X509Certificate[] certs, final String authType) throws java.security.cert.CertificateException {
        return;
    }

    @Override
    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return null;
    }
}
