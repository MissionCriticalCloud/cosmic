import logging
import os
import socket
import subprocess

from jinja2 import Environment, FileSystemLoader

import utils


class Keepalived(object):
    def __init__(self, config):
        self.config = config

        self.jinja_env = Environment(
            loader=FileSystemLoader('/opt/cosmic/router/bin/cs/templates'),
            trim_blocks=True,
            lstrip_blocks=True
        )
        # self.jinja_env = Environment(loader=FileSystemLoader('/Users/bschrijver/github.com/MissionCriticalCloud/cosmic/cosmic-core/systemvm/patches/centos7/opt/cosmic/router/bin/cs/templates'), trim_blocks=True, lstrip_blocks=True)
        self.keepalived_config_path = '/etc/keepalived/conf.d/'
        # self.keepalived_config_path = '/tmp/keep/'

        self.sync_group_name = 'cosmic'
        self.routes_vrrp_id = '254'

        self.filenames = []
        self.vrrp_instances = []

        self.vrrp_excluded_interface_types = ['sync', 'other']

    def sync(self):
        logging.info("Going to sync configuration for keepalived")
        self.init_config()
        self.write_global_defs()
        self.parse_vrrp_interface_instances()
        self.parse_vrrp_routes_instance()
        self.write_sync_group()
        self.zap_keepalived_config_directory()
        self.reload_keepalived()

    def init_config(self):
        if not os.path.exists(self.keepalived_config_path):
            os.makedirs(self.keepalived_config_path)

        self.write_keepalived_conf()

    def write_keepalived_conf(self):
        filepath = '/etc/keepalived/keepalived.conf'
        content = 'include /etc/keepalived/conf.d/*.conf'

        logging.debug("Writing keepalived config file %s with content \n%s" % (
            filepath, content
        ))

        with open(filepath, 'w') as f:
            f.write(content)

    def write_global_defs(self):
        content = self.jinja_env.get_template('keepalived_global_defs.conf').render(
            router_id=socket.gethostname()
        )

        self.write_keepalived_config('global_defs.conf', content)

    def parse_vrrp_interface_instances(self):
        sync_interface_name = self.config.get_sync_interface_name()

        for interface in self.config.dbag_network_overview['interfaces']:
            # Skip the certain networks
            if interface['metadata']['type'] in self.vrrp_excluded_interface_types:
                continue

            interface_name = utils.get_interface_name_from_mac_address(interface['mac_address'])
            interface_id = utils.get_interface_id_from_mac_address(interface['mac_address'])

            if interface_name is None or interface_id is None:
                continue

            name = '%s_%s' % (interface['metadata']['type'], interface_name)

            ipv4addresses = []
            for i in interface['ipv4_addresses']:
                ipv4addresses.append('%s dev %s' % (i['cidr'], interface_name))

            self.write_vrrp_instance(
                name=name,
                state='BACKUP',
                interface=sync_interface_name,
                virtual_router_id=interface_id,
                advert_int=self.config.get_advert_int(),
                virtual_ipaddress=[],
                virtual_ipaddress_excluded=ipv4addresses
            )

    def parse_vrrp_routes_instance(self):
        sync_interface_name = self.config.get_sync_interface_name()

        virtualroutes = []

        # Set the default route here until we handle it from the management server
        if 'source_nat' in self.config.dbag_network_overview['services'] and \
                self.config.dbag_network_overview['services']['source_nat']:
            virtualroutes.append(
                'default via %s' % self.config.dbag_network_overview['services']['source_nat'][0]['gateway']
            )

        for route in self.config.dbag_network_overview['routes']:
            virtualroutes.append('%s via %s metric %s' % (route['cidr'], route['next_hop'], route['metric']))

        self.write_vrrp_instance(
            name='zzz_routes',
            state='BACKUP',
            interface=sync_interface_name,
            virtual_router_id=self.routes_vrrp_id,
            advert_int=self.config.get_advert_int(),
            virtual_ipaddress=[],
            virtual_routes=virtualroutes
        )

    def write_vrrp_instance(
            self,
            name,
            state,
            interface,
            virtual_router_id,
            advert_int,
            virtual_ipaddress=None,
            virtual_ipaddress_excluded=None,
            virtual_routes=None
    ):
        content = self.jinja_env.get_template('keepalived_vrrp_instance.conf').render(
            name=name,
            state=state,
            interface=interface,
            virtual_router_id=virtual_router_id,
            advert_int=advert_int,
            virtual_ipaddress=virtual_ipaddress,
            virtual_ipaddress_excluded=virtual_ipaddress_excluded,
            virtual_routes=virtual_routes
        )

        self.vrrp_instances.append(name)

        filename = 'keepalived_vrrp_instance__%s__.conf' % name

        self.write_keepalived_config(filename, content)

    def write_sync_group(self):
        content = self.jinja_env.get_template('keepalived_sync_group.conf').render(
            sync_group_name=self.sync_group_name,
            vrrp_instances=self.vrrp_instances,
        )

        filename = 'keepalived_sync_group__%s__.conf' % self.sync_group_name

        self.write_keepalived_config(filename, content)

    def write_keepalived_config(self, filename, content):
        self.filenames.append(filename)

        logging.debug("Writing keepalived config file %s with content \n%s" % (
            self.keepalived_config_path + filename, content
        ))

        with open(self.keepalived_config_path + filename, 'w') as f:
            f.write(content)

    def zap_keepalived_config_directory(self):
        logging.debug("Zapping directory %s" % self.keepalived_config_path)
        for file_name in os.listdir(self.keepalived_config_path):
            if file_name in self.filenames:
                continue

            file_path = os.path.join(self.keepalived_config_path, file_name)
            try:
                if os.path.isfile(file_path):
                    logging.debug("Removing file %s" % file_path)
                    os.unlink(file_path)
            except Exception as e:
                logging.error("Failed to remove file: %s" % e)

    def reload_keepalived(self):
        logging.debug("Reloading keepalived with new config")

        try:
            subprocess.call(['systemctl', 'start', 'keepalived'])
        except Exception as e:
            logging.error("Failed to start keepalived with error: %s" % e)

        try:
            subprocess.call(['systemctl', 'reload', 'keepalived'])
        except Exception as e:
            logging.error("Failed to reload keepalived with error: %s" % e)
