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

        self.apiclient = self.testClient.getApiClient()
        self.services = self.testClient.getParsedTestDataConfig()

        # Get Zone, Domain and templates
        self.domain = get_domain(self.apiclient)
        self.zone = get_zone(self.apiclient, self.testClient.getZoneForTests())
        template = get_template(
            self.apiclient,
            self.zone.id
        )
        self.services["virtual_machine"]["zoneid"] = self.zone.id

        # Create an account, network, VM and IP addresses
        self.account = Account.create(
            self.apiclient,
            self.services["account"],
            admin=True,
            domainid=self.domain.id
        )
        self.service_offering = get_default_virtual_machine_offering(self.apiclient)
        self.vm_1 = VirtualMachine.create(
            self.apiclient,
            self.services["virtual_machine"],
            templateid=template.id,
            accountid=self.account.name,
            domainid=self.account.domainid,
            serviceofferingid=self.service_offering.id
        )

        src_nat_ip_addrs = list_public_ip(
            self.apiclient,
            account=self.account.name,
            domainid=self.account.domainid
        )

        try:
            src_nat_ip_addr = src_nat_ip_addrs[0]
        except Exception as e:
            self.fail("SSH failed for VM with IP: %s %s" %
                      (src_nat_ip_addr.ipaddress, e))

        self.lb_rule = LoadBalancerRule.create(
            self.apiclient,
            self.services["lbrule"],
            src_nat_ip_addr.id,
            self.account.name
        )
        self.lb_rule.assign(self.apiclient, [self.vm_1])

        self.nat_rule = NATRule.create(
            self.apiclient,
            self.vm_1,
            self.services["natrule"],
            src_nat_ip_addr.id
        )
        self.cleanup = []
        return

    @attr(tags=["advanced", "advancedns", "smoke"], required_hardware="false")
    def test_delete_account(self):
        """Test for delete account"""

        # Validate the Following
        # 1. after account.cleanup.interval (global setting)
        #    time all the PF/LB rules should be deleted
        # 2. verify that list(LoadBalancer/PortForwarding)Rules
        #    API does not return any rules for the account
        # 3. The domR should have been expunged for this account

        self.account.delete(self.apiclient)
        interval = list_configurations(
            self.apiclient,
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
                self.apiclient,
                account=self.account.name,
                domainid=self.account.domainid
            )
        except CloudstackAPIException:
            self.logger.debug("Port Forwarding Rule is deleted")

        # ListPortForwardingRules should not
        # list associated rules with deleted account
        try:
            list_nat_rules(
                self.apiclient,
                account=self.account.name,
                domainid=self.account.domainid
            )
        except CloudstackAPIException:
            self.logger.debug("NATRule is deleted")

        # Retrieve router for the user account
        try:
            routers = list_routers(
                self.apiclient,
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
        cleanup_resources(self.apiclient, self.cleanup)
        return
