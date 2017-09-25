class CsApp:
    def __init__(self, ip):
        self.dev = ip.getDevice()
        self.ip = ip.get_ip_address()
        self.type = ip.get_type()
        self.fw = ip.fw
        self.config = ip.config


class CsDnsmasq(CsApp):
    """ Set up dnsmasq """

    def add_firewall_rules(self):
        """ Add the necessary firewall rules
        """
        self.fw.append(["", "front",
                        "-A INPUT -i %s -p udp -m udp --dport 67 -j ACCEPT" % self.dev
                        ])

        self.fw.append(["", "front",
                        "-A INPUT -i %s -d %s/32 -p udp -m udp --dport 53 -j ACCEPT" % (self.dev, self.ip)
                        ])

        self.fw.append(["", "front",
                        "-A INPUT -i %s -d %s/32 -p tcp -m tcp --dport 53 -j ACCEPT" % (self.dev, self.ip)
                        ])
