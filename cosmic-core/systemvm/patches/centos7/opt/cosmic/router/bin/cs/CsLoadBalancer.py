import logging

import CsHelper
from CsFile import CsFile
from CsProcess import CsProcess
from cs.CsDatabag import CsDataBag

HAPROXY_CONF_T = "/etc/haproxy/haproxy.cfg.new"
HAPROXY_CONF_P = "/etc/haproxy/haproxy.cfg"


class CsLoadBalancer(CsDataBag):
    """ Manage Load Balancer entries """

    def process(self):
        if "config" not in self.dbag.keys():
            return
        if 'configuration' not in self.dbag['config'][0].keys():
            return
        config = self.dbag['config'][0]['configuration']
        file1 = CsFile(HAPROXY_CONF_T)
        file1.empty()
        for x in config:
            [file1.append(w, -1) for w in x.split('\n')]

        file1.commit()
        file2 = CsFile(HAPROXY_CONF_P)
        if not file2.compare(file1):
            CsHelper.copy(HAPROXY_CONF_T, HAPROXY_CONF_P)

            proc = CsProcess(['/var/run/haproxy.pid'])
            if not proc.find():
                logging.debug("CsLoadBalancer:: will restart HAproxy!")
                CsHelper.service("haproxy", "restart")
            else:
                logging.debug("CsLoadBalancer:: will reload HAproxy!")
                CsHelper.service("haproxy", "reload")

        add_rules = self.dbag['config'][0]['add_rules']
        remove_rules = self.dbag['config'][0]['remove_rules']
        stat_rules = self.dbag['config'][0]['stat_rules']
        self._configure_firewall(add_rules, remove_rules, stat_rules)

    def _configure_firewall(self, add_rules, remove_rules, stat_rules):
        firewall = self.config.get_fw()
        ingress_rules = self.config.get_ingress_rules(self.dbag['config'][0]['router_ip'])

        logging.debug("CsLoadBalancer:: configuring firewall. Add rules ==> %s" % add_rules)
        logging.debug("CsLoadBalancer:: configuring firewall. Remove rules ==> %s" % remove_rules)
        logging.debug("CsLoadBalancer:: configuring firewall. Stat rules ==> %s" % stat_rules)

        for rules in add_rules:
            path = rules.split(':')
            ip = path[0]
            port = path[1]
            protocol = path[2]
            if len(path) > 4:
                dstport = path[4]
            else:
                dstport = None

            if ingress_rules is None or dstport is None:
                firewall.append(["filter", "", "-A INPUT -p %s -d %s --dport %s -m state --state NEW -j ACCEPT" % (protocol, ip, port)])
            else:
                for ingress_rule in ingress_rules:
                    ingress_cidrs = ingress_rule['cidr'].split(',')
                    if 'first_port' in ingress_rule.keys() and 'type' in ingress_rule.keys() and ingress_rule['first_port'] == int(dstport) and ingress_rule['type'] == protocol:
                        for ingress_cidr in ingress_cidrs:
                            firewall.append(["filter", "", "-A INPUT -i eth1 -s %s -p %s -d %s --dport %s -m state --state NEW -j ACCEPT" % (ingress_cidr, protocol, ip, port)])

        for rules in stat_rules:
            path = rules.split(':')
            ip = path[0]
            port = path[1]
            if ingress_rules is None:
                firewall.append(["filter", "", "-A INPUT -p tcp -d %s --dport %s -m state --state NEW -j ACCEPT" % (ip, port)])
            else:
                for ingress_rule in ingress_rules:
                    ingress_cidrs = ingress_rule['cidr'].split(',')
                    if 'first_port' in ingress_rule.keys() and ingress_rule['first_port'] == int(port):
                        for ingress_cidr in ingress_cidrs:
                            firewall.append(["filter", "", "-A INPUT -i eth1 -s %s -p %s -d %s --dport %s -m state --state NEW -j ACCEPT" % (ingress_cidr, ingress_rule['type'], ip, port)])

        for rules in remove_rules:
            path = rules.split(':')
            ip = path[0]
            port = path[1]
            if ["filter", "", "-A INPUT -p tcp -d %s --dport %s -m state --state NEW -j ACCEPT" % (ip, port)] in firewall:
                firewall.remove(["filter", "", "-A INPUT -p tcp -d %s --dport %s -m state --state NEW -j ACCEPT" % (ip, port)])
