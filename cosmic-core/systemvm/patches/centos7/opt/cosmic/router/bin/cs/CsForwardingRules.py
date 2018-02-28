class CsForwardingRules(object):
    def __init__(self, config):
        self.config = config
        self.dbag = config.dbag_forwardingrules

    def process(self):
        for public_ip in self.dbag:
            if public_ip == "id":
                continue
            for rule in self.dbag[public_ip]:
                if rule["type"] == "forward":
                    self.processForwardRule(rule)
                elif rule["type"] == "staticnat":
                    self.processStaticNatRule(rule)

    def portsToString(self, ports, delimiter):
        ports_parts = ports.split(":", 2)
        if ports_parts[0] == ports_parts[1]:
            return str(ports_parts[0])
        else:
            return "%s%s%s" % (ports_parts[0], delimiter, ports_parts[1])

    def processForwardRule(self, rule):
        if self.config.is_vpc():
            self.forward_vpc(rule)

    def forward_vpc(self, rule):
        fw_prerout_rule = "-A PREROUTING -d %s/32" % rule["public_ip"]
        if not rule["protocol"] == "any":
            fw_prerout_rule += " -m %s -p %s" % (rule["protocol"], rule["protocol"])
        if not rule["public_ports"] == "any":
            fw_prerout_rule += " --dport %s" % self.portsToString(rule["public_ports"], ":")
        fw_prerout_rule += " -j DNAT --to-destination %s" % rule["internal_ip"]
        if not rule["internal_ports"] == "any":
            fw_prerout_rule += ":" + self.portsToString(rule["internal_ports"], "-")

        fw_postrout_rule = "-A POSTROUTING -d %s/32 " % rule["public_ip"]
        if not rule["protocol"] == "any":
            fw_postrout_rule += " -m %s -p %s" % (rule["protocol"], rule["protocol"])
        if not rule["public_ports"] == "any":
            fw_postrout_rule += " --dport %s" % self.portsToString(rule["public_ports"], ":")
        fw_postrout_rule += " -j SNAT --to-source %s" % rule["internal_ip"]
        if not rule["internal_ports"] == "any":
            fw_postrout_rule += ":" + self.portsToString(rule["internal_ports"], "-")

        fw_output_rule = "-A OUTPUT -d %s/32" % rule["public_ip"]
        if not rule["protocol"] == "any":
            fw_output_rule += " -m %s -p %s" % (rule["protocol"], rule["protocol"])
        if not rule["public_ports"] == "any":
            fw_output_rule += " --dport %s" % self.portsToString(rule["public_ports"], ":")
        fw_output_rule += " -j DNAT --to-destination %s" % rule["internal_ip"]
        if not rule["internal_ports"] == "any":
            fw_output_rule += ":" + self.portsToString(rule["internal_ports"], "-")

        self.config.fw.append(["nat", "", fw_prerout_rule])
        self.config.fw.append(["nat", "", fw_postrout_rule])
        self.config.fw.append(["nat", "", fw_output_rule])

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
