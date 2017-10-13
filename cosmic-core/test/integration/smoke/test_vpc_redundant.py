import inspect
import socket
import time
from marvin.cloudstackAPI import (
    startRouter,
    stopRouter,
    rebootRouter,
    destroyRouter
)
from marvin.cloudstackTestCase import cloudstackTestCase
from marvin.lib.base import (
    Configurations,
    NetworkACL,
    NATRule,
    PublicIPAddress,
    VirtualMachine,
    Network,
    VPC,
    Account
)
from marvin.lib.common import (
    get_default_network_offering_no_load_balancer,
    get_default_network_offering,
    list_hosts,
    list_routers,
    get_default_redundant_vpc_offering,
    get_default_virtual_machine_offering,
    get_template,
    get_zone,
    get_domain
)
from marvin.lib.utils import (
    get_process_status,
    get_host_credentials,
    cleanup_resources
)
from marvin.utils.MarvinLog import MarvinLog
from nose.plugins.attrib import attr


class TestVPCRedundancy(cloudstackTestCase):
    @classmethod
    def setUpClass(cls):
        cls.logger = MarvinLog(MarvinLog.LOGGER_TEST).get_logger()

        # We want to fail quicker if it's failure
        socket.setdefaulttimeout(60)

        cls.testClient = super(TestVPCRedundancy, cls).getClsTestClient()
        cls.api_client = cls.testClient.getApiClient()

        cls.services = cls.testClient.getParsedTestDataConfig()
        # Get Zone, Domain and templates
        cls.domain = get_domain(cls.api_client)
        cls.zone = get_zone(cls.api_client, cls.testClient.getZoneForTests())
        cls.template = get_template(
            cls.api_client,
            cls.zone.id
        )
        cls.services["virtual_machine"]["zoneid"] = cls.zone.id
        cls.services["virtual_machine"]["template"] = cls.template.id

        cls.service_offering = get_default_virtual_machine_offering(cls.api_client)

        return

    def setUp(self):
        self.routers = []
        self.networks = []
        self.ips = []

        self.apiclient = self.testClient.getApiClient()
        self.hypervisor = self.testClient.getHypervisorInfo()

        self.account = Account.create(
            self.apiclient,
            self.services["account"],
            admin=True,
            domainid=self.domain.id)

        self.vpc_off = get_default_redundant_vpc_offering(self.apiclient)

        self.logger.debug("Creating a VPC network in the account: %s" % self.account.name)
        self.services["vpc"]["cidr"] = '10.1.1.1/16'
        self.vpc = VPC.create(
            self.apiclient,
            self.services["vpc"],
            vpcofferingid=self.vpc_off.id,
            zoneid=self.zone.id,
            account=self.account.name,
            domainid=self.account.domainid)

        self.cleanup = [self.account]
        return

    def tearDown(self):
        try:
            cleanup_resources(self.api_client, self.cleanup)
        except Exception as e:
            raise Exception("Warning: Exception during cleanup : %s" % e)
        return

    @attr(tags=['advanced'])
    def test_01_create_redundant_VPC_2tiers_4VMs_4IPs_4PF_ACL(self):
        """ Create a redundant VPC with two networks with two VMs in each network """
        self.logger.debug("Starting test_01_create_redundant_VPC_2tiers_4VMs_4IPs_4PF_ACL")
        self.query_routers()
        net_off = get_default_network_offering(self.apiclient)
        self.networks.append(self.create_network(net_off, "10.1.1.1"))
        net_off_no_lb = get_default_network_offering_no_load_balancer(self.apiclient)
        self.networks.append(self.create_network(net_off_no_lb, "10.1.2.1"))
        self.check_routers_state()
        self.add_nat_rules()
        self.do_vpc_test(False)

        self.stop_router_by_type("MASTER")
        self.check_routers_state(1)
        self.do_vpc_test(False)

        self.delete_nat_rules()
        self.check_routers_state(count=1)
        self.do_vpc_test(True)
        self.delete_public_ip()

        self.start_routers()
        self.add_nat_rules()
        self.check_routers_state()
        self.do_vpc_test(False)

    @attr(tags=['advanced'])
    def test_02_redundant_VPC_default_routes(self):
        """ Create a redundant VPC with two networks with two VMs in each network and check default routes"""
        self.logger.debug("Starting test_02_redundant_VPC_default_routes")
        self.query_routers()
        net_off = get_default_network_offering(self.apiclient)
        self.networks.append(self.create_network(net_off, "10.1.1.1"))
        net_off_no_lb = get_default_network_offering_no_load_balancer(self.apiclient)
        self.networks.append(self.create_network(net_off_no_lb, "10.1.2.1"))
        self.check_routers_state()
        self.add_nat_rules()
        self.do_default_routes_test()

    @attr(tags=['advanced'])
    def test_03_create_redundant_VPC_1tier_2VMs_2IPs_2PF_ACL_reboot_routers(self):
        """ Create a redundant VPC with two networks with two VMs in each network """
        self.logger.debug("Starting test_01_create_redundant_VPC_2tiers_4VMs_4IPs_4PF_ACL")
        self.query_routers()
        net_off = get_default_network_offering(self.apiclient)
        self.networks.append(self.create_network(net_off, "10.1.1.1"))
        self.check_routers_state()
        self.add_nat_rules()
        self.do_vpc_test(False)

        self.reboot_router_by_type("MASTER")
        self.check_routers_state()
        self.do_vpc_test(False)

        self.reboot_router_by_type("MASTER")
        self.check_routers_state()
        self.do_vpc_test(False)

    @attr(tags=['advanced'])
    def test_04_rvpc_multi_tiers(self):
        """ Create a redundant VPC with 3 Tiers, 3 VMs, 3 PF rules"""
        self.logger.debug("Starting test_05_rvpc_multi_tiers")
        self.query_routers()

        net_off = get_default_network_offering(self.apiclient)
        network1 = self.create_network(net_off, "10.1.1.1", nr_vms=1)
        self.networks.append(network1)
        net_off_no_lb = get_default_network_offering_no_load_balancer(self.apiclient)
        self.networks.append(self.create_network(net_off_no_lb, "10.1.2.1", nr_vms=1))
        network2 = self.create_network(net_off_no_lb, "10.1.3.1", nr_vms=1)
        self.networks.append(network2)

        self.check_routers_state()
        self.add_nat_rules()
        self.do_vpc_test(False)

        self.destroy_vm(network1)
        network1.get_net().delete(self.apiclient)
        self.networks.remove(network1)

        self.check_routers_state(status_to_check="MASTER")
        self.do_vpc_test(False)

        self.destroy_vm(network2)
        network2.get_net().delete(self.apiclient)
        self.networks.remove(network2)

        self.check_routers_state(status_to_check="MASTER")
        self.do_vpc_test(False)

    def query_routers(self, count=2, showall=False):
        self.logger.debug('query_routers count: %s, showall: %s' % (count, showall))
        self.routers = list_routers(self.apiclient,
                                    account=self.account.name,
                                    domainid=self.account.domainid,
                                    )
        if not showall:
            self.routers = [r for r in self.routers if r.state != "Stopped"]

        self.logger.debug('query_routers routers: %s' % self.routers)
        self.assertEqual(
            isinstance(self.routers, list), True,
            "Check for list routers response return valid data")

        self.assertEqual(
            len(self.routers), count,
            "Check that %s routers were indeed created" % count)

    def check_routers_state(self, count=2, status_to_check="MASTER", expected_count=1, showall=False):
        vals = ["MASTER", "BACKUP", "UNKNOWN", "TESTFAILED"]
        cnts = [0, 0, 0]

        result = "TESTFAILED"
        self.logger.debug('check_routers_state count: %s, status_to_check: %s, expected_count: %s, showall: %s' % (count, status_to_check, expected_count, showall))

        vrrp_interval = Configurations.list(self.apiclient, name="router.redundant.vrrp.interval")
        self.logger.debug("router.redundant.vrrp.interval is ==> %s" % vrrp_interval)

        total_sleep = 20
        if vrrp_interval:
            total_sleep = (int(vrrp_interval[0].value) * 4) + 10
        else:
            self.logger.debug("Could not retrieve the key 'router.redundant.vrrp.interval'. Sleeping for 10 seconds.")

        '''
        Sleep (router.redundant.vrrp.interval * 4) seconds here because VRRP will have to be reconfigured. Due to the configuration changes,
        it will start a new election and that will take ~4 multiplied by the advertisement interval seconds. Next to that, we need some time
        for the router to be reconfigured, so adding 10 seconds to be on the safe side.
        '''
        time.sleep(total_sleep)

        self.query_routers(count, showall)
        for router in self.routers:
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
                    for _ in range(5):
                        host.user, host.passwd = get_host_credentials(self.config, host.ipaddress)
                        result = str(get_process_status(
                            host.ipaddress,
                            22,
                            host.user,
                            host.passwd,
                            router.linklocalip,
                            "sh /opt/cosmic/router/scripts/checkrouter.sh "
                        ))

                        self.logger.debug('check_routers_state router: %s, result: %s' % (router.name, result))

                        if result.count(status_to_check) == 1:
                            cnts[vals.index(status_to_check)] += 1
                            break
                        elif result.count("UNKNOWN") == 1:
                            time.sleep(5)
                        else:
                            break


                except KeyError:
                    self.skipTest(
                        "Marvin configuration has no host credentials to\
                                check router services")

        if cnts[vals.index(status_to_check)] != expected_count:
            self.fail("Expected '%s' router[s] at state '%s', but found '%s'! Result: %s" % (expected_count, status_to_check, cnts[vals.index(status_to_check)], result))

    def check_routers_interface(self, count=2, interface_to_check="eth1", expected_exists=True, showall=False):
        result = ""

        self.query_routers(count, showall)
        for router in self.routers:
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
                    host.user, host.passwd = get_host_credentials(self.config, host.ipaddress)
                    result = str(get_process_status(
                        host.ipaddress,
                        22,
                        host.user,
                        host.passwd,
                        router.linklocalip,
                        "ip a | grep %s | grep state | awk '{print $9;}'" % interface_to_check
                    ))

                except KeyError:
                    self.skipTest("Marvin configuration has no host credentials to check router services")

                if expected_exists:
                    if (result.count("UP") == 1) or (result.count("DOWN") == 1):
                        self.logger.debug("Expected interface '%s' to exist and it does!" % interface_to_check)
                    else:
                        self.fail("Expected interface '%s' to exist, but it didn't!" % interface_to_check)
                else:
                    if (result.count("UP") == 1) or (result.count("DOWN") == 1):
                        self.fail("Expected interface '%s' to not exist, but it did!" % interface_to_check)
                    else:
                        self.logger.debug("Expected interface '%s' to not exist, and it didn't!" % interface_to_check)

    def stop_router(self, router):
        self.logger.debug('Stopping router %s' % router.id)
        cmd = stopRouter.stopRouterCmd()
        cmd.id = router.id
        cmd.forced = "true"
        self.apiclient.stopRouter(cmd)

    def reboot_router(self, router):
        self.logger.debug('Rebooting router %s' % router.id)
        cmd = rebootRouter.rebootRouterCmd()
        cmd.id = router.id
        self.apiclient.rebootRouter(cmd)

    def stop_router_by_type(self, type):
        self.check_routers_state()
        self.logger.debug('Stopping %s router' % type)
        for router in self.routers:
            if router.redundantstate == type:
                self.stop_router(router)

    def reboot_router_by_type(self, type):
        self.check_routers_state()
        self.logger.debug('Rebooting %s router' % type)
        for router in self.routers:
            if router.redundantstate == type:
                self.reboot_router(router)

    def destroy_routers(self):
        self.logger.debug('Destroying routers')
        for router in self.routers:
            self.stop_router(router)
            cmd = destroyRouter.destroyRouterCmd()
            cmd.id = router.id
            self.apiclient.destroyRouter(cmd)
        self.routers = []

    def start_routers(self):
        self.check_routers_state(showall=True)
        self.logger.debug('Starting stopped routers')
        for router in self.routers:
            self.logger.debug('Router %s has state %s' % (router.id, router.state))
            if router.state == "Stopped":
                self.logger.debug('Starting stopped router %s' % router.id)
                cmd = startRouter.startRouterCmd()
                cmd.id = router.id
                self.apiclient.startRouter(cmd)

    def create_network(self, network_offering, gateway='10.1.1.1', vpc=None, nr_vms=2):
        if not nr_vms or nr_vms <= 0:
            self.fail("At least 1 VM has to be created. You informed nr_vms < 1")
        try:
            self.services["network"]["name"] = "NETWORK-" + str(gateway)
            self.logger.debug('Adding Network=%s' % self.services["network"])
            network = Network.create(
                self.apiclient,
                self.services["network"],
                accountid=self.account.name,
                domainid=self.account.domainid,
                networkofferingid=network_offering.id,
                zoneid=self.zone.id,
                gateway=gateway,
                vpcid=vpc.id if vpc else self.vpc.id
            )

            self.logger.debug("Created network with ID: %s" % network.id)
        except Exception, e:
            self.fail('Unable to create a Network with offering=%s because of %s ' % (network_offering.id, e))
        o = networkO(network)

        for i in range(0, nr_vms):
            vm1 = self.deployvm_in_network(network)
            o.add_vm(vm1)

        return o

    def deployvm_in_network(self, network, host_id=None):
        try:
            self.logger.debug('Creating VM in network=%s' % network.name)
            vm = VirtualMachine.create(
                self.apiclient,
                self.services["virtual_machine"],
                accountid=self.account.name,
                domainid=self.account.domainid,
                serviceofferingid=self.service_offering.id,
                networkids=[str(network.id)],
                hostid=host_id
            )

            self.logger.debug('Created VM=%s in network=%s' % (vm.id, network.name))
            return vm
        except:
            self.fail('Unable to create VM in a Network=%s' % network.name)

    def acquire_publicip(self, network):
        self.logger.debug("Associating public IP for network: %s" % network.name)
        public_ip = PublicIPAddress.create(
            self.apiclient,
            accountid=self.account.name,
            zoneid=self.zone.id,
            domainid=self.account.domainid,
            networkid=network.id,
            vpcid=self.vpc.id
        )
        self.logger.debug("Associated %s with network %s" % (
            public_ip.ipaddress.ipaddress,
            network.id
        ))
        return public_ip

    def create_natrule(self, vm, public_ip, network, services=None):
        self.logger.debug("Creating NAT rule in network for vm with public IP")
        if not services:
            services = self.services["natrule_ssh"]
        nat_rule = NATRule.create(
            self.apiclient,
            vm,
            services,
            ipaddressid=public_ip.ipaddress.id,
            openfirewall=False,
            networkid=network.id,
            vpcid=self.vpc.id)

        self.logger.debug("Adding NetworkACL rules to make NAT rule accessible")
        nwacl_nat = NetworkACL.create(
            self.apiclient,
            networkid=network.id,
            services=services,
            traffictype='Ingress'
        )
        self.logger.debug('nwacl_nat=%s' % nwacl_nat.__dict__)

        return nat_rule

    def check_ssh_into_vm(self, vm, public_ip, expectFail=False, retries=5):
        self.logger.debug("Checking if we can SSH into VM=%s on public_ip=%s (%r)" %
                          (vm.name, public_ip.ipaddress.ipaddress, expectFail))
        vm.ssh_client = None
        try:
            if 'retries' in inspect.getargspec(vm.get_ssh_client).args:
                vm.get_ssh_client(ipaddress=public_ip.ipaddress.ipaddress, retries=retries)
            else:
                vm.get_ssh_client(ipaddress=public_ip.ipaddress.ipaddress)
            if expectFail:
                self.fail("SSH into VM=%s on public_ip=%s is successful (Not Expected)" %
                          (vm.name, public_ip.ipaddress.ipaddress))
            else:
                self.logger.debug("SSH into VM=%s on public_ip=%s is successful" %
                                  (vm.name, public_ip.ipaddress.ipaddress))
        except:
            if expectFail:
                self.logger.debug("Failed to SSH into VM - %s (Expected)" % (public_ip.ipaddress.ipaddress))
            else:
                self.fail("Failed to SSH into VM - %s" % (public_ip.ipaddress.ipaddress))

    def destroy_vm(self, network):
        vms_to_delete = []
        for vm in network.get_vms():
            vm.get_vm().delete(self.apiclient, expunge=True)
            vms_to_delete.append(vm)

        all_vms = network.get_vms()
        [all_vms.remove(vm) for vm in vms_to_delete]

    def stop_vm(self):
        for o in self.networks:
            for vm in o.get_vms():
                vm.get_vm().stop(self.apiclient)

    def start_vm(self):
        for o in self.networks:
            for vm in o.get_vms():
                vm.get_vm().start(self.apiclient)

    def delete_nat_rules(self):
        for o in self.networks:
            for vm in o.get_vms():
                if vm.get_nat() is not None:
                    vm.get_nat().delete(self.apiclient)
                    vm.set_nat(None)

    def delete_public_ip(self):
        for o in self.networks:
            for vm in o.get_vms():
                if vm.get_ip() is not None:
                    vm.get_ip().delete(self.apiclient)
                    vm.set_ip(None)
                    vm.set_nat(None)

    def add_nat_rules(self):
        for o in self.networks:
            for vm in o.get_vms():
                if vm.get_ip() is None:
                    vm.set_ip(self.acquire_publicip(o.get_net()))
                if vm.get_nat() is None:
                    vm.set_nat(self.create_natrule(vm.get_vm(), vm.get_ip(), o.get_net()))

    def do_vpc_test(self, expectFail):
        retries = 5
        if expectFail:
            retries = 2
        for o in self.networks:
            for vm in o.get_vms():
                self.check_ssh_into_vm(vm.get_vm(), vm.get_ip(), expectFail=expectFail, retries=retries)

    def do_default_routes_test(self):
        for o in self.networks:
            for vmObj in o.get_vms():
                ssh_command = "ping -c 3 8.8.8.8"

                # Should be able to SSH VM
                result = 'failed'
                try:
                    vm = vmObj.get_vm()
                    public_ip = vmObj.get_ip()
                    self.logger.debug("SSH into VM: %s" % public_ip.ipaddress.ipaddress)

                    ssh = vm.get_ssh_client(ipaddress=public_ip.ipaddress.ipaddress)

                    self.logger.debug("Ping gateway from VM")
                    result = str(ssh.execute(ssh_command))

                    self.logger.debug("SSH result: %s; COUNT is ==> %s" % (result, result.count("3 packets received")))
                except Exception as e:
                    self.fail("SSH Access failed for %s: %s" % (vmObj.get_ip(), e))

                self.assertEqual(result.count("3 packets received"), 1, "Ping gateway from VM should be successful")


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
