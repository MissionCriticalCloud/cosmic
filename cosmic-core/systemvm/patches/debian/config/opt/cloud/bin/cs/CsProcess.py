# -- coding: utf-8 --

import logging
import os
import re

import CsHelper


class CsProcess(object):
    """ Manipulate processes """

    def __init__(self, search):
        self.search = search

    def start(self, thru, background=''):
        # if(background):
        #     cmd = cmd + " &"
        logging.info("Started %s", " ".join(self.search))
        os.system("%s %s %s" % (thru, " ".join(self.search), background))

    def kill_all(self):
        pids = self.find_pid()
        for p in pids:
            CsHelper.execute("kill -9 %s" % p)

    def find_pid(self):
        self.pid = []
        for i in CsHelper.execute("ps aux"):
            items = len(self.search)
            proc = re.split("\s+", i)[items * -1:]
            matches = len([m for m in proc if m in self.search])
            if matches == items:
                self.pid.append(re.split("\s+", i)[1])

        logging.debug("CsProcess:: Searching for process ==> %s and found PIDs ==> %s", self.search, self.pid)
        return self.pid

    def find(self):
        has_pid = len(self.find_pid()) > 0
        return has_pid

    def kill(self, pid):
        if pid > 1:
            CsHelper.execute("kill -9 %s" % pid)

    def grep(self, str):
        for i in CsHelper.execute("ps aux"):
            if i.find(str) != -1:
                return re.split("\s+", i)[1]
        return -1
