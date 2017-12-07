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

    # return the VR guest interface ip
    def getGuestIp(self):
        interfaces = []
        ipAddr = None
        for interface in self.config.address().get_interfaces():
            if interface.is_guest():
                interfaces.append(interface)
            if len(interfaces) > 0:
                ipAddr = sorted(interfaces)[-1]
            if ipAddr:
                return ipAddr.get_ip()

        return None

    def getDeviceByIp(self, ipa):
        for interface in self.config.address().get_interfaces():
            if interface.ip_in_subnet(ipa):
                return interface.get_device()
        return None

    def getNetworkByIp(self, ipa):
        for interface in self.config.address().get_interfaces():
            if interface.ip_in_subnet(ipa):
                return interface.get_network()
        return None

    def getGatewayByIp(self, ipa):
        for interface in self.config.address().get_interfaces():
            if interface.ip_in_subnet(ipa):
                return interface.get_gateway()
        return None

    def portsToString(self, ports, delimiter):
        ports_parts = ports.split(":", 2)
        if ports_parts[0] == ports_parts[1]:
            return str(ports_parts[0])
        else:
            return "%s%s%s" % (ports_parts[0], delimiter, ports_parts[1])

    def processForwardRule(self, rule):
        if self.config.is_vpc():
            self.forward_vpc(rule)
        else:
            self.forward_vr(rule)

    def forward_vr(self, rule):
        # prefetch iptables variables
        public_fwinterface = self.getDeviceByIp(rule['public_ip'])
        internal_fwinterface = self.getDeviceByIp(rule['internal_ip'])
        public_fwports = self.portsToString(rule['public_ports'], ':')
        internal_fwports = self.portsToString(rule['internal_ports'], '-')
        fw1 = "-A PREROUTING -d %s/32 -i %s -p %s -m %s --dport %s -j DNAT --to-destination %s:%s" % \
              (
                  rule['public_ip'],
                  public_fwinterface,
                  rule['protocol'],
                  rule['protocol'],
                  public_fwports,
                  rule['internal_ip'],
                  internal_fwports
              )
        fw2 = "-A PREROUTING -d %s/32 -i %s -p %s -m %s --dport %s -j DNAT --to-destination %s:%s" % \
              (
                  rule['public_ip'],
                  internal_fwinterface,
                  rule['protocol'],
                  rule['protocol'],
                  public_fwports,
                  rule['internal_ip'],
                  internal_fwports
              )
        fw3 = "-A OUTPUT -d %s/32 -p %s -m %s --dport %s -j DNAT --to-destination %s:%s" % \
              (
                  rule['public_ip'],
                  rule['protocol'],
                  rule['protocol'],
                  public_fwports,
                  rule['internal_ip'],
                  internal_fwports
              )
        fw4 = "-A POSTROUTING -j SNAT --to-source %s -s %s -d %s/32 -o %s -p %s -m %s --dport %s" % \
              (
                  self.getGuestIp(),
                  self.getNetworkByIp(rule['internal_ip']),
                  rule['internal_ip'],
                  internal_fwinterface,
                  rule['protocol'],
                  rule['protocol'],
                  self.portsToString(rule['internal_ports'], ':')
              )
        fw5 = "-A PREROUTING -d %s/32 -i %s -p %s -m %s --dport %s -j MARK --set-xmark %s/0xffffffff" % \
              (
                  rule['public_ip'],
                  public_fwinterface,
                  rule['protocol'],
                  rule['protocol'],
                  public_fwports,
                  hex(int(public_fwinterface[3:]))
              )
        fw6 = "-A PREROUTING -d %s/32 -i %s -p %s -m %s --dport %s -m state --state NEW -j CONNMARK --save-mark --nfmask 0xffffffff --ctmask 0xffffffff" % \
              (
                  rule['public_ip'],
                  public_fwinterface,
                  rule['protocol'],
                  rule['protocol'],
                  public_fwports,
              )
        fw7 = "-A FORWARD -i %s -o %s -p %s -m %s --dport %s -m state --state NEW,ESTABLISHED -j ACCEPT" % \
              (
                  public_fwinterface,
                  internal_fwinterface,
                  rule['protocol'],
                  rule['protocol'],
                  self.portsToString(rule['internal_ports'], ':')
              )
        self.config.fw.append(["nat", "", fw1])
        self.config.fw.append(["nat", "", fw2])
        self.config.fw.append(["nat", "", fw3])
        self.config.fw.append(["nat", "", fw4])
        self.config.fw.append(["nat", "", fw5])
        self.config.fw.append(["nat", "", fw6])
        self.config.fw.append(["filter", "", fw7])

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
        # FIXME this needs ordering with the VPN no nat rule
        device = self.getDeviceByIp(rule["public_ip"])
        if device is None:
            raise Exception("Ip address %s has no device in the ips databag" % rule["public_ip"])
        self.config.fw.append(["nat", "front",
                        "-A PREROUTING -d %s/32 -j DNAT --to-destination %s" % (
                        rule["public_ip"], rule["internal_ip"])])
        self.config.fw.append(["nat", "front",
                        "-A POSTROUTING -o %s -s %s/32 -j SNAT --to-source %s" % (
                        device, rule["internal_ip"], rule["public_ip"])])
        self.config.fw.append(["nat", "front",
                        "-A OUTPUT -d %s/32 -j DNAT --to-destination %s" % (rule["public_ip"], rule["internal_ip"])])
        self.config.fw.append(["filter", "",
                        "-A FORWARD -i %s -o eth0  -d %s  -m state  --state NEW -j ACCEPT " % (
                        device, rule["internal_ip"])])

        # configure the hairpin nat
        self.config.fw.append(["nat", "front",
                        "-A PREROUTING -d %s -i eth0 -j DNAT --to-destination %s" % (
                        rule["public_ip"], rule["internal_ip"])])

        self.config.fw.append(
            ["nat", "front", "-A POSTROUTING -s %s -d %s -j SNAT -o eth0 --to-source %s" % (
            self.getNetworkByIp(rule['internal_ip']), rule["internal_ip"], self.getGuestIp())])
