# -- coding: utf-8 --

import os
import glob
import logging

import CsHelper
from CsFile import CsFile
from CsProcess import CsProcess


class CsApp:
    def __init__(self, ip):
        self.dev = ip.getDevice()
        self.ip = ip.get_ip_address()
        self.type = ip.get_type()
        self.fw = ip.fw
        self.config = ip.config


class CsPasswdSvc():
    """
      nohup bash /opt/cloud/bin/vpc_passwd_server $ip >/dev/null 2>&1 &
    """

    def __init__(self, ip):
        self.ip = ip

    def start(self):
        proc = CsProcess(["dummy"])
        if proc.grep("passwd_server_ip %s" % self.ip) == -1:
            proc.start("/opt/cloud/bin/passwd_server_ip %s >> /var/log/cloud.log 2>&1" % self.ip, "&")

    def stop(self):
        proc = CsProcess(["Password Service"])
        pid = proc.grep("passwd_server_ip.py %s" % self.ip)
        proc.kill(pid)
        pid = proc.grep("passwd_server_ip %s" % self.ip)
        proc.kill(pid)
        pid = proc.grep("8080,reuseaddr,fork,crnl,bind=%s" % self.ip)
        proc.kill(pid)

    def restart(self):
        self.stop()
        self.start()


class CsDnsmasq(CsApp):
    """ Set up dnsmasq """

    def add_firewall_rules(self):
        """ Add the necessary firewall rules
        """
        self.fw.append(["", "front",
                        "-A INPUT -i %s -p udp -m udp --dport 67 -j ACCEPT" % self.dev
                        ])

        self.fw.append(["", "front",
                        "-A INPUT -i %s -d %s/32 -p udp -m udp --dport 53 -j ACCEPT" % (self.dev, self.ip)
                        ])

        self.fw.append(["", "front",
                        "-A INPUT -i %s -d %s/32 -p tcp -m tcp --dport 53 -j ACCEPT" % (self.dev, self.ip)
                        ])
