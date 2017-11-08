# -- coding: utf-8 --

import logging

import CsHelper
from CsDatabag import CsDatabag
from CsDevice import CsDevice
from CsInterface import CsInterface

VRRP_TYPES = ['guest']


class CsAddress(CsDatabag):
    def compare(self):
        for dbag_data in self.config.ips.dbag.values():
            if dbag_data == "ips":
                continue
            print dbag_data
            try:
                device_data = dbag_data[0]
                ip = CsIP(device_data['device'], device_data["mac_address"], self.config)
                # Process for all types, except the link local interface
                if device_data['device'] is not self.get_control_if():
                    ip.compare(self.dbag)
            except:
                pass

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

    def needs_vrrp(self, o):
        """
        Returns if the ip needs to be managed by keepalived or not
        """
        if "nw_type" in o and o['nw_type'] in VRRP_TYPES:
            return True
        return False

    def get_control_if(self):
        """
        Return the address object that has the control interface
        """
        for interface in self.get_interfaces():
            if interface.is_control():
                return interface
        return None

    def process(self):
        has_sourcenat = False

        for identifier in self.dbag:
            if identifier == "id":
                continue

            try:
                dev = self.dbag[identifier][0]['device']
                ip = CsIP(dev, identifier, self.config)
            except:
                continue

            for address in self.dbag[identifier]:
                ip.setAddress(address)
                logging.info("Address found in DataBag ==> %s" % address)

                if not address['add'] and not ip.configured():
                    logging.info("Skipping %s as the add flag is set to %s " % (address['public_ip'], address['add']))
                    continue

                if ip.configured():
                    logging.info("Address %s on device %s already configured", ip.ip(), dev)
                    ip.post_configure(address)
                else:
                    logging.info("Address %s on device %s not configured", ip.ip(), dev)
                    if CsDevice(dev, identifier, self.config).waitfordevice():
                        ip.configure(address)

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


