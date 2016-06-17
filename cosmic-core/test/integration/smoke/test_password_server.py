# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

""" Tests for Password Service
"""
#Import Local Modules
from marvin.cloudstackTestCase import *
from marvin.cloudstackAPI import *
from marvin.lib.utils import *
from marvin.lib.base import *
from marvin.lib.common import *
from nose.plugins.attrib import attr
import time
import logging

class Services:
    """Test network services - Port Forwarding Rules Test Data Class.
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

class TestPasswordService(cloudstackTestCase):

    @classmethod
    def setUpClass(cls):

        cls.testClient = super(TestPasswordService, cls).getClsTestClient()
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

        cls.logger = logging.getLogger('TestPasswordService')
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
    def test_01_vpc_password_service_single_vpc(self):
        self.logger.debug("Starting test for single VPC")
        vpc_off = VpcOffering.create(
            self.apiclient,
            self.services["vpc_offering"])

        self.logger.debug("Enabling the VPC offering created")
        vpc_off.update(self.apiclient, state='Enabled')

        self.perform_password_service_tests(vpc_off)

    @attr(tags=["advanced"], required_hardware="true")
    def test_02_vpc_password_service_redundant_vpc(self):
        self.logger.debug("Starting test for Redundant VPC")
        vpc_off = VpcOffering.create(
            self.apiclient,
            self.services["redundant_vpc_offering"])

        self.logger.debug("Enabling the VPC offering created")
        vpc_off.update(self.apiclient, state='Enabled')

        self.perform_password_service_tests(vpc_off)

    @attr(tags=["advanced"], required_hardware="true")
    def test_03_password_service_isolated(self):
        self.logger.debug("Starting test for Isolated network")
        self.perform_password_service_tests(None)

    # Generic methods
    def perform_password_service_tests(self, vpc_off):

        if vpc_off is None:
            self.logger.debug("No need to create a VPC, creating isolated network")
            network_1 = self.createIsolatedNetwork()
            vpc_1 = None
        else:
            self.logger.debug("Creating VPC with offering ID %s" % vpc_off.id)
            vpc_1 = self.createVPC(vpc_off, cidr = '10.0.0.0/16')
            self.cleanup += [vpc_1, vpc_off, self.account]
            self.logger.debug("Creating network inside VPC")
            network_1 = self.createNetwork(vpc_1, gateway = '10.0.0.1')
            acl1 = self.createACL(vpc_1)
            self.createACLItem(acl1.id, cidr = "0.0.0.0/0")
            self.replaceNetworkAcl(acl1.id, network_1)

        # VM
        vm1 = self.createVM(network_1)
        self.cleanup.insert(0, vm1)
        vm2 = self.createVM(network_1)
        self.cleanup.insert(0, vm2)

        # Routers in the right state?
        self.assertEqual(self.routers_in_right_state(), True,
                         "Check whether the routers are in the right state.")

        routers = list_routers(self.apiclient, account=self.account.name, domainid=self.account.domainid)
        for router in routers:
            self._perform_password_service_test(router, vm2)

        # Do the same after restart with cleanup
        if vpc_off is None:
            self.restart_network_with_cleanup(network_1, True)
        else:
            self.restart_vpc_with_cleanup(vpc_1, True)

        self.logger.debug("Getting the router info again after the cleanup (router names / ip addresses changed)")
        routers = list_routers(self.apiclient, account=self.account.name, domainid=self.account.domainid)

        self.assertEqual(isinstance(routers, list), True,
                         "Check for list routers response return valid data")
        self.logger.debug("Check whether routers are happy")

        vm3 = self.createVM(network_1)
        self.cleanup.insert(0, vm3)

        # Routers in the right state?
        self.assertEqual(self.routers_in_right_state(), True,
                         "Check whether the routers are in the right state.")

        for router in routers:
            self._perform_password_service_test(router, vm3)

    def wait_vm_ready(self, router, vmip):
        self.logger.debug("Check whether VM %s is up" % vmip)
        max_tries = 15
        test_tries = 0
        ping_result = 0
        host = self.get_host_details(router)

        while test_tries < max_tries:
            try:
                ping_result = get_process_status(
                    host.ipaddress,
                    host.port,
                    host.user,
                    host.passwd,
                    router.linklocalip,
                    "ping -c 1 " + vmip + ">/dev/null; echo $?"
                )
                # Return value 0 means we were able to ping
                if int(ping_result[0]) == 0:
                    self.logger.debug("VM %s is pingable, give it 10s to request the password" % vmip)
                    time.sleep(10)
                    return True

            except KeyError:
                self.skipTest("Provide a marvin config file with host credentials to run %s" % self._testMethodName)

            self.logger.debug("Ping result from the Router on IP '%s' is -> '%s'" % (router.linklocalip, ping_result[0]))

            test_tries += 1
            self.logger.debug("Executing vm ping %s/%s" % (test_tries, max_tries))
            time.sleep(5)
        return False


    def routers_in_right_state(self):
        self.logger.debug("Check whether routers are happy")
        max_tries = 30
        test_tries = 0
        master_found = 0
        backup_found = 0
        while test_tries < max_tries:
            routers = list_routers(self.apiclient, account=self.account.name, domainid=self.account.domainid)
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

    def _perform_password_service_test(self, router, vm):
        self.wait_vm_ready(router, vm.nic[0].ipaddress)
        self.logger.debug("Checking router %s for passwd_server_ip.py process, state %s", router.linklocalip, router.redundantstate)
        self.test_process_running("passwd_server_ip.py", router)
        self.logger.debug("Checking router %s for dnsmasq process, state %s", router.linklocalip, router.redundantstate)
        self.test_process_running("dnsmasq", router)
        self.logger.debug("Checking password of %s in router %s, state %s", vm.name, router.linklocalip, router.redundantstate)
        self.test_password_server_logs(vm, router)

    def test_process_running(self, find_process, router):
        host = self.get_host_details(router)

        router_state = self.get_router_state(router)

        number_of_processes_found = 0
        try:
            number_of_processes_found = get_process_status(
                host.ipaddress,
                host.port,
                host.user,
                host.passwd,
                router.linklocalip,
                "ps aux | grep " + find_process + " | grep -v grep | wc -l"
            )

        except KeyError:
            self.skipTest("Provide a marvin config file with host credentials to run %s" % self._testMethodName)

        self.logger.debug("Result from the Router on IP '%s' is -> Number of processess found: '%s'" % (router.linklocalip, number_of_processes_found[0]))

        expected_nr_or_processes = 1
        if router.isredundantrouter and router_state == "BACKUP":
            expected_nr_or_processes = 0

        self.assertEqual(int(number_of_processes_found[0]), expected_nr_or_processes, msg="Router should have " + str(expected_nr_or_processes) + " '" + find_process + "' processes running, found " + str(number_of_processes_found[0]))

    def test_password_server_logs(self, vm, router):
        host = self.get_host_details(router)

        router_state = self.get_router_state(router)

        if router.isredundantrouter and router_state != "MASTER":
            print "Found router in non-MASTER state '" + router.redundantstate + "' so skipping test."
            return True

        # Get the related passwd server logs for our vm
        command_to_execute = "grep %s /var/log/messages" % vm.nic[0].ipaddress

        password_log_result = ""
        try:
            password_log_result = get_process_status(
                host.ipaddress,
                host.port,
                host.user,
                host.passwd,
                router.linklocalip,
                command_to_execute)
        except KeyError:
            self.skipTest(
                "Provide a marvin config file with host\
                        credentials to run %s" %
                self._testMethodName)

        command_result = str(password_log_result)

        # Check to see if our VM is in the password file
        self.assertGreater(
            command_result.count("password saved for VM IP"),
            0,
            "Log line 'password saved for VM IP' not found, password was not saved.")

        # Check if the password was retrieved from the passwd server. If it is, the actual password is replaced with 'saved_password'
        self.assertGreater(
            command_result.count("password sent to"),
            0,
            "Log line 'password sent to' not found. The password was not retrieved by the VM!")


    def get_host_details(self, router):
        hosts = list_hosts(self.apiclient, id=router.hostid, type="Routing")

        self.assertEqual(isinstance(hosts, list), True, "Check for list hosts response return valid data")

        host = hosts[0]
        host.user = self.services["configurableData"]["host"]["username"]
        host.passwd = self.services["configurableData"]["host"]["password"]
        host.port = self.services["configurableData"]["host"]["port"]
        return host

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

    def get_router_state(self, router):
        host = self.get_host_details(router)

        router_state = "UNKNOWN"
        if router.isredundantrouter:
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
