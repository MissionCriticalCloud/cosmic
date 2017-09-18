#!/usr/bin/python


import glob
import logging
import os.path
import sys

import configure
from cs.CsVmPassword import *
from merge import QueueFile

OCCURRENCES = 1

logging.basicConfig(filename='/var/log/cloud.log', level=logging.DEBUG,
                    format='%(asctime)s  %(filename)s %(funcName)s:%(lineno)d %(message)s')

# first commandline argument should be the file to process
if len(sys.argv) != 2:
    print("[ERROR]: Invalid usage")
    sys.exit(1)

# FIXME we should get this location from a configuration class
jsonPath = "/var/cache/cloud/%s"
jsonCmdConfigPath = jsonPath % sys.argv[1]
currentGuestNetConfig = "/etc/cosmic/router/guestnetwork.json"


def finish_config():
    # Converge
    returncode = configure.main(sys.argv)
    sys.exit(returncode)


def process(do_merge=True):
    print("[INFO] Processing JSON file %s" % sys.argv[1])
    qf = QueueFile()
    qf.setFile(sys.argv[1])
    qf.do_merge = do_merge
    qf.load(None)
    return qf


def process_file():
    print("[INFO] process_file")
    process()
    # Converge
    finish_config()


def process_vmpasswd():
    print("[INFO] process_vmpassword")
    qf = process(False)
    print("[INFO] Sending password to password server")
    CsPassword(qf.getData())


filename = min(glob.iglob(jsonCmdConfigPath + '*'), key=os.path.getctime)
if not (os.path.isfile(filename) and os.access(filename, os.R_OK)):
    print("[ERROR] update_config.py :: You are telling me to process %s, but i can't access it" % jsonCmdConfigPath)
    sys.exit(1)

if sys.argv[1].startswith("vm_password.json"):
    print("[INFO] update_config.py :: Processing incoming vm_passwd file => %s" % sys.argv[1])
    process_vmpasswd()
else:
    print("[INFO] update_config.py :: Processing incoming file => %s" % sys.argv[1])
    process_file()
