import logging
import subprocess


class PasswordService:

    def __init__(self, config):
        self.config = config

    def sync(self):
        for interface in self.config.dbag_network_overview['interfaces']:
            if interface['metadata']['type'] == 'guesttier':
                self.start_password_service(interface['ipv4_addresses'][0]['cidr'])

    @staticmethod
    def start_password_service(cidr):
        listen_ip = cidr.split("/")[0]

        try:
            try:
                subprocess.call(['systemctl', 'start', 'cosmic-password-server@%s' % listen_ip])
            except Exception as e:
                logging.error("Failed to reload nginx with error: %s" % e)
            return True
        except Exception as e:
            logging.error('Failed to start password service on %s due to error: %s' % (listen_ip, e))
            return False
