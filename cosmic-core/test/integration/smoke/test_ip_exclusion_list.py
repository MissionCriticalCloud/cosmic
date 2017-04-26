from nose.plugins.attrib import attr

from marvin.cloudstackTestCase import cloudstackTestCase
from marvin.lib.base import (
    Account,
    VPC,
    Network,
    VirtualMachine,
    PublicIPAddress,
    NATRule
)
from marvin.lib.common import (
    get_zone,
    get_domain,
    get_template,
    get_default_vpc_offering,
    get_default_redundant_vpc_offering,
    get_default_network_offering,
    get_default_virtual_machine_offering,
    get_default_acl
)
from marvin.lib.utils import cleanup_resources
from marvin.utils.MarvinLog import MarvinLog


class TestIpExclusionList(cloudstackTestCase):

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
                'gateway': '10.1.2.1',
                'netmask': '255.255.255.248'
            }
        },
        'vms': {
            'vm1': {
                'name': 'vm1',
                'displayname': 'vm1'
            },
            'vm2': {
                'name': 'vm2',
                'displayname': 'vm2',
                'privateport': 22,
                'publicport': 22,
                'protocol': 'TCP'
            },
            'vm3': {
                'name': 'vm3',
                'displayname': 'vm3',
                'privateport': 22,
                'publicport': 22,
                'protocol': 'TCP'
            }
        },
        'nat_rule': {
            'protocol': 'TCP',
            'publicport': 22,
            'privateport': 22
        },
        'acls': {
            'acl1': {
                'name': 'acl1',
                'description': 'acl1',
                'entries': {
                    'entry1': {
                        'protocol': 'TCP',
                        'action': 'Allow',
                        'traffictype': 'Ingress',
                        'startport': 22,
                        'endport': 22
                    }
                }
            },
            'acl2': {
                'name': 'acl2',
                'description': 'acl2',
                'entries': {
                    'entry2': {
                        'protocol': 'TCP',
                        'action': 'Deny',
                        'traffictype': 'Ingress',
                        'startport': 22,
                        'endport': 22
                    }
                }
            }
        }
    }

    @classmethod
    def setUpClass(cls):
        cls.logger = MarvinLog(MarvinLog.LOGGER_TEST).get_logger()

        cls.test_client = super(TestIpExclusionList, cls).getClsTestClient()
        cls.api_client = cls.test_client.getApiClient()

        cls.class_cleanup = []

    @classmethod
    def setup_infra(cls, redundant=False):

        if len(cls.class_cleanup) > 0:
            cleanup_resources(cls.api_client, cls.class_cleanup, cls.logger)
            cls.class_cleanup = []

        cls.zone = get_zone(cls.api_client, cls.test_client.getZoneForTests())
        cls.logger.debug("Zone '%s' selected" % cls.zone.name)

        cls.domain = get_domain(cls.api_client)
        cls.logger.debug("Domain '%s' selected" % cls.domain.name)

        cls.template = get_template(
            cls.api_client,
            cls.zone.id)
        cls.logger.debug("Template '%s' selected" % cls.template.name)

        cls.account = Account.create(
            cls.api_client,
            cls.attributes['account'],
            admin=True,
            domainid=cls.domain.id)

        cls.class_cleanup += [cls.account]
        cls.logger.debug("Account '%s' created", cls.account.name)

        cls.vpc_offering = get_default_redundant_vpc_offering(cls.api_client) if redundant else get_default_vpc_offering(cls.api_client)
        cls.logger.debug("VPC Offering '%s' selected", cls.vpc_offering.name)

        cls.network_offering = get_default_network_offering(cls.api_client)
        cls.logger.debug("Network Offering '%s' selected", cls.network_offering.name)

        cls.virtual_machine_offering = get_default_virtual_machine_offering(cls.api_client)
        cls.logger.debug("Virtual Machine Offering '%s' selected", cls.virtual_machine_offering.name)

        cls.default_allow_acl = get_default_acl(cls.api_client, 'default_allow')
        cls.logger.debug("ACL '%s' selected", cls.default_allow_acl.name)

        cls.default_deny_acl = get_default_acl(cls.api_client, 'default_deny')
        cls.logger.debug("ACL '%s' selected", cls.default_deny_acl.name)

        cls.vpc1 = VPC.create(cls.api_client,
            cls.attributes['vpcs']['vpc1'],
            vpcofferingid=cls.vpc_offering.id,
            zoneid=cls.zone.id,
            domainid=cls.domain.id,
            account=cls.account.name)
        cls.logger.debug("VPC '%s' created, CIDR: %s", cls.vpc1.name, cls.vpc1.cidr)

        cls.network1 = Network.create(cls.api_client,
            cls.attributes['networks']['network1'],
            networkofferingid=cls.network_offering.id,
            aclid=cls.default_allow_acl.id,
            vpcid=cls.vpc1.id,
            zoneid=cls.zone.id,
            domainid=cls.domain.id,
            accountid=cls.account.name)
        cls.logger.debug("Network '%s' created, CIDR: %s, Gateway: %s", cls.network1.name, cls.network1.cidr, cls.network1.gateway)

        cls.vm1 = VirtualMachine.create(cls.api_client,
            cls.attributes['vms']['vm1'],
            templateid=cls.template.id,
            serviceofferingid=cls.virtual_machine_offering.id,
            networkids=[cls.network1.id],
            zoneid=cls.zone.id,
            domainid=cls.domain.id,
            accountid=cls.account.name)
        cls.logger.debug("VM '%s' created, Network: %s, IP %s", cls.vm1.name, cls.network1.name, cls.vm1.nic[0].ipaddress)

        cls.public_ip1 = PublicIPAddress.create(cls.api_client,
            zoneid=cls.zone.id,
            domainid=cls.account.domainid,
            accountid=cls.account.name,
            vpcid=cls.vpc1.id,
            networkid=cls.network1.id)
        cls.logger.debug("Public IP '%s' acquired, VPC: %s, Network: %s", cls.public_ip1.ipaddress.ipaddress, cls.vpc1.name, cls.network1.name)

        cls.nat_rule1 = NATRule.create(cls.api_client,
            cls.vm1,
            cls.attributes['nat_rule'],
            vpcid=cls.vpc1.id,
            networkid=cls.network1.id,
            ipaddressid=cls.public_ip1.ipaddress.id)
        cls.logger.debug("Port Forwarding Rule '%s (%s) %s => %s' created",
            cls.nat_rule1.ipaddress,
            cls.nat_rule1.protocol,
            cls.nat_rule1.publicport,
            cls.nat_rule1.privateport)

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

    @attr(tags=['advanced'])
    def test_01(self):

        self.setup_infra(redundant=False)
        self.setup_new_network()
        #
        # Deploy and test new VM
        #
        self.vm2 = self.deploy_new_vm('vm2')
        self.test_vm(self.vm2, '10.1.2.6')
        #
        # Try to deploy new VM without free IPs
        # Test failure
        #
        self.vm3 = self.deploy_new_vm('vm3')
        self.test_deploy_new_vm_failed(self.vm3)
        #
        # Update exlcluded IPs range
        # Deploy and test new VM
        #
        self.expand_free_ips()
        self.vm3 = self.deploy_new_vm('vm3')
        self.test_vm(self.vm3, '10.1.2.5')

    def setup_new_network(self):
        self.network2 = Network.create(self.api_client,
                                       self.attributes['networks']['network2'],
                                       networkofferingid=self.network_offering.id,
                                       aclid=self.default_allow_acl.id,
                                       vpcid=self.vpc1.id,
                                       zoneid=self.zone.id,
                                       domainid=self.domain.id,
                                       accountid=self.account.name,
                                       ipexclusionlist='10.1.2.2-10.1.2.5')
        self.logger.debug("Network '%s' created, CIDR: %s, Gateway: %s, Excluded IPs: %s",
                          self.network2.name, self.network2.cidr, self.network2.gateway,
                          self.network2.ipexclusionlist)

    def deploy_new_vm(self, vm_name):
        self.logger.debug('Try to deploy new VM')

        try:
            new_vm = VirtualMachine.create(self.api_client,
                                           self.attributes['vms'][vm_name],
                                           templateid=self.template.id,
                                           serviceofferingid=self.virtual_machine_offering.id,
                                           networkids=[self.network2.id],
                                           zoneid=self.zone.id,
                                           domainid=self.domain.id,
                                           accountid=self.account.name,
                                           mode='advanced')
            self.logger.debug("VM '%s' created, Network: %s, IP %s", new_vm.name, self.network2.name,
                              new_vm.nic[0].ipaddress)
            return new_vm
        except Exception as e:
            if not 'Unable to acquire Guest IP address for network Ntwk' in str(e):
                self.logger.debug('Unexpected Exception: %s', e)
                raise Exception("Exception: %s" % e)
            return None

    def test_deploy_new_vm_failed(self, vm):
        self.assertTrue(vm is None, "VM deployment should fail due to no IPs available")
        self.logger.debug('Check (fail) VM deployment without available IPs: OK')

    def expand_free_ips(self):
        self.network2.update(self.api_client,
                             ipexclusionlist='10.1.2.2-10.1.2.4')
        self.logger.debug('IP list expanded')

    def test_vm(self, vm, expected_value):
        self.assertTrue(vm.nic[0].ipaddress == expected_value, "VM should be assigned the only IP available")
        ssh_client = vm.get_ssh_client(reconnect=True, retries=10)
        result = ssh_client.execute("/sbin/ip addr show")
        self.assertTrue(expected_value in str(result),
                        "VM should implement the only IP available, ip addr show: " + str(result))
        self.logger.debug('Check implemented IP: OK')
