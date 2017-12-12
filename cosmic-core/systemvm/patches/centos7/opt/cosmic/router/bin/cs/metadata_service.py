import logging
import os
import subprocess
from jinja2 import Environment, FileSystemLoader


class MetadataService:

    def __init__(self, config):
        self.config = config

        self.jinja_env = Environment(
            loader=FileSystemLoader('/opt/cosmic/router/bin/cs/templates'),
            trim_blocks=True,
            lstrip_blocks=True
        )
        self.nginx_conf_path = '/etc/nginx/conf.d/'
        self.metadata_folder = '/var/www/html/metadata/'
        self.user_data_folder = '/var/www/html/userdata/'

        self.filenames = []

    def sync(self):
        self.create_data_folders()
        for interface in self.config.dbag_network_overview['interfaces']:
            if interface['metadata']['type'] == 'guesttier':
                self.setup(interface['ipv4_addresses'][0]['cidr'].split('/')[0])

        self.zap_nginx_config_directory()

        try:
            subprocess.call(['systemctl', 'start', 'nginx'])
        except Exception as e:
            logging.error("Failed to start nginx with error: %s" % e)

        try:
            subprocess.call(['systemctl', 'reload', 'nginx'])
        except Exception as e:
            logging.error("Failed to reload nginx with error: %s" % e)

    def create_data_folders(self):
        if not os.path.exists(self.metadata_folder):
            os.makedirs(self.metadata_folder)
        if not os.path.exists(self.user_data_folder):
            os.makedirs(self.user_data_folder)

    def setup(self, ip):
        content = self.jinja_env.get_template('vhost-metadata.conf').render(
            ipaddress=ip
        )

        filename = "vhost-%s.conf" % ip

        self.filenames.append(filename)

        with open(self.nginx_conf_path + filename, 'w') as f:
            f.write(content)

    def zap_nginx_config_directory(self):
        logging.debug("Zapping directory %s" % self.nginx_conf_path)
        for file_name in os.listdir(self.nginx_conf_path):
            if file_name in self.filenames:
                continue

            file_path = os.path.join(self.nginx_conf_path, file_name)
            try:
                if os.path.isfile(file_path):
                    logging.debug("Removing file %s" % file_path)
                    os.unlink(file_path)
            except Exception as e:
                logging.error("Failed to remove file: %s" % e)
