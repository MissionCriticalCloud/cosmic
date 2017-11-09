#!/usr/bin/python
# -- coding: utf-8 --
import logging
import sys
from collections import OrderedDict

import cs.CsHelper as CsHelper
from cs.CsAcl import CsAcl
from cs.CsDhcp import CsDhcp
from cs.CsForwardingRules import CsForwardingRules
from cs.CsLoadBalancer import CsLoadBalancer
from cs.CsMetadataService import CsMetadataServiceVMConfig
from cs.CsMonitor import CsMonitor
from cs.CsNetfilter import CsNetfilters
from cs.network import Network
from cs.CsRemoteAccessVpn import CsRemoteAccessVpn
from cs.CsSite2SiteVpn import CsSite2SiteVpn
from cs.CsVpnUser import CsVpnUser
from cs.CsVrConfig import CsVrConfig
from cs.config import Config
from cs.firewall import Firewall

OCCURRENCES = 1


class IpTablesExecutor:
    config = None

    def __init__(self, config):
        self.config = config

    def process(self):
        firewall = Firewall(self.config)
        firewall.sync()

        acls = CsAcl(self.config)
        acls.process()

        fwd = CsForwardingRules(self.config)
        fwd.process()

        acls = CsVrConfig(self.config)
        acls.process()

        vpns = CsSite2SiteVpn(self.config)
        vpns.process()

        rvpn = CsRemoteAccessVpn(self.config)
        rvpn.process()

        lb = CsLoadBalancer(self.config)
        lb.process()

        logging.debug("Configuring iptables rules")
        nf = CsNetfilters(self.config, False)
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
    config = Config()

    logging.basicConfig(level=logging.DEBUG, format='%(asctime)s  %(filename)s %(funcName)s:%(lineno)d %(message)s')

    # CsAddress.process

    databag_map = OrderedDict(
        [
            ("network_overview", {"process_iptables": True, "executor": IpTablesExecutor(config)}),
            ("vm_metadata", {"process_iptables": False, "executor": CsMetadataServiceVMConfig(config)}),
            ("network_acl", {"process_iptables": True, "executor": IpTablesExecutor(config)}),
            ("public_ip_acl", {"process_iptables": True, "executor": IpTablesExecutor(config)}),
            ("firewall_rules", {"process_iptables": True, "executor": IpTablesExecutor(config)}),
            ("forwarding_rules", {"process_iptables": True, "executor": IpTablesExecutor(config)}),
            ("staticnat_rules", {"process_iptables": True, "executor": IpTablesExecutor(config)}),
            ("site_2_site_vpn", {"process_iptables": True, "executor": IpTablesExecutor(config)}),
            ("remote_access_vpn", {"process_iptables": True, "executor": IpTablesExecutor(config)}),
            ("vpn_user_list", {"process_iptables": False, "executor": CsVpnUser(config)}),
            ("vm_dhcp_entry", {"process_iptables": False, "executor": CsDhcp(config)}),
            ("dhcp", {"process_iptables": False, "executor": CsDhcp(config)}),
            ("load_balancer", {"process_iptables": True, "executor": IpTablesExecutor(config)}),
            ("monitor_service", {"process_iptables": False, "executor": CsMonitor(config)}),
            ("vr", {"process_iptables": True, "executor": IpTablesExecutor(config)})
        ]
    )

    if process_file == "network_overview":
        logging.debug("Processing file %s" % process_file)
        cs_network = Network(config)
        cs_network.sync()

    if process_file == "cmd_line":
        logging.debug("cmd_line.json changed. All other files will be processed as well.")

        while databag_map:
            item = databag_map.popitem(last=False)
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


if __name__ == "__main__":
    main(sys.argv)
