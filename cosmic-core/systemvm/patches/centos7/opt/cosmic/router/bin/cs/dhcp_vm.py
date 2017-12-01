import cs.utils as utils
import logging
import subprocess
from jinja2 import Environment, FileSystemLoader


class DhcpVm:

    def __init__(self, config):
        self.config = config

        self.jinja_env = Environment(
            loader=FileSystemLoader('/opt/cosmic/router/bin/cs/templates'),
            trim_blocks=True,
            lstrip_blocks=True
        )
        self.config_path = '/etc/'

    def sync(self):
        self.write_dnsmasq_dhcphosts()
        self.write_dnsmasq_dhcpopts()
        self.write_hosts()
        self.restart_dnsmasq()

    def write_dnsmasq_dhcphosts(self):
        hosts = {}

        counter = 0
        for data in self.config.dbag_dhcpentry:
            if data == 'id':
                continue
            dhcp_host = self.config.dbag_dhcpentry[data]
            hosts[counter] = {
                'ip_address': dhcp_host['ipv4_adress'],
                'mac_address': dhcp_host['mac_address'],
                'tag': self.generate_dhcp_tag(dhcp_host['ipv4_adress']),
                'hostname': dhcp_host['host_name']
            }
            counter += 1

        filename = 'dhcphosts.txt'
        content = self.jinja_env.get_template('etc_dhcphosts.conf').render(
            hosts=hosts
        )

        logging.debug("Writing dhcphosts config file %s with content \n%s" % (
            self.config_path + filename, content
        ))

        with open(self.config_path + filename, 'w') as f:
            f.write(content)

    def write_hosts(self):

        host_entries = {}

        for data in self.config.dbag_dhcpentry:
            if data == 'id':
                continue
            dhcp_host = self.config.dbag_dhcpentry[data]
            host_entries[dhcp_host['ipv4_adress']] = dhcp_host['host_name']

        content = self.jinja_env.get_template('etc_hosts.conf').render(
            host_entries=host_entries
        )

        filename = 'hosts'

        logging.debug("Writing hosts config file %s with content \n%s" % (
            self.config_path + filename, content
        ))

        with open(self.config_path + filename, 'w') as f:
            f.write(content)

    def write_dnsmasq_dhcpopts(self):

        tags = []

        for data in self.config.dbag_dhcpentry:
            dhcp_host = self.config.dbag_dhcpentry[data]
            if data == 'id':
                continue
            if 'default_gateway' in dhcp_host:
                continue
            tags.append(self.generate_dhcp_tag(dhcp_host['ipv4_adress']))

        filename = 'dhcpopts.txt'
        content = self.jinja_env.get_template('etc_dhcpopts.conf').render(
            tags=tags
        )

        logging.debug("Writing dhcpopts config file %s with content \n%s" % (
            self.config_path + filename, content
        ))

        with open(self.config_path + filename, 'w') as f:
            f.write(content)

    @staticmethod
    def generate_dhcp_tag(ip):
        return ip.replace('.', '_')

    @staticmethod
    def restart_dnsmasq():
        logging.debug("Reloading dnsmasq with new config")

        try:
            subprocess.call(['systemctl', 'restart', 'dnsmasq'])
        except Exception as e:
            logging.error("Failed to reload dnsmasq with error: %s" % e)

