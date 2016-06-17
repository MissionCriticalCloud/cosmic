""" Tests for Port Forwarding Rules Removing
"""
from marvin.cloudstackTestCase import *
from marvin.cloudstackAPI import *
from marvin.lib.utils import *
from marvin.lib.base import *
from marvin.lib.common import *
from nose.plugins.attrib import attr

import time
import logging

class Services:
    """Test network services - Port Forwarding Rules Remove Test Data Class.
    """

    def __init__(self):
        self.services = {
            "configurableData": {
                "host": {
                    "password": "password",
                    "username": "root",
                    "port": 22
                }
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
            "host1": None,
            "service_offering": {
                "name": "Tiny Instance",
                "displaytext": "Tiny Instance",
                "cpunumber": 1,
                "cpuspeed": 100,
                "memory": 128,
            },
            "network_offering": {
                "name": 'VPC Network offering',
                "displaytext": 'VPC Network off',
                "guestiptype": 'Isolated',
                "supportedservices": 'Vpn,Dhcp,Dns,SourceNat,PortForwarding,Lb,UserData,StaticNat,NetworkACL',
                "traffictype": 'GUEST',
                "availability": 'Optional',
                "useVpc": 'on',
                "serviceProviderList": {
                    "Vpn": 'VpcVirtualRouter',
                    "Dhcp": 'VpcVirtualRouter',
                    "Dns": 'VpcVirtualRouter',
                    "SourceNat": 'VpcVirtualRouter',
                    "PortForwarding": 'VpcVirtualRouter',
                    "Lb": 'VpcVirtualRouter',
                    "UserData": 'VpcVirtualRouter',
                    "StaticNat": 'VpcVirtualRouter',
                    "NetworkACL": 'VpcVirtualRouter'
                },
            },
            "network_offering_no_lb": {
                "name": 'VPC Network offering',
                "displaytext": 'VPC Network off',
                "guestiptype": 'Isolated',
                "supportedservices": 'Dhcp,Dns,SourceNat,PortForwarding,UserData,StaticNat,NetworkACL',
                "traffictype": 'GUEST',
                "availability": 'Optional',
                "useVpc": 'on',
                "serviceProviderList": {
                    "Dhcp": 'VpcVirtualRouter',
                    "Dns": 'VpcVirtualRouter',
                    "SourceNat": 'VpcVirtualRouter',
                    "PortForwarding": 'VpcVirtualRouter',
                    "UserData": 'VpcVirtualRouter',
                    "StaticNat": 'VpcVirtualRouter',
                    "NetworkACL": 'VpcVirtualRouter'
                },
            },
            "redundant_vpc_offering": {
                "name": 'Redundant VPC off',
                "displaytext": 'Redundant VPC off',
                "supportedservices": 'Dhcp,Dns,SourceNat,PortForwarding,Vpn,Lb,UserData,StaticNat',
                "serviceProviderList": {
                    "Vpn": 'VpcVirtualRouter',
                    "Dhcp": 'VpcVirtualRouter',
                    "Dns": 'VpcVirtualRouter',
                    "SourceNat": 'VpcVirtualRouter',
                    "PortForwarding": 'VpcVirtualRouter',
                    "Lb": 'VpcVirtualRouter',
                    "UserData": 'VpcVirtualRouter',
                    "StaticNat": 'VpcVirtualRouter',
                    "NetworkACL": 'VpcVirtualRouter'
                },
                "serviceCapabilityList": {
                    "SourceNat": {
                        "RedundantRouter": 'true'
                    }
                },
            },
            "isolated_network_offering": {
                "name": "Network offering-DA services",
                "displaytext": "Network offering-DA services",
                "guestiptype": "Isolated",
                "supportedservices":
                    "Dhcp,Dns,SourceNat,PortForwarding,Vpn,Firewall,Lb,UserData,StaticNat",
                "traffictype": "GUEST",
                "availability": "Optional'",
                "serviceProviderList": {
                    "Dhcp": "VirtualRouter",
                    "Dns": "VirtualRouter",
                    "SourceNat": "VirtualRouter",
                    "PortForwarding": "VirtualRouter",
                    "Vpn": "VirtualRouter",
                    "Firewall": "VirtualRouter",
                    "Lb": "VirtualRouter",
                    "UserData": "VirtualRouter",
                    "StaticNat": "VirtualRouter"
                }
            },
            "vpc_offering": {
                "name": "VPC off",
                "displaytext": "VPC off",
                "supportedservices":
                    "Dhcp,Dns,SourceNat,PortForwarding,Vpn,Lb,UserData,StaticNat,NetworkACL"
            },
            "vpc": {
                "name": "TestVPC",
                "displaytext": "TestVPC",
                "cidr": '10.0.0.1/24'
            },
            "network": {
                "name": "Test Network",
                "displaytext": "Test Network",
                "netmask": '255.255.255.0'
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
            "natrule": {
                "privateport": 22,
                "publicport": 22,
                "startport": 22,
                "endport": 22,
                "protocol": "TCP",
                "cidrlist": '0.0.0.0/0',
            },
            "ostype": 'CentOS 5.3 (64-bit)',
            "timeout": 10,
        }

class TestPortforwardRemove(cloudstackTestCase):

    @classmethod
    def setUpClass(cls):

        cls.testClient = super(TestPortforwardRemove, cls).getClsTestClient()
        cls.api_client = cls.testClient.getApiClient()

        cls.services = Services().services
        # Get Zone, Domain and templates
        cls.domain = get_domain(cls.api_client)
        cls.zone = get_zone(cls.api_client, cls.testClient.getZoneForTests())
        cls.services['mode'] = cls.zone.networktype
        cls.template = get_template(
            cls.api_client,
            cls.zone.id,
            cls.services["ostype"])

        cls.services["virtual_machine"]["zoneid"] = cls.zone.id
        cls.services["virtual_machine"]["template"] = cls.template.id

        cls.service_offering = ServiceOffering.create(
            cls.api_client,
            cls.services["service_offering"])
        cls._cleanup = [cls.service_offering]

        cls.logger = logging.getLogger('TestPortforwardRemove')
        cls.stream_handler = logging.StreamHandler()
        cls.logger.setLevel(logging.DEBUG)
        cls.logger.addHandler(cls.stream_handler)

    @classmethod
    def tearDownClass(cls):
        try:
            cleanup_resources(cls.api_client, cls._cleanup, cls.logger)
        except Exception as e:
            raise Exception("Warning: Exception during cleanup : %s" % e)
        return

    def setUp(self):
        self.apiclient = self.testClient.getApiClient()

        self.logger.debug("Creating Admin Account for Domain ID ==> %s" % self.domain.id)
        self.account = Account.create(
            self.apiclient,
            self.services["account"],
            admin=True,
            domainid=self.domain.id)

        self.cleanup = []
        return

    def tearDown(self):
        try:
            cleanup_resources(self.apiclient, self.cleanup, self.logger)
        except Exception as e:
            raise Exception("Warning: Exception during cleanup : %s" % e)
        return

    @attr(tags=["advanced"], required_hardware="true")
    def test_01_remove_portforward_single_vpc(self):
        self.logger.debug("Starting test for single VPC")
        vpc_off = VpcOffering.create(
            self.apiclient,
            self.services["vpc_offering"])

        self.logger.debug("Enabling the VPC offering created")
        vpc_off.update(self.apiclient, state='Enabled')

        self.perform_portforward_remove_tests(vpc_off)

    @attr(tags=["advanced"], required_hardware="true")
    def test_02_remove_portforward_redundant_vpc(self):
        self.logger.debug("Starting test for Redundant VPC")
        vpc_off = VpcOffering.create(
            self.apiclient,
            self.services["redundant_vpc_offering"])

        self.logger.debug("Enabling the VPC offering created")
        vpc_off.update(self.apiclient, state='Enabled')

        self.perform_portforward_remove_tests(vpc_off)

    @attr(tags=["advanced"], required_hardware="true")
    def test_03_remove_portforward_isolated(self):
        self.logger.debug("Starting test for Isolated network")
        self.perform_portforward_remove_tests(None)

    # Generic methods
    def perform_portforward_remove_tests(self, vpc_off):
        vpc_1 = None
        if vpc_off is None:
            self.logger.debug("No need to create a VPC, creating isolated network")
            network_1 = self.createIsolatedNetwork()
        else:
            self.logger.debug("Creating VPC with offering ID %s" % vpc_off.id)
            vpc_1 = self.createVPC(vpc_off, cidr = '10.0.0.0/16')
            self.cleanup += [vpc_1, vpc_off, self.account]
            self.logger.debug("Creating network inside VPC")
            network_1 = self.createNetwork(vpc_1, gateway = '10.0.0.1')
            acl1 = self.createACL(vpc_1)
            self.createACLItem(acl1.id, cidr = "0.0.0.0/0")
            self.replaceNetworkAcl(acl1.id, network_1)

        self.logger.debug("Deploying VM and create port forwarding")
        vm1 = self.createVM(network_1)
        public_ip_1 = self.acquire_publicip(vpc_1, network_1)
        nat_rule_1 = self.create_natrule(vpc_1, vm1, public_ip_1, network_1)

        self.logger.debug("Check whether routers are happy")
        routers = list_routers(self.apiclient, account=self.account.name, domainid=self.account.domainid)

        self.assertEqual(isinstance(routers, list), True,
                         "Check for list routers response return valid data")
        self.check_routers_state(routers)

        # All should be fine initially
        for router in routers:
            self._perform_health_tests(router)

        self.destroy_virtual_machine(vm1)

        self.logger.debug("Test health after destroying VM with its port forward)")
        for router in routers:
            self._perform_health_tests(router)

        self.logger.debug("Creating a new VM with port forward while reusing same public ip as before")
        vm1 = self.createVM(network_1)
        self.cleanup.insert(0, vm1)
        nat_rule_1 = self.create_natrule(vpc_1, vm1, public_ip_1, network_1)

        self.logger.debug("Test health after new VM with its port forward is created)")
        for router in routers:
            self._perform_health_tests(router)

        # We should also survive a restart with cleanup
        #if vpc_off is None:
        #    self.restart_network_with_cleanup(network_1, True)
        #else:
        #    self.restart_vpc_with_cleanup(vpc_1, True)
        #self.logger.debug("Give router 30 seconds to reconfigure properly..")
        #time.sleep(30)

        #self.logger.debug("Getting the router info again after the cleanup (router names / ip addresses changed)")
        #routers = list_routers(self.apiclient, account=self.account.name, domainid=self.account.domainid)

        #self.assertEqual(isinstance(routers, list), True,
        #                 "Check for list routers response return valid data")
        #self.logger.debug("Check whether routers are happy")
        #self.check_routers_state(routers)

        #self.logger.debug("Test health a final time after restart with cleanup)")
        #for router in routers:
        #    self._perform_health_tests(router)

    def _perform_health_tests(self, router):
        self.logger.debug("Checking health of router %s, state %s", router.linklocalip, router.redundantstate)
        self.test_public_nic_present(router)
        self.test_default_route_present(router)

    def test_public_nic_present(self, router):
        hosts = list_hosts(self.apiclient, id=router.hostid, type="Routing")

        self.assertEqual(isinstance(hosts, list), True, "Check for list hosts response return valid data")

        host = hosts[0]
        host.user = self.services["configurableData"]["host"]["username"]
        host.passwd = self.services["configurableData"]["host"]["password"]
        host.port = self.services["configurableData"]["host"]["port"]

        public_nic = "eth2"
        if router.vpcid:
            public_nic = "eth1"

        expected_public_state = "up"
        if router.isredundantrouter and router.redundantstate == "BACKUP":
            expected_public_state = "down"

        try:
            interface_state_result = get_process_status(
                host.ipaddress,
                host.port,
                host.user,
                host.passwd,
                router.linklocalip,
                "cat /sys/class/net/%s/operstate" % public_nic
            )
        except KeyError:
            self.skipTest("Provide a marvin config file with host credentials to run %s" % self._testMethodName)

        self.logger.debug("Result from the Router on IP '%s' is: State of interface '%s' is '%s', expected state is '%s'" % (router.linklocalip, public_nic, interface_state_result[0], expected_public_state))
        self.assertEqual(interface_state_result[0], expected_public_state, msg="Router should have public nic in the correct state!")

    def test_default_route_present(self, router):
        hosts = list_hosts(self.apiclient, id=router.hostid, type="Routing")

        self.assertEqual(isinstance(hosts, list), True, "Check for list hosts response return valid data")

        host = hosts[0]
        host.user = self.services["configurableData"]["host"]["username"]
        host.passwd = self.services["configurableData"]["host"]["password"]
        host.port = self.services["configurableData"]["host"]["port"]

        public_nic = "eth2"
        if router.vpcid:
            public_nic = "eth1"

        if router.isredundantrouter and router.redundantstate == "BACKUP":
            self.logger.debug("Skipping the Router on IP '%s' because its state is not MASTER but '%s'" % (router.linklocalip, router.redundantstate))
            return

        try:
            default_gw_interface = get_process_status(
                host.ipaddress,
                host.port,
                host.user,
                host.passwd,
                router.linklocalip,
                "ip route get 8.8.8.8 | grep %s | awk {'print $5'}" % public_nic
            )
        except KeyError:
            self.skipTest("Provide a marvin config file with host credentials to run %s" % self._testMethodName)

        self.logger.debug("Result from the Router on IP '%s' is: Interface of default gateway is '%s', expected '%s'" % (router.linklocalip, default_gw_interface[0], public_nic))
        self.assertEqual(default_gw_interface[0], public_nic, msg="Router should have public nic in the correct state!")


    def destroy_virtual_machine(self, vm):
        try:
            self.logger.debug("Destroying vm %s", vm.name)
            cmd = destroyVirtualMachine.destroyVirtualMachineCmd()
            cmd.expunge = True
            cmd.id = vm.id
            self.apiclient.destroyVirtualMachine(cmd)
        except Exception, e:
            self.fail('Unable to destroy VM due to %s ' % e)

    def create_natrule(self, vpc, virtual_machine, public_ip, network):
        self.logger.debug("Creating NAT rule in network for vm with public IP")

        nat_service = self.services["natrule"]

        if vpc is not None:
            nat_rule = NATRule.create(
                self.apiclient,
                virtual_machine,
                nat_service,
                ipaddressid=public_ip.ipaddress.id,
                openfirewall=False,
                networkid=network.id,
                vpcid=vpc.id)

            self.logger.debug("Adding NetworkACL rules to make NAT rule accessible")
            nwacl_nat = NetworkACL.create(
                self.apiclient,
                networkid=network.id,
                services=nat_service,
                traffictype='Ingress'
            )
            self.logger.debug('nwacl_nat=%s' % nwacl_nat.__dict__)

        else:
            nat_rule = NATRule.create(
                self.apiclient,
                virtual_machine,
                nat_service,
                ipaddressid=public_ip.ipaddress.id,
                openfirewall=True,
                networkid=network.id)

        return nat_rule

    def acquire_publicip(self, vpc, network):
        self.logger.debug("Associating public IP for network: %s" % network.name)

        if vpc is not None:
            public_ip = PublicIPAddress.create(
                self.apiclient,
                accountid=self.account.name,
                zoneid=self.zone.id,
                domainid=self.account.domainid,
                networkid=network.id,
                vpcid=vpc.id)
        else:
            public_ip = PublicIPAddress.create(
                self.apiclient,
                accountid=self.account.name,
                zoneid=self.zone.id,
                domainid=self.account.domainid,
                networkid=network.id)
        self.logger.debug("Associated %s with network %s" % (public_ip.ipaddress.ipaddress, network.id))

        return public_ip

    def createVPC(self, vpc_offering, cidr = '10.1.1.1/16'):
        try:
            self.logger.debug("Creating a VPC in the account: %s" % self.account.name)
            self.services["vpc"]["cidr"] = cidr

            vpc = VPC.create(
                self.apiclient,
                self.services["vpc"],
                vpcofferingid=vpc_offering.id,
                zoneid=self.zone.id,
                account=self.account.name,
                domainid=self.account.domainid)

            self.logger.debug("Created VPC with ID: %s" % vpc.id)
        except Exception, e:
            self.fail('Unable to create VPC due to %s ' % e)

        return vpc

    def createVM(self, network):
        try:
            self.logger.debug('Creating VM in network=%s' % network.name)
            vm = VirtualMachine.create(
                self.apiclient,
                self.services["virtual_machine"],
                accountid=self.account.name,
                domainid=self.account.domainid,
                serviceofferingid=self.service_offering.id,
                networkids=[str(network.id)]
            )
            self.logger.debug("Created VM with ID: %s" % vm.id)
        except Exception, e:
            self.fail('Unable to create virtual machine due to %s ' % e)

        return vm

    def createACL(self, vpc):
        createAclCmd = createNetworkACLList.createNetworkACLListCmd()
        createAclCmd.name = "ACL-Test-%s" % vpc.id
        createAclCmd.description = createAclCmd.name
        createAclCmd.vpcid = vpc.id
        try:
            acl = self.apiclient.createNetworkACLList(createAclCmd)
            self.assertIsNotNone(acl.id, "Failed to create ACL.")

            self.logger.debug("Created ACL with ID: %s" % acl.id)
        except Exception, e:
            self.fail('Unable to create ACL due to %s ' % e)

        return acl

    def createACLItem(self, aclId, cidr = "0.0.0.0/0"):
        createAclItemCmd = createNetworkACL.createNetworkACLCmd()
        createAclItemCmd.cidr = cidr
        createAclItemCmd.protocol = "All"
        createAclItemCmd.number = "1"
        createAclItemCmd.action = "Allow"
        createAclItemCmd.aclid = aclId
        try:
            aclItem = self.apiclient.createNetworkACL(createAclItemCmd)
            self.assertIsNotNone(aclItem.id, "Failed to create ACL item.")

            self.logger.debug("Created ACL Item ID: %s" % aclItem.id)
        except Exception, e:
            self.fail('Unable to create ACL Item due to %s ' % e)

    def createNetwork(self, vpc, net_offering = "network_offering", gateway = '10.1.1.1'):
        try:
            self.logger.debug('Create NetworkOffering')
            net_offerring = self.services[net_offering]
            net_offerring["name"] = "NET_OFF-%s" % gateway
            nw_off = NetworkOffering.create(
                self.apiclient,
                net_offerring,
                conservemode=False)

            nw_off.update(self.apiclient, state='Enabled')

            self.logger.debug('Created and Enabled NetworkOffering')

            self.services["network"]["name"] = "NETWORK-%s" % gateway

            self.logger.debug('Adding Network=%s' % self.services["network"])
            obj_network = Network.create(
                self.apiclient,
                self.services["network"],
                accountid=self.account.name,
                domainid=self.account.domainid,
                networkofferingid=nw_off.id,
                zoneid=self.zone.id,
                gateway=gateway,
                vpcid=vpc.id
            )

            self.logger.debug("Created network with ID: %s" % obj_network.id)
        except Exception, e:
            self.fail('Unable to create a Network with offering=%s because of %s ' % (net_offerring, e))

        self.cleanup.insert(0, nw_off)
        self.cleanup.insert(0, obj_network)

        return obj_network

    def createIsolatedNetwork(self):

        self.services["isolated_network_offering"]["egress_policy"] = "true"

        self.logger.debug("Creating Network Offering on zone %s" % self.zone.id)
        network_offering = NetworkOffering.create(self.api_client,
                                                  self.services["isolated_network_offering"],
                                                  conservemode=True)

        network_offering.update(self.api_client, state='Enabled')

        self.logger.debug("Creating Network for Account %s using offering %s" % (self.account.name, network_offering.id))
        network_obj = Network.create(self.api_client,
                                     self.services["network"],
                                     accountid=self.account.name,
                                     domainid=self.account.domainid,
                                     networkofferingid=network_offering.id,
                                     zoneid=self.zone.id
                                     )

        self.cleanup.insert(0, network_offering)
        self.cleanup.insert(0, network_obj)

        return network_obj

    def replaceNetworkAcl(self, aclId, network):
        self.logger.debug("Replacing Network ACL with ACL ID ==> %s" % aclId)
        replaceNetworkACLListCmd = replaceNetworkACLList.replaceNetworkACLListCmd()
        replaceNetworkACLListCmd.aclid = aclId
        replaceNetworkACLListCmd.networkid = network.id
        self._replaceAcl(replaceNetworkACLListCmd)

    def _replaceAcl(self, command):
        try:
            successResponse = self.apiclient.replaceNetworkACLList(command);
        except Exception as e:
            self.fail("Failed to replace ACL list due to %s" % e)

        self.assertTrue(successResponse.success, "Failed to replace ACL list.")

    def restart_vpc_with_cleanup(self, vpc, cleanup = True):
        try:
            self.logger.debug("Restarting VPC %s with cleanup" % vpc.id)
            cmd = restartVPC.restartVPCCmd()
            cmd.id = vpc.id
            cmd.cleanup = cleanup
            cmd.makeredundant = False
            self.api_client.restartVPC(cmd)
        except Exception, e:
            self.fail('Unable to restart VPC with cleanup due to %s ' % e)

    def restart_network_with_cleanup(self, network, cleanup = True):
        try:
            self.logger.debug("Restarting network %s with cleanup" % network.id)
            cmd = restartNetwork.restartNetworkCmd()
            cmd.id = network.id
            cmd.cleanup = cleanup
            self.api_client.restartNetwork(cmd)
        except Exception, e:
            self.fail('Unable to restart network with cleanup due to %s ' % e)

    def check_routers_state(self, routers, status_to_check="MASTER", expected_count=1):
        vals = ["MASTER", "BACKUP", "UNKNOWN"]
        cnts = [0, 0, 0]

        result = "UNKNOWN"
        for router in routers:
            if router.state == "Running" and router.isredundantrouter:
                hosts = list_hosts(self.apiclient, zoneid=router.zoneid, type='Routing', state='Up', id=router.hostid)
                self.assertEqual(isinstance(hosts, list), True, "Check list host returns a valid list")
                host = hosts[0]

                try:
                    host.user, host.passwd = get_host_credentials(self.config, host.ipaddress)
                    result = str(get_process_status(host.ipaddress, 22, host.user, host.passwd, router.linklocalip, "sh /opt/cloud/bin/checkrouter.sh "))

                except KeyError:
                    self.skipTest("Marvin configuration has no host credentials to check router services")

                if result.count(status_to_check) == 1:
                    cnts[vals.index(status_to_check)] += 1
            else:
                self.logger.debug("Skipping router %s because state is %s and is_redundant is %s", router.linklocalip, router.state, router.isredundantrouter)
                return

        if cnts[vals.index(status_to_check)] != expected_count:
            self.fail("Expected '%s' routers at state '%s', but found '%s'!" % (expected_count, status_to_check, cnts[vals.index(status_to_check)]))
