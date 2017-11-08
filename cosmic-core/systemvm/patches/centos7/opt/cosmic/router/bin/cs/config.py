from CsDatabag import CsDatabag


class Config(object):
    def __init__(self):
        self.fw = []
        self.ingress_rules = {}

        self.dbag_cmdline = CsDatabag('cmdline').dbag
        self.dbag_dhcpentry = CsDatabag('dhcpentry').dbag
        self.dbag_firewallrules = CsDatabag('firewallrules').dbag
        self.dbag_forwardingrules = CsDatabag('forwardingrules').dbag
        self.dbag_loadbalancer = CsDatabag('loadbalancer').dbag
        self.dbag_monitorservice = CsDatabag('monitorservice').dbag
        self.dbag_network_acl = CsDatabag('networkacl').dbag
        self.dbag_network_overview = CsDatabag('network_overview').dbag
        self.dbag_network_virtualrouter = CsDatabag('virtualrouter').dbag
        self.dbag_publicip_acl = CsDatabag('publicipacl').dbag
        self.dbag_remoteaccessvpn = CsDatabag('remoteaccessvpn').dbag
        self.dbag_site2sitevpn = CsDatabag('site2sitevpn').dbag
        self.dbag_vmdata = CsDatabag('vmdata').dbag
        self.dbag_vpnuserlist = CsDatabag('vpnuserlist').dbag

    def cmdline(self):
        return self.dbag_cmdline

    def get_fw(self):
        return self.fw

    def get_ingress_rules(self, key):
        if key in self.ingress_rules:
            return self.ingress_rules[key]
        return None

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
