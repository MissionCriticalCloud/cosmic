import random
import socket
import string
import time
import urlparse

from marvin.codes import (
    FAIL,
    PASS,
    MATCH_NOT_FOUND,
    INVALID_INPUT,
    EMPTY_LIST)
from marvin.utils.SshClient import SshClient


def _configure_ssh_credentials():
    return "ssh -i ~/.ssh/id_rsa.cloud -ostricthostkeychecking=no "


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
    except Exception, e:
        raise Exception("SSH connection has Failed. Waited %ss. Error is %s" % (retries * retry_interval, str(e)))
    else:
        return ssh


def get_host_credentials(config, hostip):
    """Get login information for a host `hostip` (ipv4) from marvin's `config`

    @return the tuple username, password for the host else raise keyerror"""
    for zone in config.zones:
        for pod in zone.pods:
            for cluster in pod.clusters:
                for host in cluster.hosts:
                    if str(host.url).startswith('http'):
                        hostname = urlparse.urlsplit(str(host.url)).netloc
                    else:
                        hostname = str(host.url)
                    try:
                        if socket.getfqdn(hostip) == socket.getfqdn(hostname):
                            return host.username, host.password
                    except socket.error, e:
                        raise Exception("Unresolvable host %s error is %s" % (hostip, e))
    raise KeyError("Please provide the marvin configuration file with credentials to your hosts")


def get_process_status(hostip, port, username, password, linklocalip, command):
    """Double hop and returns a command execution result"""
    ssh_command = _configure_ssh_credentials() + "-oUserKnownHostsFile=/dev/null -p 3922 %s %s" % (linklocalip, command)

    return _execute_ssh_command(hostip, port, username, password, ssh_command)


def xsplit(txt, seps):
    """
    Split a string in `txt` by list of delimiters in `seps`
    @param txt: string to split
    @param seps: list of separators
    @return: list of split units
    """
    default_sep = seps[0]
    for sep in seps[1:]:  # we skip seps[0] because that's the default separator
        txt = txt.replace(sep, default_sep)
    return [i.strip() for i in txt.split(default_sep)]


def validate_list(inp):
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
    if inp is None:
        ret[2] = INVALID_INPUT
        return ret
    if not isinstance(inp, list):
        ret[2] = INVALID_INPUT
        return ret
    if len(inp) == 0:
        ret[2] = EMPTY_LIST
        return ret
    return [PASS, inp[0], None]


def verify_element_in_list(inp, toverify, responsevar=None, pos=0):
    """
    @name: verifyElementInList
    @Description:
    1. A utility function to validate
    whether the input passed is a list.
    The list is empty or not.
    If it is list and not empty, verify
    whether a given element is there in that list or not
    at a given pos
    @Input:
             I   : Input to be verified whether its a list or not
             II  : Element to verify whether it exists in the list
             III : variable name in response object to verify
                   default to None, if None, we will verify for the complete
                   first element EX: state of response object object
             IV  : Position in the list at which the input element to verify
                   default to 0
    @output: List, containing [ Result,Reason ]
             Ist Argument('Result') : FAIL : If it is not a list
                                      If it is list but empty
                                      PASS : If it is list and not empty
                                              and matching element was found
             IIrd Argument( 'Reason' ): Reason for failure ( FAIL ),
                                        default to None.
                                        INVALID_INPUT
                                        EMPTY_LIST
                                        MATCH_NOT_FOUND
    """
    if toverify is None or toverify == '' \
            or pos is None or pos < -1 or pos == '':
        return [FAIL, INVALID_INPUT]
    out = validate_list(inp)
    if out[0] == FAIL:
        return [FAIL, out[2]]
    if len(inp) > pos:
        if responsevar is None:
            if inp[pos] == toverify:
                return [PASS, None]
        else:
            if responsevar in inp[pos].__dict__ and getattr(inp[pos], responsevar) == toverify:
                return [PASS, None]
            else:
                return [FAIL, MATCH_NOT_FOUND]
    else:
        return [FAIL, MATCH_NOT_FOUND]


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


def key_maps_to_value(dictionary, key):
    return key in dictionary and dictionary[key] is not None
