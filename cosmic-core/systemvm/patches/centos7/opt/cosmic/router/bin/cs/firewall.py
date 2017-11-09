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
        # self.jinja_env = Environment(loader=FileSystemLoader('/Users/bschrijver/github.com/MissionCriticalCloud/cosmic/cosmic-core/systemvm/patches/centos7/opt/cosmic/router/bin/cs/templates'), trim_blocks=True, lstrip_blocks=True)
        self.fw = self.config.fw

    def sync(self):
        if self.config.dbag_cmdline['config']['type'] == 'vpcrouter':
            self.sync_vpc()
        elif self.config.dbag_cmdline['config']['type'] == 'router':
            self.sync_nonvpc()

    def sync_vpc(self):
        self.add_default_vpc_rules()

        for interface in self.config.dbag_network_overview['interfaces']:
            device = utils.get_interface_name_from_mac_address(interface['mac_address'])

            if interface['metadata']['type'] in ['sync', 'other']:
                pass
            elif interface['metadata']['type'] == 'public':
                self.add_public_vpc_rules(device)
            elif interface['metadata']['type'] == 'tier':
                self.add_tier_vpc_rules(device, interface['ipv4_addresses'][0])
            elif interface['metadata']['type'] == 'private':
                self.add_private_vpc_rules(device, interface['ipv4_addresses'][0])

    def sync_nonvpc(self):
        print "NON VPC NOT YET IMPLEMENTED"
        logging.error("NON VPC NOT YET IMPLEMENTED")
        exit(1)

    def add_default_vpc_rules(self):
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

    def add_tier_vpc_rules(self, device, cidr):
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

        if 'source_nat' in self.config.dbag_network_overview['services'] and \
                self.config.dbag_network_overview['services']['source_nat']:
            self.fw.append(["nat", "front", "-A POSTROUTING -s %s -o %s -j SNAT --to-source %s" % (
                cidr, device, self.config.dbag_network_overview['services']['source_nat'][0]['to']
            )])

    def add_public_vpc_rules(self, device):
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

    def add_private_vpc_rules(self, device, cidr):
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
