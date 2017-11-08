import logging

import os

import CsHelper
from CsFile import CsFile


class CsVpnUser(object):
    def __init__(self, config):
        self.config = config
        self.dbag = config.dbag_vpnuserlist

    PPP_CHAP = '/etc/ppp/chap-secrets'

    def process(self):
        for user in self.dbag:
            if user == 'id':
                continue

            userconfig = self.dbag[user]
            if userconfig['add']:
                self.add_l2tp_ipsec_user(user, userconfig)
            else:
                self.del_l2tp_ipsec_user(user, userconfig)

    def add_l2tp_ipsec_user(self, user, obj):
        userfound = False
        password = obj['password']

        userSearchEntry = "%s \* %s \*" % (user, password)
        userAddEntry = "%s * %s *" % (user, password)
        logging.debug("Adding vpn user %s" % userSearchEntry)

        file = CsFile(self.PPP_CHAP)
        userfound = file.searchString(userSearchEntry, '#')
        if not userfound:
            logging.debug("User is not there already, so adding user ")
            self.del_l2tp_ipsec_user(user, obj)
            file.add(userAddEntry)
        file.commit()

    def del_l2tp_ipsec_user(self, user, obj):
        userfound = False
        password = obj['password']
        userentry = "%s \* %s \*" % (user, password)

        logging.debug("Deleting the user %s " % user)
        file = CsFile(self.PPP_CHAP)
        file.deleteLine(userentry)
        file.commit()

        if not os.path.exists('/var/run/pppd2.tdb'):
            return

        logging.debug("kiing the PPPD process for the user %s " % user)

        fileContents = CsHelper.execute("tdbdump /var/run/pppd2.tdb")
        print(fileContents)

        for line in fileContents:
            if user in line:
                contentlist = line.split(';')
                for str in contentlist:
                    print('in del_l2tp str = ' + str)
                    pppd = str.split('=')[0]
                    if pppd == 'PPPD_PID':
                        pid = str.split('=')[1]
                        if pid:
                            logging.debug("killing process %s" % pid)
                            CsHelper.execute('kill -9 %s' % pid)
