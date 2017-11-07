import logging

from CsDatabag import CsDatabag
from CsKeepalived import CsKeepalived


class CsNetwork(CsDatabag):
    def __init__(self, cs_databag):
        super(CsNetwork, self).__init__(cs_databag)

        self.cs_keepalived = CsKeepalived(self.dbag)

    def sync(self):
        logging.debug("Starting sync of network!")
        logging.debug(self.dbag)

        self.cs_keepalived.sync()
