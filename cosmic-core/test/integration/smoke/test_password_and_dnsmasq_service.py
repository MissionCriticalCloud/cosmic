import time

from marvin.utils.MarvinLog import MarvinLog
from nose.plugins.attrib import attr
from marvin.cloudstackTestCase import cloudstackTestCase

from marvin.cloudstackAPI import (
    createNetworkACLList,
    createNetworkACL,
    replaceNetworkACLList,
    restartVPC,
    updateNetwork,
    updateTemplate
)
from marvin.lib.base import (
    Network,
    VirtualMachine,
    VPC,
    Account
)
from marvin.lib.common import (
    list_hosts,
    list_routers,
    get_default_network_offering,
    get_default_redundant_vpc_offering,
    get_default_vpc_offering,
    get_default_virtual_machine_offering,
    get_template,
    get_zone,
    get_domain
)
from marvin.lib.utils import (
    get_process_status,
    cleanup_resources
)


class TestPasswordService(cloudstackTestCase):
    @classmethod
    def setUpClass(cls):
        cls.logger = MarvinLog(MarvinLog.LOGGER_TEST).get_logger()

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
        )

        cls.services["virtual_machine"]["zoneid"] = cls.zone.id
        cls.services["virtual_machine"]["template"] = cls.template.id

        cls.service_offering = get_default_virtual_machine_offering(cls.api_client)
        cls._cleanup = []

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

        self.cleanup = [self.account]
        return

    def tearDown(self):
        try:
            cleanup_resources(self.apiclient, self.cleanup, self.logger)
        except Exception as e:
            raise Exception("Warning: Exception during cleanup : %s" % e)
        return

    @attr(tags=['advanced'])
    def test_01_vpc_password_service_single_vpc(self):
        self.logger.debug("Starting test for single VPC")
        vpc_off = get_default_vpc_offering(self.apiclient)
        self.perform_password_service_tests(vpc_off)

    @attr(tags=['advanced'])
    def test_02_vpc_password_service_redundant_vpc(self):
        self.logger.debug("Starting test for Redundant VPC")
        vpc_off = get_default_redundant_vpc_offering(self.apiclient)
        self.perform_password_service_tests(vpc_off)

    @attr(tags=['advanced'])
    def test_01_network_dns_vpc(self):
        self.logger.debug("Starting test for network dns servers")
        vpc_off = get_default_vpc_offering(self.apiclient)
        self.perform_dnsmasq_service_dns_tests(vpc_off)

    # Generic methods
    def perform_password_service_tests(self, vpc_off):
        self.enable_template_password(
            template_id=self.template.id,
            passwordenabled=True)
        self.logger.debug("Creating VPC with offering ID %s" % vpc_off.id)
        vpc_1 = self.createVPC(vpc_off, cidr='10.0.0.0/16')
        self.logger.debug("Creating network inside VPC")
        net_off = get_default_network_offering(self.apiclient)
        network_1 = self.createNetwork(vpc_1, net_off, gateway='10.0.0.1')
        acl1 = self.createACL(vpc_1)
        self.createACLItem(acl1.id, cidr="0.0.0.0/0")
        self.replaceNetworkAcl(acl1.id, network_1)

        routers = list_routers(self.apiclient, account=self.account.name, domainid=self.account.domainid)
        for router in routers:
            if router.redundantstate == 'MASTER' or len(routers) == 1:
                self._perform_password_service_test(router, network_1)

        # Do the same after restart with cleanup
        self.restart_vpc_with_cleanup(vpc_1, True)

        self.logger.debug("Getting the router info again after the cleanup (router names / ip addresses changed)")
        routers = list_routers(self.apiclient, account=self.account.name, domainid=self.account.domainid)

        self.assertEqual(isinstance(routers, list), True,
                         "Check for list routers response return valid data")
        self.logger.debug("Check whether routers are happy")

        for router in routers:
            if router.redundantstate == 'MASTER' or len(routers) == 1:
                self._perform_password_service_test(router, network_1)
        self.enable_template_password(
            template_id=self.template.id,
            passwordenabled=False)

    def perform_dnsmasq_service_dns_tests(self, vpc_off):
        self.logger.debug("Creating VPC with offering ID %s" % vpc_off.id)
        vpc_1 = self.createVPC(vpc_off, cidr='10.0.0.0/16')
        self.logger.debug("Creating network inside VPC")
        net_off = get_default_network_offering(self.apiclient)
        network_1 = self.createNetwork(vpc_1, net_off, gateway='10.0.0.1')
        acl1 = self.createACL(vpc_1)
        self.createACLItem(acl1.id, cidr="0.0.0.0/0")
        self.replaceNetworkAcl(acl1.id, network_1)

        routers = list_routers(self.apiclient, account=self.account.name, domainid=self.account.domainid)
        for router in routers:
            if router.redundantstate == 'MASTER' or len(routers) == 1:
                self._perform_dnsmasq_service_dns_test(router, network_1)

        # Do the same after restart with cleanup
        self.restart_vpc_with_cleanup(vpc_1, True)

        self.logger.debug("Getting the router info again after the cleanup (router names / ip addresses changed)")
        routers = list_routers(self.apiclient, account=self.account.name, domainid=self.account.domainid)

        self.assertEqual(isinstance(routers, list), True,
                         "Check for list routers response return valid data")
        self.logger.debug("Check whether routers are happy")

        for router in routers:
            if router.redundantstate == 'MASTER' or len(routers) == 1:
                self._perform_dnsmasq_service_dns_test(router, network_1)

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

    def _perform_password_service_test(self, router, network):
        max_tries = 5
        test_tries = 0
        while test_tries < max_tries:
            self.logger.debug("Starting test round %s/%s" % (test_tries, max_tries))
            vm = self.createVM(network)
            self.wait_vm_ready(router, vm.nic[0].ipaddress)
            self.logger.debug("Checking router %s for passwd_server_ip.py process, state %s", router.linklocalip, router.redundantstate)
            self.test_process_running("passwd_server_ip.py", router)
            self.logger.debug("Checking router %s for dnsmasq process, state %s", router.linklocalip, router.redundantstate)
            self.test_process_running("dnsmasq", router)
            self.logger.debug("Checking password of %s in router %s, state %s", vm.name, router.linklocalip, router.redundantstate)
            if self.test_password_server_logs(vm, router):
                self.logger.debug("Test succeeded!")
                return
            self.logger.debug("Test %s failed, retrying (max %s times)" % (test_tries, max_tries))
            vm.get_vm().delete(self.apiclient)
            test_tries += 1
        self.fail('Test _perform_password_service_test failed %s times, giving up.' % test_tries)

    def _perform_dnsmasq_service_dns_test(self, router, network):
        self.logger.debug("Checking router %s for dnsmasq process, state %s", router.linklocalip, router.redundantstate)
        self.test_process_running("dnsmasq", router)

        for dns_servers in self.services["dns_servers"]:
            servers = dns_servers.split(",")
            if len(servers) == 1:
                if servers[0] != "10.0.0.1":
                    self.update_network_dns(network, dns1=servers[0])
            elif len(servers) == 2:
                self.update_network_dns(network, dns1=servers[0], dns2=servers[1])
            else:
                self.fail('Test _perform_dnsmasq_service_dns_test failed, illegal dns server count in services, giving up.')

            if not self.test_dnsmasq_service_dhcp_dns_config(router, dns_servers):
                self.fail('Test _perform_dnsmasq_service_dns_test failed, giving up.')

        self.logger.debug("Test succeeded!")
        self.logger.debug("Resetting DNS entries on network %s" % network.id)
        self.update_network_dns(network)

    def test_process_running(self, find_process, router):
        host = self.get_host_details(router)

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

        self.assertEqual(int(number_of_processes_found[0]), expected_nr_or_processes,
                         msg="Router should have " + str(expected_nr_or_processes) + " '" + find_process + "' processes running, found " + str(number_of_processes_found[0]))

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
        self.logger.debug("Got this response: %s " % command_result)

        # Check to see if our VM is in the password file
        if command_result.count("password saved for VM IP") == 0:
            return False

        # Check if the password was retrieved from the passwd server. If it is, the actual password is replaced with 'saved_password'
        if command_result.count("password sent to") == 0:
            return False

        return True

    def test_dnsmasq_service_dhcp_dns_config(self, router, dns_server):
        host = self.get_host_details(router)

        nic = 'eth3'

        # Get the dnsmasq config entry
        command_to_execute = "grep '%s,6' /etc/dnsmasq.d/%s.conf" % (nic, nic)

        dnsmasq_dns_config_result = ""
        try:
            dnsmasq_dns_config_result = get_process_status(
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

        command_result = str(dnsmasq_dns_config_result)
        self.logger.debug("Got this response: %s " % command_result)

        # Check to see if our VM is in the password file
        if command_result.count(dns_server) == 0:
            return False

        return True

    def get_host_details(self, router):
        hosts = list_hosts(self.apiclient, id=router.hostid, type="Routing")

        self.assertEqual(isinstance(hosts, list), True, "Check for list hosts response return valid data")

        host = hosts[0]
        host.user = self.services["configurableData"]["host"]["username"]
        host.passwd = self.services["configurableData"]["host"]["password"]
        host.port = self.services["configurableData"]["host"]["port"]
        return host

    def createVPC(self, vpc_offering, cidr='10.1.1.1/16'):
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

    def createACLItem(self, aclId, cidr="0.0.0.0/0"):
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

    def createNetwork(self, vpc, network_offering, gateway='10.1.1.1'):
        try:
            self.services["network"]["name"] = "NETWORK-%s" % gateway
            self.logger.debug('Adding Network=%s' % self.services["network"])
            obj_network = Network.create(
                self.apiclient,
                self.services["network"],
                accountid=self.account.name,
                domainid=self.account.domainid,
                networkofferingid=network_offering.id,
                zoneid=self.zone.id,
                gateway=gateway,
                vpcid=vpc.id
            )

            self.logger.debug("Created network with ID: %s" % obj_network.id)
        except Exception, e:
            self.fail('Unable to create a Network with offering=%s because of %s ' % (network_offering.id, e))

        return obj_network

    def replaceNetworkAcl(self, aclId, network):
        self.logger.debug("Replacing Network ACL with ACL ID ==> %s" % aclId)
        replaceNetworkACLListCmd = replaceNetworkACLList.replaceNetworkACLListCmd()
        replaceNetworkACLListCmd.aclid = aclId
        replaceNetworkACLListCmd.networkid = network.id
        self._replaceAcl(replaceNetworkACLListCmd)

    def _replaceAcl(self, command):
        try:
            successResponse = self.apiclient.replaceNetworkACLList(command)
        except Exception as e:
            self.fail("Failed to replace ACL list due to %s" % e)

        self.assertTrue(successResponse.success, "Failed to replace ACL list.")

    def restart_vpc_with_cleanup(self, vpc, cleanup=True):
        try:
            self.logger.debug("Restarting VPC %s with cleanup" % vpc.id)
            cmd = restartVPC.restartVPCCmd()
            cmd.id = vpc.id
            cmd.cleanup = cleanup
            cmd.makeredundant = False
            self.api_client.restartVPC(cmd)
        except Exception, e:
            self.fail('Unable to restart VPC with cleanup due to %s ' % e)

    def update_network_dns(self, network, dns1="", dns2=""):
        try:
            self.logger.debug("Updating Network %s with dns server(s): %s %s" % (network.id, dns1, dns2))
            cmd = updateNetwork.updateNetworkCmd()
            cmd.id = network.id
            cmd.dns1 = dns1
            cmd.dns2 = dns2
            self.api_client.updateNetwork(cmd)
        except Exception, e:
            self.fail('Unable to update Network due to %s ' % e)

    def enable_template_password(self, template_id, passwordenabled=True):
        try:
            self.logger.debug("Updating template %s setting passwordenabled to %s" % (template_id, passwordenabled))
            cmd = updateTemplate.updateTemplateCmd()
            cmd.id = template_id
            cmd.passwordenabled = passwordenabled
            self.api_client.updateTemplate(cmd)
        except Exception, e:
            self.fail('Unable to update template due to %s ' % e)

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
                    "/opt/cosmic/router/scripts/checkrouter.sh | cut -d\" \" -f2"
                )
            except:
                self.logger.debug("Oops, unable to determine redundant state for router with link local address %s" % (router.linklocalip))
                pass
        self.logger.debug("The router with link local address %s reports state %s" % (router.linklocalip, router_state))
        return router_state[0]


class Services:
    """Test network services - password and dnsmasq Test Data Class.
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
            "dns_servers": [
                "10.0.0.1",
                "1.1.1.1",
                "1.1.1.1,2.2.2.2"
            ],
        }
