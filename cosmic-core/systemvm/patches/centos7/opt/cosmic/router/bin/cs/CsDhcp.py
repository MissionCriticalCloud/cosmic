import logging

from netaddr import *

import CsHelper
from cs.CsFile import CsFile

LEASES = "/var/lib/misc/dnsmasq.leases"
DHCP_HOSTS = "/etc/dhcphosts.txt"
DHCP_OPTS = "/etc/dhcpopts.txt"


class CsDhcp(object):
    def __init__(self, config):
        self.config = config
        self.dbag = self.config.dbag_dhcpentry
        self.interfaces = self.config.dbag_network_overview['interfaces']

    """ Manage dhcp entries """

    def process(self):
        self.hosts = { }
        self.changed = []
        self.preseed()
        self.cloud = CsFile(DHCP_HOSTS)
        self.dhcp_opts = CsFile(DHCP_OPTS)

        self.cloud.repopulate()
        self.dhcp_opts.repopulate()

        for item in self.dbag:
            if item == "id":
                continue
            self.add(self.dbag[item])
            if 'default_gateway' not in self.dbag[item]:
                self.add_dhcp_opts(self.dbag[item])

        self.write_hosts()

        if self.cloud.is_changed():
            self.delete_leases()

        self.cloud.commit()
        self.dhcp_opts.commit()

    def delete_leases(self):
        try:
            open(LEASES, 'w').close()
        except IOError:
            return

    def preseed(self):
        self.add_host("127.0.0.1", "localhost %s" % CsHelper.get_hostname())
        self.add_host("::1", "localhost ip6-localhost ip6-loopback")
        self.add_host("ff02::1", "ip6-allnodes")
        self.add_host("ff02::2", "ip6-allrouters")

    def write_hosts(self):
        file = CsFile("/etc/hosts")
        file.repopulate()
        for ip in self.hosts:
            file.add("%s\t%s" % (ip, self.hosts[ip]))
        if file.is_changed():
            file.commit()
            logging.info("Updated hosts file")
        else:
            logging.debug("Hosts file unchanged")

    def add(self, entry):
        self.add_host(entry['ipv4_adress'], entry['host_name'])
        tag = "set:" + str(entry['ipv4_adress']).replace(".","_")
        self.cloud.add("%s,%s,%s,%s,infinite" % (entry['mac_address'],
                                              tag,
                                              entry['ipv4_adress'],
                                              entry['host_name']))

    def add_dhcp_opts(self, entry):
        # This means we won't serve these DHCP options for hosts with this tag
        tag = str(entry['ipv4_adress']).replace(".","_")
        self.dhcp_opts.add("%s,%s" % (tag, 3))
        self.dhcp_opts.add("%s,%s" % (tag, 6))
        self.dhcp_opts.add("%s,%s" % (tag, 15))

    def add_host(self, ip, hosts):
        self.hosts[ip] = hosts
