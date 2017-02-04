# -- coding: utf-8 --

import logging

import CsHelper


class CsRule:
    """ Manage iprules
    Supported Types:
    fwmark
    """

    def __init__(self, dev):
        self.dev = dev
        self.tableNo = int(dev[3:])
        self.table = "Table_%s" % (dev)

    def addRule(self, rule):
        if not self.findRule(rule + " lookup " + self.table):
            cmd = "ip rule add " + rule + " table " + self.table
            CsHelper.execute(cmd)
            logging.info("Added rule %s for %s" % (cmd, self.table))

    def findRule(self, rule):
        for i in CsHelper.execute("ip rule show"):
            if rule in i.strip():
                return True
        return False

    def addMark(self):
        if not self.findMark():
            cmd = "ip rule add fwmark %s table %s" % (self.tableNo, self.table)
            CsHelper.execute(cmd)
            logging.info("Added fwmark rule for %s" % (self.table))

    def delMark(self):
        if self.findMark():
            cmd = "ip rule delete fwmark %s table %s" % (self.tableNo, self.table)
            CsHelper.execute(cmd)
            logging.info("Deleting fwmark rule for %s" % (self.table))

    def findMark(self):
        srch = "from all fwmark %s lookup %s" % (hex(self.tableNo), self.table)
        for i in CsHelper.execute("ip rule show"):
            if srch in i.strip():
                return True
        return False
