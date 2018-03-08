#!/usr/bin/python
# -- coding: utf-8 --

import logging

import CsHelper
from cs.CsFile import CsFile


class CsVrConfig(object):
    def __init__(self, config):
        self.config = config
        self.dbag = self.config.dbag_network_virtualrouter

    def process(self):
        logging.debug("Processing CsVrConfig file ==> %s" % self.dbag)

        for item in self.dbag:
            if item == "id":
                continue

            if item == "source_nat_list":
                self._configure_firewall(self.dbag[item])

    def _configure_firewall(self, sourcenatlist):
        firewall = self.config.get_fw()

        logging.debug("Processing source NAT list: %s" % sourcenatlist)
        for cidr in sourcenatlist.split(','):
            firewall.append(["filter", "", "-A SOURCE_NAT_LIST -o eth2 -s %s -j ACCEPT" % cidr])
