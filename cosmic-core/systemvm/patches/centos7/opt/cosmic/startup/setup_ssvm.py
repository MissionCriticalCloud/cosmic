import logging
import os

from utils import Utils


class SecondaryStorageVM:
    def __init__(self, cmdline) -> None:
        super().__init__()

        self.cmdline = cmdline

        self.config_dir = "/etc/cosmic/agent/"

    def start(self):
        logging.info("Setting up configuration for %s" % self.cmdline["type"])
        self.setup_agent_config()

        os.system("systemctl start cosmic-agent")

    def setup_agent_config(self):
        Utils(self.cmdline).setup_agent_properties()
