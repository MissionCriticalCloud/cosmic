"""Utilities functions
"""

import datetime
import email
import imaplib
import random
import socket
import string
import time
import urlparse
from platform import system

from marvin.cloudstackException import printException
from marvin.codes import (
    SUCCESS,
    FAIL,
    PASS,
    MATCH_NOT_FOUND,
    INVALID_INPUT,
    EMPTY_LIST,
    FAILED)
from marvin.sshClient import SshClient


def _configure_ssh_credentials(hypervisor):
    ssh_command = "ssh -i ~/.ssh/id_rsa.cloud -ostricthostkeychecking=no "

    if (str(hypervisor).lower() == 'vmware'):
        ssh_command = "ssh -i /var/cloudstack/management/.ssh/id_rsa -ostricthostkeychecking=no "

    return ssh_command


def _configure_timeout(hypervisor):
    timeout = 5

    return timeout


def _execute_ssh_command(hostip, port, username, password, ssh_command):
    # SSH to the machine
    ssh = SshClient(hostip, port, username, password)
    # Ensure the SSH login is successful
    while True:
        res = ssh.execute(ssh_command)
        if "Connection refused".lower() in res[0].lower():
            pass
        elif res[0] != "Host key verification failed.":
            break
        elif timeout == 0:
            break

        time.sleep(5)
        timeout = timeout - 1
    return res


def restart_mgmt_server(server):
    """Restarts the management server"""

    try:
        # Get the SSH client
        ssh = is_server_ssh_ready(
            server["ipaddress"],
            server["port"],
            server["username"],
            server["password"],
        )
        result = ssh.execute("/etc/init.d/cloud-management restart")
        res = str(result)
        # Server Stop - OK
        # Server Start - OK
        if res.count("OK") != 2:
            raise ("ErrorInReboot!")
    except Exception as e:
        raise e
    return


def fetch_latest_mail(services, from_mail):
    """Fetch mail"""

    # Login to mail server to verify email
    mail = imaplib.IMAP4_SSL(services["server"])
    mail.login(
        services["email"],
        services["password"]
    )
    mail.list()
    mail.select(services["folder"])
    date = (datetime.date.today() - datetime.timedelta(1)).strftime("%d-%b-%Y")

    result, data = mail.uid(
        'search',
        None,
        '(SENTSINCE {date} HEADER FROM "{mail}")'.format(
            date=date,
            mail=from_mail
        )
    )
    # Return False if email is not present
    if data == []:
        return False

    latest_email_uid = data[0].split()[-1]
    result, data = mail.uid('fetch', latest_email_uid, '(RFC822)')
    raw_email = data[0][1]
    email_message = email.message_from_string(raw_email)
    result = get_first_text_block(email_message)
    return result


def get_first_text_block(email_message_instance):
    """fetches first text block from the mail"""
    maintype = email_message_instance.get_content_maintype()
    if maintype == 'multipart':
        for part in email_message_instance.get_payload():
            if part.get_content_maintype() == 'text':
                return part.get_payload()
    elif maintype == 'text':
        return email_message_instance.get_payload()


def random_gen(id=None, size=6, chars=string.ascii_uppercase + string.digits):
    """Generate Random Strings of variable length"""
    randomstr = ''.join(random.choice(chars) for x in range(size))
    if id:
        return ''.join([id, '-', randomstr])
    return randomstr


def cleanup_resources(api_client, resources, logger=None):
    if logger is not None:
        logger.debug("Cleaning up all resources: %s" % resources)
    """Delete resources"""
    for obj in resources:
        if logger is not None:
            logger.debug("Deleting %s" % obj.id)
        obj.delete(api_client)


def is_server_ssh_ready(ipaddress, port, username, password, retries=20, retryinterv=30, timeout=10.0,
                        keyPairFileLocation=None):
    '''
    @Name: is_server_ssh_ready
    @Input: timeout: tcp connection timeout flag,
            others information need to be added
    @Output:object for SshClient
    Name of the function is little misnomer and is not
              verifying anything as such mentioned
    '''

    try:
        ssh = SshClient(
            host=ipaddress,
            port=port,
            user=username,
            passwd=password,
            keyPairFiles=keyPairFileLocation,
            retries=retries,
            delay=retryinterv,
            timeout=timeout)
    except Exception, e:
        raise Exception("SSH connection has Failed. Waited %ss. Error is %s" % (retries * retryinterv, str(e)))
    else:
        return ssh


def format_volume_to_ext3(ssh_client, device="/dev/sda"):
    """Format attached storage to ext3 fs"""
    cmds = [
        "echo -e 'n\np\n1\n\n\nw' | fdisk %s" % device,
        "mkfs.ext3 %s1" % device,
    ]
    for c in cmds:
        ssh_client.execute(c)


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


def get_process_status(hostip, port, username, password, linklocalip, command, hypervisor=None):
    """Double hop and returns a command execution result"""

    ssh_command = _configure_ssh_credentials(hypervisor)

    ssh_command = ssh_command + \
                  "-oUserKnownHostsFile=/dev/null -p 3922 %s %s" % (
                      linklocalip,
                      command)
    timeout = _configure_timeout(hypervisor)

    result = _execute_ssh_command(hostip, port, username, password, ssh_command)
    return result


def isAlmostEqual(first_digit, second_digit, range=0):
    digits_equal_within_range = False

    try:
        if ((first_digit - range) < second_digit < (first_digit + range)):
            digits_equal_within_range = True
    except Exception as e:
        raise e
    return digits_equal_within_range


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


def validateList(inp):
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


def verifyElementInList(inp, toverify, responsevar=None, pos=0):
    '''
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
    '''
    if toverify is None or toverify == '' \
            or pos is None or pos < -1 or pos == '':
        return [FAIL, INVALID_INPUT]
    out = validateList(inp)
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


def checkVolumeSize(ssh_handle=None,
                    volume_name="/dev/sda",
                    cmd_inp="/sbin/fdisk -l | grep Disk",
                    size_to_verify=0):
    '''
    @Name : getDiskUsage
    @Desc : provides facility to verify the volume size against the size to verify
    @Input: 1. ssh_handle : machine against which to execute the disk size cmd
            2. volume_name : The name of the volume against which to verify the size
            3. cmd_inp : Input command used to veify the size
            4. size_to_verify: size against which to compare.
    @Output: Returns FAILED in case of an issue, else SUCCESS
    '''
    try:
        if ssh_handle is None or cmd_inp is None or volume_name is None:
            return INVALID_INPUT

        cmd = cmd_inp
        '''
        Retrieve the cmd output
        '''
        if system().lower() != "windows":
            fdisk_output = ssh_handle.runCommand(cmd_inp)
            if fdisk_output["status"] != SUCCESS:
                return FAILED
            for line in fdisk_output["stdout"]:
                if volume_name in line:
                    parts = line.strip().split()
                    if str(parts[-2]) == str(size_to_verify):
                        return [SUCCESS, str(parts[-2])]
            return [FAILED, "Volume Not Found"]
    except Exception, e:
        printException(e)
        return [FAILED, str(e)]


def validateState(apiclient, obj, state, timeout=600, interval=5):
    """Check if an object is in the required state
       returnValue: List[Result, Reason]
             @Result: PASS if object is in required state,
                      else FAIL
             @Reason: Reason for failure in case Result is FAIL
    """

    returnValue = [FAIL, "%s state not trasited to %s, operation timed out" % (obj.__class__.__name__, state)]

    while timeout > 0:
        try:
            objects = obj.__class__.list(apiclient, id=obj.id)
            validationresult = validateList(objects)
            if validationresult[0] == FAIL:
                raise Exception("%s list validation failed: %s" % (obj.__class__.__name__, validationresult[2]))
            elif obj.state_check_function(objects, state):
                returnValue = [PASS, None]
                break
        except Exception as e:
            returnValue = [FAIL, e]
            break
        time.sleep(interval)
        timeout -= interval
    return returnValue


def key_maps_to_value(dictionary, key):
    return key in dictionary and dictionary[key] is not None


def wait_until(retry_interval=2, no_of_times=2, callback=None, *callback_args):
    """ Utility method to try out the callback method at most no_of_times with a interval of retry_interval,
        Will return immediately if callback returns True. The callback method should be written to return a list of values first being a boolean """

    if callback is None:
        raise ("Bad value for callback method !")

    wait_result = False
    for i in range(0,no_of_times):
        time.sleep(retry_interval)
        wait_result, return_val = callback(*callback_args)
        if not(isinstance(wait_result, bool)):
            raise ("Bad parameter returned from callback !")
        if wait_result :
            break

    return wait_result, return_val
