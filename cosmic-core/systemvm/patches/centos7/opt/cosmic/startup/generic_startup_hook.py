#!/usr/bin/python3.6
import json
import logging
import os
import sys
import time
import glob

from setup_cpvm import ConsoleProxyVM
from setup_routervm import RouterVM
from setup_ssvm import SecondaryStorageVM
from utils import Utils

CMDLINE_DIR = "/var/cache/cloud/"
CMDLINE_FILE = "cmdline"
CMDLINE_DONE = "cmdline_incoming"
CMDLINE_JSON = "cmd_line.json"

AGENT_PROPERTIES = "/etc/cosmic/agent/agent.properties"

LOG_DIR = "/var/log/cosmic/startup/"


def wait_for_cmdline():
    logging.info("Waiting for cmdline to arrive")

    while not os.path.exists(CMDLINE_DIR + CMDLINE_DONE):
        time.sleep(1)


class App:
    def __init__(self) -> None:
        super().__init__()

        self.cmdline = {}

    def create_cmdline_json_from_properties(self):
        self.cmdline = {}

        with open(AGENT_PROPERTIES, "r") as f:
            for item in f:
                key = item.split("=")[0].strip()
                value = item.split("=")[1].strip()

                self.cmdline[key] = value

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

        logging.info("Cmd_line json: %s" % cmdline_json)

        try:
            os.remove(CMDLINE_DIR + CMDLINE_FILE)
            os.remove(CMDLINE_DIR + CMDLINE_DONE)
            os.system("sync")
        except OSError as e: # name the Exception `e`
            print "Failed with:", e.strerror # look what it says
            print "Error code:", e.code

        self.cmdline = cmdline_json["cmd_line"]

    def start_app(self):
        Utils(self.cmdline).bootstrap()

        if self.cmdline["type"] == "secstorage":
            logging.info("Starting app %s" % self.cmdline["type"])

            SecondaryStorageVM(self.cmdline).start()

        elif self.cmdline["type"] in ("vpcrouter", "router"):
            logging.info("Starting app %s" % self.cmdline["type"])

            RouterVM(self.cmdline).start()

        elif self.cmdline["type"] == "consoleproxy":
            logging.info("Starting app %s" % self.cmdline["type"])

            ConsoleProxyVM(self.cmdline).start()

        else:
            logging.error("Unknown type %s" % self.cmdline["type"])
            sys.exit(1)


def full_start(application):
    wait_for_cmdline()

    application.write_cmdline_json()

    application.start_app()


def reboot_start(application):
    application.create_cmdline_json_from_properties()

    application.start_app()


if __name__ == "__main__":
    if not os.path.isdir(LOG_DIR):
        os.makedirs(LOG_DIR, 0o755, True)

    if not os.path.isdir(CMDLINE_DIR):
        os.makedirs(CMDLINE_DIR, 0o755, True)

    logging.basicConfig(level=logging.DEBUG, format='%(asctime)s  %(filename)s %(funcName)s:%(lineno)d %(message)s')

    app = App()

    if os.path.exists(AGENT_PROPERTIES):
        with open(AGENT_PROPERTIES, "r") as f:
            for line in f:
                if 'secstorage' in line:
                    reboot_start(app)
                    exit(0)
                elif 'consoleproxy' in line:
                    reboot_start(app)
                    exit(0)
            full_start(app)
    else:
        full_start(app)
