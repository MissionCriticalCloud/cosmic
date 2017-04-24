import logging
import time

from ddt import ddt, data
from nose.plugins.attrib import attr

from marvin.cloudstackAPI import rebootRouter
from marvin.cloudstackException import CloudstackAPIException
from marvin.cloudstackTestCase import cloudstackTestCase
from marvin.codes import (FAILED, STATIC_NAT_RULE, LB_RULE,
                          NAT_RULE)
from marvin.lib.base import (Account,
                             VirtualMachine,
                             ServiceOffering,
                             NATRule,
                             PublicIPAddress,
                             StaticNATRule,
                             FireWallRule,
                             Network,
                             NetworkOffering,
                             LoadBalancerRule,
                             Router)
from marvin.lib.common import (get_domain,
                               get_zone,
                               get_template,
                               list_hosts,
                               list_publicIP,
                               list_nat_rules,
                               list_routers,
                               list_virtual_machines,
                               list_lb_rules,
                               list_configurations)
from marvin.lib.utils import cleanup_resources, get_process_status
from marvin.sshClient import SshClient

logger = logging.getLogger('TestNetworkOps')
stream_handler = logging.StreamHandler()
logger.setLevel(logging.DEBUG)
logger.addHandler(stream_handler)


class TestDeleteAccount(cloudstackTestCase):
    def setUp(self):

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

        # Create an account, network, VM and IP addresses
        self.account = Account.create(
            self.apiclient,
            self.services["account"],
            admin=True,
            domainid=self.domain.id
        )
        self.service_offering = ServiceOffering.create(
            self.apiclient,
            self.services["service_offerings"]["tiny"]
        )
        self.vm_1 = VirtualMachine.create(
            self.apiclient,
            self.services["virtual_machine"],
            templateid=template.id,
            accountid=self.account.name,
            domainid=self.account.domainid,
            serviceofferingid=self.service_offering.id
        )

        src_nat_ip_addrs = list_publicIP(
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
            logger.debug("Port Forwarding Rule is deleted")

        # ListPortForwardingRules should not
        # list associated rules with deleted account
        try:
            list_nat_rules(
                self.apiclient,
                account=self.account.name,
                domainid=self.account.domainid
            )
        except CloudstackAPIException:
            logger.debug("NATRule is deleted")

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
            logger.debug("Router is deleted")

        except Exception as e:
            raise Exception(
                "Encountered %s raised while fetching routers for account: %s" %
                (e, self.account.name))
        return

    def tearDown(self):
        cleanup_resources(self.apiclient, self.cleanup)
        return


@ddt
class TestRouterRules(cloudstackTestCase):
    @classmethod
    def setUpClass(cls):

        testClient = super(TestRouterRules, cls).getClsTestClient()
        cls.apiclient = testClient.getApiClient()
        cls.services = testClient.getParsedTestDataConfig()
        # Get Zone, Domain and templates
        cls.domain = get_domain(cls.apiclient)
        cls.zone = get_zone(cls.apiclient, testClient.getZoneForTests())
        cls.hypervisor = testClient.getHypervisorInfo()
        template = get_template(
            cls.apiclient,
            cls.zone.id,
            cls.services["ostype"]
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
        cls.service_offering = ServiceOffering.create(
            cls.apiclient,
            cls.services["service_offerings"]["tiny"]
        )
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
            cls.account,
            cls.service_offering
        ]

    def setUp(self):
        self.apiclient = self.testClient.getApiClient()
        self.cleanup = []
        return

    @classmethod
    def tearDownClass(cls):
        try:
            cleanup_resources(cls.apiclient, cls._cleanup)
        except Exception as e:
            raise Exception("Warning: Exception during cleanup : %s" % e)

    def tearDown(self):
        cleanup_resources(self.apiclient, self.cleanup)
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

        logger.debug("Releasing IP %s from account %s" % (self.ipaddress.ipaddress.ipaddress, self.account.name))
        self.ipaddress.delete(self.apiclient)

        return

    @data(STATIC_NAT_RULE, NAT_RULE, LB_RULE)
    @attr(tags=["advanced", "advancedns", "smoke", "dvs"], required_hardware="true")
    def test_network_rules_acquired_public_ip(self, value):
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
        logger.debug(response)
        stringToMatch = "inet %s" % self.ipaddress.ipaddress.ipaddress
        self.assertTrue(stringToMatch in str(response), "IP address is\
                not added to the VR!")

        try:
            logger.debug("SSHing into VM with IP address %s with NAT IP %s" %
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
        logger.debug(response)
        stringToMatch = "inet %s" % self.ipaddress.ipaddress.ipaddress
        self.assertFalse(stringToMatch in str(response), "IP address is\
                not removed from VR even after disabling stat in NAT")

        # Check if the Public SSH port is inaccessible
        with self.assertRaises(Exception):
            logger.debug(
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
