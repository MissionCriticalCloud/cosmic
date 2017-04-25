import time

from nose.plugins.attrib import attr

from marvin.cloudstackException import CloudstackAPIException
from marvin.cloudstackTestCase import cloudstackTestCase
from marvin.lib.base import (
    LoadBalancerRule,
    NATRule,
    PublicIPAddress,
    VirtualMachine,
    Account
)
from marvin.lib.common import (
    list_lb_rules,
    list_nat_rules,
    list_public_ip,
    get_template,
    get_zone,
    get_domain,
    get_default_virtual_machine_offering)
from marvin.lib.utils import cleanup_resources
from marvin.utils.MarvinLog import MarvinLog
from marvin.utils.SshClient import SshClient


class TestReleaseIP(cloudstackTestCase):
    def setUp(self):
        self.logger = MarvinLog(MarvinLog.LOGGER_TEST).get_logger()

        self.apiclient = self.testClient.getApiClient()
        self.services = self.testClient.getParsedTestDataConfig()

        # Get Zone, Domain and templates
        self.domain = get_domain(self.apiclient)
        self.zone = get_zone(self.apiclient, self.testClient.getZoneForTests())
        template = get_template(
            self.apiclient,
            self.zone.id,
            self.services["ostype"]
        )
        self.services["virtual_machine"]["zoneid"] = self.zone.id

        # Create an account, network, VM, Port forwarding rule, LB rules
        self.account = Account.create(
            self.apiclient,
            self.services["account"],
            admin=True,
            domainid=self.domain.id
        )

        self.service_offering = get_default_virtual_machine_offering(self.apiclient)

        self.virtual_machine = VirtualMachine.create(
            self.apiclient,
            self.services["virtual_machine"],
            templateid=template.id,
            accountid=self.account.name,
            domainid=self.account.domainid,
            serviceofferingid=self.service_offering.id
        )

        self.ip_address = PublicIPAddress.create(
            self.apiclient,
            self.account.name,
            self.zone.id,
            self.account.domainid
        )

        ip_addrs = list_public_ip(
            self.apiclient,
            account=self.account.name,
            domainid=self.account.domainid,
            issourcenat=False
        )
        try:
            self.ip_addr = ip_addrs[0]
        except Exception as e:
            raise Exception(
                "Failed: During acquiring source NAT for account: %s, :%s" %
                (self.account.name, e))

        self.nat_rule = NATRule.create(
            self.apiclient,
            self.virtual_machine,
            self.services["natrule"],
            self.ip_addr.id
        )
        self.lb_rule = LoadBalancerRule.create(
            self.apiclient,
            self.services["lbrule"],
            self.ip_addr.id,
            accountid=self.account.name
        )
        self.cleanup = [
            self.virtual_machine,
            self.account
        ]
        return

    def tearDown(self):
        cleanup_resources(self.apiclient, self.cleanup)

    @attr(tags=["advanced", "advancedns", "smoke"], required_hardware="false")
    def test_releaseIP(self):
        """Test for release public IP address"""

        self.logger.debug("Deleting Public IP : %s" % self.ip_addr.id)

        self.ip_address.delete(self.apiclient)

        retriesCount = 10
        isIpAddressDisassociated = False
        while retriesCount > 0:
            listResponse = list_public_ip(
                self.apiclient,
                id=self.ip_addr.id
            )
            if listResponse is None:
                isIpAddressDisassociated = True
                break
            retriesCount -= 1
            time.sleep(60)
        # End while

        self.assertTrue(
            isIpAddressDisassociated,
            "Failed to disassociate IP address")

        # ListPortForwardingRules should not list
        # associated rules with Public IP address
        try:
            list_nat_rule = list_nat_rules(
                self.apiclient,
                id=self.nat_rule.id
            )
            self.logger.debug("List NAT Rule response" + str(list_nat_rule))
        except CloudstackAPIException:
            self.logger.debug("Port Forwarding Rule is deleted")

        # listLoadBalancerRules should not list
        # associated rules with Public IP address
        try:
            list_lb_rule = list_lb_rules(
                self.apiclient,
                id=self.lb_rule.id
            )
            self.logger.debug("List LB Rule response" + str(list_lb_rule))
        except CloudstackAPIException:
            self.logger.debug("Port Forwarding Rule is deleted")

        # SSH Attempt though public IP should fail
        with self.assertRaises(Exception):
            SshClient(
                self.ip_addr.ipaddress,
                self.services["natrule"]["publicport"],
                self.virtual_machine.username,
                self.virtual_machine.password,
                retries=2,
                delay=0
            )
        return
