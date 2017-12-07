import os
import subprocess

class Utils:
    def __init__(self, cmdline) -> None:
        super().__init__()

        self.cmdline = cmdline
        self.config_dir = "/etc/cosmic/agent/"
        self.ssh_port = 3922
        self.link_local_ip = None

    def bootstrap(self):
        self.setup_hostname()
        self.setup_dns()
        self.setup_private_nic()
        if "type" in self.cmdline and self.cmdline['type'] not in ("secstorage", "consoleproxy"):
            self.setup_sync_nic()
        self.setup_ssh()
        self.setup_banner()
        self.setup_default_gw()
        self.enable_keepalived()
        self.restart_watchdog()

    def setup_private_nic(self):

        for interface in [0, 1, 2, 3]:
            if "eth%sip" % interface in self.cmdline:
                ifcfg = """
DEVICE="eth%s"
IPV6INIT="no"
BOOTPROTO="none"
ONBOOT="yes"
HWADDR="%s"
IPADDR="%s"
NETMASK="%s"
""" % (interface, self.cmdline["eth%smac" % interface], self.cmdline["eth%sip" % interface], self.cmdline["eth%smask" %
                                                                                                          interface])

                if self.cmdline["eth%sip" % interface].count("169.254.") == 1:
                    self.link_local_ip = self.cmdline["eth%sip" % interface]
                    print(self.link_local_ip)

                with open("/etc/sysconfig/network-scripts/ifcfg-eth%s" % interface, "w") as f:
                    f.write(ifcfg)

                os.system("ifdown eth%s; ifup eth%s" % (interface, interface))

    def get_device_from_mac_address(self, macaddress):
        device = self.execute("find /sys/class/net/*/address | xargs grep %s | cut -d\/ -f5 " % macaddress)
        if not device:
            return False
        return device[0]

    def find_sync_nic(self):
        return self.get_device_from_mac_address(self.cmdline["syncmac"])

    def setup_sync_nic(self):

        sync_device = self.find_sync_nic()
        sync_ip_address = self.link_local_ip.replace("169.254", "100.100")

        if not sync_device:
            return False

        ifcfg = """
    DEVICE="%s"
    IPV6INIT="no"
    BOOTPROTO="none"
    ONBOOT="yes"
    IPADDR="%s"
    NETMASK="255.255.0.0"
    HWADDR="%s"
    """ % (sync_device, sync_ip_address, self.cmdline["%smac" % sync_device])

        with open("/etc/sysconfig/network-scripts/ifcfg-%s" % sync_device, "w") as f:
            f.write(ifcfg)

        os.system("ifdown %s; ifup %s" % (sync_device, sync_device))

    def setup_dns(self):
        resolv_conf = []

        if "domain" in self.cmdline:
            resolv_conf.append("search %s" % self.cmdline["domain"])
        if "internaldns1" in self.cmdline:
            resolv_conf.append("nameserver %s" % self.cmdline["internaldns1"])
        if "internaldns2" in self.cmdline:
            resolv_conf.append("nameserver %s" % self.cmdline["internaldns2"])
        if "dns1" in self.cmdline:
            resolv_conf.append("nameserver %s" % self.cmdline["dns1"])
        if "dns2" in self.cmdline:
            resolv_conf.append("nameserver %s" % self.cmdline["dns2"])

        with open("/etc/resolv.conf", "w") as f:
            f.write("\n".join(resolv_conf))

    def setup_hostname(self):
        # Setup hostname
        os.system("hostnamectl set-hostname %s" % self.cmdline["name"])

    def setup_ssh(self):

        link_local_ip = self.cmdline["eth0ip"]

        sshd_config = """
Port %s
AddressFamily inet
ListenAddress %s

PermitRootLogin yes
PasswordAuthentication no
PubkeyAuthentication yes
AuthorizedKeysFile .ssh/authorized_keys
HostKey /etc/ssh/ssh_host_rsa_key
HostKey /etc/ssh/ssh_host_ecdsa_key
HostKey /etc/ssh/ssh_host_ed25519_key

SyslogFacility AUTHPRIV

ChallengeResponseAuthentication no

PrintMotd yes

AcceptEnv LANG LC_CTYPE LC_NUMERIC LC_TIME LC_COLLATE LC_MONETARY LC_MESSAGES
AcceptEnv LC_PAPER LC_NAME LC_ADDRESS LC_TELEPHONE LC_MEASUREMENT
AcceptEnv LC_IDENTIFICATION LC_ALL LANGUAGE
AcceptEnv XMODIFIERS
""" % (self.ssh_port, link_local_ip)

        with open("/etc/ssh/sshd_config", "w") as f:
            f.write(sshd_config)

        os.chmod("/root/.ssh/authorized_keys", 0o644)
        os.system("systemctl restart sshd")

    def setup_agent_properties(self):
        if not os.path.isdir(self.config_dir):
            os.makedirs(self.config_dir, 0o644, True)

        agent_properties = []
        for key, value in self.cmdline.items():
            agent_properties.append("%s=%s" % (key, value))

        with open(self.config_dir + "agent.properties", "w") as f:
            f.write("\n".join(agent_properties))

    def setup_banner(self):
        with open("/etc/redhat-release", "r") as f:
            release = f.readline()

        link_local_ip = self.cmdline["eth0ip"]

        prelogin_banner = """
Cosmic sytemvm powered by %s

                           *   	   +
                             ╔═╗╔═╗╔═╗╔╦╗╦╔═╗  *
               +             ║  ║ ║╚═╗║║║║║
                      '      ╚═╝╚═╝╚═╝╩ ╩╩╚═╝
                  *          + 		 *
                      +   /\
         +              .'  '.   *
                *      /======\      +
                      ;:.  _   ;
                      |:. (_)  |
                      |:.  _   |
            +         |:. (_)  |          *
                      ;:.      ;
                    .' \:.    / `.
                   / .-'':._.'`-. \
                   |/    /||\    \|
                 _..--""``````""--.._
           _.-'``                    ``'-._
         -'                                '-
  ____________________________________________
 ( Void 100%% of your warranty @ %s )
  --------------------------------------------
""" % (release, link_local_ip)

        with open("/etc/issue", "w") as f:
            f.write(prelogin_banner)
        with open("/etc/issue.net", "w") as f:
            f.write(prelogin_banner)

        motd = """
Cosmic sytemvm powered by %s
  ______________________________________
 ( Booo! 0%% of your warranty remaining! )
  --------------------------------------
        \   ^__^
         \  (oo)\_______
            (__)\       )\/
               ||----w |
               ||     ||

""" % release

        with open("/etc/motd", "w") as f:
            f.write(motd)

    def setup_default_gw(self):
        if "gateway" in  self.cmdline:
            with open("/etc/sysconfig/network", "w") as f:
                f.write("GATEWAY=%s" % self.cmdline["gateway"])

            os.system("ip route add default via %s" % self.cmdline["gateway"])

    def execute(self, command):
        stdoutdata = subprocess.getoutput(command)
        return stdoutdata.split()

    def enable_keepalived(self):
        # On startup, make sure keepalived is enabled and started, or else redundancy will fail
        # When router is rebooted without Cosmic knowing it
        os.system("systemctl enable keepalived")
        os.system("systemctl start keepalived")

    def restart_watchdog(self):
        os.system("systemctl restart watchdog")