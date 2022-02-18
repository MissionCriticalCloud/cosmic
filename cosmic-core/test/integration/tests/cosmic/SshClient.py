import socket

import time
from paramiko import (
    BadHostKeyException,
    AuthenticationException,
    SSHException,
    SSHClient,
    AutoAddPolicy,
    Transport,
    SFTPClient
)

from .codes import (
    SUCCESS,
    FAILED,
    INVALID_INPUT
)
from .cosmicLog import CosmicLog


class SshClient(object):
    """
    @Desc : SSH Library for Marvin.
    Facilitates SSH,SCP services to marvin users
    @Input: host: Host to connect
            port: port on host to connect
            user: Username to be used for connecting
            passwd: Password for connection
            retries and delay applies for establishing connection
            timeout : Applies while executing command
    """

    def __init__(self, host, port, user, password, retries=60, delay=10, key_pair_files=None,
                 timeout=10.0, banner_timeout=60.0):
        self.host = None
        self.port = 22
        self.user = user
        self.passwd = password
        self.keyPairFiles = key_pair_files
        self.ssh = SSHClient()
        self.ssh.set_missing_host_key_policy(AutoAddPolicy())
        self.retryCnt = retries
        self.delay = delay
        self.timeout = timeout
        self.banner_timeout = banner_timeout
        self.logger = CosmicLog('ssh').get_logger()

        # Check invalid host value and raise exception
        # At least host is required for connection
        if host is not None and host != '':
            self.host = host
        if port is not None and port >= 0:
            self.port = port
        if self.create_connection() == FAILED:
            raise Exception("Connection Failed")

    def execute(self, command):
        stdin, stdout, stderr = self.ssh.exec_command(command)
        output = stdout.readlines()
        errors = stderr.readlines()
        results = []
        if output is not None and len(output) == 0:
            if errors is not None and len(errors) > 0:
                for error in errors:
                    results.append(error.rstrip())

        else:
            for strOut in output:
                results.append(strOut.rstrip())
        self.logger.debug("Executing command via host %s: %s Output: %s" % (str(self.host), command, results))
        return results

    def create_connection(self):
        """
        @Name: createConnection
        @Desc: Creates an ssh connection for
               retries mentioned,along with sleep mentioned
        @Output: SUCCESS on successful connection
                 FAILED If connection through ssh failed
        """
        ret = FAILED
        while self.retryCnt >= 0:
            try:
                self.logger.debug("Trying SSH Connection to host %s on port %s as user %s. RetryCount: %s" %
                                  (self.host, str(self.port), self.user, str(self.retryCnt)))
                if self.keyPairFiles is None:
                    self.ssh.connect(hostname=self.host,
                                     port=self.port,
                                     username=self.user,
                                     password=self.passwd,
                                     timeout=self.timeout,
                                     banner_timeout=self.banner_timeout)
                else:
                    self.ssh.connect(hostname=self.host,
                                     port=self.port,
                                     username=self.user,
                                     password=self.passwd,
                                     key_filename=self.keyPairFiles,
                                     timeout=self.timeout,
                                     banner_timeout=self.banner_timeout,
                                     look_for_keys=False
                                     )
                self.logger.debug("Connection to host %s on port %s is SUCCESSFUL" % (str(self.host), str(self.port)))
                ret = SUCCESS
                break
            except BadHostKeyException as e:
                self.logger.debug("Failed to create connection: %s" % e)
            except AuthenticationException as e:
                self.logger.debug("Failed to create connection: %s" % e)
            except SSHException as e:
                self.logger.debug("Failed to create connection: %s" % e)
            except socket.error as e:
                self.logger.debug("Failed to create connection: %s" % e)
            except Exception as e:
                self.logger.debug("Failed to create connection: %s" % e)
                self.ssh.close()
            finally:
                if self.retryCnt == 0 or ret == SUCCESS:
                    break
                self.retryCnt -= 1
                time.sleep(self.delay)
        return ret

    def run_command(self, command):
        """
        @Name: runCommand
        @Desc: Runs a command over ssh and
               returns the result along with status code
        @Input: command to execute
        @Output: 1: status of command executed.
                 SUCCESS : If command execution is successful
                 FAILED    : If command execution has failed
                 2: stdin,stdout,stderr values of command output
        """
        ret = {"status": FAILED, "stdin": None, "stdout": None,
               "stderr": INVALID_INPUT}
        if command is None or command == '':
            return ret
        try:
            stdin, stdout, stderr = self.ssh.exec_command(command, timeout=self.timeout)
            if stdout is not None:
                status_check = stdout.channel.recv_exit_status()
                if status_check == 0:
                    ret["status"] = SUCCESS
                ret["stdout"] = stdout.readlines()
                if stderr is not None:
                    ret["stderr"] = stderr.readlines()
        except Exception as e:
            self.logger.debug("Failed to run command: %s" % e)
        finally:
            self.logger.debug("Connection to host %s on port %s is SUCCESSFUL" % (str(self.host), command))
            return ret

    def scp(self, src_file, dest_path):
        transport = Transport((self.host, int(self.port)))
        transport.connect(username=self.user, password=self.passwd)
        sftp = SFTPClient.from_transport(transport)
        try:
            sftp.put(src_file, dest_path)
        except IOError as e:
            raise e

    def __del__(self):
        self.close()

    def close(self):
        if self.ssh is not None:
            self.ssh.close()
            self.ssh = None

    def _ssh_available(self):
        """
        Helper to check if SSH is available as paramiko has a bug doing this.
        """
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.connect((self.host, self.port))
        data = s.recv(1024)
        s.close()
        return 'SSH-' in str(data[:4])
