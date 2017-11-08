import logging
import subprocess

import CsHelper
from CsDnsmasq import CsDnsmasq
from CsInterface import CsInterface
from CsMetadataService import CsMetadataService
from CsPasswordService import CsPasswordService

VRRP_TYPES = ['guest']


class CsIP(object):
    def __init__(self, dev, macaddress, config):
        self.dev = dev
        self.macaddress = macaddress
        self.dnum = hex(int(dev[3:]))
        self.iplist = {}
        self.address = {}
        self.address = '''
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
        }'''
        self.list()
        self.fw = config.get_fw()
        self.cl = config.cmdline()
        self.config = config

    def setAddress(self, address):
        self.address = address

    def getAddress(self):
        return self.address

    # TODO Read this part!!
    def post_configure(self):
        """ The steps that must be done after a device is configured """
        if not self.get_type() in ["control"]:
            self.post_config_change()

        '''For isolated/redundant and dhcpsrvr routers, call this method after the post_config is complete '''
        if not self.config.is_vpc():
            self.setup_router_control()

    def post_config_change(self):
        self.fw_router()


        #self.fw_vpcrouter()
        # DONE ^^^

        if self.get_type() in 'guest':
            if self.config.has_dns() or self.config.is_dhcp():
                dns = CsDnsmasq(self)
                dns.add_firewall_rules()

            if self.config.has_metadata():
                app = CsMetadataService(self)
                app.setup()

        # Start passwd server on non-redundant routers and on the master router of redundant pairs
        if self.get_type() in ["guest"] and (not self.cl.is_redundant() or self.cl.is_master()):
            CsPasswordService(self.address['public_ip']).start()
        elif self.get_type() in ["guest"]:
            # Or else make sure it's stopped
            CsPasswordService(self.address['public_ip']).stop()

        if self.get_type() == "public" and self.config.is_vpc():
            if self.address["source_nat"]:
                logging.info("Adding SourceNAT for interface %s to %s" % (self.dev, self.address['public_ip']))
                self.fw.append(["nat", "", "-A POSTROUTING -o %s -d 10.0.0.0/8 -j RETURN" % self.dev])
                self.fw.append(["nat", "", "-A POSTROUTING -o %s -d 172.16.0.0/12 -j RETURN" % self.dev])
                self.fw.append(["nat", "", "-A POSTROUTING -o %s -d 192.168.0.0/16 -j RETURN" % self.dev])
                self.fw.append(
                    ["nat", "", "-A POSTROUTING -j SNAT -o %s --to-source %s" % (self.dev, self.address['public_ip'])])
            else:
                logging.info("Not adding SourceNAT for interface %s to %s, because source_nat=False" % (
                self.dev, self.address['public_ip']))

    def get_type(self):
        """ Return the type of the IP
        guest
        control
        public
        """
        if "nw_type" in self.address:
            return self.address['nw_type']
        return "unknown"

    def get_ip_address(self):
        """
        Return ip address if known
        """
        if "public_ip" in self.address:
            return self.address['public_ip']
        return "unknown"

    def setup_router_control(self):
        if self.config.is_vpc():
            return

        self.fw.append(["filter", "",
                        "-A INPUT -i eth1 -p tcp -s 169.254.0.1/32 -m tcp --dport 3922 -m state --state NEW,ESTABLISHED -j ACCEPT"])

        self.fw.append(["filter", "", "-P INPUT DROP"])
        self.fw.append(["filter", "", "-P FORWARD DROP"])

    def fw_router(self):
        if self.config.is_vpc():
            return
        self.fw.append(["mangle", "front",
                        "-A PREROUTING -m state --state RELATED,ESTABLISHED -j CONNMARK --restore-mark --nfmask 0xffffffff --ctmask 0xffffffff"])
        self.fw.append(["mangle", "front", "-A POSTROUTING -p udp -m udp --dport 68 -j CHECKSUM --checksum-fill"])

        if self.get_type() in ["public"]:
            self.fw.append(["mangle", "front", "-A PREROUTING -d %s/32 -j VPN_%s" % (
            self.address['public_ip'], self.address['public_ip'])])
            self.fw.append(["mangle", "front", "-A PREROUTING -d %s/32 -j FIREWALL_%s" % (
            self.address['public_ip'], self.address['public_ip'])])
            self.fw.append(["mangle", "front",
                            "-A FIREWALL_%s -m state --state RELATED,ESTABLISHED -j ACCEPT" % self.address[
                                'public_ip']])
            self.fw.append(
                ["mangle", "", "-A VPN_%s -m state --state RELATED,ESTABLISHED -j ACCEPT" % self.address['public_ip']])
            self.fw.append(["mangle", "", "-A VPN_%s -j RETURN" % self.address['public_ip']])

            self.fw.append(["nat", "", "-A POSTROUTING -o eth2 -d 10.0.0.0/8 -j RETURN"])
            self.fw.append(["nat", "", "-A POSTROUTING -o eth2 -d 172.16.0.0/12 -j RETURN"])
            self.fw.append(["nat", "", "-A POSTROUTING -o eth2 -d 192.168.0.0/16 -j RETURN"])
            self.fw.append(["nat", "", "-A POSTROUTING -o eth2 -j SNAT --to-source %s" % self.address['public_ip']])

            self.fw.append(["mangle", "",
                            "-A PREROUTING -i %s -m state --state NEW -j CONNMARK --set-xmark %s/0xffffffff" % (
                            self.dev, self.dnum)])
            self.fw.append(["mangle", "", "-A FIREWALL_%s -j DROP" % self.address['public_ip']])

        self.fw.append(["filter", "", "-A INPUT -d 224.0.0.18/32 -j ACCEPT"])
        self.fw.append(["filter", "", "-A INPUT -d 224.0.0.22/32 -j ACCEPT"])
        self.fw.append(["filter", "", "-A INPUT -d 224.0.0.252/32 -j ACCEPT"])
        self.fw.append(["filter", "", "-A INPUT -d 225.0.0.50/32 -j ACCEPT"])
        self.fw.append(["filter", "", "-A INPUT -i %s -m state --state RELATED,ESTABLISHED -j ACCEPT" % self.dev])
        self.fw.append(["filter", "", "-A INPUT -p icmp -j ACCEPT"])
        self.fw.append(["filter", "", "-A INPUT -i lo -j ACCEPT"])

        if self.get_type() in ["guest"]:
            guestNetworkCidr = self.address['network']
            self.fw.append(["filter", "", "-A INPUT -i %s -p udp -m udp --dport 67 -j ACCEPT" % self.dev])
            self.fw.append(["filter", "",
                            "-A INPUT -i %s -p udp -m udp --dport 53 -s %s -j ACCEPT" % (self.dev, guestNetworkCidr)])
            self.fw.append(["filter", "",
                            "-A INPUT -i %s -p tcp -m tcp --dport 53 -s %s -j ACCEPT" % (self.dev, guestNetworkCidr)])
            self.fw.append(
                ["filter", "", "-A INPUT -i %s -p tcp -m tcp --dport 80 -m state --state NEW -j ACCEPT" % self.dev])
            self.fw.append(
                ["filter", "", "-A INPUT -i %s -p tcp -m tcp --dport 8080 -m state --state NEW -j ACCEPT" % self.dev])

            self.fw.append(
                ["filter", "", "-A FORWARD -i %s -o eth1 -m state --state RELATED,ESTABLISHED -j ACCEPT" % self.dev])
            self.fw.append(
                ["filter", "", "-A FORWARD -i %s -o %s -m state --state NEW -j ACCEPT" % (self.dev, self.dev)])
            self.fw.append(["filter", "", "-A FORWARD -i eth2 -o eth0 -m state --state RELATED,ESTABLISHED -j ACCEPT"])
            self.fw.append(["filter", "", "-A FORWARD -i eth0 -o eth0 -m state --state RELATED,ESTABLISHED -j ACCEPT"])

            self.fw.append(["filter", "", "-A FORWARD -i eth0 -o eth2 -m state --state RELATED,ESTABLISHED -j ACCEPT"])
            self.fw.append(["filter", "", "-A FORWARD -i eth0 -o eth2 -j FW_OUTBOUND"])
            self.fw.append(["mangle", "",
                            "-A PREROUTING -i %s -m state --state NEW -j CONNMARK --set-xmark %s/0xffffffff" % (
                            self.dev, self.dnum)])

        self.fw.append(['', 'front', '-A FORWARD -j NETWORK_STATS'])
        self.fw.append(['', 'front', '-A INPUT -j NETWORK_STATS'])
        self.fw.append(['', 'front', '-A OUTPUT -j NETWORK_STATS'])

        self.fw.append(['', '', '-A NETWORK_STATS -i eth0 -o eth2'])
        self.fw.append(['', '', '-A NETWORK_STATS -i eth2 -o eth0'])
        self.fw.append(['', '', '-A NETWORK_STATS -o eth2 ! -i eth0 -p tcp'])
        self.fw.append(['', '', '-A NETWORK_STATS -i eth2 ! -o eth0 -p tcp'])

    def list(self):
        self.iplist = {}
        cmd = ("ip addr show dev " + self.dev)
        for i in CsHelper.execute(cmd):
            vals = i.lstrip().split()
            if (vals[0] == 'inet'):
                cidr = vals[1]
                self.iplist[cidr] = self.dev

    def configured(self):
        if self.address['cidr'] in self.iplist.keys():
            return True
        return False

    def needs_vrrp(self):
        """
        Returns if the ip needs to be managed by keepalived or not
        """
        if "nw_type" in self.address and self.address['nw_type'] in VRRP_TYPES:
            return True
        return False

    def is_public(self):
        if "nw_type" in self.address and self.address['nw_type'] in ['public']:
            return True
        return False

    def ip(self):
        return str(self.address['cidr'])

    def getDevice(self):
        return self.dev

    def hasIP(self, ip):
        return ip in self.address.values()

    # Delete any ips that are configured but not in the bag
    def compare(self, bag):
        if len(self.iplist) > 0 and (self.macaddress not in bag.keys() or len(bag[self.macaddress]) == 0):
            # Handle all except control nics
            if self.get_type() not in ["control"]:
                # Remove all IPs on this device
                logging.info("Device is of type %s" % self.get_type())
                logging.info("Will remove all configured addresses on device %s", self.dev)
                self.delete("all")
                app = CsMetadataService(self)
                app.remove()
            else:
                logging.info("Not removing interfaces of device %s, as it is control traffic", self.dev)

        for ip in self.iplist:
            found = False
            if self.macaddress in bag.keys():
                for address in bag[self.macaddress]:
                    self.setAddress(address)
                    if (self.hasIP(ip) or self.is_guest_gateway(address, ip)) and address["add"]:
                        logging.debug("The IP address in '%s' will be configured" % address)
                        found = True
            if not found:
                self.delete(ip)

    def is_guest_gateway(self, bag, ip):
        """ Exclude the vrrp maintained addresses on a redundant router """
        interface = CsInterface(bag, self.config)
        if not self.config.cl.is_redundant():
            return False

        rip = ip.split('/')[0]
        logging.info("Checking if cidr is a gateway for rVPC. IP ==> %s / device ==> %s", ip, self.dev)

        gw = interface.get_gateway()
        logging.info("Interface has the following gateway ==> %s", gw)

        guest_gw = self.config.cmdline().get_guest_gw()
        logging.info("Interface has the following gateway ==> %s", guest_gw)

        if bag['nw_type'] == "guest" and (rip == gw or rip == guest_gw):
            return True
        return False

    def delete(self, ip):
        remove = []
        if ip == "all":
            logging.info("Removing addresses from device %s", self.dev)
            remove = self.iplist.keys()
        else:
            remove.append(ip)
        for ip in remove:
            cmd = "ip addr del dev %s %s" % (self.dev, ip)
            subprocess.call(cmd, shell=True)
            logging.info("Removed address %s from device %s", ip, self.dev)
            self.post_config_change()
