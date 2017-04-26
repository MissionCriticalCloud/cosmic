import socket
import time

from nose.plugins.attrib import attr

from marvin.cloudstackAPI import (
    stopRouter,
    destroyRouter
)
from marvin.cloudstackTestCase import cloudstackTestCase
from marvin.lib.base import (
    PublicIPAddress,
    VirtualMachine,
    Network,
    NetworkACL,
    NATRule,
    VPC,
    Account
)
from marvin.lib.common import (
    list_routers,
    list_hosts,
    get_template,
    get_zone,
    get_domain,
    get_default_virtual_machine_offering,
    get_default_network_offering,
    get_default_network_offering_no_load_balancer,
    get_default_vpc_offering
)
from marvin.lib.utils import (
    get_process_status,
    cleanup_resources
)
from marvin.utils.MarvinLog import MarvinLog


class Services:
    """Test VPC network services - Port Forwarding Rules Test Data Class.
    """

    def __init__(self):
        self.services = {
            "configurableData": {
                "host": {
                    "password": "password",
                    "username": "root",
                    "port": 22
                },
                "input": "INPUT",
                "forward": "FORWARD"
            },
            "account": {
                "email": "test@test.com",
                "firstname": "Test",
                "lastname": "User",
                "username": "test",
                # Random characters are appended for unique
                # username
                "password": "password",
            },
            "vpc": {
                "name": "TestVPC",
                "displaytext": "TestVPC",
                "cidr": '10.1.1.1/16'
            },
            "network": {
                "name": "Test Network",
                "displaytext": "Test Network",
                "netmask": '255.255.255.0'
            },
            "natrule": {
                "privateport": 22,
                "publicport": 22,
                "startport": 22,
                "endport": 22,
                "protocol": "TCP",
                "cidrlist": '0.0.0.0/0',
            },
            "virtual_machine": {
                "displayname": "Test VM",
                "username": "root",
                "password": "password",
                "ssh_port": 22,
                "privateport": 22,
                "publicport": 22,
                "protocol": 'TCP',
            },
            "ostype": 'CentOS 5.3 (64-bit)',
            "timeout": 10,
        }


class TestVPCIpTablesPolicies(cloudstackTestCase):
    @classmethod
    def setUpClass(cls):
        cls.logger = MarvinLog(MarvinLog.LOGGER_TEST).get_logger()

        # We want to fail quicker if it's failure
        socket.setdefaulttimeout(60)

        cls.testClient = super(TestVPCIpTablesPolicies, cls).getClsTestClient()
        cls.apiclient = cls.testClient.getApiClient()

        cls.services = Services().services
        # Get Zone, Domain and templates
        cls.domain = get_domain(cls.apiclient)
        cls.zone = get_zone(cls.apiclient, cls.testClient.getZoneForTests())
        cls.template = get_template(
            cls.apiclient,
            cls.zone.id
        )

        cls.services["virtual_machine"]["zoneid"] = cls.zone.id
        cls.services["virtual_machine"]["template"] = cls.template.id

        cls.account = Account.create(
            cls.apiclient,
            cls.services["account"],
            admin=True,
            domainid=cls.domain.id)

        cls.service_offering = get_default_virtual_machine_offering(cls.apiclient)

        cls.entity_manager = EntityManager(cls.apiclient, cls.services, cls.service_offering, cls.account, cls.zone, cls.logger)

        cls._cleanup = [cls.account]
        return

    @classmethod
    def tearDownClass(cls):
        try:
            cleanup_resources(cls.apiclient, cls._cleanup)
        except Exception as e:
            raise Exception("Warning: Exception during cleanup : %s" % e)
        return

    def setUp(self):
        self.vpc_off = get_default_vpc_offering(self.apiclient)

        self.vpc = VPC.create(
            self.apiclient,
            self.services["vpc"],
            vpcofferingid=self.vpc_off.id,
            zoneid=self.zone.id,
            account=self.account.name,
            domainid=self.account.domainid)

        self.cleanup = [self.vpc]
        self.entity_manager.set_cleanup(self.cleanup)
        return

    def tearDown(self):
        try:
            self.entity_manager.destroy_routers()
            cleanup_resources(self.apiclient, self.cleanup)
        except Exception as e:
            raise Exception("Warning: Exception during cleanup : %s" % e)
        return

    @attr(tags=["advanced", "intervlan"])
    def test_01_single_VPC_iptables_policies(self):
        """ Test iptables default INPUT/FORWARD policies on VPC router """
        self.logger.debug("Starting test_01_single_VPC_iptables_policies")

        routers = self.entity_manager.query_routers()

        self.assertEqual(
            isinstance(routers, list), True,
            "Check for list routers response return valid data")

        net_off = get_default_network_offering(self.apiclient)
        self.entity_manager.create_network(net_off, self.vpc.id, "10.1.1.1")
        net_off_no_lb = get_default_network_offering_no_load_balancer(self.apiclient)
        self.entity_manager.create_network(net_off_no_lb, self.vpc.id, "10.1.2.1")

        self.entity_manager.add_nat_rules(self.vpc.id)
        self.entity_manager.do_vpc_test()

        for router in routers:
            if not router.isredundantrouter and router.vpcid:
                hosts = list_hosts(
                    self.apiclient,
                    id=router.hostid)
                self.assertEqual(
                    isinstance(hosts, list),
                    True,
                    "Check for list hosts response return valid data")

                host = hosts[0]
                host.user = self.services["configurableData"]["host"]["username"]
                host.passwd = self.services["configurableData"]["host"]["password"]
                host.port = self.services["configurableData"]["host"]["port"]
                tables = [self.services["configurableData"]["input"], self.services["configurableData"]["forward"]]

                for table in tables:
                    try:
                        result = get_process_status(
                            host.ipaddress,
                            host.port,
                            host.user,
                            host.passwd,
                            router.linklocalip,
                            'iptables -L %s' % table)
                    except KeyError:
                        self.skipTest(
                            "Provide a marvin config file with host\
                                    credentials to run %s" %
                            self._testMethodName)

                    self.logger.debug("iptables -L %s: %s" % (table, result))
                    res = str(result)

                    self.assertEqual(
                        res.count("DROP"),
                        1,
                        "%s Default Policy should be DROP" % table)


class EntityManager(object):
    def __init__(self, apiclient, services, service_offering, account, zone, logger):
        self.apiclient = apiclient
        self.services = services
        self.service_offering = service_offering
        self.account = account
        self.zone = zone
        self.logger = logger

        self.cleanup = []
        self.networks = []
        self.routers = []
        self.ips = []

    def set_cleanup(self, cleanup):
        self.cleanup = cleanup

    def add_nat_rules(self, vpc_id):
        for o in self.networks:
            for vm in o.get_vms():
                if vm.get_ip() is None:
                    vm.set_ip(self.acquire_publicip(o.get_net(), vpc_id))
                if vm.get_nat() is None:
                    vm.set_nat(self.create_natrule(vm.get_vm(), vm.get_ip(), o.get_net(), vpc_id))
                    time.sleep(5)

    def do_vpc_test(self):
        for o in self.networks:
            for vm in o.get_vms():
                self.check_ssh_into_vm(vm.get_vm(), vm.get_ip())

    def create_natrule(self, vm, public_ip, network, vpc_id):
        self.logger.debug("Creating NAT rule in network for vm with public IP")

        nat_rule_services = self.services["natrule"]

        nat_rule = NATRule.create(
            self.apiclient,
            vm,
            nat_rule_services,
            ipaddressid=public_ip.ipaddress.id,
            openfirewall=False,
            networkid=network.id,
            vpcid=vpc_id)

        self.logger.debug("Adding NetworkACL rules to make NAT rule accessible")
        nwacl_nat = NetworkACL.create(
            self.apiclient,
            networkid=network.id,
            services=nat_rule_services,
            traffictype='Ingress'
        )
        self.logger.debug('nwacl_nat=%s' % nwacl_nat.__dict__)
        return nat_rule

    def check_ssh_into_vm(self, vm, public_ip):
        self.logger.debug("Checking if we can SSH into VM=%s on public_ip=%s" %
                          (vm.name, public_ip.ipaddress.ipaddress))
        vm.ssh_client = None
        try:
            vm.get_ssh_client(ipaddress=public_ip.ipaddress.ipaddress)
            self.logger.debug("SSH into VM=%s on public_ip=%s is successful" %
                              (vm.name, public_ip.ipaddress.ipaddress))
        except:
            raise Exception("Failed to SSH into VM - %s" % (public_ip.ipaddress.ipaddress))

    def create_network(self, network_offering, vpc_id, gateway='10.1.1.1'):
        try:
            self.services["network"]["name"] = "NETWORK-" + str(gateway)
            self.logger.debug('Adding Network=%s to VPC ID %s' % (self.services["network"], vpc_id))
            obj_network = Network.create(
                self.apiclient,
                self.services["network"],
                accountid=self.account.name,
                domainid=self.account.domainid,
                networkofferingid=network_offering.id,
                zoneid=self.zone.id,
                gateway=gateway,
                vpcid=vpc_id)

            self.logger.debug("Created network with ID: %s" % obj_network.id)
        except Exception, e:
            raise Exception('Unable to create a Network with offering=%s because of %s ' % (network_offering.id, e))

        o = networkO(obj_network)

        vm1 = self.deployvm_in_network(obj_network)
        self.cleanup.insert(1, obj_network)

        o.add_vm(vm1)
        self.networks.append(o)
        return o

    def deployvm_in_network(self, network):
        try:
            self.logger.debug('Creating VM in network=%s' % network.name)
            vm = VirtualMachine.create(
                self.apiclient,
                self.services["virtual_machine"],
                accountid=self.account.name,
                domainid=self.account.domainid,
                serviceofferingid=self.service_offering.id,
                networkids=[str(network.id)])

            self.logger.debug('Created VM=%s in network=%s' % (vm.id, network.name))
            self.cleanup.insert(0, vm)
            return vm
        except:
            raise Exception('Unable to create VM in a Network=%s' % network.name)

    def acquire_publicip(self, network, vpc_id):
        self.logger.debug("Associating public IP for network: %s" % network.name)
        public_ip = PublicIPAddress.create(
            self.apiclient,
            accountid=self.account.name,
            zoneid=self.zone.id,
            domainid=self.account.domainid,
            networkid=network.id,
            vpcid=vpc_id)
        self.logger.debug("Associated %s with network %s" % (
            public_ip.ipaddress.ipaddress,
            network.id))

        self.ips.append(public_ip)
        return public_ip

    def query_routers(self):
        self.routers = list_routers(self.apiclient,
                                    account=self.account.name,
                                    domainid=self.account.domainid)

        return self.routers

    def stop_router(self, router):
        self.logger.debug('Stopping router')
        cmd = stopRouter.stopRouterCmd()
        cmd.id = router.id
        self.apiclient.stopRouter(cmd)

    def destroy_routers(self):
        self.logger.debug('Destroying routers')
        for router in self.routers:
            self.stop_router(router)
            cmd = destroyRouter.destroyRouterCmd()
            cmd.forced = "true"
            cmd.id = router.id
            self.apiclient.destroyRouter(cmd)
        self.routers = []


class networkO(object):
    def __init__(self, net):
        self.network = net
        self.vms = []

    def get_net(self):
        return self.network

    def add_vm(self, vm):
        self.vms.append(vmsO(vm))

    def get_vms(self):
        return self.vms


class vmsO(object):
    def __init__(self, vm):
        self.vm = vm
        self.ip = None
        self.nat = None

    def get_vm(self):
        return self.vm

    def get_ip(self):
        return self.ip

    def get_nat(self):
        return self.nat

    def set_ip(self, ip):
        self.ip = ip

    def set_nat(self, nat):
        self.nat = nat
