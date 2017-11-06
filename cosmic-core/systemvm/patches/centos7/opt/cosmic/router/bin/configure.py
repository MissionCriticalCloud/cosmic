#!/usr/bin/python
# -- coding: utf-8 --

import re
import sys
from collections import OrderedDict

from cs.CsConfig import CsConfig
from cs.CsDatabag import CsDatabag
from cs.CsDhcp import CsDhcp
from cs.CsLoadBalancer import CsLoadBalancer
from cs.CsMetadataService import CsMetadataServiceVMConfig
from cs.CsMonitor import CsMonitor
from cs.CsNetfilter import CsNetfilters
from cs.CsRedundant import *
from cs.CsStaticRoutes import CsStaticRoutes
from cs.CsVrConfig import CsVrConfig

OCCURRENCES = 1


class CsAcl(CsDatabag):
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
            if "icmp_type" in self.rule.keys() and self.rule['icmp_type'] != -1:
                icmp_type = self.rule['icmp_type']
            if "icmp_code" in self.rule.keys() and rule['icmp_code'] != -1:
                icmp_type = "%s/%s" % (self.rule['icmp_type'], self.rule['icmp_code'])
            rnge = ''
            if "first_port" in self.rule.keys() and \
                            self.rule['first_port'] == self.rule['last_port']:
                rnge = " --dport %s " % self.rule['first_port']
            if "first_port" in self.rule.keys() and \
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
            if 'public_ip' in obj.keys():
                self.public_ip = obj['public_ip']
            if "ingress_rules" in obj.keys():
                self.ingress = obj['ingress_rules']
                config.set_ingress_rules(self.ip, obj['ingress_rules'])
            if "egress_rules" in obj.keys():
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
                if "icmp_type" in rule.keys() and rule['icmp_type'] != -1:
                    self.icmp_type = rule['icmp_type']
                if "icmp_code" in rule.keys() and rule['icmp_code'] != -1:
                    self.icmp_type = "%s/%s" % (self.icmp_type, rule['icmp_code'])
                if self.type == "protocol":
                    if rule['protocol'] == 41:
                        rule['protocol'] = "ipv6"
                    self.protocol = rule['protocol']
                self.action = "DROP"
                self.dport = ""
                if 'allowed' in rule.keys() and rule['allowed']:
                    self.action = "ACCEPT"
                if 'first_port' in rule.keys():
                    self.dport = "-m %s --dport %s" % (self.protocol, rule['first_port'])
                if 'last_port' in rule.keys() and self.dport and \
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


class CsSite2SiteVpn(CsDatabag):
    """
    Setup any configured vpns (using strongswan)
    left is the local machine
    right is where the clients connect from
    """

    VPNCONFDIR = "/etc/strongswan/ipsec.d"

    def process(self):
        self.confips = []
        # collect a list of configured vpns
        for file in os.listdir(self.VPNCONFDIR):
            m = re.search("^ipsec.vpn-(.*).conf", file)
            if m:
                self.confips.append(m.group(1))

        for vpn in self.dbag:
            if vpn == "id":
                continue

            local_ip = self.dbag[vpn]['local_public_ip']
            dev = CsHelper.get_device(local_ip)

            if dev == "":
                logging.error("Request for ipsec to %s not possible because ip is not configured", local_ip)
                continue

            self.configure_iptables(dev, self.dbag[vpn])
            self.configure_ipsec(self.dbag[vpn])

        # Delete vpns that are no longer in the configuration
        for ip in self.confips:
            self.deletevpn(ip)

        self.check_ipsec()

    def check_ipsec(self):
        CsHelper.start_if_stopped("strongswan")

        logging.info("Checking if strongswan is running correctly: systemctl status strongswan")
        p = CsHelper.execute2("systemctl status strongswan", log=False)

        out, _ = p.communicate()

        if "Security Associations" not in out:
            logging.error("Security Associations not found in: %s" % out)
            CsHelper.execute2("systemctl stop strongswan")
            CsHelper.execute2("systemctl start strongswan")

    def deletevpn(self, ip):
        logging.info("Removing VPN configuration for %s", ip)
        CsHelper.execute("strongswan down vpn-%s" % ip, wait=False)
        vpnconffile = "%s/ipsec.vpn-%s.conf" % (self.VPNCONFDIR, ip)
        vpnsecretsfile = "%s/ipsec.vpn-%s.secrets" % (self.VPNCONFDIR, ip)
        os.remove(vpnconffile)
        os.remove(vpnsecretsfile)
        CsHelper.execute("strongswan reload")

    def configure_iptables(self, dev, obj):
        self.fw.append(["", "front", "-A INPUT -i %s -p udp -m udp --dport 500 -s %s -d %s -j ACCEPT" % (dev, obj['peer_gateway_ip'], obj['local_public_ip'])])
        self.fw.append(["", "front", "-A INPUT -i %s -p udp -m udp --dport 4500 -s %s -d %s -j ACCEPT" % (dev, obj['peer_gateway_ip'], obj['local_public_ip'])])
        self.fw.append(["", "front", "-A INPUT -i %s -p esp -s %s -d %s -j ACCEPT" % (dev, obj['peer_gateway_ip'], obj['local_public_ip'])])
        self.fw.append(["nat", "front", "-A POSTROUTING -o %s -m mark --mark 0x525 -j ACCEPT" % dev])
        for net in obj['peer_guest_cidr_list'].lstrip().rstrip().split(','):
            self.fw.append(["mangle", "front",
                            "-A FORWARD -s %s -d %s -j MARK --set-xmark 0x525/0xffffffff" % (obj['local_guest_cidr'], net)])
            self.fw.append(["mangle", "",
                            "-A OUTPUT -s %s -d %s -j MARK --set-xmark 0x525/0xffffffff" % (obj['local_guest_cidr'], net)])
            self.fw.append(["mangle", "front",
                            "-A FORWARD -s %s -d %s -j MARK --set-xmark 0x524/0xffffffff" % (net, obj['local_guest_cidr'])])
            self.fw.append(["mangle", "",
                            "-A INPUT -s %s -d %s -j MARK --set-xmark 0x524/0xffffffff" % (net, obj['local_guest_cidr'])])

    def configure_ipsec(self, obj):
        leftpeer = obj['local_public_ip']
        rightpeer = obj['peer_gateway_ip']
        peerlist = obj['peer_guest_cidr_list'].split(',')
        vpnconffile = "%s/ipsec.vpn-%s.conf" % (self.VPNCONFDIR, rightpeer)
        vpnsecretsfile = "%s/ipsec.vpn-%s.secrets" % (self.VPNCONFDIR, rightpeer)
        ikepolicy = obj['ike_policy'].replace(';', '-')
        esppolicy = obj['esp_policy'].replace(';', '-')

        strokefile = '/etc/strongswan.d/charon/stroke.conf'

        # Set timeout to 30s
        file = CsFile(strokefile)
        file.greplace("# timeout = 0", "timeout = 30000")
        file.commit()

        if rightpeer in self.confips:
            self.confips.remove(rightpeer)
        file = CsFile(vpnconffile)
        file.empty()
        for idx, p in enumerate(peerlist):
            if idx == 0:
                file.add("#conn for vpn-%s" % rightpeer, -1)
                file.add("conn vpn-%s" % rightpeer, -1)
            else:
                file.add("#conn for vpn-%s-%i" % (rightpeer, idx), -1)
                file.add("conn vpn-%s-%i" % (rightpeer, idx), -1)
            file.add(" left=%s" % leftpeer, -1)
            file.add(" leftsubnet=%s" % obj['local_guest_cidr'], -1)
            file.add(" right=%s" % rightpeer, -1)
            file.add(" rightsubnet=%s" % p, -1)
            file.add(" type=tunnel", -1)
            file.add(" authby=secret", -1)
            file.add(" keyexchange=ike", -1)
            file.add(" ike=%s" % ikepolicy, -1)
            file.add(" ikelifetime=%s" % self.convert_sec_to_h(obj['ike_lifetime']), -1)
            file.add(" esp=%s" % esppolicy, -1)
            file.add(" lifetime=%s" % self.convert_sec_to_h(obj['esp_lifetime']), -1)
            file.add(" keyingtries=%forever", -1)
            file.add(" auto=start", -1)
            file.add(" closeaction=restart", -1)
            file.add(" inactivity=0", -1)
            if 'encap' not in obj:
                obj['encap'] = False
            file.add(" forceencaps=%s" % CsHelper.bool_to_yn(obj['encap']), -1)
            if obj['dpd']:
                file.add(" dpddelay=30", -1)
                file.add(" dpdtimeout=120", -1)
                file.add(" dpdaction=restart", -1)
            file.add(" ", -1)
        secret = CsFile(vpnsecretsfile)
        secret.search("%s " % leftpeer, "%s %s : PSK \"%s\"" % (leftpeer, rightpeer, obj['ipsec_psk']))
        if secret.is_changed() or file.is_changed():
            secret.commit()
            file.commit()
            logging.info("Configured vpn %s %s", leftpeer, rightpeer)
            CsHelper.execute("strongswan rereadsecrets")

        # This will load the new config and start the connection when needed since auto=start in the config
        os.chmod(vpnsecretsfile, 0o400)
        CsHelper.execute("strongswan reload")

    def convert_sec_to_h(self, val):
        hrs = int(val) / 3600
        return "%sh" % hrs


class CsVpnUser(CsDatabag):
    PPP_CHAP = '/etc/ppp/chap-secrets'

    def process(self):
        for user in self.dbag:
            if user == 'id':
                continue

            userconfig = self.dbag[user]
            if userconfig['add']:
                self.add_l2tp_ipsec_user(user, userconfig)
            else:
                self.del_l2tp_ipsec_user(user, userconfig)

    def add_l2tp_ipsec_user(self, user, obj):
        userfound = False
        password = obj['password']

        userSearchEntry = "%s \* %s \*" % (user, password)
        userAddEntry = "%s * %s *" % (user, password)
        logging.debug("Adding vpn user %s" % userSearchEntry)

        file = CsFile(self.PPP_CHAP)
        userfound = file.searchString(userSearchEntry, '#')
        if not userfound:
            logging.debug("User is not there already, so adding user ")
            self.del_l2tp_ipsec_user(user, obj)
            file.add(userAddEntry)
        file.commit()

    def del_l2tp_ipsec_user(self, user, obj):
        userfound = False
        password = obj['password']
        userentry = "%s \* %s \*" % (user, password)

        logging.debug("Deleting the user %s " % user)
        file = CsFile(self.PPP_CHAP)
        file.deleteLine(userentry)
        file.commit()

        if not os.path.exists('/var/run/pppd2.tdb'):
            return

        logging.debug("kiing the PPPD process for the user %s " % user)

        fileContents = CsHelper.execute("tdbdump /var/run/pppd2.tdb")
        print(fileContents)

        for line in fileContents:
            if user in line:
                contentlist = line.split(';')
                for str in contentlist:
                    print('in del_l2tp str = ' + str)
                    pppd = str.split('=')[0]
                    if pppd == 'PPPD_PID':
                        pid = str.split('=')[1]
                        if pid:
                            logging.debug("killing process %s" % pid)
                            CsHelper.execute('kill -9 %s' % pid)


class CsRemoteAccessVpn(CsDatabag):
    VPNCONFDIR = "/etc/strongswan/ipsec.d"

    def process(self):
        self.confips = []

        logging.debug(self.dbag)
        for public_ip in self.dbag:
            if public_ip == "id":
                continue
            vpnconfig = self.dbag[public_ip]

            # Enable remote access vpn
            if vpnconfig['create']:
                logging.debug("Enabling  remote access vpn  on " + public_ip)
                CsHelper.start_if_stopped("strongswan")
                self.configure_l2tpIpsec(public_ip, self.dbag[public_ip])
                logging.debug("Remote accessvpn  data bag %s", self.dbag)
                self.remoteaccessvpn_iptables(public_ip, self.dbag[public_ip])

                CsHelper.execute("strongswan update")
                CsHelper.execute("systemctl start xl2tpd")
                CsHelper.execute("strongswan rereadsecrets")
            else:
                logging.debug("Disabling remote access vpn .....")
                # disable remote access vpn
                CsHelper.execute("strongswan down L2TP-PSK")
                CsHelper.execute("systemctl stop xl2tpd")

    def configure_l2tpIpsec(self, left, obj):
        l2tpconffile = "%s/l2tp.conf" % (self.VPNCONFDIR)
        vpnsecretfilte = "%s/ipsec.any.secrets" % (self.VPNCONFDIR)
        xl2tpdconffile = "/etc/xl2tpd/xl2tpd.conf"
        xl2tpoptionsfile = '/etc/ppp/options.xl2tpd'
        strokefile = '/etc/strongswan.d/charon/stroke.conf'

        file = CsFile(l2tpconffile)
        localip = obj['local_ip']
        iprange = obj['ip_range']
        psk = obj['preshared_key']

        # l2tp config options
        file.search("rekey=", "        rekey=no")
        file.search("keyingtries=", "        keyingtries=3")
        file.search("keyexchange=", "        keyexchange=ikev1")
        file.search("forceencaps=", "        forceencaps=yes")
        file.search("leftfirewall=", "        leftfirewall=yes")
        file.search("leftnexthop=", "        leftnexthop=%defaultroute")
        file.search("type=", "        type=transport")
        file.search("leftprotoport=", "        leftprotoport=17/1701")
        file.search("right=", "        right=%any")
        file.search("rightprotoport=", "        rightprotoport=17/%any")
        file.search("auto=", "        auto=add")
        file.search("rightsubnetwithin=", "        rightsubnetwithin=0.0.0.0/0")
        file.search("left=", "        left=%s" % left)
        file.search("authby=", "        authby=psk")
        file.commit()

        # Set timeout to 30s
        file = CsFile(strokefile)
        file.greplace("# timeout = 0", "timeout = 30000")
        file.commit()

        # Secrets
        secret = CsFile(vpnsecretfilte)
        secret.addeq(": PSK \"%s\"" % psk)
        secret.commit()

        xl2tpdconf = CsFile(xl2tpdconffile)
        xl2tpdconf.addeq("ip range = %s" % iprange)
        xl2tpdconf.addeq("local ip = %s" % localip)
        xl2tpdconf.commit()

        xl2tpoptions = CsFile(xl2tpoptionsfile)
        xl2tpoptions.search("ms-dns ", "ms-dns %s" % localip)
        xl2tpoptions.commit()

    def remoteaccessvpn_iptables(self, publicip, obj):
        publicdev = obj['public_interface']
        localcidr = obj['local_cidr']
        local_ip = obj['local_ip']

        self.fw.append(["", "", "-A INPUT -i %s --dst %s -p udp -m udp --dport 500 -j ACCEPT" % (publicdev, publicip)])
        self.fw.append(["", "", "-A INPUT -i %s --dst %s -p udp -m udp --dport 4500 -j ACCEPT" % (publicdev, publicip)])
        self.fw.append(["", "", "-A INPUT -i %s --dst %s -p udp -m udp --dport 1701 -j ACCEPT" % (publicdev, publicip)])
        self.fw.append(["", "", "-A INPUT -i %s -p ah -j ACCEPT" % publicdev])
        self.fw.append(["", "", "-A INPUT -i %s -p esp -j ACCEPT" % publicdev])

        if self.config.is_vpc():
            self.fw.append(["", "", " -N VPN_FORWARD"])
            self.fw.append(["", "", "-I FORWARD -i ppp+ -j VPN_FORWARD"])
            self.fw.append(["", "", "-I FORWARD -o ppp+ -j VPN_FORWARD"])
            self.fw.append(["", "", "-I FORWARD -o ppp+ -j VPN_FORWARD"])
            self.fw.append(["", "", "-A VPN_FORWARD -s  %s -j RETURN" % localcidr])
            self.fw.append(["", "", "-A VPN_FORWARD -i ppp+ -d %s -j RETURN" % localcidr])
            self.fw.append(["", "", "-A VPN_FORWARD -i ppp+  -o ppp+ -j RETURN"])
        else:
            self.fw.append(["", "", "-A FORWARD -i ppp+ -o  ppp+ -j ACCEPT"])
            self.fw.append(["", "", "-A FORWARD -s %s -o  ppp+ -j ACCEPT" % localcidr])
            self.fw.append(["", "", "-A FORWARD -i ppp+ -d %s  -j ACCEPT" % localcidr])

        self.fw.append(["", "", "-A INPUT -i ppp+ -m udp -p udp --dport 53 -j ACCEPT"])
        self.fw.append(["", "", "-A INPUT -i ppp+ -m tcp -p tcp --dport 53 -j ACCEPT"])
        self.fw.append(["nat", "front", "-A PREROUTING -i ppp+ -m tcp -p tcp --dport 53 -j DNAT --to-destination %s" % local_ip])

        if self.config.is_vpc():
            return

        self.fw.append(["mangle", "", "-N  VPN_%s " % publicip])
        self.fw.append(["mangle", "", "-A VPN_%s -j RETURN " % publicip])
        self.fw.append(["mangle", "", "-I VPN_%s -p ah  -j ACCEPT " % publicip])
        self.fw.append(["mangle", "", "-I VPN_%s -p esp  -j ACCEPT " % publicip])
        self.fw.append(["mangle", "", "-I PREROUTING  -d %s -j VPN_%s " % (publicip, publicip)])


class CsForwardingRules(CsDatabag):
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
        self.fw.append(["nat", "", fw1])
        self.fw.append(["nat", "", fw2])
        self.fw.append(["nat", "", fw3])
        self.fw.append(["nat", "", fw4])
        self.fw.append(["nat", "", fw5])
        self.fw.append(["nat", "", fw6])
        self.fw.append(["filter", "", fw7])

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

        self.fw.append(["nat", "", fw_prerout_rule])
        self.fw.append(["nat", "", fw_postrout_rule])
        self.fw.append(["nat", "", fw_output_rule])

    def processStaticNatRule(self, rule):
        # FIXME this needs ordering with the VPN no nat rule
        device = self.getDeviceByIp(rule["public_ip"])
        if device is None:
            raise Exception("Ip address %s has no device in the ips databag" % rule["public_ip"])
        self.fw.append(["nat", "front",
                        "-A PREROUTING -d %s/32 -j DNAT --to-destination %s" % (rule["public_ip"], rule["internal_ip"])])
        self.fw.append(["nat", "front",
                        "-A POSTROUTING -o %s -s %s/32 -j SNAT --to-source %s" % (device, rule["internal_ip"], rule["public_ip"])])
        self.fw.append(["nat", "front",
                        "-A OUTPUT -d %s/32 -j DNAT --to-destination %s" % (rule["public_ip"], rule["internal_ip"])])
        self.fw.append(["filter", "",
                        "-A FORWARD -i %s -o eth0  -d %s  -m state  --state NEW -j ACCEPT " % (device, rule["internal_ip"])])

        # configure the hairpin nat
        self.fw.append(["nat", "front",
                        "-A PREROUTING -d %s -i eth0 -j DNAT --to-destination %s" % (rule["public_ip"], rule["internal_ip"])])

        self.fw.append(
            ["nat", "front", "-A POSTROUTING -s %s -d %s -j SNAT -o eth0 --to-source %s" % (self.getNetworkByIp(rule['internal_ip']), rule["internal_ip"], self.getGuestIp())])


class IpTablesExecutor:
    config = None

    def __init__(self, config):
        self.config = config

    def process(self):
        acls = CsAcl('networkacl', self.config)
        acls.process()

        acls = CsAcl('publicipacl', self.config)
        acls.process()

        acls = CsAcl('firewallrules', self.config)
        acls.process()

        fwd = CsForwardingRules("forwardingrules", self.config)
        fwd.process()

        acls = CsVrConfig('virtualrouter', self.config)
        acls.process()

        logging.debug("Found StrongSwan compatible systemvm template so let's configure VPN with it")
        vpns = CsSite2SiteVpn("site2sitevpn", self.config)
        vpns.process()
        rvpn = CsRemoteAccessVpn("remoteaccessvpn", self.config)
        rvpn.process()

        lb = CsLoadBalancer("loadbalancer", self.config)
        lb.process()

        logging.debug("Configuring iptables rules")
        nf = CsNetfilters(False)
        nf.compare(self.config.get_fw())

        logging.debug("Configuring iptables rules done ...saving rules")

        # Save iptables configuration - will be loaded on reboot by the iptables-restore that is configured on /etc/rc.local
        CsHelper.save_iptables("iptables-save", "/etc/iptables/router_rules.v4")
        CsHelper.save_iptables("ip6tables-save", "/etc/iptables/router_rules.v6")


def main(argv):
    # The file we are currently processing, if it is "cmd_line.json" everything will be processed.
    process_file = argv[1]
    logging.debug("Processing file %s" % process_file)
    process_file = process_file.split('.')[0]

    if process_file is None:
        logging.debug("No file was received, do not go on processing the other actions. Just leave for now.")
        return

    # The "GLOBAL" Configuration object
    config = CsConfig()

    logging.basicConfig(level=logging.DEBUG, format='%(asctime)s  %(filename)s %(funcName)s:%(lineno)d %(message)s')

    databag_map = OrderedDict(
        [
            ("network.json", {"process_iptables": True, "executor": IpTablesExecutor(config)}),
            ("vm_metadata.json", {"process_iptables": False, "executor": CsMetadataServiceVMConfig('vmdata', config)}),
            ("network_acl.json", {"process_iptables": True, "executor": IpTablesExecutor(config)}),
            ("public_ip_acl.json", {"process_iptables": True, "executor": IpTablesExecutor(config)}),
            ("firewall_rules.json", {"process_iptables": True, "executor": IpTablesExecutor(config)}),
            ("forwarding_rules.json", {"process_iptables": True, "executor": IpTablesExecutor(config)}),
            ("staticnat_rules.json", {"process_iptables": True, "executor": IpTablesExecutor(config)}),
            ("site_2_site_vpn.json", {"process_iptables": True, "executor": IpTablesExecutor(config)}),
            ("remote_access_vpn.json", {"process_iptables": True, "executor": IpTablesExecutor(config)}),
            ("vpn_user_list.json", {"process_iptables": False, "executor": CsVpnUser("vpnuserlist", config)}),
            ("vm_dhcp_entry.json", {"process_iptables": False, "executor": CsDhcp("dhcpentry", config)}),
            ("dhcp.json", {"process_iptables": False, "executor": CsDhcp("dhcpentry", config)}),
            ("load_balancer.json", {"process_iptables": True, "executor": IpTablesExecutor(config)}),
            ("monitor_service.json", {"process_iptables": False, "executor": CsMonitor("monitorservice", config)}),
            ("static_routes.json", {"process_iptables": False, "executor": CsStaticRoutes("staticroutes", config)}),
            ("vr.json", {"process_iptables": True, "executor": IpTablesExecutor(config)})
        ]
    )

    if process_file == "network.json":
        logging.debug("Processing file %s" % process_file)
        cs_network = CsNetwork(process_file)
        cs_network.sync()

    if process_file == "cmd_line.json":
        logging.debug("cmd_line.json changed. All other files will be processed as well.")

        while databag_map:
            item = databag_map.popitem(last=False)
            item_name = item[0]
            item_dict = item[1]
            if not item_dict["process_iptables"]:
                executor = item_dict["executor"]
                executor.process()

        iptables_executor = IpTablesExecutor(config)
        iptables_executor.process()
    else:
        while databag_map:
            item = databag_map.popitem(last=False)
            item_name = item[0]
            item_dict = item[1]
            if process_file.count(item_name) == OCCURRENCES:
                executor = item_dict["executor"]
                executor.process()

                if item_dict["process_iptables"]:
                    iptables_executor = IpTablesExecutor(config)
                    iptables_executor.process()

                break

    red = CsRedundant(config)
    red.set()


if __name__ == "__main__":
    main(sys.argv)
