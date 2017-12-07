import logging

import CsHelper
from CsFile import CsFile


class CsRemoteAccessVpn(object):
    def __init__(self, config):
        self.config = config
        self.dbag = self.config.dbag_remoteaccessvpn

    VPNCONFDIR = "/etc/strongswan/ipsec.d"

    def process(self):
        self.confips = []

        logging.debug(self.dbag)
        for public_ip in self.dbag:
            if public_ip == "id":
                continue
            vpnconfig = self.dbag[public_ip]

            # Enable remote access vpn
            if vpnconfig['create']:
                logging.debug("Enabling  remote access vpn  on " + public_ip)
                CsHelper.start_if_stopped("strongswan")
                self.configure_l2tpIpsec(public_ip, self.dbag[public_ip])
                logging.debug("Remote accessvpn  data bag %s", self.dbag)
                self.remoteaccessvpn_iptables(public_ip, self.dbag[public_ip])

                CsHelper.execute("strongswan update")
                CsHelper.execute("systemctl start xl2tpd")
                CsHelper.execute("strongswan rereadsecrets")
            else:
                logging.debug("Disabling remote access vpn .....")
                # disable remote access vpn
                CsHelper.execute("strongswan down L2TP-PSK")
                CsHelper.execute("systemctl stop xl2tpd")

    def configure_l2tpIpsec(self, left, obj):
        l2tpconffile = "%s/l2tp.conf" % (self.VPNCONFDIR)
        vpnsecretfilte = "%s/ipsec.any.secrets" % (self.VPNCONFDIR)
        xl2tpdconffile = "/etc/xl2tpd/xl2tpd.conf"
        xl2tpoptionsfile = '/etc/ppp/options.xl2tpd'
        strokefile = '/etc/strongswan.d/charon/stroke.conf'

        file = CsFile(l2tpconffile)
        localip = obj['local_ip']
        iprange = obj['ip_range']
        psk = obj['preshared_key']

        # l2tp config options
        file.search("rekey=", "        rekey=no")
        file.search("keyingtries=", "        keyingtries=3")
        file.search("keyexchange=", "        keyexchange=ikev1")
        file.search("forceencaps=", "        forceencaps=yes")
        file.search("leftfirewall=", "        leftfirewall=yes")
        file.search("leftnexthop=", "        leftnexthop=%defaultroute")
        file.search("type=", "        type=transport")
        file.search("leftprotoport=", "        leftprotoport=17/1701")
        file.search("right=", "        right=%any")
        file.search("rightprotoport=", "        rightprotoport=17/%any")
        file.search("auto=", "        auto=add")
        file.search("rightsubnetwithin=", "        rightsubnetwithin=0.0.0.0/0")
        file.search("left=", "        left=%s" % left)
        file.search("authby=", "        authby=psk")
        file.commit()

        # Set timeout to 30s
        file = CsFile(strokefile)
        file.greplace("# timeout = 0", "timeout = 30000")
        file.commit()

        # Secrets
        secret = CsFile(vpnsecretfilte)
        secret.addeq(": PSK \"%s\"" % psk)
        secret.commit()

        xl2tpdconf = CsFile(xl2tpdconffile)
        xl2tpdconf.addeq("ip range = %s" % iprange)
        xl2tpdconf.addeq("local ip = %s" % localip)
        xl2tpdconf.commit()

        xl2tpoptions = CsFile(xl2tpoptionsfile)
        xl2tpoptions.search("ms-dns ", "ms-dns %s" % localip)
        xl2tpoptions.commit()

    def remoteaccessvpn_iptables(self, publicip, obj):
        publicdev = obj['public_interface']
        localcidr = obj['local_cidr']
        local_ip = obj['local_ip']

        self.config.fw.append(["", "", "-A INPUT -i %s --dst %s -p udp -m udp --dport 500 -j ACCEPT" % (publicdev, publicip)])
        self.config.fw.append(["", "", "-A INPUT -i %s --dst %s -p udp -m udp --dport 4500 -j ACCEPT" % (publicdev, publicip)])
        self.config.fw.append(["", "", "-A INPUT -i %s --dst %s -p udp -m udp --dport 1701 -j ACCEPT" % (publicdev, publicip)])
        self.config.fw.append(["", "", "-A INPUT -i %s -p ah -j ACCEPT" % publicdev])
        self.config.fw.append(["", "", "-A INPUT -i %s -p esp -j ACCEPT" % publicdev])

        if self.config.is_vpc():
            self.config.fw.append(["", "", " -N VPN_FORWARD"])
            self.config.fw.append(["", "", "-I FORWARD -i ppp+ -j VPN_FORWARD"])
            self.config.fw.append(["", "", "-I FORWARD -o ppp+ -j VPN_FORWARD"])
            self.config.fw.append(["", "", "-I FORWARD -o ppp+ -j VPN_FORWARD"])
            self.config.fw.append(["", "", "-A VPN_FORWARD -s  %s -j RETURN" % localcidr])
            self.config.fw.append(["", "", "-A VPN_FORWARD -i ppp+ -d %s -j RETURN" % localcidr])
            self.config.fw.append(["", "", "-A VPN_FORWARD -i ppp+  -o ppp+ -j RETURN"])
        else:
            self.config.fw.append(["", "", "-A FORWARD -i ppp+ -o  ppp+ -j ACCEPT"])
            self.config.fw.append(["", "", "-A FORWARD -s %s -o  ppp+ -j ACCEPT" % localcidr])
            self.config.fw.append(["", "", "-A FORWARD -i ppp+ -d %s  -j ACCEPT" % localcidr])

        self.config.fw.append(["", "", "-A INPUT -i ppp+ -m udp -p udp --dport 53 -j ACCEPT"])
        self.config.fw.append(["", "", "-A INPUT -i ppp+ -m tcp -p tcp --dport 53 -j ACCEPT"])
        self.config.fw.append(["nat", "front", "-A PREROUTING -i ppp+ -m tcp -p tcp --dport 53 -j DNAT --to-destination %s" % local_ip])

        if self.config.is_vpc():
            return

        self.config.fw.append(["mangle", "", "-N  VPN_%s " % publicip])
        self.config.fw.append(["mangle", "", "-A VPN_%s -j RETURN " % publicip])
        self.config.fw.append(["mangle", "", "-I VPN_%s -p ah  -j ACCEPT " % publicip])
        self.config.fw.append(["mangle", "", "-I VPN_%s -p esp  -j ACCEPT " % publicip])
        self.config.fw.append(["mangle", "", "-I PREROUTING  -d %s -j VPN_%s " % (publicip, publicip)])
