import logging
import time

import CsHelper


class CsDevice:
    """ Configure Network Devices """

    def __init__(self, dev, macaddress, config):
        self.devlist = []
        self.dev = dev
        self.macaddress = macaddress
        self.buildlist()
        self.table = ''
        self.tableNo = ''
        if dev != '':
            self.tableNo = dev[3:]
            self.table = "Table_%s" % dev
        self.fw = config.get_fw()
        self.cl = config.cmdline()

    def configure_rp(self):
        """
        Configure Reverse Path Filtering
        """
        filename = "/proc/sys/net/ipv4/conf/%s/rp_filter" % self.dev
        CsHelper.updatefile(filename, "1\n", "w")

    def buildlist(self):
        """
        List all available network devices on the system
        """
        self.devlist = []
        for line in open('/proc/net/dev'):
            vals = line.lstrip().split(':')
            if (not vals[0].startswith("eth")):
                continue
            self.devlist.append(vals[0])

    def waitfordevice(self, timeout=2):
        count = 0
        while count < timeout:
            if self.dev in self.devlist:
                return True
            time.sleep(1)
            count += 1
            self.buildlist()
        logging.error(
            "Device %s cannot be configured - device was not found", self.dev)
        return False

    def list(self):
        return self.devlist
