import logging
import subprocess


class PasswordService(object):
    def __init__(self, config):
        self.config = config

    def sync(self):
        for interface in self.config.dbag_network_overview['interfaces']:
            if interface['metadata']['type'] in ['sync', 'other']:
                pass
            elif interface['metadata']['type'] == 'public':
                pass
            elif interface['metadata']['type'] == 'tier':
                self.start_password_service(interface['ipv4_addresses'])
            elif interface['metadata']['type'] == 'private':
                pass

    @staticmethod
    def start_password_service(listen_ip):

        try:
            subprocess.call(['systemctl', 'start', 'cosmic-password-server@%s' % listen_ip])
            return True
        except Exception as e:
            logging.error("Failed to start password service on %s due to error: %s" % (listen_ip, e))
            return False
