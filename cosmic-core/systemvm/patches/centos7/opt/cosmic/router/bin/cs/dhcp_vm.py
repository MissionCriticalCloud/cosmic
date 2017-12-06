import cs.utils as utils
import logging
import subprocess
from jinja2 import Environment, FileSystemLoader


#@TODO Old code used to delete leases file: still needed??


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
        self.delete_leases()
        self.restart_dnsmasq()

    def write_dnsmasq_dhcphosts(self):
        hosts = {}

        counter = 0
        if "vms" not in self.config.dbag_vm_overview:
            return
        for vm in self.config.dbag_vm_overview["vms"]:
            for interface in vm["interfaces"]:
                hosts[counter] = {
                    'ip_address': interface["ipv4_address"],
                    'mac_address': interface["mac_address"],
                    'tag': self.generate_dhcp_tag(interface["ipv4_address"]),
                    'hostname': vm['host_name']
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

        if "vms" not in self.config.dbag_vm_overview:
            return

        for vm in self.config.dbag_vm_overview["vms"]:
            for interface in vm["interfaces"]:
                host_entries[interface['ipv4_address']] = vm['host_name']

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

        if "vms" not in self.config.dbag_vm_overview:
            return

        for vm in self.config.dbag_vm_overview["vms"]:
            for interface in vm["interfaces"]:
                if interface["is_default"]:
                    continue
                # Add excludes on DHCP options when it's not the default NIC
                tags.append(self.generate_dhcp_tag(interface["ipv4_address"]))

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
    def delete_leases():
        try:
            open('/var/lib/misc/dnsmasq.leases', 'w').close()
        except IOError:
            return

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

