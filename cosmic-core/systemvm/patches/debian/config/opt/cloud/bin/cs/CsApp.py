# -- coding: utf-8 --

import os
import glob
import logging

import CsHelper
from CsFile import CsFile
from CsProcess import CsProcess


class CsApp:
    def __init__(self, ip):
        self.dev = ip.getDevice()
        self.ip = ip.get_ip_address()
        self.type = ip.get_type()
        self.fw = ip.fw
        self.config = ip.config


class CsApache(CsApp):
    """ Set up Apache """

    # Make sure Apache config files that are not used any more (they were created before Cosmic 5.3) are deleted
    def remove_legacy_apache_config_files(self):
        legacy_file_patterns = [
            '/etc/apache2/ports.conf',
            '/etc/apache2/sites-available/default',
            '/etc/apache2/sites-available/default-ssl',
            '/etc/apache2/conf.d/ports.*.meta-data.conf',
            '/etc/apache2/sites-available/ipAlias*',
            '/etc/apache2/sites-enabled/ipAlias*',
            '/etc/apache2/conf.d/vhost*.conf',
            '/etc/apache2/ports.conf',
            '/etc/apache2/vhostexample.conf',
            '/etc/apache2/sites-available/default',
            '/etc/apache2/sites-available/default-ssl',
            '/etc/apache2/sites-enabled/default'
        ]

        for legacy_file_pattern in legacy_file_patterns:
            for legacy_file in glob.glob(legacy_file_pattern):
                if os.path.isfile(legacy_file):
                    os.remove(legacy_file)
                    logging.debug("Found and removed legacy Apache config file '%s'" % legacy_file)

        # Remove legacy ports.conf include
        apache_config = CsFile("/etc/apache2/apache2.conf")
        apache_config.deleteLine("Include ports.conf", False)
        if apache_config.is_changed():
            logging.debug("Found and removed legacy Apache ports.conf inclusion")
            apache_config.commit()

    def remove(self):
        self.remove_legacy_apache_config_files()
        file = "/etc/apache2/sites-enabled/vhost-%s.conf" % self.dev
        if os.path.isfile(file):
            os.remove(file)
            CsHelper.service("apache2", "restart")

    def setup(self):
        self.remove_legacy_apache_config_files()
        CsHelper.copy_if_needed("/etc/apache2/vhost.template",
                                "/etc/apache2/sites-enabled/vhost-%s.conf" % self.ip)

        file = CsFile("/etc/apache2/sites-enabled/vhost-%s.conf" % (self.ip))
        file.search("<VirtualHost.*:80>", "\t<VirtualHost %s:80>" % (self.ip))
        file.search("<VirtualHost.*:443>", "\t<VirtualHost %s:443>" % (self.ip))
        file.search("Listen .*:80", "Listen %s:80" % (self.ip))
        file.search("Listen .*:443", "Listen %s:443" % (self.ip))
        file.search("NameVirtualHost .*:80", "NameVirtualHost %s:80" % (self.ip))
        file.search("ServerName.*", "\tServerName %s.%s" % (self.config.cl.get_type(), self.config.get_domain()))
        if file.is_changed():
            file.commit()
            CsHelper.service("apache2", "restart")

        self.fw.append(["", "front",
                        "-A INPUT -i %s -d %s/32 -p tcp -m tcp -m state --state NEW --dport 80 -j ACCEPT" % (self.dev, self.ip)
                        ])

        self.fw.append(["", "front",
                        "-A INPUT -i %s -d %s/32 -p tcp -m tcp -m state --state NEW --dport 443 -j ACCEPT" % (self.dev, self.ip)
                        ])


class CsPasswdSvc():
    """
      nohup bash /opt/cloud/bin/vpc_passwd_server $ip >/dev/null 2>&1 &
    """

    def __init__(self, ip):
        self.ip = ip

    def start(self):
        proc = CsProcess(["dummy"])
        if proc.grep("passwd_server_ip %s" % self.ip) == -1:
            proc.start("/opt/cloud/bin/passwd_server_ip %s >> /var/log/cloud.log 2>&1" % self.ip, "&")

    def stop(self):
        proc = CsProcess(["Password Service"])
        pid = proc.grep("passwd_server_ip.py %s" % self.ip)
        proc.kill(pid)
        pid = proc.grep("passwd_server_ip %s" % self.ip)
        proc.kill(pid)
        pid = proc.grep("8080,reuseaddr,fork,crnl,bind=%s" % self.ip)
        proc.kill(pid)

    def restart(self):
        self.stop()
        self.start()


class CsDnsmasq(CsApp):
    """ Set up dnsmasq """

    def add_firewall_rules(self):
        """ Add the necessary firewall rules
        """
        self.fw.append(["", "front",
                        "-A INPUT -i %s -p udp -m udp --dport 67 -j ACCEPT" % self.dev
                        ])

        self.fw.append(["", "front",
                        "-A INPUT -i %s -d %s/32 -p udp -m udp --dport 53 -j ACCEPT" % (self.dev, self.ip)
                        ])

        self.fw.append(["", "front",
                        "-A INPUT -i %s -d %s/32 -p tcp -m tcp --dport 53 -j ACCEPT" % (self.dev, self.ip)
                        ])
