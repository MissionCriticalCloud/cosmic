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

        self.api_client = self.testClient.getApiClient()
        self.services = self.testClient.getParsedTestDataConfig()

        # Get Zone, Domain and templates
        self.domain = get_domain(self.api_client)
        self.zone = get_zone(self.api_client, self.testClient.getZoneForTests())
        template = get_template(
            self.api_client,
            self.zone.id
        )
        self.services["virtual_machine"]["zoneid"] = self.zone.id

        # Create an account, network, VM, Port forwarding rule, LB rules
        self.account = Account.create(
            self.api_client,
            self.services["account"],
            admin=True,
            domainid=self.domain.id
        )

        self.service_offering = get_default_virtual_machine_offering(self.api_client)

        self.virtual_machine = VirtualMachine.create(
            self.api_client,
            self.services["virtual_machine"],
            templateid=template.id,
            accountid=self.account.name,
            domainid=self.account.domainid,
            serviceofferingid=self.service_offering.id
        )

        self.ip_address = PublicIPAddress.create(
            self.api_client,
            self.account.name,
            self.zone.id,
            self.account.domainid
        )

        ip_addrs = list_public_ip(
            self.api_client,
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
            self.api_client,
            self.virtual_machine,
            self.services["natrule"],
            self.ip_addr.id
        )
        self.lb_rule = LoadBalancerRule.create(
            self.api_client,
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
        cleanup_resources(self.api_client, self.cleanup)

    @attr(tags=['advanced'])
    def test_01_release_ip(self):
        """Test for release public IP address"""

        self.logger.debug("Deleting Public IP : %s" % self.ip_addr.id)

        self.ip_address.delete(self.api_client)

        retriesCount = 10
        isIpAddressDisassociated = False
        while retriesCount > 0:
            listResponse = list_public_ip(
                self.api_client,
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
                self.api_client,
                id=self.nat_rule.id
            )
            self.logger.debug("List NAT Rule response" + str(list_nat_rule))
        except CloudstackAPIException:
            self.logger.debug("Port Forwarding Rule is deleted")

        # listLoadBalancerRules should not list
        # associated rules with Public IP address
        try:
            list_lb_rule = list_lb_rules(
                self.api_client,
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
