# -- coding: utf-8 --

""" General helper functions
for use in the configuration process

"""
import logging
import os.path
import re
import shutil
import subprocess
import sys
from subprocess import check_output

from netaddr import *

# @TODO fix hardcoded eth1 public interface -- is OK for now since it's for redundant VPCs and these have always a public interface. Although that is still an assumption and may not be true anymore some day
STATE_COMMANDS = { "router": "ip addr | grep eth2 | grep state | awk '{print $9;}' | xargs bash -c 'if [ \"$0\" == \"UP\" ]; then echo \"MASTER\"; else echo \"BACKUP\"; fi'",
                   "vpcrouter": "ip addr | grep eth1 | grep state | awk '{print $9;}' | xargs bash -c 'if [ $0 == \"UP\" ];     then echo \"MASTER\"; else echo \"BACKUP\"; fi'" }


def get_device_from_mac_address(macaddress):
    logging.info("Looking for interface with macaddress " + macaddress)
    device = execute("find /sys/class/net/*/address | xargs grep %s | cut -d\/ -f5 " % macaddress)
    if not device:
        return False
    logging.info("Looking for mac address " + macaddress + " we found matching interface => " + str(device))
    return device[0]


def get_systemvm_version():
    try:
        with open("/etc/cosmic-release") as file:
            content = file.readlines()
        version_data = content[0].split(" ")[2]
        version = ""
        for line in version_data.split("."):
            version += str(line).zfill(2)
        logging.info("This systemvm has version " + str(version))
        return int(version)
    except:
        logging.info("Got an exception while trying to find systemvm version. Returning version 0")
        return 0


def is_mounted(name):
    for i in execute("mount"):
        vals = i.lstrip().split()
        if vals[0] == "tmpfs" and vals[2] == name:
            return True
    return False


def mount_tmpfs(name):
    if not is_mounted(name):
        execute("mount tmpfs %s -t tmpfs" % name)


def umount_tmpfs(name):
    if is_mounted(name):
        execute("umount %s" % name)


def rm(name):
    os.remove(name) if os.path.isfile(name) else None


def rmdir(name):
    if name:
        shutil.rmtree(name, True)


def mkdir(name, mode, fatal):
    try:
        os.makedirs(name, mode)
    except OSError as e:
        if e.errno != 17:
            print("failed to make directories " + name + " due to :" + e.strerror)
            if fatal:
                sys.exit(1)


def updatefile(filename, val, mode):
    """ add val to file """
    handle = open(filename, 'r')
    for line in handle.read():
        if line.strip().lstrip() == val:
            return
    # set the value
    handle.close()
    handle = open(filename, mode)
    handle.write(val)
    handle.close()


def bool_to_yn(val):
    if val:
        return "yes"
    return "no"


def get_device_info():
    """ Returns all devices on system with their ipv4 ip netmask """
    list = []
    for i in execute("ip addr show"):
        vals = i.strip().lstrip().rstrip().split()
        if vals[0] == "inet":
            to = { }
            to['ip'] = vals[1]
            to['dev'] = vals[-1]
            to['network'] = IPNetwork(to['ip'])
            to['dnsmasq'] = False
            list.append(to)
    return list


def get_domain():
    for line in open("/etc/resolv.conf"):
        vals = line.lstrip().split()
        if vals[0] == "domain":
            return vals[1]
    return "cloudnine.internal"


def get_device(ip):
    """ Returns the device which has a specific ip
    If the ip is not found returns an empty string
    """
    for i in execute("ip addr show"):
        vals = i.strip().lstrip().rstrip().split()
        if vals[0] == "inet":
            if vals[1].split('/')[0] == ip:
                return vals[-1]
    return ""


def get_ip(device):
    """ Return first ip on an interface """
    cmd = "ip addr show dev %s" % device
    for i in execute(cmd):
        vals = i.lstrip().split()
        if (vals[0] == 'inet'):
            return vals[1]
    return ""


def definedinfile(filename, val):
    """ Check if val is defined in the file """
    for line in open(filename):
        if re.search(val, line):
            return True
    return False


def addifmissing(filename, val):
    """ Add something to a file
    if it is not already there """
    if not os.path.isfile(filename):
        logging.debug("File %s doesn't exist, so create" % filename)
        open(filename, "w").close()
    if not definedinfile(filename, val):
        updatefile(filename, val + "\n", "a")
        logging.debug("Added %s to file %s" % (val, filename))
        return True
    return False


def get_hostname():
    for line in open("/etc/hostname"):
        return line.strip()


def execute(command, wait=True):
    """ Execute command """
    logging.debug("Executing: %s" % command)
    p = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True)
    if wait:
        return p.communicate()[0].splitlines()


def save_iptables(command, iptables_file):
    """ Execute command """
    logging.debug("Saving iptables for %s" % command)

    result = execute(command)
    fIptables = open(iptables_file, "w+")

    for line in result:
        fIptables.write(line)
        fIptables.write("\n")
    fIptables.close()


def execute2(command, log=True):
    """ Execute command """
    logging.debug("Executing: %s" % command)
    p = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True)
    p.wait()
    if log:
        out, err = p.communicate()
        if out:
            logging.info(out)
        if err:
            logging.error(err)
    return p


def get_output_of_command(command):
    """ Execute command """
    logging.debug("Executing command and returning output: %s" % command)
    return check_output(command, shell=True)


def service(name, op):
    execute("systemctl %s %s" % (op, name))
    logging.info("systemctl %s %s" % (op, name))


def start_if_stopped(name):
    logging.info("Start if stopped: %s" % name)

    ret = execute2("systemctl status %s" % name)
    if ret.returncode:
        execute2("systemctl start %s" % name)


def hup_dnsmasq(name, user):
    pid = ""
    for i in execute("ps -ef | grep %s" % name):
        vals = i.lstrip().split()
        if (vals[0] == user):
            pid = vals[1]
    if pid:
        logging.info("Sent hup to %s", name)
        execute("kill -HUP %s" % pid)
    else:
        service("dnsmasq", "start")


def copy_if_needed(src, dest):
    """ Copy a file if the destination does not already exist
    """
    if os.path.isfile(dest):
        return
    copy(src, dest)


def copy(src, dest):
    """
    copy source to destination.
    """
    try:
        shutil.copy2(src, dest)
    except IOError:
        logging.error("Could not copy %s to %s" % (src, dest))
    else:
        logging.info("Copied %s to %s" % (src, dest))
