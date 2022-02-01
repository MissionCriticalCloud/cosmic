from cosmic.base import *
from cosmic.common import *
from cosmic.cosmicLog import CosmicLog
from cosmic.cosmicTestCase import cosmicTestCase


class TestVmMigration(cosmicTestCase):
    @classmethod
    def setUpClass(cls, redundant=False):
        cls.class_cleanup = []
        cls.logger = CosmicLog(CosmicLog.LOGGER_TEST).get_logger()

        cls.testClient = super(TestVmMigration, cls).getClsTestClient()
        cls.apiclient = cls.testClient.getApiClient()
        cls.services = cls.testClient.getParsedTestDataConfig()

        # Get Zone, Domain and templates
        cls.domain = get_domain(cls.apiclient)
        cls.zone = get_zone(cls.apiclient, cls.testClient.getZoneForTests())
        cls.template = get_template(
            cls.apiclient,
            cls.zone.id,
        )

        cls.vpc_offering = get_default_redundant_vpc_offering(cls.apiclient) if redundant else get_default_vpc_offering(cls.apiclient)
        cls.logger.debug("VPC Offering '%s' selected", cls.vpc_offering.name)

        cls.network_offering = get_default_network_offering(cls.apiclient)
        cls.logger.debug("Network Offering '%s' selected", cls.network_offering.name)

        cls.virtual_machine_offering = get_default_virtual_machine_offering(cls.apiclient)
        cls.logger.debug("Virtual Machine Offering '%s' selected", cls.virtual_machine_offering.name)

        cls.hosts = Host.list(cls.apiclient, listall=True, type="Routing")
        cls.logger.debug("Creating Admin Account for Domain ID ==> %s" % cls.domain.id)
        cls.account = Account.create(
            cls.apiclient,
            cls.services["account"],
            admin=True,
            domainid=cls.domain.id)
        cls.class_cleanup.append(cls.account)

        vpc = VPC.create(
            api_client=cls.apiclient,
            services=cls.services["vpc"],
            networkDomain="vpc.vpn",
            vpcofferingid=cls.vpc_offering.id,
            zoneid=cls.zone.id,
            account=cls.account.name,
            domainid=cls.domain.id
        )
        wait_vpc_ready(vpc)

        cls.logger.debug("VPC %s created" % vpc.id)

        ntwk = Network.create(
            api_client=cls.apiclient,
            services=cls.services["network_1"],
            accountid=cls.account.name,
            domainid=cls.domain.id,
            networkofferingid=cls.network_offering.id,
            zoneid=cls.zone.id,
            vpcid=vpc.id
        )
        cls.logger.debug("Network %s created in VPC %s" % (ntwk.id, vpc.id))

        cls.vm = VirtualMachine.create(cls.apiclient, services=cls.services["virtual_machine"],
                                       templateid=cls.template.id,
                                       zoneid=cls.zone.id,
                                       accountid=cls.account.name,
                                       domainid=cls.domain.id,
                                       serviceofferingid=cls.virtual_machine_offering.id,
                                       networkids=ntwk.id
                                       )
        cls.logger.debug("VM %s deployed in VPC %s" % (cls.vm.id, vpc.id))
        cls.logger.debug("Deployed virtual machine: OK")

    @classmethod
    def tearDownClass(cls):
        try:
            cleanup_resources(cls.apiclient, cls.class_cleanup, cls.logger)
        except Exception as e:
            raise Exception("Warning: Exception during cleanup : %s" % e)
        return

    def setUp(self):
        self.method_cleanup = []

    def tearDown(self):
        try:
            cleanup_resources(self.apiclient, self.method_cleanup, self.logger)
        except Exception as e:
            raise Exception("Warning: Exception during cleanup : %s" % e)
        return

    def test_01_live_migrate(self):
        hv_to_migrate_to = self.get_dest_hypervisor()[0].id
        self.logger.debug("Migrate VM %s from HV %s to HV %s" % (self.vm.id, self.vm.hostid, hv_to_migrate_to))
        self.vm = self.vm.migrate(self.apiclient, hostid=hv_to_migrate_to)
        self.assertEqual(self.vm.hostid, hv_to_migrate_to, "VM not migrate to HV %s" % hv_to_migrate_to)

    def test_02_migrate_back(self):
        vm = self.vm.list(self.apiclient, id=self.vm.id)[0]
        hv_to_migrate_to = self.get_dest_hypervisor(vm.hostid)[0].id
        self.logger.debug("Migrate VM %s from HV %s to HV %s" % (self.vm.id, self.vm.hostid, hv_to_migrate_to))
        self.vm.migrate(self.apiclient, hostid=hv_to_migrate_to)
        self.assertEqual(self.vm.hostid, hv_to_migrate_to, "VM not migrate to HV %s" % hv_to_migrate_to)

    def get_dest_hypervisor(self, hostid=None):
        if hostid is None:
            hostid = self.vm.hostid
        return list(filter(lambda x: x.id != hostid, self.hosts))
