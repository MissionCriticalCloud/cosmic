#!/usr/bin/python
# -- coding: utf-8 --


import sys
import logging

from CsDatabag import CsDataBag


class CsVrConfig(CsDataBag):
    def process(self):
        logging.debug("Processing CsVrConfig file ==> %s" % self.dbag)

        for item in self.dbag:
            if item == "id":
                continue

            if item == "source_nat_list":
                self._configure_firewall(self.dbag[item])

            result = self.__update(self.dbag[item])
            logging.debug("Processing item from data bag: %s, returncode: %s" % (self.dbag[item], result))
            if result is not None and result is False:
                logging.debug("Executing CsVrConfig command returned False, exiting.")
                sys.exit(1)

    def __update(self, dbag):
        # For now no specific private gateway config yet
        return True

    def _configure_firewall(self, sourcenatlist):
        firewall = self.config.get_fw()

        for cidr in sourcenatlist.split(','):
            firewall.append(["filter", "", "-A SOURCE_NAT_LIST -o eth1 -s %s -j ACCEPT" % cidr])
