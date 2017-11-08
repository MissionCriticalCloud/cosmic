import logging

from CsDatabag import CsDatabag
from keepalived import Keepalived
from firewall import Firewall


class CsNetwork(CsDatabag):
    def __init__(self, config, cs_databag):
        super(CsNetwork, self).__init__(cs_databag)

        self.config = config

        self.keepalived = Keepalived(self.config, self.dbag)
        self.firewall = Firewall(self.config, self.dbag)

    def sync(self):
        logging.debug("Starting sync of network!")
        logging.debug(self.dbag)

        self.keepalived.sync()
        self.firewall.sync()
