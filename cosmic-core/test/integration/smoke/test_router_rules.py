from ddt import ddt, data
from nose.plugins.attrib import attr

from marvin.cloudstackTestCase import cloudstackTestCase
from marvin.codes import (
    FAILED,
    STATIC_NAT_RULE,
    LB_RULE,
    NAT_RULE
)
from marvin.lib.base import (
    Account,
    VirtualMachine,
    NATRule,
    PublicIPAddress,
    StaticNATRule,
    FireWallRule,
    LoadBalancerRule,
    Router
)
from marvin.lib.common import (
    get_domain,
    get_zone,
    get_template,
    list_hosts,
    get_default_virtual_machine_offering
)
from marvin.lib.utils import (
    cleanup_resources,
    get_process_status
)
from marvin.utils.MarvinLog import MarvinLog
from marvin.utils.SshClient import SshClient

@ddt
class TestRouterRules(cloudstackTestCase):
    @classmethod
    def setUpClass(cls):
        cls.logger = MarvinLog(MarvinLog.LOGGER_TEST).get_logger()

        testClient = super(TestRouterRules, cls).getClsTestClient()
        cls.apiclient = testClient.getApiClient()
        cls.services = testClient.getParsedTestDataConfig().copy()
        # Get Zone, Domain and templates
        cls.domain = get_domain(cls.apiclient)
        cls.zone = get_zone(cls.apiclient, testClient.getZoneForTests())
        cls.hypervisor = testClient.getHypervisorInfo()
        template = get_template(
            cls.apiclient,
            cls.zone.id
        )
        if template == FAILED:
            assert False, "get_template() failed to return template\
                    with description %s" % cls.services["ostype"]

        # Create an account, network, VM and IP addresses
        cls.account = Account.create(
            cls.apiclient,
            cls.services["account"],
            admin=True,
            domainid=cls.domain.id
        )
        cls.services["virtual_machine"]["zoneid"] = cls.zone.id
        cls.service_offering = get_default_virtual_machine_offering(cls.apiclient)
        cls.virtual_machine = VirtualMachine.create(
            cls.apiclient,
            cls.services["virtual_machine"],
            templateid=template.id,
            accountid=cls.account.name,
            domainid=cls.account.domainid,
            serviceofferingid=cls.service_offering.id
        )
        cls.defaultNetworkId = cls.virtual_machine.nic[0].networkid

        cls._cleanup = [
            cls.virtual_machine,
            cls.account
        ]

    @classmethod
    def tearDownClass(cls):
        try:
            cleanup_resources(cls.apiclient, cls._cleanup)
        except Exception as e:
            raise Exception("Warning: Exception during cleanup : %s" % e)

    def setUp(self):
        self.apiclient = self.testClient.getApiClient()
        self.cleanup = []
        return

    def tearDown(self):
        cleanup_resources(self.apiclient, self.cleanup)
        return

    @data(STATIC_NAT_RULE, NAT_RULE, LB_RULE)
    @attr(tags=['advanced'])
    def test_01_network_rules_acquired_public_ip(self, value):
        """Test for Router rules for network rules on acquired public IP"""

        # Validate the following:
        # 1. listPortForwardingRules should not return the deleted rule anymore
        # 2. attempt to do ssh should now fail

        self.ipaddress = PublicIPAddress.create(
            self.apiclient,
            accountid=self.account.name,
            zoneid=self.zone.id,
            domainid=self.account.domainid,
            networkid=self.defaultNetworkId
        )

        self.createNetworkRules(rule=value,
                                ipaddressobj=self.ipaddress,
                                networkid=self.defaultNetworkId)

        router = Router.list(self.apiclient,
                             networkid=self.virtual_machine.nic[0].networkid,
                             listall=True)[0]

        response = self.getCommandResultFromRouter(router, "ip addr")
        self.logger.debug(response)
        stringToMatch = "inet %s" % self.ipaddress.ipaddress.ipaddress
        self.assertTrue(stringToMatch in str(response), "IP address is\
                not added to the VR!")

        try:
            self.logger.debug("SSHing into VM with IP address %s with NAT IP %s" %
                         (
                             self.virtual_machine.ipaddress,
                             self.ipaddress.ipaddress.ipaddress
                         ))
            self.virtual_machine.get_ssh_client(
                self.ipaddress.ipaddress.ipaddress)
        except Exception as e:
            self.fail(
                "SSH Access failed for %s: %s" %
                (self.virtual_machine.ipaddress, e)
            )

        # Validate the following:
        # 1. listIpForwardingRules should not return the deleted rule anymore
        # 2. attempt to do ssh should now fail

        self.removeNetworkRules(rule=value)

        response = self.getCommandResultFromRouter(router, "ip addr")
        self.logger.debug(response)
        stringToMatch = "inet %s" % self.ipaddress.ipaddress.ipaddress
        self.assertFalse(stringToMatch in str(response), "IP address is\
                not removed from VR even after disabling stat in NAT")

        # Check if the Public SSH port is inaccessible
        with self.assertRaises(Exception):
            self.logger.debug(
                "SSHing into VM with IP address %s after NAT rule deletion" %
                self.virtual_machine.ipaddress)

            SshClient(
                self.ipaddress.ipaddress.ipaddress,
                self.virtual_machine.ssh_port,
                self.virtual_machine.username,
                self.virtual_machine.password,
                retries=2,
                delay=0
            )
        return

    def getCommandResultFromRouter(self, router, command):
        """Run given command on router and return the result"""

        hosts = list_hosts(
            self.apiclient,
            id=router.hostid,
        )
        self.assertEqual(
            isinstance(hosts, list),
            True,
            "Check for list hosts response return valid data"
        )
        host = hosts[0]
        host.user = self.services["configurableData"]["host"]["username"]
        host.passwd = self.services["configurableData"]["host"]["password"]

        result = get_process_status(
            host.ipaddress,
            22,
            host.user,
            host.passwd,
            router.linklocalip,
            command
        )

        return result

    def createNetworkRules(self, rule, ipaddressobj, networkid):
        """ Create specified rule on acquired public IP and
        default network of virtual machine
        """
        # Open up firewall port for SSH
        self.fw_rule = FireWallRule.create(
            self.apiclient,
            ipaddressid=ipaddressobj.ipaddress.id,
            protocol=self.services["fwrule"]["protocol"],
            cidrlist=['0.0.0.0/0'],
            startport=self.services["fwrule"]["startport"],
            endport=self.services["fwrule"]["endport"]
        )

        if rule == STATIC_NAT_RULE:
            StaticNATRule.enable(
                self.apiclient,
                ipaddressobj.ipaddress.id,
                self.virtual_machine.id,
                networkid
            )

        elif rule == LB_RULE:
            self.lb_rule = LoadBalancerRule.create(
                self.apiclient,
                self.services["lbrule"],
                ipaddressid=ipaddressobj.ipaddress.id,
                accountid=self.account.name,
                networkid=self.virtual_machine.nic[0].networkid,
                domainid=self.account.domainid)

            vmidipmap = [{ "vmid": str(self.virtual_machine.id),
                           "vmip": str(self.virtual_machine.nic[0].ipaddress) }]

            self.lb_rule.assign(
                self.apiclient,
                vmidipmap=vmidipmap
            )
        else:
            self.nat_rule = NATRule.create(
                self.apiclient,
                self.virtual_machine,
                self.services["natrule"],
                ipaddressobj.ipaddress.id
            )
        return

    def removeNetworkRules(self, rule):
        """ Remove specified rule on acquired public IP and
        default network of virtual machine
        """
        self.fw_rule.delete(self.apiclient)

        if rule == STATIC_NAT_RULE:
            StaticNATRule.disable(
                self.apiclient,
                self.ipaddress.ipaddress.id)

        elif rule == LB_RULE:
            self.lb_rule.delete(self.apiclient)
        else:
            self.nat_rule.delete(self.apiclient)

        self.logger.debug("Releasing IP %s from account %s" % (self.ipaddress.ipaddress.ipaddress, self.account.name))
        self.ipaddress.delete(self.apiclient)

        return
