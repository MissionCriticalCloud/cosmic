import logging
import os

from utils import Utils


def setup_html():
    html_dir = "/var/www/html/copy"
    if not os.path.isdir(html_dir):
        os.makedirs(html_dir, 0o755, True)


def setup_iptable_rules():
    os.system("iptables-restore < /etc/iptables/iptables-secstorage")


class SecondaryStorageVM:
    def __init__(self, cmdline) -> None:
        super().__init__()

        self.cmdline = cmdline

        self.config_dir = "/etc/cosmic/agent/"

    def start(self):
        logging.info("Setting up configuration for %s" % self.cmdline["type"])
        self.setup_agent_config()
        setup_html()
        setup_iptable_rules()
        self.setup_nginx()
        Utils(self.cmdline).set_rfc1918_routes()

        os.system("systemctl start cosmic-agent")

    def setup_agent_config(self):
        Utils(self.cmdline).setup_agent_properties()

    def setup_nginx(self):
        if not os.path.isdir("/var/www/html/userdata"):
            os.makedirs("/var/www/html/userdata", 0o755, True)

        vhost = """
server {
    listen       %s:80;
    listen       %s:443 ssl;
    server_name  _;
    root         /var/www/html;

    autoindex off;

    location /userdata {
        autoindex off;
    }
}
""" % (self.cmdline["eth2ip"], self.cmdline["eth2ip"])

        filename = "/etc/nginx/conf.d/vhost-%s.conf" % (self.cmdline["eth2ip"])

        with open(filename, 'w') as f:
            f.write(vhost)

        os.system("systemctl start nginx")
        os.system("systemctl reload nginx")
