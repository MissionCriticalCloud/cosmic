import logging


class CsAcl(object):
    def __init__(self, config):
        self.config = config

        self.dbag = config.dbag_network_acl
        self.process()

        self.dbag = config.dbag_publicip_acl
        self.process()

        self.dbag = config.dbag_firewallrules
        self.process()

    """
        Deal with Network acls
    """

    class AclIP():
        """ For type Virtual Router """

        def __init__(self, obj, config):
            self.fw = config.get_fw()
            self.direction = 'egress'
            if obj['traffic_type'] == 'Ingress':
                self.direction = 'ingress'
            self.device = ''
            self.ip = obj['src_ip']
            self.rule = obj
            self.rule['type'] = obj['protocol']
            # src_port_range
            if 'src_port_range' in obj:
                self.rule['first_port'] = obj['src_port_range'][0]
                self.rule['last_port'] = obj['src_port_range'][1]

            self.rule['allowed'] = True
            self.rule['action'] = "ACCEPT"

            if self.rule['type'] == 'all' and not obj['source_cidr_list']:
                self.rule['cidr'] = ['0.0.0.0/0']
            else:
                self.rule['cidr'] = obj['source_cidr_list']

            logging.debug("AclIP created for rule ==> %s", self.rule)

        def create(self):
            for cidr in self.rule['cidr']:
                self.add_rule(cidr)

        def add_rule(self, cidr):
            icmp_type = ''
            rule = self.rule
            icmp_type = "any"
            if "icmp_type" in list(self.rule.keys()) and self.rule['icmp_type'] != -1:
                icmp_type = self.rule['icmp_type']
            if "icmp_code" in list(self.rule.keys()) and rule['icmp_code'] != -1:
                icmp_type = "%s/%s" % (self.rule['icmp_type'], self.rule['icmp_code'])
            rnge = ''
            if "first_port" in list(self.rule.keys()) and \
                            self.rule['first_port'] == self.rule['last_port']:
                rnge = " --dport %s " % self.rule['first_port']
            if "first_port" in list(self.rule.keys()) and \
                            self.rule['first_port'] != self.rule['last_port']:
                rnge = " --dport %s:%s" % (rule['first_port'], rule['last_port'])
            if self.direction == 'ingress':
                if rule['protocol'] == "icmp":
                    self.fw.append(["mangle", "front",
                                    " -A FIREWALL_%s" % self.ip +
                                    " -s %s " % cidr +
                                    " -p %s " % rule['protocol'] +
                                    " -m %s " % rule['protocol'] +
                                    " --icmp-type %s -j %s" % (icmp_type, self.rule['action'])])
                else:
                    self.fw.append(["mangle", "front",
                                    " -A FIREWALL_%s" % self.ip +
                                    " -s %s " % cidr +
                                    " -p %s " % rule['protocol'] +
                                    " -m %s " % rule['protocol'] +
                                    " %s -j %s" % (rnge, self.rule['action'])])

            logging.debug("Current ACL IP direction is ==> %s", self.direction)
            if self.direction == 'egress':
                self.fw.append(["filter", "", " -A FW_OUTBOUND -j FW_EGRESS_RULES"])
                fwr = " -I FW_EGRESS_RULES"
                # In case we have a default rule (accept all or drop all), we have to evaluate the action again.
                if rule['type'] == 'all' and not rule['source_cidr_list']:
                    fwr = " -A FW_EGRESS_RULES"
                    # For default egress ALLOW or DENY, the logic is inverted.
                    # Having default_egress_policy == True, means that the default rule should have ACCEPT,
                    # otherwise DROP. The rule should be appended, not inserted.
                    if self.rule['default_egress_policy']:
                        self.rule['action'] = "ACCEPT"
                    else:
                        self.rule['action'] = "DROP"
                else:
                    # For other rules added, if default_egress_policy == True, following rules should be DROP,
                    # otherwise ACCEPT
                    if self.rule['default_egress_policy']:
                        self.rule['action'] = "DROP"
                    else:
                        self.rule['action'] = "ACCEPT"
                if rule['protocol'] == "icmp":
                    fwr += " -s %s " % cidr + \
                           " -p %s " % rule['protocol'] + \
                           " -m %s " % rule['protocol'] + \
                           " --icmp-type %s" % icmp_type
                elif rule['protocol'] != "all":
                    fwr += " -s %s " % cidr + \
                           " -p %s " % rule['protocol'] + \
                           " -m %s " % rule['protocol'] + \
                           " %s" % rnge
                elif rule['protocol'] == "all":
                    fwr += " -s %s " % cidr

                self.fw.append(["filter", "", "%s -j %s" % (fwr, rule['action'])])
                logging.debug("EGRESS rule configured for protocol ==> %s, action ==> %s", rule['protocol'], rule['action'])

    class AclDevice():
        """ A little class for each list of acls per device """

        FIXED_RULES_INGRESS = 3
        FIXED_RULES_EGRESS = 3

        def __init__(self, obj, config):
            self.ingress = []
            self.egress = []
            self.device = obj['device']
            self.ip = obj['nic_ip']
            self.netmask = obj['nic_netmask']
            self.config = config
            self.cidr = "%s/%s" % (self.ip, self.netmask)
            if 'public_ip' in list(obj.keys()):
                self.public_ip = obj['public_ip']
            if "ingress_rules" in list(obj.keys()):
                self.ingress = obj['ingress_rules']
                config.set_ingress_rules(self.ip, obj['ingress_rules'])
            if "egress_rules" in list(obj.keys()):
                self.egress = obj['egress_rules']
            self.fw = config.get_fw()

        def create(self):
            if hasattr(self, 'public_ip'):
                self.process("ingress", 'ACL_PUBLIC_IP', self.ingress, self.FIXED_RULES_INGRESS)
                self.process("egress", 'ACL_PUBLIC_IP', self.ingress, self.FIXED_RULES_INGRESS)
            else:
                self.process("ingress", 'ACL_INBOUND', self.ingress, self.FIXED_RULES_INGRESS)
                self.process("egress", 'ACL_OUTBOUND', self.egress, self.FIXED_RULES_EGRESS)

            # to drop traffic destined to public ips
            if hasattr(self, 'public_ip'):
                self.fw.append(["mangle", "", "-A ACL_PUBLIC_IP_%s -d %s -m limit --limit 2/second -j LOG  --log-prefix \"iptables denied: [public ip] \" --log-level 4" % (
                    self.device, self.public_ip)])
                self.fw.append(["mangle", "", "-A ACL_PUBLIC_IP_%s -d %s -j DROP" % (self.device, self.public_ip)])
            # rule below moved from CsAddress to replicate default behaviour
            # default behaviour is that only if one or more egress rules exist
            # we will drop everything else, otherwise we will allow all egress traffic
            # now also with logging
            elif len(self.egress) > 0:
                self.fw.append(["mangle", "", "-A ACL_OUTBOUND_%s -m limit --limit 2/second -j LOG  --log-prefix \"iptables denied: [egress] \" --log-level 4" % self.device])
                self.fw.append(["mangle", "", "-A ACL_OUTBOUND_%s -j DROP" % self.device])

        def process(self, direction, acl_chain, rule_list, base):
            count = base
            rule_list_splitted = []
            for rule in rule_list:
                if ',' in rule['cidr']:
                    cidrs = rule['cidr'].split(',')
                    for cidr in cidrs:
                        new_rule = {
                            'cidr': cidr,
                        }
                        if 'allowed' in rule:
                            new_rule['allowed'] = rule['allowed']
                        if 'type' in rule:
                            new_rule['type'] = rule['type']
                        if 'first_port' in rule:
                            new_rule['first_port'] = rule['first_port']
                        if 'last_port' in rule:
                            new_rule['last_port'] = rule['last_port']
                        if 'icmp_code' in rule:
                            new_rule['icmp_code'] = rule['icmp_code']
                        if 'icmp_type' in rule:
                            new_rule['icmp_type'] = rule['icmp_type']
                        rule_list_splitted.append(new_rule)
                else:
                    rule_list_splitted.append(rule)

            for i in rule_list_splitted:
                r = self.AclRule(direction, acl_chain, self, i, self.config, count)
                r.create()
                count += 1

        class AclRule():

            def __init__(self, direction, acl_chain, acl, rule, config, count):
                self.count = count
                if config.is_vpc():
                    self.init_vpc(direction, acl_chain, acl, rule, config)

            def init_vpc(self, direction, acl_chain, acl, rule, config):
                self.table = ""
                self.device = acl.device
                self.direction = direction
                # acl is an object of the AclDevice type. So, its fw attribute is already a list.
                self.fw = acl.fw
                self.chain = "%s_%s" % (acl_chain, self.device)
                self.dest = "-s %s" % rule['cidr']
                if hasattr(acl, 'public_ip'):
                    if direction == "ingress":
                        self.table = 'mangle'
                    self.dest = "-s %s -d %s" % (rule['cidr'], acl.public_ip)
                elif direction == "egress":
                    self.table = 'mangle'
                    self.dest = "-d %s" % rule['cidr']
                self.type = ""
                self.type = rule['type']
                self.icmp_type = "any"
                self.protocol = self.type
                if "icmp_type" in list(rule.keys()) and rule['icmp_type'] != -1:
                    self.icmp_type = rule['icmp_type']
                if "icmp_code" in list(rule.keys()) and rule['icmp_code'] != -1:
                    self.icmp_type = "%s/%s" % (self.icmp_type, rule['icmp_code'])
                if self.type == "protocol":
                    if rule['protocol'] == 41:
                        rule['protocol'] = "ipv6"
                    self.protocol = rule['protocol']
                self.action = "DROP"
                self.dport = ""
                if 'allowed' in list(rule.keys()) and rule['allowed']:
                    self.action = "ACCEPT"
                if 'first_port' in list(rule.keys()):
                    self.dport = "-m %s --dport %s" % (self.protocol, rule['first_port'])
                if 'last_port' in list(rule.keys()) and self.dport and \
                                rule['last_port'] != rule['first_port']:
                    self.dport = "%s:%s" % (self.dport, rule['last_port'])

            def create(self):
                rstr = ""
                rstr = "%s -A %s -p %s %s" % (rstr, self.chain, self.protocol, self.dest)
                if self.type == "icmp":
                    rstr = "%s -m icmp --icmp-type %s" % (rstr, self.icmp_type)
                rstr = "%s %s -j %s" % (rstr, self.dport, self.action)
                rstr = rstr.replace("  ", " ").lstrip()
                self.fw.append([self.table, "", rstr])

    def process(self):
        for item in self.dbag:
            if item == "id":
                continue
            if self.config.is_vpc():
                self.AclDevice(self.dbag[item], self.config).create()
            else:
                self.AclIP(self.dbag[item], self.config).create()
