from copy import deepcopy
import logging
import subprocess
from jinja2 import Environment, FileSystemLoader
import os
import re
import utils


class Vpn:

    def __init__(self, config):
        self.config = config

        self.jinja_env = Environment(
            loader=FileSystemLoader('/opt/cosmic/router/bin/cs/templates'),
            trim_blocks=True,
            lstrip_blocks=True
        )
        self.config_path_ipsec = '/etc/strongswan/ipsec.d/'
        self.xl2tpd_path = '/etc/xl2tpd/'
        self.stroke_path = '/etc/strongswan.d/charon/'
        self.ppp_path = '/etc/ppp/'
        self.filenames = []

    def sync(self):
        if 'vpn' in self.config.dbag_network_overview:
            if 'site2site' in self.config.dbag_network_overview['vpn']:
                self.write_ipsec_config()
                self.write_strokefile()
                self.cleanup_strongswan_config()
                self.reload_strongswan()
            if 'remote_access' in self.config.dbag_network_overview['vpn']:
                self.write_xl2tpd_config(self.config.dbag_network_overview['vpn']['remote_access'])
                self.write_l2tp(self.config.dbag_network_overview['vpn']['remote_access'])
                self.write_xl2tpd_options(self.config.dbag_network_overview['vpn']['remote_access'])
                self.write_remote_access_users(self.config.dbag_network_overview['vpn']['remote_access'])
                self.check_connected_users(self.config.dbag_network_overview['vpn']['remote_access'])
                self.write_remote_access_secret(self.config.dbag_network_overview['vpn']['remote_access'])
                self.write_strokefile()
                self.reload_strongswan()
            else:
                self.stop_remote_access()

    def cleanup_strongswan_config(self):
        logging.debug("Zapping directory %s" % self.config_path_ipsec)
        for file_name in os.listdir(self.config_path_ipsec):
            if file_name in self.filenames:
                continue

            file_path = os.path.join(self.config_path_ipsec, file_name)
            try:
                if os.path.isfile(file_path):
                    logging.debug("Removing file %s" % file_path)
                    os.unlink(file_path)
            except Exception as e:
                logging.error("Failed to remove file: %s" % e)

    def write_ipsec_config(self):
        for site2site in self.config.dbag_network_overview['vpn']['site2site']:
            config = deepcopy(site2site)
            filename = "ipsec.vpn-%s.conf" % config['right']
            self.filenames.append(filename)

            if 'dpd' in site2site and site2site['dpd']:
                config['dpddelay'] = 30
                config['dpdtimeout'] = 120
                config['dpdaction'] = "restart"

            config['ike'] = config['ike'].replace(';', '-')
            config['esp'] = config['esp'].replace(';', '-')
            config['forceencaps'] = utils.bool_to_yn(config['forceencaps'])
            peerlist = config['peerlist'].split(',')
            config.pop('peerlist', None)
            config.pop('dpd', None)
            config.pop('psk', None)
            config.pop('passive', None)

            content = self.jinja_env.get_template('ipsec_instance_config.conf').render(
                peerlist=peerlist,
                identifier=config['right'],
                site2site=config
            )
            logging.debug("Writing ipsec config file %s with content \n%s" % (self.config_path_ipsec + filename, content))

            with open(self.config_path_ipsec + filename, 'w') as f:
                f.write(content)

            self.write_secrets(site2site['left'], site2site['right'], site2site['psk'])
        self.reread_secrets()

    def write_secrets(self, left, right, psk):
        filename = "ipsec.vpn-%s.secrets" % right
        self.filenames.append(filename)
        content = self.jinja_env.get_template('ipsec_instance_secret.conf').render(psk=psk, left=left, right=right)
        logging.debug("Writing ipsec secrets file %s with content \n%s" % (self.config_path_ipsec + filename, content))

        with open(self.config_path_ipsec + filename, 'w') as f:
            f.write(content)
        os.chmod(self.config_path_ipsec + filename, 0o400)

    def write_strokefile(self):
        if not os.path.exists(self.stroke_path):
            os.makedirs(self.stroke_path)

        stroke_file = '%s/stroke.conf' % self.stroke_path
        content = self.jinja_env.get_template('stroke.conf').render(timeout=3000)
        logging.debug("Writing strongswan stroke file %s with content \n%s" % (self.stroke_path + stroke_file, content))

        with open(stroke_file, 'w') as f:
            f.write(content)

    def write_l2tp(self, remote_access):
        filename = 'l2tp.conf'
        self.filenames.append(filename)
        content = self.jinja_env.get_template('l2tp.conf').render(left=remote_access['local_ip'])
        logging.debug("Writing remote_access file %s with content \n%s" % (self.config_path_ipsec + filename, content))

        with open(self.config_path_ipsec + filename, 'w') as f:
            f.write(content)

    def write_xl2tpd_config(self, remote_access):
        filename = 'xl2tpd.conf'
        self.filenames.append(filename)
        content = self.jinja_env.get_template('xl2tpd.conf').render(
            ip_range=remote_access['ip_range'],
            local_ip=remote_access['local_ip']
        )
        logging.debug("Writing remote_access file %s with content \n%s" % (self.xl2tpd_path + filename, content))

        with open(self.xl2tpd_path + filename, 'w') as f:
            f.write(content)

    def write_remote_access_secret(self, remote_access):
        filename = 'ipsec.any.secrets'
        self.filenames.append(filename)
        content = self.jinja_env.get_template('ipsec.any.secrets.conf').render(psk=remote_access['preshared_key'])
        logging.debug("Writing remote_access file %s with content \n%s" % (self.config_path_ipsec + filename, content))

        with open(self.config_path_ipsec + filename, 'w') as f:
            f.write(content)

    def write_xl2tpd_options(self, remote_access):
        filename = 'options.xl2tpd'
        self.filenames.append(filename)
        content = self.jinja_env.get_template('options.xl2tpd.conf').render(local_ip=remote_access['local_ip'])
        logging.debug("Writing remote_access file %s with content \n%s" % (self.ppp_path + filename, content))

        with open(self.ppp_path + filename, 'w') as f:
            f.write(content)

    def write_remote_access_users(self, remote_access):
        filename = 'chap-secrets'
        self.filenames.append(filename)
        content = self.jinja_env.get_template('chap-secrets.conf').render(vpn_users=remote_access['vpn_users'])
        logging.debug("Writing remote_access file %s with content \n%s" % (self.ppp_path + filename, content))

        with open(self.ppp_path + filename, 'w') as f:
            f.write(content)

    @staticmethod
    def get_connected_users():
        if not os.path.exists('/var/run/pppd2.tdb'):
            return
        p = subprocess.Popen('/usr/bin/tdbdump /var/run/pppd2.tdb', stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                             shell=True)
        file_contents = p.communicate()[0].splitlines()
        pattern = r".*= \".*PPPD_PID=(.*);SPEED.*PEERNAME=(.*);IPLOCAL.*\""
        search = re.compile(pattern=pattern)
        connected_users = {}
        for line in file_contents:
            if 'data' in line:
                try:
                    pid = search.match(line).group(1)
                    username = search.match(line).group(2)
                    connected_users[pid] = username
                except:
                    pass
        return connected_users

    def check_connected_users(self, remote_access):
        logging.debug("Checking Remote Access VPN users")
        connected_users = self.get_connected_users()
        if connected_users is None:
            return
        for pid, username in connected_users.iteritems():
            user_active = False
            for vpn_user in remote_access['vpn_users']:
                if vpn_user == username:
                    user_active = True
            if not user_active:
                logging.debug("Deleting Remote Access user '%s'" % username)
                self.delete_remote_access_user_session(pid)

    @staticmethod
    def delete_remote_access_user_session(pid):
        os.system("kill -9 %s" % pid)

    @staticmethod
    def reload_strongswan():
        try:
            subprocess.call(['systemctl', 'start', 'strongswan'])
        except Exception as e:
            logging.error("Failed to start strongswan with error: %s" % e)

        try:
            subprocess.call(['strongswan', 'reload'])
        except Exception as e:
            logging.error("Failed to reload strongswan with error: %s" % e)

    @staticmethod
    def update_strongswan():
        try:
            subprocess.call(['strongswan', 'update'])
        except Exception as e:
            logging.error("Failed to update strongswan with error: %s" % e)

    @staticmethod
    def reread_secrets():
        try:
            subprocess.call(['strongswan', 'rereadsecrets'])
        except Exception as e:
            logging.error("Failed to rereadsecrets strongswan with error: %s" % e)

    @staticmethod
    def reload_xl2tpd():
        try:
            subprocess.call(['systemctl', 'start', 'xl2tpd'])
        except Exception as e:
            logging.error("Failed to start xl2tpd with error: %s" % e)

        try:
            subprocess.call(['strongswan', 'reload', 'xl2tpd'])
        except Exception as e:
            logging.error("Failed to reload xl2tpd with error: %s" % e)

    @staticmethod
    def stop_remote_access():
        try:
            subprocess.call(['strongswan', 'down', 'L2TP-PSK'])
        except Exception as e:
            logging.error("Failed to put down strongswan L2TP-PSK with error: %s" % e)

        try:
            subprocess.call(['systemctl', 'stop', 'xl2tpd'])
        except Exception as e:
            logging.error("Failed to stop xl2tpd with error: %s" % e)
