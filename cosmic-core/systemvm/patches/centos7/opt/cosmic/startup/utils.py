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

    def setup_private_nic(self):
        ifcfg = """
DEVICE="eth0"
IPV6INIT="no"
BOOTPROTO="none"
ONBOOT="yes"
HWADDR="%s"
IPADDR="%s"
NETMASK="%s"
EOF
""" % (self.cmdline["eth0mac"], self.cmdline["eth0ip"], self.cmdline["eth0mask"])

        with open("/etc/sysconfig/network-scripts/ifcfg-eth0", "w") as f:
            f.write(ifcfg)

        os.system("ifdown eth0; ifup eth0")

    def setup_dns(self):
        resolv_conf = []

        if self.cmdline["domain"]:
            resolv_conf.append("search %s" % self.cmdline["domain"])
        if self.cmdline["internaldns1"]:
            resolv_conf.append("nameserver %s" % self.cmdline["internaldns1"])
        if self.cmdline["internaldns2"]:
            resolv_conf.append("nameserver %s" % self.cmdline["internaldns2"])
        if self.cmdline["dns1"]:
            resolv_conf.append("nameserver %s" % self.cmdline["dns1"])
        if self.cmdline["dns2"]:
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
            os.mkdir(self.config_dir, 0o644)

        agent_properties = []
        for key, value in self.cmdline.items():
            agent_properties.append("%s=%s" % (key, value))

        with open(self.config_dir + "agent.properties", "w") as f:
            f.write("\n".join(agent_properties))
