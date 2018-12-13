import logging
import os
import socket
import subprocess

from jinja2 import Environment, FileSystemLoader
from netaddr import IPNetwork, IPAddress

import utils


class Keepalived:

    def __init__(self, config):
        self.config = config

        self.jinja_env = Environment(
            loader=FileSystemLoader('/opt/cosmic/router/bin/cs/templates'),
            trim_blocks=True,
            lstrip_blocks=True
        )
        self.keepalived_config_path = '/etc/keepalived/conf.d/'

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
        # Reload an extra time to make sure all routes are moved from old to new files
        # This can be removed once all pre-Cosmic 6.2 routers are replaced
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

            if self.config.get_advert_method() == "UNICAST":
                unicast_src, unicast_peer = utils.get_unicast_ips(self.config)
            else:
                unicast_src = None
                unicast_peer = None

            self.write_vrrp_instance(
                name=name,
                state='BACKUP',
                interface=sync_interface_name,
                virtual_router_id=interface_id,
                advert_int=self.config.get_advert_int(),
                virtual_ipaddress=[],
                virtual_ipaddress_excluded=ipv4addresses,
                network=interface,
                advert_method=self.config.get_advert_method(),
                virtual_routes=[],
                unicast_src=unicast_src,
                unicast_peer=unicast_peer
            )

    # The reason we write the default route to its own keepalived config file
    # is so that the group always has 2: public and the routes one.
    # Else if won't execute the scripts on state change.
    def parse_vrrp_routes_instance(self):
        sync_interface_name = self.config.get_sync_interface_name()
        virtual_routes = []
        # Set the default route here until we handle it from the management server
        if 'source_nat' in self.config.dbag_network_overview['services'] and \
                self.config.dbag_network_overview['services']['source_nat']:
            virtual_routes.append(
                'default via %s' % self.config.dbag_network_overview['services']['source_nat'][0]['gateway']
            )

        if self.config.get_advert_method() == "UNICAST":
            unicast_src, unicast_peer = utils.get_unicast_ips(self.config)
        else:
            unicast_src = None
            unicast_peer = None
        self.write_vrrp_instance(
            name='routes',
            state='BACKUP',
            interface=sync_interface_name,
            network={},
            virtual_router_id=self.routes_vrrp_id,
            advert_int=self.config.get_advert_int(),
            advert_method=self.config.get_advert_method(),
            virtual_routes=virtual_routes,
            unicast_src=unicast_src,
            unicast_peer=unicast_peer,
            virtual_ipaddress=[]
        )

    def write_vrrp_instance(
            self,
            name,
            state,
            interface,
            network,
            virtual_router_id,
            advert_int,
            advert_method,
            unicast_src,
            unicast_peer,
            virtual_routes=[],
            virtual_ipaddress=None,
            virtual_ipaddress_excluded=None
    ):

        # Generate virtual routes
        if len(network) > 0 and 'type' in network['metadata'] and network['metadata']['type'] in ("guesttier", "private", "public"):
            for route in self.config.dbag_network_overview['routes']:
                if IPAddress(route['next_hop']) in IPNetwork(network['ipv4_addresses'][0]['cidr']):
                    logging.debug("Adding route to %s for %s because part of cidr %s" % (route['next_hop'], interface, network['ipv4_addresses'][0]['cidr']))
                    virtual_routes.append('%s via %s metric %s' % (route['cidr'], route['next_hop'], route['metric']))
                else:
                    logging.debug("Skipping route %s for %s because not part of cidr %s" % (route['next_hop'], interface, network['ipv4_addresses'][0]['cidr']))
            # Set the default route here too so it is linked to the public interface
            if network['metadata']['type'] == 'public' and 'source_nat' in self.config.dbag_network_overview['services'] and \
                    self.config.dbag_network_overview['services']['source_nat']:
                virtual_routes.append(
                    'default via %s' % self.config.dbag_network_overview['services']['source_nat'][0]['gateway']
                )
        else:
            logging.debug("Skipping because network is empty. Network: %s" % network)

        content = self.jinja_env.get_template('keepalived_vrrp_instance.conf').render(
            name=name,
            state=state,
            interface=interface,
            virtual_router_id=virtual_router_id,
            advert_int=advert_int,
            virtual_ipaddress=virtual_ipaddress,
            virtual_ipaddress_excluded=virtual_ipaddress_excluded,
            virtual_routes=virtual_routes,
            advert_method=advert_method,
            unicast_src=unicast_src,
            unicast_peer=unicast_peer
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
