//

//

package com.cloud.utils.ssh;

import com.cloud.utils.Pair;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.trilead.ssh2.ChannelCondition;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SshHelper {
    private static final int DEFAULT_CONNECT_TIMEOUT = 180000;
    private static final int DEFAULT_KEX_TIMEOUT = 60000;

    /**
     * Waiting time to check if the SSH session was successfully opened. This value (of 1000
     * milliseconds) represents one (1) second.
     */
    private static final long WAITING_OPEN_SSH_SESSION = 1000;

    private static final Logger s_logger = LoggerFactory.getLogger(SshHelper.class);

    public static Pair<Boolean, String> sshExecute(final String host, final int port, final String user, final File pemKeyFile, final String password, final String command)
            throws Exception {

        return sshExecute(host, port, user, pemKeyFile, password, command, DEFAULT_CONNECT_TIMEOUT, DEFAULT_KEX_TIMEOUT, 120000);
    }

    public static Pair<Boolean, String> sshExecute(final String host, final int port, final String user, final File pemKeyFile, final String password, final String command,
                                                   final int connectTimeoutInMs,
                                                   final int kexTimeoutInMs,
                                                   final int waitResultTimeoutInMs) throws Exception {

        com.trilead.ssh2.Connection conn = null;
        com.trilead.ssh2.Session sess = null;
        try {
            conn = new com.trilead.ssh2.Connection(host, port);
            conn.connect(null, connectTimeoutInMs, kexTimeoutInMs);

            if (pemKeyFile == null) {
                if (!conn.authenticateWithPassword(user, password)) {
                    final String msg = "Failed to authentication SSH user " + user + " on host " + host;
                    s_logger.error(msg);
                    throw new Exception(msg);
                }
            } else {
                if (!conn.authenticateWithPublicKey(user, pemKeyFile, password)) {
                    final String msg = "Failed to authentication SSH user " + user + " on host " + host;
                    s_logger.error(msg);
                    throw new Exception(msg);
                }
            }
            sess = openConnectionSession(conn);

            sess.execCommand(command);

            final InputStream stdout = sess.getStdout();
            final InputStream stderr = sess.getStderr();

            final byte[] buffer = new byte[8192];
            final StringBuffer sbResult = new StringBuffer();

            int currentReadBytes = 0;
            while (true) {
                throwSshExceptionIfStdoutOrStdeerIsNull(stdout, stderr);

                if ((stdout.available() == 0) && (stderr.available() == 0)) {
                    final int conditions = sess.waitForCondition(ChannelCondition.STDOUT_DATA | ChannelCondition.STDERR_DATA | ChannelCondition.EOF | ChannelCondition.EXIT_STATUS,
                            waitResultTimeoutInMs);

                    throwSshExceptionIfConditionsTimeout(conditions);

                    if ((conditions & ChannelCondition.EXIT_STATUS) != 0) {
                        break;
                    }

                    if (canEndTheSshConnection(waitResultTimeoutInMs, sess, conditions)) {
                        break;
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

            if (sess.getExitStatus() == null) {
                //Exit status is NOT available. Returning failure result.
                s_logger.error(String.format("SSH execution of command %s has no exit status set. Result output: %s", command, result));
                return new Pair<>(false, result);
            }

            if (sess.getExitStatus() != null && sess.getExitStatus().intValue() != 0) {
                s_logger.error(String.format("SSH execution of command %s has an error status code in return. Result output: %s", command, result));
                return new Pair<>(false, result);
            }

            return new Pair<>(true, result);
        } finally {
            if (sess != null) {
                sess.close();
            }

            if (conn != null) {
                conn.close();
            }
        }
    }

    /**
     * It gets a {@link Session} from the given {@link Connection}; then, it waits
     * {@value #WAITING_OPEN_SSH_SESSION} milliseconds before returning the session, given a time to
     * ensure that the connection is open before proceeding the execution.
     */
    protected static Session openConnectionSession(final Connection conn) throws IOException, InterruptedException {
        final Session sess = conn.openSession();
        Thread.sleep(WAITING_OPEN_SSH_SESSION);
        return sess;
    }

    /**
     * Checks if the SSH session {@link com.trilead.ssh2.Session#getStdout()} or
     * {@link com.trilead.ssh2.Session#getStderr()} is null.
     */
    protected static void throwSshExceptionIfStdoutOrStdeerIsNull(final InputStream stdout, final InputStream stderr) throws SshException {
        if (stdout == null || stderr == null) {
            final String msg = "Stdout or Stderr of SSH session is null";
            s_logger.error(msg);
            throw new SshException(msg);
        }
    }

    /**
     * It throws a {@link SshException} if the channel condition is {@link ChannelCondition#TIMEOUT}
     */
    protected static void throwSshExceptionIfConditionsTimeout(final int conditions) throws SshException {
        if ((conditions & ChannelCondition.TIMEOUT) != 0) {
            final String msg = "Timed out in waiting for SSH execution exit status";
            s_logger.error(msg);
            throw new SshException(msg);
        }
    }

    /**
     * Handles the SSH connection in case of timeout or exit. If the session ends with a timeout
     * condition, it throws an exception; if the channel reaches an end of file condition, but it
     * does not have an exit status, it returns true to break the loop; otherwise, it returns
     * false.
     */
    protected static boolean canEndTheSshConnection(final int waitResultTimeoutInMs, final com.trilead.ssh2.Session sess, final int conditions) throws SshException {
        if (isChannelConditionEof(conditions)) {
            final int newConditions = sess.waitForCondition(ChannelCondition.EXIT_STATUS, waitResultTimeoutInMs);
            throwSshExceptionIfConditionsTimeout(newConditions);
            if ((newConditions & ChannelCondition.EXIT_STATUS) != 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the channel condition mask is of {@link ChannelCondition#EOF} and not
     * {@link ChannelCondition#STDERR_DATA} or {@link ChannelCondition#STDOUT_DATA}.
     */
    protected static boolean isChannelConditionEof(final int conditions) {
        if ((conditions & ChannelCondition.EOF) != 0) {
            return true;
        }
        return false;
    }

    public static void scpTo(final String host, final int port, final String user, final File pemKeyFile, final String password, final String remoteTargetDirectory, final String
            localFile, final String fileMode)
            throws Exception {

        scpTo(host, port, user, pemKeyFile, password, remoteTargetDirectory, localFile, fileMode, DEFAULT_CONNECT_TIMEOUT, DEFAULT_KEX_TIMEOUT);
    }

    public static void scpTo(final String host, final int port, final String user, final File pemKeyFile, final String password, final String remoteTargetDirectory, final String
            localFile, final String fileMode,
                             final int connectTimeoutInMs, final int kexTimeoutInMs) throws Exception {

        com.trilead.ssh2.Connection conn = null;
        com.trilead.ssh2.SCPClient scpClient = null;

        try {
            conn = new com.trilead.ssh2.Connection(host, port);
            conn.connect(null, connectTimeoutInMs, kexTimeoutInMs);

            if (pemKeyFile == null) {
                if (!conn.authenticateWithPassword(user, password)) {
                    final String msg = "Failed to authentication SSH user " + user + " on host " + host;
                    s_logger.error(msg);
                    throw new Exception(msg);
                }
            } else {
                if (!conn.authenticateWithPublicKey(user, pemKeyFile, password)) {
                    final String msg = "Failed to authentication SSH user " + user + " on host " + host;
                    s_logger.error(msg);
                    throw new Exception(msg);
                }
            }

            scpClient = conn.createSCPClient();

            if (fileMode != null) {
                scpClient.put(localFile, remoteTargetDirectory, fileMode);
            } else {
                scpClient.put(localFile, remoteTargetDirectory);
            }
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public static void scpTo(final String host, final int port, final String user, final File pemKeyFile, final String password, final String remoteTargetDirectory, final byte[]
            data, final String remoteFileName,
                             final String fileMode) throws Exception {

        scpTo(host, port, user, pemKeyFile, password, remoteTargetDirectory, data, remoteFileName, fileMode, DEFAULT_CONNECT_TIMEOUT, DEFAULT_KEX_TIMEOUT);
    }

    public static void scpTo(final String host, final int port, final String user, final File pemKeyFile, final String password, final String remoteTargetDirectory, final byte[]
            data, final String remoteFileName,
                             final String fileMode, final int connectTimeoutInMs, final int kexTimeoutInMs) throws Exception {

        com.trilead.ssh2.Connection conn = null;
        com.trilead.ssh2.SCPClient scpClient = null;

        try {
            conn = new com.trilead.ssh2.Connection(host, port);
            conn.connect(null, connectTimeoutInMs, kexTimeoutInMs);

            if (pemKeyFile == null) {
                if (!conn.authenticateWithPassword(user, password)) {
                    final String msg = "Failed to authentication SSH user " + user + " on host " + host;
                    s_logger.error(msg);
                    throw new Exception(msg);
                }
            } else {
                if (!conn.authenticateWithPublicKey(user, pemKeyFile, password)) {
                    final String msg = "Failed to authentication SSH user " + user + " on host " + host;
                    s_logger.error(msg);
                    throw new Exception(msg);
                }
            }

            scpClient = conn.createSCPClient();
            if (fileMode != null) {
                scpClient.put(data, remoteFileName, remoteTargetDirectory, fileMode);
            } else {
                scpClient.put(data, remoteFileName, remoteTargetDirectory);
            }
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
}
