import logging

from jinja2 import Environment, FileSystemLoader

import utils


class Firewall(object):
    def __init__(self, config):
        self.config = config

        self.jinja_env = Environment(
            loader=FileSystemLoader('/opt/cosmic/router/bin/cs/templates'),
            trim_blocks=True,
            lstrip_blocks=True
        )
        self.fw = self.config.fw

    def sync(self):
        if self.config.dbag_cmdline['config']['type'] == 'vpcrouter':
            self.sync_vpc()

    def sync_vpc(self):
        logging.info("Running firewall sync")
        self.add_default_vpc_rules()

        try:
            for interface in self.config.dbag_network_overview['interfaces']:
                device = utils.get_interface_name_from_mac_address(interface['mac_address'])

                if interface['metadata']['type'] in ['sync', 'other']:
                    pass
                elif interface['metadata']['type'] == 'public':
                    self.add_public_vpc_rules(device)
                elif interface['metadata']['type'] == 'guesttier':
                    self.add_tier_vpc_rules(device, interface['ipv4_addresses'][0]['cidr'])
                elif interface['metadata']['type'] == 'private':
                    self.add_private_vpc_rules(device, interface['ipv4_addresses'][0]['cidr'])
        except:
            logging.debug("Warning: Cannot find interfaces key in network overview data bag. Skipping!")

    def add_default_vpc_rules(self):
        logging.info("Configuring default VPC rules")
        self.fw.append(["filter", "", "-P INPUT DROP"])
        self.fw.append(["filter", "", "-P FORWARD DROP"])

        self.fw.append(["", "front", "-A FORWARD -j NETWORK_STATS"])
        self.fw.append(["", "front", "-A INPUT -j NETWORK_STATS"])
        self.fw.append(["", "front", "-A OUTPUT -j NETWORK_STATS"])

        self.fw.append(["filter", "", "-A FORWARD -m state --state RELATED,ESTABLISHED -j ACCEPT"])

        self.fw.append(["mangle", "front", "-A POSTROUTING -p udp -m udp --dport 68 -j CHECKSUM --checksum-fill"])

        self.fw.append(["filter", "", "-A INPUT -i lo -j ACCEPT"])
        self.fw.append(["filter", "", "-A INPUT -p icmp -j ACCEPT"])

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

        self.fw.append(["", "front", "-A NETWORK_STATS -o %s" % device])
        self.fw.append(["", "front", "-A NETWORK_STATS -i %s" % device])

        self.fw.append(["nat", "front", "-A POSTROUTING -s %s -o %s -j SNAT --to-source %s" % (
            cidr, device, cidr.split('/')[0]
        )])

        self.fw.append(["", "front", "-A INPUT -i %s -d %s -p tcp -m tcp -m state --state NEW --dport 80 -j ACCEPT" % (
            device, cidr
        )])

        self.fw.append(["", "front", "-A INPUT -i %s -d %s -p tcp -m tcp -m state --state NEW --dport 443 -j ACCEPT" % (
            device, cidr
        )])

    def add_public_vpc_rules(self, device):
        logging.info("Configuring Public VPC rules")

        # TODO FIXME Look at this rule
        # self.fw.append(["mangle", "", "-A FORWARD -j VPN_STATS_%s" % device])
        self.fw.append(["", "front", "-A NETWORK_STATS -o %s" % device])
        self.fw.append(["", "front", "-A NETWORK_STATS -i %s" % device])

        # create ingress chain mangle (port forwarding / source nat)
        self.fw.append(["mangle", "", "-N ACL_PUBLIC_IP_%s" % device])
        self.fw.append(["mangle", "", "-A PREROUTING -m state --state NEW -i %s -j ACL_PUBLIC_IP_%s" % (
            device, device
        )])

        self.fw.append(["filter", "", "-A INPUT -i %s -m state --state RELATED,ESTABLISHED -j ACCEPT" % device])

        # create ingress chain filter (load balancing)
        self.fw.append(["filter", "", "-N ACL_PUBLIC_IP_%s" % device])
        self.fw.append(["filter", "", "-A INPUT -m state --state NEW -i %s -j ACL_PUBLIC_IP_%s" % (device, device)])

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

        self.fw.append(["", "front", "-A NETWORK_STATS -o %s" % device])
        self.fw.append(["", "front", "-A NETWORK_STATS -i %s" % device])
