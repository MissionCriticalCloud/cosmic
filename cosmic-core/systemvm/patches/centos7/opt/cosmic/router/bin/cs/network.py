import logging

from conntrackd import Conntrackd
from dhcp_service import DhcpService
from dhcp_vm import DhcpVm
from firewall import Firewall
from keepalived import Keepalived
from metadata_service import MetadataService
from metadata_vm import MetadataVm
from password_service import PasswordService
from vpn import Vpn


class Network:

    def __init__(self, config):
        self.config = config

        self.keepalived = Keepalived(self.config)
        self.conntrackd = Conntrackd(self.config)
        self.firewall = Firewall(self.config)
        self.password_service = PasswordService(self.config)
        self.metadata_service = MetadataService(self.config)
        self.metadata_vm = MetadataVm(self.config)
        self.dhcp_service = DhcpService(self.config)
        self.dhcp_vm = DhcpVm(self.config)
        self.vpn = Vpn(self.config)

    def sync(self):
        logging.debug("Starting sync of network!")
        logging.debug(self.config.dbag_network_overview)

        self.keepalived.sync()
        self.conntrackd.sync()
        self.firewall.sync()
        self.password_service.sync()
        self.metadata_service.sync()
        self.metadata_vm.sync()
        self.dhcp_service.sync()
        self.dhcp_vm.sync()
        self.vpn.sync()
