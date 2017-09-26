#!/usr/bin/python
import logging
import os


class RouterVM:
    def __init__(self, cmdline) -> None:
        super().__init__()

        self.cmdline = cmdline

    def start(self):
        logging.info("Running update_config.py on %s" % self.cmdline["name"])
        os.system("/opt/cosmic/router/bin/update_config.py cmd_line.json")
