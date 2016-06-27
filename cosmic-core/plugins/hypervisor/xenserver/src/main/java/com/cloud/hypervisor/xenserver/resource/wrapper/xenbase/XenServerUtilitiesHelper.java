package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.utils.Pair;
import com.cloud.utils.ssh.SshHelper;

import java.io.File;

/**
 * This class is used to wrap the calls to several static methods. By doing so, we make easier to mock this class
 * and the methods wrapped here.
 */
public class XenServerUtilitiesHelper {

    public static final int TIMEOUT = 10000;
    public static final String SCRIPT_CMD_PATH = "sh /opt/cloud/bin/";

    public Pair<Boolean, String> executeSshWrapper(final String hostIp, final int port, final String username, final File pemFile, final String hostPasswd, final String command)
            throws Exception {
        final Pair<Boolean, String> result = SshHelper.sshExecute(hostIp, port, username, pemFile, hostPasswd, command, 60000, 60000, TIMEOUT);
        return result;
    }

    public String buildCommandLine(final String scriptPath, final String script, final String username, final String newPassword) {
        final StringBuilder cmdLine = new StringBuilder();
        cmdLine.append(scriptPath).append(script).append(' ').append(username).append(' ').append(newPassword);

        return cmdLine.toString();
    }
}
