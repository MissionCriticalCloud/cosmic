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

    def get_fw(self):
        return self.fw

    def get_logger(self):
        return self.__LOG_FILE

    def get_level(self):
        return self.__LOG_LEVEL

    def is_vpc(self):
        return self.cl.get_type() == "vpcrouter"

    def is_router(self):
        return self.cl.get_type() == "router"

    def get_domain(self):
        return self.cl.get_domain()

    def get_dns(self):
        dns = []
        # Check what happens with use_ext_dns
        dns.append(self.address().get_guest_ip())
        names = ["dns1", "dns2"]
        for name in names:
            if name in self.cmdline().idata():
                dns.append(self.cmdline().idata()[name])
        return dns

    def get_format(self):
        return self.__LOG_FORMAT

    def get_ingress_chain(self, device, ip):
        if self.is_vpc():
            return "ACL_INBOUND_%s" % device
        else:
            return "FIREWALL_%s" % ip

    def get_egress_chain(self, device, ip):
        if self.is_vpc():
            return "ACL_OUTBOUND_%s" % device
        else:
            return "FW_EGRESS_RULES"

    def get_egress_table(self):
        if self.is_vpc():
            return 'mangle'
        else:
            return ""
