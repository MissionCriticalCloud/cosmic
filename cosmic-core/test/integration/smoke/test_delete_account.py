import time

from nose.plugins.attrib import attr

from marvin.cloudstackException import CloudstackAPIException
from marvin.cloudstackTestCase import cloudstackTestCase
from marvin.lib.base import (
    NATRule,
    LoadBalancerRule,
    VirtualMachine,
    Account
)
from marvin.lib.common import (
    list_routers,
    list_nat_rules,
    list_lb_rules,
    list_configurations,
    list_public_ip,
    get_template,
    get_zone,
    get_domain,
    get_default_virtual_machine_offering
)
from marvin.lib.utils import cleanup_resources
from marvin.utils.MarvinLog import MarvinLog


class TestDeleteAccount(cloudstackTestCase):
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

        # Create an account, network, VM and IP addresses
        self.account = Account.create(
            self.api_client,
            self.services["account"],
            admin=True,
            domainid=self.domain.id
        )
        self.service_offering = get_default_virtual_machine_offering(self.api_client)
        self.vm_1 = VirtualMachine.create(
            self.api_client,
            self.services["virtual_machine"],
            templateid=template.id,
            accountid=self.account.name,
            domainid=self.account.domainid,
            serviceofferingid=self.service_offering.id
        )

        src_nat_ip_addrs = list_public_ip(
            self.api_client,
            account=self.account.name,
            domainid=self.account.domainid
        )

        try:
            src_nat_ip_addr = src_nat_ip_addrs[0]
        except Exception as e:
            self.fail("SSH failed for VM with IP: %s %s" %
                      (src_nat_ip_addr.ipaddress, e))

        self.lb_rule = LoadBalancerRule.create(
            self.api_client,
            self.services["lbrule"],
            src_nat_ip_addr.id,
            self.account.name
        )
        self.lb_rule.assign(self.api_client, [self.vm_1])

        self.nat_rule = NATRule.create(
            self.api_client,
            self.vm_1,
            self.services["natrule"],
            src_nat_ip_addr.id
        )
        self.cleanup = []
        return

    @attr(tags=['advanced'])
    def test_01_delete_account(self):
        """Test for delete account"""

        # Validate the Following
        # 1. after account.cleanup.interval (global setting)
        #    time all the PF/LB rules should be deleted
        # 2. verify that list(LoadBalancer/PortForwarding)Rules
        #    API does not return any rules for the account
        # 3. The domR should have been expunged for this account

        self.account.delete(self.api_client)
        interval = list_configurations(
            self.api_client,
            name='account.cleanup.interval'
        )
        self.assertEqual(
            isinstance(interval, list),
            True,
            "Check if account.cleanup.interval config present"
        )
        # Sleep to ensure that all resources are deleted
        time.sleep(int(interval[0].value))

        # ListLoadBalancerRules should not list
        # associated rules with deleted account
        # Unable to find account testuser1 in domain 1 : Exception
        try:
            list_lb_rules(
                self.api_client,
                account=self.account.name,
                domainid=self.account.domainid
            )
        except CloudstackAPIException:
            self.logger.debug("Port Forwarding Rule is deleted")

        # ListPortForwardingRules should not
        # list associated rules with deleted account
        try:
            list_nat_rules(
                self.api_client,
                account=self.account.name,
                domainid=self.account.domainid
            )
        except CloudstackAPIException:
            self.logger.debug("NATRule is deleted")

        # Retrieve router for the user account
        try:
            routers = list_routers(
                self.api_client,
                account=self.account.name,
                domainid=self.account.domainid
            )
            self.assertEqual(
                routers,
                None,
                "Check routers are properly deleted."
            )
        except CloudstackAPIException:
            self.logger.debug("Router is deleted")

        except Exception as e:
            raise Exception(
                "Encountered %s raised while fetching routers for account: %s" %
                (e, self.account.name))
        return

    def tearDown(self):
        cleanup_resources(self.api_client, self.cleanup)
        return
