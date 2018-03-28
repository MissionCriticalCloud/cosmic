import logging
import os

from utils import Utils


def setup_html():
    html_dir = "/var/www/html/copy"
    if not os.path.isdir(html_dir):
        os.makedirs(html_dir, 0o755, True)


def setup_iptable_rules(cmdline):

    iptables_rules = """
*nat
:PREROUTING ACCEPT [0:0]
:POSTROUTING ACCEPT [0:0]
:OUTPUT ACCEPT [0:0]
COMMIT
*filter
:INPUT DROP [0:0]
:FORWARD DROP [0:0]
:OUTPUT ACCEPT [0:0]
:HTTP - [0:0]
-A INPUT -i %s -m state --state RELATED,ESTABLISHED -j ACCEPT
-A INPUT -i %s -m state --state RELATED,ESTABLISHED -j ACCEPT
-A INPUT -i %s -m state --state RELATED,ESTABLISHED -j ACCEPT
-A INPUT -i lo  -j ACCEPT
-A INPUT -p icmp --icmp-type 13 -j DROP
-A INPUT -p icmp -j ACCEPT
-A INPUT -i %s -p tcp -m state --state NEW -s 169.254.0.1/32 --dport 3922 -j ACCEPT
-A OUTPUT -o %s -p tcp -m state --state NEW -m tcp --dport 80 -j REJECT
-A OUTPUT -o %s -p tcp -m state --state NEW -m tcp --dport 443 -j REJECT
COMMIT
""" % (
        cmdline['controlnic'],
        cmdline['mgtnic'],
        cmdline['publicnic'],
        cmdline['controlnic'],
        cmdline['mgtnic'],
        cmdline['mgtnic']
    )

    with open("/tmp/iptables-secstorage", "w") as f:
        f.write(iptables_rules)

    os.system("iptables-restore < /tmp/iptables-secstorage")


class SecondaryStorageVM:
    def __init__(self, cmdline) -> None:
        super().__init__()

        self.cmdline = cmdline

        self.config_dir = "/etc/cosmic/agent/"

    def start(self):
        logging.info("Setting up configuration for %s" % self.cmdline["type"])
        self.setup_agent_config()
        setup_html()
        setup_iptable_rules(self.cmdline)
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
""" % (self.cmdline["publicip"], self.cmdline["publicip"])

        filename = "/etc/nginx/conf.d/vhost-%s.conf" % (self.cmdline["publicip"])

        with open(filename, 'w') as f:
            f.write(vhost)

        os.system("systemctl start nginx")
        os.system("systemctl reload nginx")
