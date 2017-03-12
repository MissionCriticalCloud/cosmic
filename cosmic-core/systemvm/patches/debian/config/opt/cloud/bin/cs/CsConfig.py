# -- coding: utf-8 --


from CsAddress import CsAddress
from CsDatabag import CsCmdLine


class CsConfig(object):
    """
    A class to cache all the stuff that the other classes need
    """
    __LOG_FILE = "/var/log/cloud.log"
    __LOG_LEVEL = "DEBUG"
    __LOG_FORMAT = "%(asctime)s %(levelname)-8s %(message)s"
    cl = None

    def __init__(self):
        self.fw = []
        self.ingress_rules = {}
        self.ips = None

    def set_address(self):
        self.ips = CsAddress("ips", self)

    @classmethod
    def get_cmdline_instance(cls):
        if cls.cl is None:
            cls.cl = CsCmdLine("cmdline")
        return cls.cl

    def cmdline(self):
        return self.get_cmdline_instance()

    def address(self):
        return self.ips

    def get_device(self):
        return self.device

    def get_macaddress(self):
        return self.macaddress

    def get_fw(self):
        return self.fw

    def get_ingress_rules(self, key):
        if self.ingress_rules.has_key(key):
            return self.ingress_rules[key]
        return None

    def set_ingress_rules(self, key, ingress_rules):
        self.ingress_rules[key] = ingress_rules

    def get_logger(self):
        return self.__LOG_FILE

    def get_level(self):
        return self.__LOG_LEVEL

    def is_vpc(self):
        return self.cl.get_type() == 'vpcrouter'

    def is_router(self):
        return self.cl.get_type() == 'router'

    def is_dhcp(self):
        return self.cl.get_type() == 'dhcpsrvr'

    def has_dns(self):
        return not self.use_extdns()

    def has_metadata(self):
        return any((self.is_vpc(), self.is_router(), self.is_dhcp()))

    def use_extdns(self):
        return self.cmdline().idata().get('useextdns', 'false') == 'true'

    def get_domain(self):
        return self.cl.get_domain()

    def get_dns(self):
        conf = self.cmdline().idata()
        dns = []
        if not self.use_extdns():
            if not self.is_vpc() and self.cl.is_redundant() and self.cl.get_guest_gw():
                dns.append(self.cl.get_guest_gw())
            else:
                dns.append(self.address().get_guest_ip())
        for name in ["dns1", "dns2"]:
            if name in conf:
                dns.append(conf[name])
        return dns

    def get_format(self):
        return self.__LOG_FORMAT
