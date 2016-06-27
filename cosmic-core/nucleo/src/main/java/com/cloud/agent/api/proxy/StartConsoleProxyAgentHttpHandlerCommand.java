//

//

package com.cloud.agent.api.proxy;

import com.cloud.agent.api.Command;
import com.cloud.agent.api.LogLevel;
import com.cloud.agent.api.LogLevel.Log4jLevel;

public class StartConsoleProxyAgentHttpHandlerCommand extends Command {
    @LogLevel(Log4jLevel.Off)
    private byte[] keystoreBits;
    @LogLevel(Log4jLevel.Off)
    private String keystorePassword;
    @LogLevel(Log4jLevel.Off)
    private String encryptorPassword;

    public StartConsoleProxyAgentHttpHandlerCommand() {
        super();
    }

    public StartConsoleProxyAgentHttpHandlerCommand(final byte[] ksBits, final String ksPassword) {
        this.keystoreBits = ksBits;
        this.keystorePassword = ksPassword;
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
}
