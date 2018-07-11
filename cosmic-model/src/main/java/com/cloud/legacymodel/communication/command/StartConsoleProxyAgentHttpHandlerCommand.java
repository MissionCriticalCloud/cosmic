package com.cloud.legacymodel.communication.command;

import com.cloud.legacymodel.communication.LogLevel;
import com.cloud.legacymodel.communication.LogLevel.Level;

public class StartConsoleProxyAgentHttpHandlerCommand extends Command {
    @LogLevel(Level.Off)
    private byte[] keystoreBits;
    @LogLevel(Level.Off)
    private String keystorePassword;
    @LogLevel(Level.Off)
    private String encryptorPassword;
    @LogLevel(Level.Off)
    private String authenticationKey;

    public StartConsoleProxyAgentHttpHandlerCommand() {
        super();
    }

    public StartConsoleProxyAgentHttpHandlerCommand(final byte[] ksBits, final String ksPassword, final String ksAuthenticationKey) {
        this.keystoreBits = ksBits;
        this.keystorePassword = ksPassword;
        this.authenticationKey = ksAuthenticationKey;
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }

    public byte[] getKeystoreBits() {
        return keystoreBits;
    }

    public void setKeystoreBits(final byte[] keystoreBits) {
        this.keystoreBits = keystoreBits;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public void setKeystorePassword(final String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }

    public String getEncryptorPassword() {
        return encryptorPassword;
    }

    public void setEncryptorPassword(final String encryptorPassword) {
        this.encryptorPassword = encryptorPassword;
    }

    public String getAuthenticationKey() {
        return authenticationKey;
    }

    public void setAuthenticationKey(String authenticationKey) {
        this.authenticationKey = authenticationKey;
    }
}
