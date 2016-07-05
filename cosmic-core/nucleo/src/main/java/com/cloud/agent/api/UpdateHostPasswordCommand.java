package com.cloud.agent.api;

import com.cloud.agent.api.LogLevel.Level;

public class UpdateHostPasswordCommand extends Command {

    @LogLevel(Level.Off)
    protected String username;
    @LogLevel(Level.Off)
    protected String newPassword;
    @LogLevel(Level.Off)
    protected String hostIp;

    protected UpdateHostPasswordCommand() {
    }

    public UpdateHostPasswordCommand(final String username, final String newPassword) {
        this(username, newPassword, null);
    }

    public UpdateHostPasswordCommand(final String username, final String newPassword, final String hostIp) {
        this.username = username;
        this.newPassword = newPassword;
        this.hostIp = hostIp;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public String getUsername() {
        return username;
    }

    public String getHostIp() {
        return hostIp;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
