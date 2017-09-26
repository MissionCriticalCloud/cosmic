#!/usr/bin/python
import logging
import os

from utils import Utils


class SecondaryStorageVM:
    def __init__(self, cmdline) -> None:
        super().__init__()

        self.cmdline = cmdline

        self.config_dir = "/etc/cosmic/agent/"

    def start(self):
        logging.info("Running update_config.py on %s" % self.cmdline["name"])
        os.system("/opt/cosmic/router/bin/update_config.py cmd_line.json")

    def setup_agent_config(self):
        Utils(self.cmdline).setup_agent_properties()
