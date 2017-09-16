import logging
import os

from utils import Utils


def setup_iptable_rules():
    os.system("iptables-restore < /etc/iptables/iptables-consoleproxy")


class ConsoleProxyVM:
    def __init__(self, cmdline) -> None:
        super().__init__()

        self.cmdline = cmdline

        self.config_dir = "/etc/cosmic/agent/"

    def start(self):
        logging.info("Setting up configuration for %s" % self.cmdline["type"])
        self.setup_agent_config()
        setup_iptable_rules()

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
