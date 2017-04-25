import copy
import time

from nose.plugins.attrib import attr

from marvin.cloudstackTestCase import cloudstackTestCase
from marvin.lib.base import (
    VpnUser,
    Vpn,
    PublicIPAddress,
    VirtualMachine,
    Network,
    VPC,
    VpnCustomerGateway,
    NetworkACLList,
    Account
)
from marvin.lib.common import (
    get_default_redundant_vpc_offering,
    list_routers,
    list_hosts,
    get_template,
    get_default_virtual_machine_offering,
    get_default_network_offering,
    get_default_vpc_offering,
    get_domain,
    get_zone
)
from marvin.lib.utils import (
    get_process_status,
    cleanup_resources
)
from marvin.utils.MarvinLog import MarvinLog


class Services:
    """Test VPC VPN Services.
    """

    def __init__(self):
        self.services = {
            "account": {
                "email": "test@test.com",
                "firstname": "Test",
                "lastname": "User",
                "username": "test",
                "password": "password",
            },
            "vpc": {
                "name": "TestVPC",
                "displaytext": "TestVPC",
                "cidr": "10.100.0.0/16"
            },
            "network_1": {
                "name": "Test Network 1",
                "displaytext": "Test Network 1",
                "netmask": "255.255.255.0",
                "gateway": "10.100.1.1"
            },
            "vpcN": {
                "name": "TestVPC{N}",
                "displaytext": "VPC{N}",
                "cidr": "10.{N}.0.0/16"
            },
            "network_N": {
                "name": "Test Network {N}",
                "displaytext": "Test Network {N}",
                "netmask": "255.255.255.0",
                "gateway": "10.{N}.1.1"
            },
            "vpn": {
                "vpn_user": "root",
                "vpn_pass": "Md1s#dc",
                "vpn_pass_fail": "abc!123",  # too short
                "iprange": "10.2.2.1-10.2.2.10",
                "fordisplay": "true"
            },
            "vpncustomergateway": {
                "esppolicy": "3des-md5;modp1536",
                "ikepolicy": "3des-md5;modp1536",
                "ipsecpsk": "ipsecpsk"
            },
            "natrule": {
                "protocol": "TCP",
                "cidrlist": '0.0.0.0/0',
            },
            "http_rule": {
                "privateport": 80,
                "publicport": 80,
                "startport": 80,
                "endport": 80,
                "cidrlist": '0.0.0.0/0',
                "protocol": "TCP"
            },
            "virtual_machine": {
                "displayname": "Test VM",
                "username": "root",
                "password": "password",
                "ssh_port": 22,
                "privateport": 22,
                "publicport": 22,
                "protocol": 'TCP',
            }
        }


class TestVpcVpn(cloudstackTestCase):

    @classmethod
    def setUpClass(cls, redundant=False):
        cls.logger = MarvinLog(MarvinLog.LOGGER_TEST).get_logger()

        test_client = super(TestVpcVpn, cls).getClsTestClient()
        cls.apiclient = test_client.getApiClient()
        cls.services = Services().services

        cls.zone = get_zone(cls.apiclient, test_client.getZoneForTests())
        cls.domain = get_domain(cls.apiclient)

        cls.vpc_offering = get_default_redundant_vpc_offering(cls.apiclient) if redundant else get_default_vpc_offering(cls.apiclient)
        cls.logger.debug("VPC Offering '%s' selected", cls.vpc_offering.name)

        cls.network_offering = get_default_network_offering(cls.apiclient)
        cls.logger.debug("Network Offering '%s' selected", cls.network_offering.name)

        cls.virtual_machine_offering = get_default_virtual_machine_offering(cls.apiclient)
        cls.logger.debug("Virtual Machine Offering '%s' selected", cls.virtual_machine_offering.name)

        cls.account = Account.create(cls.apiclient, services=cls.services["account"])
        cls.logger.debug("Successfully created account: %s, id: %s" % (cls.account.name, cls.account.id))

        cls.hypervisor = test_client.getHypervisorInfo()

        cls._cleanup = [cls.account]

        cls.template = get_template(
            cls.apiclient,
            cls.zone.id
        )

        return

    def setUp(self):
        self.cleanup = []

    def tearDown(self):
        try:
            self.logger.debug("Cleaning up resources")
            self.cleanup.reverse()
            cleanup_resources(self.apiclient, self.cleanup, self.logger)
        except Exception as e:
            raise Exception("Cleanup failed with %s" % e)

    @classmethod
    def tearDownClass(cls):
        try:
            cleanup_resources(cls.apiclient, cls._cleanup, cls.logger)
        except Exception as e:
            raise Exception("Warning: Exception during cleanup : %s" % e)
        return

    def get_host_details(self, router, username='root', password='password', port=22):
        hosts = list_hosts(self.apiclient, id=router.hostid, type="Routing")

        self.assertEqual(isinstance(hosts, list), True, "Check for list hosts response return valid data")

        host = hosts[0]
        host.user = username
        host.passwd = password
        host.port = port
        return host

    def get_router_state(self, router):
        host = self.get_host_details(router)

        router_state = "UNKNOWN"
        try:
            router_state = get_process_status(
                host.ipaddress,
                host.port,
                host.user,
                host.passwd,
                router.linklocalip,
                "/opt/cloud/bin/checkrouter.sh | cut -d\" \" -f2"
            )
        except:
            self.logger.debug("Oops, unable to determine redundant state for router with link local address %s" % (router.linklocalip))
            pass
        self.logger.debug("The router with link local address %s reports state %s" % (router.linklocalip, router_state))
        return router_state[0]

    def routers_in_right_state(self, vpcid=None):
        self.logger.debug("Check whether routers are happy")
        max_tries = 30
        test_tries = 0
        master_found = 0
        backup_found = 0
        while test_tries < max_tries:
            routers = list_routers(self.apiclient, account=self.account.name, domainid=self.account.domainid, vpcid=vpcid)
            self.assertEqual(isinstance(routers, list), True,
                             "Check for list routers response return valid data")
            for router in routers:
                if not router.isredundantrouter:
                    self.logger.debug("Router %s has is_redundant_router %s so continuing" % (router.linklocalip, router.isredundantrouter))
                    return True
                router_state = self.get_router_state(router)
                if router_state == "BACKUP":
                    backup_found += 1
                    self.logger.debug("Router %s currently is in state BACKUP" % router.linklocalip)
                if router_state == "MASTER":
                    master_found += 1
                    self.logger.debug("Router %s currently is in state MASTER" % router.linklocalip)
            if master_found > 0 and backup_found > 0:
                self.logger.debug("Found at least one router in MASTER and one in BACKUP state so continuing")
                break
            test_tries += 1
            self.logger.debug("Testing router states round %s/%s" % (test_tries, max_tries))
            time.sleep(2)

        if master_found == 1 and backup_found == 1:
            return True
        return False

    def _test_vpc_site2site_vpn(self, vpc_offering, num_VPCs=3):
        # Number of VPNs (to test) is number_of_VPCs - 1
        # By default test setting up 2 VPNs from VPC0, requiring total of 3 VPCs

        maxnumVM = num_VPCs - 1
        # Create VPC i
        vpc_list = []
        for i in range(num_VPCs):
            # Generate VPC (mostly subnet) info
            vpcservice_n = copy.deepcopy(self.services["vpcN"])
            for key in vpcservice_n.keys():
                vpcservice_n[key] = vpcservice_n[key].format(N=`i`)

            vpc_n = VPC.create(
                apiclient=self.apiclient,
                services=vpcservice_n,
                networkDomain="vpc%d.vpn" % i,
                vpcofferingid=vpc_offering.id,
                zoneid=self.zone.id,
                account=self.account.name,
                domainid=self.domain.id
            )
            self.assertIsNotNone(vpc_n, "VPC%d creation failed" % i)
            vpc_list.append(vpc_n)
            self.cleanup.append(vpc_n)
            self.logger.debug("VPC%d %s created" % (i, vpc_list[i].id))

        default_acl = NetworkACLList.list(self.apiclient, name="default_allow")[0]

        # Create network in VPC i
        ntwk_list = []
        for i in range(num_VPCs):
            # Generate network (mostly subnet) info
            ntwk_info_n = copy.deepcopy(self.services["network_N"])
            for key in ntwk_info_n.keys():
                ntwk_info_n[key] = ntwk_info_n[key].format(N=`i`)

            ntwk_n = Network.create(
                apiclient=self.apiclient,
                services=ntwk_info_n,
                accountid=self.account.name,
                domainid=self.account.domainid,
                networkofferingid=self.network_offering.id,
                zoneid=self.zone.id,
                vpcid=vpc_list[i].id,
                aclid=default_acl.id
            )
            self.assertIsNotNone(ntwk_n, "Network%d failed to create" % i)
            self.cleanup.append(ntwk_n)
            ntwk_list.append(ntwk_n)
            self.logger.debug("Network%d %s created in VPC %s" % (i, ntwk_list[i].id, vpc_list[i].id))

        # Deploy a vm in network i
        vm_list = []
        vm_n = None

        for i in range(num_VPCs):
            vm_n = VirtualMachine.create(self.apiclient, services=self.services["virtual_machine"],
                                         templateid=self.template.id,
                                         zoneid=self.zone.id,
                                         accountid=self.account.name,
                                         domainid=self.account.domainid,
                                         serviceofferingid=self.virtual_machine_offering.id,
                                         networkids=[ntwk_list[i].id],
                                         hypervisor=self.hypervisor,
                                         mode='advanced' if (i == 0) or (i == maxnumVM) else 'default'
                                         )
            self.assertIsNotNone(vm_n, "VM%d failed to deploy" % i)
            self.cleanup.append(vm_n)
            vm_list.append(vm_n)
            self.logger.debug("VM%d %s deployed in VPC %s" % (i, vm_list[i].id, vpc_list[i].id))
            self.assertEquals(vm_n.state, 'Running', "VM%d is not running" % i)

        # 4) Enable Site-to-Site VPN for VPC
        vpn_response_list = []
        for i in range(num_VPCs):
            vpn_response = Vpn.createVpnGateway(self.apiclient, vpc_list[i].id)
            self.assertIsNotNone(vpn_response, "Failed to enable VPN Gateway %d" % i)
            vpn_response_list.append(vpn_response)
            self.logger.debug("VPN gateway for VPC%d %s enabled" % (i, vpc_list[i].id))

        # 5) Add VPN Customer gateway info
        vpn_cust_gw_list = []
        services = self.services["vpncustomergateway"]
        for i in range(num_VPCs):
            src_nat_list = PublicIPAddress.list(
                self.apiclient,
                account=self.account.name,
                domainid=self.account.domainid,
                listall=True,
                issourcenat=True,
                vpcid=vpc_list[i].id
            )
            ip = src_nat_list[0]

            customer_response = VpnCustomerGateway.create(self.apiclient, services, "Peer VPC" + `i`, ip.ipaddress, vpc_list[i].cidr, self.account.name, self.domain.id)
            self.cleanup.insert(0, customer_response)  # this has to be cleaned up after the VPCs have been destroyed (due to client connectionsonections)
            vpn_cust_gw_list.append(customer_response)
            self.logger.debug("VPN customer gateway added for VPC%d %s enabled" % (i, vpc_list[i].id))

        # Before the next step ensure the last VPC is up and running
        # Routers in the right state?
        self.assertEqual(self.routers_in_right_state(vpcid=vpc_list[maxnumVM].id), True, "Check whether the routers are in the right state.")

        # 6) Connect VPCi with VPC0
        for i in range(num_VPCs)[1:]:
            passiveVpn = Vpn.createVpnConnection(self.apiclient, vpn_cust_gw_list[0].id, vpn_response_list[i]['id'], True)
            self.logger.debug("VPN passive connection created for VPC%d %s" % (i, vpc_list[i].id))

            vpnconn2_response = Vpn.createVpnConnection(self.apiclient, vpn_cust_gw_list[i].id, vpn_response_list[0]['id'])
            self.logger.debug("VPN connection created for VPC%d %s" % (0, vpc_list[0].id))

            self.assertEqual(vpnconn2_response['state'], "Connected", "Failed to connect between VPCs 0 and %d!" % i)
            self.logger.debug("VPN connected between VPC0 and VPC%d" % i)

            time.sleep(5)
            self.logger.debug("Resetting VPN connection with id %s" % (passiveVpn['id']))
            Vpn.resetVpnConnection(self.apiclient, passiveVpn['id'])
            self.logger.debug("Waiting 20s for the VPN to connect")
            time.sleep(20)

        # First the last VM
        # setup ssh connection to vm maxnumVM
        self.logger.debug("Setup SSH connection to last VM created (%d) to ensure availability for ping tests" % maxnumVM)
        ssh_max_client = vm_list[maxnumVM].get_ssh_client(retries=20)
        self.assertIsNotNone(ssh_max_client, "Failed to setup SSH to last VM created (%d)" % maxnumVM)

        self.logger.debug("Setup SSH connection to first VM created (0) to ensure availability for ping tests")
        ssh_client = vm_list[0].get_ssh_client(retries=10)
        self.assertIsNotNone(ssh_client, "Failed to setup SSH to VM0")

        if ssh_client:
            # run ping test
            for i in range(num_VPCs)[1:]:
                packet_loss = ssh_client.execute(
                    "/bin/ping -c 3 -t 10 " + vm_list[i].nic[0].ipaddress + " |grep packet|cut -d ' ' -f 7| cut -f1 -d'%'")[0]
                self.assertEquals(int(packet_loss), 0, "Ping towards vm" + `i` + "did not succeed")
                self.logger.debug("SUCCESS! Ping from vm0 to vm%d succeeded." % i)
        else:
            self.fail("Failed to setup ssh connection to %s" % vm_list[0].public_ip)

        return

    @attr(tags=["advanced"], required_hardware="true")
    def test_01_vpc_remote_access_vpn(self):
        """Test Remote Access VPN in VPC"""
        # 1) Create VPC
        vpc = VPC.create(
            apiclient=self.apiclient,
            services=self.services["vpc"],
            networkDomain="vpc.vpn",
            vpcofferingid=self.vpc_offering.id,
            zoneid=self.zone.id,
            account=self.account.name,
            domainid=self.domain.id
        )

        self.assertIsNotNone(vpc, "VPC creation failed")
        self.logger.debug("VPC %s created" % (vpc.id))

        self.cleanup.append(vpc)

        # 2) Create network in VPC
        ntwk = Network.create(
            apiclient=self.apiclient,
            services=self.services["network_1"],
            accountid=self.account.name,
            domainid=self.domain.id,
            networkofferingid=self.network_offering.id,
            zoneid=self.zone.id,
            vpcid=vpc.id
        )

        self.assertIsNotNone(ntwk, "Network failed to create")
        self.logger.debug("Network %s created in VPC %s" % (ntwk.id, vpc.id))

        self.cleanup.append(ntwk)

        # 3) Deploy a vm
        vm = VirtualMachine.create(self.apiclient, services=self.services["virtual_machine"],
                                   templateid=self.template.id,
                                   zoneid=self.zone.id,
                                   accountid=self.account.name,
                                   domainid=self.domain.id,
                                   serviceofferingid=self.virtual_machine_offering.id,
                                   networkids=ntwk.id,
                                   hypervisor=self.hypervisor
                                   )
        self.assertIsNotNone(vm, "VM failed to deploy")
        self.assertEquals(vm.state, 'Running', "VM is not running")
        self.debug("VM %s deployed in VPC %s" % (vm.id, vpc.id))

        self.logger.debug("Deployed virtual machine: OK")
        self.cleanup.append(vm)

        # 4) Enable VPN for VPC
        src_nat_list = PublicIPAddress.list(
            self.apiclient,
            account=self.account.name,
            domainid=self.account.domainid,
            listall=True,
            issourcenat=True,
            vpcid=vpc.id
        )
        ip = src_nat_list[0]

        self.logger.debug("Acquired public ip address: OK")

        vpn = Vpn.create(self.apiclient,
                         publicipid=ip.id,
                         account=self.account.name,
                         domainid=self.account.domainid,
                         iprange=self.services["vpn"]["iprange"],
                         fordisplay=self.services["vpn"]["fordisplay"]
                         )

        self.assertIsNotNone(vpn, "Failed to create Remote Access VPN")
        self.logger.debug("Created Remote Access VPN: OK")

        vpn_user = None
        # 5) Add VPN user for VPC
        vpn_user = VpnUser.create(self.apiclient,
                                  account=self.account.name,
                                  domainid=self.account.domainid,
                                  username=self.services["vpn"]["vpn_user"],
                                  password=self.services["vpn"]["vpn_pass"]
                                  )

        self.assertIsNotNone(vpn_user, "Failed to create Remote Access VPN User")
        self.logger.debug("Created VPN User: OK")

        # TODO: Add an actual remote vpn connection test from a remote vpc

        # 9) Disable VPN for VPC
        vpn.delete(self.apiclient)

        self.logger.debug("Deleted the Remote Access VPN: OK")

    @attr(tags=["advanced"], required_hardware="true")
    def test_02_vpc_site2site_vpn(self):
        """Test Site 2 Site VPN Across VPCs"""
        # Set up 1 VPNs; needs 2 VPCs
        self._test_vpc_site2site_vpn(self.vpc_offering, 2)

    @attr(tags=["advanced"], required_hardware="true")
    def test_03_redundant_vpc_site2site_vpn(self):
        """Test Site 2 Site VPN Across redundant VPCs"""
        # Set up 1 VPNs; needs 2 VPCs
        self._test_vpc_site2site_vpn(get_default_redundant_vpc_offering(self.apiclient), 2)

    @attr(tags=["advanced"], required_hardware="true")
    def test_04_vpc_site2site_multiple_vpn(self):
        """Test Site 2 Site multiple VPNs Across VPCs"""
        # Set up 3 VPNs; needs 4 VPCs
        self._test_vpc_site2site_vpn(self.vpc_offering, 4)
