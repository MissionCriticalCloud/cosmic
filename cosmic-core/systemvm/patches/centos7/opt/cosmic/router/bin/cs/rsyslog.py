import logging
import subprocess

from jinja2 import Environment, FileSystemLoader

class Rsyslog:

    def __init__(self, config):
        self.config = config

        self.jinja_env = Environment(
            loader=FileSystemLoader('/opt/cosmic/router/bin/cs/templates'),
            trim_blocks=True,
            lstrip_blocks=True
        )
        self.rsyslogd_config_path = '/etc/rsyslog.d/'
        self.syslogservers = []

    def sync(self):
        logging.info("Going to sync configuration for rsyslogd")
        self.init_config()
        self.write_rsyslog_config()
        self.restart_rsyslog()

    def init_config(self):
        syslogservers = self.config.dbag_network_overview.get('syslog', {}).get('servers', {})
        for syslogserver in syslogservers:
            proto = "UDP"
            args = syslogserver.split(':')
            if len(args) < 2:
                # Only got an IP
                ip = args.pop()
                port = 514
                proto = "TCP"
            elif len(args) == 2:
                # Got an IP and port
                ip, port = args
            elif len(args) == 3:
                # Got an IP, port and protocol
                ip, port, proto = args
            elif len(args) > 3:
                # We got to much garbage, report and ignore
                logging.error("Error in parsing syslog server list: %s" % ip)
                continue
            if proto.lower() == "udp":
                proto = "@"
            else:
                proto = "@@"
            self.syslogservers.append({"ip": ip, "port": port, "proto": proto})

    def write_rsyslog_config(self):
        filename = '00-iptables.conf'
        content = self.jinja_env.get_template('rsyslog.conf').render(syslogservers=self.syslogservers)
        logging.debug("Writing rsyslogd config file %s with content \n%s" % (
            self.rsyslogd_config_path + filename, content
        ))
        with open(self.rsyslogd_config_path + filename, 'w') as f:
            f.write(content)

    @staticmethod
    def restart_rsyslog():
        try:
            subprocess.call(['systemctl', 'restart', 'rsyslog'])
        except Exception as e:
            logging.error("Failed to reload rsyslogd with error: %s" % e)