from netaddr import IPAddress, IPNetwork

VRRP_TYPES = ['guest']


class CsInterface:
    """ Hold one single ip """

    def __init__(self, o, config):
        self.address = o
        self.config = config

    def get_ip(self):
        return self.get_attr("public_ip")

    def get_network(self):
        return self.get_attr("network")

    def get_netmask(self):
        return self.get_attr("netmask")

    def get_gateway(self):
        if self.config.is_vpc() or not self.is_guest():
            return self.get_attr("gateway")
        else:
            return self.config.cmdline().get_guest_gw()

    def ip_in_subnet(self, ip):
        ipo = IPAddress(ip)
        net = IPNetwork("%s/%s" % (self.get_ip(), self.get_size()))
        return ipo in net

    def get_gateway_cidr(self):
        return "%s/%s" % (self.get_gateway(), self.get_size())

    def get_size(self):
        """ Return the network size in bits (24, 16, 8 etc) """
        return self.get_attr("size")

    def get_device(self):
        return self.get_attr("device")

    def get_cidr(self):
        return self.get_attr("cidr")

    def get_broadcast(self):
        return self.get_attr("broadcast")

    def get_attr(self, attr):
        if attr in self.address:
            return self.address[attr]
        else:
            return "ERROR"

    def needs_vrrp(self):
        """
        Returns if the ip needs to be managed by keepalived or not
        """
        if "nw_type" in self.address and self.address['nw_type'] in VRRP_TYPES:
            return True
        return False

    def is_control(self):
        if "nw_type" in self.address and self.address['nw_type'] in ['control']:
            return True
        return False

    def is_guest(self):
        if "nw_type" in self.address and self.address['nw_type'] in ['guest']:
            return True
        return False

    def is_public(self):
        if "nw_type" in self.address and self.address['nw_type'] in ['public']:
            return True
        return False

    def is_added(self):
        return self.get_attr("add")

    def to_str(self):
        return self.address
