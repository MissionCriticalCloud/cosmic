import logging
import os

from utils import Utils


def setup_html():
    html_dir = "/var/www/html/copy"
    if not os.path.isdir(html_dir):
        os.makedirs(html_dir, 0o755, True)


class SecondaryStorageVM:
    def __init__(self, cmdline) -> None:
        super().__init__()

        self.cmdline = cmdline

        self.config_dir = "/etc/cosmic/agent/"

    def start(self):
        logging.info("Setting up configuration for %s" % self.cmdline["type"])
        self.setup_agent_config()
        setup_html()

        os.system("systemctl start cosmic-agent")

    def setup_agent_config(self):
        Utils(self.cmdline).setup_agent_properties()
