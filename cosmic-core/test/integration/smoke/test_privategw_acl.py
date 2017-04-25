from nose.plugins.attrib import attr

from marvin.cloudstackAPI import (
    stopRouter,
    replaceNetworkACLList
)
from marvin.cloudstackTestCase import cloudstackTestCase
from marvin.lib.base import (
    NetworkACL,
    NetworkACLList,
    StaticRoute,
    PrivateGateway,
    Network,
    NATRule,
    PublicIPAddress,
    VirtualMachine,
    VPC,
    Account
)
from marvin.lib.common import (
    list_hosts,
    list_routers,
    get_default_acl,
    get_default_private_network_offering,
    get_default_virtual_machine_offering,
    get_default_network_offering,
    get_default_vpc_offering,
    get_default_redundant_vpc_offering,
    get_template,
    get_domain,
    get_zone
)
from marvin.lib.utils import (
    get_process_status,
    get_host_credentials,
    cleanup_resources
)
from marvin.utils.MarvinLog import MarvinLog


class TestPrivateGateway(cloudstackTestCase):

    attributes = {
        'account': {
            'email': 'e.cartman@southpark.com',
            'firstname': 'Eric',
            'lastname': 'Cartman',
            'username': 'e.cartman',
            'password': 'southpark'
        },
        'vpcs': {
            'vpc1': {
                'name': 'vpc1',
                'displaytext': 'vpc1',
                'cidr': '10.1.0.0/16'
            },
            'vpc2': {
                'name': 'vpc2',
                'displaytext': 'vpc2',
                'cidr': '10.2.0.0/16'
            }
        },
        'networks': {
            'network1': {
                'name': 'network1',
                'displaytext': 'network1',
                'gateway': '10.1.1.1',
                'netmask': '255.255.255.0'
            },
            'network2': {
                'name': 'network2',
                'displaytext': 'network2',
                'gateway': '10.2.1.1',
                'netmask': '255.255.255.0'
            },
            'private_gateways_network': {
                'name': 'private_gateways_network',
                'displaytext': 'private_gateways_network',
                'cidr': '172.16.1.0/24'
            }
        },
        'vms': {
            'vm1': {
                'name': 'vm1',
                'displayname': 'vm1'
            },
            'vm2': {
                'name': 'vm2',
                'displayname': 'vm2'
            }
        },
        'private_gateways': {
            'private_gateway1': '172.16.1.1',
            'private_gateway2': '172.16.1.2'
        },
        'nat_rule': {
            'protocol': 'TCP',
            'publicport': 22,
            'privateport': 22
        },
        'static_routes': {
            'static_route1': {
                'cidr': '10.2.0.0/16',
                'nexthop': '172.16.1.2'
            },
            'static_route2': {
                'cidr': '10.1.0.0/16',
                'nexthop': '172.16.1.1'
            }
        },
        'acls': {
            'acl1': {
                'name': 'acl1',
                'description': 'acl1',
                'entries': {
                    'entry1': {
                        'protocol': 'All',
                        'action': 'Allow',
                        'traffictype': 'Ingress'
                    }
                }
            },
            'acl2': {
                'name': 'acl2',
                'description': 'acl2',
                'entries': {
                    'entry2': {
                        'protocol': 'All',
                        'action': 'Allow',
                        'traffictype': 'Ingress'
                    }
                }
            }
        }
    }

    @classmethod
    def setUpClass(cls):
        cls.logger = MarvinLog('test').get_logger()

        cls.test_client = super(TestPrivateGateway, cls).getClsTestClient()
        cls.api_client = cls.test_client.getApiClient()

        cls.class_cleanup = []

    @classmethod
    def setup_infra(cls, redundant=False):

        if len(cls.class_cleanup) > 0:
            cleanup_resources(cls.api_client, cls.class_cleanup, cls.logger)
            cls.class_cleanup = []

        cls.zone = get_zone(cls.api_client, cls.test_client.getZoneForTests())
        cls.logger.debug("[TEST] Zone '%s' selected" % cls.zone.name)

        cls.domain = get_domain(cls.api_client)
        cls.logger.debug("[TEST] Domain '%s' selected" % cls.domain.name)

        cls.template = get_template(
            cls.api_client,
            cls.zone.id)

        cls.logger.debug("[TEST] Template '%s' selected" % cls.template.name)

        cls.account = Account.create(
            cls.api_client,
            cls.attributes['account'],
            admin=True,
            domainid=cls.domain.id)

        cls.class_cleanup += [cls.account]
        cls.logger.debug("[TEST] Account '%s' created", cls.account.name)

        cls.vpc_offering = get_default_redundant_vpc_offering(cls.api_client) if redundant else get_default_vpc_offering(cls.api_client)
        cls.logger.debug("[TEST] VPC Offering '%s' selected", cls.vpc_offering.name)

        cls.network_offering = get_default_network_offering(cls.api_client)
        cls.logger.debug("[TEST] Network Offering '%s' selected", cls.network_offering.name)

        cls.virtual_machine_offering = get_default_virtual_machine_offering(cls.api_client)
        cls.logger.debug("[TEST] Virtual Machine Offering '%s' selected", cls.virtual_machine_offering.name)

        cls.private_network_offering = get_default_private_network_offering(cls.api_client)
        cls.logger.debug("[TEST] Private Network Offering '%s' selected", cls.private_network_offering.name)

        cls.default_allow_acl = get_default_acl(cls.api_client, 'default_allow')
        cls.logger.debug("[TEST] ACL '%s' selected", cls.default_allow_acl.name)

        cls.vpc1 = VPC.create(cls.api_client,
            cls.attributes['vpcs']['vpc1'],
            vpcofferingid=cls.vpc_offering.id,
            zoneid=cls.zone.id,
            domainid=cls.domain.id,
            account=cls.account.name)
        cls.logger.debug("[TEST] VPC '%s' created, CIDR: %s", cls.vpc1.name, cls.vpc1.cidr)

        cls.network1 = Network.create(cls.api_client,
            cls.attributes['networks']['network1'],
            networkofferingid=cls.network_offering.id,
            aclid=cls.default_allow_acl.id,
            vpcid=cls.vpc1.id,
            zoneid=cls.zone.id,
            domainid=cls.domain.id,
            accountid=cls.account.name)
        cls.logger.debug("[TEST] Network '%s' created, CIDR: %s, Gateway: %s", cls.network1.name, cls.network1.cidr, cls.network1.gateway)

        cls.vm1 = VirtualMachine.create(cls.api_client,
            cls.attributes['vms']['vm1'],
            templateid=cls.template.id,
            serviceofferingid=cls.virtual_machine_offering.id,
            networkids=[cls.network1.id],
            zoneid=cls.zone.id,
            domainid=cls.domain.id,
            accountid=cls.account.name)
        cls.logger.debug("[TEST] VM '%s' created, Network: %s, IP %s", cls.vm1.name, cls.network1.name, cls.vm1.nic[0].ipaddress)

        cls.public_ip1 = PublicIPAddress.create(cls.api_client,
            zoneid=cls.zone.id,
            domainid=cls.account.domainid,
            accountid=cls.account.name,
            vpcid=cls.vpc1.id,
            networkid=cls.network1.id)
        cls.logger.debug("[TEST] Public IP '%s' acquired, VPC: %s, Network: %s", cls.public_ip1.ipaddress.ipaddress, cls.vpc1.name, cls.network1.name)

        cls.nat_rule1 = NATRule.create(cls.api_client,
            cls.vm1,
            cls.attributes['nat_rule'],
            vpcid=cls.vpc1.id,
            networkid=cls.network1.id,
            ipaddressid=cls.public_ip1.ipaddress.id)
        cls.logger.debug("[TEST] Port Forwarding Rule '%s (%s) %s => %s' created",
            cls.nat_rule1.ipaddress,
            cls.nat_rule1.protocol,
            cls.nat_rule1.publicport,
            cls.nat_rule1.privateport)

        cls.vpc2 = VPC.create(cls.api_client,
            cls.attributes['vpcs']['vpc2'],
            vpcofferingid=cls.vpc_offering.id,
            zoneid=cls.zone.id,
            domainid=cls.domain.id,
            account=cls.account.name)
        cls.logger.debug("[TEST] VPC '%s' created, CIDR: %s", cls.vpc2.name, cls.vpc2.cidr)

        cls.network2 = Network.create(cls.api_client,
            cls.attributes['networks']['network2'],
            networkofferingid=cls.network_offering.id,
            aclid=cls.default_allow_acl.id,
            vpcid=cls.vpc2.id,
            zoneid=cls.zone.id,
            domainid=cls.domain.id,
            accountid=cls.account.name)
        cls.logger.debug("[TEST] Network '%s' created, CIDR: %s, Gateway: %s", cls.network2.name, cls.network2.cidr, cls.network2.gateway)

        cls.vm2 = VirtualMachine.create(cls.api_client,
            cls.attributes['vms']['vm2'],
            templateid=cls.template.id,
            serviceofferingid=cls.virtual_machine_offering.id,
            networkids=[cls.network2.id],
            zoneid=cls.zone.id,
            domainid=cls.domain.id,
            accountid=cls.account.name)
        cls.logger.debug("[TEST] VM '%s' created, Network: %s, IP: %s", cls.vm2.name, cls.network2.name, cls.vm2.nic[0].ipaddress)

        cls.public_ip2 = PublicIPAddress.create(cls.api_client,
            zoneid=cls.zone.id,
            domainid=cls.account.domainid,
            accountid=cls.account.name,
            vpcid=cls.vpc2.id,
            networkid=cls.network2.id)
        cls.logger.debug("[TEST] Public IP '%s' acquired, VPC: %s, Network: %s", cls.public_ip2.ipaddress.ipaddress, cls.vpc2.name, cls.network2.name)

        cls.nat_rule2 = NATRule.create(cls.api_client,
            cls.vm2,
            cls.attributes['nat_rule'],
            vpcid=cls.vpc2.id,
            networkid=cls.network2.id,
            ipaddressid=cls.public_ip2.ipaddress.id)
        cls.logger.debug("[TEST] Port Forwarding Rule '%s (%s) %s => %s' created",
            cls.nat_rule2.ipaddress,
            cls.nat_rule2.protocol,
            cls.nat_rule2.publicport,
            cls.nat_rule2.privateport)

        cls.private_gateways_network = Network.create(cls.api_client,
            cls.attributes['networks']['private_gateways_network'],
            networkofferingid=cls.private_network_offering.id,
            aclid=cls.default_allow_acl.id,
            zoneid=cls.zone.id,
            domainid=cls.domain.id,
            accountid=cls.account.name)
        cls.logger.debug("[TEST] Network '%s' created, CIDR: %s", cls.private_gateways_network.name, cls.private_gateways_network.cidr)

        cls.private_gateway1 = PrivateGateway.create(cls.api_client,
            ipaddress=cls.attributes['private_gateways']['private_gateway1'],
            networkid=cls.private_gateways_network.id,
            aclid=cls.default_allow_acl.id,
            vpcid=cls.vpc1.id)
        cls.logger.debug("[TEST] Private Gateway '%s' created, Network: %s, VPC: %s", cls.private_gateway1.ipaddress, cls.private_gateways_network.name, cls.vpc1.name)

        cls.static_route1 = StaticRoute.create(cls.api_client,
            cls.attributes['static_routes']['static_route1'],
            vpcid=cls.vpc1.id)
        cls.logger.debug("[TEST] Static Route '%s => %s' created, VPC: %s", cls.static_route1.cidr, cls.static_route1.nexthop, cls.vpc1.name)

        cls.private_gateway2 = PrivateGateway.create(cls.api_client,
            ipaddress=cls.attributes['private_gateways']['private_gateway2'],
            networkid=cls.private_gateways_network.id,
            aclid=cls.default_allow_acl.id,
            vpcid=cls.vpc2.id)
        cls.logger.debug("[TEST] Private Gateway '%s' created, Network: %s, VPC: %s", cls.private_gateway2.ipaddress, cls.private_gateways_network.name, cls.vpc2.name)

        cls.static_route2 = StaticRoute.create(cls.api_client,
            cls.attributes['static_routes']['static_route2'],
            vpcid=cls.vpc2.id)
        cls.logger.debug("[TEST] Static Route '%s => %s' created, VPC: %s", cls.static_route2.cidr, cls.static_route2.nexthop, cls.vpc2.name)

    @classmethod
    def tearDownClass(cls):

        try:
            cleanup_resources(cls.api_client, cls.class_cleanup, cls.logger)

        except Exception as e:
            raise Exception("Exception: %s" % e)

    def setUp(self):

        self.method_cleanup = []

    def tearDown(self):

        try:
            cleanup_resources(self.api_client, self.method_cleanup, self.logger)

        except Exception as e:
            raise Exception("Exception: %s" % e)

    @attr(tags=['advanced'], required_hardware='true')
    def test_01(self):

        self.setup_infra(redundant=False)
        self.test_connectivity()

    @attr(tags=['advanced'], required_hardware='true')
    def test_02(self):

        self.cleanup_vpcs()
        self.test_connectivity()

    @attr(tags=['advanced'], required_hardware='true')
    def test_03(self):

        self.define_custom_acl()
        self.test_connectivity()

    @attr(tags=['advanced'], required_hardware='true')
    def test_04(self):

        self.setup_infra(redundant=True)
        self.test_connectivity()

    @attr(tags=['advanced'], required_hardware='true')
    def test_05(self):

        self.cleanup_vpcs()
        self.test_connectivity()

    @attr(tags=['advanced'], required_hardware='true')
    def test_06(self):

        self.define_custom_acl()
        self.test_connectivity()

    @attr(tags=['advanced'], required_hardware='true')
    def test_07(self):

        self.stop_master_router(self.vpc1)
        self.stop_master_router(self.vpc2)
        self.test_connectivity()

    def test_connectivity(self):

        try:
            ping_count = 3
            ssh_client = self.vm1.get_ssh_client(ipaddress=self.public_ip1.ipaddress.ipaddress, reconnect=True)

            ping_vm1_command = "ping -c %s %s" % (ping_count, self.vm1.nic[0].ipaddress)
            ping_vm1_command_output = str(ssh_client.execute(ping_vm1_command))
            self.logger.debug("[SSH COMMAND] [%s]: %s", ping_vm1_command, ping_vm1_command_output)

            ping_vm2_command = "ping -c %s %s" % (ping_count, self.vm2.nic[0].ipaddress)
            ping_vm2_command_output = str(ssh_client.execute(ping_vm2_command))
            self.logger.debug("[SSH COMMAND] [%s]: %s", ping_vm2_command, ping_vm2_command_output)

        except Exception as e:
            raise Exception("Exception: %s" % e)

        self.assertEqual(ping_vm1_command_output.count("%s packets transmitted, %s packets received" % (ping_count, ping_count)), 1)
        self.assertEqual(ping_vm2_command_output.count("%s packets transmitted, %s packets received" % (ping_count, ping_count)), 1)

    def cleanup_vpcs(self):

        self.logger.debug("[TEST] Restarting VPCs '%s' and '%s' with 'cleanup=True'", self.vpc1.name, self.vpc2.name)
        self.vpc1.restart(self.api_client, True)
        self.logger.debug("[TEST] VPC '%s' restarted", self.vpc1.name)
        self.vpc2.restart(self.api_client, True)
        self.logger.debug("[TEST] VPC '%s' restarted", self.vpc2.name)

    def define_custom_acl(self):

        acl1 = NetworkACLList.create(self.api_client,
            self.attributes['acls']['acl1'],
            vpcid=self.vpc1.id)

        NetworkACL.create(self.api_client,
            self.attributes['acls']['acl1']['entries']['entry1'],
            networkid=self.network1.id,
            aclid=acl1.id)

        try:
            command = replaceNetworkACLList.replaceNetworkACLListCmd()
            command.aclid = acl1.id
            command.gatewayid = self.private_gateway1.id
            response = self.api_client.replaceNetworkACLList(command)

        except Exception as e:
            raise Exception("Exception: %s" % e)

        self.assertTrue(response.success)
        self.logger.debug("[TEST] Private Gateway '%s' ACL replaced", self.private_gateway1.ipaddress)

        acl2 = NetworkACLList.create(self.api_client,
            self.attributes['acls']['acl2'],
            vpcid=self.vpc2.id)

        NetworkACL.create(self.api_client,
            self.attributes['acls']['acl2']['entries']['entry2'],
            networkid=self.network2.id,
            aclid=acl2.id)

        try:
            command2 = replaceNetworkACLList.replaceNetworkACLListCmd()
            command2.aclid = acl2.id
            command2.gatewayid = self.private_gateway2.id
            response2 = self.api_client.replaceNetworkACLList(command2)

        except Exception as e:
            raise Exception("Exception: %s" % e)

        self.assertTrue(response2.success)
        self.logger.debug("[TEST] Private Gateway '%s' ACL replaced", self.private_gateway2.ipaddress)

    def stop_master_router(self, vpc):

        self.logger.debug("[TEST] Stopping Master Router of VPC '%s'...", vpc.name)
        routers = list_routers(self.api_client, domainid=self.domain.id, account=self.account.name, vpcid=vpc.id)
        for router in routers:
            if router.redundantstate == 'MASTER':
                cmd = stopRouter.stopRouterCmd()
                cmd.id = router.id
                cmd.forced = 'true'
                self.api_client.stopRouter(cmd)
                break

        for router in routers:
            if router.state == 'Running':
                hosts = list_hosts(self.api_client, zoneid=router.zoneid, type='Routing', state='Up', id=router.hostid)
                self.assertTrue(isinstance(hosts, list))
                host = next(iter(hosts or []), None)

                try:
                    host.user, host.passwd = get_host_credentials(self.config, host.ipaddress)
                    get_process_status(host.ipaddress, 22, host.user, host.passwd, router.linklocalip, "sh /opt/cloud/bin/checkrouter.sh ")

                except KeyError as e:
                    raise Exception("Exception: %s" % e)

        self.logger.debug("[TEST] Master Router of VPC '%s' stopped", vpc.name)
