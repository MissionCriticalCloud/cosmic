//

//

package com.cloud.utils.ssh;

import java.io.IOException;
import java.io.InputStream;

import com.trilead.ssh2.ChannelCondition;
import com.trilead.ssh2.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SSHCmdHelper {
    private static final Logger s_logger = LoggerFactory.getLogger(SSHCmdHelper.class);
    private static final int DEFAULT_CONNECT_TIMEOUT = 180000;
    private static final int DEFAULT_KEX_TIMEOUT = 60000;

    public static com.trilead.ssh2.Connection acquireAuthorizedConnection(final String ip, final String username, final String password) {
        return acquireAuthorizedConnection(ip, 22, username, password);
    }

    public static com.trilead.ssh2.Connection acquireAuthorizedConnection(final String ip, final int port, final String username, final String password) {
        final com.trilead.ssh2.Connection sshConnection = new com.trilead.ssh2.Connection(ip, port);
        try {
            sshConnection.connect(null, DEFAULT_CONNECT_TIMEOUT, DEFAULT_KEX_TIMEOUT);
            if (!sshConnection.authenticateWithPassword(username, password)) {
                final String[] methods = sshConnection.getRemainingAuthMethods(username);
                final StringBuffer mStr = new StringBuffer();
                for (int i = 0; i < methods.length; i++) {
                    mStr.append(methods[i]);
                }
                s_logger.warn("SSH authorizes failed, support authorized methods are " + mStr);
                return null;
            }
            return sshConnection;
        } catch (final IOException e) {
            s_logger.warn("Get SSH connection failed", e);
            return null;
        }
    }

    public static void releaseSshConnection(final com.trilead.ssh2.Connection sshConnection) {
        if (sshConnection != null) {
            sshConnection.close();
        }
    }

    public static int sshExecuteCmdWithExitCode(final com.trilead.ssh2.Connection sshConnection, final String cmd) {
        return sshExecuteCmdWithExitCode(sshConnection, cmd, 3);
    }

    public static int sshExecuteCmdWithExitCode(final com.trilead.ssh2.Connection sshConnection, final String cmd, final int nTimes) {
        for (int i = 0; i < nTimes; i++) {
            try {
                return sshExecuteCmdOneShotWithExitCode(sshConnection, cmd);
            } catch (final SshException e) {
                continue;
            }
        }
        return -1;
    }

    public static int sshExecuteCmdOneShotWithExitCode(final com.trilead.ssh2.Connection sshConnection, final String cmd) throws SshException {
        s_logger.debug("Executing cmd: " + cmd);
        Session sshSession = null;
        try {
            sshSession = sshConnection.openSession();
            // There is a bug in Trilead library, wait a second before
            // starting a shell and executing commands, from http://spci.st.ewi.tudelft.nl/chiron/xref/nl/tudelft/swerl/util/SSHConnection.html
            Thread.sleep(1000);

            if (sshSession == null) {
                throw new SshException("Cannot open ssh session");
            }

            sshSession.execCommand(cmd);

            final InputStream stdout = sshSession.getStdout();
            final InputStream stderr = sshSession.getStderr();

            final byte[] buffer = new byte[8192];
            final StringBuffer sbResult = new StringBuffer();

            int currentReadBytes = 0;
            while (true) {
                if (stdout == null || stderr == null) {
                    throw new SshException("stdout or stderr of ssh session is null");
                }
                if ((stdout.available() == 0) && (stderr.available() == 0)) {
                    final int conditions = sshSession.waitForCondition(ChannelCondition.STDOUT_DATA
                                    | ChannelCondition.STDERR_DATA | ChannelCondition.EOF | ChannelCondition.EXIT_STATUS,
                            120000);

                    if ((conditions & ChannelCondition.TIMEOUT) != 0) {
                        final String msg = "Timed out in waiting SSH execution result";
                        s_logger.error(msg);
                        throw new Exception(msg);
                    }

                    if ((conditions & ChannelCondition.EXIT_STATUS) != 0) {
                        if ((conditions & (ChannelCondition.STDOUT_DATA | ChannelCondition.STDERR_DATA)) == 0) {
                            break;
                        }
                    }

                    if ((conditions & ChannelCondition.EOF) != 0) {
                        if ((conditions & (ChannelCondition.STDOUT_DATA | ChannelCondition.STDERR_DATA)) == 0) {
                            break;
                        }
                    }
                }

                while (stdout.available() > 0) {
                    currentReadBytes = stdout.read(buffer);
                    sbResult.append(new String(buffer, 0, currentReadBytes));
                }

                while (stderr.available() > 0) {
                    currentReadBytes = stderr.read(buffer);
                    sbResult.append(new String(buffer, 0, currentReadBytes));
                }
            }

            final String result = sbResult.toString();
            if (result != null && !result.isEmpty()) {
                s_logger.debug(cmd + " output:" + result);
            }
            // exit status delivery might get delayed
            for (int i = 0; i < 10; i++) {
                final Integer status = sshSession.getExitStatus();
                if (status != null) {
                    return status;
                }
                Thread.sleep(100);
            }
            return -1;
        } catch (final Exception e) {
            s_logger.debug("Ssh executed failed", e);
            throw new SshException("Ssh executed failed " + e.getMessage());
        } finally {
            if (sshSession != null) {
                sshSession.close();
            }
        }
    }

    public static boolean sshExecuteCmd(final com.trilead.ssh2.Connection sshConnection, final String cmd) {
        return sshExecuteCmd(sshConnection, cmd, 3);
    }

    public static boolean sshExecuteCmd(final com.trilead.ssh2.Connection sshConnection, final String cmd, final int nTimes) {
        for (int i = 0; i < nTimes; i++) {
            try {
                if (sshExecuteCmdOneShot(sshConnection, cmd)) {
                    return true;
                }
            } catch (final SshException e) {
                continue;
            }
        }
        return false;
    }

    public static boolean sshExecuteCmdOneShot(final com.trilead.ssh2.Connection sshConnection, final String cmd) throws SshException {
        return sshExecuteCmdOneShotWithExitCode(sshConnection, cmd) == 0;
    }
}
