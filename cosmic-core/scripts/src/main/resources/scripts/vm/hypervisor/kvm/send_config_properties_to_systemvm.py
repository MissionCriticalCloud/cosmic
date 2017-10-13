#!/usr/bin/python

# This script connects to the system vm Qemu Guest Agent and writes the
# authorized_keys and cmdline data to /var/cache/cloud. The system VM then
# reads processes these files in cloud_early_config
#

import argparse
import os
import json
import base64
import sys

SOCK_FILE = "/var/lib/libvirt/qemu/{name}.agent"
PUB_KEY_FILE = "/root/.ssh/id_rsa.pub.cloud"
MESSAGE = "pubkey:{key}\ncmdline:{cmdline}\n"
CMDLINE_INCOMING = "/var/cache/cloud/cmdline_incoming"
CMDLINE_FILE = "/var/cache/cloud/cmdline"
AUTHORIZED_KEYS_FILE_ROOT = "/root/.ssh/authorized_keys"
AUTHORIZED_KEYS_FILE_CACHE = "/var/cache/cloud/authorized_keys"

FILE_OPEN_WRITE = """{"execute":"guest-file-open", "arguments":{"path":"%s","mode":"w+"}}"""
FILE_OPEN_READ = """{"execute":"guest-file-open", "arguments":{"path":"%s","mode":"r"}}"""
FILE_WRITE = """{"execute":"guest-file-write", "arguments":{"handle":%s,"buf-b64":"%s"}}"""
FILE_CLOSE = """{"execute":"guest-file-close", "arguments":{"handle":%s}}"""
FILE_READ = """{"execute":"guest-file-read", "arguments":{"handle":%s,"count":%s}}"""

def EXE(param):
    cmd = """virsh qemu-agent-command %s '%s' """ % (arguments.name, param)
    print "Exe command:%s" % cmd
    stream = os.popen(cmd).read()
    return None if not stream else json.loads(stream)


def write_guest_file(path, content):
    file_handle = -1
    try:
        file_handle = EXE(FILE_OPEN_WRITE % path)["return"]
        write_count = EXE(FILE_WRITE % (file_handle, content))["return"]["count"]
        print "Write count: " + str(write_count)
    except Exception as ex:
        print Exception, ":", ex
        print "ERROR: Something went wrong, exiting."
        sys.exit(1)
    finally:
        if file_handle > -1:
            EXE(FILE_CLOSE % file_handle)
    return write_count


def read_guest_file(path, write_count):
    file_handle = -1
    try:
        file_handle = EXE(FILE_OPEN_READ % path)["return"]
        result = EXE(FILE_READ % (file_handle, write_count))
        read_count = result["return"]["count"]
        print "Read count: " + str(read_count)
        print "Content from read-back: " + base64.b64decode(result["return"]["buf-b64"])
    except Exception as ex:
        print Exception, ":", ex
        print "ERROR: Something went wrong, exiting."
        sys.exit(1)
    finally:
        if file_handle > -1:
            EXE(FILE_CLOSE % file_handle)
    return read_count


def get_key(key_file):
    if not os.path.exists(key_file):
        print("ERROR: ssh public key not found on host at {0}".format(key_file))
        print "ERROR: Something went wrong, exiting."
        sys.exit(1)

    try:
        with open(key_file, "r") as f:
            pub_key = f.read()
    except IOError as e:
        print("ERROR: unable to open {0} - {1}".format(key_file, e.strerror))
        print "ERROR: Something went wrong, exiting."
        sys.exit(1)

    return pub_key


def write_file(file_in_vm, data):
    try:
        content = base64.standard_b64encode(data)
        write_count = write_guest_file(file_in_vm, content)
        return write_count
    except Exception as ex:
        print "Warning: it was not possible to write to the Qemu Guest Agent at this time. Will try again later."


def compare_write_read(file, write_count, read_count):
    print "Result for %s: write %d read %d" % (file, write_count, read_count)
    if write_count != read_count:
        print "ERROR: Write count count doesn't match read count. File wasn't written successfully. Aborting."
        sys.exit(1)
    print "All fine for %s, read count matches write count." % (file)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Send configuration to system VM socket")
    parser.add_argument("-n", "--name", required=True, help="Name of VM")
    parser.add_argument("-p", "--cmdline", required=True, help="Command line")

    arguments = parser.parse_args()

    # cmdline file
    cmdline = arguments.cmdline.replace("%", " ")
    write_count = write_file(CMDLINE_FILE, cmdline)
    read_count = read_guest_file(CMDLINE_FILE, write_count)
    compare_write_read(CMDLINE_FILE, write_count, read_count)

    # write public key to authorized_keys
    pub_key = get_key(PUB_KEY_FILE)
    write_count = write_file(AUTHORIZED_KEYS_FILE_ROOT, pub_key)
    read_count = read_guest_file(AUTHORIZED_KEYS_FILE_ROOT, write_count)
    compare_write_read(AUTHORIZED_KEYS_FILE_ROOT, write_count, read_count)

    write_count = write_file(AUTHORIZED_KEYS_FILE_CACHE, pub_key)
    read_count = read_guest_file(AUTHORIZED_KEYS_FILE_CACHE, write_count)
    compare_write_read(AUTHORIZED_KEYS_FILE_CACHE, write_count, read_count)

    # write file when done
    write_count = write_file(CMDLINE_INCOMING, "DONE")
    read_count = read_guest_file(CMDLINE_INCOMING, write_count)
    compare_write_read(CMDLINE_INCOMING, write_count, read_count)
