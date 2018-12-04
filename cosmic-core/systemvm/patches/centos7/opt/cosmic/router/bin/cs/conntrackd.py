import logging
import subprocess
import utils

from jinja2 import Environment, FileSystemLoader


class Conntrackd:

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
        address_ignore = []

        for ipv4 in self.config.get_all_ipv4_addresses_on_router():
            address_ignore.append('IPv4_address %s' % ipv4)

        unicast_src, unicast_peer = utils.get_unicast_ips(self.config)

        content = self.jinja_env.get_template('conntrackd.conf').render(
            ipv6_multicast_address=self.conntrackd_ipv6_multicast_address,
            sync_interface=self.config.get_sync_interface_name(),
            address_ignore=address_ignore,
            advert_method=self.config.get_advert_method(),
            unicast_src=unicast_src,
            unicast_peer=unicast_peer
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
