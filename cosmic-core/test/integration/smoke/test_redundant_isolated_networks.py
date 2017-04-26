from nose.plugins.attrib import attr
from marvin.cloudstackTestCase import cloudstackTestCase

from marvin.lib.base import (
    Router,
    VirtualMachine,
    Network,
    EgressFireWallRule,
    NATRule,
    FireWallRule,
    Account
)
from marvin.lib.common import (
    list_vlan_ipranges,
    list_networks,
    list_hosts,
    list_public_ip,
    get_default_virtual_machine_offering,
    get_template,
    get_zone,
    get_domain,
    get_default_redundant_isolated_network_offering,
    get_default_redundant_isolated_network_offering_with_egress
)
from marvin.lib.utils import (
    get_process_status,
    get_host_credentials,
    cleanup_resources
)
from marvin.utils.MarvinLog import MarvinLog


class TestRedundantIsolatedNetworks(cloudstackTestCase):
    # TODO: refactor these and others in the same file to super class
    HTTP_COMMAND = "wget -t 1 -T 5 %s:8080"
    HTTP_CHECK_STRING = 'HTTP request sent, awaiting response... 200 OK'
    HTTP_ASSERT_SUCCESS_MESSAGE = 'Attempt to retrieve index page from cloud-init on gateway should be successful!'
    HTTP_ASSERT_FAILURE_MESSAGE = 'Attempt to retrieve index page from cloud-init on gateway should NOT be successful!'

    @classmethod
    def setUpClass(cls):
        cls.logger = MarvinLog(MarvinLog.LOGGER_TEST).get_logger()

        cls.testClient = super(TestRedundantIsolatedNetworks, cls).getClsTestClient()
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

        cls.services["egress_8080"] = {
            "startport": 8080,
            "endport": 8080,
            "protocol": "TCP",
            "cidrlist": ["0.0.0.0/0"]
        }

        cls.services["egress_53"] = {
            "startport": 53,
            "endport": 53,
            "protocol": "UDP",
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
            cleanup_resources(self.api_client, self.cleanup)
        except Exception as e:
            raise Exception("Warning: Exception during cleanup : %s" % e)
        return

    @attr(tags=['advanced'])
    def test_01_RVR_Network_FW_PF_SSH_default_routes_egress_true(self):
        """ Test redundant router internals """
        self.logger.debug("Starting test_01_RVR_Network_FW_PF_SSH_default_routes_egress_true...")

        network_offering_egress_true = get_default_redundant_isolated_network_offering_with_egress(self.apiclient)

        self.logger.debug("Creating network with network offering: %s" % network_offering_egress_true.id)
        network = Network.create(
            self.apiclient,
            self.services["network"],
            accountid=self.account.name,
            domainid=self.account.domainid,
            networkofferingid=network_offering_egress_true.id,
            zoneid=self.zone.id
        )
        self.logger.debug("Created network with ID: %s" % network.id)

        networks = Network.list(
            self.apiclient,
            id=network.id,
            listall=True
        )
        self.assertEqual(
            isinstance(networks, list),
            True,
            "List networks should return a valid response for created network"
        )

        self.logger.debug("Deploying VM in account: %s" % self.account.name)
        virtual_machine = VirtualMachine.create(
            self.apiclient,
            self.services["virtual_machine"],
            templateid=self.template.id,
            accountid=self.account.name,
            domainid=self.account.domainid,
            serviceofferingid=self.service_offering.id,
            networkids=[str(network.id)]
        )

        self.logger.debug("Deployed VM in network: %s" % network.id)

        self.cleanup.insert(0, network)
        self.cleanup.insert(0, virtual_machine)

        vms = VirtualMachine.list(
            self.apiclient,
            id=virtual_machine.id,
            listall=True
        )
        self.assertEqual(
            isinstance(vms, list),
            True,
            "List Vms should return a valid list"
        )
        vm = vms[0]
        self.assertEqual(
            vm.state,
            "Running",
            "VM should be in running state after deployment"
        )

        self.logger.debug("Listing routers for network: %s" % network.name)
        routers = Router.list(
            self.apiclient,
            networkid=network.id,
            listall=True
        )
        self.assertEqual(
            isinstance(routers, list),
            True,
            "list router should return Master and backup routers"
        )
        self.assertEqual(
            len(routers),
            2,
            "Length of the list router should be 2 (Backup & master)"
        )

        public_ips = list_public_ip(
            self.apiclient,
            account=self.account.name,
            domainid=self.account.domainid,
            zoneid=self.zone.id
        )

        public_ip = public_ips[0]

        self.assertEqual(
            isinstance(public_ips, list),
            True,
            "Check for list public IPs response return valid data"
        )

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
        nat_rule = NATRule.create(
            self.apiclient,
            virtual_machine,
            self.services["natrule"],
            public_ip.id
        )

        # Test SSH after closing port 22
        expected = 1
        gateway = self.find_public_gateway()
        ssh_command = "ping -c 3 %s" % gateway
        check_string = "3 packets received"
        result = self.check_router_command(virtual_machine, nat_rule.ipaddress, ssh_command, check_string, self)

        self.assertEqual(
            result,
            expected,
            "Ping to outside world from VM should be successful!"
        )

        expected = 1
        ssh_command = self.HTTP_COMMAND % gateway
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
            protocol=self.services["egress_8080"]["protocol"],
            startport=self.services["egress_8080"]["startport"],
            endport=self.services["egress_8080"]["endport"],
            cidrlist=self.services["egress_8080"]["cidrlist"]
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
    def test_02_RVR_Network_FW_PF_SSH_default_routes_egress_false(self):
        """ Test redundant router internals """
        self.logger.debug("Starting test_02_RVR_Network_FW_PF_SSH_default_routes_egress_false...")

        network_offering_egress_false = get_default_redundant_isolated_network_offering(self.apiclient)

        self.logger.debug("Creating network with network offering: %s" % network_offering_egress_false.id)
        network = Network.create(
            self.apiclient,
            self.services["network"],
            accountid=self.account.name,
            domainid=self.account.domainid,
            networkofferingid=network_offering_egress_false.id,
            zoneid=self.zone.id
        )
        self.logger.debug("Created network with ID: %s" % network.id)

        networks = Network.list(
            self.apiclient,
            id=network.id,
            listall=True
        )
        self.assertEqual(
            isinstance(networks, list),
            True,
            "List networks should return a valid response for created network"
        )

        self.logger.debug("Deploying VM in account: %s" % self.account.name)
        virtual_machine = VirtualMachine.create(
            self.apiclient,
            self.services["virtual_machine"],
            templateid=self.template.id,
            accountid=self.account.name,
            domainid=self.account.domainid,
            serviceofferingid=self.service_offering.id,
            networkids=[str(network.id)]
        )

        self.logger.debug("Deployed VM in network: %s" % network.id)

        self.cleanup.insert(0, network)
        self.cleanup.insert(0, virtual_machine)

        vms = VirtualMachine.list(
            self.apiclient,
            id=virtual_machine.id,
            listall=True
        )
        self.assertEqual(
            isinstance(vms, list),
            True,
            "List Vms should return a valid list"
        )
        vm = vms[0]
        self.assertEqual(
            vm.state,
            "Running",
            "VM should be in running state after deployment"
        )

        self.logger.debug("Listing routers for network: %s" % network.name)
        routers = Router.list(
            self.apiclient,
            networkid=network.id,
            listall=True
        )
        self.assertEqual(
            isinstance(routers, list),
            True,
            "list router should return Master and backup routers"
        )
        self.assertEqual(
            len(routers),
            2,
            "Length of the list router should be 2 (Backup & master)"
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
            protocol=self.services["natrule"]["protocol"],
            cidrlist=['0.0.0.0/0'],
            startport=self.services["natrule"]["publicport"],
            endport=self.services["natrule"]["publicport"]
        )

        self.logger.debug("Creating NAT rule for VM ID: %s" % virtual_machine.id)
        nat_rule = NATRule.create(
            self.apiclient,
            virtual_machine,
            self.services["natrule"],
            public_ip.id
        )

        expected = 0
        gateway = self.find_public_gateway()
        ssh_command = "ping -c 3 %s" % gateway
        check_string = "3 packets received"
        result = self.check_router_command(virtual_machine, nat_rule.ipaddress, ssh_command, check_string, self)

        self.assertEqual(
            result,
            expected,
            "Ping to outside world from VM should NOT be successful"
        )

        expected = 0
        ssh_command = self.HTTP_COMMAND % gateway
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
            protocol=self.services["egress_8080"]["protocol"],
            startport=self.services["egress_8080"]["startport"],
            endport=self.services["egress_8080"]["endport"],
            cidrlist=self.services["egress_8080"]["cidrlist"]
        )

        EgressFireWallRule.create(
            self.apiclient,
            networkid=network.id,
            protocol=self.services["egress_53"]["protocol"],
            startport=self.services["egress_53"]["startport"],
            endport=self.services["egress_53"]["endport"],
            cidrlist=self.services["egress_53"]["cidrlist"]
        )

        expected = 1
        result = self.check_router_command(virtual_machine, nat_rule.ipaddress, ssh_command, check_string, self)

        self.assertEqual(
            result,
            expected,
            self.HTTP_ASSERT_SUCCESS_MESSAGE
        )

        return

    @attr(tags=['advanced'])
    def test_03_RVR_Network_check_router_state(self):
        """ Test redundant router internals """
        self.logger.debug("Starting test_03_RVR_Network_check_router_state...")

        network_offering_egress_false = get_default_redundant_isolated_network_offering(self.apiclient)

        self.logger.debug("Creating network with network offering: %s" % network_offering_egress_false.id)
        network = Network.create(
            self.apiclient,
            self.services["network"],
            accountid=self.account.name,
            domainid=self.account.domainid,
            networkofferingid=network_offering_egress_false.id,
            zoneid=self.zone.id
        )
        self.logger.debug("Created network with ID: %s" % network.id)

        networks = Network.list(
            self.apiclient,
            id=network.id,
            listall=True
        )
        self.assertEqual(
            isinstance(networks, list),
            True,
            "List networks should return a valid response for created network"
        )

        self.logger.debug("Deploying VM in account: %s" % self.account.name)
        virtual_machine = VirtualMachine.create(
            self.apiclient,
            self.services["virtual_machine"],
            templateid=self.template.id,
            accountid=self.account.name,
            domainid=self.account.domainid,
            serviceofferingid=self.service_offering.id,
            networkids=[str(network.id)]
        )

        self.logger.debug("Deployed VM in network: %s" % network.id)

        self.cleanup.insert(0, network)
        self.cleanup.insert(0, virtual_machine)

        vms = VirtualMachine.list(
            self.apiclient,
            id=virtual_machine.id,
            listall=True
        )
        self.assertEqual(
            isinstance(vms, list),
            True,
            "List Vms should return a valid list"
        )
        vm = vms[0]
        self.assertEqual(
            vm.state,
            "Running",
            "VM should be in running state after deployment"
        )

        self.logger.debug("Listing routers for network: %s" % network.name)
        routers = Router.list(
            self.apiclient,
            networkid=network.id,
            listall=True
        )
        self.assertEqual(
            isinstance(routers, list),
            True,
            "list router should return Master and backup routers"
        )
        self.assertEqual(
            len(routers),
            2,
            "Length of the list router should be 2 (Backup & master)"
        )

        vals = ["MASTER", "BACKUP", "UNKNOWN"]
        cnts = [0, 0, 0]

        result = "UNKNOWN"
        for router in routers:
            if router.state == "Running":
                hosts = list_hosts(
                    self.apiclient,
                    zoneid=router.zoneid,
                    type='Routing',
                    state='Up',
                    id=router.hostid
                )
                self.assertEqual(
                    isinstance(hosts, list),
                    True,
                    "Check list host returns a valid list"
                )
                host = hosts[0]

                try:
                    host.user, host.passwd = get_host_credentials(
                        self.config, host.ipaddress)
                    result = str(get_process_status(
                        host.ipaddress,
                        22,
                        host.user,
                        host.passwd,
                        router.linklocalip,
                        "sh /opt/cloud/bin/checkrouter.sh "
                    ))

                except KeyError:
                    self.skipTest(
                        "Marvin configuration has no host credentials to\
                                check router services")

                if result.count(vals[0]) == 1:
                    cnts[vals.index(vals[0])] += 1

        if cnts[vals.index('MASTER')] != 1:
            self.fail("No Master or too many master routers found %s" % cnts[vals.index('MASTER')])

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

    def find_public_gateway(self):
        networks = list_networks(self.apiclient,
                                 zoneid=self.zone.id,
                                 listall=True,
                                 issystem=True,
                                 traffictype="Public")
        self.logger.debug('::: Public Networks ::: ==> %s' % networks)

        self.assertTrue(len(networks) == 1, "Test expects only 1 Public network but found -> '%s'" % len(networks))

        ip_ranges = list_vlan_ipranges(self.apiclient,
                                       zoneid=self.zone.id,
                                       networkid=networks[0].id)
        self.logger.debug('::: IP Ranges ::: ==> %s' % ip_ranges)

        self.assertTrue(len(ip_ranges) == 1, "Test expects only 1 VLAN IP Range network but found -> '%s'" % len(ip_ranges))
        self.assertIsNotNone(ip_ranges[0].gateway, "The network with id -> '%s' returned an IP Range with a None gateway. Please check your Datacenter settings." % networks[0].id)

        return ip_ranges[0].gateway
