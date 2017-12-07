import cs.utils as utils
import logging
import os
import subprocess
from jinja2 import Environment, FileSystemLoader
from netaddr import IPNetwork


class DhcpService:

    def __init__(self, config):
        self.config = config

        self.jinja_env = Environment(
            loader=FileSystemLoader('/opt/cosmic/router/bin/cs/templates'),
            trim_blocks=True,
            lstrip_blocks=True
        )
        self.dnsmasq_config_path = '/etc/dnsmasq.d/'

    def sync(self):
        self.cleanup_dnsmasq_config_dir()
        self.write_dnsmasq_global_config()
        for interface in self.config.dbag_network_overview['interfaces']:
            if interface['metadata']['type'] == 'guesttier':
                self.write_interface_dnsmasq_config(interface)
        self.restart_dnsmasq()

    def cleanup_dnsmasq_config_dir(self):
        for config_filename in os.listdir(self.dnsmasq_config_path):
            config_file = os.path.join(self.dnsmasq_config_path, config_filename)
            try:
                if os.path.isfile(config_file):
                    os.remove(config_file)
            except Exception as e:
                logging.error("Failed to cleanup dnsmasq config directory with error: %s" % e)

    def write_dnsmasq_global_config(self):
        interfaces = []
        for interface in self.config.dbag_network_overview['interfaces']:
            if interface['metadata']['type'] != 'guesttier':
                interface_name = utils.get_interface_name_from_mac_address(interface['mac_address'])
                interfaces.append(interface_name)

        filename = 'dnsmasq_global.conf'
        content = self.jinja_env.get_template('dnsmasq_global.conf').render(
            interfaces=interfaces
        )

        logging.debug("Writing dnsmasq config file %s with content \n%s" % (
            self.dnsmasq_config_path + filename, content
        ))

        with open(self.dnsmasq_config_path + filename, 'w') as f:
            f.write(content)

    def write_interface_dnsmasq_config(self, interface):

        interface_name = utils.get_interface_name_from_mac_address(interface['mac_address'])
        ip = next(iter(interface['ipv4_addresses'] or []), None)
        network = IPNetwork(ip['cidr'])

        dns_servers = [interface['metadata']['dns1']]
        if 'dns2' in interface['metadata']:
            dns_servers.append(interface['metadata']['dns2'])

        content = self.jinja_env.get_template('dnsmasq_interface.conf').render(
            interface_name=interface_name,
            gateway=ip['gateway'],
            netmask=network.netmask,
            dns_servers=dns_servers,
            domain_name=interface['metadata']['domain_name']
        )

        filename = '{}.conf'.format(interface_name)

        logging.debug("Writing dnsmasq config file %s with content \n%s" % (
            self.dnsmasq_config_path + filename, content
        ))

        with open(self.dnsmasq_config_path + filename, 'w') as f:
            f.write(content)


    def restart_dnsmasq(self):
        logging.debug("Reloading dnsmasq with new config")

        try:
            subprocess.call(['systemctl', 'start', 'dnsmasq'])
        except Exception as e:
            logging.error("Failed to start dnsmasq with error: %s" % e)

        try:
            subprocess.call(['systemctl', 'restart', 'dnsmasq'])
        except Exception as e:
            logging.error("Failed to reload dnsmasq with error: %s" % e)

