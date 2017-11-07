import logging
import time

import CsHelper
from CsProcess import CsProcess


class CsPasswordService:
    def __init__(self, ip):
        self.ip = ip

    def start(self):
        CsHelper.service("cosmic-password-server@%s" % self.ip, "start")

    def stop(self):
        CsHelper.service("cosmic-password-server@%s" % self.ip, "stop")

    def restart(self):
        CsHelper.service("cosmic-password-server@%s" % self.ip, "restart")


class CsPasswordServiceVMConfig:
    TOKEN_FILE = "/tmp/passwdsrvrtoken"

    def __init__(self, dbag):
        self.dbag = dbag
        self.process()

    def process(self):
        self.__update(self.dbag['ip_address'], self.dbag['password'])

    def __update(self, vm_ip, password):
        token = ""
        try:
            token_file = open(self.TOKEN_FILE)
            token = token_file.read()
        except IOError:
            logging.debug("File %s does not exist" % self.TOKEN_FILE)

        logging.debug("Got VM '%s' and password '%s'" % (vm_ip, password))
        get_cidrs_cmd = "ip addr show | grep inet | grep -v secondary | awk '{print $2}'"
        cidrs = CsHelper.execute(get_cidrs_cmd)
        logging.debug("Found these CIDRs: %s" % cidrs)
        for cidr in cidrs:
            logging.debug("Processing CIDR '%s'" % cidr)
            if CsHelper.IPAddress(vm_ip) in CsHelper.IPNetwork(cidr):
                ip = cidr.split('/')[0]
                logging.debug(
                    "Cidr %s matches vm ip address %s so adding passwd to passwd server at %s" % (cidr, vm_ip, ip))
                proc = CsProcess(['/opt/cosmic/router/bin/passwd_server_ip.py', ip])

                max_tries = 5
                test_tries = 0
                while test_tries < max_tries:
                    logging.debug("Updating passwd server on %s" % ip)
                    if proc.find():
                        update_command = 'curl --header "DomU_Request: save_password" "http://{SERVER_IP}:8080/" -F "ip=' \
                                         '{VM_IP}" -F "password={PASSWORD}" -F "token={TOKEN}" --interface 127.0.0.1 ' \
                                         '>/dev/null 2>/dev/null &'.format(SERVER_IP=ip, VM_IP=vm_ip, PASSWORD=password,
                                                                           TOKEN=token)
                        result = CsHelper.execute(update_command)
                        logging.debug("Update password server result ==> %s" % result)
                        return result

                    test_tries += 1
                    logging.debug("Testing password server process round %s/%s" % (test_tries, max_tries))
                    time.sleep(2)

                logging.debug("Update password server skipped because we didn't find a passwd server process for "
                              "%s (makes sense on backup routers)" % ip)
