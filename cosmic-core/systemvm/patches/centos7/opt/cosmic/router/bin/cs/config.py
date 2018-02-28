import utils
from CsDatabag import CsDatabag


class Config:

    def __init__(self):
        self.fw = []
        self.ingress_rules = {}

        self.dbag_cmdline = CsDatabag('cmdline').dbag
        self.dbag_dhcpentry = CsDatabag('dhcpentry').dbag
        self.dbag_firewallrules = CsDatabag('firewallrules').dbag
        self.dbag_forwardingrules = CsDatabag('forwardingrules').dbag
        self.dbag_loadbalancer = CsDatabag('loadbalancer').dbag
        self.dbag_network_acl = CsDatabag('networkacl').dbag
        self.dbag_network_overview = CsDatabag('network_overview').dbag
        self.dbag_vm_overview = CsDatabag('vm_overview').dbag
        self.dbag_network_virtualrouter = CsDatabag('virtualrouter').dbag
        self.dbag_publicip_acl = CsDatabag('publicipacl').dbag

    def cmdline(self):
        return self.dbag_cmdline

    def get_fw(self):
        return self.fw

    def get_ingress_rules(self, key):
        if key in self.ingress_rules:
            return self.ingress_rules[key]
        return None

    def get_advert_int(self):
        if 'advert_int' in self.dbag_cmdline['config']:
            return self.dbag_cmdline['config']['advert_int']
        return 1

    def set_ingress_rules(self, key, ingress_rules):
        self.ingress_rules[key] = ingress_rules

    def is_vpc(self):
        return self.dbag_cmdline['config']['type'] == 'vpcrouter'

    def is_router(self):
        return self.dbag_cmdline['config']['type'] == 'router'

    def is_dhcp(self):
        return self.dbag_cmdline['config']['type'] == 'dhcpsrvr'

    def has_dns(self):
        return not self.use_extdns()

    def has_metadata(self):
        return any((self.is_vpc(), self.is_router(), self.is_dhcp()))

    def use_extdns(self):
        if 'useextdns' in self.dbag_cmdline['config']:
            return self.dbag_cmdline['config']['useextdns'] == 'true'
        return False

    def get_domain(self):
        return self.dbag_cmdline['config']['domain']

    def get_dns(self):
        conf = self.dbag_cmdline['config']
        dns = []
        if not self.use_extdns():
            pass
            if not self.is_vpc() and 'guestgw' in conf:
                dns.append(conf['guestgw'])
            # else:
            #     # FIXME look at the below line
            #     dns.append(self.address().get_guest_ip())

        for name in ["dns1", "dns2"]:
            if name in conf:
                dns.append(conf[name])
        return dns

    def get_sync_interface_name(self):
        for interface in self.dbag_network_overview['interfaces']:
            if interface['metadata']['type'] == 'sync':
                return utils.get_interface_name_from_mac_address(interface['mac_address'])

    def get_public_interface_name(self):
        for interface in self.dbag_network_overview['interfaces']:
            if interface['metadata']['type'] == 'public':
                return utils.get_interface_name_from_mac_address(interface['mac_address'])

    def get_all_ipv4_addresses_on_router(self):
        ipv4_addresses = []

        for interface in self.dbag_network_overview['interfaces']:
            for ip in interface['ipv4_addresses']:
                ipv4_addresses.append(ip['cidr'].split('/')[0])

        return ipv4_addresses
