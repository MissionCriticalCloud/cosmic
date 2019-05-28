import logging
import os

from utils import Utils


def setup_iptable_rules(cmdline):

    iptables_rules = """
*nat
:PREROUTING ACCEPT [0:0]
:POSTROUTING ACCEPT [0:0]
:OUTPUT ACCEPT [0:0]
COMMIT
*filter
:INPUT DROP [0:0]
:FORWARD DROP [0:0]
:OUTPUT ACCEPT [0:0]
-A INPUT -i lo  -j ACCEPT
-A INPUT -i %s -m state --state RELATED,ESTABLISHED -j ACCEPT
-A INPUT -i %s -m state --state RELATED,ESTABLISHED -j ACCEPT
-A INPUT -i %s -m state --state RELATED,ESTABLISHED -j ACCEPT
-A INPUT -p icmp --icmp-type 13 -j DROP
-A INPUT -p icmp -j ACCEPT
-A INPUT -i %s -p tcp -m state --state NEW -m tcp -s 169.254.0.1/32 --dport 3922 -j ACCEPT
-A INPUT -i %s -p tcp -m state --state NEW -m tcp --dport 8001 -j ACCEPT
-A INPUT -i %s -p tcp -m state --state NEW -m tcp --dport 8001 -j ACCEPT
-A INPUT -i %s -p tcp -m state --state NEW -m tcp --dport 443 -j ACCEPT
-A INPUT -i %s -p tcp -m state --state NEW -m tcp --dport 80 -j ACCEPT
COMMIT
""" % (
        cmdline['controlnic'],
        cmdline['mgtnic'],
        cmdline['publicnic'],
        cmdline['controlnic'],
        cmdline['controlnic'],
        cmdline['mgtnic'],
        cmdline['publicnic'],
        cmdline['publicnic']
    )

    with open("/tmp/iptables-consoleproxy", "w") as f:
        f.write(iptables_rules)

    os.system("iptables-restore < /tmp/iptables-consoleproxy")


class ConsoleProxyVM:
    def __init__(self, cmdline) -> None:
        super().__init__()

        self.cmdline = cmdline

        self.config_dir = "/etc/cosmic/agent/"

    def start(self):
        logging.info("Setting up configuration for %s" % self.cmdline["type"])
        self.setup_agent_config()
        setup_iptable_rules(self.cmdline)
        if self.cmdline['setrfc1918routes'] == 'true':
            logging.info("Setting rfc1918 routes")
            Utils(self.cmdline).set_rfc1918_routes()
        logging.info("Setting local routes")
        Utils(self.cmdline).set_local_routes()

        os.system("systemctl start cosmic-agent")

    def setup_agent_config(self):
        if not os.path.isdir(self.config_dir):
            os.makedirs(self.config_dir, 0o644, True)

        consoleproxy_properties = """
consoleproxy.tcpListenPort=0
consoleproxy.httpListenPort=80
consoleproxy.httpCmdListenPort=8001
consoleproxy.jarDir=./applet/
consoleproxy.viewerLinger=180
consoleproxy.reconnectMaxRetry=5
"""
        with open(self.config_dir + "consoleproxy.properties", "w") as f:
            f.write(consoleproxy_properties)

        Utils(self.cmdline).setup_agent_properties()
