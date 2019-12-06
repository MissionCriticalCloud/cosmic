import logging

from jinja2 import Environment, FileSystemLoader

import utils


class Firewall:

    def __init__(self, config):
        self.config = config

        self.jinja_env = Environment(
            loader=FileSystemLoader('/opt/cosmic/router/bin/cs/templates'),
            trim_blocks=True,
            lstrip_blocks=True
        )
        self.fw = self.config.fw

    def sync(self):
        logging.info("Running firewall sync")
        public_device = None
        public_ip = None

        self.add_default_vpc_rules()

        if "interfaces" not in self.config.dbag_network_overview:
            logging.info("Skipping firewall sync, as we have no 'interfaces' object in network_overview.")
            return

        for interface in self.config.dbag_network_overview['interfaces']:
            device = utils.get_interface_name_from_mac_address(interface['mac_address'])

            if interface['metadata']['type'] == 'sync':
                self.add_sync_vpc_rules(device)
            elif interface['metadata']['type'] == 'other':
                pass
            elif interface['metadata']['type'] == 'public':
                self.add_public_vpc_rules(device)
                public_device = device
                public_ip = interface['ipv4_addresses'][0]['cidr']
            elif interface['metadata']['type'] == 'guesttier':
                self.add_tier_vpc_rules(device, interface['ipv4_addresses'][0]['cidr'])
            elif interface['metadata']['type'] == 'private':
                self.add_private_vpc_rules(device, interface['ipv4_addresses'][0]['cidr'])

        vpn_open = False
        if public_device is not None and 'vpn' in self.config.dbag_network_overview:
            if 'site2site' in self.config.dbag_network_overview['vpn']:
                for site2site in self.config.dbag_network_overview['vpn']['site2site']:
                    self.add_site2site_vpn_rules(public_device, site2site)
                    vpn_open = True
            if 'remote_access' in self.config.dbag_network_overview['vpn']:
                if public_ip is not None:
                    self.add_remote_access_vpn_rules(
                        public_device, public_ip, self.config.dbag_network_overview['vpn']['remote_access']
                    )
                vpn_open = True

        # default block VPN ports
        logging.info("VPN_open is %s" % (vpn_open))
        if not vpn_open:
            self.block_vpn_rules(public_device)

    def add_default_vpc_rules(self):
        logging.info("Configuring default VPC rules")

        self.fw.append(["filter", "", "-P INPUT DROP"])
        self.fw.append(["filter", "", "-P FORWARD DROP"])

        self.fw.append(["filter", "", "-A FORWARD -m state --state RELATED,ESTABLISHED -j ACCEPT"])

        self.fw.append(["mangle", "front", "-A POSTROUTING -p udp -m udp --dport 68 -j CHECKSUM --checksum-fill"])

        self.fw.append(["filter", "", "-A INPUT -i lo -j ACCEPT"])
        self.fw.append(["filter", "", "-A INPUT -p icmp -j ACCEPT"])

        if self.config.get_advert_method() == "MULTICAST":
            self.fw.append(["filter", "", "-A INPUT -d 224.0.0.18/32 -j ACCEPT"])
            self.fw.append(["filter", "", "-A INPUT -d 224.0.0.22/32 -j ACCEPT"])
            self.fw.append(["filter", "", "-A INPUT -d 224.0.0.252/32 -j ACCEPT"])
            self.fw.append(["filter", "", "-A INPUT -d 225.0.0.50/32 -j ACCEPT"])

        self.fw.append(["filter", "",
                        "-A INPUT -i eth0 -p tcp -m tcp -s 169.254.0.1/32 --dport 3922 -m "
                        "state --state NEW,ESTABLISHED -j ACCEPT"])
        self.fw.append(["filter", "", "-A INPUT -m state --state RELATED,ESTABLISHED -j ACCEPT"])

        self.fw.append(["filter", "", "-A FORWARD -s %s ! -d %s -j ACCEPT" % (
            self.config.dbag_cmdline['config']['vpccidr'], self.config.dbag_cmdline['config']['vpccidr']
        )])

    def add_tier_vpc_rules(self, device, cidr):
        logging.info("Configuring VPC tier rules for device %s" % device)

        self.fw.append(["filter", "", "-A INPUT -i %s -m state --state RELATED,ESTABLISHED -j ACCEPT" % device])
        self.fw.append(["filter", "", "-A FORWARD -m state --state NEW -o %s -j ACL_INBOUND_%s" % (device, device)])
        self.fw.append(["filter", "", "-A OUTPUT -m state --state NEW -o %s -j ACL_INBOUND_%s" % (device, device)])

        self.fw.append(["filter", "front", "-A ACL_INBOUND_%s -d 224.0.0.18/32 -j ACCEPT" % device])
        self.fw.append(["filter", "front", "-A ACL_INBOUND_%s -d 224.0.0.22/32 -j ACCEPT" % device])
        self.fw.append(["filter", "front", "-A ACL_INBOUND_%s -d 224.0.0.252/32 -j ACCEPT" % device])
        self.fw.append(["filter", "front", "-A ACL_INBOUND_%s -d 225.0.0.50/32 -j ACCEPT" % device])
        self.fw.append(["filter", "front", "-A ACL_INBOUND_%s -d %s -p udp -m udp --dport 68 -j ACCEPT" % (
            device, cidr
        )])
        self.fw.append(["filter", "", "-A INPUT -i %s -p udp -m udp --dport 67 -j ACCEPT" % device])
        self.fw.append(["filter", "", "-A INPUT -i %s -p udp -m udp --dport 53 -s %s -j ACCEPT" % (device, cidr)])
        self.fw.append(["filter", "", "-A INPUT -i %s -p tcp -m tcp --dport 53 -s %s -j ACCEPT" % (device, cidr)])
        self.fw.append(["filter", "", "-A INPUT -i %s -p tcp -m tcp --dport 80 -m state --state NEW -j ACCEPT" %
                        device
                        ])
        self.fw.append(["filter", "", "-A INPUT -i %s -p tcp -m tcp --dport 8080 -m state --state NEW -j ACCEPT" %
                        device])

        self.fw.append(["mangle", "", "-A PREROUTING -m state --state NEW -i %s ! -d %s -j ACL_OUTBOUND_%s" % (
            device, cidr, device
        )])
        self.fw.append(["mangle", "front", "-A ACL_OUTBOUND_%s -d 224.0.0.18/32 -j ACCEPT" % device])
        self.fw.append(["mangle", "front", "-A ACL_OUTBOUND_%s -d 224.0.0.22/32 -j ACCEPT" % device])
        self.fw.append(["mangle", "front", "-A ACL_OUTBOUND_%s -d 224.0.0.252/32 -j ACCEPT" % device])
        self.fw.append(["mangle", "front", "-A ACL_OUTBOUND_%s -d 225.0.0.50/32 -j ACCEPT" % device])
        self.fw.append(["mangle", "front", "-A ACL_OUTBOUND_%s -d 255.255.255.255/32 -j ACCEPT" % device])

        self.fw.append(["nat", "front", "-A POSTROUTING -s %s -o %s -j SNAT --to-source %s" % (
            cidr, device, cidr.split('/')[0]
        )])

        self.fw.append(["", "front", "-A INPUT -i %s -d %s -p tcp -m tcp -m state --state NEW --dport 80 -j ACCEPT" % (
            device, cidr
        )])

        self.fw.append(["", "front", "-A INPUT -i %s -d %s -p tcp -m tcp -m state --state NEW --dport 443 -j ACCEPT" % (
            device, cidr
        )])

    def add_sync_vpc_rules(self, device):
        logging.info("Configuring Sync VPC rules")

        if self.config.get_advert_method() == "UNICAST":
            self.fw.append(["filter", "", "-A INPUT -i %s -p vrrp -j ACCEPT" % device])
            self.fw.append(["filter", "", "-A OUTPUT -o %s -p vrrp -j ACCEPT" % device])
            self.fw.append(["filter", "", "-A INPUT -i %s -p tcp --dport 3780 -j ACCEPT" % device])
            self.fw.append(["filter", "", "-A OUTPUT -o %s -p tcp --dport 3780 -j ACCEPT" % device])

    def add_public_vpc_rules(self, device):
        logging.info("Configuring Public VPC rules")

        # create ingress chain mangle (port forwarding / source nat)
        self.fw.append(["mangle", "", "-N ACL_PUBLIC_IP_%s" % device])
        self.fw.append(["mangle", "", "-A PREROUTING -m state --state NEW -i %s -j ACL_PUBLIC_IP_%s" % (
            device, device
        )])

        self.fw.append(["filter", "", "-A INPUT -i %s -m state --state RELATED,ESTABLISHED -j ACCEPT" % device])

        # create ingress chain filter (load balancing)
        self.fw.append(["filter", "", "-N ACL_PUBLIC_IP_%s" % device])
        self.fw.append(["filter", "", "-A INPUT -m state --state NEW -j ACL_PUBLIC_IP_%s" % device])

        # create egress chain
        self.fw.append(["mangle", "front", "-N ACL_OUTBOUND_%s" % device])
        # jump to egress chain
        self.fw.append(["mangle", "front", "-A PREROUTING -m state --state NEW -i %s -j ACL_OUTBOUND_%s" % (
            device, device
        )])

        # create source nat list chain
        self.fw.append(["filter", "", "-N SOURCE_NAT_LIST"])
        self.fw.append(["filter", "", "-A FORWARD -j SOURCE_NAT_LIST"])

        if 'source_nat' in self.config.dbag_network_overview['services'] and \
                self.config.dbag_network_overview['services']['source_nat']:
            logging.info("Adding SourceNAT for interface %s to %s" % (
                device, self.config.dbag_network_overview['services']['source_nat'][0]['to']
            ))
            self.fw.append(["nat", "", "-A POSTROUTING -o %s -d 10.0.0.0/8 -j RETURN" % device])
            self.fw.append(["nat", "", "-A POSTROUTING -o %s -d 172.16.0.0/12 -j RETURN" % device])
            self.fw.append(["nat", "", "-A POSTROUTING -o %s -d 192.168.0.0/16 -j RETURN" % device])
            self.fw.append(["nat", "", "-A POSTROUTING -j SNAT -o %s --to-source %s" % (
                device, self.config.dbag_network_overview['services']['source_nat'][0]['to']
            )])

    def add_private_vpc_rules(self, device, cidr):
        logging.info("Configuring Private VPC rules")

        self.fw.append(["filter", "", "-A INPUT -i %s -m state --state RELATED,ESTABLISHED -j ACCEPT" % device])

        # create egress chain
        self.fw.append(["mangle", "", "-N ACL_OUTBOUND_%s" % device])
        # jump to egress chain
        self.fw.append(["mangle", "", "-A PREROUTING -m state --state NEW -i %s ! -d %s -j ACL_OUTBOUND_%s" % (
            device, cidr, device
        )])
        # create ingress chain
        self.fw.append(["filter", "", "-N ACL_INBOUND_%s" % device])
        # jump to ingress chain
        self.fw.append(["filter", "", "-A FORWARD -m state --state NEW -o %s -j ACL_INBOUND_%s" % (device, device)])

    def add_site2site_vpn_rules(self, device, site2site):
        logging.info("Configuring Site2Site VPN rules")

        self.config.fw.append(["", "front", "-A INPUT -i %s -p udp -m udp --dport 500 -s %s -d %s -j ACCEPT" % (
            device, site2site['right'], site2site['left'])])
        self.config.fw.append(["", "front", "-A INPUT -i %s -p udp -m udp --dport 4500 -s %s -d %s -j ACCEPT" % (
            device, site2site['right'], site2site['left'])])
        self.config.fw.append(["", "front", "-A INPUT -i %s -p esp -s %s -d %s -j ACCEPT" % (
            device, site2site['right'], site2site['left'])])
        self.config.fw.append(["nat", "front", "-A POSTROUTING -o %s -m mark --mark 0x525 -j ACCEPT" % device])

        # Make it possible to tcpdump on ipsec tunnels
        # https://wiki.strongswan.org/projects/strongswan/wiki/CorrectTrafficDump

        # ingress IPsec and IKE Traffic rule
        self.config.fw.append(["filter", "front", "-I INPUT -p esp -j NFLOG --nflog-group 5"])
        self.config.fw.append(["filter", "front", "-I INPUT -p ah -j NFLOG --nflog-group 5"])
        self.config.fw.append(["filter", "front",
                               "-I INPUT -p udp -m multiport --dports 500,4500 -j NFLOG --nflog-group 5"])

        # egress IPsec and IKE traffic
        self.config.fw.append(["filter", "front", "-I OUTPUT -p esp -j NFLOG --nflog-group 5"])
        self.config.fw.append(["filter", "front", "-I OUTPUT -p ah -j NFLOG --nflog-group 5"])
        self.config.fw.append(["filter", "front",
                               "-I OUTPUT -p udp -m multiport --dports 500,4500 -j NFLOG --nflog-group 5"])

        # decapsulated IPsec traffic
        self.config.fw.append(["mangle", "front",
                               "-I PREROUTING -m policy --pol ipsec --dir in -j NFLOG --nflog-group 5"])
        self.config.fw.append(["mangle", "front",
                               "-I POSTROUTING -m policy --pol ipsec --dir out -j NFLOG --nflog-group 5"])

        # IPsec traffic that is destinated for the local host (iptables INPUT chain)
        self.config.fw.append(["filter", "front",
                               "-I INPUT -m addrtype --dst-type LOCAL -m policy --pol ipsec --dir in"
                               " -j NFLOG --nflog-group 5"])

        # IPsec traffic that is destinated for a remote host (iptables FORWARD chain)
        self.config.fw.append(["filter", "front",
                               "-I INPUT -m addrtype ! --dst-type LOCAL -m policy --pol ipsec --dir in"
                               " -j NFLOG --nflog-group 5"])

        # IPsec traffic that is outgoing (iptables OUTPUT chain)
        self.config.fw.append(["filter", "front", "-I OUTPUT -m policy --pol ipsec --dir out -j NFLOG --nflog-group 5"])

        for net in site2site['peer_list'].lstrip().rstrip().split(','):
            self.config.fw.append(["mangle", "front",
                                   "-A FORWARD -s %s -d %s -j MARK --set-xmark 0x525/0xffffffff" % (
                                       site2site['left_subnet'], net)])
            self.config.fw.append(["mangle", "",
                                   "-A OUTPUT -s %s -d %s -j MARK --set-xmark 0x525/0xffffffff" % (
                                       site2site['left_subnet'], net)])
            self.config.fw.append(["mangle", "front",
                                   "-A FORWARD -s %s -d %s -j MARK --set-xmark 0x524/0xffffffff" % (
                                       net, site2site['left_subnet'])])
            self.config.fw.append(["mangle", "",
                                   "-A INPUT -s %s -d %s -j MARK --set-xmark 0x524/0xffffffff" % (
                                       net, site2site['left_subnet'])])
        # Block anything else
        self.block_vpn_rules(device)

    def add_remote_access_vpn_rules(self, device, publicip, remote_access):
        logging.info("Configuring RemoteAccess VPN rules")

        localcidr = remote_access['local_cidr']
        local_ip = remote_access['local_ip']

        self.config.fw.append(["", "", "-I INPUT -i %s --dst %s -p udp -m udp --dport 500 -j ACCEPT" % (device, publicip.split("/")[0])])
        self.config.fw.append(["", "", "-I INPUT -i %s --dst %s -p udp -m udp --dport 4500 -j ACCEPT" % (device, publicip.split("/")[0])])
        self.config.fw.append(["", "", "-I INPUT -i %s --dst %s -p udp -m udp --dport 1701 -j ACCEPT" % (device, publicip.split("/")[0])])
        self.config.fw.append(["", "", "-I INPUT -i %s ! --dst %s -p udp -m udp --dport 500 -j REJECT" % (device, publicip.split("/")[0])])
        self.config.fw.append(["", "", "-I INPUT -i %s ! --dst %s -p udp -m udp --dport 4500 -j REJECT" % (device, publicip.split("/")[0])])
        self.config.fw.append(["", "", "-I INPUT -i %s ! --dst %s -p udp -m udp --dport 1701 -j REJECT" % (device, publicip.split("/")[0])])
        self.config.fw.append(["", "", "-I INPUT -i %s -p ah -j ACCEPT" % device])
        self.config.fw.append(["", "", "-I INPUT -i %s -p esp -j ACCEPT" % device])
        self.config.fw.append(["", "", " -N VPN_FORWARD"])
        self.config.fw.append(["", "", "-I FORWARD -i ppp+ -j VPN_FORWARD"])
        self.config.fw.append(["", "", "-I FORWARD -o ppp+ -j VPN_FORWARD"])
        self.config.fw.append(["", "", "-I FORWARD -o ppp+ -j VPN_FORWARD"])
        self.config.fw.append(["", "", "-A VPN_FORWARD -s  %s -j RETURN" % localcidr])
        self.config.fw.append(["", "", "-A VPN_FORWARD -i ppp+ -d %s -j RETURN" % localcidr])
        self.config.fw.append(["", "", "-A VPN_FORWARD -i ppp+  -o ppp+ -j RETURN"])
        self.config.fw.append(["", "", "-I INPUT -i ppp+ -m udp -p udp --dport 53 -j ACCEPT"])
        self.config.fw.append(["", "", "-I INPUT -i ppp+ -m tcp -p tcp --dport 53 -j ACCEPT"])
        self.config.fw.append(["nat", "front", "-A PREROUTING -i ppp+ -m tcp -p tcp --dport 53 -j DNAT --to-destination %s" % local_ip])

    def block_vpn_rules(self, device):
        logging.info("Dropping VPN rules")

        self.config.fw.append(["", "", "-A INPUT -i %s -p udp -m udp --dport 500 -j REJECT" % device])
        self.config.fw.append(["", "", "-A INPUT -i %s -p udp -m udp --dport 4500 -j REJECT" % device])
        self.config.fw.append(["", "", "-A INPUT -i %s -p udp -m udp --dport 1701 -j REJECT" % device])
        self.config.fw.append(["", "", "-A INPUT -i %s -p ah -j REJECT" % device])
        self.config.fw.append(["", "", "-A INPUT -i %s -p esp -j REJECT" % device])
        self.config.fw.append(["", "", "-A INPUT -i ppp+ -m udp -p udp --dport 53 -j REJECT"])
        self.config.fw.append(["", "", "-A INPUT -i ppp+ -m tcp -p tcp --dport 53 -j REJECT"])
