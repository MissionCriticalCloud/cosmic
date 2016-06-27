package org.apache.cloudstack.framework.security.keystore;

import com.cloud.agent.api.LogLevel;
import com.cloud.agent.api.LogLevel.Log4jLevel;
import com.cloud.utils.component.Manager;

public interface KeystoreManager extends Manager {
    boolean validateCertificate(String certificate, String key, String domainSuffix);

    void saveCertificate(String name, String certificate, String key, String domainSuffix);

    byte[] getKeystoreBits(String name, String aliasForCertificateInStore, String storePassword);

    void saveCertificate(String name, String certificate, Integer index, String domainSuffix);

    Certificates getCertificates(String name);

    public static class Certificates {
        @LogLevel(Log4jLevel.Off)
        private String privKey;
        @LogLevel(Log4jLevel.Off)
        private String privCert;
        @LogLevel(Log4jLevel.Off)
        private String certChain;
        @LogLevel(Log4jLevel.Off)
        private String rootCACert;

        public Certificates() {

        }

        public Certificates(final String prvKey, final String privCert, final String certChain, final String rootCACert) {
            this.privKey = prvKey;
            this.privCert = privCert;
            this.certChain = certChain;
            this.rootCACert = rootCACert;
        }

        public String getPrivKey() {
            return privKey;
        }

        public String getPrivCert() {
            return privCert;
        }

        public String getCertChain() {
            return certChain;
        }

        public String getRootCACert() {
            return rootCACert;
        }
    }
}
