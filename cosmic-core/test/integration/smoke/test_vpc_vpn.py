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
""" Tests for VPN in VPC
"""
# Import Local Modules
from marvin.codes import PASS, FAILED
from marvin.cloudstackTestCase import cloudstackTestCase
from marvin.lib.utils import (validateList,
                              cleanup_resources,
                              get_process_status)

from marvin.lib.base import (Domain,
                             Account,
                             Configurations,
                             VPC,
                             VpcOffering,
                             ServiceOffering,
                             NetworkOffering,
                             Network,
                             PublicIPAddress,
                             NATRule,
                             NetworkACL,
                             NetworkACLList,
                             LoadBalancerRule,
                             ApplicationLoadBalancer,
                             VirtualMachine,
                             Template,
                             FireWallRule,
                             StaticNATRule,
                             Vpn,
                             VpnCustomerGateway,
                             VpnUser
                             )

from marvin.sshClient import SshClient


from marvin.lib.common import (get_zone,
                               get_domain,
                               get_template,
                               list_hosts,
                               list_network_offerings,
                               list_routers)

from nose.plugins.attrib import attr

import logging
import time
import copy


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
            "host1": None,
            "host2": None,
            "compute_offering": {
                "name": "Tiny Instance",
                "displaytext": "Tiny Instance",
                "cpunumber": 1,
                "cpuspeed": 100,
                "memory": 128,
            },
            "network_offering": {
                "name": 'VPC Network offering',
                "displaytext": 'VPC Network',
                "guestiptype": 'Isolated',
                "supportedservices": 'Vpn,Dhcp,Dns,SourceNat,Lb,PortForwarding,UserData,StaticNat,NetworkACL',
                "traffictype": 'GUEST',
                "availability": 'Optional',
                "useVpc": 'on',
                "serviceProviderList": {
                    "Vpn": 'VpcVirtualRouter',
                    "Dhcp": 'VpcVirtualRouter',
                    "Dns": 'VpcVirtualRouter',
                    "SourceNat": 'VpcVirtualRouter',
                    "Lb": 'VpcVirtualRouter',
                    "PortForwarding": 'VpcVirtualRouter',
                    "UserData": 'VpcVirtualRouter',
                    "StaticNat": 'VpcVirtualRouter',
                    "NetworkACL": 'VpcVirtualRouter'
                },
            },
            "network_offering_internal_lb": {
                "name": 'VPC Network Internal Lb offering',
                "displaytext": 'VPC Network internal lb',
                "guestiptype": 'Isolated',
                "supportedservices": 'Dhcp,Dns,SourceNat,PortForwarding,UserData,StaticNat,NetworkACL,Lb',
                "traffictype": 'GUEST',
                "availability": 'Optional',
                "useVpc": 'on',
                "serviceCapabilityList": {
                    "Lb": {
                        "SupportedLbIsolation": 'dedicated',
                        "lbSchemes": 'internal'
                    }
                },
                "serviceProviderList": {
                    "Dhcp": 'VpcVirtualRouter',
                    "Dns": 'VpcVirtualRouter',
                    "SourceNat": 'VpcVirtualRouter',
                    "PortForwarding": 'VpcVirtualRouter',
                    "UserData": 'VpcVirtualRouter',
                    "StaticNat": 'VpcVirtualRouter',
                    "NetworkACL": 'VpcVirtualRouter',
                    "Lb": 'InternalLbVm'
                },
                "egress_policy": "true",
            },
            "vpc_offering": {
                "name": 'VPC off',
                "displaytext": 'VPC off',
                "supportedservices": 'Dhcp,Dns,SourceNat,PortForwarding,Vpn,Lb,UserData,StaticNat',
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
            },
            "template": {

                "kvm": {
                    "name": "tiny-kvm",
                    "displaytext": "macchinina kvm",
                    "format": "qcow2",
                    "hypervisor": "kvm",
                    "ostype": "Other PV (64-bit)",
                    "url": "http://dl.openvm.eu/cloudstack/macchinina/x86_64/macchinina-kvm.qcow2.bz2",
                    "requireshvm": "True",
                },

                "xenserver": {
                    "name": "tiny-xen",
                    "displaytext": "macchinina xen",
                    "format": "vhd",
                    "hypervisor": "xen",
                    "ostype": "Other PV (64-bit)",
                    "url": "http://dl.openvm.eu/cloudstack/macchinina/x86_64/macchinina-xen.vhd.bz2",
                    "requireshvm": "True",
                },

                "hyperv": {
                    "name": "tiny-hyperv",
                    "displaytext": "macchinina xen",
                    "format": "vhd",
                    "hypervisor": "hyperv",
                    "ostype": "Other PV (64-bit)",
                    "url": "http://dl.openvm.eu/cloudstack/macchinina/x86_64/macchinina-hyperv.vhd.zip",
                    "requireshvm": "True",
                },

                "vmware": {
                    "name": "tiny-vmware",
                    "displaytext": "macchinina vmware",
                    "format": "ova",
                    "hypervisor": "vmware",
                    "ostype": "Other PV (64-bit)",
                    "url": "http://dl.openvm.eu/cloudstack/macchinina/x86_64/macchinina-vmware.vmdk.bz2",
                    "requireshvm": "True",
                }
            }
        }


class TestVpcRemoteAccessVpn(cloudstackTestCase):

    @classmethod
    def setUpClass(cls):

        cls.logger = logging.getLogger('TestVPCRemoteAccessVPN')
        cls.stream_handler = logging.StreamHandler()
        cls.logger.setLevel(logging.DEBUG)
        cls.logger.addHandler(cls.stream_handler)

        testClient = super(TestVpcRemoteAccessVpn, cls).getClsTestClient()
        cls.apiclient = testClient.getApiClient()
        cls.services = Services().services

        cls.zone = get_zone(cls.apiclient, testClient.getZoneForTests())
        cls.domain = get_domain(cls.apiclient)

        cls.compute_offering = ServiceOffering.create(
            cls.apiclient,
            cls.services["compute_offering"]
        )
        cls.account = Account.create(
            cls.apiclient, services=cls.services["account"])

        cls.hypervisor = testClient.getHypervisorInfo()

        cls.logger.debug("Downloading Template: %s from: %s" % (cls.services["template"][
                         cls.hypervisor.lower()], cls.services["template"][cls.hypervisor.lower()]["url"]))
        cls.template = Template.register(cls.apiclient, cls.services["template"][cls.hypervisor.lower(
        )], cls.zone.id, hypervisor=cls.hypervisor.lower(), account=cls.account.name, domainid=cls.domain.id)
        cls.template.download(cls.apiclient)

        if cls.template == FAILED:
            assert False, "get_template() failed to return template"

        cls.logger.debug("Successfully created account: %s, id: \
                   %s" % (cls.account.name,
                          cls.account.id))

        cls._cleanup = [cls.template, cls.account, cls.compute_offering]
        return

    def setUp(self):
        self.apiclient = self.testClient.getApiClient()
        self.cleanup = []

    @attr(tags=["advanced"], required_hardware="true")
    def test_01_vpc_remote_access_vpn(self):
        """Test Remote Access VPN in VPC"""

        self.logger.debug("Starting test: test_01_vpc_remote_access_vpn")

        # 0) Get the default network offering for VPC
        self.logger.debug("Retrieving default VPC offering")
        networkOffering = NetworkOffering.list(
            self.apiclient, name="DefaultIsolatedNetworkOfferingForVpcNetworks")
        self.assert_(networkOffering is not None and len(
            networkOffering) > 0, "No VPC based network offering")

        # 1) Create VPC
        vpcOffering = VpcOffering.list(self.apiclient, isdefault=True)
        self.assert_(vpcOffering is not None and len(
            vpcOffering) > 0, "No VPC offerings found")

        try:
            vpc = VPC.create(
                apiclient=self.apiclient,
                services=self.services["vpc"],
                networkDomain="vpc.vpn",
                vpcofferingid=vpcOffering[0].id,
                zoneid=self.zone.id,
                account=self.account.name,
                domainid=self.domain.id
            )
        except Exception as e:
            self.fail(e)
        finally:
            self.assert_(vpc is not None, "VPC creation failed")
            self.logger.debug("VPC %s created" % (vpc.id))
        self.cleanup.append(vpc)

        try:
            # 2) Create network in VPC
            ntwk = Network.create(
                apiclient=self.apiclient,
                services=self.services["network_1"],
                accountid=self.account.name,
                domainid=self.domain.id,
                networkofferingid=networkOffering[0].id,
                zoneid=self.zone.id,
                vpcid=vpc.id
            )
        except Exception as e:
            self.fail(e)
        finally:
            self.assertIsNotNone(ntwk, "Network failed to create")
            self.logger.debug(
                "Network %s created in VPC %s" % (ntwk.id, vpc.id))
        self.cleanup.append(ntwk)

        try:
            # 3) Deploy a vm
            vm = VirtualMachine.create(self.apiclient, services=self.services["virtual_machine"],
                                       templateid=self.template.id,
                                       zoneid=self.zone.id,
                                       accountid=self.account.name,
                                       domainid=self.domain.id,
                                       serviceofferingid=self.compute_offering.id,
                                       networkids=ntwk.id,
                                       hypervisor=self.hypervisor
                                       )
            self.assert_(vm is not None, "VM failed to deploy")
            self.assert_(vm.state == 'Running', "VM is not running")
            self.debug("VM %s deployed in VPC %s" % (vm.id, vpc.id))
        except Exception as e:
            self.fail(e)
        finally:
            self.logger.debug("Deployed virtual machine: OK")
        self.cleanup.append(vm)

        try:
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
        except Exception as e:
            self.fail(e)
        finally:
            self.logger.debug("Acquired public ip address: OK")

        try:
            vpn = Vpn.create(self.apiclient,
                             publicipid=ip.id,
                             account=self.account.name,
                             domainid=self.account.domainid,
                             iprange=self.services["vpn"]["iprange"],
                             fordisplay=self.services["vpn"]["fordisplay"]
                             )
        except Exception as e:
            self.fail(e)
        finally:
            self.assertIsNotNone(vpn, "Failed to create Remote Access VPN")
            self.logger.debug("Created Remote Access VPN: OK")

        vpnUser = None
        # 5) Add VPN user for VPC
        try:
            vpnUser = VpnUser.create(self.apiclient,
                                     account=self.account.name,
                                     domainid=self.account.domainid,
                                     username=self.services["vpn"]["vpn_user"],
                                     password=self.services["vpn"]["vpn_pass"]
                                     )
        except Exception as e:
            self.fail(e)
        finally:
            self.assertIsNotNone(
                vpnUser, "Failed to create Remote Access VPN User")
            self.logger.debug("Created VPN User: OK")

        # TODO: Add an actual remote vpn connection test from a remote vpc

        try:
            # 9) Disable VPN for VPC
            vpn.delete(self.apiclient)
        except Exception as e:
            self.fail(e)
        finally:
            self.logger.debug("Deleted the Remote Access VPN: OK")

        self.cleanup.reverse()

    def tearDown(cls):
        try:
            cls.logger.debug("Cleaning up resources")
            cleanup_resources(cls.apiclient, cls.cleanup)
        except Exception, e:
            raise Exception("Cleanup failed with %s" % e)

    @classmethod
    def tearDownClass(cls):
        try:
            cls.apiclient = super(TestVpcRemoteAccessVpn, cls).getClsTestClient().getApiClient()
            cleanup_resources(cls.apiclient, cls._cleanup)
        except Exception as e:
            raise Exception("Warning: Exception during cleanup : %s" % e)
        return

class TestVpcSite2SiteVpn(cloudstackTestCase):

    @classmethod
    def setUpClass(cls):
        cls.logger = logging.getLogger('TestVPCSite2SiteVPN')
        cls.stream_handler = logging.StreamHandler()
        cls.logger.setLevel(logging.DEBUG)
        cls.logger.addHandler(cls.stream_handler)

        testClient = super(TestVpcSite2SiteVpn, cls).getClsTestClient()
        cls.apiclient = testClient.getApiClient()
        cls.services = Services().services

        cls.zone = get_zone(cls.apiclient, testClient.getZoneForTests())
        cls.domain = get_domain(cls.apiclient)

        cls.compute_offering = ServiceOffering.create(
            cls.apiclient,
            cls.services["compute_offering"]
        )

        cls.account = Account.create(
            cls.apiclient, services=cls.services["account"])

        cls.hypervisor = testClient.getHypervisorInfo()

        cls.logger.debug("Downloading Template: %s from: %s" % (cls.services["template"][
                         cls.hypervisor.lower()], cls.services["template"][cls.hypervisor.lower()]["url"]))
        cls.template = Template.register(cls.apiclient, cls.services["template"][cls.hypervisor.lower(
        )], cls.zone.id, hypervisor=cls.hypervisor.lower(), account=cls.account.name, domainid=cls.domain.id)
        cls.template.download(cls.apiclient)

        if cls.template == FAILED:
            assert False, "get_template() failed to return template"

        cls.logger.debug("Successfully created account: %s, id: \
                   %s" % (cls.account.name,
                          cls.account.id))

        cls.networkoffering = NetworkOffering.list(
                cls.apiclient, name="DefaultIsolatedNetworkOfferingForVpcNetworks")
        assert cls.networkoffering is not None and len(
                cls.networkoffering) > 0, "No VPC based network offering"

        cls._cleanup = [cls.template, cls.account, cls.compute_offering]
        return

    def setUp(self):
        self.apiclient = self.testClient.getApiClient()
        self.cleanup = []

    def tearDown(cls):
        try:
            cls.logger.debug("Cleaning up resources")
            cleanup_resources(cls.apiclient, cls.cleanup)
        except Exception, e:
            raise Exception("Cleanup failed with %s" % e)

    @classmethod
    def tearDownClass(cls):
        try:
            cls.apiclient = super(TestVpcSite2SiteVpn, cls).getClsTestClient().getApiClient()
            cleanup_resources(cls.apiclient, cls._cleanup)
        except Exception as e:
            raise Exception("Warning: Exception during cleanup : %s" % e)
        return

    def _validate_vpc_offering(self, vpc_offering):

        self.logger.debug("Check if the VPC offering is created successfully?")
        vpc_offs = VpcOffering.list(
            self.apiclient,
            id=vpc_offering.id
        )
        offering_list = validateList(vpc_offs)
        self.assertEqual(offering_list[0],
                         PASS,
                         "List VPC offerings should return a valid list"
                         )
        self.assertEqual(
            vpc_offering.name,
            vpc_offs[0].name,
            "Name of the VPC offering should match with listVPCOff data"
        )
        self.logger.debug(
            "VPC offering is created successfully - %s" %
            vpc_offering.name)
        return

    def _create_vpc_offering(self, offering_name):

        vpc_off = None
        if offering_name is not None:

            self.logger.debug("Creating VPC offering: %s", offering_name)
            vpc_off = VpcOffering.create(
                self.apiclient,
                self.services[offering_name]
            )

            self._validate_vpc_offering(vpc_off)

        return vpc_off

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
            vpc_n = None
            try:
                # Generate VPC (mostly subnet) info
                vpcservice_n = copy.deepcopy(self.services["vpcN"])
                for key in vpcservice_n.keys():
                    vpcservice_n[key] = vpcservice_n[key].format(N = `i`)

                vpc_n = VPC.create(
                    apiclient=self.apiclient,
                    services=vpcservice_n,
                    networkDomain="vpc%d.vpn" % i,
                    vpcofferingid=vpc_offering.id,
                    zoneid=self.zone.id,
                    account=self.account.name,
                    domainid=self.domain.id
                )
            except Exception as e:
                self.fail(e)
            finally:
                self.assert_(vpc_n is not None, "VPC%d creation failed" % i)

            self.cleanup.insert( 0, vpc_n )
            vpc_list.append( vpc_n )

            self.logger.debug("VPC%d %s created" % (i, vpc_list[i].id))


        default_acl = NetworkACLList.list(
            self.apiclient, name="default_allow")[0]

        # Create network in VPC i
        ntwk_list = []
        for i in range(num_VPCs):
            ntwk_n = None
            try:
                # Generate network (mostly subnet) info
                ntwk_info_n = copy.deepcopy(self.services["network_N"])
                for key in ntwk_info_n.keys():
                    ntwk_info_n[key] = ntwk_info_n[key].format(N = `i`)

                ntwk_n = Network.create(
                    apiclient=self.apiclient,
                    services=ntwk_info_n,
                    accountid=self.account.name,
                    domainid=self.account.domainid,
                    networkofferingid=self.networkoffering[0].id,
                    zoneid=self.zone.id,
                    vpcid=vpc_list[i].id,
                    aclid=default_acl.id
                )
            except Exception as e:
                self.fail(e)
            finally:
                self.assertIsNotNone(ntwk_n, "Network%d failed to create" % i)

            self.cleanup.insert( 0, ntwk_n )
            ntwk_list.append( ntwk_n )
            self.logger.debug("Network%d %s created in VPC %s" % (i, ntwk_list[i].id, vpc_list[i].id))
        
        # Deploy a vm in network i
        vm_list = []
        vm_n = None

        for i in range(num_VPCs):
            try:
                vm_n = VirtualMachine.create(self.apiclient, services=self.services["virtual_machine"],
                                            templateid=self.template.id,
                                            zoneid=self.zone.id,
                                            accountid=self.account.name,
                                            domainid=self.account.domainid,
                                            serviceofferingid=self.compute_offering.id,
                                            networkids=[ntwk_list[i].id],
                                            hypervisor=self.hypervisor,
                                            mode='advanced' if (i == 0) or (i == maxnumVM) else 'default'
                                            )
            except Exception as e:
                self.fail(e)
            finally:
                self.assert_(vm_n is not None, "VM%d failed to deploy" % i)
                self.assert_(vm_n.state == 'Running', "VM%d is not running" % i)

            self.cleanup.insert( 0, vm_n )
            vm_list.append( vm_n )
            self.logger.debug("VM%d %s deployed in VPC %s" % (i, vm_list[i].id, vpc_list[i].id))

        # 4) Enable Site-to-Site VPN for VPC
        vpn_response_list = []
        for i in range(num_VPCs):
            vpn_response = None
            vpn_response = Vpn.createVpnGateway(self.apiclient, vpc_list[i].id)
            self.assert_(
                vpn_response is not None, "Failed to enable VPN Gateway %d" % i)
            self.logger.debug("VPN gateway for VPC%d %s enabled" % (i, vpc_list[i].id))
            vpn_response_list.append( vpn_response )

        # 5) Add VPN Customer gateway info
        vpn_cust_gw_list = []
        services = self.services["vpncustomergateway"]
        for i in range(num_VPCs):
            src_nat_list = None
            src_nat_list = PublicIPAddress.list(
                self.apiclient,
                account=self.account.name,
                domainid=self.account.domainid,
                listall=True,
                issourcenat=True,
                vpcid=vpc_list[i].id
            )
            ip = src_nat_list[0]

            customer_response = None
            customer_response = VpnCustomerGateway.create(
                self.apiclient, services, "Peer VPC" + `i`, ip.ipaddress, vpc_list[i].cidr, self.account.name, self.domain.id)
            self.debug("VPN customer gateway added for VPC%d %s enabled" % (i, vpc_list[i].id))

            self.cleanup.append( customer_response )
            vpn_cust_gw_list.append(customer_response)

        # Before the next step ensure the last VPC is up and running
        # Routers in the right state?
        self.assertEqual(self.routers_in_right_state(vpcid=vpc_list[maxnumVM].id), True,
                         "Check whether the routers are in the right state.")

        
        # 6) Connect VPCi with VPC0
        for i in range(num_VPCs)[1:]:
            vpnconn1_response = Vpn.createVpnConnection(
                self.apiclient, vpn_cust_gw_list[0].id, vpn_response_list[i]['id'], True)
            self.debug("VPN passive connection created for VPC%d %s" % (i, vpc_list[i].id))

            vpnconn2_response = Vpn.createVpnConnection(
                self.apiclient, vpn_cust_gw_list[i].id, vpn_response_list[0]['id'])
            self.debug("VPN connection created for VPC%d %s" % (0, vpc_list[0].id))

            self.assertEqual(
                vpnconn2_response['state'], "Connected", "Failed to connect between VPCs 0 and %d!" % i)
            self.logger.debug("VPN connected between VPC0 and VPC%d" % i)

        # First the last VM
        # setup ssh connection to vm maxnumVM
        self.logger.debug("Setup SSH connection to last VM created (%d) to ensure availability for ping tests" % maxnumVM)
        try:
            ssh_max_client = vm_list[maxnumVM].get_ssh_client(retries=20)
        except Exception as e:
            self.fail(e)
        finally:
            self.assert_(
                ssh_max_client is not None, "Failed to setup SSH to last VM created (%d)" % maxnumVM)

        self.logger.debug("Setup SSH connection to first VM created (0) to ensure availability for ping tests")
        try:
            ssh_client = vm_list[0].get_ssh_client(retries=10)
        except Exception as e:
            self.fail(e)
        finally:
            self.assert_(
                ssh_client is not None, "Failed to setup SSH to VM0")

        if ssh_client:
            # run ping test
            for i in range(num_VPCs)[1:]:
                packet_loss = ssh_client.execute(
                    "/bin/ping -c 3 -t 10 " + vm_list[i].nic[0].ipaddress + " |grep packet|cut -d ' ' -f 7| cut -f1 -d'%'")[0]
                self.assert_(int(packet_loss) == 0, "Ping towards vm" + `i` + "did not succeed")
                self.logger.debug("Ping from vm0 to vm%d did succeed" % i)
        else:
            self.fail("Failed to setup ssh connection to %s" % vm_list[0].public_ip)

        return


    @attr(tags=["advanced"], required_hardware="true")
    def test_01_vpc_site2site_vpn(self):
        """Test Site 2 Site VPN Across VPCs"""
        self.logger.debug("Starting test: test_01_vpc_site2site_vpn")

        # Create and Enable VPC offering
        vpc_offering = self._create_vpc_offering('vpc_offering')
        self.assert_(vpc_offering is not None, "Failed to create VPC Offering")
        vpc_offering.update(self.apiclient, state='Enabled')

        # Set up 1 VPNs; needs 2 VPCs
        self._test_vpc_site2site_vpn( vpc_offering, 2)

        self.cleanup.append(vpc_offering)

    @attr(tags=["advanced"], required_hardware="true")
    def test_02_redundant_vpc_site2site_vpn(self):
        """Test Site 2 Site VPN Across redundant VPCs"""
        self.logger.debug("Starting test: test_02_redundant_vpc_site2site_vpn")

        # Create and enable redundant VPC offering
        redundant_vpc_offering = self._create_vpc_offering(
                'redundant_vpc_offering')
        self.assert_(redundant_vpc_offering is not None,
                     "Failed to create redundant VPC Offering")
        redundant_vpc_offering.update(self.apiclient, state='Enabled')

        # Set up 1 VPNs; needs 2 VPCs
        self._test_vpc_site2site_vpn( redundant_vpc_offering, 2)

        self.cleanup.append(redundant_vpc_offering)

    @attr(tags=["advanced"], required_hardware="true")
    def test_03_vpc_site2site_multiple_vpn(self):
        """Test Site 2 Site multiple VPNs Across VPCs"""
        self.logger.debug("Starting test: test_03_vpc_site2site_multiple_vpn")

        # Create and Enable VPC offering
        vpc_offering = self._create_vpc_offering('vpc_offering')
        self.assert_(vpc_offering is not None, "Failed to create VPC Offering")
        vpc_offering.update(self.apiclient, state='Enabled')

        # Set up 3 VPNs; needs 4 VPCs
        self._test_vpc_site2site_vpn( vpc_offering, 4)

        self.cleanup.append(vpc_offering)