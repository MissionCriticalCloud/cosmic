class CsForwardingRules(object):
    def __init__(self, config):
        self.config = config
        self.dbag = config.dbag_forwardingrules

    def process(self):
        for public_ip in self.dbag:
            if public_ip == "id":
                continue
            for rule in self.dbag[public_ip]:
                if rule["type"] == "staticnat":
                    self.processStaticNatRule(rule)

    def portsToString(self, ports, delimiter):
        ports_parts = ports.split(":", 2)
        if ports_parts[0] == ports_parts[1]:
            return str(ports_parts[0])
        else:
            return "%s%s%s" % (ports_parts[0], delimiter, ports_parts[1])

    def processStaticNatRule(self, rule):
        device = self.config.get_public_interface_name()
        if device is None:
            raise Exception("Ip address %s has no device in the ips databag" % rule["public_ip"])

        self.config.fw.append(["nat", "front",
                        "-A PREROUTING -d %s/32 -j DNAT --to-destination %s" % (
                        rule["public_ip"], rule["internal_ip"])])

        self.config.fw.append(["nat", "front",
                        "-A POSTROUTING -o %s -s %s/32 -j SNAT --to-source %s" % (
                        device, rule["internal_ip"], rule["public_ip"])])

        self.config.fw.append(["nat", "front",
                        "-A OUTPUT -d %s/32 -j DNAT --to-destination %s" % (
                       rule["public_ip"], rule["internal_ip"])])
