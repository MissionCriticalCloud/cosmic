#!/usr/bin/python3.6
import json
import logging
import os
import sys
import time

from setup_cpvm import ConsoleProxyVM
from setup_routervm import RouterVM
from setup_ssvm import SecondaryStorageVM
from utils import Utils

CMDLINE_DIR = "/var/cache/cloud/"
CMDLINE_FILE = "cmdline"
CMDLINE_DONE = "cmdline_incoming"
CMDLINE_JSON = "cmd_line.json"

LOG_DIR = "/var/log/cosmic/startup/"


def wait_for_cmdline():
    logging.info("Waiting for cmdline to arrive")

    while not os.path.exists(CMDLINE_DIR + CMDLINE_DONE):
        time.sleep(1)


class App:
    def __init__(self) -> None:
        super().__init__()

        self.cmdline = {}

    def write_cmdline_json(self):
        logging.info("Writing for cmd_line.json")

        cmdline_json = {
            "type": "cmdline",
            "cmd_line": {}
        }

        with open(CMDLINE_DIR + CMDLINE_FILE, "r") as f:
            for item in f.readline().split():
                key = item.split("=")[0]
                value = item.split("=")[1]

                cmdline_json["cmd_line"][key] = value

        json.dump(cmdline_json, open(CMDLINE_DIR + CMDLINE_JSON, "w"))

        self.cmdline = cmdline_json["cmd_line"]

    def start_app(self):
        Utils(self.cmdline).bootstrap()

        if self.cmdline["type"] == "secstorage":
            logging.info("Starting app %s" % self.cmdline["type"])

            SecondaryStorageVM(self.cmdline).start()

        elif self.cmdline["type"] == "vpcrouter":
            logging.info("Starting app %s" % self.cmdline["type"])

            RouterVM(self.cmdline).start()

        elif self.cmdline["type"] == "consoleproxy":
            logging.info("Starting app %s" % self.cmdline["type"])

            ConsoleProxyVM(self.cmdline).start()

        else:
            logging.error("Unknown type %s" % self.cmdline["type"])
            sys.exit(1)


if __name__ == "__main__":
    if not os.path.isdir(LOG_DIR):
        os.makedirs(LOG_DIR, 0o755, True)

    if not os.path.isdir(CMDLINE_DIR):
        os.makedirs(CMDLINE_DIR, 0o755, True)

    logging.basicConfig(filename=LOG_DIR + "startup.log", level=logging.DEBUG)

    app = App()

    wait_for_cmdline()

    app.write_cmdline_json()

    app.start_app()
