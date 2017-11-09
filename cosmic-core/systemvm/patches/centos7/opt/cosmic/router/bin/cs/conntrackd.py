import logging
import subprocess

from jinja2 import Environment, FileSystemLoader


class Conntrackd(object):
    def __init__(self, config):
        self.config = config

        self.jinja_env = Environment(
            loader=FileSystemLoader('/opt/cosmic/router/bin/cs/templates'),
            trim_blocks=True,
            lstrip_blocks=True
        )
        self.conntrackd_config_file = '/etc/conntrackd/conntrackd.conf'

        self.conntrackd_ipv6_multicast_address = 'ff12::50'

    def sync(self):
        self.write_config_file()
        self.restart_conntrackd()

    def write_config_file(self):
        content = self.jinja_env.get_template('keepalived_sync_group.conf').render(
            ipv6_multicast_address=self.conntrackd_ipv6_multicast_address,
            sync_interface=self.config.get_sync_interface_name(),
            address_ignore=self.config.get_all_ipv4_addresses_on_router()
        )

        logging.debug("Writing keepalived config file %s with content \n%s" % (
            self.conntrackd_config_file, content
        ))

        with open(self.conntrackd_config_file, 'w') as f:
            f.write(content)

    def restart_conntrackd(self):
        logging.debug("Restarting conntrackd with new config")

        try:
            subprocess.call(['systemctl', 'start', 'conntrackd'])
        except Exception as e:
            logging.error("Failed to start conntrackd with error: %s" % e)

        try:
            subprocess.call(['systemctl', 'restart', 'conntrackd'])
        except Exception as e:
            logging.error("Failed to restart conntrackd with error: %s" % e)
