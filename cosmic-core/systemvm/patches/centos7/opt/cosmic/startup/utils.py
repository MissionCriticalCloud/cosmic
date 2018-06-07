import os
import subprocess
import yaml


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
        self.setup_system_nics()
        if "type" in self.cmdline and self.cmdline['type'] not in ("secstorage", "consoleproxy"):
            self.setup_sync_nic()
        self.setup_ssh()
        self.setup_banner()
        self.setup_default_gw()
        self.enable_keepalived()
        self.set_root_password()
        self.restart_watchdog()

    def set_root_password(self):
        if 'vmpassword' in self.cmdline and len(self.cmdline['vmpassword']) > 0:
            print("Changed root password")
            os.system("echo \"root:%s\" | chpasswd" % self.cmdline['vmpassword'])
        else:
            print("No need to change the root password")

    def setup_system_nics(self):

        for network_type in ["control", "public", "mgt"]:

            if "type" in self.cmdline and self.cmdline['type'] not in ("secstorage", "consoleproxy"):
                if network_type == "public" or network_type == "mgt":
                    continue

            print("Processing %s" % network_type)
            interface = self.find_nic(network_type)

            if not interface:
                print("Unable to find interface for network_type %s" % network_type)
                continue

            print("Found interface %s" % interface)
            if "%sip" % network_type in self.cmdline:
                ifcfg = """
DEVICE="%s"
IPV6INIT="no"
BOOTPROTO="none"
ONBOOT="yes"
HWADDR="%s"
IPADDR="%s"
NETMASK="%s"
""" % (interface, self.cmdline["%smac" % network_type], self.cmdline["%sip" % network_type], self.cmdline["%smask" %
                                                                                                          network_type])

                # Save interface names to be used later
                self.cmdline["%snic" % network_type] = interface

                if network_type == "control":
                    self.link_local_ip = self.cmdline["controlip"]
                    print(self.link_local_ip)

                with open("/etc/sysconfig/network-scripts/ifcfg-%s" % interface, "w") as f:
                    f.write(ifcfg)

                os.system("ifdown %s; ifup %s" % (interface, interface))

    def get_device_from_mac_address(self, macaddress):
        print("Finding device for mac_address %s" % macaddress)
        device = self.execute("find /sys/class/net/*/address | xargs grep %s | cut -d\/ -f5 " % macaddress)
        if not device:
            return False
        return device[0]

    def find_nic(self, name):
        return self.get_device_from_mac_address(self.cmdline["%smac" % name])

    def setup_sync_nic(self):

        sync_device = self.find_nic("sync")
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
    """ % (sync_device, sync_ip_address, self.cmdline['syncmac'])

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

        link_local_ip = self.cmdline["controlip"]

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

Subsystem sftp /usr/libexec/openssh/sftp-server

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

        agent_properties = { }
        for key, value in self.cmdline.items():
            if key == 'host':
                agent_properties['hosts'] = value.split(',')
            else:
                agent_properties[key] = value

        config = { 'cosmic': agent_properties }
        with open(self.config_dir + "application.yml", "w") as f:
            yaml.dump(config, f, default_flow_style=False)

    def setup_banner(self):
        with open("/etc/redhat-release", "r") as f:
            release = f.readline()

        link_local_ip = self.cmdline["controlip"]

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
        if "gateway" in self.cmdline:
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

    def set_rfc1918_routes(self):
        os.system("ip route add 10.0.0.0/8 via %s" % self.cmdline["localgw"])
        os.system("ip route add 172.16.0.0/12 via %s" % self.cmdline["localgw"])
        os.system("ip route add 192.168.0.0/16 via %s" % self.cmdline["localgw"])
