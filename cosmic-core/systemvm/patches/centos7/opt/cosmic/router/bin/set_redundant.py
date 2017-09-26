#!/usr/bin/python
# -- coding: utf-8 --


# This file is used by the tests to switch the redundancy status

import logging
from optparse import OptionParser

from cs.CsConfig import CsConfig

parser = OptionParser()
parser.add_option("-e", "--enable",
                  action="store_true", default=False, dest="enable",
                  help="Set router redundant")
parser.add_option("-d", "--disable",
                  action="store_true", default=False, dest="disable",
                  help="Set router non redundant")

(options, args) = parser.parse_args()

config = CsConfig()
logging.basicConfig(level=logging.DEBUG, format='%(asctime)s  %(filename)s %(funcName)s:%(lineno)d %(message)s')

config.set_cl()

if options.enable:
    config.get_cmdline().set_redundant("true")
if options.disable:
    config.get_cmdline().set_redundant("false")

config.get_cmdline().save()
