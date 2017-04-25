from nose.plugins.attrib import attr

from marvin.cloudstackException import CloudstackAPIException
from marvin.cloudstackTestCase import cloudstackTestCase
from marvin.codes import FAILED
from marvin.lib.base import (
    NATRule,
    FireWallRule,
    VirtualMachine,
    PublicIPAddress,
    Account
)
from marvin.lib.common import (
    list_nat_rules,
    list_public_ip,
    get_template,
    get_zone,
    get_domain,
    get_default_virtual_machine_offering
)
from marvin.lib.utils import cleanup_resources
from marvin.utils.MarvinLog import MarvinLog
from marvin.utils.SshClient import SshClient


class TestPortForwarding(cloudstackTestCase):
    @classmethod
    def setUpClass(cls):
        cls.logger = MarvinLog(MarvinLog.LOGGER_TEST).get_logger()

        testClient = super(TestPortForwarding, cls).getClsTestClient()
        cls.apiclient = testClient.getApiClient()
        cls.services = testClient.getParsedTestDataConfig()
        cls.hypervisor = testClient.getHypervisorInfo()
        # Get Zone, Domain and templates
        cls.domain = get_domain(cls.apiclient)
        cls.zone = get_zone(cls.apiclient, testClient.getZoneForTests())
        template = get_template(
            cls.apiclient,
            cls.zone.id
        )
        if template == FAILED:
            assert False, "get_template() failed to return template with description %s" % cls.services[
                "ostype"]

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
        cls._cleanup = [
            cls.virtual_machine,
            cls.account
        ]

    def setUp(self):
        self.apiclient = self.testClient.getApiClient()
        self.cleanup = []
        return

    @classmethod
    def tearDownClass(cls):
        try:
            cls.apiclient = super(
                TestPortForwarding,
                cls).getClsTestClient().getApiClient()
            cleanup_resources(cls.apiclient, cls._cleanup)
        except Exception as e:
            raise Exception("Warning: Exception during cleanup : %s" % e)

    def tearDown(self):
        cleanup_resources(self.apiclient, self.cleanup)
        return

    @attr(tags=["advanced", "advancedns", "smoke"], required_hardware="true")
    def test_01_port_fwd_on_src_nat(self):
        """Test for port forwarding on source NAT"""

        # Validate the following:
        # 1. listPortForwarding rules API should return the added PF rule
        # 2. attempt to do an ssh into the  user VM through the sourceNAT

        src_nat_ip_addrs = list_public_ip(
            self.apiclient,
            account=self.account.name,
            domainid=self.account.domainid
        )

        self.assertEqual(
            isinstance(src_nat_ip_addrs, list),
            True,
            "Check list response returns a valid list"
        )
        src_nat_ip_addr = src_nat_ip_addrs[0]

        # Check if VM is in Running state before creating NAT rule
        vm_response = VirtualMachine.list(
            self.apiclient,
            id=self.virtual_machine.id
        )

        self.assertEqual(
            isinstance(vm_response, list),
            True,
            "Check list VM returns a valid list"
        )

        self.assertNotEqual(
            len(vm_response),
            0,
            "Check Port Forwarding Rule is created"
        )
        self.assertEqual(
            vm_response[0].state,
            'Running',
            "VM state should be Running before creating a NAT rule."
        )
        # Open up firewall port for SSH
        FireWallRule.create(
            self.apiclient,
            ipaddressid=src_nat_ip_addr.id,
            protocol=self.services["natrule"]["protocol"],
            cidrlist=['0.0.0.0/0'],
            startport=self.services["natrule"]["publicport"],
            endport=self.services["natrule"]["publicport"]
        )

        # Create NAT rule
        nat_rule = NATRule.create(
            self.apiclient,
            self.virtual_machine,
            self.services["natrule"],
            src_nat_ip_addr.id
        )

        list_nat_rule_response = list_nat_rules(
            self.apiclient,
            id=nat_rule.id
        )
        self.assertEqual(
            isinstance(list_nat_rule_response, list),
            True,
            "Check list response returns a valid list"
        )

        self.assertNotEqual(
            len(list_nat_rule_response),
            0,
            "Check Port Forwarding Rule is created"
        )
        self.assertEqual(
            list_nat_rule_response[0].id,
            nat_rule.id,
            "Check Correct Port forwarding Rule is returned"
        )
        # SSH virtual machine to test port forwarding
        try:
            self.logger.debug("SSHing into VM with IP address %s with NAT IP %s" %
                         (
                             self.virtual_machine.ipaddress,
                             src_nat_ip_addr.ipaddress
                         ))

            self.virtual_machine.get_ssh_client(src_nat_ip_addr.ipaddress)
            vm_response = VirtualMachine.list(
                self.apiclient,
                id=self.virtual_machine.id
            )
            if vm_response[0].state != 'Running':
                self.fail(
                    "State of VM : %s is not found to be Running" % str(
                        self.virtual_machine.ipaddress))

        except Exception as e:
            self.fail(
                "SSH Access failed for %s: %s" %
                (self.virtual_machine.ipaddress, e)
            )

        try:
            nat_rule.delete(self.apiclient)
        except Exception as e:
            self.fail("NAT Rule Deletion Failed: %s" % e)

        # NAT rule listing should fail as the nat rule does not exist
        with self.assertRaises(Exception):
            list_nat_rules(self.apiclient,
                           id=nat_rule.id)

        # Check if the Public SSH port is inaccessible
        with self.assertRaises(Exception):
            self.logger.debug(
                "SSHing into VM with IP address %s after NAT rule deletion" %
                self.virtual_machine.ipaddress)

            SshClient(
                src_nat_ip_addr.ipaddress,
                self.virtual_machine.ssh_port,
                self.virtual_machine.username,
                self.virtual_machine.password,
                retries=2,
                delay=0
            )
        return

    @attr(tags=["advanced", "advancedns", "smoke"], required_hardware="true")
    def test_02_port_fwd_on_non_src_nat(self):
        """Test for port forwarding on non source NAT"""

        # Validate the following:
        # 1. listPortForwardingRules should not return the deleted rule anymore
        # 2. attempt to do ssh should now fail

        ip_address = PublicIPAddress.create(
            self.apiclient,
            self.account.name,
            self.zone.id,
            self.account.domainid,
            self.services["virtual_machine"]
        )
        self.cleanup.append(ip_address)

        # Check if VM is in Running state before creating NAT rule
        vm_response = VirtualMachine.list(
            self.apiclient,
            id=self.virtual_machine.id
        )

        self.assertEqual(
            isinstance(vm_response, list),
            True,
            "Check list VM returns a valid list"
        )

        self.assertNotEqual(
            len(vm_response),
            0,
            "Check Port Forwarding Rule is created"
        )
        self.assertEqual(
            vm_response[0].state,
            'Running',
            "VM state should be Running before creating a NAT rule."
        )
        # Open up firewall port for SSH
        FireWallRule.create(
            self.apiclient,
            ipaddressid=ip_address.ipaddress.id,
            protocol=self.services["natrule"]["protocol"],
            cidrlist=['0.0.0.0/0'],
            startport=self.services["natrule"]["publicport"],
            endport=self.services["natrule"]["publicport"]
        )
        # Create NAT rule
        nat_rule = NATRule.create(
            self.apiclient,
            self.virtual_machine,
            self.services["natrule"],
            ip_address.ipaddress.id
        )
        # Validate the following:
        # 1. listPortForwardingRules should not return the deleted rule anymore
        # 2. attempt to do ssh should now fail

        list_nat_rule_response = list_nat_rules(
            self.apiclient,
            id=nat_rule.id
        )
        self.assertEqual(
            isinstance(list_nat_rule_response, list),
            True,
            "Check list response returns a valid list"
        )
        self.assertNotEqual(
            len(list_nat_rule_response),
            0,
            "Check Port Forwarding Rule is created"
        )
        self.assertEqual(
            list_nat_rule_response[0].id,
            nat_rule.id,
            "Check Correct Port forwarding Rule is returned"
        )

        try:
            self.logger.debug("SSHing into VM with IP address %s with NAT IP %s" %
                         (
                             self.virtual_machine.ipaddress,
                             ip_address.ipaddress.ipaddress
                         ))
            self.virtual_machine.get_ssh_client(ip_address.ipaddress.ipaddress)
        except Exception as e:
            self.fail(
                "SSH Access failed for %s: %s" %
                (self.virtual_machine.ipaddress, e)
            )

        nat_rule.delete(self.apiclient)

        try:
            list_nat_rule_response = list_nat_rules(
                self.apiclient,
                id=nat_rule.id
            )
        except CloudstackAPIException:
            self.logger.debug("Nat Rule is deleted")

        # Check if the Public SSH port is inaccessible
        with self.assertRaises(Exception):
            self.logger.debug(
                "SSHing into VM with IP address %s after NAT rule deletion" %
                self.virtual_machine.ipaddress)

            SshClient(
                ip_address.ipaddress.ipaddress,
                self.virtual_machine.ssh_port,
                self.virtual_machine.username,
                self.virtual_machine.password,
                retries=2,
                delay=0
            )
        return
