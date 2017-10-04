from nose.plugins.attrib import attr

from marvin.cloudstackAPI import (
    stopRouter,
    replaceNetworkACLList
)
from marvin.cloudstackTestCase import cloudstackTestCase
from marvin.lib.base import (
    NetworkACL,
    NetworkACLList,
    NATRule,
    PublicIPAddress,
    VirtualMachine,
    Network,
    VPC,
    Account
)
from marvin.lib.common import (
    list_hosts,
    list_routers,
    get_network_acl,
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


class TestPublicIpAcl(cloudstackTestCase):

    @classmethod
    def setUpClass(cls):
        cls.logger = MarvinLog(MarvinLog.LOGGER_TEST).get_logger()

        cls.test_client = super(TestPublicIpAcl, cls).getClsTestClient()
        cls.api_client = cls.test_client.getApiClient()
        cls.attributes = cls.test_client.getParsedTestDataConfig()
        cls.class_cleanup = []

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

    def test_acls(self, first_time_retries=2):
        self.define_acl(self.default_allow_acl)
        self.test_connectivity(retries=first_time_retries)
        self.define_acl(self.default_deny_acl)
        self.test_no_connectivity()
        self.define_custom_acl('acl1', 'entry1')
        self.test_connectivity()
        self.define_custom_acl('acl2', 'entry2')
        self.test_no_connectivity()
        self.define_acl(self.default_allow_acl)
        self.test_connectivity()

    @attr(tags=['advanced'])
    def test_01(self):

        self.setup_infra(redundant=False)
        self.test_acls(first_time_retries=10)

    @attr(tags=['advanced'])
    def test_02(self):

        self.cleanup_vpc()
        self.test_acls()

    @attr(tags=['advanced'])
    def test_03(self):

        self.setup_infra(redundant=True)
        self.test_acls(first_time_retries=10)

    @attr(tags=['advanced'])
    def test_04(self):

        self.cleanup_vpc()
        self.test_acls()

    @attr(tags=['advanced'])
    def test_05(self):

        self.stop_master_router(self.vpc1)
        self.test_acls()

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

        cls.default_allow_acl = get_network_acl(cls.api_client, 'default_allow')
        cls.logger.debug("ACL '%s' selected", cls.default_allow_acl.name)

        cls.default_deny_acl = get_network_acl(cls.api_client, 'default_deny')
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

    def test_connectivity(self, retries=2):

        try:
            self.vm1.get_ssh_client(ipaddress=self.public_ip1.ipaddress.ipaddress, reconnect=True, retries=retries)
            self.logger.debug('Ensure connectivity: OK')

        except Exception as e:
            raise Exception("Exception: %s" % e)

    def test_no_connectivity(self):

        failed = False
        try:
            self.vm1.get_ssh_client(ipaddress=self.public_ip1.ipaddress.ipaddress, reconnect=True, retries=2)

        except Exception as e:
            self.logger.debug('Ensure no connectivity: OK')
            failed = True

        self.assertTrue(failed)

    def cleanup_vpc(self):

        self.logger.debug("Restarting VPC '%s' with 'cleanup=True'", self.vpc1.name)
        self.vpc1.restart(self.api_client, True)
        self.logger.debug("VPC '%s' restarted", self.vpc1.name)

    def define_acl(self, acl):

        try:
            command = replaceNetworkACLList.replaceNetworkACLListCmd()
            command.aclid = acl.id
            command.publicipid = self.public_ip1.ipaddress.id
            response = self.api_client.replaceNetworkACLList(command)

        except Exception as e:
            raise Exception("Exception: %s" % e)

        self.assertTrue(response.success)
        self.logger.debug("Public IP '%s' ACL replaced with '%s'", self.public_ip1.ipaddress.ipaddress, acl.name)

    def define_custom_acl(self, acl_config, acl_entry_config):

        acl = NetworkACLList.create(self.api_client,
            self.attributes['acls'][acl_config],
            vpcid=self.vpc1.id)

        NetworkACL.create(self.api_client,
            self.attributes['acls'][acl_config]['entries'][acl_entry_config],
            networkid=self.network1.id,
            aclid=acl.id)

        self.define_acl(acl)

    def stop_master_router(self, vpc):

        self.logger.debug("Stopping Master Router of VPC '%s'...", vpc.name)
        routers = list_routers(self.api_client, domainid=self.domain.id, account=self.account.name, vpcid=vpc.id)
        for router in routers:
            if router.redundantstate == 'MASTER':
                cmd = stopRouter.stopRouterCmd()
                cmd.id = router.id
                cmd.forced = 'true'
                self.api_client.stopRouter(cmd)
                break

        routers = list_routers(self.api_client, domainid=self.domain.id, account=self.account.name, vpcid=vpc.id)
        for router in routers:
            if router.state == 'Running':
                hosts = list_hosts(self.api_client, zoneid=router.zoneid, type='Routing', state='Up', id=router.hostid)
                self.assertTrue(isinstance(hosts, list))
                host = next(iter(hosts or []), None)

                try:
                    host.user, host.passwd = get_host_credentials(self.config, host.ipaddress)
                    get_process_status(host.ipaddress, 22, host.user, host.passwd, router.linklocalip, "sh /opt/cosmic/router/scripts/checkrouter.sh ")

                except KeyError as e:
                    raise Exception("Exception: %s" % e)

        self.logger.debug("Master Router of VPC '%s' stopped", vpc.name)
