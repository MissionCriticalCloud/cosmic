import base64
import logging
import os
import subprocess
import sys
from fcntl import flock, LOCK_EX, LOCK_UN

from jinja2 import Environment, FileSystemLoader

import CsHelper


class MetadataService(object):
    def __init__(self, config):
        self.config = config

        self.jinja_env = Environment(
            loader=FileSystemLoader('/opt/cosmic/router/bin/cs/templates'),
            trim_blocks=True,
            lstrip_blocks=True
        )

        self.nginx_conf_path = '/etc/nginx/conf.d/'

        self.filenames = []

    def sync(self):
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


class CsMetadataServiceVMConfig(object):
    def __init__(self, config):
        self.config = config
        self.dbag = self.config.dbag_vmdata

    def process(self):
        for ip in self.dbag:
            if "id" == ip:
                continue
            logging.info("Processing metadata for %s" % ip)
            for item in self.dbag[ip]:
                folder = item[0]
                file = item[1]
                data = item[2]

                # process only valid data
                if folder != "userdata" and folder != "metadata":
                    continue

                if file == "":
                    continue

                if data == "":
                    self.__deletefile(ip, folder, file)
                else:
                    self.__createfile(ip, folder, file, data)

    def __deletefile(self, ip, folder, file):
        datafile = "/var/www/html/" + folder + "/" + ip + "/" + file

        if os.path.exists(datafile):
            os.remove(datafile)

    def __createfile(self, ip, folder, file, data):
        dest = "/var/www/html/" + folder + "/" + ip + "/" + file
        metamanifestdir = "/var/www/html/" + folder + "/" + ip
        metamanifest = metamanifestdir + "/meta-data"

        if not os.path.isdir(metamanifestdir):
            CsHelper.mkdir(metamanifestdir, 0o755, False)

        # base64 decode userdata
        if folder == "userdata" or folder == "user-data":
            if data is not None:
                data = base64.b64decode(data)

        fh = open(dest, "w")
        self.__exflock(fh)
        if data is not None:
            fh.write(data)
        else:
            fh.write("")
        self.__unflock(fh)
        fh.close()
        os.chmod(dest, 0o644)

        if folder == "metadata" or folder == "meta-data":
            try:
                os.makedirs(metamanifestdir, 0o755)
            except OSError as e:
                # error 17 is already exists, we do it this way for concurrency
                if e.errno != 17:
                    print("failed to make directories " + metamanifestdir + " due to :" + e.strerror)
                    sys.exit(1)
            if os.path.exists(metamanifest):
                fh = open(metamanifest, "r+a")
                self.__exflock(fh)
                if file not in fh.read():
                    fh.write(file + '\n')
                self.__unflock(fh)
                fh.close()
            else:
                fh = open(metamanifest, "w")
                self.__exflock(fh)
                fh.write(file + '\n')
                self.__unflock(fh)
                fh.close()

        if os.path.exists(metamanifest):
            os.chmod(metamanifest, 0o644)

    def __exflock(self, file):
        try:
            flock(file, LOCK_EX)
        except IOError as e:
            print("failed to lock file" + file.name + " due to : " + e.strerror)
            sys.exit(1)  # FIXME
        return True

    def __unflock(self, file):
        try:
            flock(file, LOCK_UN)
        except IOError as e:
            print("failed to unlock file" + file.name + " due to : " + e.strerror)
            sys.exit(1)  # FIXME
        return True
