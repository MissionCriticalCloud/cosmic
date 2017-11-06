#!/usr/bin/python
# -- coding: utf-8 --


import sys
import logging

from CsDatabag import CsDatabag


class CsPrivateGateway(CsDatabag):
    def process(self):
        is_master = self.cl.is_master()
        if self.cl.is_redundant() and not is_master:
            logging.debug("Not processing CsPrivateGateway file ==> %s because redundant state is %s" %
                          (self.dbag, str(is_master)))
            return True

        logging.debug("Processing CsPrivateGateway file ==> %s" % self.dbag)

        for item in self.dbag:
            if item == "id":
                continue
            result = self.__update(self.dbag[item])
            logging.debug("Processing item from data bag: %s, returncode: %s" % (self.dbag[item], result))
            if result is not None and result is False:
                logging.debug("Executing CsPrivateGateway command returned False, exiting.")
                sys.exit(1)

    def __update(self, dbag):
        # For now no specific private gateway config yet
        return True
