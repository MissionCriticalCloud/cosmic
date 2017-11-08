import logging
import os
import re

import CsHelper
from CsFile import CsFile


class CsSite2SiteVpn(object):
    def __init__(self, config):
        self.config = config
        self.dbag = self.config.dbag_site2sitevpn

    """
    Setup any configured vpns (using strongswan)
    left is the local machine
    right is where the clients connect from
    """

    VPNCONFDIR = "/etc/strongswan/ipsec.d"

    def process(self):
        self.confips = []
        # collect a list of configured vpns
        for file in os.listdir(self.VPNCONFDIR):
            m = re.search("^ipsec.vpn-(.*).conf", file)
            if m:
                self.confips.append(m.group(1))

        for vpn in self.dbag:
            if vpn == "id":
                continue

            local_ip = self.dbag[vpn]['local_public_ip']
            dev = CsHelper.get_device(local_ip)

            if dev == "":
                logging.error("Request for ipsec to %s not possible because ip is not configured", local_ip)
                continue

            self.configure_iptables(dev, self.dbag[vpn])
            self.configure_ipsec(self.dbag[vpn])

        # Delete vpns that are no longer in the configuration
        for ip in self.confips:
            self.deletevpn(ip)

        self.check_ipsec()

    def check_ipsec(self):
        CsHelper.start_if_stopped("strongswan")

        logging.info("Checking if strongswan is running correctly: systemctl status strongswan")
        p = CsHelper.execute2("systemctl status strongswan", log=False)

        out, _ = p.communicate()

        if "Security Associations" not in out:
            logging.error("Security Associations not found in: %s" % out)
            CsHelper.execute2("systemctl stop strongswan")
            CsHelper.execute2("systemctl start strongswan")

    def deletevpn(self, ip):
        logging.info("Removing VPN configuration for %s", ip)
        CsHelper.execute("strongswan down vpn-%s" % ip, wait=False)
        vpnconffile = "%s/ipsec.vpn-%s.conf" % (self.VPNCONFDIR, ip)
        vpnsecretsfile = "%s/ipsec.vpn-%s.secrets" % (self.VPNCONFDIR, ip)
        os.remove(vpnconffile)
        os.remove(vpnsecretsfile)
        CsHelper.execute("strongswan reload")

    def configure_iptables(self, dev, obj):
        self.config.fw.append(["", "front", "-A INPUT -i %s -p udp -m udp --dport 500 -s %s -d %s -j ACCEPT" % (dev, obj['peer_gateway_ip'], obj['local_public_ip'])])
        self.config.fw.append(["", "front", "-A INPUT -i %s -p udp -m udp --dport 4500 -s %s -d %s -j ACCEPT" % (dev, obj['peer_gateway_ip'], obj['local_public_ip'])])
        self.config.fw.append(["", "front", "-A INPUT -i %s -p esp -s %s -d %s -j ACCEPT" % (dev, obj['peer_gateway_ip'], obj['local_public_ip'])])
        self.config.fw.append(["nat", "front", "-A POSTROUTING -o %s -m mark --mark 0x525 -j ACCEPT" % dev])
        for net in obj['peer_guest_cidr_list'].lstrip().rstrip().split(','):
            self.config.fw.append(["mangle", "front",
                            "-A FORWARD -s %s -d %s -j MARK --set-xmark 0x525/0xffffffff" % (obj['local_guest_cidr'], net)])
            self.config.fw.append(["mangle", "",
                            "-A OUTPUT -s %s -d %s -j MARK --set-xmark 0x525/0xffffffff" % (obj['local_guest_cidr'], net)])
            self.config.fw.append(["mangle", "front",
                            "-A FORWARD -s %s -d %s -j MARK --set-xmark 0x524/0xffffffff" % (net, obj['local_guest_cidr'])])
            self.config.fw.append(["mangle", "",
                            "-A INPUT -s %s -d %s -j MARK --set-xmark 0x524/0xffffffff" % (net, obj['local_guest_cidr'])])

    def configure_ipsec(self, obj):
        leftpeer = obj['local_public_ip']
        rightpeer = obj['peer_gateway_ip']
        peerlist = obj['peer_guest_cidr_list'].split(',')
        vpnconffile = "%s/ipsec.vpn-%s.conf" % (self.VPNCONFDIR, rightpeer)
        vpnsecretsfile = "%s/ipsec.vpn-%s.secrets" % (self.VPNCONFDIR, rightpeer)
        ikepolicy = obj['ike_policy'].replace(';', '-')
        esppolicy = obj['esp_policy'].replace(';', '-')

        strokefile = '/etc/strongswan.d/charon/stroke.conf'

        # Set timeout to 30s
        file = CsFile(strokefile)
        file.greplace("# timeout = 0", "timeout = 30000")
        file.commit()

        if rightpeer in self.confips:
            self.confips.remove(rightpeer)
        file = CsFile(vpnconffile)
        file.empty()
        for idx, p in enumerate(peerlist):
            if idx == 0:
                file.add("#conn for vpn-%s" % rightpeer, -1)
                file.add("conn vpn-%s" % rightpeer, -1)
            else:
                file.add("#conn for vpn-%s-%i" % (rightpeer, idx), -1)
                file.add("conn vpn-%s-%i" % (rightpeer, idx), -1)
            file.add(" left=%s" % leftpeer, -1)
            file.add(" leftsubnet=%s" % obj['local_guest_cidr'], -1)
            file.add(" right=%s" % rightpeer, -1)
            file.add(" rightsubnet=%s" % p, -1)
            file.add(" type=tunnel", -1)
            file.add(" authby=secret", -1)
            file.add(" keyexchange=ike", -1)
            file.add(" ike=%s" % ikepolicy, -1)
            file.add(" ikelifetime=%s" % self.convert_sec_to_h(obj['ike_lifetime']), -1)
            file.add(" esp=%s" % esppolicy, -1)
            file.add(" lifetime=%s" % self.convert_sec_to_h(obj['esp_lifetime']), -1)
            file.add(" keyingtries=%forever", -1)
            file.add(" auto=start", -1)
            file.add(" closeaction=restart", -1)
            file.add(" inactivity=0", -1)
            if 'encap' not in obj:
                obj['encap'] = False
            file.add(" forceencaps=%s" % CsHelper.bool_to_yn(obj['encap']), -1)
            if obj['dpd']:
                file.add(" dpddelay=30", -1)
                file.add(" dpdtimeout=120", -1)
                file.add(" dpdaction=restart", -1)
            file.add(" ", -1)
        secret = CsFile(vpnsecretsfile)
        secret.search("%s " % leftpeer, "%s %s : PSK \"%s\"" % (leftpeer, rightpeer, obj['ipsec_psk']))
        if secret.is_changed() or file.is_changed():
            secret.commit()
            file.commit()
            logging.info("Configured vpn %s %s", leftpeer, rightpeer)
            CsHelper.execute("strongswan rereadsecrets")

        # This will load the new config and start the connection when needed since auto=start in the config
        os.chmod(vpnsecretsfile, 0o400)
        CsHelper.execute("strongswan reload")

    def convert_sec_to_h(self, val):
        hrs = int(val) / 3600
        return "%sh" % hrs
