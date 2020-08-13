from marvin.cloudstackAPI import (
    replaceNetworkACLList
)
from marvin.cloudstackTestCase import cloudstackTestCase
from marvin.codes import FAILED
from marvin.lib.base import (
    Account,
    VirtualMachine,
    PublicIPAddress,
    LoadBalancerRule,
    VPC,
    Network
)
from marvin.lib.common import (
    get_domain,
    get_zone,
    get_template,
    list_lb_rules,
    list_lb_instances,
    get_default_virtual_machine_offering,
    get_default_network_offering,
    get_default_vpc_offering,
    get_network_acl
)
from marvin.lib.utils import cleanup_resources
from marvin.utils.MarvinLog import MarvinLog
from marvin.utils.SshClient import SshClient
from nose.plugins.attrib import attr


class TestLoadBalance(cloudstackTestCase):
    @classmethod
    def setUpClass(cls):
        cls.logger = MarvinLog(MarvinLog.LOGGER_TEST).get_logger()
        testClient = super(TestLoadBalance, cls).getClsTestClient()
        cls.apiclient = testClient.getApiClient()
        cls.services = testClient.getParsedTestDataConfig()

        # Get Zone, Domain and templates
        cls.domain = get_domain(cls.apiclient)
        cls.zone = get_zone(cls.apiclient, testClient.getZoneForTests())
        cls.template = get_template(
            cls.apiclient,
            cls.zone.id
        )
        if cls.template == FAILED:
            assert False, "get_template() failed to return template with description %s" % cls.services["ostype"]

        cls.services["virtual_machine"]["zoneid"] = cls.zone.id

        # Create an account, network, VM and IP addresses
        cls.account = Account.create(
            cls.apiclient,
            cls.services["account"],
            admin=True,
            domainid=cls.domain.id
        )
        cls.service_offering = get_default_virtual_machine_offering(cls.apiclient)

        cls.network_offering = get_default_network_offering(cls.apiclient)
        cls.logger.debug("Network Offering '%s' selected", cls.network_offering.name)

        cls.vpc_offering = get_default_vpc_offering(cls.apiclient)
        cls.logger.debug("VPC Offering '%s' selected", cls.vpc_offering.name)

        cls.vpc1 = VPC.create(cls.apiclient,
                              cls.services['vpcs']['vpc1'],
                              vpcofferingid=cls.vpc_offering.id,
                              zoneid=cls.zone.id,
                              domainid=cls.domain.id,
                              account=cls.account.name)
        cls.logger.debug("VPC '%s' created, CIDR: %s", cls.vpc1.name, cls.vpc1.cidr)

        cls.default_allow_acl = get_network_acl(cls.apiclient, 'default_allow')
        cls.logger.debug("ACL '%s' selected", cls.default_allow_acl.name)

        cls.network1 = Network.create(cls.apiclient,
                                      cls.services['networks']['network1'],
                                      networkofferingid=cls.network_offering.id,
                                      aclid=cls.default_allow_acl.id,
                                      vpcid=cls.vpc1.id,
                                      zoneid=cls.zone.id,
                                      domainid=cls.domain.id,
                                      accountid=cls.account.name)
        cls.logger.debug("Network '%s' created, CIDR: %s, Gateway: %s", cls.network1.name, cls.network1.cidr, cls.network1.gateway)

        cls.network2 = Network.create(cls.apiclient,
                                      cls.services['networks']['network2'],
                                      networkofferingid=cls.network_offering.id,
                                      aclid=cls.default_allow_acl.id,
                                      vpcid=cls.vpc1.id,
                                      zoneid=cls.zone.id,
                                      domainid=cls.domain.id,
                                      accountid=cls.account.name)
        cls.logger.debug("Network '%s' created, CIDR: %s, Gateway: %s", cls.network2.name, cls.network2.cidr, cls.network2.gateway)

        cls.vm_1 = VirtualMachine.create(
            cls.apiclient,
            cls.services["virtual_machine"],
            templateid=cls.template.id,
            accountid=cls.account.name,
            domainid=cls.account.domainid,
            serviceofferingid=cls.service_offering.id,
            networkids=[cls.network1.id]
        )
        cls.vm_2 = VirtualMachine.create(
            cls.apiclient,
            cls.services["virtual_machine"],
            templateid=cls.template.id,
            accountid=cls.account.name,
            domainid=cls.account.domainid,
            serviceofferingid=cls.service_offering.id,
            networkids=[cls.network1.id]
        )
        cls.vm_3 = VirtualMachine.create(
            cls.apiclient,
            cls.services["virtual_machine"],
            templateid=cls.template.id,
            accountid=cls.account.name,
            domainid=cls.account.domainid,
            serviceofferingid=cls.service_offering.id,
            networkids=[cls.network1.id]
        )
        cls.vm_4 = VirtualMachine.create(
            cls.apiclient,
            cls.services["virtual_machine"],
            templateid=cls.template.id,
            accountid=cls.account.name,
            domainid=cls.account.domainid,
            serviceofferingid=cls.service_offering.id,
            networkids=[cls.network2.id]
        )
        cls.vm_5 = VirtualMachine.create(
            cls.apiclient,
            cls.services["virtual_machine"],
            templateid=cls.template.id,
            accountid=cls.account.name,
            domainid=cls.account.domainid,
            serviceofferingid=cls.service_offering.id,
            networkids=[cls.network2.id]
        )
        cls.vm_6 = VirtualMachine.create(
            cls.apiclient,
            cls.services["virtual_machine"],
            templateid=cls.template.id,
            accountid=cls.account.name,
            domainid=cls.account.domainid,
            serviceofferingid=cls.service_offering.id,
            networkids=[cls.network2.id]
        )

        cls.non_src_nat_ip_1 = PublicIPAddress.create(cls.apiclient,
                                                      zoneid=cls.zone.id,
                                                      domainid=cls.account.domainid,
                                                      accountid=cls.account.name,
                                                      vpcid=cls.vpc1.id,
                                                      networkid=cls.network1.id)
        cls.logger.debug("Public IP '%s' acquired, VPC: %s, Network: %s", cls.non_src_nat_ip_1.ipaddress.ipaddress, cls.vpc1.name, cls.network1.name)

        cls.non_src_nat_ip_2 = PublicIPAddress.create(cls.apiclient,
                                                      zoneid=cls.zone.id,
                                                      domainid=cls.account.domainid,
                                                      accountid=cls.account.name,
                                                      vpcid=cls.vpc1.id,
                                                      networkid=cls.network2.id)
        cls.logger.debug("Public IP '%s' acquired, VPC: %s, Network: %s", cls.non_src_nat_ip_2.ipaddress.ipaddress, cls.vpc1.name, cls.network2.name)

        command = replaceNetworkACLList.replaceNetworkACLListCmd()
        command.aclid = cls.default_allow_acl.id
        command.publicipid = cls.non_src_nat_ip_1.ipaddress.id
        cls.apiclient.replaceNetworkACLList(command)

        command = replaceNetworkACLList.replaceNetworkACLListCmd()
        command.aclid = cls.default_allow_acl.id
        command.publicipid = cls.non_src_nat_ip_2.ipaddress.id
        cls.apiclient.replaceNetworkACLList(command)

        cls._cleanup = [
            cls.account
        ]

    @classmethod
    def tearDownClass(cls):
        cleanup_resources(cls.apiclient, cls._cleanup)
        return

    def setUp(self):
        self.apiclient = self.testClient.getApiClient()
        self.cleanup = []
        return

    def tearDown(self):
        cleanup_resources(self.apiclient, self.cleanup)
        return

    @attr(tags=['advanced'])
    def _test_01_create_lb_rule_src_nat(self):
        """Test to create Load balancing rule with source NAT"""

        # Validate the Following:
        # 1. listLoadBalancerRules should return the added rule
        # 2. attempt to ssh twice on the load balanced IP
        # 3. verify using the UNAME of the VM
        #   that round robin is indeed happening as expected
        src_nat_ip_addrs = PublicIPAddress.list(
            self.apiclient,
            account=self.account.name,
            domainid=self.account.domainid
        )
        self.assertEqual(
            isinstance(src_nat_ip_addrs, list),
            True,
            "Check list response returns a valid list"
        )
        src_nat_ip_addr_1 = src_nat_ip_addrs[0]
        src_nat_ip_addr_2 = src_nat_ip_addrs[1]

        # Check if VM is in Running state before creating LB rule
        vm_response = VirtualMachine.list(
            self.apiclient,
            account=self.account.name,
            domainid=self.account.domainid
        )

        self.assertEqual(
            isinstance(vm_response, list),
            True,
            "Check list VM returns a valid list"
        )

        self.assertNotEqual(
            len(vm_response),
            0,
            "Check Port Forwarding Rule is created"
        )
        for vm in vm_response:
            self.assertEqual(
                vm.state,
                'Running',
                "VM state should be Running before creating a NAT rule."
            )

        # Create Load Balancer rule and assign VMs to rule
        lb_rule_1 = self.create_lb_rule(src_nat_ip_addr_1.id, self.vpc1.id, self.network1.id, [self.vm_1, self.vm_2])

        # Create Load Balancer rule and assign VMs to rule
        lb_rule_2 = self.create_lb_rule(src_nat_ip_addr_2.id, self.vpc1.id, self.network2.id, [self.vm_4, self.vm_5])

        # listLoadBalancerRuleInstances should list all
        # instances associated with that LB rule
        self.check_lb_rules(lb_rule_1.id, [self.vm_1.id, self.vm_2.id])
        self.check_lb_rules(lb_rule_2.id, [self.vm_4.id, self.vm_5.id])

        uname_results = []
        for x in range(0, 5):
            self.try_ssh(src_nat_ip_addr_1.ipaddress, uname_results)

        self.logger.debug("OUTPUT: %s" % str(uname_results))
        self.assertIn(
            self.vm_1.id.split("-", 3)[3].upper(),
            uname_results,
            "Check if ssh succeeded for server1"
        )
        self.assertIn(
            self.vm_2.id.split("-", 3)[3].upper(),
            uname_results,
            "Check if ssh succeeded for server2"
        )

        uname_results = []
        for x in range(0, 5):
            self.try_ssh(src_nat_ip_addr_2.ipaddress, uname_results)

        self.logger.debug("OUTPUT: %s" % str(uname_results))
        self.assertIn(
            self.vm_4.id.split("-", 3)[3].upper(),
            uname_results,
            "Check if ssh succeeded for server4"
        )
        self.assertIn(
            self.vm_5.id.split("-", 3)[3].upper(),
            uname_results,
            "Check if ssh succeeded for server5"
        )

        uname_results.append(self.remove_and_check(lb_rule_1, src_nat_ip_addr_1, [self.vm_2, self.vm_1]))
        uname_results.append(self.remove_and_check(lb_rule_2, src_nat_ip_addr_2, [self.vm_4, self.vm_5]))
        return

    @attr(tags=['advanced'])
    def _test_02_create_lb_rule_non_nat(self):
        """Test to create Load balancing rule with non source NAT"""

        # Validate the Following:
        # 1. listLoadBalancerRules should return the added rule
        # 2. attempt to ssh twice on the load balanced IP
        # 3. verify using the UNAME of the VM that
        #   round robin is indeed happening as expected

        # Create Load Balancer rule and assign VMs to rule
        lb_rule_1 = self.create_lb_rule(self.non_src_nat_ip_1.ipaddress.id,
                                        self.vpc1.id,
                                        self.network1.id,
                                        [self.vm_1, self.vm_2])

        lb_rule_2 = self.create_lb_rule(self.non_src_nat_ip_2.ipaddress.id,
                                        self.vpc1.id,
                                        self.network2.id,
                                        [self.vm_4, self.vm_5])

        # listLoadBalancerRuleInstances should list
        # all instances associated with that LB rule
        self.check_lb_rules(lb_rule_1.id, [self.vm_1.id, self.vm_2.id])
        self.check_lb_rules(lb_rule_2.id, [self.vm_4.id, self.vm_5.id])
        uname_results = []
        for x in range(0, 5):
            self.try_ssh(self.non_src_nat_ip_1.ipaddress.ipaddress, uname_results)

        self.logger.debug("OUTPUT: %s" % str(uname_results))
        self.assertIn(
            self.vm_1.id.split("-", 3)[3].upper(),
            uname_results,
            "Check if ssh succeeded for server1"
        )
        self.assertIn(
            self.vm_2.id.split("-", 3)[3].upper(),
            uname_results,
            "Check if ssh succeeded for server2"
        )

        uname_results = []
        for x in range(0, 5):
            self.try_ssh(self.non_src_nat_ip_2.ipaddress.ipaddress, uname_results)

        self.logger.debug("OUTPUT: %s" % str(uname_results))
        self.assertIn(
            self.vm_4.id.split("-", 3)[3].upper(),
            uname_results,
            "Check if ssh succeeded for server1"
        )
        self.assertIn(
            self.vm_5.id.split("-", 3)[3].upper(),
            uname_results,
            "Check if ssh succeeded for server2"
        )

        uname_results.append(self.remove_and_check(lb_rule_1, self.non_src_nat_ip_1, [self.vm_1, self.vm_2]))
        uname_results.append(self.remove_and_check(lb_rule_2, self.non_src_nat_ip_2, [self.vm_4, self.vm_5]))
        return

    @attr(tags=['advanced'])
    def test_03_assign_and_removal_lb(self):
        """Test for assign & removing load balancing rule"""

        # Validate:
        # 1. Verify list API - listLoadBalancerRules lists
        #   all the rules with the relevant ports
        # 2. listLoadBalancerInstances will list
        #   the instances associated with the corresponding rule.
        # 3. verify ssh attempts should pass as long as there
        #   is at least one instance associated with the rule

        # Check if VM is in Running state before creating LB rule
        vm_response = VirtualMachine.list(
            self.apiclient,
            account=self.account.name,
            domainid=self.account.domainid
        )

        self.assertEqual(
            isinstance(vm_response, list),
            True,
            "Check list VM returns a valid list"
        )

        self.assertNotEqual(
            len(vm_response),
            0,
            "Check Port Forwarding Rule is created"
        )
        for vm in vm_response:
            self.assertEqual(
                vm.state,
                'Running',
                "VM state should be Running before creating a NAT rule."
            )

        lb_rule_1 = self.create_lb_rule(self.non_src_nat_ip_1.ipaddress.id,
                                        self.vpc1.id,
                                        self.network1.id,
                                        [self.vm_1, self.vm_2])

        lb_rule_2 = self.create_lb_rule(self.non_src_nat_ip_2.ipaddress.id,
                                        self.vpc1.id,
                                        self.network2.id,
                                        [self.vm_4, self.vm_5])

        self.check_lb_rules(lb_rule_1.id, [self.vm_1.id, self.vm_2.id])
        self.check_lb_rules(lb_rule_2.id, [self.vm_4.id, self.vm_5.id])

        uname_results = []
        for x in range(0, 5):
            self.try_ssh(self.non_src_nat_ip_1.ipaddress.ipaddress, uname_results)

        self.logger.debug("OUTPUT: %s" % str(uname_results))
        self.assertIn(
            self.vm_1.id.split("-", 3)[3].upper(),
            uname_results,
            "Check if ssh succeeded for server1"
        )
        self.assertIn(
            self.vm_2.id.split("-", 3)[3].upper(),
            uname_results,
            "Check if ssh succeeded for server2"
        )

        uname_results = []
        for x in range(0, 5):
            self.try_ssh(self.non_src_nat_ip_2.ipaddress.ipaddress, uname_results)

        self.logger.debug("OUTPUT: %s" % str(uname_results))
        self.assertIn(
            self.vm_4.id.split("-", 3)[3].upper(),
            uname_results,
            "Check if ssh succeeded for server4"
        )
        self.assertIn(
            self.vm_5.id.split("-", 3)[3].upper(),
            uname_results,
            "Check if ssh succeeded for server5"
        )

        # Removing VM and assigning another VM to LB rule
        self.remove_add_check(lb_rule_1, self.non_src_nat_ip_1, [self.vm_2, self.vm_3])
        self.remove_add_check(lb_rule_2, self.non_src_nat_ip_2, [self.vm_4, self.vm_6])
        return

    def try_ssh(self, ip_addr, command):
        try:
            self.logger.debug(
                "SSH into VM (IPAddress: %s) & NAT Rule (Public IP: %s)" %
                (self.vm_1.ipaddress, ip_addr)
            )
            # If Round Robin Algorithm is chosen,
            # each ssh command should alternate between VMs

            ssh_1 = SshClient(
                ip_addr,
                self.services['lbrule']["publicport"],
                self.vm_1.username,
                self.vm_1.password,
                retries=10
            )
            command.append(ssh_1.execute("cat /sys/devices/virtual/dmi/id/product_uuid | cut -c20-")[0])
            self.logger.debug(command)
        except Exception as e:
            self.fail("%s: SSH failed for VM with IP Address: %s" %
                      (e, ip_addr))
        return

    def create_lb_rule(self, ipaddr, vpcid, networkid, vms):
        """ Create Loadbalancer rule
        :param ipaddr: IP Address uuid to create LB rule on
        :param vpcid: VPC uuid where to create LB rule
        :param networkid: Network uuid
        :param vms: List of VM's
        :return: loadbalancer rule, loadbalancer rules
        """
        # Create Load Balancer rule and assign VMs to rule
        lb_rule = LoadBalancerRule.create(
            self.apiclient,
            self.services["lbrule"],
            ipaddr,
            accountid=self.account.name,
            vpcid=vpcid,
            networkid=networkid
        )
        self.cleanup.append(lb_rule)
        lb_rule.assign(self.apiclient, vms)
        lb_rules = list_lb_rules(
            self.apiclient,
            id=lb_rule.id
        )
        self.assertEqual(
            isinstance(lb_rules, list),
            True,
            "Check list response returns a valid list"
        )
        # verify listLoadBalancerRules lists the added load balancing rule
        self.assertNotEqual(
            len(lb_rules),
            0,
            "Check Load Balancer Rule in its List"
        )
        self.assertEqual(
            lb_rules[0].id,
            lb_rule.id,
            "Check List Load Balancer Rules returns valid Rule"
        )
        return lb_rule

    def check_lb_rules(self, lb_rule_id, vm_ids):
        """ Check Loadbalancer rules
        :param lb_rule_id: Load balancer uuid
        :param vm_ids: List of VM uuid's
        :return:
        """
        lb_instance_rules = list_lb_instances(
            self.apiclient,
            id=lb_rule_id
        )
        self.assertEqual(
            isinstance(lb_instance_rules, list),
            True,
            "Check list response returns a valid list"
        )
        self.assertNotEqual(
            len(lb_instance_rules),
            0,
            "Check Load Balancer instances Rule in its List"
        )
        self.logger.debug("lb_instance_rules Ids: %s, %s" % (
            lb_instance_rules[0].id,
            lb_instance_rules[1].id
        ))
        self.logger.debug("VM ids: %s" % ", ".join(vm_ids))

        self.assertIn(
            lb_instance_rules[0].id,
            vm_ids,
            "Check List Load Balancer instances Rules returns valid VM ID"
        )

        self.assertIn(
            lb_instance_rules[1].id,
            vm_ids,
            "Check List Load Balancer instances Rules returns valid VM ID"
        )

    def remove_and_check(self, lb_rule, ip_addr, vms):
        """ Remove and check
        This removes the VM from the LB rule and check if it's still possible
        to SSH to the VM

        :param lb_rule: Load balancer rule
        :param ip_addr: IP address
        :param vms: List of VM's
        :return: List with SSH output
        """
        lb_rule.remove(self.apiclient, [vms[0]])

        uname_results = []

        try:
            self.logger.debug("SSHing into IP address: %s after removing VM (ID: %s)" %
                              (
                                  ip_addr.ipaddress.ipaddress,
                                  vms[0].id
                              ))

            self.try_ssh(ip_addr.ipaddress.ipaddress, uname_results)
            self.assertIn(
                vms[1].id.split("-", 3)[3].upper(),
                uname_results,
                "Check if ssh succeeded for server1"
            )
        except Exception as e:
            self.fail("%s: SSH failed for VM with IP Address: %s" %
                      (e, ip_addr.ipaddress.ipaddress))

        lb_rule.remove(self.apiclient, [vms[1]])

        with self.assertRaises(Exception):
            self.logger.debug("Removed all VMs, trying to SSH")
            self.try_ssh(ip_addr.ipaddress.ipaddress, uname_results)

        return uname_results

    def remove_add_check(self, lb_rule, ip_addr, vms):
        """ Remove VM, Add VM and check
        :param lb_rule: Load balancer rule
        :param ip_addr: IP address
        :param vms: List of VM's, first VM will be removed, second one will be added
        :return: List with SSH output
        """
        lb_rule.remove(self.apiclient, [vms[0]])

        results = []

        try:
            self.logger.debug("SSHing into IP address: %s after removing VM (ID: %s)" %
                              (
                                  ip_addr.ipaddress.ipaddress,
                                  vms[0].id
                              ))

            self.try_ssh(ip_addr.ipaddress.ipaddress, results)
            self.assertNotIn(
                vms[0].id.split("-", 3)[3].upper(),
                results,
                "Check if ssh did not succeeded for server %s" % vms[0].id
            )
        except Exception as e:
            self.fail("%s: SSH failed for VM with IP Address: %s" %
                      (e, ip_addr.ipaddress.ipaddress))

        lb_rule.assign(self.apiclient, [vms[1]])

        results[:] = []
        for x in range(0, 5):
            self.try_ssh(ip_addr.ipaddress.ipaddress, results)
        self.logger.debug("OUTPUT: %s" % str(results))
        self.assertIn(
            vms[1].id.split("-", 3)[3].upper(),
            results,
            "Check if ssh succeeded for server %s" % vms[1].id
        )
        return results
