import logging

from netaddr import *

import CsHelper
from CsDatabag import CsDataBag
from CsGuestNetwork import CsGuestNetwork
from cs.CsFile import CsFile

LEASES = "/var/lib/misc/dnsmasq.leases"
DHCP_HOSTS = "/etc/dhcphosts.txt"
DHCP_OPTS = "/etc/dhcpopts.txt"
CLOUD_CONF = "/etc/dnsmasq.d/cloud.conf"


class CsDhcp(CsDataBag):
    """ Manage dhcp entries """

    def process(self):
        self.hosts = { }
        self.changed = []
        self.devinfo = CsHelper.get_device_info()
        self.preseed()
        self.cloud = CsFile(DHCP_HOSTS)
        self.dhcp_opts = CsFile(DHCP_OPTS)
        self.conf = CsFile(CLOUD_CONF)

        self.cloud.repopulate()
        self.dhcp_opts.repopulate()
        self.conf.repopulate()

        for item in self.dbag:
            if item == "id":
                continue
            self.add(self.dbag[item])
            if self.dbag[item]['default_gateway'] == "0.0.0.0":
                self.add_dhcp_opts(self.dbag[item])

        self.write_hosts()

        if self.cloud.is_changed():
            self.delete_leases()

        self.configure_server()

        self.conf.commit()
        self.cloud.commit()
        self.dhcp_opts.commit()

        # We restart DNSMASQ every time the configure.py is called in order to avoid lease problems.
        # But only do that on the master or else VMs will get leases from the backup resulting in
        # Cloud-init to get the passwd and other meta-data from the backup as well.
        if not self.cl.is_redundant() or self.cl.is_master():
            CsHelper.execute2("systemctl restart dnsmasq")

    def configure_server(self):
        # self.conf.addeq("dhcp-hostsfile=%s" % DHCP_HOSTS)
        idx = 0
        for i in self.devinfo:
            if not i['dnsmasq']:
                continue
            device = i['dev']
            # Listen only on the interfaces we configure VMs on
            sline = "interface=%s" % (device)
            line = "interface=%s" % (device)
            self.conf.search(sline, line)
            # Ip address
            ip = i['ip'].split('/')[0]
            self.conf.add("dhcp-range=tag:%s,set:interface-%s-%s,%s,static" % (device, device, idx, ip))
            gn = CsGuestNetwork(device, self.config)
            self.conf.add("dhcp-option=tag:interface-%s-%s,15,%s" % (device, idx, gn.get_domain()))
            # DNS search order
            if gn.get_dns() and device:
                dns_list = [x for x in gn.get_dns() if x is not None]
                self.conf.add("dhcp-option=tag:interface-%s-%s,6,%s" % (device, idx, ','.join(dns_list)))
            # Gateway
            gateway = ''
            if self.config.is_vpc():
                gateway = gn.get_gateway()
            else:
                gateway = i['gateway']
            if gateway != '0.0.0.0':
                self.conf.add("dhcp-option=tag:interface-%s-%s,3,%s" % (device, idx, gateway))
            # Netmask
            netmask = ''
            if self.config.is_vpc():
                netmask = gn.get_netmask()
            else:
                netmask = self.config.address().get_guest_netmask()
            self.conf.add("dhcp-option=tag:interface-%s-%s,1,%s" % (device, idx, netmask))
            idx += 1

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
        if self.config.is_router():
            self.add_host(self.config.address().get_guest_ip(), "%s data-server" % CsHelper.get_hostname())

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
        i = IPAddress(entry['ipv4_adress'])
        # Calculate the device
        for v in self.devinfo:
            if i > v['network'].network and i < v['network'].broadcast:
                v['dnsmasq'] = True
                # Virtual Router
                v['gateway'] = entry['default_gateway']

    def add_dhcp_opts(self, entry):
        # This means we won't serve these DHCP options for hosts with this tag
        tag = str(entry['ipv4_adress']).replace(".","_")
        self.dhcp_opts.add("%s,%s" % (tag, 3))
        self.dhcp_opts.add("%s,%s" % (tag, 6))
        self.dhcp_opts.add("%s,%s" % (tag, 15))

    def add_host(self, ip, hosts):
        self.hosts[ip] = hosts
