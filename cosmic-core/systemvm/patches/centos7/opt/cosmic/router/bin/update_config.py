#!/usr/bin/python

import glob
import logging
import os.path
import sys

from . import configure
from .cs.CsHelper import mkdir
from .cs.CsPasswordService import CsPasswordServiceVMConfig
from .databag.merge import QueueFile

OCCURRENCES = 1

LOG_DIR="/var/log/cosmic/router"

if not os.path.isdir(LOG_DIR):
    mkdir(LOG_DIR, 0o755, False)

logging.basicConfig(filename="/var/log/cosmic/router/router.log", level=logging.DEBUG,
                    format='%(asctime)s %(levelname)s  %(filename)s %(funcName)s:%(lineno)d %(message)s')

# first commandline argument should be the file to process
if len(sys.argv) != 2:
    logging.error("Invalid usage")
    sys.exit(1)

# FIXME we should get this location from a configuration class
jsonPath = "/var/cache/cloud/%s"
jsonCmdConfigPath = jsonPath % sys.argv[1]


def finish_config():
    # Converge
    returncode = configure.main(sys.argv)
    sys.exit(returncode)


def process(do_merge=True):
    logging.info("Processing JSON file %s" % sys.argv[1])
    qf = QueueFile()
    qf.setFile(sys.argv[1])
    qf.do_merge = do_merge
    qf.load(None)
    return qf


def process_file():
    logging.info("process_file")
    process()
    # Converge
    finish_config()


def process_vmpasswd():
    logging.info("process_vmpassword")
    qf = process(False)
    logging.info("Sending password to password server")
    CsPasswordServiceVMConfig(qf.getData())


filename = min(glob.iglob(jsonCmdConfigPath + '*'), key=os.path.getctime)
if not (os.path.isfile(filename) and os.access(filename, os.R_OK)):
    logging.error("You are telling me to process %s, but i can't access it" % jsonCmdConfigPath)
    sys.exit(1)

if sys.argv[1].startswith("vm_password.json"):
    logging.info("Processing incoming vm_passwd file => %s" % sys.argv[1])
    process_vmpasswd()
else:
    logging.info("Processing incoming file => %s" % sys.argv[1])
    process_file()
