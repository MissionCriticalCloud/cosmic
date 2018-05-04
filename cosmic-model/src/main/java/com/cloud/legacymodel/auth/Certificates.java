package com.cloud.legacymodel.auth;

import com.cloud.legacymodel.communication.LogLevel;

public class Certificates {
    @LogLevel(LogLevel.Level.Off)
    private String privKey;
    @LogLevel(LogLevel.Level.Off)
    private String privCert;
    @LogLevel(LogLevel.Level.Off)
    private String certChain;
    @LogLevel(LogLevel.Level.Off)
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
