# -- coding: utf-8 --

import logging

from CsDatabag import CsDatabag
from CsIP import CsIP
from CsInterface import CsInterface

VRRP_TYPES = ['guest']


class CsAddress(CsDatabag):
    def get_interfaces(self):
        interfaces = []
        for dev in self.dbag:
            if dev == "id":
                continue
            for ip in self.dbag[dev]:
                interfaces.append(CsInterface(ip, self.config))
        return interfaces

    def get_guest_if(self):
        """
        Return CsInterface object for the lowest in use guest interface
        """
        guest_interface = None
        lowest_device = 1000
        for interface in self.get_interfaces():
            if interface.is_guest() and interface.is_added():
                device = interface.get_device()
                device_suffix = int(''.join([digit for digit in device if digit.isdigit()]))
                if device_suffix < lowest_device:
                    lowest_device = device_suffix
                    guest_interface = interface
                    logging.debug("Guest interface will be set on device '%s' and IP '%s'" % (
                    guest_interface.get_device(), guest_interface.get_ip()))
        return guest_interface

    def get_guest_ip(self):
        """
        Return the ip of the first guest interface
        For use with routers not vpcrouters
        """
        ip = self.get_guest_if()
        if ip:
            return ip.get_ip()
        return None

    def get_guest_netmask(self):
        """
        Return the netmask of the first guest interface
        For use with routers not vpcrouters
        """
        ip = self.get_guest_if()
        if ip:
            return ip.get_netmask()
        return "255.255.255.0"

    def process(self):
        has_sourcenat = False

        for identifier in self.dbag:
            if identifier == "id":
                continue

            identifier = 'eth1'
            self.dbag[identifier] = ''''
            [
        {
            "add": true,
            "broadcast": "85.222.237.255",
            "cidr": "85.222.237.205/25",
            "device": "eth1",
            "device_mac_address": "06:80:fc:00:00:10",
            "first_i_p": false,
            "gateway": "85.222.237.129",
            "netmask": "255.255.255.128",
            "network": "85.222.237.128/25",
            "new_nic": false,
            "nic_dev_id": "1",
            "nw_type": "public",
            "one_to_one_nat": false,
            "public_ip": "85.222.237.205",
            "size": "25",
            "source_nat": true,
            "vif_mac_address": "06:80:fc:00:00:10"
        },
        {
            "add": true,
            "broadcast": "195.43.158.255",
            "cidr": "195.43.158.123/24",
            "device": "eth1",
            "device_mac_address": "06:80:fc:00:00:10",
            "first_i_p": false,
            "gateway": "195.43.158.1",
            "netmask": "255.255.255.0",
            "network": "195.43.158.0/24",
            "new_nic": false,
            "nic_dev_id": "1",
            "nw_type": "public",
            "one_to_one_nat": false,
            "public_ip": "195.43.158.123",
            "size": "24",
            "source_nat": false,
            "vif_mac_address": "06:80:fc:00:00:10"
        }
    ],'''

            try:
                dev = self.dbag[identifier][0]['device']
                ip = CsIP(dev, identifier, self.config)
            except:
                continue

            for address in self.dbag[identifier]:
                ip.setAddress(address)
                logging.info("Address found in DataBag ==> %s" % address)

                ip.post_configure()

        cmdline = self.config.cmdline()
        if self.config.is_vpc():
            vpccidr = cmdline.get_vpccidr()
            self.fw.append(["filter", "", "-A FORWARD -s %s ! -d %s -j ACCEPT" % (vpccidr, vpccidr)])
            if has_sourcenat:
                # create source nat list chain
                self.fw.append(["filter", "", "-N SOURCE_NAT_LIST"])
                self.fw.append(["filter", "", "-A FORWARD -j SOURCE_NAT_LIST"])
            # adding logging here for all ingress traffic at once
            self.fw.append(["filter", "",
                            "-A FORWARD -m limit --limit 2/second -j LOG  --log-prefix \"iptables denied: [ingress]\" --log-level 4"])


