import os


class Utils:
    def __init__(self, cmdline) -> None:
        super().__init__()

        self.cmdline = cmdline
        self.config_dir = "/etc/cosmic/agent/"
        self.ssh_port = 3922

    def bootstrap(self):
        self.setup_hostname()
        self.setup_dns()
        self.setup_private_nic()
        self.setup_ssh()
        self.setup_banner()
        self.setup_default_gw()

    def setup_private_nic(self):

        for interface in [0, 1, 2]:
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

                with open("/etc/sysconfig/network-scripts/ifcfg-eth%s" % interface, "w") as f:
                    f.write(ifcfg)

                os.system("ifdown eth%s; ifup eth%s" % (interface, interface))

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
""" % (self.ssh_port, self.cmdline["eth0ip"])

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

        prelogin_banner = """
Cosmic sytemvm powered by %s
  ____________________________________________
 ( Void 100%% of your warranty @ %s )
  --------------------------------------------
        \   ^__^
         \  (oo)\_______
           (__)\       )\/
             ||----w |
             ||     ||

""" % (release, self.cmdline["eth0ip"])

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
