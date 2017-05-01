import socket

from nose.plugins.attrib import attr

from marvin.cloudstackAPI import stopRouter
from marvin.cloudstackTestCase import cloudstackTestCase
from marvin.lib.base import (
    VirtualMachine,
    Account
)
from marvin.lib.common import (
    list_routers,
    list_hosts,
    get_template,
    get_zone,
    get_domain,
    get_default_virtual_machine_offering
)
from marvin.lib.utils import (
    get_process_status,
    cleanup_resources
)
from marvin.utils.MarvinLog import MarvinLog


class TestRouterIpTablesPolicies(cloudstackTestCase):
    @classmethod
    def setUpClass(cls):
        cls.logger = MarvinLog(MarvinLog.LOGGER_TEST).get_logger()

        # We want to fail quicker if it's failure
        socket.setdefaulttimeout(60)

        cls.testClient = super(TestRouterIpTablesPolicies, cls).getClsTestClient()
        cls.apiclient = cls.testClient.getApiClient()

        cls.services = cls.testClient.getParsedTestDataConfig().copy()
        # Get Zone, Domain and templates
        cls.domain = get_domain(cls.apiclient)
        cls.zone = get_zone(cls.apiclient, cls.testClient.getZoneForTests())
        cls.template = get_template(
            cls.apiclient,
            cls.zone.id
        )
        cls.services["vpc"]["cidr"] = '10.1.1.1/16'
        cls.services["virtual_machine"]["zoneid"] = cls.zone.id
        cls.services["virtual_machine"]["template"] = cls.template.id

        cls.account = Account.create(
            cls.apiclient,
            cls.services["account"],
            admin=True,
            domainid=cls.domain.id)

        cls.service_offering = get_default_virtual_machine_offering(cls.apiclient)

        cls.entity_manager = EntityManager(cls.apiclient, cls.services, cls.service_offering, cls.account, cls.zone, cls.logger)

        cls._cleanup = [cls.account]
        return

    @classmethod
    def tearDownClass(cls):
        try:
            cleanup_resources(cls.apiclient, cls._cleanup)
        except Exception as e:
            raise Exception("Warning: Exception during cleanup : %s" % e)
        return

    def setUp(self):
        self.cleanup = []
        self.entity_manager.set_cleanup(self.cleanup)
        return

    def tearDown(self):
        try:
            cleanup_resources(self.apiclient, self.cleanup)
        except Exception as e:
            raise Exception("Warning: Exception during cleanup : %s" % e)
        return

    @attr(tags=['advanced'])
    def test_01_routervm_iptables_policies(self):
        """ Test iptables default INPUT/FORWARD policy on RouterVM """

        self.logger.debug("Starting test_01_routervm_iptables_policies")

        vm1 = self.entity_manager.deployvm()

        routers = self.entity_manager.query_routers()

        self.assertEqual(
            isinstance(routers, list), True,
            "Check for list routers response return valid data")

        for router in routers:
            if not router.isredundantrouter and not router.vpcid:
                hosts = list_hosts(
                    self.apiclient,
                    id=router.hostid)
                self.assertEqual(
                    isinstance(hosts, list),
                    True,
                    "Check for list hosts response return valid data")

                host = hosts[0]
                host.user = self.services["configurableData"]["host"]["username"]
                host.passwd = self.services["configurableData"]["host"]["password"]
                host.port = self.services["configurableData"]["host"]["port"]
                tables = [self.services["configurableData"]["input"], self.services["configurableData"]["forward"]]

                for table in tables:
                    try:
                        result = get_process_status(
                            host.ipaddress,
                            host.port,
                            host.user,
                            host.passwd,
                            router.linklocalip,
                            'iptables -L %s' % table)
                    except KeyError:
                        self.skipTest(
                            "Provide a marvin config file with host\
                                    credentials to run %s" %
                            self._testMethodName)

                    self.logger.debug("iptables -L %s: %s" % (table, result))
                    res = str(result)

                    self.assertEqual(
                        res.count("DROP"),
                        1,
                        "%s Default Policy should be DROP" % table)


class EntityManager(object):
    def __init__(self, apiclient, services, service_offering, account, zone, logger):
        self.apiclient = apiclient
        self.services = services
        self.service_offering = service_offering
        self.account = account
        self.zone = zone
        self.logger = logger

        self.cleanup = []
        self.networks = []
        self.routers = []
        self.ips = []

    def set_cleanup(self, cleanup):
        self.cleanup = cleanup

    def deployvm(self):
        try:
            self.logger.debug('Creating VM')
            vm = VirtualMachine.create(
                self.apiclient,
                self.services["virtual_machine"],
                accountid=self.account.name,
                domainid=self.account.domainid,
                serviceofferingid=self.service_offering.id)

            self.cleanup.insert(0, vm)
            self.logger.debug('Created VM=%s' % vm.id)
            return vm
        except:
            raise Exception('Unable to create VM')

    def query_routers(self):
        self.routers = list_routers(self.apiclient,
                                    account=self.account.name,
                                    domainid=self.account.domainid)

        return self.routers

    def stop_router(self, router):
        self.logger.debug('Stopping router')
        cmd = stopRouter.stopRouterCmd()
        cmd.id = router.id
        self.apiclient.stopRouter(cmd)
