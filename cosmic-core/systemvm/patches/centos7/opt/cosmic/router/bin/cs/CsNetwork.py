import logging

from firewall import Firewall
from keepalived import Keepalived


class CsNetwork(object):
    def __init__(self, config):
        self.config = config

        self.keepalived = Keepalived(self.config)
        self.firewall = Firewall(self.config)

    def sync(self):
        logging.debug("Starting sync of network!")
        logging.debug(self.config.dbag_network_overview)

        self.keepalived.sync()
        self.firewall.sync()
