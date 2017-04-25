import logging

from nose.plugins.attrib import attr
from marvin.cloudstackTestCase import cloudstackTestCase

from marvin.lib.base import (
    EgressFireWallRule,
    NATRule,
    FireWallRule,
    VirtualMachine,
    Network,
    NetworkOffering,
    Account
)
from marvin.lib.common import (
    list_nat_rules,
    list_publicIP,
    list_routers,
    get_default_virtual_machine_offering,
    get_template,
    get_zone,
    get_domain,
    list_vlan_ipranges,
    list_networks
)
from marvin.lib.utils import cleanup_resources


class TestIsolatedNetworks(cloudstackTestCase):
    # TODO: refactor these and others in the same file to super class
    HTTP_COMMAND = "wget -t 1 -T 5 %s:8080"
    HTTP_CHECK_STRING = 'HTTP request sent, awaiting response... 200 OK'
    HTTP_ASSERT_SUCCESS_MESSAGE = 'Attempt to retrieve index page from cloud-init on gateway should be successful!'
    HTTP_ASSERT_FAILURE_MESSAGE = 'Attempt to retrieve index page from cloud-init on gateway should NOT be successful!'

    @classmethod
    def setUpClass(cls):

        cls.logger = logging.getLogger('TestIsolatedNetworks')
        cls.stream_handler = logging.StreamHandler()
        cls.logger.setLevel(logging.DEBUG)
        cls.logger.addHandler(cls.stream_handler)

        cls.testClient = super(TestIsolatedNetworks, cls).getClsTestClient()
        cls.api_client = cls.testClient.getApiClient()

        cls.services = cls.testClient.getParsedTestDataConfig()
        # Get Zone, Domain and templates
        cls.domain = get_domain(cls.api_client)
        cls.zone = get_zone(cls.api_client, cls.testClient.getZoneForTests())
        cls.services['mode'] = cls.zone.networktype
        cls.template = get_template(
            cls.api_client,
            cls.zone.id,
            cls.services["ostype"]
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

        cls.services["network_offering_egress_true"] = cls.services["network_offering"].copy()
        cls.services["network_offering_egress_true"]["egress_policy"] = "true"

        cls.services["network_offering_egress_false"] = cls.services["network_offering"].copy()
        cls.services["network_offering_egress_false"]["egress_policy"] = "false"

        cls.services["egress_8080"] = {
            "startport": 8080,
            "endport": 8080,
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

    @attr(tags=["advanced", "advancedns", "ssh"], required_hardware="true")
    def test_01_isolate_network_FW_PF_default_routes_egress_true(self):
        """ Test redundant router internals """
        self.logger.debug("Starting test_01_isolate_network_FW_PF_default_routes_egress_true...")

        self.logger.debug("Creating Network Offering with default egress TRUE")
        network_offering_egress_true = NetworkOffering.create(self.apiclient,
                                                              self.services["network_offering_egress_true"],
                                                              conservemode=True)

        network_offering_egress_true.update(self.apiclient, state='Enabled')

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

        self.cleanup.insert(0, network_offering_egress_true)
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

        public_ips = list_publicIP(
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
            protocol=self.services["natrule"]["protocol"],
            cidrlist=['0.0.0.0/0'],
            startport=self.services["natrule"]["publicport"],
            endport=self.services["natrule"]["publicport"]
        )

        self.logger.debug("Creating NAT rule for VM ID: %s" % virtual_machine.id)
        # Create NAT rule
        nat_rule = NATRule.create(
            self.apiclient,
            virtual_machine,
            self.services["natrule"],
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
        gateway = find_public_gateway(self)
        ssh_command = "ping -c 3 %s" % gateway
        check_string = "3 packets received"
        result = check_router_command(virtual_machine, nat_rule.ipaddress, ssh_command, check_string, self)

        self.assertEqual(
            result,
            expected,
            "Ping to outside world from VM should be successful!"
        )

        expected = 1
        ssh_command = self.HTTP_COMMAND % gateway
        check_string = self.HTTP_CHECK_STRING
        result = check_router_command(virtual_machine, nat_rule.ipaddress, ssh_command, check_string, self)

        self.assertEqual(
            result,
            expected,
            self.HTTP_ASSERT_SUCCESS_MESSAGE
        )

        EgressFireWallRule.create(
            self.apiclient,
            networkid=network.id,
            protocol=self.services["egress_8080"]["protocol"],
            startport=self.services["egress_8080"]["startport"],
            endport=self.services["egress_8080"]["endport"],
            cidrlist=self.services["egress_8080"]["cidrlist"]
        )

        expected = 0
        result = check_router_command(virtual_machine, nat_rule.ipaddress, ssh_command, check_string, self)

        self.assertEqual(
            result,
            expected,
            self.HTTP_ASSERT_FAILURE_MESSAGE
        )

        return

    @attr(tags=["advanced", "advancedns", "ssh"], required_hardware="true")
    def test_02_isolate_network_FW_PF_default_routes_egress_false(self):
        """ Test redundant router internals """
        self.logger.debug("Starting test_02_isolate_network_FW_PF_default_routes_egress_false...")

        self.logger.debug("Creating Network Offering with default egress FALSE")
        network_offering_egress_false = NetworkOffering.create(self.apiclient,
                                                               self.services["network_offering_egress_false"],
                                                               conservemode=True)

        network_offering_egress_false.update(self.apiclient, state='Enabled')

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

        self.cleanup.insert(0, network_offering_egress_false)
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

        public_ips = list_publicIP(
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
            protocol=self.services["natrule"]["protocol"],
            cidrlist=['0.0.0.0/0'],
            startport=self.services["natrule"]["publicport"],
            endport=self.services["natrule"]["publicport"]
        )

        self.logger.debug("Creating NAT rule for VM ID: %s" % virtual_machine.id)
        # Create NAT rule
        nat_rule = NATRule.create(
            self.apiclient,
            virtual_machine,
            self.services["natrule"],
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
        gateway = find_public_gateway(self)
        ssh_command = "ping -c 3 %s" % gateway
        check_string = "3 packets received"
        result = check_router_command(virtual_machine, nat_rule.ipaddress, ssh_command, check_string, self)

        self.assertEqual(
            result,
            expected,
            "Ping to outside world from VM should NOT be successful"
        )

        expected = 0
        ssh_command = self.HTTP_COMMAND % gateway
        check_string = self.HTTP_CHECK_STRING
        result = check_router_command(virtual_machine, nat_rule.ipaddress, ssh_command, check_string, self)

        self.assertEqual(
            result,
            expected,
            self.HTTP_ASSERT_FAILURE_MESSAGE
        )

        EgressFireWallRule.create(
            self.apiclient,
            networkid=network.id,
            protocol=self.services["egress_8080"]["protocol"],
            startport=self.services["egress_8080"]["startport"],
            endport=self.services["egress_8080"]["endport"],
            cidrlist=self.services["egress_8080"]["cidrlist"]
        )

        expected = 1
        result = check_router_command(virtual_machine, nat_rule.ipaddress, ssh_command, check_string, self)

        self.assertEqual(
            result,
            expected,
            self.HTTP_ASSERT_SUCCESS_MESSAGE
        )

        return


def check_router_command(virtual_machine, public_ip, ssh_command, check_string, test_case, retries=5):
    result = 'failed'
    try:
        ssh = virtual_machine.get_ssh_client(ipaddress=public_ip, retries=retries)
        result = str(ssh.execute(ssh_command))
    except Exception as e:
        test_case.fail("Failed to SSH into the Virtual Machine: %s" % e)

    logging.debug("Result from SSH into the Virtual Machine: %s" % result)
    return result.count(check_string)


def find_public_gateway(test_case):
    networks = list_networks(test_case.apiclient,
                             zoneid=test_case.zone.id,
                             listall=True,
                             issystem=True,
                             traffictype="Public")
    test_case.logger.debug('::: Public Networks ::: ==> %s' % networks)

    test_case.assertTrue(len(networks) == 1, "Test expects only 1 Public network but found -> '%s'" % len(networks))

    ip_ranges = list_vlan_ipranges(test_case.apiclient,
                                   zoneid=test_case.zone.id,
                                   networkid=networks[0].id)
    test_case.logger.debug('::: IP Ranges ::: ==> %s' % ip_ranges)

    test_case.assertTrue(len(ip_ranges) == 1, "Test expects only 1 VLAN IP Range network but found -> '%s'" % len(ip_ranges))
    test_case.assertIsNotNone(ip_ranges[0].gateway, "The network with id -> '%s' returned an IP Range with a None gateway. Please check your Datacenter settings." % networks[0].id)

    return ip_ranges[0].gateway
