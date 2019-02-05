import logging

from .dhcp_vm import DhcpVm
from .metadata_vm import MetadataVm


class VirtualMachine:

    def __init__(self, config):
        self.config = config

        self.dhcp_vm = DhcpVm(self.config)
        self.metadata_vm = MetadataVm(self.config)

    def sync(self):
        logging.debug("Starting sync of virtual machine!")
        logging.debug(self.config.dbag_vm_overview)

        self.dhcp_vm.sync()
        self.metadata_vm.sync()
