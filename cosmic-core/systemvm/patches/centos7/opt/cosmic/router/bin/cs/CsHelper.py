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
import cs.utils as utils


def mkdir(name, mode, fatal):
    try:
        os.makedirs(name, mode)
    except OSError as e:
        if e.errno != 17:
            print("failed to make directories " + name + " due to :" + e.strerror)
            if fatal:
                sys.exit(1)

def execute(command, wait=True):
    """ Execute command """
    logging.debug("Executing: %s" % command)
    p = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True)
    if wait:
        return p.communicate()[0].splitlines()


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


def service(name, op):
    execute("systemctl %s %s" % (op, name))
    logging.info("systemctl %s %s" % (op, name))


def start_if_stopped(name):
    logging.info("Start if stopped: %s" % name)

    ret = execute2("systemctl status %s" % name)
    if ret.returncode:
        execute2("systemctl start %s" % name)

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
