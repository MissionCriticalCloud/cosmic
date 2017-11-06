import logging

from CsDatabag import CsDatabag


class CsNetwork(CsDatabag):
    def __init__(self, cs_databag):
        super(CsNetwork, self).__init__(cs_databag)

    def sync(self):
        logging.debug("Starting sync of network!")

        logging.debug(self.dbag)
