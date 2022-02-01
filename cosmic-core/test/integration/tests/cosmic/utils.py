import random
import string
import sys
import time
import traceback

from urllib.parse import urlparse
from .SshClient import SshClient
from .codes import *


class AttrDict(dict):
    def __init__(self, *args, **kwargs):
        super(AttrDict, self).__init__(*args, **kwargs)
        self.__dict__ = self


def random_gen(uuid=None, size=6, chars=string.ascii_uppercase + string.digits):
    """Generate Random Strings of variable length"""
    random_string = ''.join(random.choice(chars) for _ in range(size))
    if uuid:
        return ''.join([uuid, '-', random_string])
    return random_string


def cleanup_resources(api_client, resources, logger=None):
    """Delete resources"""
    if logger is not None:
        logger.debug("Cleaning up all resources:")
    for obj in resources:
        if logger is not None:
            logger.debug(" -> %s %s" % (obj.__class__.__name__, obj.id))
        obj.delete(api_client)


def validate_list(inp, k):
    """
    @name: validateList
    @Description: 1. A utility function to validate
                 whether the input passed is a list
              2. The list is empty or not
              3. If it is list and not empty, return PASS and first element
              4. If not reason for FAIL
        @Input: Input to be validated
        @output: List, containing [ Result,FirstElement,Reason ]
                 Ist Argument('Result') : FAIL : If it is not a list
                                          If it is list but empty
                                         PASS : If it is list and not empty
                 IInd Argument('FirstElement'): If it is list and not empty,
                                           then first element
                                            in it, default to None
                 IIIrd Argument( 'Reason' ):  Reason for failure ( FAIL ),
                                              default to None.
                                              INVALID_INPUT
                                              EMPTY_LIST
    """
    ret = [FAIL, None, None]
    if inp.get(k, None) is None:
        ret[2] = INVALID_INPUT
        return ret
    if not isinstance(inp[k], list):
        ret[2] = INVALID_INPUT
        return ret
    if len(inp[k]) == 0:
        ret[2] = EMPTY_LIST
        return ret
    return [PASS, inp[k][0], None]


def validate_state(api_client, obj, state, timeout=600, interval=5):
    """Check if an object is in the required state
       returnValue: List[Result, Reason]
             @Result: PASS if object is in required state,
                      else FAIL
             @Reason: Reason for failure in case Result is FAIL
    """

    return_value = [FAIL, "%s state not transited to %s, operation timed out" % (obj.__class__.__name__, state)]

    while timeout > 0:
        try:
            objects = obj.__class__.list(api_client, id=obj.id)
            validation_result = validate_list(objects)
            if validation_result[0] == FAIL:
                raise Exception("%s list validation failed: %s" % (obj.__class__.__name__, validation_result[2]))
            elif obj.state_check_function(objects, state):
                return_value = [PASS, None]
                break
        except Exception as e:
            return_value = [FAIL, e]
            break
        time.sleep(interval)
        timeout -= interval
    return return_value


def is_server_ssh_ready(ip_address, port, username, password, retries=20, retry_interval=30, timeout=10.0,
                        key_pair_file_location=None):
    """
    @Name: is_server_ssh_ready
    @Input: timeout: tcp connection timeout flag,
            others information need to be added
    @Output:object for SshClient
    Name of the function is little misnomer and is not
              verifying anything as such mentioned
    """

    try:
        ssh = SshClient(
            host=ip_address,
            port=port,
            user=username,
            password=password,
            key_pair_files=key_pair_file_location,
            retries=retries,
            delay=retry_interval,
            timeout=timeout)
    except Exception as e:
        raise Exception("SSH connection has Failed. Waited %ss. Error is %s" % (retries * retry_interval, str(e)))
    else:
        return ssh


def attr(*args, **kwargs):
    """Decorator that adds attributes to classes or functions
    for use with the Attribute (-a) plugin.
    """

    def wrap_ob(ob):
        for name in args:
            setattr(ob, name, True)
        for name, value in kwargs.items():
            setattr(ob, name, value)
        return ob

    return wrap_ob


def waitforjob(api_client, jobid=None, retries=120):
    while True:
        if retries < 0:
            break
        # jobstatus 0 = Job still running
        jobstatus = api_client.queryAsyncJobResult(jobid=jobid)
        # jobstatus 1 = Job done successfully
        if int(jobstatus['jobstatus']) == 1:
            return True
        # jobstatus 2 = Job has an error
        if int(jobstatus['jobstatus']) == 2:
            break
        retries -= 1
        time.sleep(1)
    return False


def wait_vpc_ready(cls):
    retries = 10
    while retries > 0:
        if cls.is_state_ok():
            break
        time.sleep(5)
        retries -= 1
    if retries == 0:
        raise Exception("VPC is not in the correct state!")


def printException(e):
    if e is not None:
        exc_type, exc_value, exc_traceback = sys.exc_info()
        return str(repr(traceback.print_exception(exc_type, exc_value, exc_traceback)))
    else:
        return EXCEPTION_OCCURRED


def get_host_credentials(config, hostname):
    """Get login information for a host `hostip` (ipv4) from marvin's `config`

    @return the tuple username, password for the host else raise keyerror"""
    for zone in config.get('zones', []):
        for pod in zone.get('pods', []):
            for cluster in pod.get('clusters', []):
                for host in cluster.get('hosts', []):
                    url = host.get('url')
                    if str(url).startswith('http'):
                        hostname_marvin = urlparse.urlsplit(str(url)).netloc
                    else:
                        hostname_marvin = str(url)
                    if hostname == hostname_marvin:
                        return host.get('username'), host.get('password')
    raise KeyError("Please provide the marvin configuration file with credentials to your hosts")


def get_process_status(hostip, port, username, password, linklocalip, command):
    """Double hop and returns a command execution result"""
    ssh_command = _configure_ssh_credentials() + "-oUserKnownHostsFile=/dev/null -p 3922 %s %s" % (linklocalip, command)

    return _execute_ssh_command(hostip, port, username, password, ssh_command)


def _execute_ssh_command(hostip, port, username, password, ssh_command):
    # Timeout
    timeout = 5
    # SSH to the machine
    ssh = SshClient(hostip, port, username, password)
    # Ensure the SSH login is successful
    while True:
        res = ssh.execute(ssh_command)
        if "Connection refused".lower() in res[0].lower():
            time.sleep(10)
        elif res[0] != "Host key verification failed.":
            break
        elif timeout == 0:
            break

        time.sleep(5)
        timeout = timeout - 1
    return res


def _configure_ssh_credentials():
    return "ssh -i ~/.ssh/id_rsa.cloud -ostricthostkeychecking=no "
