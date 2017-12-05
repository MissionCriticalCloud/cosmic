import logging

from dhcp_vm import DhcpVm


class VirtualMachine(object):
    def __init__(self, config):
        self.config = config

        self.dhcp_vm = DhcpVm(self.config)


    def sync(self):
        logging.debug("Starting sync of virtual machine!")
        logging.debug(self.config.dbag_vm_overview)

        self.dhcp_vm.sync()
