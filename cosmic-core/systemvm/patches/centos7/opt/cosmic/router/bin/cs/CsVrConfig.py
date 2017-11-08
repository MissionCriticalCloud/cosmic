#!/usr/bin/python
# -- coding: utf-8 --

import logging

import CsHelper
from cs.CsFile import CsFile

RSYSLOG_IPTABLES_CONF = "/etc/rsyslog.d/00-iptables.conf"


class CsVrConfig(object):
    def __init__(self, config):
        self.config = config
        self.dbag = self.config.dbag_network_virtualrouter

    def process(self):
        logging.debug("Processing CsVrConfig file ==> %s" % self.dbag)

        syslogserverlist = ""

        for item in self.dbag:
            if item == "id":
                continue

            if item == "source_nat_list":
                self._configure_firewall(self.dbag[item])

            if item == "syslog_server_list":
                syslogserverlist = self.dbag[item]

        self._configure_syslog(syslogserverlist)

    def _configure_firewall(self, sourcenatlist):
        firewall = self.config.get_fw()

        logging.debug("Processing source NAT list: %s" % sourcenatlist)
        for cidr in sourcenatlist.split(','):
            firewall.append(["filter", "", "-A SOURCE_NAT_LIST -o eth1 -s %s -j ACCEPT" % cidr])

    def _configure_syslog(self, syslogserverlist):
        self.syslogconf = CsFile(RSYSLOG_IPTABLES_CONF)
        self.syslogconf.repopulate()

        logging.debug("Processing syslog server list: %s" % syslogserverlist)
        ips = filter(bool, syslogserverlist.split(','))
        if not ips:
            # no IP in the syslog server list; reset the config to default:
            self.syslogconf.append("# no remote syslog servers so stop further processing")
            self.syslogconf.append("# this file is managed by CsVrConfig.py")
            self.syslogconf.append(":msg, regex, \"^\[ *[0-9]*\.[0-9]*\] iptables denied: \" ~")
        else:
            # add IPs from the syslog server list to the config:
            self.syslogconf.append("# forwarding IP tables syslog to %s and stop further processing" % syslogserverlist)
            self.syslogconf.append("# this file is managed by CsVrConfig.py")
            first = True
            for ip in ips:
                if first:
                    self.syslogconf.append(":msg, regex, \"^\[ *[0-9]*\.[0-9]*\] iptables denied: \" @@%s:514" % ip)
                    first = False
                else:
                    self.syslogconf.append("& @@%s:514" % ip)

            self.syslogconf.append("& ~")

        changed = self.syslogconf.is_changed()
        self.syslogconf.commit()
        if changed:
            CsHelper.execute2("systemctl restart rsyslog")
