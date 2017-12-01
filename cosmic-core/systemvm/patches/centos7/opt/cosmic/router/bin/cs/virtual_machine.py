import logging

from conntrackd import Conntrackd
from dhcp_service import DhcpService
from dhcp_vm import DhcpVm
from firewall import Firewall
from keepalived import Keepalived
from metadata_service import MetadataService
from password_service import PasswordService


class VirtualMachine(object):
    def __init__(self, config):
        self.config = config

        self.dhcp_vm = DhcpVm(self.config)


    def sync(self):
        logging.debug("Starting sync of virtual machine!")
        logging.debug(self.config.dbag_network_overview)

        self.dhcp_vm.sync()
