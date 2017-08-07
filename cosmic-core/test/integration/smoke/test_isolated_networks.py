from marvin.cloudstackTestCase import cloudstackTestCase
from marvin.lib.base import (
    EgressFireWallRule,
    NATRule,
    FireWallRule,
    VirtualMachine,
    Network,
    Account
)
from marvin.lib.common import (
    list_nat_rules,
    list_public_ip,
    list_routers,
    get_default_virtual_machine_offering,
    get_template,
    get_zone,
    get_domain,
    get_default_isolated_network_offering_with_egress,
    get_default_isolated_network_offering
)
from marvin.lib.utils import cleanup_resources
from marvin.utils.MarvinLog import MarvinLog
from nose.plugins.attrib import attr


class TestIsolatedNetworks(cloudstackTestCase):
    # TODO: refactor these and others in the same file to super class
    HTTP_COMMAND = "wget -t 1 -T 5 --no-check-certificate https://www.google.com"
    HTTP_CHECK_STRING = 'HTTP request sent, awaiting response... 200 OK'
    HTTP_ASSERT_SUCCESS_MESSAGE = 'Attempt to retrieve index page from google.com on gateway should be successful!'
    HTTP_ASSERT_FAILURE_MESSAGE = 'Attempt to retrieve index page from google.com on gateway should NOT be successful!'

    @classmethod
    def setUpClass(cls):

        cls.logger = MarvinLog(MarvinLog.LOGGER_TEST).get_logger()

        cls.testClient = super(TestIsolatedNetworks, cls).getClsTestClient()
        cls.api_client = cls.testClient.getApiClient()

        cls.services = cls.testClient.getParsedTestDataConfig()
        # Get Zone, Domain and templates
        cls.domain = get_domain(cls.api_client)
        cls.zone = get_zone(cls.api_client, cls.testClient.getZoneForTests())
        cls.services['mode'] = cls.zone.networktype
        cls.template = get_template(
            cls.api_client,
            cls.zone.id
        )
        cls.services["virtual_machine"]["zoneid"] = cls.zone.id

        # Create an account, network, VM and IP addresses
        cls.account = Account.create(
            cls.api_client,
            cls.services["account"],
            admin=True,
            domainid=cls.domain.id
        )
        cls.service_offering = get_default_virtual_machine_offering(cls.api_client)

        cls.services["egress_443"] = {
            "startport": 443,
            "endport": 443,
            "protocol": "TCP",
            "cidrlist": ["0.0.0.0/0"]
        }

        cls._cleanup = [cls.account]

        return

    @classmethod
    def tearDownClass(cls):
        try:
            cleanup_resources(cls.api_client, cls._cleanup)
        except Exception as e:
            raise Exception("Warning: Exception during cleanup : %s" % e)
        return

    def setUp(self):
        self.apiclient = self.testClient.getApiClient()
        self.cleanup = []
        return

    def tearDown(self):
        try:
            cleanup_resources(self.apiclient, self.cleanup)
        except Exception as e:
            raise Exception("Warning: Exception during cleanup : %s" % e)
        return

    @attr(tags=['advanced'])
    def test_01_isolate_network_FW_PF_default_routes_egress_true(self):
        """ Test redundant router internals """
        self.logger.debug("Starting test_01_isolate_network_FW_PF_default_routes_egress_true...")

        network_offering_egress_true = get_default_isolated_network_offering_with_egress(self.apiclient)

        self.logger.debug("Creating Network with Network Offering ID %s" % network_offering_egress_true.id)
        network = Network.create(self.apiclient,
                                 self.services["network"],
                                 accountid=self.account.name,
                                 domainid=self.account.domainid,
                                 networkofferingid=network_offering_egress_true.id,
                                 zoneid=self.zone.id)

        self.logger.debug("Deploying Virtual Machine on Network %s" % network.id)
        virtual_machine = VirtualMachine.create(self.apiclient,
                                                self.services["virtual_machine"],
                                                templateid=self.template.id,
                                                accountid=self.account.name,
                                                domainid=self.domain.id,
                                                serviceofferingid=self.service_offering.id,
                                                networkids=[str(network.id)])

        self.logger.debug("Deployed VM in network: %s" % network.id)

        self.cleanup.insert(0, network)
        self.cleanup.insert(0, virtual_machine)

        self.logger.debug("Starting test_isolate_network_FW_PF_default_routes...")
        routers = list_routers(
            self.apiclient,
            account=self.account.name,
            domainid=self.account.domainid
        )

        self.assertEqual(
            isinstance(routers, list),
            True,
            "Check for list routers response return valid data"
        )

        self.assertNotEqual(
            len(routers),
            0,
            "Check list router response"
        )

        router = routers[0]

        self.assertEqual(
            router.state,
            'Running',
            "Check list router response for router state"
        )

        public_ips = list_public_ip(
            self.apiclient,
            account=self.account.name,
            domainid=self.account.domainid,
            zoneid=self.zone.id
        )

        self.assertEqual(
            isinstance(public_ips, list),
            True,
            "Check for list public IPs response return valid data"
        )

        public_ip = public_ips[0]

        self.logger.debug("Creating Firewall rule for VM ID: %s" % virtual_machine.id)
        FireWallRule.create(
            self.apiclient,
            ipaddressid=public_ip.id,
            protocol=self.services["natrule_ssh"]["protocol"],
            cidrlist=['0.0.0.0/0'],
            startport=self.services["natrule_ssh"]["publicport"],
            endport=self.services["natrule_ssh"]["publicport"]
        )

        self.logger.debug("Creating NAT rule for VM ID: %s" % virtual_machine.id)
        # Create NAT rule
        nat_rule = NATRule.create(
            self.apiclient,
            virtual_machine,
            self.services["natrule_ssh"],
            public_ip.id
        )

        nat_rules = list_nat_rules(
            self.apiclient,
            id=nat_rule.id
        )
        self.assertEqual(
            isinstance(nat_rules, list),
            True,
            "Check for list NAT rules response return valid data"
        )
        self.assertEqual(
            nat_rules[0].state,
            'Active',
            "Check list port forwarding rules"
        )

        # Test SSH after closing port 22
        expected = 1
        ssh_command = "ping -c 3 8.8.8.8"
        check_string = "3 packets received"
        result = self.check_router_command(virtual_machine, nat_rule.ipaddress, ssh_command, check_string, self)

        self.assertEqual(
            result,
            expected,
            "Ping to outside world from VM should be successful!"
        )

        expected = 1
        ssh_command = self.HTTP_COMMAND
        check_string = self.HTTP_CHECK_STRING
        result = self.check_router_command(virtual_machine, nat_rule.ipaddress, ssh_command, check_string, self)

        self.assertEqual(
            result,
            expected,
            self.HTTP_ASSERT_SUCCESS_MESSAGE
        )

        EgressFireWallRule.create(
            self.apiclient,
            networkid=network.id,
            protocol=self.services["egress_443"]["protocol"],
            startport=self.services["egress_443"]["startport"],
            endport=self.services["egress_443"]["endport"],
            cidrlist=self.services["egress_443"]["cidrlist"]
        )

        expected = 0
        result = self.check_router_command(virtual_machine, nat_rule.ipaddress, ssh_command, check_string, self)

        self.assertEqual(
            result,
            expected,
            self.HTTP_ASSERT_FAILURE_MESSAGE
        )

        return

    @attr(tags=['advanced'])
    def test_02_isolate_network_FW_PF_default_routes_egress_false(self):
        """ Test redundant router internals """
        self.logger.debug("Starting test_02_isolate_network_FW_PF_default_routes_egress_false...")

        network_offering_egress_false = get_default_isolated_network_offering(self.apiclient)

        self.logger.debug("Creating Network with Network Offering ID %s" % network_offering_egress_false.id)
        network = Network.create(self.apiclient,
                                 self.services["network"],
                                 accountid=self.account.name,
                                 domainid=self.account.domainid,
                                 networkofferingid=network_offering_egress_false.id,
                                 zoneid=self.zone.id)

        self.logger.debug("Deploying Virtual Machine on Network %s" % network.id)
        virtual_machine = VirtualMachine.create(self.apiclient,
                                                self.services["virtual_machine"],
                                                templateid=self.template.id,
                                                accountid=self.account.name,
                                                domainid=self.domain.id,
                                                serviceofferingid=self.service_offering.id,
                                                networkids=[str(network.id)])

        self.logger.debug("Deployed VM in network: %s" % network.id)

        self.cleanup.insert(0, network)
        self.cleanup.insert(0, virtual_machine)

        self.logger.debug("Starting test_isolate_network_FW_PF_default_routes...")
        routers = list_routers(
            self.apiclient,
            account=self.account.name,
            domainid=self.account.domainid
        )

        self.assertEqual(
            isinstance(routers, list),
            True,
            "Check for list routers response return valid data"
        )

        self.assertNotEqual(
            len(routers),
            0,
            "Check list router response"
        )

        router = routers[0]

        self.assertEqual(
            router.state,
            'Running',
            "Check list router response for router state"
        )

        public_ips = list_public_ip(
            self.apiclient,
            account=self.account.name,
            domainid=self.account.domainid,
            zoneid=self.zone.id
        )

        self.assertEqual(
            isinstance(public_ips, list),
            True,
            "Check for list public IPs response return valid data"
        )

        public_ip = public_ips[0]

        self.logger.debug("Creating Firewall rule for VM ID: %s" % virtual_machine.id)
        FireWallRule.create(
            self.apiclient,
            ipaddressid=public_ip.id,
            protocol=self.services["natrule_ssh"]["protocol"],
            cidrlist=['0.0.0.0/0'],
            startport=self.services["natrule_ssh"]["publicport"],
            endport=self.services["natrule_ssh"]["publicport"]
        )

        self.logger.debug("Creating NAT rule for VM ID: %s" % virtual_machine.id)
        # Create NAT rule
        nat_rule = NATRule.create(
            self.apiclient,
            virtual_machine,
            self.services["natrule_ssh"],
            public_ip.id
        )

        nat_rules = list_nat_rules(
            self.apiclient,
            id=nat_rule.id
        )
        self.assertEqual(
            isinstance(nat_rules, list),
            True,
            "Check for list NAT rules response return valid data"
        )
        self.assertEqual(
            nat_rules[0].state,
            'Active',
            "Check list port forwarding rules"
        )

        expected = 0
        ssh_command = "ping -c 3 8.8.8.8"
        check_string = "3 packets received"
        result = self.check_router_command(virtual_machine, nat_rule.ipaddress, ssh_command, check_string, self)

        self.assertEqual(
            result,
            expected,
            "Ping to outside world from VM should NOT be successful"
        )

        expected = 0
        ssh_command = self.HTTP_COMMAND
        check_string = self.HTTP_CHECK_STRING
        result = self.check_router_command(virtual_machine, nat_rule.ipaddress, ssh_command, check_string, self)

        self.assertEqual(
            result,
            expected,
            self.HTTP_ASSERT_FAILURE_MESSAGE
        )

        EgressFireWallRule.create(
            self.apiclient,
            networkid=network.id,
            protocol=self.services["egress_443"]["protocol"],
            startport=self.services["egress_443"]["startport"],
            endport=self.services["egress_443"]["endport"],
            cidrlist=self.services["egress_443"]["cidrlist"]
        )

        expected = 1
        result = self.check_router_command(virtual_machine, nat_rule.ipaddress, ssh_command, check_string, self)

        self.assertEqual(
            result,
            expected,
            self.HTTP_ASSERT_SUCCESS_MESSAGE
        )

        return

    def check_router_command(self, virtual_machine, public_ip, ssh_command, check_string, test_case, retries=5):
        result = 'failed'
        try:
            ssh = virtual_machine.get_ssh_client(ipaddress=public_ip, retries=retries)
            result = str(ssh.execute(ssh_command))
        except Exception as e:
            test_case.fail("Failed to SSH into the Virtual Machine: %s" % e)

        self.logger.debug("Result from SSH into the Virtual Machine: %s" % result)
        return result.count(check_string)
