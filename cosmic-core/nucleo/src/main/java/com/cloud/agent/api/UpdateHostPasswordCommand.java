//

//

package com.cloud.agent.api;

import com.cloud.agent.api.LogLevel.Log4jLevel;

public class UpdateHostPasswordCommand extends Command {

    @LogLevel(Log4jLevel.Off)
    protected String username;
    @LogLevel(Log4jLevel.Off)
    protected String newPassword;
    @LogLevel(Log4jLevel.Off)
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
