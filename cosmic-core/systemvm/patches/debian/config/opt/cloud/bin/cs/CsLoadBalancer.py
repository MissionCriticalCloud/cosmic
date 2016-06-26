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

        logging.debug("CsLoadBalancer:: configuring firewall. Add rules ==> %s" % add_rules)
        logging.debug("CsLoadBalancer:: configuring firewall. Remove rules ==> %s" % remove_rules)
        logging.debug("CsLoadBalancer:: configuring firewall. Stat rules ==> %s" % stat_rules)

        for rules in add_rules:
            path = rules.split(':')
            ip = path[0]
            port = path[1]
            firewall.append(["filter", "", "-A INPUT -p tcp -m tcp -d %s --dport %s -m state --state NEW -j ACCEPT" % (ip, port)])

        for rules in stat_rules:
            path = rules.split(':')
            ip = path[0]
            port = path[1]
            firewall.append(["filter", "", "-A INPUT -p tcp -m tcp -d %s --dport %s -m state --state NEW -j ACCEPT" % (ip, port)])

        for rules in remove_rules:
            path = rules.split(':')
            ip = path[0]
            port = path[1]
            if ["filter", "", "-A INPUT -p tcp -m tcp -d %s --dport %s -m state --state NEW -j ACCEPT" % (ip, port)] in firewall:
                firewall.remove(["filter", "", "-A INPUT -p tcp -m tcp -d %s --dport %s -m state --state NEW -j ACCEPT" % (ip, port)])
