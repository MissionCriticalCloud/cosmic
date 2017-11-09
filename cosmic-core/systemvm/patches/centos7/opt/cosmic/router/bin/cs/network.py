import logging

from conntrackd import Conntrackd
from firewall import Firewall
from keepalived import Keepalived


class Network(object):
    def __init__(self, config):
        self.config = config

        self.keepalived = Keepalived(self.config)
        self.conntrackd = Conntrackd(self.config)
        self.firewall = Firewall(self.config)

    def sync(self):
        logging.debug("Starting sync of network!")
        logging.debug(self.config.dbag_network_overview)

        self.keepalived.sync()
        self.conntrackd.sync()
        self.firewall.sync()
